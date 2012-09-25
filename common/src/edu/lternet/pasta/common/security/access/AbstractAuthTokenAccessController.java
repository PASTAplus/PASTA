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

package edu.lternet.pasta.common.security.access;

import javax.ws.rs.core.HttpHeaders;

import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;

/**
 * Used to facilitate the implementation of classes that are access controllers,
 * HTTP access controllers, and authorization token access controllers. That is
 * accomplished by extending this class and implementing the four methods
 * associated with authorization tokens.
 *
 */
public abstract class AbstractAuthTokenAccessController
    implements AccessController,
               JaxRsHttpAccessController,
               AuthTokenAccessController {

    private AuthToken makeToken(HttpHeaders headers) {
        return AuthTokenFactory.makeAuthTokenWithPassword(headers);
    }

    private AuthToken makeToken(String token) {
        return AuthTokenFactory.makeAuthTokenWithPassword(token);
    }

    @Override
    public boolean canRead(String authToken,
                           String accessControlList,
                           String resourceSubmitter) {

        AuthToken token = makeToken(authToken);
        return canRead(token, accessControlList, resourceSubmitter);
    }

    @Override
    public boolean canWrite(String authToken,
                            String accessControlList,
                            String resourceSubmitter) {

        AuthToken token = makeToken(authToken);
        return canWrite(token, accessControlList, resourceSubmitter);
    }

    @Override
    public boolean canChangePermission(String authToken,
                                       String accessControlList,
                                       String resourceSubmitter) {

        AuthToken token = makeToken(authToken);
        return canChangePermission(token, accessControlList, resourceSubmitter);
    }

    @Override
    public boolean canAll(String authToken,
                          String accessControlList,
                          String resourceSubmitter) {

        AuthToken token = makeToken(authToken);
        return canAll(token, accessControlList, resourceSubmitter);
    }

    @Override
    public boolean canRead(HttpHeaders requestHeaders,
                           String accessControlList,
                           String resourceSubmitter) {

        AuthToken token = makeToken(requestHeaders);
        return canRead(token, accessControlList, resourceSubmitter);
    }

    @Override
    public boolean canWrite(HttpHeaders requestHeaders,
                            String accessControlList,
                            String resourceSubmitter) {

        AuthToken token = makeToken(requestHeaders);
        return canWrite(token, accessControlList, resourceSubmitter);
    }

    @Override
    public boolean canAll(HttpHeaders requestHeaders,
                          String accessControlList,
                          String resourceSubmitter) {

        AuthToken token = makeToken(requestHeaders);
        return canAll(token, accessControlList, resourceSubmitter);
    }

    @Override
    public boolean canChangePermission(HttpHeaders requestHeaders,
                                       String accessControlList,
                                       String resourceSubmitter) {

        AuthToken token = makeToken(requestHeaders);
        return canChangePermission(token, accessControlList, resourceSubmitter);
    }
}
