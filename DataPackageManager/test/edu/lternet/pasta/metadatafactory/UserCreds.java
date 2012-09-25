/*
 *
 * $Date: 2012-02-06 15:33:02 -0700 (Mon, 06 Feb 2012) $
 * $Author: jmoss $
 * $Revision: 1634 $
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

package edu.lternet.pasta.metadatafactory;

import java.util.ResourceBundle;

import javax.ws.rs.core.HttpHeaders;

import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.datapackagemanager.DummyCookieHttpHeaders;

/**
 * Provides user credentials for JUnit testing.
 */
public class UserCreds {

    private static final String user;

    static {

        // Reading properties file
        String name = UserCreds.class.getName();
        ResourceBundle bundle = ResourceBundle.getBundle(name);

        user = bundle.getString("user");
    }

    public static String getUser() {
        return user;
    }

    public static AuthToken getAuthToken() {

        HttpHeaders headers = new DummyCookieHttpHeaders(user);
        return AuthTokenFactory.makeAuthToken(headers.getCookies());

    }

    public static HttpHeaders getHttpHeaders() {
        return new DummyCookieHttpHeaders(user);
    }
}
