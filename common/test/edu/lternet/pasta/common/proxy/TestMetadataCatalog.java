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

package edu.lternet.pasta.common.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.EmlUtility;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.UserCreds;

public class TestMetadataCatalog {

    private EmlPackageId packageId;
    private AuthToken token;
    
    @Before
    public void init() {

        token = UserCreds.getAuthToken();
        
        EmlPackageIdFormat format = new EmlPackageIdFormat(Delimiter.DOT);
        packageId = format.parse("junit.1.0");

        if (!MetadataCatalog.exists(packageId, token)) {
            String s = format.format(packageId) + 
                       " does not exist in the Metadata Catalog.";
            fail(s);
        }

    }

    @Test
    public void testGetMetadataUrl() {
        String s = MetadataCatalog.getMetadataUrl(packageId);
        assertTrue(s.startsWith(MetadataCatalog.getRootUrl()));
        URI.create(s); // ensuring parsable
    }
    
    @Test
    public void testGetMetadata() {

        String xml = MetadataCatalog.getMetadata(packageId, token);
        Document doc = XmlUtility.xmlStringToDoc(xml);
        EmlPackageId epi = EmlUtility.getEmlPackageId(doc);
        
        assertEquals(packageId, epi);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testGetMetadataUrlWithIncompletePackageId() {
        MetadataCatalog.getMetadataUrl(new EmlPackageId("junit", 1, null));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testGetMetadataWithIncompletePackageId() {
        packageId = new EmlPackageId("junit", 1, null);
        MetadataCatalog.getMetadata(packageId, token);
    }
    
    @Test(expected=JerseyProxyException.class)
    public void testGetMetadataWithNonExistentEmlDocument() {
        packageId = new EmlPackageId("non-existent", 1, 1);
        MetadataCatalog.getMetadata(packageId, token);
    }

    @Test
    public void testExistsWithJUnitEml() {
        assertTrue(MetadataCatalog.exists(packageId, token));
    }
    
    @Test
    public void testExistsWithNonExistentEmlDocument() {
        packageId = new EmlPackageId("non-existent", 1, 1);
        assertFalse(MetadataCatalog.exists(packageId, token));
    }

}
