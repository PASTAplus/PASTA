/*
 *
 * $Date$ $Author$ $Revision$
 *
 * Copyright 2010 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.gatekeeper;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.apache.log4j.Logger;

import org.apache.commons.codec.binary.Base64;

import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.auth.KnbAuthSystem;
import edu.lternet.pasta.common.security.auth.SymmetricEncrypter;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.common.security.token.AuthTokenWithPassword;
import edu.lternet.pasta.common.security.token.BasicAuthToken;

/**
 * <p>
 * The Gatekeeper web service handles all authentication from incoming requests.
 * </p>
 *
 * <p>
 * If the user submits only BASIC authentication credentials, a token will be
 * generated and returned upon completion of the requested query.
 * </p>
 *
 * <p>
 * If the user submits a token, the token will be used provided it does not
 * exceed the time to live. In that event, a ServletException is thrown.
 * </p>
 *
 * <p>
 * If no credentials or tokens are submitted, a token for special user public
 * will be created and the remainder of the query will be done as public. The
 * response will return a public token.
 * </p>
 *
 * @webservicename Gatekeeper
 * @baseurl https://pasta.lternet.edu/
 */
public final class GatekeeperFilter implements Filter
{

    private static Logger logger = Logger.getLogger(GatekeeperFilter.class);
    private FilterConfig filterConfig;
    private static int BAD_REQUEST_CODE = 400;
    private static int UNAUTHORIZED_CODE = 401;
    private boolean publicUser = false;
    private Integer contentLength = null;

    private enum CookieUse {
        EXTERNAL, INTERNAL
    }

    /**
     * Overridden init method that sets the filterConfig.
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        filterConfig = config;
    }

    /**
     * Overridden destroy method that free's the filterConfig.
     */
    @Override
    public void destroy() {
        filterConfig = null;
    }

    /**
     * Overridden doFilter method.
     * @param request ServletRequest representing the incoming user http(s)
     *                request.
     * @param request ServletResponse representing the associated response
     *                                that will eventually be passed on to the
     *                                next servlet.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;        
        HttpServletResponse res = (HttpServletResponse) response;

        // Output HttpServletRequest diagnostic information
		    logger.info("Request URL: " + req.getMethod() + " - "
 		    		+ req.getRequestURL().toString());

        doDiagnostics(req);

        try {
            Cookie internalCookie = hasAuthToken(req.getCookies()) ?
                                        doCookie(req) : doHeader(req, res);
            chain.doFilter(new PastaRequestWrapper(req, internalCookie), res);
        }
        catch (IllegalStateException e) {
            res.setStatus(BAD_REQUEST_CODE);
            PrintWriter out = res.getWriter();
            out.println(e);
        }
        catch (UnauthorizedException e) {
            res.setStatus(UNAUTHORIZED_CODE);
            PrintWriter out = res.getWriter();
            out.println(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            res.setStatus(UNAUTHORIZED_CODE);
            PrintWriter out = res.getWriter();
            out.println(e.getMessage());
        }

    }

    /*
     *  Process incoming authentication token
     */
    private Cookie doCookie(HttpServletRequest req)
            throws IllegalArgumentException, IllegalStateException, UnauthorizedException {

        String authToken = null;
        String authTokenStr = retrieveAuthTokenString(req.getCookies());

        if (authTokenStr == null) {
            String gripe = "Authentication token not found!";
            throw new IllegalStateException(gripe);
        } else {

            String[] authTokeStrParts = authTokenStr.split("-");
            authToken = authTokeStrParts[0];
            byte[] signature = Base64.decodeBase64(authTokeStrParts[1]);

            if (!isValidSignature(authToken, signature)) {
                String gripe = "Authentication token is not valid!";
                throw new IllegalStateException(gripe);
            }

        }

        AuthToken token = null;
        token = AuthTokenFactory.makeCookieAuthToken(authToken);
        assertTimeToLive(token);

        return makeAuthTokenCookie(token, CookieUse.INTERNAL);

    }

    /*
     *  Process incoming basic-authentication header or "public" user
     */
    private Cookie doHeader(HttpServletRequest req, HttpServletResponse res) {

        AuthToken authToken =
                makeAuthenticated(req.getHeader(HttpHeaders.AUTHORIZATION));

        // Only return authToken (in cookie) if real user
        if (!publicUser) {
          Cookie externalCookie =
              makeAuthTokenCookie(authToken, CookieUse.EXTERNAL);
          res.addCookie(externalCookie);
        }



        return makeAuthTokenCookie(authToken, CookieUse.INTERNAL);
    }

    private void assertTimeToLive(AuthToken attrlist) throws UnauthorizedException {

        if (attrlist == null) {
            String s = "Token not found.";
            throw new UnauthorizedException(s);
        }
        long ttl = attrlist.getExpirationDate() - (new Date().getTime());
        if (ttl < 1) {
            String s = "Token has expired.";
            throw new UnauthorizedException(s);
        }
    }

    private boolean hasAuthToken(Cookie[] cookies) {
        if (retrieveAuthTokenString(cookies) == null) return false;
        return true;
    }

