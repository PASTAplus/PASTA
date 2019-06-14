package edu.lternet.pasta.dml.parser;

import java.net.MalformedURLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author tao
 * 
 * JUnit tests for the Entity class.
 *
 */
public class EntityTest extends TestCase
{
  /*
   * Instance fields
   */
  private Entity entity         = null;
  private String id             = "001";
  private String name           = "newEntity";
  private String description    = "test";
  private Boolean caseSensitive = new Boolean(false);
  private String  orientation   = "column";
  private int     numRecords    = 200;

  
  /*
   * Constructors
   */
  
  public EntityTest (String name)
  {
    super(name);
  }

  /*
   * Class methods
   */

  /**
   * Creates a suite of tests to be run together
   */
   public static Test suite()
   {
     TestSuite suite = new TestSuite();
     suite.addTest(new EntityTest("initialize"));
     suite.addTest(new EntityTest("testGetterMethod"));
     suite.addTest(new EntityTest("testURLSetterAndGetter"));
     suite.addTest(new EntityTest("testAddAttirubte"));
     suite.addTest(new EntityTest("testAddAttirubteListGetterAndSetter"));
     suite.addTest(new EntityTest("testIsSimpleDelimited"));
     suite.addTest(new EntityTest("testDelimiter"));
     return suite;
   }
 
 
  /*
   * Instance methods 
   */

  
  /**
   * Establishes a testing framework by initializing appropriate objects.
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    entity = new Entity(id, name, description,caseSensitive,orientation,numRecords);
  }

  
  /**
   * Releases any objects after tests are complete.
   */
  protected void tearDown() throws Exception
  {
    entity = null;
    super.tearDown();
  }
  
  
  /**
   * Tests a number of different getter methods in the Entity class.
   */
  public void testGetterMethod()
  {
	  String gotId = entity.getId();
	  assertEquals(id, gotId);
	  String gotName = entity.getName();
	  assertEquals(name, gotName);
	  String gotDescription = entity.getDefinition();
	  assertEquals(gotDescription, description);
	  Boolean gotCaseSensitive = entity.getCaseSensitive();
	  assertEquals(gotCaseSensitive, caseSensitive);
	  String gotOrientation = entity.getOrientation();
	  assertEquals(gotOrientation, orientation);
	  int gotNumberRecords = entity.getNumRecords();
	  assertEquals(gotNumberRecords, numRecords);
  }

  
  /**
   * Tests the getURL() and setURL() methods.
   */
  public void testURLSetterAndGetter()
  {
	  String url="http;//knb.ecoinformatics.org";
	  
	  try {
		  entity.setURL(url);
	  }
	  catch (MalformedURLException e) {
		  fail(String.format("URL %s is considered malformed", url));
	  }
	  
	  String gotURL = entity.getURL();
	  assertEquals(url, gotURL);
  }
  
  
  /**
   * Tests the add() method for adding an attribute to the entity.
   */
  public void testAddAttirubte()
  {
	  TextDomain domain = new TextDomain();
	  String attributeName = "name";
	  String attributeId   = "id";
	  Attribute attribute = new Attribute(attributeId, attributeName, domain);
	  entity.add(attribute);
	  Attribute[] list = entity.getAttributes();
	  Attribute gotAttribute = list[0];
	  assertEquals(attribute, gotAttribute);
  }
  
  
  /**
   * Tests the getAttributeList() and setAttributeList() methods.
   */
  public void testAddAttirubteListGetterAndSetter()
  {
	  TextDomain domain = new TextDomain();
	  String attributeName = "name";
	  String attributeId   = "id";
	  Attribute attribute = new Attribute(attributeId, attributeName, domain);
	  AttributeList list = new AttributeList();
	  list.add(attribute);
	  entity.setAttributeList(list);
	  AttributeList gotList = entity.getAttributeList();
	  assertEquals(list, gotList);
  }
 
  
  /**
   * Tests the isSimpleDelimited() method.
   */
  public void testIsSimpleDelimited()
  {
	  boolean isSimple = false;
	  entity.setSimpleDelimited(isSimple);
	  boolean gotIsSimple = entity.isSimpleDelimited();
	  assertTrue(isSimple == gotIsSimple);
  }
   

  /**
   * Tests the setDelimiter() and getDelimiter() methods.
   *
   */
  public void testDelimiter()
  {
	  String delimiter = ".";
	  entity.setFieldDelimiter(delimiter);
	  String gotDelimiter = entity.getFieldDelimiter();
	  assertEquals(delimiter, gotDelimiter);
  }
  
  
  /**
  * Run an initial test that always passes to check that the test
  * harness is working.
  */
  public void initialize()
  {
    assertTrue(1 == 1);
  }

}
