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

import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.validate.NullValidator;

/**
 * Used to allow full access for all users. That is, every method invocation
 * returns {@code true}, regardless of the provided arguments.
 *
 */
public final class AllowFullAccess extends AbstractAuthTokenAccessController {

    /**
     * Returns {@code true}.
     * @return {@code true}.
     */
    @Override
    public boolean canRead(AuthToken authorizationToken,
                           String accessControlList,
                           String resourceSubmitter) {
        return true;
    }

    /**
     * Returns {@code true}.
     * @return {@code true}.
     */
    @Override
    public boolean canWrite(AuthToken authorizationToken,
                            String accessControlList,
                            String resourceSubmitter) {
        return true;
    }

    /**
     * Returns {@code true}.
     * @return {@code true}.
     */
    @Override
    public boolean canChangePermission(AuthToken authorizationToken,
                                       String accessControlList,
                                       String resourceSubmitter) {
        return true;
    }

    /**
     * Returns {@code true}.
     * @return {@code true}.
     */
    @Override
    public boolean canAll(AuthToken authorizationToken,
                          String accessControlList,
                          String resourceSubmitter) {
        return true;
    }

    /**
     * Returns a null validator.
     * @return a null validator.
     */
    @Override
    public NullValidator<String> getAcrValidator() {
        return new NullValidator<String>();
    }

}
