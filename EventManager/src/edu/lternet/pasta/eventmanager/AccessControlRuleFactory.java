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

package edu.lternet.pasta.eventmanager;

import java.io.File;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.PastaServiceUtility;

/**
 * Supplies all coarse-grained (method-level) access control rules for the Event
 * Manager.
 */
public class AccessControlRuleFactory {

    /**
     * Returns the access control rule for creating EML modification
     * subscriptions.
     *
     * @param method A String representing the serviceMethod Name that
     *               initiated the call sequence.
     * @return the access control rule for creating EML modification
     *         subscriptions.
     */
    public static String getServiceAcr(String method) {

        File acrFileName = ConfigurationListener.getPastaServiceAcr();
        String pastaService = FileUtility.fileToString(acrFileName);
        return PastaServiceUtility.getAccessTypeString(method, pastaService);
    }
}
