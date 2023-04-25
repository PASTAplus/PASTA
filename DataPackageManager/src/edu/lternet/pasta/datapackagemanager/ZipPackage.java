package edu.lternet.pasta.datapackagemanager;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.ucsb.nceas.utilities.Options;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


public class ZipPackage {
  private static final String URI_MIDDLE_DATA = "data/eml/";
  private static final String URI_MIDDLE_METADATA = "metadata/eml/";
  private static final String URI_MIDDLE_REPORT = "report/eml/";
  private static final String XSLT_FILE_NAME = "eml_text-21.xsl";
  private static final String CONFIG_PATH = "WebRoot/WEB-INF/conf";
  private static final Logger logger = Logger.getLogger(ZipPackage.class);

  private static ConfigurationListener configurationListener = null;
  private static Options options = null;

  String scope;
  Integer identifier;
  Integer revision;
  String userId;
  AuthToken authToken;
  DataPackageManager dataPackageManager;
  String xslDir;

  public ZipPackage(
      DataPackageManager dataPackageManager,
      String scope,
      Integer identifier,
      Integer revision,
      String userId,
      AuthToken authToken
  )
  {
    this.dataPackageManager = dataPackageManager;
    this.scope = scope;
    this.identifier = identifier;
    this.revision = revision;
    this.userId = userId;
    this.authToken = authToken;

    initConfig();
    xslDir = options.getOption("datapackagemanager.xslDir");
  }

  private void initConfig()
  {
    options = ConfigurationListener.getOptions();
    if (options == null) {
      configurationListener = new ConfigurationListener();
      configurationListener.initialize(CONFIG_PATH);
      options = ConfigurationListener.getOptions();
    }
  }

  public void writeZip(OutputStream outputStream) throws Exception
  {
    List<ZipMember> zipMemberList = create();
    ZipStreamer z = new ZipStreamer();
    z.zipStream(outputStream, zipMemberList);
  }


  public List<ZipMember> create(
  ) throws Exception
  {
    List<ZipMember> zipMemberList = new ArrayList<>();

    EmlPackageId emlPackageId = new EmlPackageId(this.scope, this.identifier, this.revision);
    StringBuilder manifestBuilder = new StringBuilder();
    Date now = new Date();
    String zipName = String.format(
        "%s.%s.%s.zip", this.scope, this.identifier.toString(), this.revision.toString());
    manifestBuilder.append(String.format("Manifest file for %s created on %s\n", zipName, now));

    String resourceMapStr = this.dataPackageManager.readDataPackage(this.scope, this.identifier,
        this.revision.toString(), this.authToken, this.userId, false
    );

    Scanner mapScanner = new Scanner(resourceMapStr);

    while (mapScanner.hasNextLine()) {
      String line = mapScanner.nextLine();
      // metadata/eml/
      if (line.contains(URI_MIDDLE_METADATA)) {
        // Add the EML XML file
        File emlXmlFile = getEmlFile();
        String emlXmlName = String.format("%s.xml", emlPackageId);
        long emlXmlSize = emlXmlFile.length();
        manifestBuilder.append(String.format("%s (%d bytes)\n", emlXmlName, emlXmlSize));
        zipMemberList.add(new ZipMember(emlXmlName, emlXmlFile.toPath()));
        // Add plain text representation of the EML XML file
        String emlXmlStr = getEml();
        String emlTxtStr = transformXml(emlXmlStr, XSLT_FILE_NAME);
        String emlTxtName = String.format("%s.txt", emlPackageId);
        manifestBuilder.append(String.format("%s (%d bytes)\n", emlTxtName,
            emlTxtStr.getBytes(StandardCharsets.UTF_8).length
        ));
        zipMemberList.add(new ZipMember(emlTxtName, emlTxtStr.getBytes(StandardCharsets.UTF_8)));
      }
      // report/eml/
      else if (line.contains(URI_MIDDLE_REPORT)) {
        File reportFile = getReportFile(emlPackageId);
        String reportName = String.format("%s.report.xml", emlPackageId);
        long reportSize = reportFile.length();
        manifestBuilder.append(String.format("%s (%d bytes)\n", reportName, reportSize));
        zipMemberList.add(new ZipMember(reportName, reportFile.toPath()));
      }
      // data/eml/
      else if (line.contains(URI_MIDDLE_DATA)) {
        String[] lineParts = line.split("/");
        String entityId = lineParts[lineParts.length - 1];
        String dataPackageResourceId = DataPackageManager.composeResourceId(
            DataPackageManager.ResourceType.dataPackage, this.scope, this.identifier, this.revision,
            null
        );
        String entityResourceId = DataPackageManager.composeResourceId(
            DataPackageManager.ResourceType.data, this.scope, this.identifier, this.revision,
            entityId
        );
        String metaXml;
        String entityName;
        try {
          metaXml = dataPackageManager.readMetadata(
              scope, identifier, revision.toString(), userId, authToken);
          entityName = dataPackageManager.readDataEntityName(
              dataPackageResourceId, entityResourceId, authToken);
        } catch (UnauthorizedException e) {
          logger.error(e.getMessage());
          manifestBuilder.append("<unknown object> (access denied)\n");
          continue;
        }
        String objectName = dataPackageManager.findObjectName(metaXml, entityName);
        File dataFile = getDataFile(entityId);
        long dataSize = dataFile.length();
        manifestBuilder.append(String.format("%s (%d bytes)\n", objectName, dataSize));
        zipMemberList.add(new ZipMember(objectName, dataFile.toPath()));
      }
    }

    zipMemberList.add(new ZipMember("manifest.txt", manifestBuilder
        .toString()
        .getBytes(StandardCharsets.UTF_8)));

    return zipMemberList;
  }

