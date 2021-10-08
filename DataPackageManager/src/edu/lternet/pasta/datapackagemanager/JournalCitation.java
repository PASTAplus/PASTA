package edu.lternet.pasta.datapackagemanager;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import edu.lternet.pasta.common.UserErrorException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.owasp.encoder.Encode;

import edu.lternet.pasta.doi.DigitalObjectIdentifier;

public class JournalCitation {
    
    /*
     * Class variables
     */
    
    private static Logger logger = Logger.getLogger(JournalCitation.class);

    
    /*
     * Instance variables
     */
    
    int journalCitationId;
    String articleTitle;
    String articleDoi;
    String articleUrl;
    String principalOwner;
    LocalDateTime dateCreated;
    String packageId;
    String journalTitle;
    

    
    public static void main(String[] args) {
        String packageId = args[0];
        String dirPath = "WebRoot/WEB-INF/conf";
        boolean includeDeclaration = true;
        try {
            if (args != null && args.length >= 1) {
                ConfigurationListener configurationListener = new ConfigurationListener();
                configurationListener.initialize(dirPath);
                DataPackageManager dpm = new DataPackageManager();
                String userId = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
                StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                sb.append("<journalCitation>\n");
                sb.append(
            String.format("    <packageId>%s</packageId>\n", packageId));
                sb.append("    <articleDoi>10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae</articleDoi>\n");
                sb.append("    <articleTitle>Mesquite Tree Survey in Southern Arizona</articleTitle>\n");
                //sb.append("    <articleUrl>http://swtreejournal.com/articles/12345</articleUrl>\n");
                sb.append("    <journalTitle>Journal of Southwest Trees</journalTitle>\n");
                sb.append("</journalCitation>\n");
                String requestXML = sb.toString();
                JournalCitation journalCitation = dpm.createJournalCitation(userId, requestXML);
                System.out.println(journalCitation.toXML(includeDeclaration));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    /*
     * Constructors
     */
    
    /**
     * Create a new JournalCitation object. The empty constructor.
     * 
      */
    public JournalCitation() {
        super();
    }
    
    
    /**
     * Create a new JournalCitation object by parsing the journal citation XML string.
     * 
     * @param xml   an XML string that conforms to the journal citation format, typically sent in
     *              a web service request body
     */
    public JournalCitation(String xml) throws Exception {
        parseDocument(xml);
    }
    
    

    
    /*
     * Instance methods
     */
    
    
    /**
     * Parses an EML document.
     * 
     * @param   xml          The XML string representation of the EML document
     * @return  dataPackage  a DataPackage object holding parsed values
     */
    private void parseDocument(String xml) throws Exception {
      if (xml != null) {
        try {
          InputStream inputStream = IOUtils.toInputStream(xml, "UTF-8");
          parseDocument(inputStream);
        }
        catch (Exception e) {
          logger.error("Error parsing journal citation metadata: " + e.getMessage());
          throw e;
        }
      }
    }

 
    /**
     * Parses an EML document.
     * 
     * @param   inputStream          the input stream to the EML document
     * @return  dataPackage          a DataPackage object holding parsed values
     */
    private void parseDocument(InputStream inputStream) 
            throws Exception {
      
      DocumentBuilder documentBuilder = 
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
      CachedXPathAPI xpathapi = new CachedXPathAPI();

      Document document = null;

      try {
        document = documentBuilder.parse(inputStream);
        
        if (document != null) {

            Node journalCitationIdNode = xpathapi.selectSingleNode(document, "//journalCitationId");
            if (journalCitationIdNode != null) {
              String journalCitationIdStr = journalCitationIdNode.getTextContent();
              setJournalCitationId(Integer.parseInt(journalCitationIdStr));
            }
            
            Node packageIdNode = xpathapi.selectSingleNode(document, "//packageId");
            if (packageIdNode != null) {
              String packageId = packageIdNode.getTextContent();
              setPackageId(packageId);
            }
            
            Node articleDoiNode = xpathapi.selectSingleNode(document, "//articleDoi");
            if (articleDoiNode != null) {
              String articleDoi = articleDoiNode.getTextContent();
              if (articleDoi.isEmpty()) {
                  setArticleDoi(articleDoi);
              }
              else if (DigitalObjectIdentifier.isRawDoi(articleDoi)) {
                  setArticleDoi(articleDoi);
              }
              else {
                  throw new UserErrorException(String.format("DOI %s should be in \"prefix/suffix\" format (e.g., 10.6073/d4f26a6c)", articleDoi.trim()));
              }
            }

            Node articleTitleNode = xpathapi.selectSingleNode(document, "//articleTitle");
            if (articleTitleNode != null) {
              String articleTitle = articleTitleNode.getTextContent();
              setArticleTitle(articleTitle);
            }

            Node articleUrlNode = xpathapi.selectSingleNode(document, "//articleUrl");
            if (articleUrlNode != null) {
              String articleUrl = articleUrlNode.getTextContent();
              setArticleUrl(articleUrl);
            }

            Node journalTitleNode = xpathapi.selectSingleNode(document, "//journalTitle");
            if (journalTitleNode != null) {
              String journalTitle = journalTitleNode.getTextContent();
              setJournalTitle(journalTitle);
            }

        }
      }
      catch (SAXException e) {
          logger.error("Error parsing document: SAXException");
          e.printStackTrace();
          throw(e);
        } 
        catch (IOException e) {
          logger.error("Error parsing document: IOException");
          e.printStackTrace();
          throw(e);
        }
        catch (TransformerException e) {
          logger.error("Error parsing document: TransformerException");
          e.printStackTrace();
          throw(e);
        }
    }
    
    
    /**
     * Composes the XML representation of this JournalCitation object 
     * 
     * @return  an XML string representation
     */
    public String toXML(boolean includeDeclaration) {
        String firstLine = includeDeclaration ? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" : "";
        StringBuilder sb = new StringBuilder(firstLine);
        sb.append("<journalCitation>\n");
        
        if (this.journalCitationId > 0)
            { sb.append(String.format("    <journalCitationId>%d</journalCitationId>\n", this.journalCitationId)); } 
        
        sb.append(String.format("    <packageId>%s</packageId>\n", this.packageId)); 
        sb.append(String.format("    <principalOwner>%s</principalOwner>\n", this.principalOwner)); 
        sb.append(String.format("    <dateCreated>%s</dateCreated>\n", getDateCreatedStr())); 
        
        if (this.articleDoi != null)
            { sb.append(String.format("    <articleDoi>%s</articleDoi>\n", Encode.forXml(this.articleDoi))); }
        
        if (this.articleTitle != null)
            { sb.append(String.format("    <articleTitle>%s</articleTitle>\n", Encode.forXml(this.articleTitle))); }
        
        if (this.articleUrl != null)
            { sb.append(String.format("    <articleUrl>%s</articleUrl>\n", Encode.forXml(this.articleUrl))); }
    
        if (this.journalTitle != null)
            { sb.append(String.format("    <journalTitle>%s</journalTitle>\n", Encode.forXml(this.journalTitle))); }
        
        sb.append("</journalCitation>\n");

        String xml = sb.toString();
        return xml;
    }
    
    
    private String getDateCreatedStr() {
        String dateCreatedStr = "";
        if (this.dateCreated != null) {
            dateCreatedStr = dateCreated.toString();
        }
        
        return dateCreatedStr;
    }
    
    
    /*
     * Accessors
     */
    
    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleDoi() {
        return articleDoi;
    }

    public void setArticleDoi(String articleDoi) {
        this.articleDoi = articleDoi;
    }
    
    public String getArticleUrl() {
        String url = null;
        
        if (this.articleUrl != null && !this.articleUrl.equals("")) {
            url = articleUrl;
        }
        else {
            url = deriveUrlFromDoi();
        }
        
        return url;
    }
    
    private String deriveUrlFromDoi() {
        String url = null;
        
        if (this.articleDoi != null) {
        	if (this.articleDoi.startsWith("http")) {
        		url = this.articleDoi;
        	}
        	else if (this.articleDoi.startsWith("doi.org/")) {
        		url = String.format("https://%s", this.articleDoi);
        	}
        	else {
        		url = String.format("https://doi.org/%s", this.articleDoi);
        	}
        }
            
        return url;
    }

    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }
    
    public int getJournalCitationId() {
        return journalCitationId;
    }
    
    public void setJournalCitationId(int val) {
        this.journalCitationId = val;
    }

    public String getPrincipalOwner() {
        return principalOwner;
    }

    public void setPrincipalOwner(String principalOwner) {
        this.principalOwner = principalOwner;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime localDateTime) {
        this.dateCreated = localDateTime;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public void setJournalTitle(String journalTitle) {
        this.journalTitle = journalTitle;
    }

}
