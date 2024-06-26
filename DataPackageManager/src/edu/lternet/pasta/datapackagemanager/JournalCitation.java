package edu.lternet.pasta.datapackagemanager;

import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.doi.DigitalObjectIdentifier;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.json.JSONObject;
import org.owasp.encoder.Encode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

public class JournalCitation {

    public static class ArticleAuthor {
        Integer sequence;
        String given;
        String family;
        String suffix;
        String shortOrcid;

        public ArticleAuthor(Integer sequence, String given, String family, String suffix, String orcid) {
            orcid = emptyToNull(orcid);
            String shortOrcid = orcidUrlToShort(orcid);
            assertOrcidCheckDigit(shortOrcid);
            this.sequence = sequence;
            this.given = emptyToNull(given);
            this.family = emptyToNull(family);
            this.suffix = emptyToNull(suffix);
            this.shortOrcid = emptyToNull(shortOrcid);
        }

        private String emptyToNull(String s) {
            if (s == null || s.isEmpty()) {
                return null;
            }
            return s;
        }
        public Integer getSequence() {
            return sequence;
        }

        public String getGiven() {
            return given;
        }

        public String getFamily() {
            return family;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getShortOrcid() {
            return shortOrcid;
        }

        public String getOrcidUrl() {
            return orcidShortToUrl(shortOrcid);
        }

        public static String orcidUrlToShort(String orcidUrl) {
            if (orcidUrl != null) {
                String orcidShortStr = orcidUrl.replaceAll("^https?://orcid.org/|-", "");
                assert orcidShortStr.length() == 16;
                return orcidShortStr;
            }
            return null;
        }

        public static String orcidShortToUrl(String orcidStr) {
            if (orcidStr != null) {
                assert orcidStr.length() == 16;
                String formattedOrcid = orcidStr.replaceAll("(.{4})(?!$)", "$1-");
                return "https://orcid.org/" + formattedOrcid;
            }
            return null;
        }

        public static void assertOrcidCheckDigit(String orcidStr) {
            assert orcidStr == null || orcidStr.charAt(15) == generateOrcidCheckDigit(orcidStr);
        }

        public static char generateOrcidCheckDigit(String orcidStr) {
            assert orcidStr.length() == 16;
            int total = 0;
            for (int i = 0; i < 15; i++) {
                int digit = Character.getNumericValue(orcidStr.charAt(i));
                total = (total + digit) * 2;
            }
            int remainder = total % 11;
            int result = (12 - remainder) % 11;
            return (result == 10) ? 'X' : (char) ('0' + result);
        }
    }

    /*
     * Class variables
     */
    
    private static Logger logger = Logger.getLogger(JournalCitation.class);

    
    /*
     * Instance variables
     */
    
    Integer journalCitationId;
    String articleTitle;
    String articleDoi;
    String articleUrl;
    String principalOwner;
    LocalDateTime dateCreated;
    String packageId;
    String journalTitle;
    Integer journalPubYear;
    String relationType;
    ArrayList<ArticleAuthor> articleAuthorList = new ArrayList<>();
    String journalIssue;
    String journalVolume;
    String articlePages;

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
                sb.append("    <pubDate>2020</pubDate>\n");
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

            /* The journalCitationId is set during database insert, so any value here is essentially ignored */
            Node journalCitationIdNode = xpathapi.selectSingleNode(document, "//journalCitationId");
            if (journalCitationIdNode != null) {
              String journalCitationIdStr = journalCitationIdNode.getTextContent();
              if (journalCitationIdStr != null && !journalCitationIdStr.isEmpty()) {
                  setJournalCitationId(Integer.parseInt(journalCitationIdStr));
              }
            }
            
            Node packageIdNode = xpathapi.selectSingleNode(document, "//packageId");
            if (packageIdNode != null) {
              String packageId = packageIdNode.getTextContent();
              setPackageId(packageId);
            }

            boolean doiOrUrl = false;

            Node articleDoiNode = xpathapi.selectSingleNode(document, "//articleDoi");
            if (articleDoiNode != null) {
              String articleDoi = articleDoiNode.getTextContent();
              if (articleDoi.isEmpty()) {
                  setArticleDoi(articleDoi);
              }
              else if (DigitalObjectIdentifier.isRawDoi(articleDoi)) {
                  setArticleDoi(articleDoi);
                  doiOrUrl = true;
              }
              else {
                  String gripe = String.format("DOI %s should be in \"prefix/suffix\" format (e.g., 10.6073/d4f26a6c)", articleDoi.trim());
                  logger.error(gripe);
                  throw new UserErrorException(gripe);
              }
            }

            Node articleUrlNode = xpathapi.selectSingleNode(document, "//articleUrl");
            if (articleUrlNode != null) {
              String articleUrl = articleUrlNode.getTextContent();
              if (articleUrl.isEmpty()) {
                  setArticleUrl(articleUrl);
              } else {
                  setArticleUrl(articleUrl);
                  doiOrUrl = true;
              }
            }