  private String transformXml(
      String xml,
      @SuppressWarnings("SameParameterValue") String xsltName
  ) throws TransformerException
  {
    String xslPath = String.format("%s/%s", this.xslDir, xsltName);
    File styleSheet = new File(xslPath);
    StringReader stringReader = new StringReader(xml);
    StringWriter stringWriter = new StringWriter();
    StreamSource styleSource = new StreamSource(styleSheet);
    Result result = new StreamResult(stringWriter);
    Source source = new StreamSource(stringReader);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer(styleSource);
    transformer.transform(source, result);
    return stringWriter.toString();
  }

  /**
   * Get EML metadata doc as a string.
   * <p>
   * If the user is not authorized to read the EML document, throws UnauthorizedException..
   *
   * @return the EML document as a string
   */
  public String getEml() throws SQLException, IOException, ClassNotFoundException
  {
    return FileUtils.readFileToString(getEmlFile());
  }

  public File getEmlFile() throws ClassNotFoundException, SQLException, IOException
  {
    return getMetadataFile();
  }

  public File getMetadataFile() throws ClassNotFoundException, SQLException, IOException
  {
    return dataPackageManager.getMetadataFile(this.scope, this.identifier, this.revision.toString(),
        this.userId, this.authToken
    );
  }

  public String getReport(EmlPackageId emlPackageId)
      throws SQLException, IOException, ClassNotFoundException
  {
    return FileUtils.readFileToString(getReportFile(emlPackageId));
  }

  public File getReportFile(EmlPackageId emlPackageId)
      throws ClassNotFoundException, SQLException, IOException
  {
    return this.dataPackageManager.readDataPackageReport(this.scope, this.identifier,
        this.revision.toString(), emlPackageId, this.authToken, this.userId
    );
  }

  public String getEntity(String entityId) throws Exception
  {
    return FileUtils.readFileToString(getDataFile(entityId));
  }

  public File getDataFile(String entityId) throws Exception
  {
    return dataPackageManager.getDataEntityFile(this.scope, this.identifier,
        this.revision.toString(), entityId, this.authToken, this.userId
    );
  }
}

// Represent a zip entry and its corresponding file path.
// The name is required, along with either a file path or an input stream.
class ZipMember {
  String name;
  Path path;
  InputStream stream;
  Integer streamSize;
  byte[] bytes;

  public ZipMember(
      String name,
      Path file
  )
  {
    this.name = name;
    this.path = file;
  }

  public ZipMember(
      String name,
      InputStream stream,
      Integer streamSize
  )
  {
    this.name = name;
    this.stream = stream;
    this.streamSize = streamSize;
  }

  public ZipMember(
      String name,
      byte[] bytes
  )
  {
    this.name = name;
    this.bytes = bytes;
  }

  public String getName()
  {
    return name;
  }

  public Path getPath()
  {
    return path;
  }

  public InputStream getStream()
  {
    return stream;
  }

  public byte[] getBytes()
  {
    return bytes;
  }

  public Integer getStreamSize()
  {
    return streamSize;
  }

  public boolean isPath()
  {
    return path != null;
  }

  public boolean isStream()
  {
    return stream != null;
  }

  public boolean isBytes()
  {
    return bytes != null;
  }
}
