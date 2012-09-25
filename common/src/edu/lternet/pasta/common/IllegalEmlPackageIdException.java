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

package edu.lternet.pasta.common;

/**
 * Used to indicate that a packageId attribute in an EML document is illegal.
 *
 */
public class IllegalEmlPackageIdException extends UserErrorException {

    private static final long serialVersionUID = 1L;

    private final String packageId;

    /**
     * Constructs a new exception.
     * @param msg a descriptive error message appropriate for end-users.
     * @param packageId the packageId that is illegal.
     */
    public IllegalEmlPackageIdException(String msg,
                                        String packageId) {
        super(msg);
        this.packageId = packageId;
    }

    /**
     * Constructs a new exception.
     * @param msg a descriptive error message appropriate for end-users.
     * @param packageId the packageId that is illegal.
     * @param cause an exception thrown by an EML packageId parser.
     */
    public IllegalEmlPackageIdException(String msg,
                                        String packageId,
                                        Throwable cause) {
        super(msg, cause);
        this.packageId = packageId;
    }

    /**
     * Returns the illegal packageId.
     * @return the illegal packageId.
     */
    public String getPackageId() {
        return packageId;
    }
}
