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
package edu.lternet.pasta.eventmanager;

import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.token.AttrListAuthTokenV1;
import edu.lternet.pasta.common.security.token.AuthToken;


public final class AuthenticationUtility
{
    private static final String AUTH_TOKEN = "auth-token";

    public static AuthToken HttpHeadersToAuthToken(HttpHeaders headers) {

        Map<String, Cookie> cookiesMap = headers.getCookies();

        if (!cookiesMap.containsKey(AUTH_TOKEN)) {
            throw new UnauthorizedException("Missing " + AUTH_TOKEN);
        }

        String tmpToken = cookiesMap.get(AUTH_TOKEN).getValue();
        return new AttrListAuthTokenV1(tmpToken);
    }
}
