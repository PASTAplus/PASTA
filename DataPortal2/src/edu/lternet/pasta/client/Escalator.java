/*
 * Copyright 2011-2013 the University of New Mexico.
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
 */

package edu.lternet.pasta.client;

import org.apache.commons.codec.binary.Base64;

/**
 * User: servilla
 * Date: 6/13/13
 * Time: 3:17 PM
 *
 * Provide methods to alter the user's authentication token.
 */
public class Escalator {

    // Class variables

    // Class methods

    public static String addGroup(String token, String group) {

        String[] tokenParts = token.split("-");

        String authToken = new String(Base64.decodeBase64(tokenParts[0]));
        authToken += "*" + group;
        authToken = Base64.encodeBase64String(authToken.getBytes());

        return authToken + "-" + tokenParts[1];
    }

    // Constructors

    // Instance variables

    // Instance methods

}
