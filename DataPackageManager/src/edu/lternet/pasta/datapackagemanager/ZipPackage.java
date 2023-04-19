package edu.lternet.pasta.datapackagemanager;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.ucsb.nceas.utilities.Options;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
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
    configurationListener = new ConfigurationListener();
    configurationListener.initialize(CONFIG_PATH);
    options = ConfigurationListener.getOptions();
  }

  /**
   * @return Stream of zip file bytes
   */
  public String getZipStream()
  {
    return "test";
  }

  /**
   * @return String of manifest file
   * @throws Exception
   */
  public String createManifest(
  ) throws Exception
  {
    EmlPackageId emlPackageId = new EmlPackageId(this.scope, this.identifier, this.revision);
    StringBuilder manifestBuilder = new StringBuilder();
    Date now = new Date();

    String userHash = DigestUtils.md5Hex(this.userId);
    String packageId = String.format(
        "%s.%s.%s", this.scope, this.identifier.toString(), this.revision.toString());
    String zipName = String.format("%s-%s.zip", packageId, userHash);

    manifestBuilder.append(String.format("Manifest file for %s created on %s\n", zipName, now));

    String resourceMapStr = this.dataPackageManager.readDataPackage(this.scope, this.identifier,
        this.revision.toString(), this.authToken, this.userId, false
    );

    Scanner mapScanner = new Scanner(resourceMapStr);

    while (mapScanner.hasNextLine()) {
      String line = mapScanner.nextLine();
      // metadata/eml/
      if (line.contains(URI_MIDDLE_METADATA)) {
        String emlXmlStr = getEml();
        manifestBuilder.append(String.format("%s.xml (%d bytes)\n", emlPackageId,
            emlXmlStr.getBytes(StandardCharsets.UTF_8).length
        ));
        String emlTxtStr = transformXml(emlXmlStr, XSLT_FILE_NAME);
        manifestBuilder.append(String.format("%s.txt (%d bytes)\n", emlPackageId,
            emlTxtStr.getBytes(StandardCharsets.UTF_8).length
        ));
      }
      // report/eml/
      else if (line.contains(URI_MIDDLE_REPORT)) {
        String reportStr = getReport(emlPackageId);
        String objectName = String.format("%s.report.xml", emlPackageId);
        manifestBuilder.append(String.format("%s (%d bytes)\n", objectName,
            reportStr.getBytes(StandardCharsets.UTF_8).length
        ));
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
        String entityStr = getEntity(entityId);
        manifestBuilder.append(String.format("%s (%d bytes)\n", objectName,
            entityStr.getBytes(StandardCharsets.UTF_8).length
        ));
      }
    }
    return manifestBuilder.toString();
  }

  private String transformXml(
      String xml,
      String xsltName
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
   *
   * If the user is not authorized to read the EML document, throws UnauthorizedException..
   *
   * @return the EML document as a string
   */
  private String getEml() throws SQLException, IOException, ClassNotFoundException
  {
    File f = dataPackageManager.getMetadataFile(this.scope, this.identifier,
        this.revision.toString(), this.userId, this.authToken
    );
    return FileUtils.readFileToString(f);
  }

  private String getReport(EmlPackageId emlPackageId)
      throws SQLException, IOException, ClassNotFoundException
  {
    File f = this.dataPackageManager.readDataPackageReport(this.scope, this.identifier,
        this.revision.toString(), emlPackageId, this.authToken, this.userId
    );
    return FileUtils.readFileToString(f);
  }

  private String getEntity(String entityId) throws Exception
  {
    File f = dataPackageManager.getDataEntityFile(this.scope, this.identifier,
        this.revision.toString(), entityId, this.authToken, this.userId
    );
    return FileUtils.readFileToString(f);
  }
}
