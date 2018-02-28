package edu.lternet.pasta.gatekeeper;

import com.unboundid.ldap.sdk.*;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLSocketFactory;
import java.security.GeneralSecurityException;

/**
 * @author servilla
 *
 *  Provide a general LDAP utility class for performing bind, DN queries, and more...
 */
public class LdapsConnector {

    private static Logger logger = Logger.getLogger(LdapsConnector.class);

    private static final Integer PORT = 636;
    private LDAPConnection conn;


    /**
     * Constructs an new LDAPS only connection.
     *
     * @param ldapHost
     *          host DNS to LDAPS server
     *
     */
    LdapsConnector(String ldapHost) throws IllegalStateException {

        LDAPConnectionOptions options = new LDAPConnectionOptions();
        options.setFollowReferrals(false);

        SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());

        try {
            SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();
            conn = new LDAPConnection(sslSocketFactory, options);
            conn.connect(ldapHost, PORT);
        }
        catch (GeneralSecurityException e) {
            logger.error(e.getMessage());
            throw new IllegalStateException(e);
        }
        catch (LDAPException e) {
            logger.error(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    public Boolean isAuthenticated(String userDn, String password) throws LDAPException {

        Boolean authenticated = false;
        Boolean validDn = DN.isValidDN(userDn);
        Boolean successfulBind = (this.bind(userDn, password) == ResultCode.SUCCESS);

        if (validDn && successfulBind) {
            String uid = DN.getRDNString(userDn);
            String base = userDn.replace(uid + ",", "");
            String userId = uid.split("=")[1];

            Filter filter = Filter.createEqualityFilter("uid", userId);
            SearchRequest searchRequest = new SearchRequest(base, SearchScope.SUB, filter, "uid");

            SearchResult searchResult = conn.search(searchRequest);

            SearchResultEntry entry = searchResult.getSearchEntry(userDn);

            if (entry != null && entry.getAttributeValue("uid").equals(userId)) {
                authenticated = true;
            }
        }
        return authenticated;
    }

    public void closeConn() {

        conn.close();

    }


    public ResultCode bind(String userDn, String password) throws LDAPException {

        LDAPResult result;

        result = conn.bind(userDn, password);
        return result.getResultCode();

    }

}