            if (!doiOrUrl) {
                String gripe = "Either an article DOI or URL must be set to a valid value";
                logger.error(gripe);
                throw new UserErrorException(gripe);
            }

            Node articleTitleNode = xpathapi.selectSingleNode(document, "//articleTitle");
            if (articleTitleNode != null) {
              String articleTitle = articleTitleNode.getTextContent();
              setArticleTitle(articleTitle);
            }

            Node journalTitleNode = xpathapi.selectSingleNode(document, "//journalTitle");
            if (journalTitleNode != null) {
              String journalTitle = journalTitleNode.getTextContent();
              setJournalTitle(journalTitle);
            }

            Node relationTypeNode = xpathapi.selectSingleNode(document, "//relationType");
            if (relationTypeNode != null) {
              String relationType = relationTypeNode.getTextContent();
              if (relationType.isEmpty()) {
                  String gripe = "A valid relation type is required (IsCitedBy, IsDescribedBy, or IsReferencedBy";
                  logger.error(gripe);
                  throw new UserErrorException(gripe);
              }
              setRelationType(relationType);
            }
            else {
                String gripe = "Relation type cannot be empty";
                logger.error(gripe);
                throw new UserErrorException(gripe);
            }

            Node pubDateNode = xpathapi.selectSingleNode(document, "//pubDate");
            if (pubDateNode != null) {
              String pubDate = pubDateNode.getTextContent();
              setJournalPubYear(pubDate);
            }

            Node articleAuthorNode = xpathapi.selectSingleNode(document, "//articleAuthors");
            if (articleAuthorNode != null) {
                setArticleAuthorList(articleAuthorNode);
            }

            Node journalIssueNode = xpathapi.selectSingleNode(document, "//journalIssue");
            if (journalIssueNode != null) {
              String journalIssue = journalIssueNode.getTextContent();
              setJournalIssue(journalIssue);
            }

            Node journalVolumeNode = xpathapi.selectSingleNode(document, "//journalVolume");
            if (journalVolumeNode != null) {
              String journalVolume = journalVolumeNode.getTextContent();
              setJournalVolume(journalVolume);
            }

            Node articlePagesNode = xpathapi.selectSingleNode(document, "//articlePages");
            if (articlePagesNode != null) {
              String articlePages = articlePagesNode.getTextContent();
              setArticlePages(articlePages);
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
        StringBuilder xmlBuilder = new StringBuilder(firstLine);
        xmlBuilder.append("<journalCitation>\n");
        xmlBuilder.append(String.format("    <journalCitationId>%s</journalCitationId>\n", encodeForXml(this.journalCitationId)));
        xmlBuilder.append(String.format("    <packageId>%s</packageId>\n", encodeForXml(this.packageId)));
        xmlBuilder.append(String.format("    <principalOwner>%s</principalOwner>\n", encodeForXml(this.principalOwner)));
        xmlBuilder.append(String.format("    <dateCreated>%s</dateCreated>\n", encodeForXml(getDateCreatedStr())));
        xmlBuilder.append(String.format("    <articleDoi>%s</articleDoi>\n", encodeForXml(this.articleDoi)));
        xmlBuilder.append(String.format("    <articleTitle>%s</articleTitle>\n", encodeForXml(this.articleTitle)));
        xmlBuilder.append(String.format("    <articleUrl>%s</articleUrl>\n", encodeForXml(this.articleUrl)));
        xmlBuilder.append(String.format("    <journalTitle>%s</journalTitle>\n", encodeForXml(this.journalTitle)));
        xmlBuilder.append(String.format("    <relationType>%s</relationType>\n", encodeForXml(this.relationType)));
        xmlBuilder.append(String.format("    <pubDate>%s</pubDate>\n", encodeForXml(this.journalPubYear)));
        xmlBuilder.append(String.format("    <journalIssue>%s</journalIssue>\n", encodeForXml(this.journalIssue)));
        xmlBuilder.append(String.format("    <journalVolume>%s</journalVolume>\n", encodeForXml(this.journalVolume)));
        xmlBuilder.append(String.format("    <articlePages>%s</articlePages>\n", encodeForXml(this.articlePages)));
        xmlBuilder.append("    <articleAuthors>\n");
        if (this.articleAuthorList != null) {
            for (ArticleAuthor author : this.articleAuthorList) {
                xmlBuilder.append("        <author>\n");
                xmlBuilder.append(String.format("            <sequence>%s</sequence>\n", encodeForXml(author.getSequence())));
                xmlBuilder.append(String.format("            <given>%s</given>\n", encodeForXml(author.getGiven())));
                xmlBuilder.append(String.format("            <family>%s</family>\n", encodeForXml(author.getFamily())));
                xmlBuilder.append(String.format("            <suffix>%s</suffix>\n", encodeForXml(author.getSuffix())));
                xmlBuilder.append(String.format("            <orcid>%s</orcid>\n", encodeForXml(author.getOrcidUrl())));
                xmlBuilder.append("        </author>\n");
            }
        }
        xmlBuilder.append("    </articleAuthors>\n");
        xmlBuilder.append("</journalCitation>\n");
        return xmlBuilder.toString();
    }

