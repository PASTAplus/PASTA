/*
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.common;

import org.w3c.dom.Document;

/**
 * Used to indicate that an error occurred while parsing an XML string.
 */
public class XmlParsingException extends UserErrorException {

    private static final long serialVersionUID = 1L;

    private final String xmlString;

    /**
     * Constructs a new XML parsing exception.
     *
     * @param msg the descriptive error message.
     * @param cause the exception that indicated the parsing error.
     * @param xmlString the XML that was being parsed.
     */
    public XmlParsingException(String msg, Throwable cause, String xmlString) {
        super(msg, cause);
        this.xmlString = xmlString;
    }

    /**
     * Constructs a new XML parsing exception.
     *
     * @param msg the descriptive error message.
     * @param xml the XML that was being parsed.
     */
    public XmlParsingException(String msg, Document xml) {
        super(msg);
        this.xmlString = XmlUtility.nodeToXmlString(xml);
    }

    /**
     * Returns the XML string for which a parsing error occurred.
     * @return the XML string for which a parsing error occurred.
     */
    public String getXml() {
        return xmlString;
    }
}
