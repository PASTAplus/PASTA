/*
 *
 * $Date: 2012-07-24 14:09:25 -0700 (Tue, 24 Jul 2012) $
 * $Author: dcosta $
 * $Revision: 2254 $
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageManagerResourceTest;
import edu.ucsb.nceas.utilities.Options;

public class TestMetadataFactory {

  /*
   * Class variables
   */
  
  private static ConfigurationListener configurationListener = null;
  private static DataPackageManager dataPackageManager;
  private static final String dirPath = "WebRoot/WEB-INF/conf";
  private static Options options = null;
  private static File testEmlFile = null;
  private static String testEmlFileName = null;
  private static String testPath = null;
  private static String testScope = null;
  private static String testScopeBogus = null;
  private static Integer testIdentifier = null;
  private static String testIdentifierStr = null;
  private static Integer testRevision = null;
  private static String testRevisionStr = null;
  private static String testEntityId = null;
  private static String testEntityName = null;
  private static String testEdiToken = null;
  
  
  /*
   * Instance variables
   */

  private Document doc;
  private MetadataFactory factory;
  private AuthToken authToken;
  private String ediToken;

  
  /*
   * Class methods
   */
  
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
      testScopeBogus = options.getOption("datapackagemanager.test.scope.bogus");
      if (testScopeBogus == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.scope.bogus'");
      }
      testIdentifierStr = options.getOption("datapackagemanager.test.identifier");
      if (testIdentifierStr == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.identifier'");
      }
      testRevisionStr = options.getOption("datapackagemanager.test.revision");
      if (testRevisionStr == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.revision'");
      }
      testEntityId = options.getOption("datapackagemanager.test.entity.id");
      if (testEntityId == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity.id'");
      }
      testEntityName = options.getOption("datapackagemanager.test.entity.name");
      if (testEntityName == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity.name'");
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
      }
      testEdiToken = options.getOption("datapackagemanager.test.edi.token");
      if (testEdiToken == null) {
          fail("No value found for DataPackageManager property 'datapackagemanager.test.edi.token'");
      }

      testIdentifier = new Integer(testIdentifierStr);
      testRevision = new Integer(testRevisionStr);

      try {
        dataPackageManager = new DataPackageManager();
      }
      catch (Exception e) {
        fail("Error encountered while constructing DataPackageManager object prior to running JUnit test.");
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

      String testPackageId = testScope + "." + testIdentifier + "." + testRevision;
      System.err.println("testPackageId: " + testPackageId);
      DataPackageManagerResourceTest.modifyTestEmlFile(testScope, testEmlFile, testPackageId);  
    }
  }
  
  
  /*
   * Instance methods
   */
  
    @Before
    public void init() {
        factory = new MetadataFactory();
        authToken = UserCreds.getAuthToken();
        ediToken = testEdiToken;
    }

    /*
    @Test
    public void testAppendProvenance() throws Exception {
        String entityName = "DailyWaterSample-NIN-LTER-1978-1992";
        EmlPackageId parent = new EmlPackageId("knb-lter-nin", 1, 1);

        List<String> entityNames = Collections.singletonList(entityName);

        assertNull(getMethodStepNode(doc));

        factory.make(doc, Collections.singletonMap(parent, entityNames), token);

        String xml = XmlUtility.nodeToXmlString(doc);

        assertNotNull(xml);
        assertNotNull(getMethodStepNode(doc));
        assertTrue(xml.contains(entityName));
        assertFalse(xml.contains("name1"));
    }
    */

    
    private Node getMethodStepNode(Document eml) throws Exception {
      XPath xPath = XPathFactory.newInstance().newXPath();
      String pathStr = "//methodStep";
      return (Node) xPath.evaluate(pathStr, eml, XPathConstants.NODE);
    }

    
    @Test
    public void testGenerateEML() throws Exception {
        String scope = "knb-lter-nin";
        Integer identifier = new Integer(1);
        String revision = "1";
        
        String xml = factory.generateEML(scope, identifier, revision, authToken, ediToken);

        assertNotNull(xml);
        doc = XmlUtility.xmlStringToDoc(xml);
        Node node = getMethodStepNode(doc);
        assertNotNull(node);
        assertFalse(xml.contains("name1"));
    }

}
