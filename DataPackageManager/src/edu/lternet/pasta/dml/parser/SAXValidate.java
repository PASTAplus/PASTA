package edu.lternet.pasta.dml.parser;

/**
 *     '$RCSfile: SAXValidate.java,v $'
 *     Copyright: 1997-2002 Regents of the University of California,
 *                          University of New Mexico, and
 *                          Arizona State University
 *      Sponsors: National Center for Ecological Analysis and Synthesis and
 *                Partnership for Interdisciplinary Studies of Coastal Oceans,
 *                   University of California Santa Barbara
 *                Long-Term Ecological Research Network Office,
 *                   University of New Mexico
 *                Center for Environmental Studies, Arizona State University
 * Other funding: National Science Foundation (see README for details)
 *                The David and Lucile Packard Foundation
 *   For Details: http://knb.ecoinformatics.org/
 *
 *      '$Author: tao $'
 *        '$Date: 2009/02/24 00:45:48 $'
 *    '$Revision: 1.11 $'
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


/**
 * Validate an XML document using a SAX parser
 */
public class SAXValidate extends DefaultHandler implements ErrorHandler {
	
  private boolean schemavalidate = false;

  public final static String
               DEFAULT_PARSER = "org.apache.xerces.parsers.SAXParser";
  
  public final static String NAMESPACES =
	"eml://ecoinformatics.org/eml-2.0.0 http://nis.lternet.edu/schemas/eml/eml-2.0.0/eml.xsd " +
	"eml://ecoinformatics.org/eml-2.0.1 http://nis.lternet.edu/schemas/eml/eml-2.0.1/eml.xsd " +
	"eml://ecoinformatics.org/eml-2.1.0 http://nis.lternet.edu/schemas/eml/eml-2.1.0/eml.xsd " +
	"eml://ecoinformatics.org/eml-2.1.1 http://nis.lternet.edu/schemas/eml/eml-2.1.1/eml.xsd " +
	"eml://ecoinformatics.org/eml-2.2.0 https://raw.githubusercontent.com/NCEAS/eml/BRANCH_EML_2_2/xsd/eml.xsd " +
	"eml://ecoinformatics.org/literature-2.1.0 http://nis.lternet.edu/schemas/eml/eml-2.1.0/eml-literature.xsd " +
	"eml://ecoinformatics.org/project-2.1.0 http://nis.lternet.edu/schemas/eml/eml-2.1.0/eml-project.xsd " +
	"eml://ecoinformatics.org/literature-2.1.1 http://nis.lternet.edu/schemas/eml/eml-2.1.1/eml-literature.xsd " +
	"eml://ecoinformatics.org/project-2.1.1 http://nis.lternet.edu/schemas/eml/eml-2.1.1/eml-project.xsd " +
	"http://www.xml-cml.org/schema/stmml-1.1 http://nis.lternet.edu/schemas/eml/eml-2.1.0/stmml.xsd " +
	"http://www.xml-cml.org/schema/stmml http://nis.lternet.edu/schemas/eml/eml-2.0.1/stmml.xsd";


