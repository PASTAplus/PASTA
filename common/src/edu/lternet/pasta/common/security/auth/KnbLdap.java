/*
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.common.security.auth;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.ExtendedRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchResultReference;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;

import edu.lternet.pasta.common.FileUtility;

/**
 * <p>
 * Used to authenticate users using the LDAP server of the Knowledge Network for
 * Biocomplexity (KNB). Connections are attempted with the host
 * {@code ldap.ecoinformatics.org} on port 389. If successful, a request is made
 * to StartTLS.
 * </p>
 * <p>
 * Authentication is first attempted directly with KNB's LDAP, but if that
 * fails, its LDAP referral URLs will be queried and examined. If one of those
 * URLs references the organization (o=<em>org</em>) that is contained in the
 * user's distinguished name, authentication will be attempted with that LDAP
 * server. If those attempts fail, the user could not be authenticated.
 * </p>
 */
public final class KnbLdap extends Ldap {

	private static final String SERVER = "ldap.lternet.edu";
	private static final int PORT = 389;

	private final File keystoreFile;
	private LDAPConnection connection;

	/**
	 * Constructs a new KNB LDAP authentication system.
	 */
	public KnbLdap(File keystoreFile) {
		super(SERVER, PORT);
		this.keystoreFile = FileUtility.assertCanRead(keystoreFile);
	}

	public static void main(String[] arg) throws LDAPException {

		String fileName = "./WebRoot/WEB-INF/conf/keystore.jks";
		File keystore = FileUtility.assertCanRead(fileName);

		KnbLdap ldap = new KnbLdap(keystore);

		String user = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
		String password = "S@ltL@ke";

		System.out.println(ldap.authenticate(user, password));
	}

	/**
	 * Indicates if the specified user can be authenticated by this LDAP
	 * authentication system.
	 * 
	 * @param user
	 *          the user's distinguished name.
	 * @param password
	 *          the user's password.
	 * 
	 * @return {@code true} if the user was authenticated; {@code false} if the
	 *         user was not authenticated because the provided ID does not exist
	 *         in this LDAP or the provided password is invalid.
	 * 
	 * @throws IllegalStateException
	 *           if anything unexpected occurs during communication with the LDAP
	 *           server that prevents a definitive authentication determination.
	 *           These exceptions might have an instance of {@link LDAPException}
	 *           as their cause.
	 */
	@Override
	public boolean authenticate(String user, String password) {

		// No need to attempt authentication
		if (!DN.isValidDN(user)) {
			return false;
		}

		// If the user cannot be authenticated directly by KNB's LDAP,
		// check the referral URLs that are returned.
		boolean checkReferrals = true;

		return authenticate(user, password, SERVER, checkReferrals);
	}

