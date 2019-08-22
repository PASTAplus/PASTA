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

import java.io.File;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static edu.lternet.pasta.datapackagemanager.DataPackageManager.composeResourceId;
import static org.junit.Assert.*;

import edu.lternet.pasta.dml.parser.DataPackage;
import edu.lternet.pasta.utility.PastaUtility;
import edu.ucsb.nceas.utilities.Options;
import org.junit.BeforeClass;

import edu.lternet.pasta.common.PastaResource;
import edu.lternet.pasta.common.ResourceNotFoundException;
import org.junit.Test;

import java.sql.SQLException;


public class DataPackageRegistryTest {

    private static ConfigurationListener configurationListener = null;
    private static DataPackageManager dataPackageManager;
    private static DataPackageManagerResource dataPackageManagerResource;
    private static final String dirPath = "WebRoot/WEB-INF/conf";
    private static Options options = null;
    private static File testEmlFile = null;
    private static File testEmlFile_2_2 = null;
    private static String testEmlFileName = null;
    private static String testEmlFileName_2_2 = null;
    private static String testPath = null;
    private static String testScope = null;
    private static Integer testIdentifier = null;
    private static Integer testIdentifier_2_2 = null;
    private static String testIdentifierStr = null;
    private static Integer testRevision = null;
    private static String testRevisionStr = null;
    private static String testPackageId = null;
    private static String testPackageId_2_2 = null;

    private static String testMaxIdleTimeStr = null;
    private static Integer testMaxIdleTime = null;
    private static String testIdleSleepTimeStr = null;
    private static Integer testIdleSleepTime = null;
    private static String testInitialSleepTimeStr = null;
    private static Integer testInitialSleepTime = null;

    private static final String testUser = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
    private static final String testUserAcl = "uid=gmn-pasta,o=LTER,dc=ecoinformatics,dc=org";
    private static final Integer ACL_TEST_IDENTIFIER_OFFSET = 1000000;
    private static final String ACL_START_TEXT_2_2 = "<access:access xmlns:access=\"https://eml.ecoinformatics.org/access-2.2.0\"";
    private static final String ACL_END_TEXT = "</access:access>";

    private String transaction = null;

    /**
    * Initialize objects before any tests are run.
    */
    @BeforeClass
    public static void setUpClass() {
        configurationListener = new ConfigurationListener();
        configurationListener.initialize(dirPath);
        options = ConfigurationListener.getOptions();
    
        if (options == null) {
          fail("Failed to load DataPackageManager properties file");
        }
        else {
          testScope = options.getOption("datapackagemanager.test.scope");
          if (testScope == null) {
            fail("No value found for DataPackageManager property 'datapackagemanager.test.scope'");
          }
          testIdentifierStr = options.getOption("datapackagemanager.test.identifier");
          if (testIdentifierStr == null) {
            fail("No value found for DataPackageManager property 'datapackagemanager.test.identifier'");
          }
          testRevisionStr = options.getOption("datapackagemanager.test.revision");
          if (testRevisionStr == null) {
            fail("No value found for DataPackageManager property 'datapackagemanager.test.revision'");
          }
        }
        testPath = options.getOption("datapackagemanager.test.path");
        if (testPath == null) {
            fail("No value found for DataPackageManager property 'datapackagemanager.test.path'");
        }
        else {
            testEmlFileName = options.getOption("datapackagemanager.test.emlFileName");
            if (testEmlFileName == null) {
              fail("No value found for DataPackageManager property 'datapackagemanager.test.emlFileName'");
            }
            else {
              testEmlFile = new File(testPath, testEmlFileName);
            }
            testEmlFileName_2_2 = options.getOption("datapackagemanager.test.emlFileName.eml.2.2");
            if (testEmlFileName_2_2 == null) {
              fail("No value found for DataPackageManager property 'datapackagemanager.test.emlFileName.eml.2.2'");
            }
            else {
              testEmlFile_2_2 = new File(testPath, testEmlFileName_2_2);
            }
        }
        testIdentifier = new Integer(testIdentifierStr);
        testRevision = new Integer(testRevisionStr);
        testPackageId = testScope + "." + testIdentifier + "." + testRevision;

        testMaxIdleTimeStr = options.getOption("datapackagemanager.test.maxidletime");
        if (testMaxIdleTimeStr == null) {
            fail("No value found for DataPackageManager property 'datapackagemanager.test.maxidletime'");
        }

        testIdleSleepTimeStr = options.getOption("datapackagemanager.test.idlesleeptime");
        if (testIdleSleepTimeStr == null) {
            fail("No value found for DataPackageManager property 'datapackagemanager.test.idlesleeptime'");
        }

        testInitialSleepTimeStr = options.getOption("datapackagemanager.test.initialsleeptime");
        if (testInitialSleepTimeStr == null) {
            fail("No value found for DataPackageManager property 'datapackagemanager.test.initialsleeptime'");
        }

        testMaxIdleTime = new Integer(testMaxIdleTimeStr);
        testIdleSleepTime = new Integer(testIdleSleepTimeStr);
        testInitialSleepTime = new Integer(testInitialSleepTimeStr);

        try {
            dataPackageManager = new DataPackageManager();
        }
        catch (Exception e) {
            fail("Error encountered while constructing DataPackageManager object prior to running JUnit test.");
        }
        try {
            dataPackageManagerResource = new DataPackageManagerResource();
        }
        catch (Exception e) {
            fail("Error encountered while constructing DataPackageManagerResource object prior to running JUnit test.");
        }

        Integer newestRevision = null;

      try {
        newestRevision = dataPackageManager.getNewestRevision(testScope, testIdentifier);
        while (newestRevision != null) {
          testIdentifier += 1;
          newestRevision = dataPackageManager.getNewestRevision(testScope, testIdentifier);
        }
      }
      catch (ResourceNotFoundException e) {
          newestRevision = null;
      }
      catch (Exception e) {
        fail("Error encountered while initializing identifier value prior to running JUnit test: " +
             e.getMessage());               
      }

      testPackageId = testScope + "." + testIdentifier + "." + testRevision;
      System.err.println("testPackageId: " + testPackageId);
      DataPackageManagerResourceTest.modifyTestEmlFile(testScope, testEmlFile, testPackageId);

      Integer newestRevision_2_2 = null;
      testIdentifier_2_2 = testIdentifier + ACL_TEST_IDENTIFIER_OFFSET;

      try {
        newestRevision_2_2 = dataPackageManager.getNewestRevision(testScope, testIdentifier_2_2);
        while (newestRevision_2_2 != null) {
          testIdentifier_2_2 += 1;
          newestRevision_2_2 = dataPackageManager.getNewestRevision(testScope, testIdentifier_2_2);
        }
      }
      catch (ResourceNotFoundException e) {
        newestRevision_2_2 = null;
      }
      catch (Exception e) {
        fail("Error encountered while initializing identifier value prior to running JUnit test: " +
                e.getMessage());
      }

      testPackageId_2_2 = testScope + "." + testIdentifier_2_2 + "." + testRevision;
      System.err.println("testPackageId_2_2: " + testPackageId_2_2);

      DataPackageManagerResourceTest.modifyTestEmlFile(testScope, testEmlFile_2_2, testPackageId_2_2);

    }

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

