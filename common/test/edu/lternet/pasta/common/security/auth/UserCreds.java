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

import java.util.ResourceBundle;

import javax.ws.rs.core.HttpHeaders;

import edu.lternet.pasta.common.security.token.AuthTokenWithPassword;
import edu.lternet.pasta.common.security.token.BasicAuthToken;

/**
 * Provides user credentials for JUnit testing.
 */
public class UserCreds {

    public static final String LTER = "lter";
    public static final String UNAFFILIATED = "unaffiliated";
    public static final String SANPARKS= "sanparks";
    public static final String NCEAS = "nceas";
    public static final String PISCO = "pisco";
    
    private static final ResourceBundle bundle;
    
    static {
        // Reading properties file
        String name = UserCreds.class.getName();
        bundle = ResourceBundle.getBundle(name);
    }
    
    public static String getUser(String organization) {
        return bundle.getString(organization + ".user");
    }
    
    public static String getPassword(String organization) {

        String password = bundle.getString(organization + ".password");

        if (password.equals("@PASSWORD@")) {
            String s = "You forgot to change the password in " +
                       "UserCreds.properties.";
            throw new IllegalStateException(s);
        }

        return password;
    }
    
    public static AuthTokenWithPassword getAuthToken(String organization) {
        String o = organization;
        return new BasicAuthToken(getUser(o), getPassword(o));
    }
    
    public static HttpHeaders getHttpHeaders(String organization) {
        String o = organization;
        return new DummyBasicHttpHeaders(getUser(o), getPassword(o));
    }
}
