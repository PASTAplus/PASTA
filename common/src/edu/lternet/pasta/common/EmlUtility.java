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

package edu.lternet.pasta.common;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;
import eml.ecoinformatics_org.access_2_1.AccessType;
import eml.ecoinformatics_org.access_2_1.ObjectFactory;
//import eml.ecoinformatics_org.eml_2_1.Eml;

/**
 * Used to conveniently read EML documents.
 */
public final class EmlUtility {

    private EmlUtility() {
        // preventing instantiation
    }

    /*
     * Returns an EmlPackageId object by parsing an EML file.
     */
    public static EmlPackageId emlPackageIdFromEML(File emlFile) throws Exception {
  		EmlPackageId emlPackageId = null;

  		if (emlFile != null) {
  			String emlString = FileUtils.readFileToString(emlFile);
  			try {
  				DocumentBuilder documentBuilder = DocumentBuilderFactory
  						.newInstance().newDocumentBuilder();
  				Document document = documentBuilder.parse(IOUtils
  						.toInputStream(emlString));
  				emlPackageId = getEmlPackageId(document);
  			}
  			/*
  			 * If a parsing exception is thrown, attempt to parse the packageId
  			 * using regular expressions. This could be fooled by comments text
  			 * in the EML document but is still better than nothing at all.
  			 */
  			catch (SAXException e) {
  				StringTokenizer stringTokenizer = new StringTokenizer(emlString, "\n");
                  String DOUBLE_QUOTE_PATTERN = "packageId=\"([^\"]*)\"";
                  String SINGLE_QUOTE_PATTERN = "packageId='([^']*)'";
  				Pattern doubleQuotePattern = Pattern.compile(DOUBLE_QUOTE_PATTERN);
  				Pattern singleQuotePattern = Pattern.compile(SINGLE_QUOTE_PATTERN);
  				while (stringTokenizer.hasMoreElements()) {
  					String token = stringTokenizer.nextToken();
  					if (token.contains("packageId")) {

  						Matcher doubleQuoteMatcher = doubleQuotePattern
  								.matcher(token);
  						if (doubleQuoteMatcher.find()) {
  							String packageId = doubleQuoteMatcher.group(1);
  							System.out.println(packageId);
  							EmlPackageIdFormat formatter = new EmlPackageIdFormat(
  									Delimiter.DOT);
  							emlPackageId = formatter.parse(packageId);
  							break;
  						}

  						Matcher singleQuoteMatcher = singleQuotePattern
  								.matcher(token);
  						if (singleQuoteMatcher.find()) {
  							String packageId = singleQuoteMatcher.group(1);
  							EmlPackageIdFormat formatter = new EmlPackageIdFormat(
  									Delimiter.DOT);
  							emlPackageId = formatter.parse(packageId);
  							break;
  						}

  					}
  				}
  			}
  		}

  		return emlPackageId;
    }
    
    
    /**
     * Returns the packageId of the provided EML document without parsing it
     * (the packageId). If the document does not contain the attribute
     * {@code //@packageId}, or if it does not have a value, an empty string
     * is returned.
     *
     * @param emlDocument
     *            an EML document.
     *
     * @return the packageId contained in the provided EML document.
     */
    public static String getRawEmlPackageId(Document emlDocument) {

        try {

            XPath xpath = XPathFactory.newInstance().newXPath();
            return xpath.evaluate("//@packageId", emlDocument);

        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);  // Should never be reached
        }

    }

    /**
     * Parses and returns the packageId of the provided EML document. The
     * provided document must contain the attribute {@code //@packageId},
     * with values for the full tuple (scope, identifier, revision); otherwise
     * an {@link IllegalEmlPackageIdException} will be thrown.
     *
     * @param emlDocument
     *            an EML document.
     *
     * @return the packageId of the provided EML document.
     *
     * @throws IllegalEmlPackageIdException
     *             if the packageId does not exist, cannot be parsed, or does
     *             contain all of the required values.
     */
    public static EmlPackageId getEmlPackageId(Document emlDocument) {

        String packageId = getRawEmlPackageId(emlDocument);

        EmlPackageIdFormat formatter = new EmlPackageIdFormat(Delimiter.DOT);

        EmlPackageId epi = null;

        try {
            epi = formatter.parse(packageId);
        } catch (IllegalArgumentException e) {
            String s = "The EML packageId attribute '" + packageId +
                       "' could not be parsed. The parser reported the " +
                       "following error: " + e.getMessage();
            throw new IllegalEmlPackageIdException(s, packageId, e);
        }

        if (epi.getRevision() == null) {
            String s = "The EML packageId attribute '" + packageId +
                       "' is missing a revision.";
            throw new IllegalEmlPackageIdException(s, packageId);
        }

        if (epi.getIdentifier() == null) {
            String s = "The EML packageId attribute '" + packageId +
                       "' is missing both an identifier and a revision.";
            throw new IllegalEmlPackageIdException(s, packageId);
        }

        if (epi.getScope() == null) {
            String s = "A value was not specified for the EML packageId " +
                       "attribute.";
            throw new IllegalEmlPackageIdException(s, packageId);
        }

        return epi;
    }

    /**
     * Parses the provided EML 2.1.0 string and returns a corresponding JAXB
     * object.
     *
     * @param emlString
     *            the EML string.
     * @return a JAXB object corresponding to the provided EML string.
     *
     * @throws IllegalArgumentException
     *             with a {@linkplain JAXBException} as the cause.
     *
    public static Eml getEml2_1_0(String emlString) {

        try {
            String packageName = Eml.class.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller u = jc.createUnmarshaller();
            StringReader reader = new StringReader(emlString);
            Eml eml = (Eml) u.unmarshal(reader);
            return eml;
        }
        catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }

    }*/

    /**
     * Parses the provided EML 2.1.0 {@code <access>} element string and
     * returns a corresponding JAXB object.
     *
     * @param accessString
     *            the {@code <access>} element string.
     * @return a JAXB object corresponding to the provided string.
     *
     * @throws IllegalArgumentException
     *             with a {@linkplain JAXBException} as the cause.
     */
    @SuppressWarnings("unchecked")
    public static AccessType getAccessType2_1_0(String accessString) {

        try {
            String packageName = AccessType.class.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller u = jc.createUnmarshaller();
            StringReader reader = new StringReader(accessString);

            JAXBElement<AccessType> jaxb =
                (JAXBElement<AccessType>) u.unmarshal(reader);

            return jaxb.getValue();
        }
        catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }

    }

    /**
     * Creates and returns an EML 2.1.0 {@code <access} element string that
     * corresponds to the provided JAXB object.
     *
     * @param accessType
     *            the JAXB object to be represented as a string.
     * @return an EML 2.1.0 {@code <access} element string that corresponds to
     *         the provided JAXB object.
     */
    public static String toString(AccessType accessType) {

        try {

            ObjectFactory factory = new ObjectFactory();
            JAXBElement<AccessType> jaxb = factory.createAccess(accessType);

            StringWriter writer = new StringWriter();

            String packageName = AccessType.class.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(jaxb, writer);

            return writer.toString();
        }
        catch (JAXBException e) {
            throw new IllegalStateException(e);
        }

    }
    
  	/**
  	 * Returns an EML metadata document specified by the file as a String object.
  	 * 
  	 * @param emlFile
  	 *          The EML file object.
  	 * 
  	 * @return The EML metadata document as a String object.
  	 */
  	public static String getEmlDoc(File emlFile) {

  		String eml = null;

  		try {
  			eml = FileUtils.readFileToString(emlFile);
  		} catch (IOException e) {
  			System.out.println(e);
  			e.printStackTrace();
  		}

  		return eml;

  	}

}