  /**
   * Construct an instance of the handler class
   *
   * @param validateschema  Description of Parameter
   */
  public SAXValidate(boolean validateschema) {
    this.schemavalidate = validateschema;
  }

  
  /**
   * Method for handling errors during a parse
   *
   * @param exception         The parsing error
   * @exception SAXException  Description of Exception
   */
  public void error(SAXParseException exception) 
		  throws SAXException {
    throw exception;
  }

  
  /**
   * Method for handling warnings during a parse
   *
   * @param exception         The parsing error
   * @exception SAXException  Description of Exception
   */
  public void warning(SAXParseException exception)
		  throws SAXException {
    throw new SAXException("WARNING: " + exception.getMessage());
  }

  
  /**
   * Run the validation test. Uses the schema locations declared in the
   * NAMESPACES constant.
   *
   * @param xml            
   *     the xml stream to be validated
   * @param parserName     
   *     the name of a SAX2 compliant parser class
   * @param namespaceInDoc 
   *     the namespace value specified in the document
   * @exception IOException 
   *     thrown when test files can't be opened
   * @exception ClassNotFoundException  
   *     thrown when the SAX Parser class can't be located
   * @exception SAXException
   * @exception SAXParserException
   */
  public void runTest(Reader xml, String parserName, String namespaceIndoc)
		  throws IOException, ClassNotFoundException,
                 SAXException, SAXParseException {
    runTest(xml, parserName, NAMESPACES, namespaceIndoc);
  }

  
  /**
   * Run the validation test.
   *
   * @param xml            
   *     the xml stream to be validated
   * @param parserName     
   *     the name of a SAX2 compliant parser class
   * @param schemaLocation 
   *     a string holding the schema locations 
   *     (see NAMESPACES constant for an example)
   * @param namespaceInDoc 
   *     the namespace value specified in the document
   * @exception IOException 
   *     thrown when test files can't be opened
   * @exception ClassNotFoundException  
   *     thrown when the SAX Parser class can't be located
   * @exception SAXException
   * @exception SAXParserException
   */
  public void runTest(Reader xml, String parserName, 
		              String schemaLocation, String namespaceInDoc)
           throws IOException, ClassNotFoundException,
                  SAXException, SAXParseException {
    // Get an instance of the parser
    XMLReader parser;
    
    if(parserName.equals("DEFAULT")) {
      parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER);
    }
    else {
      parser = XMLReaderFactory.createXMLReader(parserName);
    }
    
    // Set Handlers in the parser
    parser.setContentHandler((ContentHandler)this);
    parser.setErrorHandler((ErrorHandler)this);
    parser.setFeature("http://xml.org/sax/features/namespaces", true);
    parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    parser.setFeature("http://xml.org/sax/features/validation", true);
    parser.setProperty(
      "http://apache.org/xml/properties/schema/external-schemaLocation",
      schemaLocation);
    
    if (schemavalidate) {
        parser.setFeature(
            "http://apache.org/xml/features/validation/schema",
            true);
    }
    
    // Parse the document
    parser.parse(new InputSource(xml));
  }
  
  
  /**
   * Main method will be called by the script file rnEML parser
   */
  public static void main(String[] args) {
	  if (args.length > 0) {
		  System.out.println("-----------------------------------------------------------------------");
	      System.out.println("SAX validate Parser:");
	      System.out.println("This parser WILL VALIDATE your eml file against the schema");
	      System.out.println("Usage: java org.ecoinformatics.eml.SAXValidate <eml file> [schema -schemaLocation]");
	      System.out.println("-----------------------------------------------------------------------");
	    }

	    if (args.length > 2) {
	      System.out.println("Invalid number of arguments.");
	      System.exit(0);
	    }

	    String emlfile = "";
	    String schemaLocation = NAMESPACES;

	    if (args.length == 2) {
	      emlfile = args[0];
	      schemaLocation = args[1];
	    }
	    else if(args.length == 1) {
	      emlfile = args[0];
	    }
	    else {
	      System.out.println("Usage: java org.ecoinformatics.eml.SAXValidate <eml file> [schema location]");
	      System.out.println("  <eml file> = the EML file to parse");
	      System.out.println("  <schema location> = use an alternate schema location. The default one is \"eml://ecoinformatics.org/eml-2.1.0 ../eml.xsd\"");
	      
	      System.exit(0);
	    }
	    
	    String namespaceInDoc = null;
	    
	    try {
	        SAXValidate validator = new SAXValidate(true);
	        validator.runTest(new FileReader(emlfile), "DEFAULT", schemaLocation, namespaceInDoc);
	        System.out.println(emlfile+ " is XML-schema valid. There were no XML errors found in your document.");
	    }
	    catch(Exception e) {
	    	  System.out.println("Error: " + e.getMessage());
	    }
  }
  
}
