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

import java.util.Set;

/**
 * Represents an authorization system. Authorization systems determine what
 * level or degree of access a user has with regard to system resources.
 *
 */
public interface AuthorizationSystem {

    /**
     * Returns the groups of the specified user.
     *
     * @param user
     *            the user's ID.
     *
     * @return the groups of the specified user. If the user has no groups, an
     *         empty set is returned.
     */
    public Set<String> getGroups(String user);

}
