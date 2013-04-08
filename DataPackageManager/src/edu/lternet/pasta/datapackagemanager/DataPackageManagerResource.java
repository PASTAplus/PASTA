/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.utilities.Options;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.PastaWebService;
import edu.lternet.pasta.common.PercentEncoder;
import edu.lternet.pasta.common.QueryString;
import edu.lternet.pasta.common.ResourceDeletedException;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.common.WebExceptionFactory;
import edu.lternet.pasta.common.WebResponseFactory;
import edu.lternet.pasta.common.XmlParsingException;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;
import edu.lternet.pasta.common.audit.AuditManagerClient;
import edu.lternet.pasta.common.audit.AuditRecord;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EMLParser;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.authorization.AccessMatrix;
import edu.lternet.pasta.common.security.authorization.InvalidPermissionException;
import edu.lternet.pasta.common.security.authorization.Rule;
import edu.lternet.pasta.common.security.token.AttrListAuthTokenV1;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager.ResourceType;
import edu.lternet.pasta.metadatafactory.MetadataFactory;

/**
 * <p>The Data Package Manager Web Service provides a suite of operations 
 * to create, evaluate, read, update, delete, list, and search data package
 * resources in the PASTA system. Data package resources include metadata 
 * documents, data entities, and quality reports.
 * 
 * @webservicename Data Package Manager
 * @baseurl https://package.lternet.edu/package
 *
 * @author dcosta
 * @version 1.0
 * @created 16-Aug-2011 1:40:03 PM
 *
 * <p>
 * The Provenance Factory generates a provenance-based XML fragment that
 * references metadata content of a parent EML document that is in the PASTA
 * system by using the structure of the <em>methodStep</em> element, which is
 * inserted into a user provided <em>methods</em> element (for multiple
 * references to parent EML documents, multiple <em>methodStep</em> elements
 * are returned, one for each parent EML document). The user provided
 * <em>methods</em> element is not required to be associated with a valid EML
 * document; it acts only as a root element container for the provenance-based
 * <em>methodStep</em> element(s). The service, however, does require that only
 * a single <em>methods</em> element exist in the user provided XML; otherwise,
 * an ambiguity exists when determining a proper xpath location for inserting
 * the provencance-based <em>methodStep</em> XML fragment. The service will
 * accept a valid EML document and correctly insert the provenance-based
 * <em>methodStep</em> XML fragment into the <em>methods</em> element, if it is
 * the only <em>methods</em> element in the EML document. The current service
 * design recognizes that the structure and content of an EML document may vary
 * considerably and may be very complex. To be flexible, the service assumes
 * that a user wanting to insert the provenance-based XML into a complex EML
 * document would do so independent of the Provenance Factory service.
 * </p>
 *
 * <p>
 * The Provenance Factory can be used to append provenance metadata to an EML
 * document that describes a 'derived' or 'synthetic' data set, that is, a data
 * set that was produced from data that exists in the PASTA sytem.
 * </p>
 *
 * <p>
 * Let <em>D<sub>1</sub></em> denote a dataset in PASTA's Data Cache, and let
 * <em>E<sub>1</sub></em> denote an EML document in PASTA's Metadata Catalog
 * that describes <em>D<sub>1</sub></em>. If a new dataset
 * <em>D<sub>2</sub></em> is derived from <em>D<sub>1</sub></em> using method
 * <em>M</em>, a new EML document <em>E<sub>2</sub></em> should also be produced
 * to describe <em>D<sub>2</sub></em>. This can be depicted as:<br>
 *
 * <center><em>M</em> : <em>E<sub>1</sub></em> + <em>D<sub>1</sub></em> &rarr;
 * <em>E<sub>2</sub></em> + <em>D<sub>2</sub></em></center>
 * </p>
 *
 *
 * <p>
 * Ideally, <em>E<sub>2</sub></em> should include a description of the
 * provenance of <em>D<sub>2</sub></em>, that is, it was produced from
 * <em>D<sub>1</sub></em> and <em>E<sub>1</sub></em> by applying method
 * <em>M</em>.
 * </p>
 *
 * <p>
 * The Provenance Factory can be used to append <em>some</em> provenance
 * information to an EML document (<em>E<sub>2</sub></em> from above) in the
 * form of <em>methodStep</em> elements (
 * <code>/eml/dataset/methods/methodStep</code>), whose content references the
 * "parent" EML document(s) (<em>E<sub>1</sub></em> from above). To accomplish
 * this, the user supplies the Provenance Factory with an EML document, to which
 * provenance will be appended, and the packageId(s) of the parent EML
 * document(s) to be referenced. The Provenance Factory requests the specified
 * parent EML documents from PASTA's Metadata Catalog on behalf of the
 * requesting user, and a single methodStep element is appended to the
 * <em>provided</em> EML document (<em>E<sub>2</sub></em>) for each specified
 * parent. The user can also specify particular <em>entityNames</em> (
 * <code>/eml/dataset/dataTable/entityName</code>) in the parent EML document(s)
 * to be included in provenance, which is useful if a parent EML document
 * contains multiple data entities, but only one of which was used to derive the
 * new dataset (<em>D<sub>2</sub></em> from above).
 * </p>
 *
 * <p>
 * The syntax of appended methodStep elements is shown below.
 * </p>
 *
 * <p>
 *
 * <pre>
 * &lt;methodStep&gt;
 *    &lt;description&gt;
 *       &lt;para&gt;Specified entityNames from the parent EML document&lt;/para&gt;
 *    &lt;/description&gt;
 *    &lt;dataSource&gt;
 *       &lt;title&gt;   Copied from the parent EML document &lt;/title&gt;
 *       &lt;creator&gt; Copied from the parent EML document &lt;/creator&gt;
 *       &lt;distribution&gt;
 *          &lt;online&gt;
 *             &lt;url&gt; URL of the parent EML document in PASTA's Data Package Manager &lt;/url&gt;
 *          &lt;/online&gt;
 *       &lt;/distribution&gt;
 *       &lt;contact&gt; Copied from the parent EML document &lt;/contact&gt;
 *    &lt;/dataSource&gt;
 * &lt;/methodStep&gt;
 * </pre>
 * </p>
 *
 */
@Path("/")
public class DataPackageManagerResource extends PastaWebService {

  
  /*
   * Class fields
   */
  
  public static final String AUTH_TOKEN = "auth-token";

  private static Logger logger =
    Logger.getLogger(DataPackageManagerResource.class);
  
  /*
   * Instance fields
   */
  
  private String versionHeader = null;
  private String versionNumber = null;

  
  /*
   * Constructors
   */
  
  /**
   * Constructs a DataPackageManagerResource object. Initializes the
   * versionHeader and versionNumber class field values of the 
   * web service.
   */
  public DataPackageManagerResource() {
    versionHeader = ConfigurationListener.getVersionHeader();
    versionNumber = ConfigurationListener.getVersionNumber();
	}
  
  
  /*
   * Class methods
   */
  
  /**
   * Gets an AuthToken object from an HttpHeaders object
   * 
   * @param headers   the HttpHeaders object
   * @return an AuthToken token
   */
  public static AuthToken getAuthToken(HttpHeaders headers) {
    Map<String, Cookie> cookiesMap = headers.getCookies();
    
    if (!cookiesMap.containsKey(AUTH_TOKEN)) {
      throw new UnauthorizedException("Missing authentication token: " + AUTH_TOKEN);
    }
    
    String cookieToken = cookiesMap.get(AUTH_TOKEN).getValue();
    AuthToken authToken = new AttrListAuthTokenV1(cookieToken);
    
    return authToken;
  }
  
  
  /**
   * Get the method name for a depth in call stack. <br />
   * Utility function
   * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
   * @return method name
   */
  public static String getMethodName(final int depth)
  {
    final StackTraceElement[] ste = Thread.currentThread().getStackTrace();

    return ste[ste.length - 1 - depth].getMethodName();
  }

  
  /*
   * Instance methods
   */
  
  /*
   * Wrapper method for using the audit manager client
   */
  private void audit(String serviceMethodName,
                     AuthToken authToken,
                     Response response,
                     String resourceId,
                     String entryText
                    ) {
    String auditHost = getAuditHost();
    String serviceName = getVersionString();

    try {
      int status = response.getStatus();
      Date date = new Date();
      AuditRecord auditRecord = new AuditRecord(date, serviceName, entryText, authToken, status, serviceMethodName, resourceId);
      AuditManagerClient auditManagerClient = new AuditManagerClient(auditHost);
      auditManagerClient.logAudit(auditRecord);
    }
    catch (Exception e) {
      logger.error("Error occurred while auditing Data Package Manager " +
                   "service call for service method " + 
                   serviceMethodName + " : " + e.getMessage());
    }
  }
  
  
  /**
   * Returns the audit host, e.g. "audit.lternet.edu"
   * 
   * @return version, such as {@code DataPackageManager-0.1}.
   */
  public String getAuditHost() {
    String auditHost = null;
    
    Options options = ConfigurationListener.getOptions();
    if (options != null) {
      auditHost = options.getOption("datapackagemanager.auditmanager.host");
    }
    
    return auditHost;
  }

  
  /**
   * Returns the service version, such as {@code
   * DataPackageManager-0.1}.
   * 
   * @return version, such as {@code DataPackageManager-0.1}.
   */
  @Override
  public String getVersionString() {
    String versionString = null;
    
    Options options = ConfigurationListener.getOptions();
    if (options != null) {
      versionString = options.getOption("web.service.version");
    }
    
    return versionString;
  }

  
  /**
   * Returns the API documentation for the Data Manager.
   * 
   * @return the API documentation for the Data Manager.
   */
  @Override
  public File getApiDocument() {
    return getWebServiceDocument("api.document");
  }

  
  /**
   * Returns the tutorial document for the Data Manager.
   * 
   * @return the tutorial document for the Data Manager.
   */
  @Override
  public File getTutorialDocument() {
    return getWebServiceDocument("tutorial.document");
  }

  
  /**
   * Returns the welcome page for the Data Manager.
   * 
   * @return the welcome page for the Data Manager.
   */
  @Override
  public File getWelcomePage() {
      return getWebServiceDocument("welcome.page");
  }
  

