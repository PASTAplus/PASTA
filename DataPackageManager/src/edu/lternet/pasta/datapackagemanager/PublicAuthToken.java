package edu.lternet.pasta.datapackagemanager;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.common.security.token.AuthTokenWithPassword;
import edu.lternet.pasta.common.security.token.BasicAuthToken;

public class PublicAuthToken {

	private static final String dirPath = "WebRoot/WEB-INF/conf";
    private static final long TTL = 10000;
    private static final String publicUser = "public";

    private final Logger logger = Logger.getLogger(PublicAuthToken.class);

    static AuthToken makePublicAuthToken() {
        String tmpToken = BasicAuthToken.makeTokenString(publicUser, publicUser);

        AuthTokenWithPassword basicToken = AuthTokenFactory.makeAuthTokenWithPassword(tmpToken);
        String user = basicToken.getUserId();
        String password = basicToken.getPassword();
        Set<String> groups = new HashSet<String>();

        AuthSystemDef authSystem =AuthSystemDef.KNB;
        long expirationDate = new Date().getTime() + TTL;
        AuthToken token = AuthTokenFactory.makeCookieAuthToken(user, authSystem, expirationDate, groups);

        return token;
    }


}
