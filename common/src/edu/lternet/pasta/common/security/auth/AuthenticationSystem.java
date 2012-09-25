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
 * Represents an authentication system. Authentication systems verify that
 * users are who they claim to be.
 *
 */
public interface AuthenticationSystem {

    /**
     * Indicates if the specified user could be authenticated with the provided
     * password.
     *
     * @param user
     *            the user's ID.
     * @param password
     *            the user's password.
     *
     * @return {@code true} if the user was authenticated; {@code false} if the
     *         user was not authenticated because the provided ID does not exist
     *         in this authentication system or the provided password is
     *         invalid.
     *
     * @throws Exception
     *             if the user could not be authenticated for a reason other
     *             than an invalid user ID or password. The type of exception
     *             can be specified by implementations of this interface.
     */
    public boolean authenticate(String user, String password);

}