  private File getWebServiceDocument(String propertyName) {
    File documentFile = null;
    
    Options options = ConfigurationListener.getOptions();
    if (options != null) {
      String documentPath = options.getOption(propertyName);
      documentFile = new File(documentPath);
      documentFile = FileUtility.assertCanRead(documentFile);
    }
    
    return documentFile;
  }
  

  /**
   * Boolean to determine whether the user contained in the AuthToken
   * is authorized to execute the specified service method.
   * 
   * @param serviceMethodName     the name of the service method
   * @param authToken             the AuthToken containing the user name
   * @return  true if authorized to run the service method, else false
   */
  private boolean isServiceMethodAuthorized(String serviceMethodName, 
                                            Rule.Permission permission, 
                                            AuthToken authToken) {
    boolean isAuthorized = false;
    NodeList nodeList = null;    
    String serviceDocumentStr = ConfigurationListener.getServiceDocument();
    Document document = XmlUtility.xmlStringToDoc(serviceDocumentStr);
    
    try {
      if (document != null) {
        NodeList documentNodeList = document.getChildNodes();
        Node rootNode = documentNodeList.item(0);
        nodeList = rootNode.getChildNodes();
        
        if (nodeList != null) {
          int nodeListLength = nodeList.getLength();
          for (int i = 0; i < nodeListLength; i++) {
            Node childNode = nodeList.item(i);
            String nodeName = childNode.getNodeName();
            String nodeValue = childNode.getNodeValue();
            if (nodeName.contains("service-method")) {
              Element serviceElement = (Element) nodeList.item(i);
              NamedNodeMap serviceAttributesList = serviceElement.getAttributes();
              
              for (int j = 0; j < serviceAttributesList.getLength(); j++) {
                Node attributeNode = serviceAttributesList.item(j);
                nodeName = attributeNode.getNodeName();
                nodeValue = attributeNode.getNodeValue();
                if (nodeName.equals("name")) {
                  String name = nodeValue;
                  if (name.equals(serviceMethodName)) {
                    NodeList accessNodeList = serviceElement
                            .getElementsByTagName("access");
                    Node accessNode = accessNodeList.item(0);
                    String accessXML = XmlUtility.nodeToXmlString(accessNode);
                    AccessMatrix accessMatrix = new AccessMatrix(accessXML);
                    String principalOwner = "pasta";
                    isAuthorized = accessMatrix.isAuthorized(authToken,
                      principalOwner, permission);
                  }
                }
              }
              
            }
          }
        }
      }
      else {
        String message = "No service methods were found in the service.xml file";
        throw new IllegalStateException(message);
      }
    }
    catch (InvalidPermissionException e) {
      throw new IllegalStateException(e);
    }

    return isAuthorized;
  }


