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

import edu.lternet.pasta.common.security.access.v1.PastaServiceAccessControllerV1;
import edu.lternet.pasta.common.security.access.v1.UserAccessControllerV1;

/**
 * Used as a factory for access controllers. It facilitates the
 * maintenance of access controller uniformity across all PASTA web services.
 */
public final class AccessControllerFactory {

    private AccessControllerFactory() {
        // preventing instantiation
    }

    /**
     * Returns an instance of the default access controller class.
     * @return an instance of the default access controller class.
     */
    public static AccessController getDefaultAccessController() {
        return new AllowFullAccess(); // can be any access controller
    }

    /**
     * Returns an instance of the default JAX-RS HTTP access controller class.
     * @return an instance of the default JAX-RS HTTP access controller class.
     */
    public static JaxRsHttpAccessController getDefaultHttpAccessController() {
        return new PastaServiceAccessControllerV1();
    }

    /**
     * Returns an instance of the default authorization token access controller
     * class.
     * @return an instance of the default authorization token access controller
     * class.
     */
    public static AuthTokenAccessController
                    getDefaultAuthTokenAccessController() {
        return new UserAccessControllerV1();
    }
}