    String encodeForXml(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return Encode.forXml(s);
    }

    String encodeForXml(Integer i) {
        return encodeForXml(i == null ? "" : i.toString());
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

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Integer getJournalPubYear() {
        return this.journalPubYear;
    }

    public void setJournalPubYear(Integer journalPubYear) {
        // ResultSet.getInt() returns 0 if the DB value is null.
        this.journalPubYear = journalPubYear == 0 ? null : journalPubYear;
    }

    /**
     * Set journalPubYear from pubDate String in YYYY-MM-DD or YYYY format.
     */
    public void setJournalPubYear(String pubDate) {
        if (pubDate == null || pubDate.isEmpty()) {
            this.journalPubYear = null;
            return;
        }

        Pattern pubYearMonthDayPattern = Pattern.compile("^(\\d{4})-\\d{2}-\\d{2}$");
        Matcher pubYearMonthDayMatcher = pubYearMonthDayPattern.matcher(pubDate);
        if (pubYearMonthDayMatcher.matches()) {
            this.journalPubYear = Integer.valueOf(pubYearMonthDayMatcher.group(1));
            return;
        }

        Pattern pubYearPattern = Pattern.compile("^(\\d{4})$");
        Matcher pubYearMatcher = pubYearPattern.matcher(pubDate);
        if (pubYearMatcher.matches()) {
            this.journalPubYear = Integer.valueOf(pubYearMatcher.group(1));
            return;
        }

        String errorMsg = String.format("Error extracting year from PubDate: %s", pubDate);
        logger.error(errorMsg);
        throw new RuntimeException(errorMsg);
    }

    private void setArticleAuthorList(Node articleAuthorNode) throws TransformerException {
        CachedXPathAPI xpathapi = new CachedXPathAPI();
        NodeList authorNodeList = xpathapi.selectNodeList(articleAuthorNode, "author");

        articleAuthorList.clear();

        for (int i = 0; i < authorNodeList.getLength(); i++) {
            Node authorNode = authorNodeList.item(i);
            Node node;
            node = xpathapi.selectSingleNode(authorNode, "sequence");
            Integer sequence = node != null ? Integer.parseInt(node.getTextContent()) : null;
            node = xpathapi.selectSingleNode(authorNode, "given");
            String given = node != null ? node.getTextContent() : null;
            node = xpathapi.selectSingleNode(authorNode, "family");
            String family = node != null ? node.getTextContent() : null;
            node = xpathapi.selectSingleNode(authorNode, "suffix");
            String suffix = node != null ? node.getTextContent() : null;
            node = xpathapi.selectSingleNode(authorNode, "orcid");
            String orcid = node != null ? node.getTextContent() : null;
            articleAuthorList.add(new ArticleAuthor(sequence, given, family, suffix, orcid));
        }
    }

    public void setArticleAuthorList(JSONArray authors) throws SQLException {
        for (int i = 0; i < authors.length(); i++) {
            JSONObject author = authors.getJSONObject(i);
            // If there are no authors for a given citation, Postgres returns a single author with all null values
            // instead of an empty array. So we have to check for the empty case and skip the row.
            if (author.isNull("f1")) { // id
                continue;
            }
            Integer sequence = author.getInt("f2"); // sequence
            String given = author.optString("f3"); // given
            String family = author.optString("f4"); // family
            String suffix = author.optString("f5"); // suffix
            String orcid = author.optString("f6"); // orcid
            articleAuthorList.add(new ArticleAuthor(sequence, given, family, suffix, orcid));
        }
    }

    public void setArticleAuthorList(ArrayList<ArticleAuthor> articleAuthorList) {
        this.articleAuthorList = articleAuthorList;
    }

    public ArrayList<ArticleAuthor> getArticleAuthorList() {
        return articleAuthorList;
    }


    public void addArticleAuthor(ArticleAuthor articleAuthor) {
        this.articleAuthorList.add(articleAuthor);
    }

    public String getJournalIssue() {
        return journalIssue;
    }

    public void setJournalIssue(String journalIssue) {
        this.journalIssue = journalIssue;
    }

    public String getJournalVolume() {
        return journalVolume;
    }

    public void setJournalVolume(String journalVolume) {
        this.journalVolume = journalVolume;
    }

    public String getArticlePages() {
        return articlePages;
    }

    public void setArticlePages(String articlePages) {
        this.articlePages = articlePages;
    }
}