    /**
     * Test getResourceAcl
     */
    @Test public void testGetResourceAcl() throws Exception {
        DataPackageRegistry registry = DataPackageManager.makeDataPackageRegistry();
        String acl, packageId, resourceId;

        HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
        String errorSnippet = "Attempting to insert a data package that already exists in PASTA";

        // Test CREATE for OK status
        Response response = dataPackageManagerResource.createDataPackage(httpHeaders, testEmlFile_2_2);
        int statusCode = response.getStatus();
        assertEquals(202, statusCode);

        // Check the message body
        String entityString = (String) response.getEntity();
        assertTrue(entityString != null);
        assertTrue(entityString.startsWith("create_"));
        this.transaction = entityString;
        waitForPastaUpload_2_2(testRevision);

        resourceId = DataPackageManager.composeResourceId(DataPackageManager.ResourceType.metadata, testScope, testIdentifier_2_2, testRevision, null);
        acl = registry.getResourceAcl(resourceId);

        assertTrue(acl != null);
        assertTrue(acl.trim().startsWith(ACL_START_TEXT_2_2));
        assertTrue(acl.trim().endsWith(ACL_END_TEXT));

        resourceId = DataPackageManager.composeResourceId(DataPackageManager.ResourceType.dataPackage, testScope, testIdentifier_2_2, testRevision, null);
        acl = registry.getResourceAcl(resourceId);

        assertTrue(acl != null);
        assertTrue(acl.trim().startsWith(ACL_START_TEXT_2_2));
        assertTrue(acl.trim().endsWith(ACL_END_TEXT));

        resourceId = DataPackageManager.composeResourceId(DataPackageManager.ResourceType.data, testScope, testIdentifier_2_2, testRevision, "DUMMY_ENTITY_ID");
        acl = registry.getResourceAcl(resourceId);

        assertTrue(acl != null);
        assertTrue(acl.trim().startsWith(ACL_START_TEXT_2_2));
        assertTrue(acl.trim().endsWith(ACL_END_TEXT));
    }

    private void waitForPastaUpload_2_2(Integer revision) {
        try {
            // Ensure that the test data package has been successfully created
            PastaUtility.waitForPastaUpload(
                    dataPackageManager,
                    this.transaction,
                    testInitialSleepTime,
                    testMaxIdleTime,
                    testIdleSleepTime,
                    testPackageId_2_2,
                    testScope,
                    testIdentifier_2_2,
                    revision
            );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
