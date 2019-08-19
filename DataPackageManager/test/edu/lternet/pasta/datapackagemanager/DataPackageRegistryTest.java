/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager;

import static org.junit.Assert.*;

import edu.lternet.pasta.common.PastaResource;
import org.junit.Test;


public class DataPackageRegistryTest {
    /**
   * Test initialization of the test suite
   */
    @Test public void testInit() {
    assertTrue(1 == 1);
  }

    @Test public void testGetPackageId() {
        String resourceId, packageId;
        String EXPECTED = "knb-lter-nin.1.1";

        resourceId = "http://pasta.lternet.edu/package/data/eml/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2";
        packageId = PastaResource.getPackageId(resourceId);
        assertEquals(packageId, EXPECTED);

        resourceId = "http://pasta.lternet.edu/package/report/eml/knb-lter-nin/1/1/";
        packageId = PastaResource.getPackageId(resourceId);
        assertEquals(packageId, EXPECTED);

        resourceId = "http://pasta.lternet.edu/package/eml/knb-lter-nin/1/1/";
        packageId = PastaResource.getPackageId(resourceId);
        assertEquals(packageId, EXPECTED);

        resourceId = "http://pasta.lternet.edu/package/metadata/eml/knb-lter-nin/1/1/";
        packageId = PastaResource.getPackageId(resourceId);
        assertEquals(packageId, EXPECTED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPackageIdException() {
        String resourceId, packageId;

        resourceId = "http://pasta.lternet.edu/package/FOOBAR/eml/knb-lter-nin/1/1/";
        packageId = PastaResource.getPackageId(resourceId);
        assertTrue(false);  // shouldn't reach this line...
    };

    @Test public void testGetMetadataResourceId() {
        String resourceId, metadataResourceId;
        String EXPECTED = "http://pasta.lternet.edu/package/metadata/eml/knb-lter-nin/1/1";

        resourceId = "http://pasta.lternet.edu/package/data/eml/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2";
        metadataResourceId = PastaResource.getMetadataResourceId(resourceId);
        assertEquals(metadataResourceId, EXPECTED);

        resourceId = "http://pasta.lternet.edu/package/report/eml/knb-lter-nin/1/1/";
        metadataResourceId = PastaResource.getMetadataResourceId(resourceId);
        assertEquals(metadataResourceId, EXPECTED);

        resourceId = "http://pasta.lternet.edu/package/eml/knb-lter-nin/1/1/";
        metadataResourceId = PastaResource.getMetadataResourceId(resourceId);
        assertEquals(metadataResourceId, EXPECTED);

        resourceId = "http://pasta.lternet.edu/package/metadata/eml/knb-lter-nin/1/1/";
        metadataResourceId = PastaResource.getMetadataResourceId(resourceId);
        assertEquals(metadataResourceId, EXPECTED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMetadataResourceIdException() {
        String resourceId, metadataResourceId;

        resourceId = "http://pasta.lternet.edu/package/FOOBAR/eml/knb-lter-nin/1/1/";
        metadataResourceId = PastaResource.getMetadataResourceId(resourceId);
        assertTrue(false);  // shouldn't reach this line...
    };
}