  /**
	 * <strong>Create Data Package</strong> operation, specifying the EML document describing the data package to be created in the message body.
	 * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td>the EML document whose data is to be loaded</td>
   *     <td><code>application/xml</code></td>
   *     <td>curl -i -X POST -H "Content-Type: application/xml" -d @eml.xml http://package.lternet.edu/package/eml</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the create or evaluate request was successful</td>
   *     <td>a list of URLs to the data package resources that were created, one URL per line</td>
   *     <td><code>text/plain</code></td>
   *     <td>
   *     https://pasta.lternet.edu/package/data/eml/knb-lter-lno/10108/1/NoneSuchBugCount<br />
   *     https://pasta.lternet.edu/package/metadata/eml/knb-lter-lno/10108/1<br />
   *     https://pasta.lternet.edu/package/report/eml/knb-lter-lno/10108/1<br />
   *     https://pasta.lternet.edu/package/eml/knb-lter-lno/10108/1
   *     </td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the data package contains one or more metadata, metadata/data congruency, or data errors</td>
   *     <td>A quality report XML document describing the quality errors</td>
   *     <td><code>application/xml</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to create the data package</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support POST.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>409 Conflict</td>
   *     <td>If a data package already exists with the same EML packageId,
   *     or if a data package with the same EML packageId had been deleted previously</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
	 * @param emlFile  An EML document file, as specified in the
	 *                 payload of the request.
	 *                
	 * @return a Response, which if successful, contains a resource map describing
	 *         the contents of the data package
	 */
  @POST
  @Path("/eml")
  @Consumes("application/xml")
  @Produces("text/plain")
	public Response createDataPackage(@Context HttpHeaders headers,
                                    File emlFile) {
    String resourceMap = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "createDataPackage";
    Rule.Permission permission = Rule.Permission.write;
    AuthToken authToken = null;
    String entryText = null;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the 'createDataPackage' service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 
      resourceMap = dataPackageManager.createDataPackage(emlFile, userId, authToken);

      if (resourceMap != null) {
        responseBuilder = Response.ok(resourceMap);
        response = responseBuilder.build();       
      } 
      else {
        Exception e = new Exception("Data package create operation failed for unknown reason");
        throw(e);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (ResourceExistsException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
        WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
          e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    String resourceId = resourceIdFromResourceMap(resourceMap);
    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }

  
  /**
   * Decodes the Httpheaders.AUTHORIZATION token
   * (as per MetadataCatalog-0.1, MetadataCatalogResource class).
   *
   * @param token
   * @return String[]
   *
  private String[] decoder(String token) throws UnauthorizedException
  {
    String [] userCreds = null;

    if (token == null || token.isEmpty())
      throw new UnauthorizedException("token is empty");
    if (!token.contains("Basic ") || (token.length() <= "Basic ".length()))
      throw new UnauthorizedException("Improper Token");

    try {
      token = token.substring("Basic ".length());
      userCreds = new String(Base64.decodeBase64(token)).split(":");
    }
    catch (Throwable e) {
      throw new UnauthorizedException(e.getMessage());
    }

    if (userCreds.length != 2)
      throw new UnauthorizedException("Improper Token");
    return userCreds;
  }*/


	/**
	 * 
   * <strong>Delete Data Package</strong> operation, specifying the scope and identifier of the data package to be deleted in the URI.
   * The data package and its associated quality reports are deleted.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -X DELETE http://package.lternet.edu/package/eml/knb-lter-lno/1</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the delete request was successful</td>
   *     <td>None</td>
   *     <td>None</td>
   *     <td>None</td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to delete the data package</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no data package associated with the specified packageId is found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as POST but the resource
   *     can only support GET or DELETE.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope value of the data package
   * @param identifier  The identifier value of the data package
   * @return            a Response object
	 */
	@DELETE
	@Path("/eml/{scope}/{identifier}")
	@Produces("text/plain")
  public Response deleteDataPackage(@Context HttpHeaders headers,
                                    @PathParam("scope") String scope,
                                    @PathParam("identifier") Integer identifier) {
	  boolean deleted = false;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "deleteDataPackage";
    Rule.Permission permission = Rule.Permission.write;
    AuthToken authToken = null;
    String entryText = null;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the 'deleteDataPackage' service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      entryText = "Deleted " + scope + "." + identifier.toString();
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 
      deleted = dataPackageManager.deleteDataPackage(scope, identifier, userId, authToken);

      if (deleted) {
        responseBuilder = Response.ok();
        response = responseBuilder.build();       
      } 
      else {
        throw new Exception("Data package was not deleted due to an internal server error");
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
        WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
          e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    String resourceId = null;
    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }

	
  /**
   * <strong>Evaluate Data Package</strong> operation, specifying the EML document describing the data package to be evaluated in the message body.
   * Creates the data package in a scratch area for quality reporting purposes only. 
   * A quality report XML document is generated and returned in the message body.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td>the EML document whose data is to be evaluated</td>
   *     <td><code>application/xml</code></td>
   *     <td>curl -i -X POST -H "Content-Type: application/xml" -d @eml.xml http://package.lternet.edu/evaluate/eml</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the create or evaluate request was successful</td>
   *     <td>A quality report XML document</td>
   *     <td><code>application/xml</code></td>
   *     <td>
   *   <pre>
         &lt;?xml version="1.0" encoding="UTF-8"?&gt;
         &lt;qualityReport&gt;
         &lt;packageId&gt;knb-lter-lno.1.3&lt;/packageId&gt;
         .
         .
         .
         &lt;/qualityReport&gt;
   *   </pre>
   *     </td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request message body contains an error</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to evaluate the data package</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support POST.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param emlDocument  The URL to an EML document, as specified in the
   *                payload of the request.
   *                
   * @return a Response, which if successful, contains a quality report XML document
   */
  @POST
  @Path("/evaluate/eml")
  @Consumes("application/xml")
  @Produces({"application/xml", "text/html"})
  public Response evaluateDataPackage(@Context HttpHeaders headers,
                                      File emlFile) {
    boolean produceHTML = false;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "evaluateDataPackage";
    Rule.Permission permission = Rule.Permission.write;
    AuthToken authToken = null;
    String resourceId = null;
    String entryText = null;
    
    /*
     * Determine whether to produce an HTML representation
     */
    List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
    
    if (mediaTypes != null) {
      for (MediaType mediaType : mediaTypes) {
        String mediaTypeStr = mediaType.toString();
        if (mediaTypeStr.equals(MediaType.TEXT_HTML)) {
          produceHTML = true;
        }
      }
    }
    
    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the 'evaluateDataPackage' service method?
      boolean serviceMethodAuthorized = isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager();
      String xmlString = dataPackageManager.evaluateDataPackage(emlFile, userId, authToken);

      if (xmlString != null) {
        if (produceHTML) {
          Options options = ConfigurationListener.getOptions();
          String xslPath = null;
          if (options != null) {
            xslPath = options.getOption("datapackagemanager.xslPath");
          }
            
          try {
            String htmlResult = qualityReportXMLtoHTML(xmlString, xslPath);
            responseBuilder = Response.ok(htmlResult);
            if (responseBuilder != null) {       
              response = responseBuilder.build();       
            }
          }
          catch (IllegalStateException e) {
            WebApplicationException webApplicationException = 
              WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                                       e, e.getMessage());
            response = webApplicationException.getResponse();
          }
        }
        else {
          responseBuilder = Response.ok(xmlString);
          if (responseBuilder != null) {       
            response = responseBuilder.build();       
          }
        } 
      }
      else {
        Exception e = new Exception(
            "Data package evaluate operation failed for unknown reason");
        throw (e);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = WebExceptionFactory
          .make(Response.Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }
  
  
  /*
   * Matches the specified 'entityName' value with the entity names
   * found in the EML document string, and returns the corresponding 
   * objectName value for the matching entity, if an objectName was
   * specified for the matching entity.
   * 
   * Returns null if:
   *   (1) The EML document fails to parse, or
   *   (2) No entities match the specified entityName value, or
   *   (3) The matching entity does not specify an objectName in
   *       the EML document.
   */
  private String findObjectName(String xml, String entityName) {
    String objectName = null;
    EMLParser emlParser = new EMLParser();
    
    if (xml != null && entityName != null) {
      try {
        InputStream inputStream = IOUtils.toInputStream(xml, "UTF-8");
        DataPackage dataPackage = emlParser.parseDocument(inputStream);
        
        if (dataPackage != null) {
          objectName = dataPackage.findObjectName(entityName);
        }
      }
      catch (Exception e) {
        logger.error("Error parsing EML metacdata: " + e.getMessage());
      }
    }
    
    return objectName;
  }

  
  /**
   * 
   * <strong>List Data Entities</strong> operation, specifying the scope, identifier, and revision values to match in the URI. 
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -G http://package.lternet.edu/package/data/eml/knb-lter-lno/10108/1</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the list request was successful</td>
   *     <td>A list of data entity identifiers matching the specified scope, identifier, and revisions values</td>
   *     <td><code>text/plain</code></td>
   *     <td>FictionalEntityOne<br />FictionalEntityTwo</td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to access a list of the data entities</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no data package entities associated with the specified packageId are found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package. A string
   *                  that represents a whole number, or, the
   *                  symbolic values "oldest" or "newest".
   * @return a Response, containing a newline separated list of
   *         data entity identifiers
   */
  @GET
  @Path("/data/eml/{scope}/{identifier}/{revision}")
  @Produces("text/plain")
  public Response listDataEntities(
                       @Context HttpHeaders headers,
                       @PathParam("scope") String scope,
                       @PathParam("identifier") Integer identifier,
                       @PathParam("revision") String revision
                    ) {
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "listDataEntities";
    Rule.Permission permission = Rule.Permission.read;
    AuthToken authToken = null;
    String resourceId = null;
    String entryText = null;
    
    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 

      /*
       * Handle symbolic revisions such as "newest" and "oldest".
       */
      if (revision != null) {
        if (revision.equals("newest")) {
          Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
          if (newest != null) { revision = newest.toString(); }
        }
        else if (revision.equals("oldest")) {
          Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
          if (oldest != null) { revision = oldest.toString(); }
        }
      }
      
      Integer revisionInt = new Integer(revision);
      String entityList = dataPackageManager.listDataEntities(scope, identifier, revisionInt, userId);
    
      if (entityList != null) {
        responseBuilder = Response.ok(entityList.trim());
        response = responseBuilder.build();       
      }
      else {
        String message = "An unknown error occurred";
        throw new Exception(message);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
        WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                                 e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }

  
  /**
   * 
   * <strong>List Data Package Revisions</strong> operation, specifying the scope and identifier values to match in the URI. 
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -G http://package.lternet.edu/package/eml/knb-lter-lno/1</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the list request was successful</td>
   *     <td>A newline-separated list of revision values matching the specified scope and identifier values</td>
   *     <td><code>text/plain</code></td>
   *     <td>1<br/>2<br/>3</td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal scope or identifier value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to access a list of the data package revisions</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no data package revisions associated with the specified scope and identifier are found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the metadata document.
   * @param identifier  The identifier of the metadata document.
   * @return a Response, containing a newline-separated list of revision values
   */
  @GET
  @Path("/eml/{scope}/{identifier}")
  @Produces("text/plain")
  public Response listDataPackageRevisions(
                               @Context HttpHeaders headers,
                               @PathParam("scope") String scope,
                               @PathParam("identifier") String identifierStr
                    ) {
    Integer identifier = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "listDataPackageRevisions";
    Rule.Permission permission = Rule.Permission.read;
    AuthToken authToken = null;
    String resourceId = null;
    String entryText = null;

    try {
      
      // Check for a non-integer identifier
      try {
        identifier = new Integer(identifierStr);
      }
      catch (NumberFormatException e) {
        String message = "identifier value '"  + identifierStr + "' must be a non-negative integer\n";
        throw new UserErrorException(message);
      }
        
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 

      String revisionList = dataPackageManager.listDataPackageRevisions(scope, identifier);

      if (revisionList != null) {
        responseBuilder = Response.ok(revisionList.trim());
        response = responseBuilder.build();       
      }
      else {
        String message = "An unknown error occurred";
        throw new Exception(message);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
      WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                               e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }


  /**
   * 
   * <strong>List Data Package Identifiers</strong> operation, specifying the scope value to match in the URI. 
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -G http://package.lternet.edu/package/eml/knb-lter-lno</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the list request was successful</td>
   *     <td>A newline-separated list of data package identifier values matching the specified scope value</td>
   *     <td><code>text/plain</code></td>
   *     <td>1004<br/>1005<br/>1007</td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal scope value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to access a list of the identifier values</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no data packages identifiers associated with the specified scope are found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package.
   * @return a Response, containing a newline separated list of identifier values
   *         matching the specified scope values
   */
  @GET
  @Path("/eml/{scope}")
  @Produces("text/plain")
  public Response listDataPackageIdentifiers(
                                     @Context HttpHeaders headers,
                                     @PathParam("scope") String scope) {
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "listDataPackageIdentifiers";
    Rule.Permission permission = Rule.Permission.read;
    AuthToken authToken = null;
    String resourceId = null;
    String entryText = null;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 

      String identifierList = 
        dataPackageManager.listDataPackageIdentifiers(scope, userId);

      if (identifierList != null) {
        responseBuilder = Response.ok(identifierList.trim());
        response = responseBuilder.build();       
      }
      else {
        String message = "An unknown error occurred";
        throw new Exception(message);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
      WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                               e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }


  /**
   * 
   * <strong>List Data Package Scopes</strong> operation, returning all scope values extant in the data package registry.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -G http://package.lternet.edu/package/eml</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the list request was successful</td>
   *     <td>A list of all scope values extant in the data package registry</td>
   *     <td><code>text/plain</code></td>
   *     <td>knb-lter-lno<br/>knb-lter-xyz</td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to access a list of the scope values</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no scope values are extant in the data package registry</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @return a Response, containing a newline separated list of scope values
   */
  @GET
  @Path("/eml")
  @Produces("text/plain")
  public Response listDataPackageScopes(@Context HttpHeaders headers) {
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "listDataPackageScopes";
    Rule.Permission permission = Rule.Permission.read;
    AuthToken authToken = null;
    String resourceId = null;
    String entryText = null;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 

      String scopeList = dataPackageManager.listDataPackageScopes();

      if (scopeList != null) {
        responseBuilder = Response.ok(scopeList.trim());
        response = responseBuilder.build();       
      }
      else {
        String message = "An unknown error occurred";
        throw new Exception(message);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
      WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                               e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }

  
  /**
   * 
   * <strong>List Deleted Data Packages</strong> operation, returning all document identifiers (excluding revision values) that have been deleted from the data package registry.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -G http://package.lternet.edu/package/eml/deleted</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the list request was successful</td>
   *     <td>A list of all document identifiers deleted from the data package registry</td>
   *     <td><code>text/plain</code></td>
   *     <td>knb-lter-lno.1<br/>knb-lter-lno.2<br/>knb-lter-xyz.1</td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to access a list of deleted data packages</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no deleted data packages are found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @return a Response, containing a newline separated list of document identifiers.
   */
  @GET
  @Path("/eml/deleted")
  @Produces("text/plain")
  public Response listDeletedDataPackages(@Context HttpHeaders headers) {
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "listDeletedDataPackages";
    Rule.Permission permission = Rule.Permission.read;
    AuthToken authToken = null;
    String resourceId = null;
    String entryText = null;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 

      String packageList = dataPackageManager.listDeletedDataPackages();

      if (packageList != null) {
        responseBuilder = Response.ok(packageList.trim());
        response = responseBuilder.build();       
      }
      else {
        String message = "An unknown error occurred";
        throw new Exception(message);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
      WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                               e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }

  
  /**
   * Applies the quality report stylesheet to transform the quality report
   * representation from XML to HTML for presentation purposes.
   * 
   * @param xmlString    the quality report XML string
   * @param xslPath      path to the quality report stylesheet
   * 
   * @return htmlString  the result of the transformation from XML to HTML
   * @throws IllegalStateException
   *                     if an error occurs during the transformation process
   */
  private String qualityReportXMLtoHTML(String xmlString,
                                        final String xslPath) 
          throws IllegalStateException {
    String htmlString = "";
    Result result;
    StringWriter stringWriter = new StringWriter();
    javax.xml.transform.Transformer transformer;
    javax.xml.transform.TransformerFactory transformerFactory;
    Source xmlSource;
    File xsltFile = new File(xslPath);            
    Source xsltSource;
    
    StringReader stringReader = new StringReader(xmlString);
    xmlSource = new javax.xml.transform.stream.StreamSource(stringReader);
    xsltSource = new javax.xml.transform.stream.StreamSource(xsltFile);
    result = new javax.xml.transform.stream.StreamResult(stringWriter);
    logger.debug("javax.xml.transform.TransformerFactory :" + 
                 System.getProperty("javax.xml.transform.TransformerFactory"));
    transformerFactory = javax.xml.transform.TransformerFactory.newInstance();

    try {
      transformer = transformerFactory.newTransformer(xsltSource);      
      transformer.transform(xmlSource, result);
      htmlString = stringWriter.toString();
    }
    catch (TransformerConfigurationException e) {
      Throwable x = e;
      if (e.getException() != null) {
        x = e.getException();    
      }
      x.printStackTrace();
      throw new IllegalStateException(e);
    }
    catch (TransformerException e) {
      Throwable x = e;
      if (e.getException() != null) {
        x = e.getException();    
      }
      x.printStackTrace(); 
      throw new IllegalStateException(e);
    }
      
    return htmlString;
  }
  
  
  /**
   * 
   * <strong>Read Data Package</strong> operation, specifying the scope, identifier, and revision of the data package to be read in the URI, returning a resource graph with reference URLs to each of the metadata, data, and quality report resources that comprise the data package.
   * 
   * <p>Revision may be specified as "newest" or "oldest" to retrieve the newest or oldest 
   * revision, respectively.</p>
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -G http://package.lternet.edu/package/eml/knb-lter-lno/1/3</td>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -G http://package.lternet.edu/package/eml/knb-lter-lno/1/oldest</td>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -G http://package.lternet.edu/package/eml/knb-lter-lno/1/newest</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the data package was successful</td>
   *     <td>A resource graph with reference URLs to each of the metadata, data, and quality report resources that comprise the data package.</td>
   *     <td><code>'text/plain'</code></td>
   *     <td>
   *     https://pasta.lternet.edu/package/data/eml/knb-lter-lno/1/3/NoneSuchBugCount<br />
   *     https://pasta.lternet.edu/package/metadata/eml/knb-lter-lno/1/3<br />
   *     https://pasta.lternet.edu/package/report/eml/knb-lter-lno/1/3<br />
   *     https://pasta.lternet.edu/package/eml/knb-lter-lno/1/3
   *     </td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the data package</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no data package associated with the specified packageId is found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package
   * @return a Response object containing a data package resource graph
   *         if found, else returns a 404 Not Found response
   */
  @GET
  @Path("/eml/{scope}/{identifier}/{revision}")
  @Produces("text/plain")
  public Response readDataPackage(
                                  @Context HttpHeaders headers,
                                  @PathParam("scope") String scope,
                                  @PathParam("identifier") Integer identifier,
                                  @PathParam("revision") String revision
                    ) {
    AuthToken authToken = null;
    String resourceMap = null;
    String entryText = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "readDataPackage";
    Rule.Permission permission = Rule.Permission.read;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 
      resourceMap = 
        dataPackageManager.readDataPackage(scope, identifier, revision, authToken, userId);

      if (resourceMap != null) {
        responseBuilder = Response.ok(resourceMap);
        response = responseBuilder.build();
      }
      else {
        Exception e = new Exception(
            "Data package create operation failed for unknown reason");
        throw (e);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (ResourceExistsException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = WebExceptionFactory
          .make(Response.Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    String resourceId = resourceIdFromResourceMap(resourceMap);
    audit(serviceMethodName, authToken, response, resourceId, entryText);

    response = stampHeader(response);
    return response;
  }
  
  
  /**
   * 
   * DEPRECATED as of 11/21/2011
   *
   * <strong>Read Data Package Evaluate-Report</strong> operation, specifying the scope, identifier, and revision of a previously evaluated data package in the URI, returning a resource graph with reference URLs to each of the metadata and data entity reports associated with the evaluated data package.
   * 
   * <p>If an HTTP Accept header with value 'text/html' is included in the request, 
   * returns an HTML representation of the report. The default representation is XML.</p>
   * 
   * <p>Evaluate-report retrieves a quality report for a data package that was previously evaluated,
   * that is, for quality reporting purposes only. (See also <em>Evaluate</em> operation above)</p>
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td><em>XML representation: </em>curl -i -G http://package.lternet.edu/package/evaluate/report/eml/knb-lter-gce-nis/1/9/INS-GCEM-0011_1_3.TXT</td>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td><em>HTML representation: </em>curl -i -H "Accept: text/html" -G http://package.lternet.edu/package/evaluate/report/eml/knb-lter-gce-nis/1/9/INS-GCEM-0011_1_3.TXT</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the quality report was successful</td>
   *     <td>The quality report that describes the data entity</td>
   *     <td><code>application/xml</code> or <code>text/html</code> (See above)</td>
   *     <td>
   *   <pre>
         &lt;?xml version="1.0" encoding="UTF-8"?&gt;
         &lt;qualityReport type="data"&gt;
         &lt;packageId&gt;knb-lter-gce-nis.1.9&lt;/packageId&gt;
         &lt;entityId&gt;INS-GCEM-0011_1_3.TXT&lt;/entityId&gt;
         &lt;entityName&gt;INS-GCEM-0011_1_3.TXT&lt;/entityName&gt;
         &lt;dateCreated&gt;2011-02-03&lt;/dateCreated&gt;
         &lt;/qualityReport&gt;
   *   </pre>
   *     </td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the quality report for the specified data entity</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no quality reports associated with the specified packageId are found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the metadata document.
   * @param identifier  The identifier of the metadata document.
   * @param revision    The revision of the metadata document.
   * @return            A Response object containing the entity report 
   *                    XML or HTML if found, else return a 404 Not Found
   *                    response
   *
  @GET
  @Path("/evaluate/report/eml/{scope}/{identifier}/{revision}")
  @Produces({"application/xml", "text/html"})
  public Response readDataPackageEvaluateReport(
                                    @Context HttpHeaders headers,
                                    @PathParam("scope") String scope,
                                    @PathParam("identifier") Integer identifier,
                                    @PathParam("revision") String revision
                     ) {
    boolean evaluateMode = true;  
    boolean produceHTML = false;
    
    /*
     * Determine whether to produce an HTML representation
     *
    List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
    for (MediaType mediaType : mediaTypes) {
      String mediaTypeStr = mediaType.toString();
      if (mediaTypeStr.equals(MediaType.TEXT_HTML)) {
        produceHTML = true;
      }
    }
    
    ResponseBuilder responseBuilder = null;
    Response response = null;
    
    try {
      AuthToken authToken = getAuthToken(headers);
      String userId = authToken.getUserId();
      EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
      DataPackageManager dataPackageManager = new DataPackageManager(); 
      EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope, identifier.toString(), revision);
      String packageId = emlPackageIdFormat.format(emlPackageId);
      File xmlFile = 
        dataPackageManager.readDataPackageReport(scope, identifier, revision, 
                                                 emlPackageId, authToken, userId, evaluateMode);
    
      if (xmlFile != null && xmlFile.exists()) {
        if (produceHTML) {
          Options options = ConfigurationListener.getOptions();
          String xslPath = null;
          if (options != null) {
            xslPath = options.getOption("datapackagemanager.xslPath");
          }
          
          try {
            String htmlResult = qualityReportXMLtoHTML(xmlFile, xslPath);
            responseBuilder = Response.ok(htmlResult);
            if (responseBuilder != null) {       
              response = responseBuilder.build();       
            }
          }
          catch (IllegalStateException e) {
            WebApplicationException webApplicationException = 
              WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                                       e, e.getMessage());
            response = webApplicationException.getResponse();
          }
        }
        else {
          responseBuilder = Response.ok(xmlFile);
          if (responseBuilder != null) {       
            response = responseBuilder.build();       
          }
        } 
      }
      else {
        ResourceNotFoundException e = new ResourceNotFoundException(
            "Unable to access data package evaluate report for packageId: " + packageId);
        WebApplicationException webApplicationException =
          WebExceptionFactory.makeNotFound(e);
        response = webApplicationException.getResponse();
      }
    }
    catch (IllegalArgumentException e) {
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (Exception e) {
      WebApplicationException webApplicationException = 
        WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                                 e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    response = stampHeader(response);
    return response;
  }*/

  
  /**
   * 
   * <strong>Read Metadata</strong> operation, specifying the scope, identifier, and revision of the metadata document to be read in the URI.
   * 
   * <p>Revision may be specified as "newest" or "oldest" to retrieve the 
   *    newest or oldest revision, respectively.</p>
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -G http://package.lternet.edu/package/metadata/eml/knb-lter-lno/1/3</td>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -G http://package.lternet.edu/package/metadata/eml/knb-lter-lno/1/newest</td>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -G http://package.lternet.edu/package/metadata/eml/knb-lter-lno/1/oldest</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the metadata document was successful</td>
   *     <td>The metadata document</td>
   *     <td><code>'application/xml'</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the data package metadata document</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no data package associated with the specified packageId is found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package
   * @return a Response object containing the XML metadata document in its message body
   */
  @GET
  @Path("/metadata/eml/{scope}/{identifier}/{revision}")
  @Produces("application/xml")
  public Response readMetadata(
                    @Context HttpHeaders headers,
                    @PathParam("scope") String scope,
                    @PathParam("identifier") Integer identifier,
                    @PathParam("revision") String revision
                    ) {
    AuthToken authToken = null;
    String metadataString = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    final String serviceMethodName = "readMetadata";
    Rule.Permission permission = Rule.Permission.read;
    String resourceId = null;
    String entryText = null;
    
    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 

      /*
       * Handle symbolic revisions such as "newest" and "oldest".
       */
      if (revision != null) {
        if (revision.equals("newest")) {
          Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
          if (newest != null) { revision = newest.toString(); }
        }
        else if (revision.equals("oldest")) {
          Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
          if (oldest != null) { revision = oldest.toString(); }
        }
      }

      EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope, identifier.toString(), revision);
      String packageId = emlPackageIdFormat.format(emlPackageId);

      /*
       * Isolate the resourceId for the metadata resource so that its value can be
       * recorded in the audit log
       */
      Integer revisionInt = new Integer(revision);
      ArrayList<String> resources = dataPackageManager.getDataPackageResources(scope, identifier, revisionInt);
      if (resources != null && resources.size() > 0) {
        for (String resource : resources) {
          if (resource != null && resource.contains("/package/metadata/eml")) {
            resourceId = resource;
          }
        }
      }

      metadataString = dataPackageManager.readMetadata(scope, identifier, revision, userId, authToken);
      
      if (metadataString != null) {
        responseBuilder = Response.ok(metadataString);
        response = responseBuilder.build();       
      } 
      else {
        ResourceNotFoundException e = new ResourceNotFoundException(
           "Unable to access metadata for packageId: " + packageId.toString());
        throw(e);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeNotFound(e);
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
          WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                                   e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }


  /**
   * 
   * <strong>Read Data Entity</strong> operation, specifying the scope, identifier, revision, and entity identifier of the data entity to be read in the URI.
   * 
   * <p>Revision may be specified as "newest" or "oldest" to retrieve data from the newest or oldest 
   * revision, respectively.</p>
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -G http://package.lternet.edu/package/data/eml/knb-lter-lno/1/3/FictionalEntity</td>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -G http://package.lternet.edu/package/data/eml/knb-lter-lno/1/oldest/FictionalEntity</td>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td>curl -i -G http://package.lternet.edu/package/data/eml/knb-lter-lno/1/newest/FictionalEntity</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the data entity was successful</td>
   *     <td>The data that comprises the data entity</td>
   *     <td><code>application/octet-stream</code></td>
   *     <td>
   *       Site Year Month Day Transect Species_Code Count<br/>
   *       1 2000 8 26 1 G1 0<br/>
   *       1 2000 8 26 2 G1 0<br/>
   *       1 2000 8 26 3 G1 0<br/>
   *       .<br/>
   *       .<br/>
   *     </td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the data entity</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If either the specified data package or the specified data entity is not found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package
   * @param entityId    The identifier of the data entity within the data package
   * @return a Response object containing the specified data entity,
   *         if found, else returns a 404 Not Found response
   */
  @GET
  @Path("/data/eml/{scope}/{identifier}/{revision}/{entityId}")
  public Response readDataEntity(
                                 @Context HttpHeaders headers,
                                 @PathParam("scope") String scope,
                                 @PathParam("identifier") Integer identifier,
                                 @PathParam("revision") String revision,
                                 @PathParam("entityId") String entityId
                    ) {
    ResponseBuilder responseBuilder = null;
    Response response = null;
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    final String serviceMethodName = "readDataEntity";
    Rule.Permission permission = Rule.Permission.read;
    AuthToken authToken = null;
    String resourceId = null;
    String entryText = null;
    
    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 

      /*
       * Handle symbolic revisions such as "newest" and "oldest".
       */
      if (revision != null) {
        if (revision.equals("newest")) {
          Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
          if (newest != null) { revision = newest.toString(); }
        }
        else if (revision.equals("oldest")) {
          Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
          if (oldest != null) { revision = oldest.toString(); }
        }
      }
      
      EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope, identifier.toString(), revision);
      String packageId = emlPackageIdFormat.format(emlPackageId);

      /*
       * Isolate the resourceId for the data entity so that its value can be
       * recorded in the audit log
       */
      Integer revisionInt = new Integer(revision);
      ArrayList<String> resources = dataPackageManager.getDataPackageResources(scope, identifier, revisionInt);
      if (resources != null && resources.size() > 0) {
        for (String resource : resources) {
          if (resource != null && 
              resource.contains("/package/data/eml") &&
              resource.contains(entityId)
             ) {
            resourceId = resource;
          }
        }
      }

      MediaType dataFormat = dataPackageManager.getDataEntityFormat(scope, identifier, revision, entityId);
      entryText = "Data Format: " + dataFormat.toString();
      
      byte[] byteArray = 
        dataPackageManager.readDataEntity(scope, identifier, revision, entityId, authToken, userId);
    
      if (byteArray != null) {
        String dataPackageResourceId = DataPackageManager.composeResourceId(
          ResourceType.dataPackage, scope, identifier, Integer.valueOf(revision), null);

        String entityResourceId = DataPackageManager.composeResourceId(
          ResourceType.data, scope, identifier, Integer.valueOf(revision), entityId);

        String entityName = dataPackageManager.readDataEntityName(
          dataPackageResourceId, entityResourceId, authToken);
        
        String xmlMetadata = dataPackageManager.readMetadata(
          scope, identifier, revision, authToken.getUserId(), authToken);
        
        String objectName = findObjectName(xmlMetadata, entityName);
        
        entryText = String.format("%s: %s; %s: %s; %s", 
                                  "Entity Name", entityName, "Object Name", objectName, entryText);

        responseBuilder = Response.ok(byteArray, dataFormat);
        
        if (objectName != null) {
          responseBuilder.header("Content-Disposition", "attachment; filename=" + objectName);
        }

        response = responseBuilder.build();
      }
      else {
        ResourceNotFoundException e = new ResourceNotFoundException(
          "Unable to access data entity file for packageId: " + packageId.toString() + 
                                        "; entityId: " + entityId);
        throw(e);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
        WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                                 e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
    
  }
  
   
  /**
   * 
   * <strong>Read Data Entity Name</strong> operation, specifying the scope, identifier, revision, and entity identifier of the data entity whose name is to be read in the URI.
   * 
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td align=center>none</td>
   *     <td align=center>none</td>
   *     <td>curl -i -G http://package.lternet.edu/package/name/knb-lter-lno/1/3/AB39DC03999GDN123</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the data package entity name was successful</td>
   *     <td>The entity name value of the data package entity.</td>
   *     <td><code>text/plain</code></td>
   *     <td>Daily Average Moored CTD and ADCP Data</td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the data package entity name</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no entity associated with the specified data package entity identifier is found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package
   * @param entityId    The identifier of the data entity within the data package
   * @return a Response object containing a data entity name
   *         if found, else returns a 404 Not Found response
   */
  @GET
  @Path("/name/{scope}/{identifier}/{revision}/{entityId}")
  @Produces("text/plain")
  public Response readDataEntityName(
                                  @Context HttpHeaders headers,
                                  @PathParam("scope") String scope,
                                  @PathParam("identifier") Integer identifier,
                                  @PathParam("revision") String revision,
                                  @PathParam("entityId") String entityId
                    ) {
    AuthToken authToken = null;
    String dataPackageResourceId = null;
    String entityName = null;
    String entryText = null;
    String entityResourceId = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "readDataEntityName";
    Rule.Permission permission = Rule.Permission.read;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      dataPackageResourceId = DataPackageManager.composeResourceId(
          ResourceType.dataPackage, scope, identifier,
          Integer.valueOf(revision), null);

      entityResourceId = DataPackageManager.composeResourceId(
          ResourceType.data, scope, identifier,
          Integer.valueOf(revision), entityId);

      DataPackageManager dataPackageManager = new DataPackageManager(); 
      entityName = dataPackageManager.readDataEntityName(dataPackageResourceId,
                                                         entityResourceId, authToken);
      
      if (entityName != null) {
        responseBuilder = Response.ok(entityName);
        response = responseBuilder.build();
        entryText = entityName;
      }
      else {
        Exception e = new Exception(
            "Read Data Entity Name operation failed for unknown reason");
        throw (e);
      }
      
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (ResourceExistsException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = WebExceptionFactory
          .make(Response.Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    audit(serviceMethodName, authToken, response, entityResourceId, entryText);

    response = stampHeader(response);
    return response;
  }
  
  
  /**
   * <strong>Read Data Package Report</strong> operation, specifying the scope, identifier, and revision of the data package quality report document to be read in the URI.
   * 
   * <p>If an HTTP Accept header with value 'text/html' is included in the request, 
   * returns an HTML representation of the report. The default representation is XML.</p>
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td><em>XML representation: </em>curl -i -G http://package.lternet.edu/package/report/eml/knb-lter-lno/1/3</td>
   *   </tr>
   *   <tr>
   *     <td></td>
   *     <td></td>
   *     <td><em>HTML representation: </em>curl -i -H "Accept: text/html" -G http://package.lternet.edu/package/report/eml/knb-lter-lno/1/3</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the quality report was successful</td>
   *     <td>The quality report document that describes the data package</td>
   *     <td><code>application/xml</code> or <code>text/html</code> (See above)</td>
   *     <td>
   *   <pre>
         &lt;?xml version="1.0" encoding="UTF-8"?&gt;
         &lt;qualityReport&gt;
         &lt;packageId&gt;knb-lter-lno.1.3&lt;/packageId&gt;
         .
         .
         .
         &lt;/qualityReport&gt;
   *   </pre>
   *     </td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the specified data package</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no data package matching the specified scope, identifier, and revision values is found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package
   * @return            A Response object containing the data package 
   *                    quality report
   */
  @GET
  @Path("/report/eml/{scope}/{identifier}/{revision}")
  @Produces({"application/xml", "text/html"})
  public Response readDataPackageReport(
                                    @Context HttpHeaders headers,
                                    @PathParam("scope") String scope,
                                    @PathParam("identifier") Integer identifier,
                                    @PathParam("revision") String revision
                     ) {
    AuthToken authToken = null;
    boolean evaluateMode = false;  
    boolean produceHTML = false;
    final String serviceMethodName = "readDataPackageReport";
    Rule.Permission permission = Rule.Permission.read;
    String resourceId = null;
    String entryText = null;
    
    /*
     * Determine whether to produce an HTML representation
     */
    List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
    for (MediaType mediaType : mediaTypes) {
      String mediaTypeStr = mediaType.toString();
      if (mediaTypeStr.equals(MediaType.TEXT_HTML)) {
        produceHTML = true;
      }
    }
    
    ResponseBuilder responseBuilder = null;
    Response response = null;
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    authToken = getAuthToken(headers);
    String userId = authToken.getUserId();
    
    // Is user authorized to run the service method?
    boolean serviceMethodAuthorized = 
      isServiceMethodAuthorized(serviceMethodName, permission, authToken);
    if (!serviceMethodAuthorized) {
      throw new UnauthorizedException(
          "User " + userId + 
          " is not authorized to execute service method " + 
          serviceMethodName);
    }
    
    try {
      DataPackageManager dataPackageManager = new DataPackageManager(); 
      EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope, identifier.toString(), revision);
      String packageId = emlPackageIdFormat.format(emlPackageId);

      /*
       * Isolate the resourceId for the report so that its value can be
       * recorded in the audit log
       */
      Integer revisionInt = new Integer(revision);
      ArrayList<String> resources = dataPackageManager.getDataPackageResources(scope, identifier, revisionInt);
      if (resources != null && resources.size() > 0) {
        for (String resource : resources) {
          if (resource != null && resource.contains("/package/report/eml")) {
            resourceId = resource;
          }
        }
      }

      File xmlFile = 
        dataPackageManager.readDataPackageReport(scope, identifier, revision, 
                                                 emlPackageId, authToken, userId, 
                                                 evaluateMode);
    
      if (xmlFile != null && xmlFile.exists()) {
        if (produceHTML) {
          Options options = ConfigurationListener.getOptions();
          String xslPath = null;
          if (options != null) {
            xslPath = options.getOption("datapackagemanager.xslPath");
          }
          
          try {
            String xmlString = FileUtility.fileToString(xmlFile);
            String htmlResult = qualityReportXMLtoHTML(xmlString, xslPath);
            responseBuilder = Response.ok(htmlResult);
            if (responseBuilder != null) {       
              response = responseBuilder.build();       
            }
          }
          catch (IllegalStateException e) {
            entryText = e.getMessage();
            WebApplicationException webApplicationException = 
              WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                                       e, e.getMessage());
            response = webApplicationException.getResponse();
          }
        }
        else {
          responseBuilder = Response.ok(xmlFile);
          if (responseBuilder != null) {       
            response = responseBuilder.build();       
          }
        } 
      }
      else {
        ResourceNotFoundException e = new ResourceNotFoundException(
            "Unable to access data package quality report file for packageId: " + packageId);
        WebApplicationException webApplicationException =
          WebExceptionFactory.makeNotFound(e);
        entryText = e.getMessage();
        response = webApplicationException.getResponse();
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
        WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                                 e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }

  /**
   * 
   * <strong>Read Data Package DOI</strong> operation, specifying the scope, identifier, and revision of the data package DOI to be read in the URI, returning the canonical Digital Object Identifier.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td align=center>none</td>
   *     <td align=center>none</td>
   *     <td>curl -i -G http://package.lternet.edu/package/doi/knb-lter-lno/1/3</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the data package DOI was successful</td>
   *     <td>The canonical Digital Object Identifier of the data package.</td>
   *     <td><code>text/plain</code></td>
   *     <td>doi:10.6073/pasta/7a39bd7694dc0473a6ae7a7d7520ff2e</td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the data package</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no DOI associated with the specified data package is found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package
   * @return a Response object containing a data package DOI
   *         if found, else returns a 404 Not Found response
   */
  @GET
  @Path("/doi/{scope}/{identifier}/{revision}")
  @Produces("text/plain")
  public Response readDataPackageDoi(
                                  @Context HttpHeaders headers,
                                  @PathParam("scope") String scope,
                                  @PathParam("identifier") Integer identifier,
                                  @PathParam("revision") String revision
                    ) {
    AuthToken authToken = null;
    String doi = null;
    String entryText = null;
    String resourceId = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "readDataPackageDoi";
    Rule.Permission permission = Rule.Permission.read;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
			resourceId = DataPackageManager.composeResourceId(
			    ResourceType.dataPackage, scope, identifier,
			    Integer.valueOf(revision), null);

      DataPackageManager dataPackageManager = new DataPackageManager(); 
      doi = dataPackageManager.readResourceDoi(resourceId, authToken);
      
      if (doi != null) {
        responseBuilder = Response.ok(doi);
        response = responseBuilder.build();
        entryText = doi;
      }
      else {
        Exception e = new Exception(
            "Read resource DOI operation failed for unknown reason");
        throw (e);
      }
      
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (ResourceExistsException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = WebExceptionFactory
          .make(Response.Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    audit(serviceMethodName, authToken, response, resourceId, entryText);

    response = stampHeader(response);
    return response;
  }
  
  
  /*
   * 
   * <strong>Read Metadata DOI</strong> operation, specifying the scope, identifier, and revision of the metadata DOI to be read in the URI, returning the canonical Digital Object Identifier.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td align=center>none</td>
   *     <td align=center>none</td>
   *     <td>curl -i -G http://package.lternet.edu/package/metadata/doi/knb-lter-lno/1/3</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the metadata DOI was successful</td>
   *     <td>The canonical Digital Object Identifier of the metadata.</td>
   *     <td><code>text/plain</code></td>
   *     <td>doi:10.6073/pasta/7a39bd7694dc0473a6ae7a7d7520ff2e</td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the metadata</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no DOI associated with the specified metadata is found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package
   * @return a Response object containing a metadata DOI
   *         if found, else returns a 404 Not Found response
   */
  
  /*
  @GET
  @Path("/metadata/doi/{scope}/{identifier}/{revision}")
  @Produces("text/plain")
  public Response readMetadataDoi(
                                  @Context HttpHeaders headers,
                                  @PathParam("scope") String scope,
                                  @PathParam("identifier") Integer identifier,
                                  @PathParam("revision") String revision
                    ) {
    AuthToken authToken = null;
    String doi = null;
    String entryText = null;
    String resourceId = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "readMetadataDoi";
    Rule.Permission permission = Rule.Permission.read;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
			resourceId = DataPackageManager.composeResourceId(
			    ResourceType.metadata, scope, identifier,
			    Integer.valueOf(revision), null);

      DataPackageManager dataPackageManager = new DataPackageManager(); 
      doi = dataPackageManager.readResourceDoi(resourceId, authToken);

			if (doi != null) {
        responseBuilder = Response.ok(doi);
        response = responseBuilder.build();
        entryText = doi;
      }
      else {
        Exception e = new Exception(
            "Read resource DOI operation failed for unknown reason");
        throw (e);
      }
      
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
    	response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (ResourceExistsException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = WebExceptionFactory
          .make(Response.Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    audit(serviceMethodName, authToken, response, resourceId, entryText);

    response = stampHeader(response);
    return response;
  }

	*/

  /*
   * 
   * <strong>Read Data Entity DOI</strong> operation, specifying the scope, identifier, and revision of the data entity DOI to be read in the URI, returning the canonical Digital Object Identifier.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td align=center>none</td>
   *     <td align=center>none</td>
   *     <td>curl -i -G http://package.lternet.edu/package/data/doi/knb-lter-lno/1/3/enity_name</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the data entity DOI was successful</td>
   *     <td>The canonical Digital Object Identifier of the data entity.</td>
   *     <td><code>text/plain</code></td>
   *     <td>doi:10.6073/pasta/7a39bd7694dc0473a6ae7a7d7520ff2e</td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the data entity</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no DOI associated with the specified data entity is found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package
   * @param entityId    The entity identifier
   * @return a Response object containing a data entity DOI
   *         if found, else returns a 404 Not Found response
   */
  
  /*
  @GET
  @Path("/data/doi/{scope}/{identifier}/{revision}/{entityId}")
  @Produces("text/plain")
  public Response readDataEntityDoi(
                                 @Context HttpHeaders headers,
                                 @PathParam("scope") String scope,
                                 @PathParam("identifier") Integer identifier,
                                 @PathParam("revision") String revision,
                                 @PathParam("entityId") String entityId
                    ) {
    AuthToken authToken = null;
    String doi = null;
    String entryText = null;
    String resourceId = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "readDataEntityDoi";
    Rule.Permission permission = Rule.Permission.read;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
			resourceId = DataPackageManager.composeResourceId(
			    ResourceType.data, scope, identifier,
			    Integer.valueOf(revision), entityId);

      DataPackageManager dataPackageManager = new DataPackageManager(); 
      doi = dataPackageManager.readResourceDoi(resourceId, authToken);

      if (doi != null) {
        responseBuilder = Response.ok(doi);
        response = responseBuilder.build();
        entryText = doi;
     }
      else {
        Exception e = new Exception(
            "Read resource DOI operation failed for unknown reason");
        throw (e);
      }
      
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (ResourceExistsException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = WebExceptionFactory
          .make(Response.Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    audit(serviceMethodName, authToken, response, resourceId, entryText);

    response = stampHeader(response);
    return response;
  }
	
	*/
  
  /*
   * 
   * <strong>Read Report DOI</strong> operation, specifying the scope, identifier, and revision of the report DOI to be read in the URI, returning the canonical Digital Object Identifier.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td align=center>none</td>
   *     <td align=center>none</td>
   *     <td>curl -i -G http://package.lternet.edu/package/report/doi/knb-lter-lno/1/3</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the request to read the report DOI was successful</td>
   *     <td>The canonical Digital Object Identifier of the report.</td>
   *     <td><code>text/plain</code></td>
   *     <td>doi:10.6073/pasta/7a39bd7694dc0473a6ae7a7d7520ff2e</td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request contains an error, such as an illegal identifier or revision value</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to read the report</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If no DOI associated with the specified report is found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support GET.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param scope       The scope of the data package
   * @param identifier  The identifier of the data package
   * @param revision    The revision of the data package
   * @return a Response object containing a report DOI
   *         if found, else returns a 404 Not Found response
   */
  
  /*
  @GET
  @Path("/report/doi/{scope}/{identifier}/{revision}")
  @Produces("text/plain")
  public Response readDataPackageReportDoi(
                                  @Context HttpHeaders headers,
                                  @PathParam("scope") String scope,
                                  @PathParam("identifier") Integer identifier,
                                  @PathParam("revision") String revision
                    ) {
    AuthToken authToken = null;
    String doi = null;
    String entryText = null;
    String resourceId = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "readDataPackageReportDoi";
    Rule.Permission permission = Rule.Permission.read;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
			resourceId = DataPackageManager.composeResourceId(
			    ResourceType.report, scope, identifier,
			    Integer.valueOf(revision), null);
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 
      doi = dataPackageManager.readResourceDoi(resourceId, authToken);

      if (doi != null) {
        responseBuilder = Response.ok(doi);
        response = responseBuilder.build();
        entryText = doi;
      }
      else {
        Exception e = new Exception(
            "Read resource DOI operation failed for unknown reason");
        throw (e);
      }
      
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (ResourceExistsException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = WebExceptionFactory
          .make(Response.Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
      response = webApplicationException.getResponse();
    }
    
    audit(serviceMethodName, authToken, response, resourceId, entryText);

    response = stampHeader(response);
    return response;
  }

  */
  
  /*
   * Isolates the resourceId for the data package from a resource map 
   * string and returns it.
   */
  private String resourceIdFromResourceMap(String resourceMap) {
    String resourceId = null;
    
    if (resourceMap != null && !resourceMap.isEmpty()) {
      String[] mapEntries = resourceMap.split("\n");
      if (mapEntries != null && mapEntries.length > 0) {
        for (String mapEntry : mapEntries) {
          if (mapEntry.contains("/package/eml/")) {
            resourceId = mapEntry.trim();
          }
        }
      }
    }
    
    return resourceId;
  }


  /**
   * <strong>Search Data Packages</strong> operation, specifying the pathquery string used for querying data packages in the message body.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td>XML "pathquery" string used for searching the metadata catalog</td>
   *     <td><code>application/xml</code></td>
   *     <td>curl -i --user uid=LNO,o=LTER,dc=ecoinformatics,dc=org:PASSWORD -X PUT -H "Content-Type: application/xml" -T pathQuery.xml https://package.lternet.edu/package/eml/search</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the search was successful</td>
   *     <td>An XML "resultset" document containing the search results</td>
   *     <td><code>application/xml</code></td>
   *     <td>
   *       <pre>
&lt;?xml version="1.0"?&gt;
&lt;resultset&gt;
.
.
.
&lt;/resultset&gt;
   *       </pre>
   *     </td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the request message body contains an error, such as an improperly formatted pathquery string</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to execute the Search Data Packages service method</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support POST.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param pathQuery  A pathquery XML document, as specified in the
   *                   payload of the request.
   *                
   * @return a Response, which if successful, contains a resultset XML document
   */
  @PUT
  @Path("/eml/search")
  @Produces("application/xml")
  public Response searchDataPackages(@Context HttpHeaders headers,
                                     String pathQuery) {  
    AuthToken authToken = null;
    String resourceId = null;
    String entryText = null;
    String resultsetXML = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "searchDataPackages";
    Rule.Permission permission = Rule.Permission.read;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 
      resultsetXML = dataPackageManager.searchDataPackages(pathQuery, userId, authToken);

      if (resultsetXML != null) {
        responseBuilder = Response.ok(resultsetXML);
        response = responseBuilder.build();       
      } 
      else {
        ResourceNotFoundException e = new ResourceNotFoundException(
                                            "No search results returned");
        entryText = e.getMessage();
        WebApplicationException webApplicationException =
          WebExceptionFactory.makeNotFound(e);
        response = webApplicationException.getResponse();
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
        WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
                                 e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }

  
  /*
   * Stamps a web service response with information about the web
   * service version name and its corresponding version number.
   */
  private Response stampHeader(Response r)
  {
    return
      Response.fromResponse(r).header(versionHeader, versionNumber).build();
  }
  
  
  /**
   * <strong>Update Data Package</strong> operation, specifying the scope and identifier of the data package to be updated in the URI, together with the EML document describing the data package in the message body.
   * 
   * <h4>Requests:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Request</b></th>
   *   </tr>
   *   <tr>
   *     <td>the EML document described the data package to be updated</td>
   *     <td><code>application/xml</code></td>
   *     <td>curl -i -X PUT -H "Content-Type: application/xml" -T knb-lter-lno_1_2.xml https://package.lternet.edu/package/eml/knb-lter-lno/1</td>
   *   </tr>
   * </table>
   * 
   * <h4>Responses:</h4>
   * <table border="1" cellspacing="0" cellpadding="3">
   *   <tr>
   *     <th><b>Status</b></th>
   *     <th><b>Reason</b></th>
   *     <th><b>Message Body</b></th>
   *     <th><b>MIME type</b></th>
   *     <th><b>Sample Message Body</b></th>
   *   </tr>
   *   <tr>
   *     <td>200 OK</td>
   *     <td>If the update request was successful</td>
   *     <td>A newline-separated list of URLs to the data package resources that were created</td>
   *     <td><code>text/plain</code></td>
   *     <td>
   *     https://pasta.lternet.edu/package/data/eml/knb-lter-lno/1/2/NoneSuchBugCount<br />
   *     https://pasta.lternet.edu/package/metadata/eml/knb-lter-lno/1/2<br />
   *     https://pasta.lternet.edu/package/report/eml/knb-lter-lno/1/2<br />
   *     https://pasta.lternet.edu/package/eml/knb-lter-lno/1/2
   *     </td>
   *   </tr>
   *   <tr>
   *     <td>400 Bad Request</td>
   *     <td>If the data package contains one or more metadata, metadata/data congruency, or data errors</td>
   *     <td>A quality report XML document describing the quality errors</td>
   *     <td><code>application/xml</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>401 Unauthorized</td>
   *     <td>If the requesting user is not authorized to update the data package</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>404 Not Found</td>
   *     <td>If the data package matching the specified scope and identifier values is not found</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>405 Method Not Allowed</td>
   *     <td>The specified HTTP method is not allowed for the requested resource.
   *     For example, the HTTP method was specified as DELETE but the resource
   *     can only support POST.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>409 Conflict</td>
   *     <td>If a data package with an equal or higher revision value already exists, or if the data package was previously deleted</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   *   <tr>
   *     <td>500 Internal Server Error</td>
   *     <td>The server encountered an unexpected condition which prevented 
   *     it from fulfilling the request. For example, a SQL error occurred, 
   *     or an unexpected condition was encountered while processing EML 
   *     metadata.</td>
   *     <td>An error message</td>
   *     <td><code>text/plain</code></td>
   *     <td></td>
   *   </tr>
   * </table>
   * 
   * @param emlFile  The URL to an EML document, as specified in the
   *                payload of the request.
   *                
   * @return a Response, which if successful, contains a resource map describing
   *         the contents of the updated data package
   */
  @PUT
  @Path("/eml/{scope}/{identifier}")
  @Consumes("application/xml")
  @Produces("application/xml")
  public Response updateDataPackage(
                    @Context HttpHeaders headers,
                    @PathParam("scope") String scope,
                    @PathParam("identifier") Integer identifier, 
                    File emlFile) {
    AuthToken authToken = null;
    String resourceMap = null;
    ResponseBuilder responseBuilder = null;
    Response response = null;
    final String serviceMethodName = "updateDataPackage";
    String entryText = null;
    Rule.Permission permission = Rule.Permission.write;

    try {
      authToken = getAuthToken(headers);
      String userId = authToken.getUserId();

      // Is user authorized to run the service method?
      boolean serviceMethodAuthorized = 
        isServiceMethodAuthorized(serviceMethodName, permission, authToken);
      if (!serviceMethodAuthorized) {
        throw new UnauthorizedException(
            "User " + userId + 
            " is not authorized to execute service method " + 
            serviceMethodName);
      }
      
      DataPackageManager dataPackageManager = new DataPackageManager(); 
      resourceMap = dataPackageManager.updateDataPackage(emlFile, scope, identifier, userId, authToken);

      if (resourceMap != null) {
        responseBuilder = Response.ok(resourceMap);
        response = responseBuilder.build();       
      }
      else {
        Exception e = new Exception("Data package update operation failed for unknown reason");
        throw(e);
      }
    }
    catch (IllegalArgumentException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeBadRequest(e).getResponse();
    }
    catch (InvalidPermissionException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (UnauthorizedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeUnauthorized(e).getResponse();
    }
    catch (ResourceNotFoundException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeNotFound(e).getResponse();
    }
    catch (ResourceDeletedException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (ResourceExistsException e) {
      entryText = e.getMessage();
      response = WebExceptionFactory.makeConflict(e).getResponse();
    }
    catch (UserErrorException e) {
      entryText = e.getMessage();
      response = WebResponseFactory.makeBadRequest(e);
    }
    catch (Exception e) {
      entryText = e.getMessage();
      WebApplicationException webApplicationException = 
        WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
          e, e.getMessage());
      response = webApplicationException.getResponse();
    }

    String resourceId = resourceIdFromResourceMap(resourceMap);
    audit(serviceMethodName, authToken, response, resourceId, entryText);
    response = stampHeader(response);
    return response;
  }
  
  
  /*
   * The following methods have been migrated into the Data Package Manager
   * from the original Metadata Factory service.
   */

  /**
   * <strong>Add Provenance Metadata</strong> operation. Generates provenance metadata from a metadata document in PASTA and adds it to a provided EML document.
   * 
   * <h4>Request entity:</h4>
   *
   * <p>
   * The request entity should be an XML document (MIME type
   * <code>application/xml</code>) that contains an element called <em>methods</em>
   * (one and only one), located anywhere in the XML tree. A new element
   * called <em>methodStep</em> will be appended to the <em>methods</em>
   * element for each parent EML document specified in the query string.
   * </p>
   *
   * <h4>Query parameters:</h4>
   *
   * <p>
   * Query parameters must be the <code>packageId</code> attributes of the
   * parent EML documents contained in PASTA's Metadata Catalog, with the
   * syntax <em><code>scope.identifier.revision</code></em>. Multiple
   * packageIds can be specified by delimiting them with ampersands (
   * <code>&amp;</code>), e.g.<br>
   *
   * <center>
   * <em>?packageId<sub>1</sub>&amp;packageId<sub>2</sub>&amp;...&amp;packageId<sub>n</sub></em>
   * .</center>
   * </p>
   *
   *
   * <p>
   * If a particular <code>entityName</code> in a parent EML document should
   * be recorded in the provenance metadata, it can be specified in the query
   * string with the syntax <em>packageId=entityName</em>. If multiple entity
   * names from a single EML document should be recorded, they must be
   * specified individually as<br>
   * <center>
   *
   * <em>?packageId<sub>1</sub>=entityName<sub>1</sub>&amp;packageId<sub>1</sub>=entityName<sub>2</sub>&amp;...&amp;packageId<sub>1</sub>=entityName<sub>n</sub></em>
   * ,</center><br>
   * and they must be percent-encoded ("URL encoded").
   * </p>
   *
   *
   * <h4>Responses:</h4>
   *
   * <table border="1" cellspacing="0" cellpadding="3">
   * <tr>
   * <td><b>Status</b></td>
   * <td><b>Reason</b></td>
   * <td><b>Entity</b></td>
   * <td><b>MIME type</b></td>
   * </tr>
   * <tr>
   * <td>200 OK</td>
   * <td>If the request was successful.</td>
   * <td>The modified EML document.</td>
   * <td><code>application/xml</code></td>
   * </tr>
   * <tr>
   * <td>400 Bad Request</td>
   * <td>If the request entity or query parameters cannot be properly parsed.</td>
   * <td>An error message.</td>
   * <td><code>text/plain</code></td>
   * </tr>
   * <tr>
   * <td>401 Unauthorized</td>
   * <td>If the requesting user is unauthorized to use the Provenance Factory.</td>
   * <td>An error message.</td>
   * <td><code>text/plain</code></td>
   * </tr>
   * <tr>
   * <td>417 Expectation Failed</td>
   * <td>If a request made by the Provenance Factory to PASTA's Metadata Catalog
   * fails, e.g. because the requesting user is not authorized to read an EML
   * document he/she specified.</td>
   * <td>
   * An error message.</td>
   * <td><code>text/plain</code></td>
   * </tr>
   * </table>
   *
   *
   * @param headers
   *            the HTTP request headers containing the authorization token.
   *
   * @param uriInfo
   *            contains the query string parameters.
   *
   * @param eml
   *            the EML document to which provenance will be appended.
   *
   * @return an appropriate HTTP response.
   */
    @PUT
    @Path("/eml/provenance")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(value={MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response appendProvenance(@Context HttpHeaders headers,
                                     @Context UriInfo uriInfo, String eml) {

      AuthToken authToken = null;
      String entryText = null;
      String resourceId = null;
      Response response = null;
      final String serviceMethodName = "appendProvenance";
      Rule.Permission permission = Rule.Permission.write;

      try {
        authToken = AuthTokenFactory.makeAuthToken(headers.getCookies());
        String userId = authToken.getUserId();

        // Is user authorized to run the 'appendProvenance' service method?
        boolean serviceMethodAuthorized = isServiceMethodAuthorized(
          serviceMethodName, permission, authToken);
        if (!serviceMethodAuthorized) {
          throw new UnauthorizedException("User " + userId
            + " is not authorized to execute service method "
            + serviceMethodName);
        }

        Document doc = XmlUtility.xmlStringToDoc(eml);
        Map<EmlPackageId, List<String>> provenance = 
            getProvenance(uriInfo, authToken);
        MetadataFactory metadataFactory = new MetadataFactory();
        doc = metadataFactory.make(doc, provenance, authToken);
        eml = XmlUtility.nodeToXmlString(doc);
        response = Response.ok(eml, MediaType.APPLICATION_XML).build();
      }
      catch (UnauthorizedException e) {
        entryText = e.getMessage();
        response = WebResponseFactory.makeUnauthorized(e);
      }
      catch (XmlParsingException e) {
        entryText = e.getMessage();
        response = WebResponseFactory.makeBadRequest(e);
      }
      catch (WebApplicationException e) { // not necessary
        entryText = e.getMessage();
        response = e.getResponse();
      }
      catch (Exception e) {
        entryText = e.getMessage();
        WebApplicationException webApplicationException = 
          WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, 
            e, e.getMessage());
        response = webApplicationException.getResponse();
      }

      audit(serviceMethodName, authToken, response, resourceId, entryText);
      response = stampHeader(response);
      return response;
    }

      
      private List<String> decodeEntityNames(List<String> provided) {

          List<String> decoded = new LinkedList<String>();

          for (String p : provided) {
              String s = PercentEncoder.decode(p);
              decoded.add(s);
          }

          return decoded;
      }

      
      private EmlPackageId parsePackageId(String packageId) {

          EmlPackageIdFormat format = new EmlPackageIdFormat(Delimiter.DOT);
          EmlPackageId epi = null;

          try {
              epi = format.parse(packageId);
          } catch (IllegalArgumentException e) {
              throw WebExceptionFactory.makeBadRequest(e);
          }

          if (!epi.allElementsHaveValues()) {
              String s = "The specified EML packageId '" + packageId +
                         "' does not contain a scope, identifier, and " +
                         "revision.";
              throw WebExceptionFactory.makeBadRequest(s);
          }

          return epi;
      }

      
      private Map<EmlPackageId, List<String>> getProvenance(UriInfo uriInfo,
                                                            AuthToken token) {

          // packageId::entityName pairs
          QueryString query = new QueryString(uriInfo);

          Map<EmlPackageId, List<String>> pairs =
              new LinkedHashMap<EmlPackageId, List<String>>();

          for (Entry<String, List<String>> e : query.getParams().entrySet()) {
              EmlPackageId epi = parsePackageId(e.getKey());
              List<String> parsed = decodeEntityNames(e.getValue());
              pairs.put(epi, parsed);
          }

          return pairs;
      }

      /*
       * End of methods originally from the Metadata Factory service.
       */

  }
