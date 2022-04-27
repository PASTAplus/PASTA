package edu.lternet.pasta.datapackagemanager;

import java.util.ArrayList;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.log4j.Logger;


/**
 * Returns a list of EML "annotation" node elements that contain a "references" attribute
 */
public class EMLAnnotationReferences {

    private static final Logger logger = Logger.getLogger(EMLAnnotationReferences.class);
    private static final String ANNOTATION = "//annotation";

    private ArrayList<Node> annotationReferences = new ArrayList<Node>();

    public EMLAnnotationReferences(Document document) {
        if (document != null) {
            try {
                NodeList annotations = XPathAPI.selectNodeList(document, ANNOTATION);
                for (int i = 0; i < annotations.getLength(); i++) {
                    String references = ((Element)annotations.item(i)).getAttribute("references");
                    if (!references.isEmpty()) {
                        annotationReferences.add(annotations.item(i));
                    }
                }
            } catch (TransformerException exception) {
                logger.error(exception);
            }
        }
        else {
            String gripe = "EML document is null";
            throw new NullPointerException(gripe);
        }
    }

    public ArrayList<Node> getReferences() {
        return this.annotationReferences;
    }

}
