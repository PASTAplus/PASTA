/*
 *
 * $Date$ $Author$ $Revision$
 *
 * Copyright 2010-2018 the University of New Mexico.
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

import com.unboundid.ldap.sdk.LDAPException;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.auth.KnbAuthSystem;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.common.security.token.AuthTokenWithPassword;
import edu.lternet.pasta.common.security.token.BasicAuthToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Pattern;

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

	/*
	 * Class variables
	 */
    private static Logger logger = Logger.getLogger(GatekeeperFilter.class);

    private static final int BAD_REQUEST_CODE = 400;
    private static final int UNAUTHORIZED_CODE = 401;

    private static final String EDI_HOST = "ldap.edirepository.org";

    private static final String EDI_ORG = "o=EDI";
    
    /*
     * Instance variables
     */
    private FilterConfig filterConfig;

    private enum CookieUse {
        EXTERNAL, INTERNAL
    }
    
    
    /*
     * Instance methods
     */

    /**
     * Overridden init method that sets the filterConfig.
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
    	try {
            ServletContext sc = config.getServletContext();
            String realPath = sc.getRealPath("/");
            logger.info("Servlet Context Real Path: " + realPath);
    		BotMatcher.initializeRobotPatterns(realPath + "/WEB-INF/conf/robotPatterns.txt");
    	}
    	catch (IOException e) {
    		throw new ServletException(e);
    	}
    	
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

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;        
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        
            
        // Output HttpServletRequest diagnostic information
		logger.info(String.format("Request URL: %s - %s",
 		    		              httpServletRequest.getMethod(), 
 		    		              httpServletRequest.getRequestURL().toString()));
		
        try {
        	boolean hasAuthToken = hasAuthToken(httpServletRequest.getCookies());
        	Cookie internalCookie;
        	if (hasAuthToken) {
        	    /*
        	     *  Process incoming authentication token
        	     */
        		internalCookie = doCookie(httpServletRequest);
        	}
        	else {
        	    /*
        	     *  Process incoming basic-authentication header or "public" user
        	     */
        		internalCookie = doHeader(httpServletRequest, httpServletResponse);
        	}

        	PastaRequestWrapper pastaRequestWrapper = new PastaRequestWrapper(httpServletRequest, internalCookie);

            // Output bot detection information
            String robot = BotMatcher.findRobot(httpServletRequest);

            if (robot != null) {
                logger.info(String.format("Bot detected: %s", robot));
                pastaRequestWrapper.putHeader("Robot", robot);
            }

            doDiagnostics(pastaRequestWrapper);
            chain.doFilter(pastaRequestWrapper, httpServletResponse);
        }
        catch (IllegalStateException e) {
            httpServletResponse.setStatus(BAD_REQUEST_CODE);
            logger.error(e.getMessage());
        }
        catch (UnauthorizedException e) {
            httpServletResponse.setStatus(UNAUTHORIZED_CODE);
            logger.error(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            httpServletResponse.setStatus(UNAUTHORIZED_CODE);
            logger.error(e.getMessage());
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

            String[] authTokenStrParts = authTokenStr.split("-");
            authToken = authTokenStrParts[0];
            byte[] signature = Base64.decodeBase64(authTokenStrParts[1]);

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

    private String originatingUserAgent(HttpServletRequest httpServletRequest) {
    	String originatingUserAgent = null;
    	
    	if (httpServletRequest != null) {
    		final String headerName = "Originating-User-Agent";
    		originatingUserAgent = httpServletRequest.getHeader(headerName);

		}
    	
        return originatingUserAgent;
    }
    
    
    /*
     *  Process incoming basic-authentication header or "public" user
     */
    private Cookie doHeader(HttpServletRequest req, HttpServletResponse res) {
    	String rawHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
    	boolean publicUser = (rawHeader == null || rawHeader.isEmpty());
        AuthToken authToken = makeAuthenticated(rawHeader);

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
        }
        else {
            tmpHeader = rawHeader;
        }

        KnbAuthSystem knb = new KnbAuthSystem(ConfigurationListener.getLdapKeyStore());

        AuthTokenWithPassword basicToken =
                AuthTokenFactory.makeAuthTokenWithPassword(tmpHeader);
        String user = basicToken.getUserId();

        // Remove whitespace from the user DN string
        if (user != null) {
        	user = user.replace(" ", "").trim();
        }

        String password = basicToken.getPassword();

        Set<String> groups = new HashSet<String>();

        if (!user.equals(ConfigurationListener.getPublicUser())) {

            String host;
//            if (user.contains(LTER_ORG)) {
//                host = LTER_HOST;
//            } else if (user.contains(EDI_ORG)) {
            if (user.contains(EDI_ORG)) {
                host = ConfigurationListener.getLdapHost();
            } else {
                String msg = String.format("Unknown LDAPS server for user %s", user);
                throw new UnauthorizedException(msg);
            }

            LdapsConnector ldaps = null;
            try {
                ldaps = new LdapsConnector(host);
                Boolean isAuthenticated = ldaps.authenticateDn(user, password);
                if (!isAuthenticated) {
                    String msg = String.format("User %s could not be authenticated at %s", user, host);
                    logger.error(msg);
                    throw new UnauthorizedException(msg);
                }
            }
            catch (IllegalStateException e) {
                logger.error(e.getMessage());
                String msg = String.format("Could not connect to LDAPS server: %s", host);
                throw new UnauthorizedException(msg);
            }
            catch (LDAPException e) {
                logger.error(e.getMessage());
                String msg = String.format("Error while authenticating %s at %s", user, host);
                throw new UnauthorizedException(msg);
            }
            finally {
                if (ldaps != null) {
                    ldaps.closeConn();
                }
            }

            // Add user to both the 'vetted' and 'authenticated' groups
            groups.add(ConfigurationListener.getVettedGroup());
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
          cookieValue = cookieValue + "-" +
                  ((Base64.encodeBase64String(signature)).
                  replace("\r", "")).
                  replace("\n","");
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
	 *          the HttpServletRequest object.
	 * @return contentLength  
	 *          the content length that was specified in the 
	 *          request headers, possibly null
	 */
	private Integer dumpHeader(HttpServletRequest req, Boolean noAuthPeek) {
		Enumeration<String> headerNames = req.getHeaderNames();
		String headerName = null;
		Integer contentLength = null;

		String header = null;
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Header: %n"));

		while (headerNames.hasMoreElements()) {

			headerName = headerNames.nextElement();
			header = req.getHeader(headerName);

			if (headerName.equals("Authorization") && noAuthPeek)
				header = "********";

			if (headerName.equals("Content-Length")) {
				contentLength = Integer.valueOf(header);
			}

			sb.append(String.format("     %s: %s%n", headerName, header));

		}

		logger.info(sb.toString());
		return contentLength;

	}

  
	/**
	 * dumpBody outputs the contents of the request message body to the
	 * designated logger.  Note that the use of this method will render the
	 * request object inoperable for and subsequent calls.
	 * 
	 * @param req 
	 *          the HttpServletRequest object.
	 * @param contentLength 
	 *          the content length that was specified in the 
	 *          request headers, possibly null
	 */
	private void dumpBody(HttpServletRequest req, Integer contentLength) {

		if (contentLength != null) {

			try {
				BufferedReader br = req.getReader();
				String line = null;

				logger.info("Request message body:\n");

				if (br.markSupported()) {

					br.mark(contentLength + 1);

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

    Integer contentLength = dumpHeader(req, noAuthPeek);
    //dumpBody(req, contentLength);

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
      logger.error(String.format("generateSignature %s: %s", e.getClass().getSimpleName(), e.getMessage()));
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
    } catch (IOException e) {
      logger.error(String.format("writeSignature %s: %s", e.getClass().getSimpleName(), e.getMessage()));
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

    } catch (FileNotFoundException | CertificateException | InvalidKeyException | NoSuchAlgorithmException |
            SignatureException e) {
      logger.error(String.format("isValidSignature %s: %s", e.getClass().getSimpleName(), e.getMessage()));
    }

      return isValid;

  }
  
  
  /**
   * Boolean to determine whether this request originated from a browser.
   * 
   * @param request  the request object
   * @return true if this request originated from a browser, else false
   */
  private boolean isRequestFromBrowser(HttpServletRequest request) {
    boolean isFromBrowser = false;
    final String name = "User-Agent";
    Enumeration<?> values = request.getHeaders(name);
    
    if (values != null) {
      while (values.hasMoreElements()) {
        String value = (String) values.nextElement();
        if (value.contains("ICEbrowser")) { return false; } // Trac ticket #471
        if (value.contains("Chrome") || 
            value.contains("Mozilla") || 
            value.contains("Opera") || 
            value.contains("Safari")
           ) {
          isFromBrowser = true;
        }
      }
    }
    
    return isFromBrowser;
  }
  

  public static class PastaRequestWrapper extends HttpServletRequestWrapper {

    	/*
    	 * Class variables
    	 */
        private static Logger logger = Logger.getLogger(PastaRequestWrapper.class);
        
        
        /*
         * Instance variables
         */
        private Cookie cookie;
	    private final Map<String, String> customHeaders;

        
        /*
         * Constructors
         */

	    public PastaRequestWrapper(HttpServletRequest request, Cookie cookie) {
            super(request);
            this.cookie = cookie;
            this.customHeaders = new HashMap<String, String>();
        }

        
        /*
         * Instance methods
         */
        
        public String getHeader(String name) {

	        // Check the custom headers first, e.g. if name is "robot"
	        String headerValue = customHeaders.get(name);
	        
	        if (headerValue != null){
	            return headerValue;
	        }

	        if (name.equals(HttpHeaders.AUTHORIZATION)) 
	        	return null;

            String header = super.getHeader(name);

            if (name.equals(HttpHeaders.COOKIE) && 
            	header != null &&
            	header.isEmpty() && 
            	(cookie != null)
               ) {
                header = cookie.getName();
            }  

            return header;
        }

        
        public Enumeration<String> getHeaders(String name) {
        	
            Enumeration<String> enumStr = super.getHeaders(name);

            if (name.equalsIgnoreCase("Robot")) {
        		List<String> ls = new ArrayList<String>();
        		String value = getHeader(name);
        		ls.add(value);
                enumStr = Collections.enumeration(ls);
        	}

            if (name.equals(HttpHeaders.AUTHORIZATION)) {
                List<String> ls = new ArrayList<String>();
                enumStr = Collections.enumeration(ls);
            }

            if (!name.equals(HttpHeaders.COOKIE) || (cookie == null)) {
                return enumStr;
            }
            else {
            	ArrayList<String> list = Collections.list(enumStr);
            	list.add(cookie.getName() + "=" + cookie.getValue());
            	return Collections.enumeration(list);
            }
        }

        
        public Enumeration<String> getHeaderNames() {
	        // Create a set of the custom header names
	        Set<String> set = new HashSet<String>(customHeaders.keySet());
	        
	        // Now add the headers from the wrapped request object
	        @SuppressWarnings("unchecked")
	        Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
	        while (e.hasMoreElements()) {
	            // add the names of the request headers into the list
	            String n = e.nextElement();
	            set.add(n);
	        }

            if (!set.contains(HttpHeaders.COOKIE) && (cookie != null)) {
                set.add(HttpHeaders.COOKIE);
            }

            return Collections.enumeration(set);
        }

        
        public Cookie[] getCookies() {       	
            Cookie[] cookies = super.getCookies();

            if (cookie == null) {
				return cookies;
			} 
			else {
				ArrayList<Cookie> cookieList = (cookies == null) ? new ArrayList<Cookie>()
						: new ArrayList<Cookie>(Arrays.asList(cookies));
				cookieList.add(cookie);
				cookies = new Cookie[cookieList.size()];
				return cookieList.toArray(cookies);
			}
        }

        
        /**
         * Adds a custom header name and corresponding value to the wrapper
         * class
         * 
         * @param name   the custom header name
         * @param value  the custom header value
         */
	    public void putHeader(String name, String value){
	        this.customHeaders.put(name, value);
	    }
	 
    } // end PastaRequestWrapper inner class

}
