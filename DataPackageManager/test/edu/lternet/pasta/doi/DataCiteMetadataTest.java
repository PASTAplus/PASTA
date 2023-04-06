package edu.lternet.pasta.doi;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.eml.EMLParser;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.common.eml.Title;
import edu.lternet.pasta.datapackagemanager.JournalCitation;
import junit.framework.TestCase;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DataCiteMetadataTest extends TestCase {
  public static final String DATA_CITE_METADATA_SAMPLE_XML =
      "test/data/DataCiteMetadataSample.xml";

  public void testToDataCiteXml() throws Exception
  {
    DataCiteMetadata dcm = new DataCiteMetadata();

    DigitalObjectIdentifier doi = new DigitalObjectIdentifier("baadfoodbaadfoodbaadfoodbaadfood");
    dcm.setDigitalObjectIdentifier(doi);

    ResourceType resourceType = new ResourceType("Dataset");
    dcm.setResourceType(resourceType);

    // dcm.setAlternateIdentifier("doi:10.6073/pasta/123456789");
    dcm.setPublicationYear("2018");
    dcm.setLocationUrl("https://pasta.lternet.edu/package/data/eml/knb-lter-lno/1/1");

    Title title = new Title();
    title.setTitle("A new method for estimating the global oceanic heat content");
    title.setTitleType("Subtitle");

    ArrayList<Title> titles = new ArrayList<>();
    titles.add(title);

    dcm.setTitles(titles);

    JournalCitation jc = new JournalCitation();
    jc.setArticleDoi("10.1002/2017GL075000");
    jc.setArticleTitle("A new method for estimating the global oceanic heat content");
    jc.setArticleUrl("https://agupubs.onlinelibrary.wiley.com/doi/abs/10.1002/2017GL075000");
    jc.setDateCreated(LocalDateTime.now());
    jc.setJournalCitationId(1);
    jc.setJournalTitle("Geophysical Research Letters");
    jc.setPackageId("knb-lter-lno.1.1");
    jc.setPrincipalOwner("LNO");
    jc.setRelationType("IsCitedBy");

    ArrayList<JournalCitation> citations = new ArrayList<>();
    citations.add(jc);

    dcm.addJournalCitations(citations);

    AlternateIdentifier alternateIdentifier = new AlternateIdentifier(AlternateIdentifier.URL);
    alternateIdentifier.setAlternateIdentifier("knb-lter-and/2719/6");

    EMLParser emlParser = new EMLParser();
    ResponsibleParty rp = new ResponsibleParty(emlParser, "creator");
    rp.setOrganizationName("An Organization Name");
    rp.setPositionName("A Position Name");
    rp.addGivenName("A Given Name 1");
    rp.addGivenName("A Given Name 2");
    rp.setSurName("A Surname");

    rp.setUserId("https://orcid.org/0000-0003-2604-299X");
    rp.setUserIdDirectory("http://orcid.org");

    ArrayList<ResponsibleParty> responsiblePartyArrayList = new ArrayList<>();
    responsiblePartyArrayList.add(rp);
    dcm.setCreators(responsiblePartyArrayList);

    File emlStream = FileUtility.assertCanRead(DATA_CITE_METADATA_SAMPLE_XML);
    String expectedXml = FileUtility.fileToString(emlStream);
    String actualXml = dcm.toDataCiteXml();
    assertEquals(expectedXml.trim(), actualXml.trim());
  }
}