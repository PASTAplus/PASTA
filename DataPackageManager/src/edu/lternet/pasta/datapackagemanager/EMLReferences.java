package edu.lternet.pasta.datapackagemanager;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import edu.lternet.pasta.dml.quality.QualityReport;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.log4j.Logger;


/**
 * Returns a list of EML "references" node elements
 */
public class EMLReferences {

    private static final Logger logger = Logger.getLogger(EMLReferences.class);
    private static final String REFERENCES = "//references";

    private NodeList references;

    public EMLReferences(Document document) {
        if (document != null) {
            try {
                this.references = XPathAPI.selectNodeList(document, REFERENCES);
            } catch (TransformerException exception) {
                logger.error(exception);
            }
        }
        else {
            String gripe = "EML document is null";
            throw new NullPointerException(gripe);
        }
    }

    public NodeList getReferences() {
        return this.references;
    }

}
