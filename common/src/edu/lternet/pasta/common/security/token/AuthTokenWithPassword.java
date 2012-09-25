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

/**
 * Used to represent an authorization token that contains an encrypted
 * password.
 */
public interface AuthTokenWithPassword extends AuthToken {

    /**
     * Returns the user's password.
     * @return the user's password.
     */
    public String getPassword();

    /**
     * Returns the user's password encryption type.
     * @return the user's password encryption type.
     */
    public EncryptionType getEncryptionType();

    /**
     * Used to define all encryption types that can occur in authorization
     * tokens.
     */
    public enum EncryptionType {
        AES();
    }
}
