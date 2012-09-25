/*
 * $Date$
 * $Author$
 * $Revision$
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

package edu.lternet.pasta.common.security.token;

import java.util.Set;

import edu.lternet.pasta.common.security.auth.AuthSystemDef;

/**
 * Used to represent an authorization token.
 */
public interface AuthToken {

    /**
     * Returns a representation of this token as a string.
     * @return a representation of this token as a string.
     */
    public String getTokenString();

    /**
     * Returns the user's ID.
     * @return the user's ID.
     */
    public String getUserId();

    /**
     * Returns the user's {@code authSystem}.
     * @return the user's {@code authSystem}.
     */
    public AuthSystemDef getAuthSystem();

    /**
     * Returns the user's groups.
     * @return the user's groups.
     */
    public Set<String> getGroups();

    /**
     * Returns the expiration date of this token, in milliseconds since January
     * 1, 1970 00:00:00 GMT.
     *
     * @return the expiration date of this token, in milliseconds since January
     *         1, 1970 00:00:00 GMT.
     */
    public long getExpirationDate();

}
