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

/**
 * Represents an authentication and authorization system, that is, an <a href="http://knb.ecoinformatics.org/software/eml/eml-2.1.0/eml-access.html#authSystem"
 * >authSystem</a>. The authSystems supported by PASTA are defined by the
 * enumeration {@link AuthSystemDef}. Implementations of this interface must
 * identify themselves as one of these authSystems, but only a single
 * implementation should exist for any particular authSystem.
 *
 */
public interface AuthSystem extends AuthenticationSystem, AuthorizationSystem {

    /**
     * Returns the definition of this authSystem.
     *
     * @return the definition of this authSystem.
     */
    public AuthSystemDef getAuthSystemDef();

    /**
     * Indicates if the provided strings both refer to the same user in this
     * {@code authSystem}. In some authSystems, multiple (non-matching) strings
     * might be used to refer to the same user, such as distinguished names that
     * vary in terms of capitalization or whitespace. This method can be used to
     * determine if two non-equal strings are nevertheless equivalent.
     *
     * @param userId1
     *            a string that refers to a user in this authSystem.
     * @param userId2
     *            another string that refers to a user in this authSystem.
     *
     * @return {@code true} if both strings refer to the same user; {@code
     *         false} otherwise.
     */
    public boolean sameUser(String userId1, String userId2);

    /**
     * Indicates if the provided strings both refer to the same principal in
     * this {@code authSystem}. In some authSystems, multiple (non-matching)
     * strings might be used to refer to the same principle, such as
     * distinguished names that vary in terms of capitalization or whitespace.
     * This method can be used to determine if two non-equal strings are
     * nevertheless equivalent.
     *
     * @param principal1
     *            a string that refers to a principal in this authSystem.
     * @param principal2
     *            another string that refers to a principal in this authSystem.
     *
     * @return {@code true} if both strings refer to the same principal; {@code
     *         false} otherwise.
     */
    public boolean samePrincipal(String principal1, String principal2);

}