	/**
	 * Returns an LDAP connection to the specified host with all of the necessary
	 * properties.
	 * 
	 * @throws IllegalStateException
	 */
	private LDAPConnection makeTlsConnection(String host)
	    throws IllegalStateException {

		// FollowReferrals must be set to false. Otherwise, referral
		// URLs are not included in LDAP search results.
		LDAPConnectionOptions options = new LDAPConnectionOptions();
		options.setFollowReferrals(false);
		LDAPConnection connection = new LDAPConnection(options);

		try {

			connection.connect(host, PORT);

		} catch (LDAPException e) {
			throw new IllegalStateException(e);
		}

		// Securing the connection in accordance with LDAPv3

		SSLUtil sslUtil = new SSLUtil(new TrustStoreTrustManager(keystoreFile));
		SSLContext sslContext;

		try {
			sslContext = sslUtil.createSSLContext();
			ExtendedRequest request = new StartTLSExtendedRequest(sslContext);
			ExtendedResult result = connection.processExtendedOperation(request);

			ResultCode code = result.getResultCode();

			if (code != ResultCode.SUCCESS) {
				String s = "The LDAP connection to '" + host
				    + "' could not be extended to TLS. The result "
				    + "code of the request was: " + code;
				throw new IllegalStateException(s);
			}

			return connection;

		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		} catch (LDAPException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Returns an LDAP connection to the specified host with all of the necessary
	 * properties.
	 * 
	 * @throws IllegalStateException
	 */
	private LDAPConnection makeConnection(String host)
	    throws IllegalStateException {

		// FollowReferrals must be set to false. Otherwise, referral
		// URLs are not included in LDAP search results.
		LDAPConnectionOptions options = new LDAPConnectionOptions();
		options.setFollowReferrals(false);
		LDAPConnection connection = new LDAPConnection(options);

		try {

			connection.connect(host, PORT);
			return connection;

		} catch (LDAPException e) {
			throw new IllegalStateException(e);
		}
		
	}

	/**
	 * Attempts to authenticate the specified user with the specified host. If
	 * authentication fails, LDAP referral URLs will be checked if that flag is
	 * set to {@code true}, which involves recursively calling this method again.
	 */
	private boolean authenticate(String user, String password, String host,
	    boolean checkReferrals) {

		try {
			
			connection = makeTlsConnection(host);
			
		} catch (IllegalStateException e) {
			System.err.println("LDAP TLS negotiation error: " + e);
			e.printStackTrace();
			
			// Attempt non-TLS connection
			try {
				
				connection = makeConnection(host);
			
			} catch (IllegalStateException e1) {
				System.err.println("LDAP negotiation error: " + e1);
				e1.printStackTrace();
				return false;
			}
		}

		try {

			LDAPResult result = connection.bind(user, password);

			ResultCode code = result.getResultCode();

			// Authentication was achieved
			if (code.intValue() == ResultCode.SUCCESS_INT_VALUE) {
				
              String uid = DN.getRDNString(user);
              String base = user.replace(uid + ",", "");
              String userId = uid.split("=")[1];
              
              Filter filter = Filter.createEqualityFilter("uid", userId);
              SearchRequest searchRequest = new SearchRequest(base, SearchScope.SUB, filter, "uid");
              
              SearchResult searchResult = connection.search(searchRequest);
              
              SearchResultEntry entry = null;
              entry = searchResult.getSearchEntry(user);
              
              // Perform case-sensitive UID test for final authentication
              if (entry != null && entry.getAttributeValue("uid").equals(userId)) {
                return true;
              } else {
                return false;
              }
      	
			}

			String s = "LDAPConnection.bind() did not throw an exception, "
			    + "but the ResultCode was not 0 (success): " + code.toString();
			throw new IllegalStateException(s);
		}

		catch (LDAPException e) {

			ResultCode code = e.getResultCode();

			// If the DN syntax is invalid, the user does not exist in an
			// LDAP and therefore cannot be authenticated.
			if (code == ResultCode.INVALID_DN_SYNTAX) {
				return false;
			}

			// If the user could not be authenticated directly
			if (code == ResultCode.INVALID_CREDENTIALS) {

				// Check the referral URLs for other LDAP servers where
				// authentication might be achieved.
				if (checkReferrals) {
					return authenticateWithReferrals(user, password);
				}

				return false;
			}

			if (code == ResultCode.CONNECT_ERROR) {
				// A connection could not be made to the LDAP server at the
				// specified port.
			}

			String s = "The LDAP result code '" + code
			    + "' could not be interpretted.";
			throw new IllegalStateException(s, e);
		}

		finally {
			if (connection != null && connection.isConnected()) {
				connection.close();
				connection = null; // allowing garbage collection
			}
		}

	}

	private boolean authenticateWithReferrals(String user, String password) {

		String orgFromUserDN = getOrgFromUserDN(user);

		// If the user ID does not contain an organization,
		// the user cannot be authenticated using referral URLs.
		if (orgFromUserDN == null) {
			return false;
		}

		List<LDAPURL> urls = getReferralUrlsFromKnbLdap();

		String matchingOrgHost = getHostWithOrgFromDN(orgFromUserDN, urls);

		// If a host could not be found with an organization that matches
		// the user's, the user cannot be authenticated.
		if (matchingOrgHost == null) {
			return false;
		}

		// Do not check any referrals beyond this 1st level. That is,
		// do not check the referrals of the referral, if they exist.
		boolean checkReferrals = false;

		return authenticate(user, password, matchingOrgHost, checkReferrals);
	}

	private String getHostWithOrgFromDN(String orgFromDN, List<LDAPURL> urls) {

		for (LDAPURL url : urls) {

			String orgFromUrl = getOrgFromReferralUrl(url);

			// If KNB's LDAP server returned a referral URL that does not
			// contain an organization (o=...), skip it.
			if (orgFromUrl == null) {
				continue;
			}

			if (orgFromUrl.equalsIgnoreCase(orgFromDN)) {
				return url.getHost();
			}
		}

		// If a host could not be found that contains the organization
		// of the provided user.
		return null;
	}

	private String getOrgFromReferralUrl(LDAPURL url) {

		// LDAP referral URLs should look similar to this:
		// <scheme>://<host>/<baseDN>?<otherStuff>
		// e.g. ldap://ldap.lternet.edu/o=LTER,dc=ecoinformatics,dc=org??base

		String baseDN = url.getBaseDN().toNormalizedString();
		int commaIndex = baseDN.toString().indexOf(",");

		// If the base DN of the provided URL does not contain a comma
		if (commaIndex < 0) {

			// If the whole base DN is somehow the organization
			if (hasOrgPrefix(baseDN)) {
				return baseDN;
			}

			// Give up, the base DN does not contain an organization
			return null;
		}

		// Getting (what should be) the organization
		String org = baseDN.substring(0, commaIndex);

		// If it really is an organization, return it
		if (hasOrgPrefix(org)) {
			return org;
		}

		// Give up, the base DN does not contain an organization
		return null;
	}

	private boolean hasOrgPrefix(String string) {
		return string.startsWith("o=") || string.startsWith("O=");
	}

	private String getOrgFromUserDN(String distinguishedName) {

		try {
			distinguishedName = DN.normalize(distinguishedName);
		} catch (LDAPException e) {
			String s = "'" + distinguishedName
			    + "' could not be parsed as a distinguished name, "
			    + "even though it was validated as such with "
			    + "DN.isValidDN(String).";
			throw new IllegalStateException(s, e);
		}

		String[] dnParts = distinguishedName.split(",");

		for (String part : dnParts) {
			if (hasOrgPrefix(part)) {
				return part;
			}
		}

		// The provided DN does not contain an organization 'o=...'
		return null;
	}

	private List<LDAPURL> getReferralUrlsFromKnbLdap() {

		SearchRequest request = makeLdapSearchRequest();
		SearchResult result = null;

		try {
			result = connection.search(request);
		} catch (LDAPSearchException e) {
			String s = "An unknown error occurred during an LDAP search.";
			throw new IllegalStateException(s, e);
		}

		List<LDAPURL> urls = new LinkedList<LDAPURL>();

		for (SearchResultReference ref : result.getSearchReferences()) {

			for (String url : ref.getReferralURLs()) {

				LDAPURL referralUrl = parseLdapUrl(url);

				if (referralUrl != null) {
					urls.add(referralUrl);
				}
			}
		}

		return urls;
	}

	private SearchRequest makeLdapSearchRequest() {

		String baseDN = "dc=ecoinformatics,dc=org";
		Filter filter = Filter.createPresenceFilter("objectClass");

		return new SearchRequest(baseDN, SearchScope.ONE, filter);
	}

	private LDAPURL parseLdapUrl(String url) {

		try {
			return new LDAPURL(url);
		} catch (LDAPException e) {

			// If KNB's LDAP server returned a referral URL that
			// could not be parsed.
			return null;
		}
	}

}
