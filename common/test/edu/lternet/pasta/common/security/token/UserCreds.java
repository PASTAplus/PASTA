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

package edu.lternet.pasta.common.security.token;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;

import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AuthTokenWithPassword;
import edu.lternet.pasta.common.security.token.BasicAuthToken;

/**
 * Provides user credentials for JUnit testing.
 */
public class UserCreds {

    private static final String user;
    private static final String password;
    private static final long EXPIRATION = 2000000000;

    static {

        // Reading properties file
        String name = UserCreds.class.getName();
        ResourceBundle bundle = ResourceBundle.getBundle(name);

        user = bundle.getString("user");
        password = bundle.getString("password");

        if (password.equals("@PASSWORD@")) {
            String s = "You forgot to change the password in " +
                       "UserCreds.properties.";
            throw new IllegalStateException(s);
        }

    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }

    /* This capability is no longer useful */
    public static AuthTokenWithPassword getAuthToken() {
        return new BasicAuthToken(user, password);
    }

    /* This capability is no longer useful */
    public static HttpHeaders getHttpHeaders() {
        return new DummyBasicHttpHeaders(user, password);
    }

    /* This needs to be getAuthToken() in the future */
    public static AuthToken getBase64AuthToken() {
        Set<String> s = new HashSet<String>();
        s.add("vetted");
        s.add("authenticated");
        return AuthTokenFactory.makeCookieAuthToken(user, AuthSystemDef.KNB, EXPIRATION, s);
    }
}
