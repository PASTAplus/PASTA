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
 * Used to indicate that a resource already exists. This exception is
 * semantically similar to the HTTP/1.1 '<a
 * href=http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.10>409
 * Conflict</a>' status.
 */
public class ResourceExistsException extends UserErrorException {

    private static final long serialVersionUID = 1L;

    public ResourceExistsException(String msg) {
        super(msg);
    }

    public ResourceExistsException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
