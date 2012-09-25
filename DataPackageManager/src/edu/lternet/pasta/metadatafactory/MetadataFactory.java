/*
 *
 * $Date: 2012-02-10 16:25:31 -0700 (Fri, 10 Feb 2012) $
 * $Author: jmoss $
 * $Revision: 1659 $
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

package edu.lternet.pasta.metadatafactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.XmlParsingException;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.metadatafactory.eml210.MethodStepFactory;
import edu.lternet.pasta.metadatafactory.eml210.ParentEml;

/**
 * This class can be used to append provenance metadata to an EML document.
 *
 */
public final class MetadataFactory {

    /**
     * Appends provenance metadata to the provided EML document.
     * Provenance is appended as {@code //dataset/methods/methodStep}
     * elements, based on the specified parent EML documents.
     *
     * @param emlToModify
     *            the EML document to which provenance will be appended.
     *
     * @param provenance
     *            keys are packageIds of parent EML documents, values are lists
     *            of {@code entityNames} in the corresponding parent that should
     *            be included in the appended provenance.
     *
     * @param token
     *            the requesting user's credentials.
     *
     * @return the provided EML document, after appending provenance.
     *
     * @throws UnauthorizedException
     *             if the user in the provided token is not authorized to use
     *             the Metadata Factory.
     *
     * @throws XmlParsingException
     *             if the provided XML document does not have one and
     *             only one {@code //dataset} element, or if it has more than
     *             one {@code //dataset/methods} element; or if a provided
     *             entity name does not exist in its associated parent EML.
     */
    public Document make(Document emlToModify,
                         Map<EmlPackageId, List<String>> provenance,
                         AuthToken token) throws Exception {

        MethodStepFactory msf = new MethodStepFactory();

        for (Entry<EmlPackageId, List<String>> e : provenance.entrySet()) {
            EmlPackageId emlPackageId = e.getKey();
            String scope = emlPackageId.getScope();
            Integer identifier = emlPackageId.getIdentifier();
            Integer revision = emlPackageId.getRevision();
            String revisionStr = revision.toString();
            String user = token.getUserId();
            DataPackageManager dataPackageManager = new DataPackageManager();
            String eml = dataPackageManager.readMetadata(scope, identifier, revisionStr, user, token);
            Document doc = XmlUtility.xmlStringToDoc(eml);
            ParentEml parentEml = new ParentEml(doc);
            msf.append(emlToModify, parentEml, e.getValue());
        }

        return emlToModify;
    }

}