    private AuthToken decryptToken(String tokenStr) throws IllegalStateException {

        String errorMsg = "Invalid AuthToken Submitted.";

        if (tokenStr == null || tokenStr.isEmpty()) {
            throw new IllegalStateException(errorMsg);
        }

        String decrypted = null;
        try {
            decrypted =
                    SymmetricEncrypter.decrypt(tokenStr,
                                    ConfigurationListener.getPrivateKey());
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException(errorMsg);
        }

        return AuthTokenFactory.makeCookieAuthToken(decrypted);
    }

    private String retrieveAuthTokenString(Cookie[] cookies) {

        /* no cookies */
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (c.getName().equals(ConfigurationListener.getTokenName())) {
                /* found correct cookie */
                return c.getValue();
            }
        }
        return null;
    }

    private AuthToken makeAuthenticated(String rawHeader) {

        String tmpHeader = null;
        if (rawHeader == null || rawHeader.isEmpty()) {
            tmpHeader = BasicAuthToken.makeTokenString(
                    ConfigurationListener.getPublicUser(),
                    ConfigurationListener.getPublicUser());
            publicUser = true;
        }
        else {
            tmpHeader = rawHeader;
            publicUser = false;
        }

        KnbAuthSystem knb = new KnbAuthSystem(ConfigurationListener.getLdapKeyStore());

        AuthTokenWithPassword basicToken =
                AuthTokenFactory.makeAuthTokenWithPassword(tmpHeader);
        String user = basicToken.getUserId();
        String password = basicToken.getPassword();

        Set<String> groups = new HashSet<String>();
        if (!user.equals(ConfigurationListener.getPublicUser())) {

            if (!knb.authenticate(user, password)) {
                String s = "The user '" + user
                        + "' could not be authenticated "
                        + "using the LTER LDAP server.";
                throw new UnauthorizedException(s); // Handle this better
            }
            // groups = knb.getGroups(user); // No groups currently stored here
            groups.add(ConfigurationListener.getAuthGroup());
        }
        AuthSystemDef authSystem = knb.getAuthSystemDef();
        long expirationDate =
                new Date().getTime() + ConfigurationListener.getTokenTtl();
        AuthToken token =
                AuthTokenFactory.makeCookieAuthToken(user, authSystem,
                                                     expirationDate, groups);

        return token;
    }

    private Cookie makeAuthTokenCookie(AuthToken attrlist, CookieUse use) {

        String cookieValue = attrlist.getTokenString();

        if (use == CookieUse.EXTERNAL) {
          // Generate digital signature and add to token string
          byte[] signature = generateSignature(cookieValue);
          cookieValue = cookieValue + "-" + Base64.encodeBase64String(signature);
        }

        logger.debug("Cookie value: " + cookieValue);

        Cookie c = new Cookie(ConfigurationListener.getTokenName(), cookieValue);
        Long expiry = attrlist.getExpirationDate() / 1000L;
        c.setMaxAge(expiry.intValue());
        return c;

    }
    
	/**
	 * dumpHeader iterates through all request headers and lists both the header
	 * name and its contents to the designated logger.
	 * 
	 * @param req
	 *          The HttpServletRequest object.
	 */
	private void dumpHeader(HttpServletRequest req, Boolean noAuthPeek) {
		Enumeration<String> headerNames = req.getHeaderNames();
		String headerName = null;

		String header = null;
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Header: %n"));

		while (headerNames.hasMoreElements()) {

			headerName = headerNames.nextElement();
			header = req.getHeader(headerName);

      if (headerName.equals("Authorization") && noAuthPeek) header = "********";

			if (headerName.equals("Content-Length")) {
				this.contentLength = Integer.valueOf(header);
			}

			sb.append(String.format("     %s: %s%n", headerName, header));

		}

    logger.info(sb.toString());

	}
  
	/**
	 * dumpBody outputs the contents of the request message body to the
	 * designated logger.  Note that the use of this method will render the
	 * request object inoperable for and subsequent calls.
	 * 
	 * @param req The HttpServletRequest object.
	 */
	private void dumpBody(HttpServletRequest req) {

		if (contentLength != null) {

			try {
				BufferedReader br = req.getReader();
				String line = null;

				logger.info("Request message body:\n");

				if (br.markSupported()) {

					br.mark(this.contentLength + 1);

					while ((line = br.readLine()) != null) {
						System.out.println(line);
					}

					br.reset();

				}

				br.close();

			} catch (IOException e) {
				logger.error("dumpBody: " + e);
				e.printStackTrace();
			}

		}

	}

  private void doDiagnostics(HttpServletRequest req) {

    String remoteAddr = req.getRemoteAddr();
    logger.info("Remote address: " + remoteAddr);

    String requestUri = req.getRequestURI();
    logger.info("Request URI: " + requestUri);

    Boolean noAuthPeek = true;

    dumpHeader(req, noAuthPeek);
    //dumpBody(req);

  }

  /*
   * Generate MD5withRSA digital signature for tokenString and return base64
   * encoded signature as a string.
   */
  private byte[] generateSignature(String tokenString) {

    byte[] signature = null;

    File ksFile = ConfigurationListener.getLterKeyStore();
    String ksType = ConfigurationListener.getLterKeyStoreType();
    String ksAlias = ConfigurationListener.getLterKeyStoreAlias();
    char[] storePass = ConfigurationListener.getLterStorePasswd().toCharArray();
    char[] keyPass = ConfigurationListener.getLterKeyPasswd().toCharArray();

    try {

      KeyStore ks = KeyStore.getInstance(ksType);
      FileInputStream ksFis = new FileInputStream(ksFile);
      BufferedInputStream ksBufIn = new BufferedInputStream(ksFis);

      ks.load(ksBufIn, storePass);
      PrivateKey priv = (PrivateKey) ks.getKey(ksAlias, keyPass);

      Signature rsa = Signature.getInstance("MD5withRSA");
      rsa.initSign(priv);

      rsa.update(tokenString.getBytes());
      signature = rsa.sign();

    } catch (Exception e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }

    return signature;

  }

  private void writeSignature(String tokenString, byte[] signature) {

    String signatureDir = ConfigurationListener.getSignatureDir();
    String signatureFile = signatureDir + tokenString;

    FileOutputStream sigFOS = null;

    try {
      sigFOS = new java.io.FileOutputStream(signatureFile);
      sigFOS.write(signature);
      sigFOS.close();
    }
    catch (FileNotFoundException e) {
      logger.error("Gatekeeper.writeSignature: " + e.getMessage());
      e.printStackTrace();
    }
    catch (IOException e) {
      logger.error("Gatekeeper.writeSignature: " + e.getMessage());
      e.printStackTrace();
    }

  }

  private Boolean isValidSignature(String tokenString, byte[] signature) {

    Boolean isValid = false;

    File lterCert = ConfigurationListener.getLterCertificate();

    try {

        FileInputStream certFis = new FileInputStream(lterCert);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert =  (X509Certificate) cf.generateCertificate(certFis);

        PublicKey pubKey = cert.getPublicKey();

        Signature sig = Signature.getInstance("MD5withRSA");
        sig.initVerify(pubKey);

        sig.update(tokenString.getBytes());
        isValid = sig.verify(signature);

    } catch (FileNotFoundException e) {
        logger.error("Gatekeeper.validateSignature :" + e.getMessage());
        e.printStackTrace();
    } catch (CertificateException e) {
        logger.error("Gatekeeper.validateSignature :" + e.getMessage());
        e.printStackTrace();
    }  catch (NoSuchAlgorithmException e) {
        logger.error("Gatekeeper.validateSignature :" + e.getMessage());
        e.printStackTrace();
    } catch (InvalidKeyException e) {
        logger.error("Gatekeeper.validateSignature :" + e.getMessage());
        e.printStackTrace();
    } catch (SignatureException e) {
        logger.error("Gatekeeper.validateSignature :" + e.getMessage());
        e.printStackTrace();
    }

    return isValid;

  }

    public static class PastaRequestWrapper extends HttpServletRequestWrapper
    {

        private static Logger logger = Logger.getLogger(PastaRequestWrapper.class);
        private Cookie cookie;

        public PastaRequestWrapper(HttpServletRequest request, Cookie cookie) {

            super(request);
            this.cookie = cookie;
        }

        public String getHeader(String name) {

            if (name.equals(HttpHeaders.AUTHORIZATION)) return null;
            String header = super.getHeader(name);
            if (name.equals(HttpHeaders.COOKIE) && header.isEmpty()
                    && (cookie != null))
                return cookie.getName();

            return header;
        }

        public Enumeration<String> getHeaders(String name) {

            Enumeration<String> enumStr = super.getHeaders(name);

            if (name.equals(HttpHeaders.AUTHORIZATION)) {
                List<String> ls = new ArrayList<String>();
                enumStr = Collections.enumeration(ls);
            }

            if (!name.equals(HttpHeaders.COOKIE) || (cookie == null))
                return enumStr;

            ArrayList<String> list = Collections.list(enumStr);
            list.add(cookie.getName() + "=" + cookie.getValue());
            return Collections.enumeration(list);
        }

        public Enumeration<String> getHeaderNames() {

            Enumeration<String> enumStr = super.getHeaderNames();
            ArrayList<String> list = Collections.list(enumStr);
            if (!list.contains(HttpHeaders.COOKIE) && (cookie != null)) {
                list.add(HttpHeaders.COOKIE);
            }

            return Collections.enumeration(list);
        }

        public Cookie[] getCookies() {

            Cookie[] cookies = super.getCookies();
            if (cookie == null) return cookies;

            ArrayList<Cookie> list = (cookies == null) ? new ArrayList<Cookie>()
                    : new ArrayList<Cookie>(Arrays.asList(cookies));

            list.add(cookie);
            cookies = new Cookie[list.size()];
            return list.toArray(cookies);
        }

    }

}
