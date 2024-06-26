package edu.lternet.pasta.datapackagemanager;

import java.io.File;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.ucsb.nceas.utilities.Options;

import edu.lternet.pasta.common.XmlUtility;


public class EMLAnnotationReferencesTest {
  private static ConfigurationListener configurationListener = null;
  private static DataPackageManager dataPackageManager;
  private static final String dirPath = "WebRoot/WEB-INF/conf";
  private static Options options = null;
  private static File testEmlFile = null;
  private static String testEmlFileName = "EntityAnnotationReferences.xml";
  private static String testPath = null;

  Document document = null;

  @BeforeClass
  public static void setUpClass() {
    configurationListener = new ConfigurationListener();
    configurationListener.initialize(dirPath);
    options = ConfigurationListener.getOptions();

    if (options == null) {
      fail("Failed to load DataPackageManager properties file");
    }
    else {
      testPath = options.getOption("datapackagemanager.test.path");
      if (testPath == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.path'");
      }
      else {
        if (testEmlFileName == null) {
          fail("No value found for DataPackageManager property 'datapackagemanager.test.emlFileName'");
        }
        else {
          testEmlFile = new File(testPath, testEmlFileName);
        }
      }
    }
  }

  @Before
  public void setUpTest() {
     try {
       document = XmlUtility.xmlFileToDocument(testEmlFile);
     }
     catch (Exception e) {
       fail("Exception while constructing EML Document object: " + e.getMessage());
     }
  }

  @Test
  public void testEntityAnnotationsReferences() {
    EMLAnnotationReferences emlAnnotationReferences = new EMLAnnotationReferences(document);
    ArrayList<Node> nodeList = emlAnnotationReferences.getReferences();
    assertNotNull(nodeList);
    int length = nodeList.size();
    assertEquals(4, length);
    for (int i = 0; i < length; i++) {
      Node node = nodeList.get(i);
      System.out.println(((Element)node).getAttribute("references"));
    }
  }

  @After
  public void tearDownTest() {
    document = null;
  }

  @AfterClass
  public static void tearDownClass() {
    testEmlFile = null;
  }
}
