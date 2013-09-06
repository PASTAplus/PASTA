/*
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2011,2012 the University of New Mexico.
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

package edu.lternet.pasta.client;

import static org.junit.Assert.*;

import java.io.File;
import java.text.ParseException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.portal.ConfigurationListener;

/**
 * @author servilla
 * @since Apr 8, 2012
 * 
 */
public class ReportUtilityTest {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.ReportUtilityTest.class);

  private static String testReport = null; // Test quality report
  private static String testPackageId = null;
  private static String xslPath = null;
  private static String cwd = null;
  private static File xmlFile = null;
  private static String xmlString = null;
  private static ReportUtility qru = null;

  /*
   * Instance variables
   */

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {

    ConfigurationListener.configure();
    Configuration options = ConfigurationListener.getOptions();

    if (options == null) {
      fail("Failed to load the DataPortal properties file: 'dataportal.properties'");
    } else {
      
      testReport = options.getString("qualityreportutility.testreport");
      if (testReport == null) {
        fail("No value found for test report: 'qualityreportutility.testreport'");
      }
      
      xslPath = options.getString("qualityreportutility.xslpath");
      if (xslPath == null) {
        fail("No value found for XSL path: 'qualityreportutility.xslpath");
      }
      
      testPackageId = options.getString("qualityreportutility.testpackageid");
      if (testPackageId == null) {
        fail("No value found for test Package Id: 'qualityreportutility.testpackageid");
      }
      
    }

    // Read test report XML from file to string
    cwd = System.getProperty("user.dir");
    xmlFile = new File(cwd + testReport);
    xmlString = FileUtils.readFileToString(xmlFile);
    
    // Create new QualityRerportUtitliy object
    qru = new ReportUtility(xmlString);

  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    
    qru = null;
    
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void testQualityReportToHtml() {
    
    String reportHtml = qru.xmlToHtmlTable(cwd + xslPath);
    
    // Test message body
    assertFalse(reportHtml == null);
    if (reportHtml != null) {
      assertFalse(reportHtml.isEmpty());
      assertTrue(reportHtml.contains("Data Package Quality Report"));
      assertTrue(reportHtml.contains(testPackageId));
    }
    
    
  }

}
