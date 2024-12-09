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

import edu.lternet.pasta.common.*;
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
import edu.lternet.pasta.datapackagemanager.DataPackageManager.ResourceType;
import edu.lternet.pasta.datapackagemanager.xslt.XsltUtil;
import edu.lternet.pasta.eventmanager.EmlSubscription;
import edu.lternet.pasta.eventmanager.SubscriptionRegistry;
import edu.lternet.pasta.eventmanager.XmlSubscriptionFormatV1;
import edu.lternet.pasta.metadatafactory.MetadataFactory;
import edu.ucsb.nceas.utilities.Options;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.*;

import javax.naming.ServiceUnavailableException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>
 * The Data Package Manager Web Service provides a suite of operations to
 * create, evaluate, read, update, delete, list, and search data package
 * resources in the PASTA system; additional operations provide access to
 * evaluation reports, provenance metadata, event subscriptions, and whether 
 * the user has read access to specific resources. Data package resources 
 * include metadata documents, data entities, and quality reports.
 * </p>
 * 
 * @webservicename Data Package Manager
 * @baseurl https://pasta.lternet.edu/package
 * 
 * @author dcosta
 * @version 1.0
 * @created 16-Aug-2011 1:40:03 PM
 * 
 *          <p>
 *          The Provenance Factory generates a provenance-based XML fragment
 *          that references metadata content of a parent EML document that is in
 *          the PASTA system by using the structure of the <em>methodStep</em>
 *          element, which is inserted into a user provided <em>methods</em>
 *          element (for multiple references to parent EML documents, multiple
 *          <em>methodStep</em> elements are returned, one for each parent EML
 *          document). The user provided <em>methods</em> element is not
 *          required to be associated with a valid EML document; it acts only as
 *          a root element container for the provenance-based
 *          <em>methodStep</em> element(s). The service, however, does require
 *          that only a single <em>methods</em> element exist in the user
 *          provided XML; otherwise, an ambiguity exists when determining a
 *          proper xpath location for inserting the provencance-based
 *          <em>methodStep</em> XML fragment. The service will accept a valid
 *          EML document and correctly insert the provenance-based
 *          <em>methodStep</em> XML fragment into the <em>methods</em> element,
 *          if it is the only <em>methods</em> element in the EML document. The
 *          current service design recognizes that the structure and content of
 *          an EML document may vary considerably and may be very complex. To be
 *          flexible, the service assumes that a user wanting to insert the
 *          provenance-based XML into a complex EML document would do so
 *          independent of the Provenance Factory service.
 *          </p>
 * 
 *          <p>
 *          The Provenance Factory can be used to append provenance metadata to
 *          an EML document that describes a 'derived' or 'synthetic' data set,
 *          that is, a data set that was produced from data that exists in the
 *          PASTA sytem.
 *          </p>
 * 
 *          <p>
 *          Let <em>D<sub>1</sub></em> denote a dataset in PASTA's Data Cache,
 *          and let <em>E<sub>1</sub></em> denote an EML document in PASTA's
 *          Metadata Catalog that describes <em>D<sub>1</sub></em>. If a new
 *          dataset <em>D<sub>2</sub></em> is derived from
 *          <em>D<sub>1</sub></em> using method <em>M</em>, a new EML document
 *          <em>E<sub>2</sub></em> should also be produced to describe
 *          <em>D<sub>2</sub></em>. This can be depicted as:<br>
 * 
 *          <center><em>M</em> : <em>E<sub>1</sub></em> + <em>D<sub>1</sub></em>
 *          &rarr; <em>E<sub>2</sub></em> + <em>D<sub>2</sub></em></center>
 *          </p>
 * 
 * 
 *          <p>
 *          Ideally, <em>E<sub>2</sub></em> should include a description of the
 *          provenance of <em>D<sub>2</sub></em>, that is, it was produced from
 *          <em>D<sub>1</sub></em> and <em>E<sub>1</sub></em> by applying method
 *          <em>M</em>.
 *          </p>
 * 
 *          <p>
 *          The Provenance Factory can be used to append <em>some</em>
 *          provenance information to an EML document (<em>E<sub>2</sub></em>
 *          from above) in the form of <em>methodStep</em> elements (
 *          <code>/eml/dataset/methods/methodStep</code>), whose content
 *          references the "parent" EML document(s) (<em>E<sub>1</sub></em> from
 *          above). To accomplish this, the user supplies the Provenance Factory
 *          with an EML document, to which provenance will be appended, and the
 *          packageId(s) of the parent EML document(s) to be referenced. The
 *          Provenance Factory requests the specified parent EML documents from
 *          PASTA's Metadata Catalog on behalf of the requesting user, and a
 *          single methodStep element is appended to the <em>provided</em> EML
 *          document (<em>E<sub>2</sub></em>) for each specified parent. The
 *          user can also specify particular <em>entityNames</em> (
 *          <code>/eml/dataset/dataTable/entityName</code>) in the parent EML
 *          document(s) to be included in provenance, which is useful if a
 *          parent EML document contains multiple data entities, but only one of
 *          which was used to derive the new dataset (<em>D<sub>2</sub></em>
 *          from above).
 *          </p>
 * 
 *          <p>
 *          The syntax of appended methodStep elements is shown below.
 *          </p>
 * 
 *          <p>
 * 
 *          <pre>
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
 * 
 *          </p>
 * 
 */
@Path("/")
public class DataPackageManagerResource extends PastaWebService {

	/*
	 * Class fields
	 */

	public static final String AUTH_TOKEN = "auth-token";
	private static final long SIZE_THRESHOLD_DEFAULT = 1024000L;
	
	// time-to-live default for tempoary data files, in milliseconds
	private static final long TTL_DEFAULT = 3600000L;
	
	private static Logger logger = Logger
			.getLogger(DataPackageManagerResource.class);

	public static final Set<String> VALID_EVENT_QUERY_KEYS;

	static {
		Set<String> set = new TreeSet<String>();
		set.add("creator");
		set.add("scope");
		set.add("identifier");
		set.add("revision");
		set.add("url");
		VALID_EVENT_QUERY_KEYS = Collections.unmodifiableSet(set);
	}

	private static final String SEARCH_RESULT_XML_TO_CSV_NAME = "searchResultXmlToCsv.xslt";

	/*
	 * Instance fields
	 */

	private String dataServerContext = null;
	/*
	 * Data files of this size or larger will be served by the 'dataserver' 
	 * web app which lives in a separate context.
	 */
	private long sizeThreshold;
	private long ttl;  // time to live for temporary data files, in milliseconds
	private String tmpDir = null;
	private String versionHeader = null;
	private String versionNumber = null;

	private boolean readOnly = false;


	/*
	 * Constructors
	 */

	/**
	 * Constructs a DataPackageManagerResource object. Initializes the
	 * versionHeader and versionNumber class field values of the web service.
	 */
	public DataPackageManagerResource() {
		versionHeader = ConfigurationListener.getVersionHeader();
		versionNumber = ConfigurationListener.getVersionNumber();
		
		Options options = ConfigurationListener.getOptions();
		if (options != null) {
			this.dataServerContext = options.getOption("datapackagemanager.dataserver.context");
			String readOnly = options.getOption("datapackagemanager.readOnly");
			if (readOnly != null && readOnly.equals("true")) {
				this.readOnly = true;
			}
			String sizeThresholdOption = options.getOption("datapackagemanager.dataserver.sizeThreshold");
			if (sizeThresholdOption == null || sizeThresholdOption.equals("")) {
				this.sizeThreshold = SIZE_THRESHOLD_DEFAULT;
			}
			else {
				try {
					this.sizeThreshold = new Long(sizeThresholdOption);
				}
				catch (IllegalFormatException e) {
					logger.warn("Unable to parse sizeThreshold from datapackagemanager.properties file. Using default value.");
					this.sizeThreshold = SIZE_THRESHOLD_DEFAULT;
				}
			}
			
			this.tmpDir = options.getOption("datapackagemanager.tmpDir");
			
			String ttlOption = options.getOption("datapackagemanager.dataserver.tmpDir.ttl");
			if (ttlOption == null || ttlOption.equals("")) {
				this.ttl = TTL_DEFAULT;
			}
			else {
				try {
					this.ttl = new Long(ttlOption);
				}
				catch (IllegalFormatException e) {
					logger.warn("Unable to parse 'datapackagemanager.dataserver.tmpDir.ttl' from datapackagemanager.properties file. Using default value.");
					this.ttl = TTL_DEFAULT;
				}
			}
		}
	}


	/*
	 * Class methods
	 */

	/**
	 * Gets an AuthToken object from an HttpHeaders object
	 * 
	 * @param headers
	 *            the HttpHeaders object
	 * @return an AuthToken token
	 */
	public static AuthToken getAuthToken(HttpHeaders headers) {
		Map<String, Cookie> cookiesMap = headers.getCookies();

		if (!cookiesMap.containsKey(AUTH_TOKEN)) {
			throw new UnauthorizedException("Missing authentication token: "
					+ AUTH_TOKEN);
		}

		String cookieToken = cookiesMap.get(AUTH_TOKEN).getValue();
		AuthToken authToken = new AttrListAuthTokenV1(cookieToken);

		return authToken;
	}


	/**
	 * Gets the Robot header value, if provided in the headers
	 * 
	 * @param headers
	 *            the HttpHeaders object
	 * @return the Robot header string, possibly null
	 */
	public static String getRobot(HttpHeaders headers) {
		String robotHeader = null;
		MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
		
		if (requestHeaders != null) {
			robotHeader = requestHeaders.getFirst("Robot");
		}
		
		return robotHeader;
	}


	/**
	 * Gets the UserAgent header value, if provided in the headers as set
	 * by the Gatekeeper.
	 * 
	 * @param headers
	 *            the HttpHeaders object
	 * @return the UserAgent header string, possibly null
	 */
	public static String getUserAgent(HttpHeaders headers) {
		String userAgentHeader = null;
		MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
		
		if (requestHeaders != null) {
			userAgentHeader = requestHeaders.getFirst("User-Agent");
		}
		
		return userAgentHeader;
	}


	/**
	 * Get the method name for a depth in call stack. <br />
	 * Utility function
	 * 
	 * @param depth
	 *            depth in the call stack (0 means current method, 1 means call
	 *            method, ...)
	 * @return method name
	 */
	public static String getMethodName(final int depth) {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();

		return ste[ste.length - 1 - depth].getMethodName();
	}


	/*
	 * Isolates the resourceId for the data package from a resource map string
	 * and returns it.
	 */
	protected static String resourceIdFromResourceMap(String resourceMap) {
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


	/*
	 * Instance "helper" methods
	 */

	/*
	 * Wrapper method for recording an audit when there is no robot parameter
	 */
	private void audit(String serviceMethodName, AuthToken authToken,
			           Response response, String resourceId, String entryText) {
		String robot = null;
		String userAgent = null;
		
		audit(serviceMethodName, authToken, response, resourceId, entryText, 
			  robot, userAgent);
	}


	private void audit(String serviceMethodName, AuthToken authToken,
			           Response response, String resourceId, String entryText,
			           String robot, String userAgent) {
		String auditHost = getAuditHost();
		String serviceName = getVersionString();

		try {
			int status = response.getStatus();
			Date date = new Date();
			AuditRecord auditRecord = 
					new AuditRecord(date, serviceName, entryText, authToken, 
							        status, serviceMethodName, resourceId, 
							        robot, userAgent);
			AuditManagerClient auditManagerClient = new AuditManagerClient(
					auditHost);
			auditManagerClient.logAudit(auditRecord);
		}
		catch (Exception e) {
			logger.error("Error occurred while auditing Data Package Manager "
					+ "service call for service method " + serviceMethodName
					+ " : " + e.getMessage());
		}
	}


	private List<String> decodeEntityNames(List<String> provided) {

		List<String> decoded = new LinkedList<String>();

		for (String p : provided) {
			String s = PercentEncoder.decode(p);
			decoded.add(s);
		}

		return decoded;
	}


	/*
	 * Matches the specified 'entityName' value with the entity names found in
	 * the EML document string, and returns the corresponding objectName value
	 * for the matching entity, if an objectName was specified for the matching
	 * entity.
	 * 
	 * Returns null if: (1) The EML document fails to parse, or (2) No entities
	 * match the specified entityName value, or (3) The matching entity does not
	 * specify an objectName in the EML document.
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
	 * Returns the audit host, e.g. "audit.lternet.edu"
	 * 
	 * @return version, such as {@code DataPackageManager-0.1}.
	 */
	public String getAuditHost() {
		String auditHost = null;

		Options options = ConfigurationListener.getOptions();
		if (options != null) {
			auditHost = options
					.getOption("datapackagemanager.auditmanager.host");
		}

		return auditHost;
	}


	/**
	 * Returns the service version, such as {@code DataPackageManager-0.1}.
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
	
	
	private String generateTransactionID(String operation, String scope, Integer identifier, Integer revision) {
		String transactionId = "";
		long time = System.currentTimeMillis();
		long random = (long) (Math.random() * 100000L);

		if (operation != null) transactionId = operation;
		
		if ((scope != null) && (identifier != null)) {
			transactionId = String.format("%s_%s.%d", transactionId, scope, identifier);
			if (revision != null) {
				transactionId = String.format("%s.%d", transactionId, revision);
			}
		}
		transactionId = String.format("%s_%d%d", transactionId, time, random);
		
		return transactionId;
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


	private Map<EmlPackageId, List<String>> getProvenance(UriInfo uriInfo,
			AuthToken token) {

		// packageId::entityName pairs
		QueryString query = new QueryString(uriInfo);

		Map<EmlPackageId, List<String>> pairs = new LinkedHashMap<EmlPackageId, List<String>>();

		for (Entry<String, List<String>> e : query.getParams().entrySet()) {
			String key = e.getKey();
			EmlPackageId epi = parsePackageId(key);
			List<String> parsed = decodeEntityNames(e.getValue());
			pairs.put(epi, parsed);
		}

		return pairs;
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
	 * Boolean to determine whether the user contained in the AuthToken is
	 * authorized to execute the specified service method.
	 * 
	 * @param serviceMethodName
	 *            the name of the service method
	 * @param authToken
	 *            the AuthToken containing the user name
	 * @return true if authorized to run the service method, else false
	 */
	private boolean isServiceMethodAuthorized(String serviceMethodName,
			Rule.Permission permission, AuthToken authToken) {
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
							NamedNodeMap serviceAttributesList = serviceElement
									.getAttributes();

							for (int j = 0; j < serviceAttributesList
									.getLength(); j++) {
								Node attributeNode = serviceAttributesList
										.item(j);
								nodeName = attributeNode.getNodeName();
								nodeValue = attributeNode.getNodeValue();
								if (nodeName.equals("name")) {
									String name = nodeValue;
									if (name.equals(serviceMethodName)) {
										NodeList accessNodeList = serviceElement
												.getElementsByTagName("access");
										Node accessNode = accessNodeList
												.item(0);
										String accessXML = XmlUtility
												.nodeToXmlString(accessNode);
										AccessMatrix accessMatrix = new AccessMatrix(
												accessXML);
										String principalOwner = "pasta";
										isAuthorized = accessMatrix
												.isAuthorized(authToken,
														principalOwner,
														permission);
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


	private EmlPackageId parsePackageId(String packageId) {

		EmlPackageIdFormat format = new EmlPackageIdFormat(Delimiter.DOT);
		EmlPackageId epi = null;

		try {
			epi = format.parse(packageId);
		}
		catch (IllegalArgumentException e) {
			throw WebExceptionFactory.makeBadRequest(e);
		}

		if (!epi.allElementsHaveValues()) {
			String s = "The specified EML packageId '" + packageId
					+ "' does not contain a scope, identifier, and "
					+ "revision.";
			throw WebExceptionFactory.makeBadRequest(s);
		}

		return epi;
	}


	/**
	 * Applies the quality report stylesheet to transform the quality report
	 * representation from XML to HTML for presentation purposes.
	 * 
	 * @param xmlString
	 *            the quality report XML string
	 * @param xslPath
	 *            path to the quality report stylesheet
	 * 
	 * @return htmlString the result of the transformation from XML to HTML
	 * @throws IllegalStateException
	 *             if an error occurs during the transformation process
	 */
	private String qualityReportXMLtoHTML(String xmlString, final String xslPath)
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
		logger.debug("javax.xml.transform.TransformerFactory :"
				+ System.getProperty("javax.xml.transform.TransformerFactory"));
		transformerFactory = javax.xml.transform.TransformerFactory
				.newInstance();

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


	/*
	 * Stamps a web service response with information about the web service
	 * version name and its corresponding version number.
	 */
	private Response stampHeader(Response r) {
		return Response.fromResponse(r).header(versionHeader, versionNumber)
				.build();
	}


	/**
	 * Decodes the Httpheaders.AUTHORIZATION token (as per MetadataCatalog-0.1,
	 * MetadataCatalogResource class).
	 * 
	 * @param token
	 * @return String[]
	 * 
	 *         private String[] decoder(String token) throws
	 *         UnauthorizedException { String [] userCreds = null;
	 * 
	 *         if (token == null || token.isEmpty()) throw new
	 *         UnauthorizedException("token is empty"); if
	 *         (!token.contains("Basic ") || (token.length() <=
	 *         "Basic ".length())) throw new
	 *         UnauthorizedException("Improper Token");
	 * 
	 *         try { token = token.substring("Basic ".length()); userCreds = new
	 *         String(Base64.decodeBase64(token)).split(":"); } catch (Throwable
	 *         e) { throw new UnauthorizedException(e.getMessage()); }
	 * 
	 *         if (userCreds.length != 2) throw new
	 *         UnauthorizedException("Improper Token"); return userCreds; }
	 */

	/*
	 * Instance "web-service" methods
	 */

	/**
	 * <strong>Create Data Package</strong> operation, specifying the EML
	 * document describing the data package to be created in the request message
	 * body and returning a <em>transaction identifier</em> in the response
	 * message body as plain text; the <em>transaction identifier</em> may be
	 * used in a subsequent call to <code>readDataPackageError</code> to
	 * determine the operation status; see <code>readDataPackage</code> to
	 * obtain the data package resource map if the operation completed
	 * successfully.
	 * 
	 * <h4>Request:</h4>
	 * <table border="1" cellspacing="0" cellpadding="3">
	 * <tr>
	 * <th><b>Message Body</b></th>
	 * <th><b>MIME type</b></th>
	 * <th><b>Sample Request</b></th>
	 * </tr>
	 * <tr>
	 * <td align=center>EML document</td>
	 * <td align=center><code>application/xml</code></td>
	 * <td>
	 * <code>curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:PASSWORD" 
	 * -X POST -H "Content-Type: application/xml"
	 * --data-binary @knb-lter-lno.1.1.xml https://pasta.lternet.edu/package/eml</code>
	 * </td>
	 * </tr>
	 * </table>
	 * 
	 * <h4>Response:</h4>
	 * <table border="1" cellspacing="0" cellpadding="3">
	 * <tr>
	 * <th><b>Status</b></th>
	 * <th><b>Reason</b></th>
	 * <th><b>Message Body</b></th>
	 * <th><b>MIME type</b></th>
	 * <th><b>Sample Message Body</b></th>
	 * </tr>
	 * <tr>
	 * <td align=center>202 Accepted</td>
	 * <td align=center>The <em>create data package</em> request was accepted
	 * for processing</td>
	 * <td align=center>A <em>transaction identifier</em> for use in subsequent
	 * processing of the request (see <code>readDataPackageError</code> to
	 * understand how the transaction identifier may be used to determine if an
	 * error occurred during the operation)</td>
	 * <td align=center><code>text/plain</code></td>
	 * <td align=center><code>1364424858431</code></td>
	 * </tr>
	 * <tr>
	 * <td align=center>401 Unauthorized</td>
	 * <td align=center>The requesting user is not authorized to execute this
	 * service method</td>
	 * <td align=center>An error message</td>
	 * <td align=center><code>text/plain</code></td>
	 * <td align=center><code>Error message</code></td>
	 * </tr>
	 * <tr>
	 * <td align=center>405 Method Not Allowed</td>
	 * <td align=center>The specified HTTP method is not allowed for the
	 * requested resource</td>
	 * <td align=center>An error message</td>
	 * <td align=center><code>text/plain</code></td>
	 * <td align=center><code>Error message</code></td>
	 * </tr>
	 * </table>
	 * 
	 * @param emlFile
	 *            An EML document file, as specified in the payload of the
	 *            request.
	 * 
	 * @return a Response, which if successful, contains a resource map
	 *         describing the contents of the data package
	 */
	@POST
	@Path("/eml")
	@Consumes("application/xml")
	@Produces("text/plain")
	public Response createDataPackage(@Context HttpHeaders headers, File emlFile) {
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "createDataPackage";
		Rule.Permission permission = Rule.Permission.write;
		AuthToken authToken = null;

		try {
			if (this.readOnly) {
				throw new ServiceUnavailableException("PASTA is now in read-only mode");
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();
			
			// Is user authorized to run the 'createDataPackage' service method?
			boolean serviceMethodAuthorized = isServiceMethodAuthorized(serviceMethodName, permission, authToken);

			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
					String.format("User %s is not authorized to execute service method %s", 
							      userId, serviceMethodName));
			}

			String transaction = generateTransactionID("create", null, null, null);
			
			// Perform createDataPackage in new thread
			Creator creator = new Creator(emlFile, userId, authToken, transaction);
			ExecutorService executorService = Executors.newCachedThreadPool();
			executorService.execute(creator);
			executorService.shutdown();
			
			responseBuilder = Response.status(Response.Status.ACCEPTED);
			responseBuilder.entity(transaction);
			response = responseBuilder.build();
			response = stampHeader(response);
		}
		catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		}
		catch (ServiceUnavailableException e) {
			response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
		}

		return response;
	}


	/**
	 * <strong>Create Data Package Archive (Zip)</strong> operation, specifying
	 * the scope, identifier, and revision of the data package to be Zipped in
	 * the URI, and returning a <em>transaction identifier</em> in the response
	 * message body as plain text; the <em>transaction identifier</em> may be
	 * used in a subsequent call to <code>readDataPackageError</code> to
	 * determine the operation status or to <code>readDataPackageArchive</code>
	 * to obtain the Zip archive.
	 * 
	 * <h4>Request:</h4>
	 * <table border="1" cellspacing="0" cellpadding="3">
	 * <tr>
	 * <th><b>Message Body</b></th>
	 * <th><b>MIME type</b></th>
	 * <th><b>Sample Request</b></th>
	 * </tr>
	 * <tr>
	 * <td align=center>none</td>
	 * <td align=center>none</td>
	 * <td><code>curl -i -X POST
	 * https://pasta.lternet.edu/package/archive/eml/knb-lter-lno/1/1</code></td>
	 * </tr>
	 * </table>
	 * 
	 * <h4>Response:</h4>
	 * <table border="1" cellspacing="0" cellpadding="3">
	 * <tr>
	 * <th><b>Status</b></th>
	 * <th><b>Reason</b></th>
	 * <th><b>Message Body</b></th>
	 * <th><b>MIME type</b></th>
	 * <th><b>Sample Message Body</b></th>
	 * </tr>
	 * <tr>
	 * <td align=center>202 Accepted</td>
	 * <td align=center>The create request was accepted for processing</td>
	 * <td align=center>A transaction identifier for use in subsequent
	 * processing of the request (see <code>readDataPackageError</code> to
	 * understand how the transaction identifier may be used to determine if an
	 * error occurred during the operation)</td>
	 * <td align=center><code>text/plain</code></td>
	 * <td align=center><code>1364424858431</code></td>
	 * </tr>
	 * <tr>
	 * <td align=center>401 Unauthorized</td>
	 * <td align=center>The requesting user is not authorized to execute this
	 * service method</td>
	 * <td align=center>An error message</td>
	 * <td align=center><code>text/plain</code></td>
	 * <td align=center><code>Error message</code></td>
	 * 
	 * </tr>
	 * <tr>
	 * <td align=center>405 Method Not Allowed</td>
	 * <td align=center>The specified HTTP method is not allowed for the
	 * requested resource</td>
	 * <td align=center>An error message</td>
	 * <td align=center><code>text/plain</code></td>
	 * <td align=center><code>Error message</code></td>
	 * 
	 * </tr>
	 * </table>
	 * 
	 * @return a Response, which if successful, contains a resource map
	 *         describing the contents of the data package
	 */
	@POST
	@Path("/archive/eml/{scope}/{identifier}/{revision}")
	@Produces("text/plain")
	@Consumes("text/plain")
	public Response createDataPackageArchive(@Context HttpHeaders headers,
			@PathParam("scope") String scope,
			@PathParam("identifier") Integer identifier,
			@PathParam("revision") Integer revision) {

		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "createDataPackageArchive";
		Rule.Permission permission = Rule.Permission.write;
		AuthToken authToken = null;

		String transaction = generateTransactionID("archive", scope, identifier, revision);

		try {
			if (this.readOnly) {
				throw new ServiceUnavailableException("PASTA is now in read-only mode");
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the 'createDataPackage' service method?
			boolean serviceMethodAuthorized = isServiceMethodAuthorized(
					serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException("User " + userId
						+ " is not authorized to execute service method "
						+ serviceMethodName);
			}

			// Perform createDataPackage in new thread
			Archivor archivor = new Archivor(scope, identifier, revision, userId,
					authToken, transaction);
			ExecutorService executorService = Executors.newCachedThreadPool();
			executorService.execute(archivor);
			executorService.shutdown();

			responseBuilder = Response.status(Response.Status.ACCEPTED);
			responseBuilder.entity(transaction);
			response = responseBuilder.build();
			response = stampHeader(response);
		}
		catch (ServiceUnavailableException e) {
			response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
		}

		return response;

	}

	/**
	 * <strong>Download Data Package as a ZIP archive</strong> operation, specifying the
	 * scope, identifier, and revision of the data package in the URI, and returning a
	 * stream of zipped bytes in the response message body.
	 *
	 * <h4>Request:</h4>
	 * <table border="1" cellspacing="0" cellpadding="3">
	 * <tr>
	 * <th><b>Message Body</b></th>
	 * <th><b>MIME type</b></th>
	 * <th><b>Sample Request</b></th>
	 * </tr>
	 * <tr>
	 * <td align=center>none</td>
	 * <td align=center>none</td>
	 * <td><code>curl -i -X GET
	 * https://pasta.lternet.edu/package/download/eml/knb-lter-lno/1/1</code></td>
	 * </tr>
	 * </table>
	 *
	 * <h4>Response:</h4>
	 * <table border="1" cellspacing="0" cellpadding="3">
	 *
	 * <tr>
	 * <th><b>Status</b></th>
	 * <th><b>Reason</b></th>
	 * <th><b>Message Body</b></th>
	 * <th><b>MIME type</b></th>
	 * <th><b>Sample Message Body</b></th>
	 * </tr>
	 *
	 * <tr>
	 * <td align=center>200 OK</td>
	 * <td align=center>The request was successful</td>
	 * <td align=center><code>ZIP bytes</code></td>
	 * <td align=center><code>application/zip</code></td>
	 * <td align=center><code>[ZIP bytes]</code></td>
	 * </tr>
	 *
	 * <tr>
	 * <td align=center>401 Unauthorized</td>
	 * <td align=center>The requesting user is not authorized to execute this service method</td>
	 * <td align=center>An error message</td>
	 * <td align=center><code>text/plain</code></td>
	 * <td align=center><code>Error message</code></td>
	 * </tr>
	 *
	 * <tr>
	 * <td align=center>405 Method Not Allowed</td>
	 * <td align=center>The specified HTTP method is not allowed for the requested resource</td>
	 * <td align=center>An error message</td>
	 * <td align=center><code>text/plain</code></td>
	 * <td align=center><code>Error message</code></td>
	 * </tr>
	 *
	 * </table>
	 *
	 * @return a Response, which, if successful, contains a stream of zipped bytes.
	 */
	@GET
	@Path("/download/eml/{scope}/{identifier}/{revision}")
	@Produces("application/zip")
	public Response downloadDataPackageArchive(
			@Context HttpHeaders headers,
			@PathParam("scope") String scope,
			@PathParam("identifier") Integer identifier,
			@PathParam("revision") Integer revision
	) throws Exception
	{
		AuthToken authToken = getAuthToken(headers);
		String userId = authToken.getUserId();

		final String serviceMethodName = "createDataPackageArchive";

		// Is user authorized to run the 'createDataPackage' service method?
		boolean serviceMethodAuthorized = isServiceMethodAuthorized(serviceMethodName,
				Rule.Permission.write, authToken
		);
		if (!serviceMethodAuthorized) {
			throw new UnauthorizedException(String.format(
					"User %s is not authorized to execute service method %s", serviceMethodName, userId));
		}

		if (this.readOnly) {
			throw new ServiceUnavailableException("PASTA is now in read-only mode");
		}

		Response.ResponseBuilder responseBuilder;

		// Create streaming ZIP response
		DataPackageManager dpm = new DataPackageManager();
		ZipPackage zipPackage = new ZipPackage(dpm, scope, identifier, revision, userId, authToken);
		List<String> dataResourceIdList = zipPackage.getDataResourceIdList();

		String entryText = null;
		try {
			StreamingOutput streamingZipOutput = outputStream -> {
				try {
					zipPackage.writeZip(outputStream);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};
			responseBuilder = Response.status(Response.Status.OK);
			responseBuilder.entity(streamingZipOutput);
		} catch (ResourceNotFoundException e) {
			responseBuilder = Response.status(Response.Status.NOT_FOUND);
			entryText = e.getMessage();
			responseBuilder.entity(entryText);
		} catch (UnauthorizedException e) {
			responseBuilder = Response.status(Response.Status.UNAUTHORIZED);
			entryText = e.getMessage();
			responseBuilder.entity(entryText);
		} catch (Exception e) {
			responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
			entryText = e.getMessage();
			responseBuilder.entity(entryText);
		}

		Response response = responseBuilder.build();

		String resourceId = DataPackageManager.composeResourceId(ResourceType.archive, scope, identifier, revision, null);

		// Add audit record (which also increases download counter) for the download of the zip archive itself.
		audit("readDataPackageArchive", authToken, response, resourceId, entryText);

		// Add audit records (which also increases download counters) for each individual data entity in the zip file. Only data
		// entities for which the user has permissions, and which are present in the zip file, are included.
		for (String dataResourceId : dataResourceIdList) {
			audit("readDataEntity", authToken, response, dataResourceId, "Downloaded as part of zip archive");
		}

		return response;
	}

		/**
		 * <strong>Create Reservation</strong> operation, creates a new
		 * reservation in PASTA on the next reservable identifier for the
		 * specified scope.
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
		 * <td>201 Created</td>
		 * <td>If the create reservation request was successful.</td>
		 * <td>The integer value of the reserved identifier</td>
		 * <td>N/A</td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the request entity contains an error, such as improperly formatted
		 * XML, EML packageId, or URL.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to create subscriptions.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            the HTTP request headers containing the authorization token.
		 * @return an appropriate HTTP response.
		 */
		@POST @Path("/reservations/eml/{scope}") @Consumes(MediaType.APPLICATION_XML)
		public Response createReservation (@Context HttpHeaders
		headers, @PathParam("scope") String scope){
		ResponseBuilder responseBuilder = null;
		AuthToken authToken = null;
		String msg = null;
		Rule.Permission permission = Rule.Permission.write;
		Response response = null;
		final String serviceMethodName = "createReservation";

		try {
			if (this.readOnly) {
				throw new ServiceUnavailableException("PASTA is now in read-only mode");
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the 'createReservation' service
			// method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);

			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dpm = new DataPackageManager();

			Integer reservationIdentifier = dpm.createReservation(userId, scope);
			msg = String.format("Created reservation for data package: %s.%d", scope,
					reservationIdentifier);
			responseBuilder = Response.status(Response.Status.CREATED);
			responseBuilder.entity(reservationIdentifier.toString());
			response = responseBuilder.build();
			response = stampHeader(response);
		} catch (XmlParsingException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
			msg = e.getMessage();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
			msg = e.getMessage();
		} catch (ResourceExistsException e) {
			response = WebExceptionFactory.makeConflict(e).getResponse();
			msg = e.getMessage();
		} catch (UserErrorException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
			msg = e.getMessage();
		} catch (ServiceUnavailableException e) {
			response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
		} catch (Exception e) {
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
			msg = e.getMessage();
		} finally {
			audit(serviceMethodName, authToken, response, null, msg);
		}

		return response;
	}


		/**
		 * <strong>Delete Reservation</strong> operation, deletes an existing
		 * reservation in PASTA for the specified scope and identifier.
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
		 * <td>If the delete request was successful.</td>
		 * <td>The integer value of the deleted identifier.</td>
		 * <td>N/A</td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the request entity contains an error, such as improperly formatted
		 * XML, EML packageId, or URL.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to delete the specified reservation.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            the HTTP request headers containing the authorization token.
		 *
		 * @return an appropriate HTTP response.
		 */
		@DELETE @Path("/reservations/eml/{scope}/{identifier}")
		public Response deleteReservation (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier){
		ResponseBuilder responseBuilder = null;
		AuthToken authToken = null;
		String msg = null;
		Rule.Permission permission = Rule.Permission.write;
		Response response = null;
		final String serviceMethodName = "deleteReservation";

		try {
			if (this.readOnly) {
				throw new ServiceUnavailableException("PASTA is now in read-only mode");
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the 'deleteReservation' service
			// method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);

			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dpm = new DataPackageManager();

			Integer deletedIdentifier = dpm.deleteReservation(userId, scope, identifier);
			msg = String.format("Deleted reservation for data package: %s.%d", scope,
					deletedIdentifier);
			responseBuilder = Response.status(Response.Status.OK);
			responseBuilder.entity(deletedIdentifier.toString());
			response = responseBuilder.build();
			response = stampHeader(response);
		} catch (NotActiveException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
			msg = e.getMessage();
		} catch (ResourceNotFoundException e) {
			msg = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
			msg = e.getMessage();
		} catch (ServiceUnavailableException e) {
			response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
		} catch (UserErrorException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
			msg = e.getMessage();
		} catch (Exception e) {
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
			msg = e.getMessage();
		} finally {
			audit(serviceMethodName, authToken, response, null, msg);
		}

		return response;
	}


		/**
		 * <strong>Evaluate Data Package</strong> operation, specifying the EML
		 * document describing the data package to be evaluated in the request
		 * message body, and returning a <em>transaction identifier</em> in the
		 * response message body as plain text; the <em>transaction identifier</em>
		 * may be used in a subsequent call to <code>readDataPackageError</code> to
		 * determine the operation status or to <code>readEvaluateReport</code> to
		 * obtain the evaluate quality report.
		 *
		 * <h4>Request:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>EML document</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code>curl -i -X POST -H "Content-Type: application/xml"
		 * --data-binary @eml.xml
		 * https://pasta.lternet.edu/package/evaluate/eml</code></td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Query parameters:</h4>
		 *
		 * <p>
		 * An optional query parameter, "useChecksum", can be appended to the URL. When specified,
		 * the useChecksum query parameter directs the server to determine whether it can use an
		 * existing copy of a data entity from a previous revision of the data package based on
		 * matching a metadata-documented checksum value (MD5 or SHA-1) to the checksum of the
		 * existing copy. If a match is found, the server will skip the upload of the data entity from
		 * the remote URL and instead use its matching copy.
		 *
		 * Please Note: Specifying "useChecksum" can save time by eliminating data uploads, but clients
		 * should take care to ensure that metadata-documented checksum values are accurate and up to date.
		 * </p>
		 *
		 * <h4>Response:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>202 Accepted</td>
		 * <td align=center>The evaluate request was accepted for processing</td>
		 * <td align=center>A transaction identifier for use in subsequent
		 * processing of the request (see <code>readDataPackageError</code> to
		 * understand how the transaction identifier may be used to determine if an
		 * error occurred during the operation)</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>1364424858431</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to execute this
		 * service method</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * </table>
		 *
		 * @param emlFile
		 *            An EML document file, as specified in the payload of the
		 *            request.
		 *
		 * @return a Response, which if successful, contains a quality report XML
		 *         document
		 */
		@POST @Path("/evaluate/eml") @Consumes("application/xml") @Produces(
				{"application/xml", "text/html"}) public Response evaluateDataPackage
		(@Context HttpHeaders headers, @Context UriInfo uriInfo, File emlFile){
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "evaluateDataPackage";
		Rule.Permission permission = Rule.Permission.write;
		AuthToken authToken = null;


		try {
			if (this.readOnly) {
				throw new ServiceUnavailableException("PASTA is now in read-only mode");
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			QueryString queryStr = new QueryString(uriInfo);
			Map<String, List<String>> queryParams = queryStr.getParams();
			boolean useChecksum = false;
			if (queryParams != null) {
				for (String key : queryParams.keySet()) {
					if (key.equalsIgnoreCase("useChecksum")) {
						useChecksum = true;
					}
				}
			}

			// Is user authorized to run the 'evaluateDataPackage' service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			String transaction = generateTransactionID("evaluate", null, null, null);
			String msg = String.format("Evaluate (transaction: %s)", transaction);
			logger.info(msg);

			// Perform evaluateDataPackage in new thread
			Evaluator evaluator =
					new Evaluator(emlFile, userId, authToken, transaction, useChecksum);
			ExecutorService executorService = Executors.newCachedThreadPool();
			executorService.execute(evaluator);
			executorService.shutdown();

			responseBuilder = Response.status(Response.Status.ACCEPTED);
			responseBuilder.entity(transaction);
			response = responseBuilder.build();
			response = stampHeader(response);
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ServiceUnavailableException e) {
			response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
		}

		return response;

	}


		/**
		 * <strong>Get Provenance Metadata</strong> operation, specifying the scope,
		 * identifier, and revision of the parent data package whose provenance metadata is
		 * to be generated and retrieved, returning an XML string representing a
		 * &lt;methodStep&gt; element that can be inserted into the &lt;methods&gt;
		 * section of a dependent data package.
		 *
		 * <h4>Request:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET https://pasta.lternet.edu/package/provenance/eml/knb-lter-lno/1/1</code></td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 *
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <td align=center><b>Status</b></td>
		 * <td align=center><b>Reason</b></td>
		 * <td align=center><b>Message Body</b></td>
		 * <td align=center><b>MIME type</b></td>
		 * <td align=center><b>Sample Message Body</b></td>
		 * </tr>
		 *
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request was successful</td>
		 * <td align=center>The modified XML document</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 *
		 * <pre>
		 * &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
		 * &lt;methods&gt;
		 *   &lt;methodStep&gt;
		 *     &lt;description&gt;
		 *       &lt;para&gt;This method step describes provenance-based metadata...
		 * .
		 * .
		 * .
		 *   &lt;/methodStep&gt;
		 * &lt;/methods&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 *
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request entity or query parameters cannot be
		 * properly parsed</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is unauthorized to use the
		 * Provenance Factory</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>417 Expectation Failed</td>
		 * <td align=center>The request fails for an incorrect user expectation
		 * (e.g., because the requesting user is not authorized to read an EML
		 * document that is specified in the query parameter)</td>
		 * <td align=center>
		 * An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
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
		@GET @Path("/provenance/eml/{scope}/{identifier}/{revision}") @Consumes(
				MediaType.APPLICATION_XML) @Produces(
				value = {MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
		public Response getProvenanceMetadata (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){

		AuthToken authToken = null;
		String entryText = null;
		String resourceId = null;
		Response response = null;
		final String serviceMethodName = "getProvenanceMetadata";
		Rule.Permission permission = Rule.Permission.write;
		String eml = null;

		try {
			authToken = AuthTokenFactory.makeAuthToken(headers.getCookies());
			String userId = authToken.getUserId();

			// Is user authorized to run the 'appendProvenance' service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			MetadataFactory metadataFactory = new MetadataFactory();
			eml = metadataFactory.generateEML(scope, identifier, revision, authToken);
			response = Response.ok(eml, MediaType.APPLICATION_XML).build();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeUnauthorized(e);
		} catch (XmlParsingException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeNotFound(e);
		} catch (WebApplicationException e) { // not necessary
			entryText = e.getMessage();
			response = e.getResponse();
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);
		response = stampHeader(response);
		return response;
	}


		/*
		 * The following methods have been migrated into the Data Package Manager
		 * from the original Metadata Factory service.
		 */

		/**
		 * <strong>Add Provenance Metadata</strong> from Level-1 metadata in PASTA
		 * to an XML document containing a single <em>methods</em> element in the
		 * request message body.
		 *
		 * <h4>Request:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>XML document</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code>curl -i -X PUT -H "Content-Type: application/xml"
		 * --data-binary @methods.xml
		 * https://pasta.lternet.edu/package/provenance/eml?knb-lter-lno.1.1</code></td>
		 * </tr>
		 * </table>
		 *
		 * <p>
		 * The request entity should be an XML document (MIME type
		 * <code>application/xml</code>) that contains an element called
		 * <em>methods</em> (one and only one), located anywhere in the XML tree. A
		 * new element called <em>methodStep</em> will be appended to the
		 * <em>methods</em> element for each parent EML document specified in the
		 * query string.
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
		 * <p>
		 * If a particular <code>entityName</code> in a parent EML document should
		 * be recorded in the provenance metadata, it can be specified in the query
		 * string with the syntax <em>packageId=entityName</em>. If multiple entity
		 * names from a single EML document should be recorded, they must be
		 * specified individually as<br>
		 * <center>
		 *
		 * <em>?packageId<sub>1</sub>=entityName<sub>1</sub>&amp;packageId<sub>1</sub>=entityName<sub>2</sub>&amp;...&amp;packageId<sub>1</sub>=entityName<sub>n</sub></em>
		 * </center><br>
		 * All query parameter values must be URL encoded.
		 * </p>
		 *
		 *
		 * <h4>Responses:</h4>
		 *
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <td align=center><b>Status</b></td>
		 * <td align=center><b>Reason</b></td>
		 * <td align=center><b>Message Body</b></td>
		 * <td align=center><b>MIME type</b></td>
		 * <td align=center><b>Sample Message Body</b></td>
		 * </tr>
		 *
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request was successful</td>
		 * <td align=center>The modified XML document</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 *
		 * <pre>
		 * &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
		 * &lt;methods&gt;
		 *   &lt;methodStep&gt;
		 *     &lt;description&gt;
		 *       &lt;para&gt;This method step describes provenance-based metadata...
		 * .
		 * .
		 * .
		 *   &lt;/methodStep&gt;
		 * &lt;/methods&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 *
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request entity or query parameters cannot be
		 * properly parsed</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is unauthorized to use the
		 * Provenance Factory</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>417 Expectation Failed</td>
		 * <td align=center>The request fails for an incorrect user expectation
		 * (e.g., because the requesting user is not authorized to read an EML
		 * document that is specified in the query parameter)</td>
		 * <td align=center>
		 * An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
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
		 *
		 @PUT
		 @Path("/provenance/eml")
		 @Consumes(MediaType.APPLICATION_XML)
		 @Produces(value = { MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN })
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
		 Map<EmlPackageId, List<String>> provenance = getProvenance(uriInfo,
		 authToken);
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
		 catch (ResourceNotFoundException e) {
		 entryText = e.getMessage();
		 response = WebResponseFactory.makeNotFound(e);
		 }
		 catch (WebApplicationException e) { // not necessary
		 entryText = e.getMessage();
		 response = e.getResponse();
		 }
		 catch (Exception e) {
		 entryText = e.getMessage();
		 WebApplicationException webApplicationException = WebExceptionFactory
		 .make(Response.Status.INTERNAL_SERVER_ERROR, e,
		 e.getMessage());
		 response = webApplicationException.getResponse();
		 }

		 audit(serviceMethodName, authToken, response, resourceId, entryText);
		 response = stampHeader(response);
		 return response;
		 }
		 */

		/**
		 * <strong>Is Authorized</strong> (to <em>READ</em> resource) operation,
		 * determines whether the user as defined in the authentication token has
		 * permission to read the specified data package resource.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * http://pasta.lternet.edu/package/authz?resourceId=https://pasta.lternet.edu/package/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Query parameters:</h4>
		 *
		 * <p>
		 * The query parameter must be the <code>resourceId</code> of the PASTA
		 * resource that is being reviewed for authorization:
		 *
		 * <center><em>?resourceId=&lt;resource identifier&gt;</em></center>
		 * </p>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The user is authorized to read the data package resource
		 * </td>
		 * <td align=center>The specific resource</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>https://pasta.lternet.edu/package/eml/knb-lter-lno/1/1</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the
		 * resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>The resource being reviewed is not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @return a Response object containing a data package resource id if
		 *         permission is allowed, else returns a 401 Unauthorized response
		 */
		@GET @Path("/authz") @Produces("text/plain") public Response isAuthorized
		(@Context HttpHeaders headers, @QueryParam("resourceId") @DefaultValue("") String
		resourceId){

		AuthToken authToken = null;
		String entryText = "/package/authz?resourceId=" + resourceId;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "isAuthorized";
		Rule.Permission permission = Rule.Permission.read;

		authToken = getAuthToken(headers);
		String userId = authToken.getUserId();

		// Is user authorized to run the service method?
		boolean serviceMethodAuthorized =
				isServiceMethodAuthorized(serviceMethodName, permission, authToken);
		if (!serviceMethodAuthorized) {
			throw new UnauthorizedException(
					"User " + userId + " is not authorized to execute service method " +
							serviceMethodName);
		}

		try {

			DataPackageManager dpm = new DataPackageManager();
			Boolean isAuthorized = dpm.isAuthorized(authToken, resourceId, permission);

			if (isAuthorized != null && isAuthorized) {
				responseBuilder = Response.ok(resourceId);
				response = responseBuilder.build();
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;

	}


		/**
		 *
		 * <strong>List Data Entities</strong> operation, specifying the scope,
		 * identifier, and revision values to match in the URI.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/data/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>A list of data entity identifiers matching the specified
		 * scope, identifier, and revisions values</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 *
		 * <pre>
		 * EntityOne
		 * EntityTwo
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of the data entities</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package entities associated with the specified
		 * packageId are found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package. A string that represents a
		 *            whole number, or, the symbolic values "oldest" or "newest".
		 * @return a Response, containing a newline separated list of data entity
		 *         identifiers
		 */
		@GET @Path("/data/eml/{scope}/{identifier}/{revision}") @Produces("text/plain")
		public Response listDataEntities (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
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
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			/*
			 * Handle symbolic revisions such as "newest" and "oldest".
			 */
			if (revision != null) {
				if (revision.equals("newest")) {
					Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
					if (newest != null) {
						revision = newest.toString();
					}
				}
				else if (revision.equals("oldest")) {
					Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
					if (oldest != null) {
						revision = oldest.toString();
					}
				}
			}

			Integer revisionInt = new Integer(revision);
			String entityList =
					dataPackageManager.listDataEntities(scope, identifier, revisionInt, userId);

			if (entityList != null) {
				responseBuilder = Response.ok(entityList.trim());
				response = responseBuilder.build();
			}
			else {
				String message = "An unknown error occurred";
				throw new Exception(message);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List Active Reservations</strong> operation, lists the set of data
		 * package identifiers that users have actively reserved in PASTA.
		 * Note that data packages identifier that have been successfully uploaded
		 * into PASTA are no longer considered active reservations and thus are not
		 * included in this list.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>None</td>
		 * <td align=center></td>
		 * <td>
		 * <code>curl -i -X GET "https://pasta.lternet.edu/package/reservations/eml"
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The query was successful</td>
		 * <td align=center>An XML document containing the list of active data
		 * package reservations</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 *
		 * <pre>
		 &lt;reservations&gt;
		 &lt;reservation&gt;
		 &lt;docid&gt;edi.99&lt;/docid&gt;
		 &lt;principal&gt;uid=LNO,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;dateReserved&gt;2017-01-23 14:11:48.234&lt;/dateReserved&gt;
		 &lt;/reservation&gt;
		 &lt;reservation&gt;
		 &lt;docid&gt;edi.100&lt;/docid&gt;
		 &lt;principal&gt;uid=LNO,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;dateReserved&gt;2017-01-23 14:14:49.205&lt;/dateReserved&gt;
		 &lt;/reservation&gt;
		 &lt;/reservations&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request message body contains an error, such as an
		 * improperly formatted path query string</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to execute the
		 * Search Data Packages service method</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @return a Response, which if successful, contains an XML document
		 */
		@GET @Path("/reservations/eml") @Produces("application/xml")
		public Response listActiveReservations (@Context HttpHeaders
		headers, @Context UriInfo uriInfo){
		AuthToken authToken = null;
		String resourceId = null;
		String entryText = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listActiveReservations";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			String xml = dataPackageManager.listActiveReservations();
			responseBuilder = Response.ok(xml);
			response = responseBuilder.build();
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);
		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List Reservation Identifiers</strong> operation, lists the set of
		 * numeric identifiers for the specified scope that end users have actively
		 * reserved for future upload to PASTA.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>None</td>
		 * <td align=center></td>
		 * <td>
		 * <code>curl -i -X GET "https://pasta.lternet.edu/package/reservations/eml/edi"
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The query was successful</td>
		 * <td align=center>A simple list of numeric identifier values reserved for
		 * the specified scope, one identifier per line</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 *
		 * <pre>
		 67
		 68
		 69
		 70
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request message body contains an error, such as an
		 * improperly formatted path query string</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to execute the
		 * Search Data Packages service method</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @return a Response, which if successful, contains an XML document
		 */
		@GET @Path("/reservations/eml/{scope}") @Produces("text/plain")
		public Response listReservationIdentifiers (@Context HttpHeaders
		headers, @PathParam("scope") String scope){
		AuthToken authToken = null;
		String resourceId = null;
		String entryText = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listReservationIdentifiers";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			String reservations = dataPackageManager.listReservationIdentifiers(scope);
			responseBuilder = Response.ok(reservations);
			response = responseBuilder.build();
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);
		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>List Data Descendants</strong> operation, specifying the scope,
		 * identifier, and revision values to match in the URI.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/descendants/eml/lter-landsat/7/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>An XML-formatted list representing descendant data packages. A descendant
		 * data package is defined as a data package which depends on the specified data package as
		 * one of its data sources.</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 * <pre>
		 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 * &lt;dataDescendants&gt;
		 *     &lt;dataDescendant&gt;
		 *         &lt;packageId&gt;lter-landsat-ledaps.7.1&lt;/packageId&gt;
		 *         &lt;title&gt;LEDAPS corrected Landsat Enhanced Thematic Mapper image data for Andrews Forest LTER collected on 1984&#x2d;05&#x2d;16&lt;/title&gt;
		 *         &lt;url&gt;https://pasta.lternet.edu/package/metadata/eml/lter-landsat-ledaps/7/1&lt;/url&gt;
		 *     &lt;/dataDescendant&gt;
		 * &lt;/dataDescendants&gt;
		 * </pre>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of the data entities</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package entities associated with the specified
		 * packageId are found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package. A string that represents a
		 *            whole number, or, the symbolic values "oldest" or "newest".
		 * @return a Response, containing an XML-formatted list of data descendants.
		 */
		@GET @Path("/descendants/eml/{scope}/{identifier}/{revision}") @Produces(
				"application/xml") public Response listDataDescendants (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listDataDescendants";
		Rule.Permission permission = Rule.Permission.read;
		AuthToken authToken = null;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			/*
			 * Handle symbolic revisions such as "newest" and "oldest".
			 */
			if (revision != null) {
				if (revision.equals("newest")) {
					Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
					if (newest != null) {
						revision = newest.toString();
					}
				}
				else if (revision.equals("oldest")) {
					Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
					if (oldest != null) {
						revision = oldest.toString();
					}
				}
			}

			Integer revisionInt = new Integer(revision);
			String dataDescendantsXML =
					dataPackageManager.listDataDescendants(scope, identifier, revisionInt,
							authToken);

			if (dataDescendantsXML != null) {
				responseBuilder = Response.ok(dataDescendantsXML.trim());
				response = responseBuilder.build();
			}
			else {
				String message = "An unknown error occurred";
				throw new Exception(message);
			}
		} catch (IllegalArgumentException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>List Data Sources</strong> operation, specifying the scope,
		 * identifier, and revision values to match in the URI.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/sources/eml/lter-landsat-ledaps/7/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>An XML-formatted list of PASTA metadata resource identifiers representing
		 * the data sources from which this data package was derived</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 * <pre>
		 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 * &lt;dataSources&gt;
		 *    &lt;dataSource&gt;
		 *        &lt;packageId&gt;lter-landsat.7.1&lt;/packageId&gt;
		 *        &lt;title&gt;Landsat Enhanced Thematic Mapper image data for Andrews Forest LTER collected on 1984-07-03&lt;/title&gt;
		 *        &lt;url&gt;https://pasta.lternet.edu/package/metadata/eml/lter-landsat/7/1&lt;/url&gt;
		 *    &lt;/dataSource&gt;
		 * &lt;/dataSources&gt;
		 * </pre>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of the data entities</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package entities associated with the specified
		 * packageId are found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package. A string that represents a
		 *            whole number, or, the symbolic values "oldest" or "newest".
		 * @return a Response, containing an XML-formatted list of
		 *         metadata resources representing the data sources from which the
		 *         specified data package was derived
		 */
		@GET @Path("/sources/eml/{scope}/{identifier}/{revision}") @Produces(
				"application/xml") public Response listDataSources (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listDataSources";
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
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			/*
			 * Handle symbolic revisions such as "newest" and "oldest".
			 */
			if (revision != null) {
				if (revision.equals("newest")) {
					Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
					if (newest != null) {
						revision = newest.toString();
					}
				}
				else if (revision.equals("oldest")) {
					Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
					if (oldest != null) {
						revision = oldest.toString();
					}
				}
			}

			Integer revisionInt = new Integer(revision);
			String dataSourcesList =
					dataPackageManager.listDataSources(scope, identifier, revisionInt, authToken);

			if (dataSourcesList != null) {
				responseBuilder = Response.ok(dataSourcesList.trim());
				response = responseBuilder.build();
			}
			else {
				String message = "An unknown error occurred";
				throw new Exception(message);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List Data Package Identifiers</strong> operation, specifying the
		 * scope value to match in the URI.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/eml/knb-lter-lno</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>A newline-separated list of data package identifier
		 * values matching the specified scope value</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 *
		 * <pre>
		 * 1004
		 * 1005
		 * 1007
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal scope
		 * value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of the identifier values</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data packages identifiers associated with the
		 * specified scope are found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package.
		 * @return a Response, containing a newline separated list of identifier
		 *         values matching the specified scope values
		 */
		@GET @Path("/eml/{scope}") @Produces("text/plain")
		public Response listDataPackageIdentifiers (@Context HttpHeaders
		headers, @PathParam("scope") String scope){
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
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			boolean includeDeleted = false;
			String identifierList =
					dataPackageManager.listDataPackageIdentifiers(scope, includeDeleted);

			if (identifierList != null) {
				responseBuilder = Response.ok(identifierList.trim());
				response = responseBuilder.build();
			}
			else {
				String message = "An unknown error occurred";
				throw new Exception(message);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List Data Package Revisions</strong> operation, specifying the
		 * scope and identifier values to match in the URI. The request may be
		 * filtered by applying the modifiers "oldest" or "newest" to the "filter"
		 * query parameter.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/eml/knb-lter-lno/1</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/eml/knb-lter-lno/1?filter=newest</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Query parameters:</h4>
		 *
		 * <p>
		 * If present, the query parameter must be <code>filter</code> and one of
		 * either "newest" or "oldest":
		 *
		 * <center><em>?filter=newest</em> or <center><em>?filter=oldest</em>
		 * </center>
		 * </p>
		 *
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>A newline-separated list of revision values matching the
		 * specified scope and identifier values</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 *
		 * <pre>
		 * 1
		 * 2
		 * 3
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal scope
		 * or identifier value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of the data package revisions</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package revisions associated with the specified
		 * scope and identifier are found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the metadata document.
		 * @param identifier
		 *            The identifier of the metadata document.
		 * @param filter
		 *            To return either the "oldest" or "newest" revision
		 * @return a Response, containing a newline-separated list of revision
		 *         values
		 */
		@GET @Path("/eml/{scope}/{identifier}") @Produces("text/plain")
		public Response listDataPackageRevisions (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") String
		identifierStr, @QueryParam("filter") @DefaultValue("") String filter){
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
			} catch (NumberFormatException e) {
				String message =
						"identifier value '" + identifierStr + "' must be a non-negative integer\n";
				throw new UserErrorException(message);
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			String revisionList = "";
			DataPackageManager dataPackageManager = new DataPackageManager();
			if (filter.isEmpty()) {
				revisionList = dataPackageManager.listDataPackageRevisions(scope, identifier);
			}
			else {
				if (filter.equals("oldest")) {
					Integer revisionInt = dataPackageManager.getOldestRevision(scope, identifier);
					if (revisionInt != null) {
						revisionList = revisionInt.toString();
					}
				}
				else if (filter.equals("newest")) {
					Integer revisionInt = dataPackageManager.getNewestRevision(scope, identifier);
					if (revisionInt != null) {
						revisionList = revisionInt.toString();
					}
				}
			}

			if (revisionList != null) {
				responseBuilder = Response.ok(revisionList.trim());
				response = responseBuilder.build();
			}
			else {
				String message = "An unknown error occurred";
				throw new Exception(message);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List Data Package Scopes</strong> operation, returning all scope
		 * values extant in the data package registry.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET https://pasta.lternet.edu/package/eml</code></td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>A list of all scope values extant in the data package
		 * registry</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 *
		 * <pre>
		 * knb-lter-lno
		 * knb-lter-xyz
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of the scope values</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No scope values are extant in the data package registry</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @return a Response, containing a newline separated list of scope values
		 */
		@GET @Path("/eml") @Produces("text/plain") public Response listDataPackageScopes
		(@Context HttpHeaders headers){
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
						"User " + userId + " is not authorized to execute service method " +
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
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List Deleted Data Packages</strong> operation, returning all
		 * document identifiers (excluding revision values) that have been deleted
		 * from the data package registry.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/eml/deleted</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>A list of all document identifiers deleted from the data
		 * package registry</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 *
		 * <pre>
		 * knb-lter-lno.1
		 * knb-lter-lno.2
		 * knb-lter-xyz.1
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of deleted data packages</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @return a Response, containing a newline separated list of document
		 *         identifiers.
		 */
		@GET @Path("/eml/deleted") @Produces("text/plain")
		public Response listDeletedDataPackages (@Context HttpHeaders headers){
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
						"User " + userId + " is not authorized to execute service method " +
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
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List Service Methods</strong> operation, returning a simple list
		 * of web service methods supported by the Data Package Manager web service.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/service-methods</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>A newline-separated list of revision values matching the
		 * specified scope and identifier values</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 *
		 * <pre>
		 * appendProvenance
		 * createDataPackage
		 * createDataPackageArchive
		 * .
		 * .
		 * . (truncated for brevity)
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal scope
		 * or identifier value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of the data package revisions</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package revisions associated with the specified
		 * scope and identifier are found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @return a Response, containing a newline-separated list of service
		 *         method names supported by the Data Package Manager
		 */
		@GET @Path("/service-methods") @Produces("text/plain")
		public Response listServiceMethods (@Context HttpHeaders headers){
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listServiceMethods";
		Rule.Permission permission = Rule.Permission.read;
		AuthToken authToken = null;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			String serviceMethods = "";
			StringBuffer stringBuffer = new StringBuffer();
			NodeList nodeList = null;
			String serviceDocumentStr = ConfigurationListener.getServiceDocument();
			Document document = XmlUtility.xmlStringToDoc(serviceDocumentStr);

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
									stringBuffer.append(String.format("%s\n", name));
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

			serviceMethods = stringBuffer.toString();
			responseBuilder = Response.ok(serviceMethods.trim());
			response = responseBuilder.build();
		} catch (IllegalArgumentException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Entity</strong> operation, specifying the scope,
		 * identifier, revision, and entity identifier of the data entity to be read
		 * in the URI.
		 *
		 * <p>
		 * Revision may be specified as "newest" or "oldest" to retrieve data from
		 * the newest or oldest revision, respectively.
		 * </p>
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/data/eml/knb-lter-lno/1/1/67e99349d1666e6f4955e9dda42c3cc2</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * httpd://pasta.lternet.edu/package/data/eml/knb-lter-lno/1/oldest/67e99349d1666e6f4955e9dda42c3cc2</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/data/eml/knb-lter-lno/1/newest/67e99349d1666e6f4955e9dda42c3cc2</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data entity was successful</td>
		 * <td align=center>The data that comprises the data entity</td>
		 * <td align=center><code>application/octet-stream</code></td>
		 * <td>
		 *
		 * <pre>
		 * Site Year Month Day Transect Species_Code Count
		 * 1 2000 8 26 1 G1 0
		 * 1 2000 8 26 2 G1 0
		 * 1 2000 8 26 3 G1 0
		 * .
		 * .
		 * .
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * entity</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>Either the specified data package or the specified data
		 * entity is not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @param entityId
		 *            The identifier of the data entity within the data package
		 * @return a File object containing the specified data entity, if found,
		 *         else returns a 404 Not Found response
		 */
		@GET @Path("/data/eml/{scope}/{identifier}/{revision}/{entityId}")
		public Response readDataEntity (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision, @PathParam("entityId") String
		entityId){
		ResponseBuilder responseBuilder = null;
		Response response = null;
		EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
		final String serviceMethodName = "readDataEntity";
		Rule.Permission permission = Rule.Permission.read;
		AuthToken authToken = null;
		String resourceId = null;
		String entryText = null;
		String robot = null;
		String userAgent = null;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();
			robot = getRobot(headers);
			userAgent = getUserAgent(headers);

			// Is user a bot?
			if ((userId != null) && (userId.equals("public")) && (robot != null)) {
				throw new UnauthorizedException(String.format(
						"Robots are not authorized access to data objects. Robot detected: %s",
						robot));
			}

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			/*
			 * Handle symbolic revisions such as "newest" and "oldest".
			 */
			if (revision != null) {
				if (revision.equals("newest")) {
					Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
					if (newest != null) {
						revision = newest.toString();
					}
				}
				else if (revision.equals("oldest")) {
					Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
					if (oldest != null) {
						revision = oldest.toString();
					}
				}
			}

			EmlPackageId emlPackageId =
					emlPackageIdFormat.parse(scope, identifier.toString(), revision);
			String packageId = emlPackageIdFormat.format(emlPackageId);

			/*
			 * Isolate the resourceId for the data entity so that its value can
			 * be recorded in the audit log
			 */
			Integer revisionInt = new Integer(revision);
			ArrayList<String> resources =
					dataPackageManager.getDataPackageResources(scope, identifier, revisionInt);
			if (resources != null && resources.size() > 0) {
				for (String resource : resources) {
					if (resource != null && resource.contains("/package/data/eml") &&
							resource.contains(entityId)) {
						resourceId = resource;
					}
				}
			}

			MediaType dataFormat =
					dataPackageManager.getDataEntityFormat(scope, identifier, revision, entityId);
			entryText = "Data Format: " + dataFormat.toString();

			File file =
					dataPackageManager.getDataEntityFile(scope, identifier, revision, entityId,
							authToken, userId);

			if (file != null && file.exists()) {

				Long size = FileUtils.sizeOf(file);

				String dataPackageResourceId =
						DataPackageManager.composeResourceId(ResourceType.dataPackage, scope,
								identifier, Integer.valueOf(revision), null);

				String entityResourceId =
						DataPackageManager.composeResourceId(ResourceType.data, scope, identifier,
								Integer.valueOf(revision), entityId);

				String entityName = dataPackageManager.readDataEntityName(dataPackageResourceId,
						entityResourceId, authToken);

				String xmlMetadata =
						dataPackageManager.readMetadata(scope, identifier, revision,
								authToken.getUserId(), authToken);

				String objectName = findObjectName(xmlMetadata, entityName);

				entryText = String.format("%s: %s; %s: %s; %s", "Entity Name", entityName,
						"Object Name", objectName, entryText);

				if (size < this.sizeThreshold) {
					responseBuilder = Response.ok(file, dataFormat);
					responseBuilder.header("Content-Length", size.toString());

					if (objectName != null) {
						responseBuilder.header("Content-Disposition",
								"attachment; filename=\"" + objectName + "\"");
					}
				}
				else {
					boolean createLink = true;
					responseBuilder = getRedirect(file, objectName, size, createLink);
				}

				response = responseBuilder.build();
			}
			else {
				ResourceNotFoundException e = new ResourceNotFoundException(
						"Unable to access data entity file for packageId: " + packageId.toString() +
								"; entityId: " + entityId);
				throw (e);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);
		cleanTemporaryDir();

		response = stampHeader(response);
		return response;

	}


		private ResponseBuilder getRedirect (File file, String filename,long size,
		boolean createLink)
			throws Exception {
		String dataToken = null;

		/*
		 * createLink will be true for data entities but false for archive zip files.
		 */
		if (createLink) {
			long nowTime = new Date().getTime();
			String md5Hex = DigestUtils.md5Hex(filename);
			dataToken = String.format("%d-%s", nowTime, md5Hex);
			createDataFileLink(file, dataToken);
		}
		else {
			dataToken = filename;  // for archives, use the transaction name
		}

		String encodedFilename = URLEncoder.encode(filename, "UTF-8");
		String locationStr =
				String.format("%s?dataToken=%s&size=%d&objectName=%s", this.dataServerContext,
						dataToken, size, encodedFilename);
		URI location = new URI(locationStr);
		ResponseBuilder responseBuilder = Response.temporaryRedirect(location);
		logger.warn("Redirecting to: " + locationStr);
		return responseBuilder;
	}


		private void cleanTemporaryDir () {
		Cleaner cleaner = new Cleaner(this.tmpDir, this.ttl);
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.execute(cleaner);
		executorService.shutdown();
	}


		/*
		 * Create a symbolic link to the file in the temporary data dir
		 */
		private void createDataFileLink (File entityFile, String dataToken)
			throws Exception {
		String msg = null;

		/*
		 * Create a link from the path of the data entity for
		 * this revision to the path of the data entity for the
		 * prior revision.
		 */
		String dataTokenPathStr = String.format("%s/%s", this.tmpDir, dataToken);
		FileSystem fileSystem = FileSystems.getDefault();

		String entityPathStr = entityFile.getAbsolutePath();
		java.nio.file.Path entityPath = fileSystem.getPath(entityPathStr);
		java.nio.file.Path dataTokenPath = fileSystem.getPath(dataTokenPathStr);
		String createLinkMsg =
				String.format("Creating link from %s to %s", dataTokenPathStr, entityPathStr);
		logger.warn(createLinkMsg);

		try {
			if (SystemUtils.IS_OS_WINDOWS) {
				Files.createLink(dataTokenPath, entityPath);
			}
			else {
				Files.createSymbolicLink(dataTokenPath, entityPath);
			}
		} catch (FileAlreadyExistsException e) {
			// this is okay, just issue a warning
			msg = String.format("Failed to create link from %s to %s: %s", dataTokenPathStr,
					entityPathStr, e.getMessage());
			logger.warn(msg);
		} catch (Exception e) {
			msg = String.format("Error creating link from %s to %s: %s", dataTokenPathStr,
					entityPathStr, e.getMessage());
			logger.error(msg);
			throw (e);
		}

	}


		/**
		 *
		 * <strong>Read Data Entity ACL</strong> operation, specifying the scope,
		 * identifier, and revision of the data entity object whose Access Control
		 * List (ACL) is to be read in the URI, returning an XML string representing
		 * the ACL for the data entity. Please note: only a very limited set of
		 * users are authorized to use this service method.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/data/acl/eml/knb-lter-lno/1/3/67e99349d1666e6f4955e9dda42c3cc2</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data entity ACL was successful</td>
		 * <td align=center>An XML string representing the access control list (ACL)
		 * for the data entity</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code><pre>
		 &lt;access authSystem="https://pasta.edirepository.org/authentication" order="allowFirst" system="https://pasta.edirepository.org"&gt;
		 &lt;allow&gt;
		 &lt;principal role="owner"&gt;uid=dcosta,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;permission&gt;changePermission&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;allow&gt;
		 &lt;principal&gt;uid=NIN,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;permission&gt;changePermission&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;allow&gt;
		 &lt;principal>public&lt;/principal&gt;
		 &lt;permission>read&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;/access&gt;
		 * </pre></code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * entity ACL</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No ACL associated with the specified data entity is
		 * found or the data entity itself was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @param entityId
		 *            The entity identifier
		 * @return a Response object containing a data entity access control list
		 *         XML string if found, else returns a 404 Not Found response
		 */
		@GET @Path("/data/acl/eml/{scope}/{identifier}/{revision}/{entityId}") @Produces(
				"application/xml") public Response readDataEntityAcl (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision, @PathParam("entityId") String
		entityId){
		AuthToken authToken = null;
		String acl = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataEntityAcl";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.data, scope, identifier,
							Integer.valueOf(revision), entityId);

			DataPackageManager dataPackageManager = new DataPackageManager();
			acl = dataPackageManager.readResourceAcl(resourceId);

			if (acl != null) {
				responseBuilder = Response.ok(acl);
				response = responseBuilder.build();
				entryText = acl;
			}
			else {
				Exception e =
						new Exception("Read data entity ACL operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>Read Data Entity Resource Metadata</strong> operation, specifying the scope,
		 * identifier, revision, and entity identifier of the data entity object whose resource metadata is to be read in
		 * the URI, returning an XML string representing the resource metadata for the data entity.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/data/rmd/eml/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data entity resource metadata was successful</td>
		 * <td align=center>An XML string representing the data entity resource metadata
		 * for the data entity</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code><pre>
		 &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 &lt;resourceMetadata&gt;
		 &lt;dataFormat&gt;text/csv&lt;/dataFormat&gt;
		 &lt;dateCreated&gt;2016-12-06 13:59:15.696&lt;/dateCreated&gt;
		 &lt;entityId&gt;67e99349d1666e6f4955e9dda42c3cc2&lt;/entityId&gt;
		 &lt;entityName&gt;DailyWaterSample-NIN-LTER-1978-1992&lt;/entityName&gt;
		 &lt;fileName&gt;LTER.NIN.DWS.csv&lt;/fileName&gt;
		 &lt;identifier&gt;1&lt;/identifier&gt;
		 &lt;md5Checksum&gt;699f0409577b9b8d194480310fed2dbb&lt;/md5Checksum&gt;
		 &lt;packageId&gt;knb-lter-nin.1.1&lt;/packageId&gt;
		 &lt;principalOwner&gt;uid=LNO,o=LTER,dc=ecoinformatics,dc=org&lt;/principalOwner&gt;
		 &lt;resourceId&gt;https://pasta.lternet.edu/package/data/eml/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2&lt;/resourceId&gt;
		 &lt;resourceLocation&gt;/home/pasta/local/data&lt;/resourceLocation&gt;
		 &lt;resourceSize&gt;924291&lt;/resourceSize&gt;
		 &lt;resourceType&gt;data&lt;/resourceType&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;scope&gt;knb-lter-nin&lt;/scope&gt;
		 &lt;sha1Checksum&gt;7a47318b5a6baec54e24c6b3b698e5b4fa207ac4&lt;/sha1Checksum&gt;
		 &lt;/resourceMetadata&gt;
		 * </pre></code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * entity resource metadata</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No resource metadata associated with the specified data entity is
		 * found or the data entity itself was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @param entityId
		 *            The entity identifier
		 * @return a Response object containing a data entity resource metadata
		 *         XML string if found, else returns a 404 Not Found response
		 */
		@GET @Path("/data/rmd/eml/{scope}/{identifier}/{revision}/{entityId}") @Produces(
				"application/xml") public Response readDataEntityRmd (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision, @PathParam("entityId") String
		entityId){
		AuthToken authToken = null;
		String resourceMetadata = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataEntityRmd";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.data, scope, identifier,
							Integer.valueOf(revision), entityId);

			DataPackageManager dataPackageManager = new DataPackageManager();
			resourceMetadata =
					dataPackageManager.readResourceMetadata(ResourceType.data, resourceId);

			if (resourceMetadata != null) {
				responseBuilder = Response.ok(resourceMetadata);
				response = responseBuilder.build();
				entryText = resourceMetadata;
			}
			else {
				Exception e =
						new Exception("Read Resource Metadata operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>Read Data Entity Checksum</strong> operation, specifying the
		 * scope, identifier, and revision of the data entity object whose checksum
		 * is to be read in the URI, returning a 40-character SHA-1 checksum value.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/data/checksum/eml/knb-lter-lno/1/3/67e99349d1666e6f4955e9dda42c3cc2</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data entity checksum was
		 * successful</td>
		 * <td align=center>The canonical Digital Object Identifier of the data
		 * entity.</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>7a39bd7694dc0473a6ae7a7d7520ff2e7a39bd76</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * entity</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No checksum associated with the specified data entity is
		 * found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @param entityId
		 *            The entity identifier
		 * @return a Response object containing a data entity checksum if found,
		 *         else returns a 404 Not Found response
		 */

		@GET @Path(
				"/data/checksum/eml/{scope}/{identifier}/{revision}/{entityId}") @Produces(
				"text/plain") public Response readDataEntityChecksum (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision, @PathParam("entityId") String
		entityId){
		AuthToken authToken = null;
		String checksum = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataEntityChecksum";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.data, scope, identifier,
							Integer.valueOf(revision), entityId);

			DataPackageManager dataPackageManager = new DataPackageManager();
			checksum = dataPackageManager.readResourceChecksum(resourceId, authToken);

			if (checksum != null) {
				responseBuilder = Response.ok(checksum);
				response = responseBuilder.build();
				entryText = checksum;
			}
			else {
				Exception e =
						new Exception("Read resource checksum operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>Read Data Entity Size</strong> operation, specifying the
		 * scope, identifier, revision, and entity ID of the data entity object whose size
		 * is to be read in the URI, returning an integer, the size value in bytes.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/data/size/eml/knb-lter-lno/1/3/67e99349d1666e6f4955e9dda42c3cc2</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data entity size was successful</td>
		 * <td align=center>The canonical Digital Object Identifier of the data
		 * entity.</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>7a39bd7694dc0473a6ae7a7d7520ff2e7a39bd76</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * entity</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No size value associated with the specified data entity is
		 * found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @param entityId
		 *            The entity identifier
		 * @return a Response object containing a data entity size value if found,
		 *         else returns a 404 Not Found response
		 */

		@GET @Path("/data/size/eml/{scope}/{identifier}/{revision}/{entityId}") @Produces(
				"text/plain") public Response readDataEntitySize (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision, @PathParam("entityId") String
		entityId){
		AuthToken authToken = null;
		Long entitySize = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataEntitySize";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.data, scope, identifier,
							Integer.valueOf(revision), entityId);

			DataPackageManager dataPackageManager = new DataPackageManager();
			entitySize = dataPackageManager.readResourceSize(resourceId, authToken);

			if (entitySize != null) {
				entryText = entitySize.toString();
				responseBuilder = Response.ok(entryText);
				response = responseBuilder.build();
			}
			else {
				Exception e =
						new Exception("Read resource size operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>Read Data Entity Sizes</strong> operation, specifying the
		 * scope, identifier, and revision of the data package whose entity sizes
		 * are to be read in the URI. Returns a newline-separated list of lines,
		 * where each line contains an entity id followed by a comma followed
		 * by the entity size of that entity.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td>
		 *   <code>curl -i -X GET https://pasta.lternet.edu/package/data/size/eml/knb-lter-nin/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data entity sizes was successful</td>
		 * <td align=center>a newline-separated list of lines,
		 * where each line contains an entity id followed by a comma followed
		 * by the entity size of that entity (in bytes)</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 *   <pre>
		 *     <code>7a39bd7694da0473a6ae7a7d7520ff2e7a39bd76,1254096</code>
		 *     <code>8a39bd7694db0473a6ae7a7d7520ff2e7a39bd77,37004</code>
		 *     <code>9a39bd7694dc0473a6ae7a7d7520ff2e7a39bd78,1098345788</code>
		 *   </pre>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * entity sizes</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No size values associated with the specified data entity are
		 * found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a list of data entity size values if found,
		 *         else returns a 404 Not Found response
		 */
		@GET @Path("/data/size/eml/{scope}/{identifier}/{revision}") @Produces("text/csv")
		public Response readDataEntitySizes (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") Integer revision){
		AuthToken authToken = null;
		String entitySizesCSV = null;
		String entryText = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataEntitySizes";
		Rule.Permission permission = Rule.Permission.read;
		String resourceId = null;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			resourceId = DataPackageManager.composeResourceId(ResourceType.dataPackage, scope,
					identifier, revision, null);
			entitySizesCSV =
					dataPackageManager.readEntitySizes(scope, identifier, revision, authToken);

			if (entitySizesCSV != null) {
				entryText = entitySizesCSV;
				responseBuilder = Response.ok(entryText);
				response = responseBuilder.build();
			}
			else {
				Exception e = new Exception(
						"Read data entity sizes operation failed for an unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Entity Name</strong> operation, specifying the scope,
		 * identifier, revision, and entity identifier of the data entity whose name
		 * is to be read in the URI.
		 *
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/name/eml/knb-lter-lno/1/3/67e99349d1666e6f4955e9dda42c3cc2</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package entity name was
		 * successful</td>
		 * <td align=center>The entity name value of the data package entity.</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>Daily Average Moored CTD and ADCP Data</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package entity name</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No entity associated with the specified data package
		 * entity identifier is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @param entityId
		 *            The identifier of the data entity within the data package
		 * @return a Response object containing a data entity name if found, else
		 *         returns a 404 Not Found response
		 */
		@GET @Path("/name/eml/{scope}/{identifier}/{revision}/{entityId}") @Produces(
				"text/plain") public Response readDataEntityName (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision, @PathParam("entityId") String
		entityId){
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
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			dataPackageResourceId =
					DataPackageManager.composeResourceId(ResourceType.dataPackage, scope,
							identifier, Integer.valueOf(revision), null);

			entityResourceId =
					DataPackageManager.composeResourceId(ResourceType.data, scope, identifier,
							Integer.valueOf(revision), entityId);

			DataPackageManager dataPackageManager = new DataPackageManager();
			entityName =
					dataPackageManager.readDataEntityName(dataPackageResourceId, entityResourceId,
							authToken);

			if (entityName != null) {
				responseBuilder = Response.ok(entityName);
				response = responseBuilder.build();
				entryText = entityName;
			}
			else {
				Exception e =
						new Exception("Read Data Entity Name operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, entityResourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>Read Data Entity Names</strong> operation, specifying the
		 * scope, identifier, and revision of the data package whose entity names
		 * are to be read in the URI. Returns a newline-separated list of lines,
		 * where each line contains an entity id followed by a comma followed
		 * by the entity name of that entity.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td>
		 *   <code>curl -i -X GET https://pasta.lternet.edu/package/name/eml/knb-lter-nin/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data entity names was successful</td>
		 * <td align=center>a newline-separated list of lines,
		 * where each line contains an entity id followed by a comma followed
		 * by the entity name of that entity</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 *   <pre>
		 *     <code>7a39bd7694da0473a6ae7a7d7520ff2e7a39bd76,First Entity Name</code>
		 *     <code>8a39bd7694db0473a6ae7a7d7520ff2e7a39bd77,Second Entity Name</code>
		 *     <code>9a39bd7694dc0473a6ae7a7d7520ff2e7a39bd78,Third Entity Name</code>
		 *   </pre>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * entity names</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No entity name values associated with the specified data entity are
		 * found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a list of data entity id and name values if found,
		 *         else returns a 404 Not Found response
		 */
		@GET @Path("/name/eml/{scope}/{identifier}/{revision}") @Produces("text/csv")
		public Response readDataEntityNames (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") Integer revision){
		AuthToken authToken = null;
		String entityNamesCSV = null;
		String entryText = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataEntityNames";
		Rule.Permission permission = Rule.Permission.read;
		String resourceId = null;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			resourceId = DataPackageManager.composeResourceId(ResourceType.dataPackage, scope,
					identifier, revision, null);
			entityNamesCSV =
					dataPackageManager.readDataEntityNames(scope, identifier, revision,
							authToken);

			if (entityNamesCSV != null) {
				entryText = entityNamesCSV;
				responseBuilder = Response.ok(entryText);
				response = responseBuilder.build();
			}
			else {
				Exception e = new Exception(
						"Read data entity names operation failed for an unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Package</strong> operation, specifying the scope,
		 * identifier, and revision of the data package to be read in the URI,
		 * returning a resource graph with reference URLs to each of the metadata,
		 * data, and quality report resources that comprise the data package.
		 *
		 * <p>
		 * Revision may be specified as "newest" or "oldest" to retrieve the newest
		 * or oldest revision, respectively.
		 * </p>
		 *
		 * <p>
		 * When the "?ore" query parameter is appended to the request URL, an OAI-ORE
		 * compliant resource map in RDF-XML format is returned.
		 * </p>
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/eml/knb-lter-lno/1/oldest</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/eml/knb-lter-lno/1/newest</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package was successful</td>
		 * <td align=center>A resource map with reference URLs to each of the
		 * metadata, data, and quality report resources that comprise the data
		 * package.</td>
		 * <td align=center><code>'text/plain'</code></td>
		 * <td>
		 *
		 * <pre>
		 * https://pasta.lternet.edu/package/data/eml/knb-lter-lno/1/1/67e99349d1666e6f4955e9dda42c3cc2
		 * https://pasta.lternet.edu/package/metadata/eml/knb-lter-lno/1/1
		 * https://pasta.lternet.edu/package/report/eml/knb-lter-lno/1/1
		 * https://pasta.lternet.edu/package/eml/knb-lter-lno/1/1
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package associated with the specified packageId
		 * is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a data package resource graph if
		 *         found, else returns a 404 Not Found response
		 */
		@GET @Path("/eml/{scope}/{identifier}/{revision}") @Produces(
				{"application/rdf+xml", "text/plain"}) public Response readDataPackage
		(@Context HttpHeaders headers, @PathParam("scope") String
		scope, @PathParam("identifier") Integer identifier, @PathParam("revision") String
		revision, @QueryParam("ore") String oreParam){
		AuthToken authToken = null;
		String resourceMap = null;
		String entryText = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataPackage";
		Rule.Permission permission = Rule.Permission.read;
		String robot = null;
		String userAgent = null;
		boolean oreFormat = (oreParam != null);

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();
			robot = getRobot(headers);
			userAgent = getUserAgent(headers);

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			resourceMap =
					dataPackageManager.readDataPackage(scope, identifier, revision, authToken,
							userId, oreFormat);

			if (resourceMap != null) {
				String mediaType = oreFormat ? "application/rdf+xml" : MediaType.TEXT_PLAIN;
				responseBuilder = Response.ok(resourceMap, mediaType);
				response = responseBuilder.build();
			}
			else {
				Exception e =
						new Exception("Data package create operation failed for unknown reason");
				throw (e);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		String resourceId = resourceIdFromResourceMap(resourceMap);
		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);

		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>Read Data Package ACL</strong> operation, specifying the scope,
		 * identifier, and revision of the data package whose Access Control List
		 * (ACL) is to be read in the URI, returning an XML string representing the
		 * ACL for the data package. Please note: only a very limited set of users
		 * are authorized to use this service method.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/acl/eml/knb-lter-lno/1/3</code></td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package ACL was successful</td>
		 * <td align=center>An XML string representing the access control list (ACL)
		 * for the data package</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code><pre>
		 &lt;access authSystem="https://pasta.edirepository.org/authentication" order="allowFirst" system="https://pasta.edirepository.org"&gt;
		 &lt;allow&gt;
		 &lt;principal role="owner"&gt;uid=dcosta,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;permission&gt;changePermission&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;allow&gt;
		 &lt;principal&gt;uid=NIN,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;permission&gt;changePermission&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;allow&gt;
		 &lt;principal>public&lt;/principal&gt;
		 &lt;permission>read&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;/access&gt;
		 * </pre></code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package ACL</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No ACL associated with the specified data package is
		 * found or the data package itself was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a data entity access control list
		 *         XML string if found, else returns a 404 Not Found response
		 */
		@GET @Path("/acl/eml/{scope}/{identifier}/{revision}") @Produces("application/xml")
		public Response readDataPackageAcl (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		AuthToken authToken = null;
		String acl = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataPackageAcl";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId = DataPackageManager.composeResourceId(ResourceType.dataPackage, scope,
					identifier, Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			acl = dataPackageManager.readResourceAcl(resourceId);

			if (acl != null) {
				responseBuilder = Response.ok(acl);
				response = responseBuilder.build();
				entryText = acl;
			}
			else {
				Exception e =
						new Exception("Read data package ACL operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>Read Data Package Resource Metadata</strong> operation, specifying the scope,
		 * identifier, and revision of the data package whose resource metadata
		 * is to be read in the URI, returning an XML string representing the
		 * resource metadata for the data package resource.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/rmd/eml/knb-lter-nin/1/1</code></td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package resource metadata was successful</td>
		 * <td align=center>An XML string representing the resource metadata for the data package resource</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code><pre>
		 &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 &lt;resourceMetadata&gt;
		 &lt;dateCreated&gt;2016-12-06 13:59:17.257&lt;/dateCreated&gt;
		 &lt;doi&gt;doi:10.5072/FK2/2a97391f89ea8519e09a6365fe993da7&lt;/doi&gt;
		 &lt;identifier&gt;1&lt;/identifier&gt;
		 &lt;packageId&gt;knb-lter-nin.1.1&lt;/packageId&gt;
		 &lt;principalOwner&gt;uid=LNO,o=LTER,dc=ecoinformatics,dc=org&lt;/principalOwner&gt;
		 &lt;resourceId&gt;https://pasta.lternet.edu/package/eml/knb-lter-nin/1/1&lt;/resourceId&gt;
		 &lt;resourceType&gt;dataPackage&lt;/resourceType&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;scope&gt;knb-lter-nin&lt;/scope&gt;
		 &lt;/resourceMetadata&gt;
		 * </pre></code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package resource metadata</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No resource metadata associated with the specified data package is
		 * found or the data package itself was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a data entity access control list
		 *         XML string if found, else returns a 404 Not Found response
		 */
		@GET @Path("/rmd/eml/{scope}/{identifier}/{revision}") @Produces("application/xml")
		public Response readDataPackageRmd (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		AuthToken authToken = null;
		String resourceMetadata = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataPackageRmd";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId = DataPackageManager.composeResourceId(ResourceType.dataPackage, scope,
					identifier, Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			resourceMetadata =
					dataPackageManager.readResourceMetadata(ResourceType.dataPackage, resourceId);

			if (resourceMetadata != null) {
				responseBuilder = Response.ok(resourceMetadata);
				response = responseBuilder.build();
				entryText = resourceMetadata;
			}
			else {
				Exception e = new Exception(
						"Read data package resource metadata operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Package Archive</strong> operation, specifying the
		 * <em>transaction identifier</em> of the data package archive to be read in
		 * the URI, returning the data package archive as a binary object in the ZIP
		 * file format. Note that the user id of the request must be the same as the
		 * original requestor.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -s -X GET https://pasta.lternet.edu/package/archive/eml/knb-lter-nin/1/1/archive_knb-lter-nin.1.1_15494687022457218 > knb-lter-nin.1.1.zip</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package archive was
		 * successful</td>
		 * <td align=center>The data package ZIP archive as a binary stream</td>
		 * <td align=center><code>application/octet-stream</code></td>
		 * <td align=center>...binary stream...</td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package archive</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No archive associated with the specified
		 * <em>transaction identifier</em> is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @param transaction
		 *            The transaction of the data package error
		 * @return a Response object containing a data package error if found, else
		 *         returns a 404 Not Found response
		 */
		@GET @Path("/archive/eml/{scope}/{identifier}/{revision}/{transaction}")
		public Response readDataPackageArchive (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") Integer
		revision, @PathParam("transaction") String transaction){

		AuthToken authToken = null;
		String entryText = null;
		String resourceId =
				DataPackageManager.composeResourceId(ResourceType.archive, scope, identifier,
						revision, null);
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataPackageArchive";
		Rule.Permission permission = Rule.Permission.read;

		authToken = getAuthToken(headers);
		String userId = authToken.getUserId();
		String robot = getRobot(headers);
		String userAgent = getUserAgent(headers);


		// Is user authorized to run the service method?
		boolean serviceMethodAuthorized =
				isServiceMethodAuthorized(serviceMethodName, permission, authToken);
		if (!serviceMethodAuthorized) {
			throw new UnauthorizedException(
					"User " + userId + " is not authorized to execute service method " +
							serviceMethodName);
		}

		String packageId =
				String.format("%s.%s.%s", scope, identifier.toString(), revision.toString());

		try {

			DataPackageManager dataPackageManager = new DataPackageManager();
			File file =
					dataPackageManager.getDataPackageArchiveFile(transaction, packageId, userId);
			String filename = String.format("%s.zip", packageId);

			if (file != null && file.exists()) {
				Long size = FileUtils.sizeOf(file);

				if (size < this.sizeThreshold) {
					responseBuilder = Response.ok(file, "application/zip");
					responseBuilder.header("Content-Disposition",
							"attachment; filename=\"" + filename + "\"");
					responseBuilder.header("Content-Length", size.toString());
				}
				else {
					boolean createLink = false;
					responseBuilder = getRedirect(file, filename, size, createLink);
				}

				response = responseBuilder.build();
			}
			else {
				String gripe = "Unable to access data package archive " + transaction +
						".zip for transaction: " + transaction;
				ResourceNotFoundException e = new ResourceNotFoundException(gripe);
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);
		cleanTemporaryDir();

		response = stampHeader(response);
		return response;

	}


		/**
		 * <strong>Read Data Package DOI</strong> operation, specifying the scope,
		 * identifier, and revision of the data package DOI to be read in the URI,
		 * returning the canonical Digital Object Identifier.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/doi/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package DOI was successful</td>
		 * <td align=center>The canonical Digital Object Identifier of the data
		 * package.</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>doi:10.6073/pasta/7a39bd7694dc0473a6ae7a7d7520ff2e</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No DOI associated with the specified data package is
		 * found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a data package DOI if found, else
		 *         returns a 404 Not Found response
		 */
		@GET @Path("/doi/eml/{scope}/{identifier}/{revision}") @Produces("text/plain")
		public Response readDataPackageDoi (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
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
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId = DataPackageManager.composeResourceId(ResourceType.dataPackage, scope,
					identifier, Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			doi = dataPackageManager.readResourceDoi(resourceId, authToken);

			if (doi != null) {
				responseBuilder = Response.ok(doi);
				response = responseBuilder.build();
				entryText = doi;
			}
			else {
				Exception e =
						new Exception("Read resource DOI operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Package Error</strong> operation, specifying the scope,
		 * identifier, revision, and transaction id of the data package error to be
		 * read in the URI, returning the error message as plain text.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/error/eml/1364521882823</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package error was
		 * successful</td>
		 * <td align=center>The error message of the data package.</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 * <code>Attempting to update a data package to revision '1' but an equal or
		 * higher revision ('1') already exists in PASTA: knb-lter-lno.1.1.</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No error associated with the specified data package is
		 * found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @param transaction
		 *            The transaction of the data package error
		 * @return a Response object containing a data package error if found, else
		 *         returns a 404 Not Found response
		 */
		@GET @Path("/error/eml/{transaction}") @Produces("text/plain")
		public Response readDataPackageError (@Context HttpHeaders
		headers, @PathParam("transaction") String transaction){
		AuthToken authToken = null;
		String entryText = null;
		String resourceId = transaction + ".txt";
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataPackageError";
		Rule.Permission permission = Rule.Permission.read;

		authToken = getAuthToken(headers);
		String userId = authToken.getUserId();

		// Is user authorized to run the service method?
		boolean serviceMethodAuthorized =
				isServiceMethodAuthorized(serviceMethodName, permission, authToken);
		if (!serviceMethodAuthorized) {
			throw new UnauthorizedException(
					"User " + userId + " is not authorized to execute service method " +
							serviceMethodName);
		}

		try {
			DataPackageManager dpm = new DataPackageManager();
			entryText = dpm.readDataPackageError(transaction);
			responseBuilder = Response.ok(entryText);
			response = responseBuilder.build();
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;

	}


		/**
		 * <strong>Read Data Package From DOI</strong> operation, specifying the
		 * DOI of the data package to be read in the URI, returning a resource graph
		 * with reference URLs to each of the metadata, data, and quality report
		 * resources that comprise the data package.
		 *
		 * <p>
		 * When the "ore" query parameter is appended to the request URL, an OAI-ORE
		 * compliant resource map in RDF-XML format is returned, for example:
		 * <pre>
		 * <code>https://pasta.lternet.edu/package/doi/doi:10.6073/pasta/0675d3602ff57f24838ca8d14d7f3961?ore</code>
		 * </pre>
		 * </p>
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/doi/doi:10.6073/pasta/0675d3602ff57f24838ca8d14d7f3961</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package was successful</td>
		 * <td align=center>A resource map with reference URLs to each of the
		 * metadata, data, and quality report resources that comprise the data
		 * package.</td>
		 * <td align=center><code>'text/plain'</code></td>
		 * <td>
		 *
		 * <pre>
		 * https://pasta.lternet.edu/package/data/eml/knb-lter-lno/1/1/67e99349d1666e6f4955e9dda42c3cc2
		 * https://pasta.lternet.edu/package/metadata/eml/knb-lter-lno/1/1
		 * https://pasta.lternet.edu/package/report/eml/knb-lter-lno/1/1
		 * https://pasta.lternet.edu/package/eml/knb-lter-lno/1/1
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as a missing 'doi' query parameter</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package associated with the specified DOI value
		 * is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param shoulder
		 *            The DOI shoulder, including the 'doi:' protocol,
		 *            for example: "doi:10.6073". If the "doi:" protocol is left
		 *            off, the code will prepend it as a convenience to the user.
		 * @param pasta
		 *            Ususally the literal string "pasta", but also be "FK2"
		 *            for test DOIs.
		 * @param md5
		 *            The portion of the PASTA DOI that equates to an MD5 value.
		 *            For example: "0675d3602ff57f24838ca8d14d7f3961"
		 * @return a Response object containing a data package resource graph if
		 *         found, else returns a 404 Not Found response
		 */
		@GET @Path("/doi/{shoulder}/{pasta}/{md5}") @Produces(
				{"application/rdf+xml", "text/plain"}) public Response readDataPackageFromDoi
		(@Context HttpHeaders headers, @PathParam("shoulder") String
		shoulder, @PathParam("pasta") String pasta, @PathParam("md5") String
		md5, @QueryParam("ore") String oreParam){
		String doi = null;
		AuthToken authToken = null;
		String resourceMap = null;
		String entryText = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataPackageFromDoi";
		Rule.Permission permission = Rule.Permission.read;
		String robot = null;
		String userAgent = null;
		boolean oreFormat = (oreParam != null);

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();
			robot = getRobot(headers);
			userAgent = getUserAgent(headers);

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			if (shoulder != null && !shoulder.isEmpty()) {
				if (!shoulder.startsWith("doi:")) {
					doi = String.format("doi:%s/%s/%s", shoulder, pasta, md5);
				}
				else {
					doi = String.format("%s/%s/%s", shoulder, pasta, md5);
				}
			}
			else {
				String msg = "Missing DOI shoulder value";
				throw new UserErrorException(msg);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			resourceMap =
					dataPackageManager.readDataPackageFromDoi(doi, authToken, userId, oreFormat);

			if (resourceMap != null) {
				String mediaType = oreFormat ? "application/rdf+xml" : MediaType.TEXT_PLAIN;
				responseBuilder = Response.ok(resourceMap, mediaType);
				response = responseBuilder.build();
			}
			else {
				String msg = String.format("No data package found for DOI %s", doi);
				throw new ResourceNotFoundException(msg);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		String resourceId = resourceIdFromResourceMap(resourceMap);
		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Package Report</strong> operation, specifying the
		 * scope, identifier, and revision of the data package quality report
		 * document to be read in the URI.
		 *
		 * <p>
		 * If an HTTP Accept header with value 'text/html' is included in the
		 * request, returns an HTML representation of the report. The default
		 * representation is XML.
		 * </p>
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><em>XML representation: </em><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/report/eml/knb-lter-lno/1/3</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><em>HTML representation: </em>
		 * <code>curl -i -X GET -H "Accept: text/html"
		 * https://pasta.lternet.edu/package/report/eml/knb-lter-lno/1/3</code></td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the quality report was successful</td>
		 * <td align=center>The quality report document that describes the data
		 * package</td>
		 * <td align=center><code>application/xml</code> or <code>text/html</code></td>
		 * <td>
		 *
		 * <pre>
		 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 * &lt;qualityReport&gt;
		 *   &lt;packageId&gt;knb-lter-lno.1.3&lt;/packageId&gt;
		 * .
		 * .
		 * .
		 * &lt;/qualityReport&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the
		 * specified data package</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package matching the specified scope,
		 * identifier, and revision values is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return A Response object containing the data package quality report
		 */
		@GET @Path("/report/eml/{scope}/{identifier}/{revision}") @Produces(
				{"application/xml", "text/html"}) public Response readDataPackageReport
		(@Context HttpHeaders headers, @PathParam("scope") String
		scope, @PathParam("identifier") Integer identifier, @PathParam("revision") String
		revision){
		AuthToken authToken = null;
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
		String robot = getRobot(headers);
		String userAgent = getUserAgent(headers);

		// Is user authorized to run the service method?
		boolean serviceMethodAuthorized =
				isServiceMethodAuthorized(serviceMethodName, permission, authToken);
		if (!serviceMethodAuthorized) {
			throw new UnauthorizedException(
					"User " + userId + " is not authorized to execute service method " +
							serviceMethodName);
		}

		try {
			DataPackageManager dataPackageManager = new DataPackageManager();
			EmlPackageId emlPackageId =
					emlPackageIdFormat.parse(scope, identifier.toString(), revision);
			String packageId = emlPackageIdFormat.format(emlPackageId);

			/*
			 * Isolate the resourceId for the report so that its value can be
			 * recorded in the audit log
			 */
			Integer revisionInt = new Integer(revision);
			ArrayList<String> resources =
					dataPackageManager.getDataPackageResources(scope, identifier, revisionInt);
			if (resources != null && resources.size() > 0) {
				for (String resource : resources) {
					if (resource != null && resource.contains("/package/report/eml")) {
						resourceId = resource;
					}
				}
			}

			File xmlFile =
					dataPackageManager.readDataPackageReport(scope, identifier, revision,
							emlPackageId, authToken, userId);

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
					} catch (IllegalStateException e) {
						entryText = e.getMessage();
						WebApplicationException webApplicationException =
								WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
										e.getMessage());
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
						"Unable to access data package quality report file for packageId: " +
								packageId);
				WebApplicationException webApplicationException =
						WebExceptionFactory.makeNotFound(e);
				entryText = e.getMessage();
				response = webApplicationException.getResponse();
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);
		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Package Report ACL</strong> operation, specifying the
		 * scope, identifier, and revision of the data package report whose access
		 * control list (ACL) is to be read in the URI, returning an XML string
		 * representing the ACL for the data package report resource. Please note:
		 * only a very limited set of users are authorized to use this service
		 * method.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/report/acl/eml/knb-lter-lno/1/3</code></td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package report ACL was
		 * successful</td>
		 * <td align=center>An XML string representing the access control list (ACL)
		 * for the data package report</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code><pre>
		 &lt;access authSystem="https://pasta.edirepository.org/authentication" order="allowFirst" system="https://pasta.edirepository.org"&gt;
		 &lt;allow&gt;
		 &lt;principal role="owner"&gt;uid=dcosta,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;permission&gt;changePermission&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;allow&gt;
		 &lt;principal&gt;uid=NIN,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;permission&gt;changePermission&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;allow&gt;
		 &lt;principal>public&lt;/principal&gt;
		 &lt;permission>read&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;/access&gt;
		 * </pre></code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package report ACL</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No ACL associated with the specified data package report
		 * is found or the data package report itself was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a data entity access control list
		 *         XML string if found, else returns a 404 Not Found response
		 */
		@GET @Path("/report/acl/eml/{scope}/{identifier}/{revision}") @Produces(
				"application/xml") public Response readDataPackageReportAcl
		(@Context HttpHeaders headers, @PathParam("scope") String
		scope, @PathParam("identifier") Integer identifier, @PathParam("revision") String
		revision){
		AuthToken authToken = null;
		String acl = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataPackageReportAcl";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.report, scope, identifier,
							Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			acl = dataPackageManager.readResourceAcl(resourceId);

			if (acl != null) {
				responseBuilder = Response.ok(acl);
				response = responseBuilder.build();
				entryText = acl;
			}
			else {
				Exception e = new Exception(
						"Read data package report ACL operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Package Report Resource Metadata</strong> operation, specifying the
		 * scope, identifier, and revision of the data package report whose resource metadata
		 * is to be read in the URI, returning an XML string
		 * representing the resource metadata for the data package report resource.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/report/rmd/eml/knb-lter-nin/1/1</code></td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the data package report resource metadata was
		 * successful</td>
		 * <td align=center>An XML string representing the resource metadata
		 * for the data package report resource</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code><pre>
		 &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 &lt;resourceMetadata&gt;
		 &lt;dateCreated&gt;2016-12-06 13:59:16.796&lt;/dateCreated&gt;
		 &lt;fileName&gt;quality_report.xml&lt;/fileName&gt;
		 &lt;identifier&gt;1&lt;/identifier&gt;
		 &lt;md5Checksum&gt;2a544e93585029114b85c83a8081cac8&lt;/md5Checksum&gt;
		 &lt;packageId&gt;knb-lter-nin.1.1&lt;/packageId&gt;
		 &lt;principalOwner&gt;uid=LNO,o=LTER,dc=ecoinformatics,dc=org&lt;/principalOwner&gt;
		 &lt;resourceId&gt;https://pasta.lternet.edu/package/report/eml/knb-lter-nin/1/1&lt;/resourceId&gt;
		 &lt;resourceType&gt;report&lt;/resourceType&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;scope&gt;knb-lter-nin&lt;/scope&gt;
		 &lt;sha1Checksum&gt;393c3b336579b3954392d5867d59abfd2c8101c8&lt;/sha1Checksum&gt;
		 &lt;/resourceMetadata&gt;
		 * </pre></code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package report resource metadata</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No resource metadata associated with the specified data package report resource
		 * is found or the data package report resource itself was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a data entity access control list
		 *         XML string if found, else returns a 404 Not Found response
		 */
		@GET @Path("/report/rmd/eml/{scope}/{identifier}/{revision}") @Produces(
				"application/xml") public Response readDataPackageReportRmd
		(@Context HttpHeaders headers, @PathParam("scope") String
		scope, @PathParam("identifier") Integer identifier, @PathParam("revision") String
		revision){
		AuthToken authToken = null;
		String resourceMetadata = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataPackageReportRmd";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.report, scope, identifier,
							Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			resourceMetadata =
					dataPackageManager.readResourceMetadata(ResourceType.report, resourceId);

			if (resourceMetadata != null) {
				responseBuilder = Response.ok(resourceMetadata);
				response = responseBuilder.build();
				entryText = resourceMetadata;
			}
			else {
				Exception e = new Exception(
						"Read data package report resource metadata operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Package Report Checksum</strong> operation, specifying
		 * the scope, identifier, and revision of the data package report object
		 * whose checksum is to be read in the URI, returning a 40 character SHA-1
		 * checksum value.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/report/checksum/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the report checksum was successful</td>
		 * <td align=center>The canonical Digital Object Identifier of the report.</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>7a39bd76947520ff2edc0473a6ae7a7d7520ff2e</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the report
		 * </td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No checksum associated with the specified report is
		 * found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a report checksum if found, else
		 *         returns a 404 Not Found response
		 */
		@GET @Path("/report/checksum/eml/{scope}/{identifier}/{revision}") @Produces(
				"text/plain") public Response readDataPackageReportChecksum
		(@Context HttpHeaders headers, @PathParam("scope") String
		scope, @PathParam("identifier") Integer identifier, @PathParam("revision") String
		revision){
		AuthToken authToken = null;
		String checksum = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readDataPackageReportChecksum";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.report, scope, identifier,
							Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			checksum = dataPackageManager.readResourceChecksum(resourceId, authToken);

			if (checksum != null) {
				responseBuilder = Response.ok(checksum);
				response = responseBuilder.build();
				entryText = checksum;
			}
			else {
				Exception e =
						new Exception("Read resource checksum operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Data Package Report DOI</strong> operation, specifying the
		 * scope, identifier, and revision of the data package report DOI to be read
		 * in the URI, returning the canonical Digital Object Identifier.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/report/doi/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the report DOI was successful</td>
		 * <td align=center>The canonical Digital Object Identifier of the report.</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>doi:10.6073/pasta/7a39bd7694dc0473a6ae7a7d7520ff2e</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the report
		 * </td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No DOI associated with the specified report is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a report DOI if found, else returns
		 *         a 404 Not Found response
		 *
		 @GET
		 @Path("/report/doi/eml/{scope}/{identifier}/{revision}")
		 @Produces("text/plain") public Response readDataPackageReportDoi(@Context HttpHeaders headers,
		 @PathParam("scope") String scope,
		 @PathParam("identifier") Integer identifier,
		 @PathParam("revision") String revision) {
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
		 boolean serviceMethodAuthorized = isServiceMethodAuthorized(
		 serviceMethodName, permission, authToken);
		 if (!serviceMethodAuthorized) {
		 throw new UnauthorizedException("User " + userId
		 + " is not authorized to execute service method "
		 + serviceMethodName);
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
		 .make(Response.Status.INTERNAL_SERVER_ERROR, e,
		 e.getMessage());
		 response = webApplicationException.getResponse();
		 }

		 audit(serviceMethodName, authToken, response, resourceId, entryText);

		 response = stampHeader(response);
		 return response;
		 }
		 */


		/**
		 * <strong>Read Evaluate Report</strong> operation, specifying the
		 * <em>transaction identifier</em> of the evaluate quality report document
		 * to be read in the URI.
		 *
		 * <p>
		 * If an HTTP Accept header with value 'text/html' is included in the
		 * request, returns an HTML representation of the report. The default
		 * representation is XML.
		 * </p>
		 *
		 * <p>
		 * See the <code>Evaluate Data Package</code> service method for information
		 * about how to obtain the transaction id.
		 * </p>
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><em>XML representation: </em><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/evaluate/report/eml/1364424858431</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><em>HTML representation: </em>
		 * <code>curl -i -H "Accept: text/html" -X GET
		 * https://pasta.lternet.edu/package/evaluate/report/eml/31364424858431</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the quality report was successful</td>
		 * <td align=center>The quality report document that describes the data
		 * package</td>
		 * <td align=center><code>application/xml</code> or <code>text/html</code></td>
		 * <td>
		 *
		 * <pre>
		 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 * &lt;qualityReport&gt;
		 *   &lt;packageId&gt;knb-lter-lno.1.1&lt;/packageId&gt;
		 * .
		 * .
		 * .
		 * &lt;/qualityReport&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the
		 * specified data package</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package matching the specified scope,
		 * identifier, and revision values is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @param transaction
		 *            The transaction identifier, e.g. "1364424858431"
		 * @return A Response object containing the evaluate quality report
		 */
		@GET @Path("/evaluate/report/eml/{transaction}") @Produces(
				{"application/xml", "text/html"}) public Response readEvaluateReport
		(@Context HttpHeaders headers, @PathParam("transaction") String transaction){
		AuthToken authToken = null;
		boolean produceHTML = false;
		final String serviceMethodName = "readEvaluateReport";
		Rule.Permission permission = Rule.Permission.read;
		String resourceId = "";
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
		String robot = getRobot(headers);
		String userAgent = getUserAgent(headers);

		// Is user authorized to run the service method?
		boolean serviceMethodAuthorized =
				isServiceMethodAuthorized(serviceMethodName, permission, authToken);
		if (!serviceMethodAuthorized) {
			throw new UnauthorizedException(
					"User " + userId + " is not authorized to execute service method " +
							serviceMethodName);
		}

		try {
			DataPackageManager dataPackageManager = new DataPackageManager();

			File xmlFile = dataPackageManager.readEvaluateReport(transaction);

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
					} catch (IllegalStateException e) {
						entryText = e.getMessage();
						WebApplicationException webApplicationException =
								WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
										e.getMessage());
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
				ResourceNotFoundException e = new ResourceNotFoundException(String.format(
						"Unable to access data package evaluate report file for transaction id: %s ",
						transaction));
				WebApplicationException webApplicationException =
						WebExceptionFactory.makeNotFound(e);
				entryText = e.getMessage();
				response = webApplicationException.getResponse();
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);
		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Metadata</strong> operation, specifying the scope,
		 * identifier, and revision of the EML document to be read in the URI.
		 *
		 * <p>
		 * Revision may be specified as "newest" or "oldest" to retrieve the newest
		 * or oldest revision, respectively.
		 * </p>
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/metadata/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/metadata/eml/knb-lter-lno/1/newest</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/metadata/eml/knb-lter-lno/1/oldest</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the EML document was successful</td>
		 * <td align=center>EML document</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 *
		 * <pre>
		 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 * &lt;eml:eml packageId="knb-lter-nin.1.1" scope="system"
		 *   system="https://pasta.lternet.edu"
		 *   xmlns:eml="eml://ecoinformatics.org/eml-2.1.0"
		 *   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 *   xsi:schemaLocation="eml://ecoinformatics.org/eml-2.1.0
		 *   http://nis.lternet.edu/schemas/eml/eml-2.1.0/eml.xsd"&gt;
		 * .
		 * .
		 * .
		 * &lt;/eml:eml&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package metadata document</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package associated with the specified packageId
		 * is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing the XML metadata document in its
		 *         message body
		 */
		@GET @Path("/metadata/eml/{scope}/{identifier}/{revision}") @Produces(
				"application/xml") public Response readMetadata (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		AuthToken authToken = null;
		String metadataString = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
		final String serviceMethodName = "readMetadata";
		Rule.Permission permission = Rule.Permission.read;
		String resourceId = null;
		String entryText = null;
		String robot = null;
		String userAgent = null;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();
			robot = getRobot(headers);
			userAgent = getUserAgent(headers);

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			/*
			 * Handle symbolic revisions such as "newest" and "oldest".
			 */
			if (revision != null) {
				if (revision.equals("newest")) {
					Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
					if (newest != null) {
						revision = newest.toString();
					}
				}
				else if (revision.equals("oldest")) {
					Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
					if (oldest != null) {
						revision = oldest.toString();
					}
				}
			}

			EmlPackageId emlPackageId =
					emlPackageIdFormat.parse(scope, identifier.toString(), revision);
			String packageId = emlPackageIdFormat.format(emlPackageId);

			/*
			 * Isolate the resourceId for the metadata resource so that its
			 * value can be recorded in the audit log
			 */
			Integer revisionInt = new Integer(revision);
			ArrayList<String> resources =
					dataPackageManager.getDataPackageResources(scope, identifier, revisionInt);
			if (resources != null && resources.size() > 0) {
				for (String resource : resources) {
					if (resource != null && resource.contains("/package/metadata/eml")) {
						resourceId = resource;
					}
				}
			}

			metadataString =
					dataPackageManager.readMetadata(scope, identifier, revision, userId,
							authToken);

			if (metadataString != null) {
				byte[] byteArray = metadataString.getBytes("UTF-8");
				responseBuilder = Response.ok();
				responseBuilder.header("Content-Length", byteArray.length);
				responseBuilder.entity(metadataString);
				response = responseBuilder.build();
			}
			else {
				ResourceNotFoundException e = new ResourceNotFoundException(
						"Unable to access metadata for packageId: " + packageId.toString());
				throw (e);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeNotFound(e);
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);
		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Metadata Dublin Core</strong> operation, specifying the scope,
		 * identifier, and revision of the Dublin Core metadata document to be read in
		 * the URI.
		 *
		 * <p>
		 * Revision may be specified as "newest" or "oldest" to retrieve the newest
		 * or oldest revision, respectively.
		 * </p>
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/metadata/dc/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/metadata/dc/knb-lter-lno/1/newest</code>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/metadata/dc/knb-lter-lno/1/oldest</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the metadata document was successful</td>
		 * <td align=center>Dublin Core XML document</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 *
		 * <pre>
		 * &lt;oai_dc:dc
		 *     xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
		 *     xmlns:fn="http://www.w3.org/2005/xpath-functions"
		 *     xmlns:xs="http://www.w3.org/2001/XMLSchema"
		 *     xmlns:fo="http://www.w3.org/1999/XSL/Format"
		 *     xmlns:eml="eml://ecoinformatics.org/eml-2.1.1"
		 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 *     xmlns:dc="http://purl.org/dc/elements/1.1/"
		 *     xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd"&gt;
		 *     &lt;dc:title&gt;Stormwater runoff...&lt;/dc:title&gt;
		 *     .
		 *     .
		 *     .
		 * &lt;/oai_dc:dc&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the data
		 * package metadata document</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package associated with the specified packageId
		 * is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing the Dublin Core XML metadata document in its
		 *         message body
		 */
		@GET @Path("/metadata/dc/{scope}/{identifier}/{revision}") @Produces(
				"application/xml") public Response readMetadataDublinCore (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		AuthToken authToken = null;
		String metadataString = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
		final String serviceMethodName = "readMetadata";
		Rule.Permission permission = Rule.Permission.read;
		String resourceId = null;
		String entryText = null;
		String robot = null;
		String userAgent = getUserAgent(headers);

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();
			robot = getRobot(headers);

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			/*
			 * Handle symbolic revisions such as "newest" and "oldest".
			 */
			if (revision != null) {
				if (revision.equals("newest")) {
					Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
					if (newest != null) {
						revision = newest.toString();
					}
				}
				else if (revision.equals("oldest")) {
					Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
					if (oldest != null) {
						revision = oldest.toString();
					}
				}
			}

			EmlPackageId emlPackageId =
					emlPackageIdFormat.parse(scope, identifier.toString(), revision);
			String packageId = emlPackageIdFormat.format(emlPackageId);

			/*
			 * Isolate the resourceId for the metadata resource so that its
			 * value can be recorded in the audit log
			 */
			Integer revisionInt = new Integer(revision);
			ArrayList<String> resources =
					dataPackageManager.getDataPackageResources(scope, identifier, revisionInt);
			if (resources != null && resources.size() > 0) {
				for (String resource : resources) {
					if (resource != null && resource.contains("/package/metadata/eml")) {
						resourceId = resource;
					}
				}
			}

			metadataString =
					dataPackageManager.readMetadataDublinCore(scope, identifier, revision, userId,
							authToken);

			if (metadataString != null) {
				byte[] byteArray = metadataString.getBytes("UTF-8");
				responseBuilder = Response.ok();
				responseBuilder.header("Content-Length", byteArray.length);
				responseBuilder.entity(metadataString);
				response = responseBuilder.build();
			}
			else {
				ResourceNotFoundException e = new ResourceNotFoundException(
						"Unable to access metadata for packageId: " + packageId.toString());
				throw (e);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeNotFound(e);
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);
		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Metadata ACL</strong> operation, specifying the scope,
		 * identifier, and revision of the data package metadata whose Access
		 * Control List (ACL) is to be read in the URI, returning an XML string
		 * representing the ACL for the data package metadata resource. Please note:
		 * only a very limited set of users are authorized to use this service
		 * method.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/metadata/acl/eml/knb-lter-lno/1/3</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the metadata ACL was successful</td>
		 * <td align=center>An XML string representing the access control list (ACL)
		 * for the metadata</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code><pre>
		 &lt;access authSystem="https://pasta.edirepository.org/authentication" order="allowFirst" system="https://pasta.edirepository.org"&gt;
		 &lt;allow&gt;
		 &lt;principal role="owner"&gt;uid=dcosta,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;permission&gt;changePermission&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;allow&gt;
		 &lt;principal&gt;uid=NIN,o=LTER,dc=ecoinformatics,dc=org&lt;/principal&gt;
		 &lt;permission&gt;changePermission&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;allow&gt;
		 &lt;principal>public&lt;/principal&gt;
		 &lt;permission>read&lt;/permission&gt;
		 &lt;/allow&gt;
		 &lt;/access&gt;
		 * </pre></code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the
		 * metadata ACL</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No ACL associated with the specified metadata is found
		 * or the metadata itself was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a data entity access control list
		 *         XML string if found, else returns a 404 Not Found response
		 */
		@GET @Path("/metadata/acl/eml/{scope}/{identifier}/{revision}") @Produces(
				"application/xml") public Response readMetadataAcl (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		AuthToken authToken = null;
		String acl = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readMetadataAcl";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.metadata, scope, identifier,
							Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			acl = dataPackageManager.readResourceAcl(resourceId);

			if (acl != null) {
				responseBuilder = Response.ok(acl);
				response = responseBuilder.build();
				entryText = acl;
			}
			else {
				Exception e =
						new Exception("Read metadata ACL operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Metadata Resource Metadata</strong> operation, specifying the scope,
		 * identifier, and revision of the data package metadata resource whose resource metadata
		 * is to be read in the URI, returning an XML string
		 * representing the resource metadata for the data package metadata resource.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td><code>curl -i -X GET
		 * https://pasta.lternet.edu/package/metadata/rmd/eml/knb-lter-nin/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the metadata resource metadata was successful</td>
		 * <td align=center>An XML string representing the resource metadata for the metadata resource</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td><code><pre>
		 &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 &lt;resourceMetadata&gt;
		 &lt;dateCreated&gt;2016-12-06 13:59:16.378&lt;/dateCreated&gt;
		 &lt;fileName&gt;Level-1-EML.xml&lt;/fileName&gt;
		 &lt;formatType&gt;eml://ecoinformatics.org/eml-2.1.0&lt;/formatType&gt;
		 &lt;identifier&gt;1&lt;/identifier&gt;
		 &lt;md5Checksum&gt;92f6674ace73e79d9de79871ecb36f4f&lt;/md5Checksum&gt;
		 &lt;packageId&gt;knb-lter-nin.1.1&lt;/packageId&gt;
		 &lt;principalOwner&gt;uid=LNO,o=LTER,dc=ecoinformatics,dc=org&lt;/principalOwner&gt;
		 &lt;resourceId&gt;https://pasta.lternet.edu/package/metadata/eml/knb-lter-nin/1/1&lt;/resourceId&gt;
		 &lt;resourceType&gt;metadata&lt;/resourceType&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;scope&gt;knb-lter-nin&lt;/scope&gt;
		 &lt;sha1Checksum&gt;406b48590973622b0681d123c8a8443d6d00d13c&lt;/sha1Checksum&gt;
		 &lt;/resourceMetadata&gt;
		 * </pre></code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the
		 * metadata resource metadata</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No resource metadata associated with the specified metadata resource is found
		 * or the metadata resource itself was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a data entity access control list
		 *         XML string if found, else returns a 404 Not Found response
		 */
		@GET @Path("/metadata/rmd/eml/{scope}/{identifier}/{revision}") @Produces(
				"application/xml") public Response readMetadataRmd (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		AuthToken authToken = null;
		String resourceMetadata = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readMetadataRmd";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.metadata, scope, identifier,
							Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			resourceMetadata =
					dataPackageManager.readResourceMetadata(ResourceType.metadata, resourceId);

			if (resourceMetadata != null) {
				responseBuilder = Response.ok(resourceMetadata);
				response = responseBuilder.build();
				entryText = resourceMetadata;
			}
			else {
				Exception e = new Exception(
						"Read metadata resource metadata operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Metadata Checksum</strong> operation, specifying the scope,
		 * identifier, and revision of the metadata object whose checksum value is
		 * to be read in the URI, returning a 40 character SHA-1 checksum value.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/metadata/checksum/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the metadata checksum was successful
		 * </td>
		 * <td align=center>The canonical Digital Object Identifier of the metadata</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>7a39bd7694dc7520ff2e0473a6ae7a7d7520ff2</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the
		 * metadata</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No checksum associated with the specified metadata is
		 * found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a metadata checksum if found, else
		 *         returns a 404 Not Found response
		 */
		@GET @Path("/metadata/checksum/eml/{scope}/{identifier}/{revision}") @Produces(
				"text/plain") public Response readMetadataChecksum (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		AuthToken authToken = null;
		String checksum = null;
		String entryText = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readMetadataChecksum";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.metadata, scope, identifier,
							Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			checksum = dataPackageManager.readResourceChecksum(resourceId, authToken);

			if (checksum != null) {
				responseBuilder = Response.ok(checksum);
				response = responseBuilder.build();
				entryText = checksum;
			}
			else {
				Exception e =
						new Exception("Read resource checksum operation failed for unknown reason");
				throw (e);
			}

		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Read Metadata DOI</strong> operation, specifying the scope,
		 * identifier, and revision of the metadata DOI to be read in the URI,
		 * returning the canonical Digital Object Identifier.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/metadata/doi/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the metadata DOI was successful</td>
		 * <td align=center>The canonical Digital Object Identifier of the metadata</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>doi:10.6073/pasta/7a39bd7694dc0473a6ae7a7d7520ff2e</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the
		 * metadata</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No DOI associated with the specified metadata is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a metadata DOI if found, else
		 *         returns a 404 Not Found response
		 *
		 @GET
		 @Path("/metadata/doi/eml/{scope}/{identifier}/{revision}")
		 @Produces("text/plain") public Response readMetadataDoi(@Context HttpHeaders headers,
		 @PathParam("scope") String scope,
		 @PathParam("identifier") Integer identifier,
		 @PathParam("revision") String revision) {
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
		 boolean serviceMethodAuthorized = isServiceMethodAuthorized(
		 serviceMethodName, permission, authToken);
		 if (!serviceMethodAuthorized) {
		 throw new UnauthorizedException("User " + userId
		 + " is not authorized to execute service method "
		 + serviceMethodName);
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
		 .make(Response.Status.INTERNAL_SERVER_ERROR, e,
		 e.getMessage());
		 response = webApplicationException.getResponse();
		 }

		 audit(serviceMethodName, authToken, response, resourceId, entryText);

		 response = stampHeader(response);
		 return response;
		 }
		 */

		/**
		 * <strong>Read Metadata Format</strong> operation, specifying the scope,
		 * identifier, and revision of the metadata to be read in the URI,
		 * returning the metadata format type, e.g. "eml://ecoinformatics.org/eml-2.1.1"
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/metadata/format/eml/knb-lter-lno/1/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The request to read the metadata format was successful</td>
		 * <td align=center>The canonical Digital Object Identifier of the metadata</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td><code>eml://ecoinformatics.org/eml-2.1.1</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to read the
		 * metadata</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>The specified metadata resource was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package
		 * @param revision
		 *            The revision of the data package
		 * @return a Response object containing a metadata format value if found,
		 *         else returns a 404 Not Found response
		 */
		@GET @Path("/metadata/format/eml/{scope}/{identifier}/{revision}") @Produces(
				"text/plain") public Response readMetadataFormat (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier, @PathParam("revision") String revision){
		AuthToken authToken = null;
		String entryText = null;
		String formatType = null;
		String resourceId = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "readMetadataFormat";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			resourceId =
					DataPackageManager.composeResourceId(ResourceType.metadata, scope, identifier,
							Integer.valueOf(revision), null);

			DataPackageManager dataPackageManager = new DataPackageManager();
			formatType = dataPackageManager.readResourceFormatType(resourceId, authToken);
			responseBuilder = Response.ok(formatType);
			response = responseBuilder.build();
			entryText = formatType;
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Search Data Packages</strong> operation, specifying the Solr
		 * query as query parameters in the URL.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>None</td>
		 * <td align=center></td>
		 * <td><code>curl -i -X GET https://pasta.lternet.edu/package/search/eml?q=LTER</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The search was successful</td>
		 * <td align=center>A "resultset" XML document containing the search results
		 * </td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 *
		 * <pre>
		 * &lt;?xml version="1.0"?&gt;
		 * &lt;resultset&gt;
		 * .
		 * .
		 * .
		 * &lt;/resultset&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request message body contains an error, such as an
		 * improperly formatted path query string</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to execute the
		 * Search Data Packages service method</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param pathQuery
		 *            A pathquery XML document, as specified in the payload of the
		 *            request.
		 *
		 * @return a Response, which if successful, contains a resultset XML
		 *         document
		 */
		@GET @Path("/search/eml") @Produces("application/xml")
		public Response searchDataPackages (@Context HttpHeaders headers, @Context UriInfo
		uriInfo){
		AuthToken authToken = null;
		String resourceId = null;
		String entryText = "";
		String resultsetXML = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "searchDataPackages";
		Rule.Permission permission = Rule.Permission.read;
		String robot = null;
		String userAgent = getUserAgent(headers);

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();
			robot = getRobot(headers);

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			try {
				URI uri = uriInfo.getRequestUri();
				if (uri != null) {
					entryText = uri.getQuery();
				}
			} catch (UnsupportedOperationException e) {
				/*
				 * DummyUriInfo doesn't support the getRequestUri() method but it's only
				 * used during testing so we don't really care
				 */
				;
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			resultsetXML = dataPackageManager.searchDataPackages(uriInfo, userId, authToken);

			if (resultsetXML != null) {
				responseBuilder = Response.ok(resultsetXML);
				response = responseBuilder.build();
			}
			else {
				ResourceNotFoundException e =
						new ResourceNotFoundException("No search results returned");
				entryText += "; " + e.getMessage();
				WebApplicationException webApplicationException =
						WebExceptionFactory.makeNotFound(e);
				response = webApplicationException.getResponse();
			}
		} catch (IllegalArgumentException e) {
			entryText += "; " + e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText += "; " + e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText += "; " + e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText += "; " + e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);
		response = stampHeader(response);
		return response;
	}

		@GET @Path("/search/download") @Produces("application/csv")
		public Response searchDownload (@Context HttpHeaders headers, @Context UriInfo
		uriInfo){

		AuthToken authToken = null;
		String resourceId = null;
		String entryText = "";
		String resultsetXML = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "searchDownload";
		Rule.Permission permission = Rule.Permission.read;
		String robot = null;
		String userAgent = getUserAgent(headers);

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();
			robot = getRobot(headers);

			// // Is user authorized to run the service method?
			// boolean serviceMethodAuthorized = isServiceMethodAuthorized(
			// 		serviceMethodName, permission, authToken);
			// if (!serviceMethodAuthorized) {
			// 	throw new UnauthorizedException("User " + userId
			// 			+ " is not authorized to execute service method "
			// 			+ serviceMethodName);
			// }

			try {
				URI uri = uriInfo.getRequestUri();
				if (uri != null) {
					entryText = uri.getQuery();
				}
			} catch (UnsupportedOperationException e) {
				/*
				 * DummyUriInfo doesn't support the getRequestUri() method but it's only
				 * used during testing so we don't really care
				 */
				;
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			resultsetXML = dataPackageManager.searchDataPackages(uriInfo, userId, authToken);

			String csv = XsltUtil.transformToText(resultsetXML, SEARCH_RESULT_XML_TO_CSV_NAME, null);

			if (csv != null) {
				responseBuilder = Response.ok(csv);
				response = responseBuilder.build();
			}
			else {
				ResourceNotFoundException e =
						new ResourceNotFoundException("No search results returned");
				entryText += "; " + e.getMessage();
				WebApplicationException webApplicationException =
						WebExceptionFactory.makeNotFound(e);
				response = webApplicationException.getResponse();
			}
		} catch (IllegalArgumentException e) {
			entryText += "; " + e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText += "; " + e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText += "; " + e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText += "; " + e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText, robot,
				userAgent);
		response = stampHeader(response);
		return response;
	}

		/**
		 * <strong>List Recent Uploads</strong> operation, optionally specifying the upload type ("insert" or "update") and a maximum limit as query parameters in the URL.
		 * (See example below.)
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>None</td>
		 * <td align=center></td>
		 * <td><code>curl -i -X GET "https://pasta.lternet.edu/package/uploads/eml?type=update&limit=5"
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The query was successful</td>
		 * <td align=center>An XML document containing the list of recent data package inserts or updates</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 * <pre>
		 &lt;dataPackageUploads&gt;
		 &lt;dataPackageUpload&gt;
		 &lt;packageId&gt;knb-lter-nwk.1504.1&lt;/packageId&gt;
		 &lt;scope&gt;knb-lter-nwk&lt;/scope&gt;
		 &lt;identifier&gt;1504&lt;/identifier&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;serviceMethod&gt;createDataPackage&lt;/serviceMethod&gt;
		 &lt;uploadDate&gt;2015-06-09&lt;/uploadDate&gt;
		 &lt;/dataPackageUpload&gt;
		 &lt;dataPackageUpload&gt;
		 &lt;packageId&gt;lter-landsat-ledaps.7.1&lt;/packageId&gt;
		 &lt;scope&gt;lter-landsat-ledaps&lt;/scope&gt;
		 &lt;identifier&gt;7&lt;/identifier&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;serviceMethod&gt;createDataPackage&lt;/serviceMethod&gt;
		 &lt;uploadDate&gt;2015-04-16&lt;/uploadDate&gt;
		 &lt;/dataPackageUpload&gt;
		 &lt;dataPackageUpload&gt;
		 &lt;packageId&gt;knb-lter-nwk.1490.1&lt;/packageId&gt;
		 &lt;scope&gt;knb-lter-nwk&lt;/scope&gt;
		 &lt;identifier&gt;1490&lt;/identifier&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;serviceMethod&gt;createDataPackage&lt;/serviceMethod&gt;
		 &lt;uploadDate&gt;2015-04-13&lt;/uploadDate&gt;
		 &lt;/dataPackageUpload&gt;
		 &lt;/dataPackageUploads&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request message body contains an error, such as an
		 * improperly formatted path query string</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to execute the
		 * Search Data Packages service method</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param type   optional query parameter to determine which type
		 *               of recent data package uploads to return;
		 *               recognized values are "type=insert" or "type=update".
		 * @param limit  optional query parameter used to limit the number
		 *               of recent data packages uploads returned in the list,
		 *               e.g. "limit=3".
		 *
		 * @return a Response, which if successful, contains a resultset XML
		 *         document
		 */
		@GET @Path("/uploads/eml") @Produces("application/xml")
		public Response listRecentUploads (@Context HttpHeaders headers, @Context UriInfo
		uriInfo){
		AuthToken authToken = null;
		String resourceId = null;
		String entryText = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listRecentUploads";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			QueryString queryStr = new QueryString(uriInfo);
			Map<String, List<String>> queryParams = queryStr.getParams();
			String type = "insert";
			String xml = "";
			int limit = DataPackageUploadManager.ARRAY_LIMIT;

			if (queryParams != null) {
				for (String key : queryParams.keySet()) {
					if (key.equalsIgnoreCase("limit")) {
						List<String> values = queryParams.get(key);
						String value = values.get(0);
						try {
							limit = Integer.parseInt(value);
						} catch (NumberFormatException e) {
							; // no action needed, just keep the default value
						}
					}
					else if (key.equals("type")) {
						List<String> values = queryParams.get(key);
						String typeParam = values.get(0);
						if ((typeParam != null) && (typeParam.startsWith("update"))) {
							type = "update";
						}
					}
				}
			}

			if (type.equals("insert")) {
				xml = DataPackageUploadManager.getRecentInserts(limit);
			}
			else {
				xml = DataPackageUploadManager.getRecentUpdates(limit);
			}

			responseBuilder = Response.ok(xml);
			response = responseBuilder.build();
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);
		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List Recent Changes</strong> operation, listing data package
		 * changes (insert, update, and delete operations) recorded in PASTA's resource
		 * registry.
		 *
		 * If a query string is omitted, all changes (inserts, updates, and
		 * deletes) to the resource registry will be returned. If query parameters
		 * are included, they are used to filter that set of changes based on their
		 * attributes.</p>
		 *
		 * <h4>Query parameters:</h4>
		 *
		 * <p>
		 * Query parameters are specified as <em>key=value</em> pairs, multiple
		 * pairs must be delimited with ampersands (&amp;), and only a single value
		 * should be specified for a particular key. The following query parameter
		 * keys are allowed:
		 * </p>
		 *
		 * <ul>
		 * <li><code>fromDate</code></li>
		 * <li><code>toDate</code></li>
		 * <li><code>scope</code></li>
		 * </ul>
		 *
		 * <p>
		 * If <code>fromDate</code> or <code>toDate</code> are specified, their values
		 * should comply with ISO 8601 datetime format. For example:
		 * <ul>
		 * <li><code>fromDate=2017-02-01T12:00:00</code></li>
		 * <li><code>toDate=2017-02-28</code></li>
		 * </ul>
		 * If <code>scope</code> is specified, its value must be one of the recognized
		 * scope values as configured in PASTA. For example:
		 * <ul>
		 * <li><code>scope=knb-lter-bes</code></li>
		 * </ul>
		 * </p>
		 *
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>None</td>
		 * <td align=center></td>
		 * <td><code>curl -i -X GET "https://pasta.lternet.edu/package/changes/eml?fromDate=2017-02-01T12:00:00&toDate=2017-02-28&scope=knb-lter-bes"
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The query was successful</td>
		 * <td align=center>An XML document containing the list of recent changes,
		 * including inserts (createDataPackage), updates (updateDataPackage), and
		 * deletes (deleteDataPackage).</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 * <pre>
		 &lt;dataPackageChanges&gt;
		 &lt;dataPackageUpload&gt;
		 &lt;packageId&gt;knb-lter-nwk.1210.1&lt;/packageId&gt;
		 &lt;scope&gt;knb-lter-nwk&lt;/scope&gt;
		 &lt;identifier&gt;1210&lt;/identifier&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;serviceMethod&gt;createDataPackage&lt;/serviceMethod&gt;
		 &lt;date&gt;2017-02-02 15:15:06.9&lt;/date&gt;
		 &lt;/dataPackageUpload&gt;
		 &lt;dataPackageDelete&gt;
		 &lt;packageId&gt;knb-lter-nwk.1210.1&lt;/packageId&gt;
		 &lt;scope&gt;knb-lter-nwk&lt;/scope&gt;
		 &lt;identifier&gt;1210&lt;/identifier&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;serviceMethod&gt;deleteDataPackage&lt;/serviceMethod&gt;
		 &lt;date&gt;2017-02-02 15:15:12.301&lt;/date&gt;
		 &lt;/dataPackageDelete&gt;
		 &lt;dataPackageUpload&gt;
		 &lt;packageId&gt;knb-lter-nwk.1211.1&lt;/packageId&gt;
		 &lt;scope&gt;knb-lter-nwk&lt;/scope&gt;
		 &lt;identifier&gt;1211&lt;/identifier&gt;
		 &lt;revision&gt;1&lt;/revision&gt;
		 &lt;serviceMethod&gt;createDataPackage&lt;/serviceMethod&gt;
		 &lt;date&gt;2017-02-02 15:15:58.331&lt;/date&gt;
		 &lt;/dataPackageUpload&gt;
		 &lt;dataPackageUpload&gt;
		 &lt;packageId&gt;knb-lter-nwk.1211.2&lt;/packageId&gt;
		 &lt;scope&gt;knb-lter-nwk&lt;/scope&gt;
		 &lt;identifier&gt;1211&lt;/identifier&gt;
		 &lt;revision&gt;2&lt;/revision&gt;
		 &lt;serviceMethod&gt;updateDataPackage&lt;/serviceMethod&gt;
		 &lt;date&gt;2017-02-02 15:16:28.374&lt;/date&gt;
		 &lt;/dataPackageUpload&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request message body contains an error, such as an
		 * improperly formatted path query string</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to execute the
		 * Search Data Packages service method</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            HTTP headers containing the requesting user's credentials.
		 * @param uriInfo
		 *            contains the query parameters used to match subscriptions.
		 * @return a Response, which if successful, contains an XML
		 *            document (see example above)
		 */
		@GET @Path("/changes/eml") @Produces("application/xml")
		public Response listRecentChanges (@Context HttpHeaders headers, @Context UriInfo
		uriInfo){
		AuthToken authToken = null;
		String resourceId = null;
		String entryText = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listRecentChanges";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			QueryString queryStr = new QueryString(uriInfo);
			Map<String, List<String>> queryParams = queryStr.getParams();
			String fromDate = null;
			String toDate = null;
			String scope = null;
			String xml = "";

			if (queryParams != null) {
				for (String key : queryParams.keySet()) {
					if (key.equalsIgnoreCase("fromDate")) {
						List<String> values = queryParams.get(key);
						String fromDateParam = values.get(0);
						if (fromDateParam != null) {
							if (fromDateParam.startsWith("1") || (fromDateParam.startsWith("2"))) {
								fromDate = fromDateParam;
							}
							else {
								throw new IllegalArgumentException(
										"Bad date parameter: " + fromDateParam);
							}
						}
					}
					else if (key.equalsIgnoreCase("toDate")) {
						List<String> values = queryParams.get(key);
						String toDateParam = values.get(0);
						if (toDateParam != null) {
							if (toDateParam.startsWith("1") || (toDateParam.startsWith("2"))) {
								toDate = toDateParam;
							}
							else {
								throw new IllegalArgumentException(
										"Bad date parameter: " + toDateParam);
							}
						}
					}
					else if (key.equals("scope")) {
						List<String> values = queryParams.get(key);
						String scopeParam = values.get(0);
						if (scopeParam != null) {
							boolean isValidScope = DataPackageManager.isValidScope(scopeParam);
							if (isValidScope) {
								scope = scopeParam;
							}
							else {
								throw new IllegalArgumentException(
										"Bad scope parameter: " + scopeParam);
							}
						}
					}
				}
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			xml = dataPackageManager.listDataPackageChanges(fromDate, toDate, scope);
			responseBuilder = Response.ok(xml);
			response = responseBuilder.build();
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);
		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List User Data Packages</strong> operation, returning all
		 * packageId values (including revision values) where the principal
		 * owner (i.e. the user who uploaded the data package) matches the
		 * specified distinguished name value.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/user/uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>An ordered list of all packageId values (including revision value)
		 * of all (non-deleted) data packages in the resource registry where the principal
		 * owner value matches the specified distinguished name value. </td>
		 * <td align=center><code>text/plain</code></td>
		 * <td>
		 *
		 * <pre>
		 * knb-lter-lno.1.1
		 * knb-lter-lno.1.2
		 * knb-lter-lno.2.1
		 * knb-lter-lno.2.2
		 * knb-lter-xyz.1.1
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 *
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @return a Response, containing a newline separated list of packageId values (including revision)
		 */
		@GET @Path("/user/{dn}") @Produces("text/plain")
		public Response listUserDataPackages (@Context HttpHeaders
		headers, @PathParam("dn") String dn){
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listUserDataPackages";
		Rule.Permission permission = Rule.Permission.read;
		AuthToken authToken = null;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			String packageList = dataPackageManager.listUserDataPackages(dn);

			if (packageList != null) {
				responseBuilder = Response.ok(packageList.trim());
				response = responseBuilder.build();
			}
			else {
				String message = "An unknown error occurred";
				throw new Exception(message);
			}
		} catch (IllegalArgumentException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>List Working On</strong> operation, lists the set of data
		 * packages that PASTA is currently working on inserting or updating.
		 * Note that data packages that are being evaluated by PASTA are not
		 * included in this list. (See example below.)
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>None</td>
		 * <td align=center></td>
		 * <td>
		 * <code>curl -i -X GET "https://pasta.lternet.edu/package/workingon/eml"
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The query was successful</td>
		 * <td align=center>An XML document containing the list of recent data package inserts or updates</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 *
		 * <pre>
		 &lt;workingOn&gt;
		 &lt;dataPackage&gt;
		 &lt;packageId&gt;knb-lter-nwk.1504.1&lt;/packageId&gt;
		 &lt;startDate&gt;2016-12-08 16:58:29.307&lt;/startDate&gt;
		 &lt;/dataPackage&gt;
		 &lt;dataPackage&gt;
		 &lt;packageId&gt;lter-landsat-ledaps.7.1&lt;/packageId&gt;
		 &lt;startDate&gt;2016-12-08 17:20:59.998&lt;/startDate&gt;
		 &lt;/dataPackage&gt;
		 &lt;dataPackage&gt;
		 &lt;packageId&gt;knb-lter-nwk.1490.1&lt;/packageId&gt;
		 &lt;startDate&gt;2016-12-12 16:55:05.453&lt;/startDate&gt;
		 &lt;/dataPackage&gt;
		 &lt;/workingOn&gt;
		 * </pre>
		 *
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request message body contains an error, such as an
		 * improperly formatted path query string</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to execute the
		 * Search Data Packages service method</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 *
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @return a Response, which if successful, contains an XML document
		 */
		@GET @Path("/workingon/eml") @Produces("application/xml")
		public Response listWorkingOn (@Context HttpHeaders headers, @Context UriInfo
		uriInfo){
		AuthToken authToken = null;
		String resourceId = null;
		String entryText = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listWorkingOn";
		Rule.Permission permission = Rule.Permission.read;

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();
			String xml = dataPackageManager.listWorkingOn();
			responseBuilder = Response.ok(xml);
			response = responseBuilder.build();
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		audit(serviceMethodName, authToken, response, resourceId, entryText);
		response = stampHeader(response);
		return response;
	}


		/**
		 * <strong>Update Data Package</strong> operation, specifying the scope and
		 * identifier of the data package to be updated in the URI, along with the
		 * EML document describing the data package to be created in the request
		 * message body, and returning a <em>transaction identifier</em> in the
		 * response message body as plain text; the <em>transaction identifier</em>
		 * may be used in a subsequent call to <code>readDataPackageError</code> to
		 * determine the operation status; see <code>readDataPackage</code> to
		 * obtain the data package resource map if the operation completed
		 * successfully.
		 *
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>EML document</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 * <code>curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:PASSWORD"
		 * -X PUT -H "Content-Type: application/xml"
		 * --data-binary @knb-lter-lno.1.1.xml
		 * https://pasta.lternet.edu/package/eml/knb-lter-lno/1</code></td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Query parameters:</h4>
		 *
		 * <p>
		 * An optional query parameter, "useChecksum", can be appended to the URL. When specified,
		 * the useChecksum query parameter directs the server to determine whether it can use an
		 * existing copy of a data entity from a previous revision of the data package based on
		 * matching a metadata-documented checksum value (MD5 or SHA-1) to the checksum of the
		 * existing copy. If a match is found, the server will skip the upload of the data entity from
		 * the remote URL and instead use its matching copy.
		 *
		 * Please Note: Specifying "useChecksum" can save time by eliminating data uploads, but clients
		 * should take care to ensure that metadata-documented checksum values are accurate and up to date.
		 * </p>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>202 Accepted</td>
		 * <td align=center>The <em>update data package</em> request was accepted
		 * for processing</td>
		 * <td align=center>A <em>transaction identifier</em> for use in subsequent
		 * processing of the request (see <code>readDataPackageError</code> to
		 * understand how the transaction identifier may be used to determine if an
		 * error occurred during the operation)</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>1364424858431</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to execute this
		 * service method</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param emlFile
		 *            The URL to an EML document, as specified in the payload of the
		 *            request.
		 *
		 * @return a Response, which if successful, contains a resource map
		 *         describing the contents of the updated data package
		 */
		@PUT @Path("/eml/{scope}/{identifier}") @Consumes("application/xml") @Produces(
				"text/plain") public Response updateDataPackage (@Context HttpHeaders
		headers, @Context UriInfo uriInfo, @PathParam("scope") String
		scope, @PathParam("identifier") Integer identifier, File emlFile){
		AuthToken authToken = null;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "updateDataPackage";
		Rule.Permission permission = Rule.Permission.write;

		try {
			if (this.readOnly) {
				throw new ServiceUnavailableException("PASTA is now in read-only mode");
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			QueryString queryStr = new QueryString(uriInfo);
			Map<String, List<String>> queryParams = queryStr.getParams();
			boolean useChecksum = false;
			if (queryParams != null) {
				for (String key : queryParams.keySet()) {
					if (key.equalsIgnoreCase("useChecksum")) {
						useChecksum = true;
					}
				}
			}

			// Is user authorized to run the service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						String.format("User %s is not authorized to execute service method %s",
								userId, serviceMethodName));
			}

			String transaction = generateTransactionID("update", scope, identifier, null);

			// Perform updateDataPackage in new thread
			Updator updator =
					new Updator(emlFile, scope, identifier, userId, authToken, transaction,
							useChecksum);
			ExecutorService executorService = Executors.newCachedThreadPool();
			executorService.execute(updator);
			executorService.shutdown();

			responseBuilder = Response.status(Response.Status.ACCEPTED);
			responseBuilder.entity(transaction);
			response = responseBuilder.build();
			response = stampHeader(response);
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ServiceUnavailableException e) {
			response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
		}

		return response;
	}


		/**
		 * <strong>Delete Data Package</strong> operation, specifying the scope and
		 * identifier of the data package to be deleted in the URI. The data package
		 * and its associated quality reports are deleted.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X DELETE https://pasta.lternet.edu/package/eml/knb-lter-lno/1</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The delete request was successful</td>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to delete the data
		 * package</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=center>No data package associated with the specified packageId
		 * is found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope value of the data package
		 * @param identifier
		 *            The identifier value of the data package
		 * @return a Response object
		 */
		@DELETE @Path("/eml/{scope}/{identifier}") @Produces("text/plain")
		public Response deleteDataPackage (@Context HttpHeaders
		headers, @PathParam("scope") String scope, @PathParam("identifier") Integer
		identifier){
		boolean deleted = false;
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "deleteDataPackage";
		Rule.Permission permission = Rule.Permission.write;
		AuthToken authToken = null;
		String entryText = null;

		try {
			if (this.readOnly) {
				throw new ServiceUnavailableException("PASTA is now in read-only mode");
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the 'deleteDataPackage' service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			entryText = "Deleted " + scope + "." + identifier.toString();

			DataPackageManager dataPackageManager = new DataPackageManager();
			deleted =
					dataPackageManager.deleteDataPackage(scope, identifier, userId, authToken);

			if (deleted) {
				responseBuilder = Response.ok();
				response = responseBuilder.build();
			}
			else {
				throw new Exception(
						"Data package was not deleted due to an internal server error");
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (ServiceUnavailableException e) {
			response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		String resourceId = null;
		audit(serviceMethodName, authToken, response, resourceId, entryText);
		response = stampHeader(response);
		return response;
	}


		/**********************************************************************
		 * Event-related web services (formerly handled by the Event Manager) *
		 **********************************************************************/

		/**
		 * <strong>Create Event Subscription</strong> operation, creates a new event subscription.
		 *
		 * <h4>Request entity:</h4>
		 *
		 * <p>
		 * The request entity should be an XML document (MIME type
		 * <code>application/xml</code>) that contains the subscription's EML
		 * packageId, and URL, with the syntax:
		 * </p>
		 *
		 * <pre>
		 *    &lt;subscription type="eml"&gt;
		 *       &lt;packageId&gt;<em>packageId</em>&lt;/packageId&gt;
		 *       &lt;url&gt;<em>url</em>&lt;/url&gt;
		 *    &lt;/subscription&gt;
		 * </pre>
		 *
		 * <p>
		 * The packageId can be either complete or partial. The URL must have 'http'
		 * as its scheme and must be able to receive POST requests with MIME type
		 * <code>text/plain</code>. Note that some characters must be escaped in
		 * XML, such as ampersands (&amp;) in the query string of the URL, from
		 * <code>&amp;</code> to <code>&amp;amp;</code>.
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
		 * <td>201 Created</td>
		 * <td>If the request was successful.</td>
		 * <td>None, but the <code>Location</code> header will contain a URL that
		 * references the new subscription.</td>
		 * <td>N/A</td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the request entity contains an error, such as improperly formatted
		 * XML, EML packageId, or URL.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to create subscriptions.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>409 Conflict</td>
		 * <td>If a subscription already exists with the same creator, EML
		 * packageId, and URL attributes.</td>
		 * <td>
		 * An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 *
		 * @param headers
		 *            the HTTP request headers containing the authorization token.
		 * @param requestBody
		 *            the POST request's body, containing XML.
		 *
		 * @return an appropriate HTTP response.
		 */
		@POST @Path("/event/eml") @Consumes(MediaType.APPLICATION_XML)
		public Response createSubscription (@Context HttpHeaders headers, String requestBody)
		{
			XmlSubscriptionFormatV1 xmlSubscriptionFormatV1 = new XmlSubscriptionFormatV1();
			AuthToken authToken = null;
			String msg = null;
			Rule.Permission permission = Rule.Permission.write;
			Response response = null;
			final String serviceMethodName = "createSubscription";

			try {
				if (this.readOnly) {
					throw new ServiceUnavailableException("PASTA is now in read-only mode");
				}

				authToken = getAuthToken(headers);
				String userId = authToken.getUserId();

				// Is user authorized to run the 'createSubscription' service
				// method?
				boolean serviceMethodAuthorized =
						isServiceMethodAuthorized(serviceMethodName, permission, authToken);

				if (!serviceMethodAuthorized) {
					throw new UnauthorizedException(
							"User " + userId + " is not authorized to execute service method " +
									serviceMethodName);
				}

				EmlSubscription emlSubscription = xmlSubscriptionFormatV1.parse(requestBody);
				emlSubscription.setCreator(userId);
				String creator = emlSubscription.getCreator();
				String scope = emlSubscription.getScope();
				Integer identifier = emlSubscription.getIdentifier();
				Integer revision = emlSubscription.getRevision();
				String url = emlSubscription.getUrl().toString();
				SubscriptionRegistry subscriptionRegistry = new SubscriptionRegistry();
				int subscriptionId =
						subscriptionRegistry.addSubscription(creator, scope, identifier, revision,
								url);
				URI uri = URI.create(String.format("%d", subscriptionId));
				emlSubscription.setSubscriptionId(new Integer(subscriptionId));
				msg = String.format("Created subscription with the following attributes: %s",
						emlSubscription.toString());
				response = Response.created(uri).build();
			} catch (XmlParsingException e) {
				response = WebExceptionFactory.makeBadRequest(e).getResponse();
				msg = e.getMessage();
			} catch (UnauthorizedException e) {
				response = WebExceptionFactory.makeUnauthorized(e).getResponse();
				msg = e.getMessage();
			} catch (ResourceExistsException e) {
				response = WebExceptionFactory.makeConflict(e).getResponse();
				msg = e.getMessage();
			} catch (ServiceUnavailableException e) {
				response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
			} catch (Exception e) {
				WebApplicationException webApplicationException =
						WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
								e.getMessage());
				response = webApplicationException.getResponse();
				msg = e.getMessage();
			} finally {
				audit(serviceMethodName, authToken, response, null, msg);
			}

			return response;
		}


		/**
		 * <strong>Delete Event Subscription</strong> operation, deletes the event subscription with the specified ID from the subscription
		 * database. After "deletion," the subscription might still exist in the
		 * subscription database, but it will be inactive - it will not conflict
		 * with future creation requests, it cannot be read, and it will not be
		 * notified of events.
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
		 * <td>None</td>
		 * <td>N/A</td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the specified identification number cannot be parsed as an
		 * integer.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to delete the specified
		 * subscription.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>404 Not Found</td>
		 * <td>If a subscription has never existed in the subscription database with
		 * the specified identification number.</td>
		 * <td>
		 * An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>410 Gone</td>
		 * <td>If the specified subscription has been previously deleted.</td>
		 * <td>
		 * An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            the HTTP request headers containing the authorization token.
		 *
		 * @param subscriptionId
		 *            the ID of the subscription to be deleted.
		 *
		 * @return an appropriate HTTP response.
		 */
		@DELETE @Path("/event/eml/{subscriptionId}") public Response deleteSubscription
		(@Context HttpHeaders headers, @PathParam("subscriptionId") String subscriptionId){
		AuthToken authToken = null;
		String msg = null;
		Rule.Permission permission = Rule.Permission.write;
		Response response = null;
		final String serviceMethodName = "deleteSubscription";

		try {
			if (this.readOnly) {
				throw new ServiceUnavailableException("PASTA is now in read-only mode");
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the 'deleteSubscription' service
			// method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);

			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			Integer id = parseSubscriptionId(subscriptionId);
			SubscriptionRegistry subscriptionRegistry = new SubscriptionRegistry();
			Integer deletedSubscriptionId =
					subscriptionRegistry.deleteSubscription(id, userId);
			msg =
					String.format("Deleted subscription with id = '%d'.", deletedSubscriptionId);
			response = Response.ok().build();
		} catch (IllegalArgumentException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
			msg = e.getMessage();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
			msg = e.getMessage();
		} catch (ResourceNotFoundException e) {
			response = WebExceptionFactory.makeNotFound(e).getResponse();
			msg = e.getMessage();
		} catch (ResourceDeletedException e) {
			response = WebExceptionFactory.makeGone(e).getResponse();
			msg = e.getMessage();
		} catch (WebApplicationException e) {
			response = e.getResponse();
			msg = e.getMessage();
		} catch (ServiceUnavailableException e) {
			response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
		} catch (Exception e) {
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
			msg = e.getMessage();
		} finally {
			audit(serviceMethodName, authToken, response, null, msg);
		}

		return response;
	}


		/**
		 * <strong>Execute Event Subscription</strong> operation, specifying the
		 * ID of the event subscription whose URL is to be executed.
		 *
		 * Used to execute a particular subscription in the event manager, via an
		 * HTTP POST request. Upon notification, the event manager queries its
		 * database for the subscription matching the specified subscriptionId. POST
		 * requests are then made (asynchronously) to the matching subscription.
		 * <p>
		 * The request headers must contain an authorization token. If the request
		 * is successful, an HTTP response with status code 200 'OK' is returned. If
		 * the request is unauthorized, based on the content of the authorization
		 * token and the current access control rule for event notification, status
		 * code 401 'Unauthorized' is returned. If the request contains an error,
		 * status code 400 'Bad Request' is returned, with a description of the
		 * encountered error.
		 * </p>
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td></td>
		 * <td></td>
		 * <td>curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:PASSWORD" -X POST
		 * https://pasta.lternet.edu/package/event/eml/120</td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td>200 OK</td>
		 * <td>If the operation is successful</td>
		 * <td></td>
		 * <td><code></code></td>
		 * <td></td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the request contains an error, such as a request containing a
		 * non-integer subscriptionId value</td>
		 * <td>An error message</td>
		 * <td><code>text/plain</code></td>
		 * <td>The provided subscription ID 'abc' cannot be parsed as an integer.</td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to execute the specified
		 * subscription</td>
		 * <td>An error message</td>
		 * <td><code>text/plain</code></td>
		 * <td></td>
		 * </tr>
		 * <tr>
		 * <td>405 Method Not Allowed</td>
		 * <td>The specified HTTP method is not allowed for the requested resource.
		 * For example, the HTTP method was specified as DELETE but the resource can
		 * only support POST.</td>
		 * <td>An error message</td>
		 * <td><code>text/plain</code></td>
		 * <td></td>
		 * </tr>
		 * <tr>
		 * <td>409 Conflict</td>
		 * <td>If a subscription with the specified subscriptionId had been deleted
		 * previously</td>
		 * <td>An error message</td>
		 * <td><code>text/plain</code></td>
		 * <td></td>
		 * </tr>
		 * <tr>
		 * <td>500 Internal Server Error</td>
		 * <td>The server encountered an unexpected condition which prevented it
		 * from fulfilling the request. For example, a SQL error occurred, or an
		 * unexpected condition was encountered while processing the request</td>
		 * <td>An error message</td>
		 * <td><code>text/plain</code></td>
		 * <td></td>
		 * </tr>
		 * </table>
		 *
		 * @param httpHeaders
		 *            the HTTP request headers containing the authorization token.
		 * @param subscriptionId
		 *            the subscription identifier value, e.g. "84"
		 *
		 * @return an HTTP response with an appropriate status code.
		 */
		@POST @Path("/event/eml/{subscriptionId}") public Response executeSubscription
		(@Context HttpHeaders httpHeaders, @PathParam("subscriptionId") String
		subscriptionId){
		AuthToken authToken = null;
		String msg = null;
		Rule.Permission permission = Rule.Permission.write;
		Response response = null;
		String serviceMethodName = MethodNameUtility.methodName();

		try {
			authToken = AuthTokenFactory.makeAuthToken(httpHeaders.getCookies());
			String userId = authToken.getUserId();

			// Is user authorized to run the 'executeSubscription' service
			// method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);

			if (!serviceMethodAuthorized) {
				String errorMsg =
						String.format("User %s is not authorized to execute service method %s.",
								userId, serviceMethodName);
				throw new UnauthorizedException(errorMsg);
			}

			EmlSubscription emlSubscription = getSubscription(subscriptionId, userId);
			msg = String.format("Executed subscription with the following attributes: %s",
					emlSubscription.toString());
			EventManagerClient.asynchronousNotify(null, emlSubscription, null);
			response = Response.ok().build();
		} catch (IllegalArgumentException e) {
			msg = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (UnauthorizedException e) {
			msg = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (ResourceNotFoundException e) {
			msg = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			msg = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (ResourceExistsException e) {
			msg = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			msg = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			msg = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		} finally {
			if (response != null) {
				audit(serviceMethodName, authToken, response, null, msg);
			}
		}

		return response;
	}


		/**
		 * <strong>Query Event Subscriptions</strong> operation, returns a list of the subscriptions whose attributes match those specified in the query string.
		 *
		 * If a query string is omitted, all subscriptions in the
		 * subscription database will be returned for which the requesting user is
		 * authorized to read. If query parameters are included, they are used to
		 * filter that set of subscriptions based on their attributes.</p>
		 *
		 * <h4>Query parameters:</h4>
		 *
		 * <p>
		 * Query parameters are specified as <em>key=value</em> pairs, multiple
		 * pairs must be delimited with ampersands (&amp;), and only a single value
		 * should be specified for a particular key. The following query parameter
		 * keys are allowed:
		 * </p>
		 *
		 * <ul>
		 * <li><code>creator</code></li>
		 * <li><code>scope</code></li>
		 * <li><code>identifier</code></li>
		 * <li><code>revision</code></li>
		 * <li><code>url</code></li>
		 * </ul>
		 *
		 * <p>
		 * If a query parameter is specified, and a subscription's respective
		 * attribute does not match it, that subscription will not be included in
		 * the group of subscriptions returned. If <code>scope</code>,
		 * <code>identifier</code>, or <code>revision</code> are used, their values
		 * must together constitute a syntactically and semantically correct EML
		 * packageId - either partial or complete. If <code>url</code> is used, its
		 * value must not contain ampersands. Therefore, if a subscription's URL
		 * contains ampersands, it cannot be filtered based on its URL.
		 * </p>
		 *
		 *
		 * <h4>Responses:</h4>
		 *
		 * <p>
		 * If the request is successful, the response entity will be an XML
		 * representation of the subscription group with the following syntax:
		 * </p>
		 *
		 * <pre>
		 * &lt;subscriptions&gt;
		 *
		 *    &lt;subscription type="eml"&gt;
		 *       &lt;id&gt;<em>id</em>&lt;/id&gt;
		 *       &lt;creator&gt;<em>creator</em>&lt;/creator&gt;
		 *       &lt;packageId&gt;<em>packageId</em>&lt;/packageId&gt;
		 *       &lt;url&gt;<em>url</em>&lt;/url&gt;
		 *    &lt;/subscription&gt;
		 *
		 *    ...
		 *
		 * &lt;/subscriptions&gt;
		 * </pre>
		 *
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
		 * <td>The matching subscriptions.</td>
		 * <td><code>application/xml</code></td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the query string contains an error.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to read subscriptions.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            HTTP headers containing the requesting user's credentials.
		 * @param uriInfo
		 *            contains the query parameters used to match subscriptions.
		 *
		 * @return all active subscriptions in the database that match the provided
		 *         query and for which the user is authorized to read.
		 *
		 * @return an appropriate HTTP response.
		 */
		@GET @Path("/event/eml") @Produces(
				value = {MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
		public Response getMatchingSubscriptions (@Context HttpHeaders
		headers, @Context UriInfo uriInfo){
		AuthToken authToken = null;
		String msg = null;
		Rule.Permission permission = Rule.Permission.read;
		Response response = null;
		final String serviceMethodName = "getMatchingSubscriptions";

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the 'getMatchingSubscriptions' service
			// method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);

			if (!serviceMethodAuthorized) {
				String errorMsg =
						String.format("User %s is not authorized to execute service method %s.",
								userId, serviceMethodName);
				throw new UnauthorizedException(errorMsg);
			}

			QueryString queryStr = new QueryString(uriInfo);
			queryStr.checkForIllegalKeys(VALID_EVENT_QUERY_KEYS);
			Map<String, List<String>> queryParams = queryStr.getParams();
			SubscriptionRegistry subscriptionRegistry = new SubscriptionRegistry();
			List<EmlSubscription> emlSubscriptions =
					subscriptionRegistry.getSubscriptions(userId, queryParams);
			String xml = subscriptionsToXML(emlSubscriptions);
			response = Response.ok(xml, MediaType.APPLICATION_XML).build();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
			msg = e.getMessage();
		} catch (WebApplicationException e) {
			response = e.getResponse();
			msg = e.getMessage();
		} catch (Exception e) {
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
			msg = e.getMessage();
		} finally {
			audit(serviceMethodName, authToken, response, null, msg);
		}

		return response;
	}


		private EmlSubscription getSubscription (String subscriptionId, String userId)
			throws Exception {
		EmlSubscription emlSubscription = null;

		SubscriptionRegistry subscriptionRegistry = new SubscriptionRegistry();
		Integer id = parseSubscriptionId(subscriptionId);
		emlSubscription = subscriptionRegistry.getSubscription(id, userId);

		return emlSubscription;
	}


		/**
		 * <strong>Get Event Subscription</strong> operation, returns the event subscription with the specified ID.
		 *
		 * <h4>Responses:</h4>
		 *
		 * <p>
		 * If the request is successful, the response will contain an XML entity
		 * with the following syntax:
		 * </p>
		 *
		 * <pre>
		 *    &lt;subscription type="eml"&gt;
		 *       &lt;id&gt;<em>id</em>&lt;/id&gt;
		 *       &lt;creator&gt;<em>creator</em>&lt;/creator&gt;
		 *       &lt;packageId&gt;<em>packageId</em>&lt;/packageId&gt;
		 *       &lt;url&gt;<em>url</em>&lt;/url&gt;
		 *    &lt;/subscription&gt;
		 * </pre>
		 *
		 * <p>
		 * The difference between this response entity and the request entity used
		 * to create the subscription is the addition of the <code>id</code> and
		 * <code>creator</code> elements, which are determined by the Event Manager
		 * upon subscription creation.
		 * </p>
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
		 * <td>The specified subscription's attributes.</td>
		 * <td><code>application/xml</code></td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the specified identification number cannot be parsed as an
		 * integer.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to read the specified
		 * subscription.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>404 Not Found</td>
		 * <td>If a subscription has never existed in the subscription database with
		 * the specified identification number.</td>
		 * <td>
		 * An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>410 Gone</td>
		 * <td>If the specified subscription has been previously deleted.</td>
		 * <td>
		 * An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            the HTTP request headers containing the authorization token.
		 *
		 * @param subscriptionId
		 *            the ID of the subscription to be returned.
		 *
		 * @return an appropriate HTTP response.
		 */
		@GET @Path("/event/eml/{subscriptionId}") @Produces(
				value = {MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
		public Response getSubscriptionWithId (@Context HttpHeaders
		headers, @PathParam("subscriptionId") String subscriptionId){

		AuthToken authToken = null;
		String msg = null;
		Rule.Permission permission = Rule.Permission.read;
		Response response = null;
		final String serviceMethodName = "getSubscriptionWithId";

		try {
			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the 'getSubscriptionWithId' service
			// method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);

			if (!serviceMethodAuthorized) {
				String errorMsg =
						String.format("User %s is not authorized to execute service method %s.",
								userId, serviceMethodName);
				throw new UnauthorizedException(errorMsg);
			}

			Integer id = parseSubscriptionId(subscriptionId);
			SubscriptionRegistry subscriptionRegistry = new SubscriptionRegistry();
			EmlSubscription emlSubscription =
					subscriptionRegistry.getSubscription(id, userId);
			StringBuffer stringBuffer = new StringBuffer("<subscriptions>\n");
			stringBuffer.append(emlSubscription.toXML());
			stringBuffer.append("</subscriptions>\n");
			msg = String.format("Subscription id = '%d'", id);
			String xml = stringBuffer.toString();
			response = Response.ok(xml, MediaType.APPLICATION_XML).build();
		} catch (IllegalArgumentException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
			msg = e.getMessage();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
			msg = e.getMessage();
		} catch (ResourceNotFoundException e) {
			response = WebExceptionFactory.makeNotFound(e).getResponse();
			msg = e.getMessage();
		} catch (ResourceDeletedException e) {
			response = WebExceptionFactory.makeGone(e).getResponse();
			msg = e.getMessage();
		} catch (WebApplicationException e) {
			response = e.getResponse();
			msg = e.getMessage();
		} catch (Exception e) {
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
			msg = e.getMessage();
		} finally {
			audit(serviceMethodName, authToken, response, null, msg);
		}

		return response;
	}


		private Integer parseSubscriptionId (String s){
		try {
			return new Integer(s);
		} catch (NumberFormatException e) {
			String err = String.format(
					"The provided subscription ID '%s' cannot be parsed as an integer.", s);
			throw new IllegalArgumentException(err);
		}
	}


		private String subscriptionsToXML (List < EmlSubscription > subscriptions) {
		String xmlString = null;

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<subscriptions>\n");

		for (EmlSubscription subscription : subscriptions) {
			stringBuffer.append(subscription.toXML());
		}

		stringBuffer.append("</subscriptions>\n");
		xmlString = stringBuffer.toString();
		return xmlString;
	}


		/**
		 * <strong>Get Event Subscription Schema</strong> operation, returns the XML schema for event subscription creation request entities.
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
		 * <td>The XML schema.</td>
		 * <td><code>application/xml</code></td>
		 * </tr>
		 * </table>
		 *
		 * @return an appropriate HTTP response.
		 */
		@GET @Path("/event/eml/schema") @Produces(MediaType.APPLICATION_XML) @Consumes(
				MediaType.TEXT_PLAIN) public Response respondWithSchema () {
		String schemaString = ConfigurationListener.getEventSubscriptionDocument();
		return Response.ok(schemaString, MediaType.APPLICATION_XML).build();
	}

		/*
		 * End of event-related web services section
		 */


		/**
		 * Thread framework for executing the createDataPackageArchive in a new
		 * thread.
		 *
		 * @author servilla
		 * @since April 12, 2013
		 *
		 */
		class Archivor implements Runnable {

			String scope = null;
			Integer identifier = null;
			Integer revision = null;
			String userId = null;
			AuthToken authToken = null;
			String transaction = null;


			public Archivor(String scope, Integer identifier, Integer revision, String userId,
											AuthToken authToken, String transaction)
			{

				this.scope = scope;
				this.identifier = identifier;
				this.revision = revision;
				this.userId = userId;
				this.authToken = authToken;
				this.transaction = transaction;

			}


			public void run()
			{

				String archive = "";
				String gripe = null;
				String resourceId = DataPackageManager.composeResourceId(ResourceType.archive, scope, identifier, revision, null);
				Response response = null;
				ResponseBuilder responseBuilder = null;
				String serviceMethodName = "createDataPackageArchive";
				DataPackageManager dpm = null;

				try {

					dpm = new DataPackageManager();
					archive = dpm.createDataPackageArchive(scope, identifier, revision, userId,
							authToken, transaction);

					responseBuilder = Response.ok(archive);
					response = responseBuilder.build();

				} catch (IllegalArgumentException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeBadRequest(e).getResponse();
				} catch (UnauthorizedException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeUnauthorized(e).getResponse();
				} catch (ResourceNotFoundException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeNotFound(e).getResponse();
				} catch (ResourceDeletedException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeConflict(e).getResponse();
				} catch (ResourceExistsException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeConflict(e).getResponse();
				} catch (UserErrorException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebResponseFactory.makeBadRequest(e);
				} catch (Exception e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response =
							WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, null,
									e.getMessage()).getResponse();
				}

				audit(serviceMethodName, authToken, response, resourceId, gripe);

			}

		}

		/**
		 * Thread framework for executing the createDataPackage in a new thread.
		 *
		 * @author servilla
		 * @since Mar 26, 2013
		 *
		 */
		class Creator implements Runnable {

			File emlFile = null;
			String userId = null;
			AuthToken authToken = null;
			String transaction = null;


			public Creator(File emlFile, String userId, AuthToken authToken,
										 String transaction)
			{

				this.emlFile = emlFile;
				this.userId = userId;
				this.authToken = authToken;
				this.transaction = transaction;

			}


			public void run()
			{

				String map = null;
				String gripe = null;
				Response response = null;
				ResponseBuilder responseBuilder = null;
				String serviceMethodName = "createDataPackage";
				String resourceId = "";
				DataPackageManager dpm = null;

				try {

					dpm = new DataPackageManager();
					map = dpm.createDataPackage(emlFile, userId, authToken, transaction);

					if (map == null) {
						gripe = "Data package create operation failed for unknown reason";
						Exception e = new Exception(gripe);
						dpm.writeDataPackageError(transaction, e);
						response =
								WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, null,
										gripe).getResponse();
					}
					else {
						resourceId = DataPackageManagerResource.resourceIdFromResourceMap(map);
						responseBuilder = Response.ok(map);
						response = responseBuilder.build();
					}

				} catch (IllegalArgumentException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeBadRequest(e).getResponse();
				} catch (UnauthorizedException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeUnauthorized(e).getResponse();
				} catch (ResourceNotFoundException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeNotFound(e).getResponse();
				} catch (ResourceDeletedException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeConflict(e).getResponse();
				} catch (ResourceExistsException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeConflict(e).getResponse();
				} catch (UserErrorException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebResponseFactory.makeBadRequest(e);
				} catch (Exception e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response =
							WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, null,
									e.getMessage()).getResponse();
				}

				audit(serviceMethodName, authToken, response, resourceId, gripe);

			}

		}

		/**
		 * Thread framework for executing the evaluateDataPackage service method in
		 * a new thread.
		 *
		 * @author costa
		 * @since Mar 28, 2013
		 *
		 */
		class Evaluator implements Runnable {

			File emlFile = null;
			String userId = null;
			AuthToken authToken = null;
			String transaction = null;
			boolean useChecksum = false;


			public Evaluator(File emlFile, String userId, AuthToken authToken,
											 String transaction, boolean useChecksum)
			{

				this.emlFile = emlFile;
				this.userId = userId;
				this.authToken = authToken;
				this.transaction = transaction;
				this.useChecksum = useChecksum;
			}


			public void run()
			{

				String xmlString = null;
				String gripe = null;
				Response response = null;
				ResponseBuilder responseBuilder = null;
				String serviceMethodName = "evaluateDataPackage";
				String resourceId = "";
				DataPackageManager dpm = null;

				try {

					dpm = new DataPackageManager();
					xmlString = dpm.evaluateDataPackage(emlFile, userId, authToken, transaction,
							useChecksum);

					if (xmlString == null) {
						gripe = "Data package evaluate operation failed for unknown reason";
						Exception e = new Exception(gripe);
						dpm.writeDataPackageError(transaction, e);
						response =
								WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, null,
										gripe).getResponse();
					}
					else {
						responseBuilder = Response.ok(xmlString);
						response = responseBuilder.build();
					}

				} catch (IllegalArgumentException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeBadRequest(e).getResponse();
				} catch (UnauthorizedException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeUnauthorized(e).getResponse();
				} catch (ResourceNotFoundException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeNotFound(e).getResponse();
				} catch (ResourceDeletedException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeConflict(e).getResponse();
				} catch (ResourceExistsException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeConflict(e).getResponse();
				} catch (UserErrorException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebResponseFactory.makeBadRequest(e);
				} catch (Exception e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response =
							WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, null,
									e.getMessage()).getResponse();
				}

				audit(serviceMethodName, authToken, response, resourceId, gripe);

			}

		}

		/**
		 * Thread framework for executing the updateDataPackage in a new thread.
		 *
		 * @author servilla
		 * @since Mar 26, 2013
		 *
		 */
		class Updator implements Runnable {

			File emlFile = null;
			String scope = null;
			Integer identifier = null;
			String userId = null;
			AuthToken authToken = null;
			String transaction = null;
			boolean useChecksum = false;


			public Updator(File emlFile, String scope, Integer identifier, String userId,
										 AuthToken authToken, String transaction, boolean useChecksum)
			{

				this.emlFile = emlFile;
				this.scope = scope;
				this.identifier = identifier;
				this.userId = userId;
				this.authToken = authToken;
				this.transaction = transaction;
				this.useChecksum = useChecksum;
			}


			public void run()
			{

				String map = null;
				String gripe = null;
				Response response = null;
				ResponseBuilder responseBuilder = null;
				String serviceMethodName = "updateDataPackage";
				String resourceId = "";
				DataPackageManager dpm = null;

				try {

					dpm = new DataPackageManager();
					map = dpm.updateDataPackage(emlFile, scope, identifier, userId, authToken,
							transaction, useChecksum);

					if (map == null) {
						gripe = "Data package update operation failed for unknown reason";
						Exception e = new Exception(gripe);
						dpm.writeDataPackageError(transaction, e);
						response =
								WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, null,
										gripe).getResponse();
					}
					else {
						resourceId = DataPackageManagerResource.resourceIdFromResourceMap(map);
						responseBuilder = Response.ok(map);
						response = responseBuilder.build();
					}

				} catch (IllegalArgumentException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeBadRequest(e).getResponse();
				} catch (UnauthorizedException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeUnauthorized(e).getResponse();
				} catch (ResourceNotFoundException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeNotFound(e).getResponse();
				} catch (ResourceDeletedException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeConflict(e).getResponse();
				} catch (ResourceExistsException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebExceptionFactory.makeConflict(e).getResponse();
				} catch (UserErrorException e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response = WebResponseFactory.makeBadRequest(e);
				} catch (Exception e) {
					gripe = e.getMessage();
					dpm.writeDataPackageError(transaction, e);
					response =
							WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, null,
									e.getMessage()).getResponse();
				}

				audit(serviceMethodName, authToken, response, resourceId, gripe);

			}

		}


		/**
		 * Thread framework for cleaning the temporary data/archive directory of old files.
		 *
		 * @author dcosta
		 * @since April 24, 2015
		 *
		 */
		class Cleaner implements Runnable {
			String tmpDir = null;
			long ttl = -1L;

			public Cleaner(String tmpDir, long ttl)
			{
				this.tmpDir = tmpDir;
				this.ttl = ttl;
			}


			public void run()
			{
				if (this.ttl > -1L) {
					doClean();
				}
			}


			/**
			 * Removes any file that is older than the specified time-to-live
			 * (ttl).
			 *
			 * @param ttl
			 *            The time-to-live value in milliseconds.
			 */
			public void doClean()
			{
				File tmpDir = new File(this.tmpDir);
				String[] ext =
						null; // null will cause files with all file extensions to be deleted
				Long time = new Date().getTime();
				Long lastModified = null;
				boolean recursive = false;

				Collection<File> files = FileUtils.listFiles(tmpDir, ext, recursive);
				final String patternString = "^(\\d+)[-\\.].+$";
				Pattern pattern = Pattern.compile(patternString);

				for (File file : files) {
					if (file != null && file.exists()) {
						String filename = file.getName();
						Matcher matcher = pattern.matcher(filename);
						if (matcher.matches()) {
							String dateString = matcher.group(1);
							lastModified = Long.parseLong(dateString);
							// Remove file if older than the ttl
							if (lastModified + this.ttl <= time) {
								try {
									FileUtils.forceDelete(file);
								} catch (IOException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}
							}
						}
					}
				}
			}

		}


		/*
		 * Journal citation web services
		 */


		/**
		 * <strong>Create Journal Citation</strong> operation, creates a new
		 * journal citation entry in PASTA.
		 *
		 * <h4>Request entity:</h4>
		 *
		 * <p>
		 * The request entity should be an XML document (MIME type
		 * <code>application/xml</code>) that contains the journal citation metadata.
		 * For example:
		 * </p>
		 *
		 * <pre>
		 &lt;journalCitation&gt;
		 &lt;packageId&gt;edi.0.3&lt;/packageId&gt;
		 &lt;articleDoi&gt;10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae&lt;/articleDoi&gt;
		 &lt;articleTitle&gt;Tree Survey in Southern Arizona&lt;/articleTitle&gt;
		 &lt;articleUrl&gt;http://swtrees.com/articles/12345&lt;/articleUrl&gt;
		 &lt;journalTitle&gt;Journal of Southwestern Trees&lt;/journalTitle&gt;
		 &lt;/journalCitation&gt;
		 * </pre>
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
		 * <td>201 Created</td>
		 * <td>If the request was successful.</td>
		 * <td>None</td>
		 * <td>N/A</td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the request entity contains an error, such as improperly formatted
		 * XML, EML packageId, or URL.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to create journal citations.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <tr>
		 * <td>404 Not Found</td>
		 * <td>A data package with the specified package identifier was not found in the repository.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>409 Conflict</td>
		 * <td>If a journal citation already exists with the same creator and attributes.</td>
		 * <td>
		 * An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            the HTTP request headers containing the authorization token.
		 * @param requestBody
		 *            the POST request's body, containing XML.
		 *
		 * @return an appropriate HTTP response.
		 */
		@POST @Path("/citation/eml") @Consumes(
				MediaType.APPLICATION_XML + "; charset=utf-8")
		public Response createJournalCitation (@Context HttpHeaders headers, String
		requestBody){
		AuthToken authToken = null;
		String msg = null;
		Rule.Permission permission = Rule.Permission.write;
		Response response = null;
		final String serviceMethodName = "createJournalCitation";

		try {
			if (this.readOnly) {
				throw new ServiceUnavailableException("PASTA is now in read-only mode");
			}

			authToken = getAuthToken(headers);
			String userId = authToken.getUserId();

			// Is user authorized to run the 'createJournalCitation' service method?
			boolean serviceMethodAuthorized =
					isServiceMethodAuthorized(serviceMethodName, permission, authToken);

			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dpm = new DataPackageManager();

			JournalCitation journalCitation = dpm.createJournalCitation(userId, requestBody);

			if (journalCitation != null) {
				int journalCitationId = journalCitation.getJournalCitationId();
				URI uri = URI.create(String.format("%d", journalCitationId));
				msg = String.format("Created journal citation with journalCitationId value: %d",
						journalCitationId);
				response = Response.created(uri).build();
			}
			else {
				throw new Exception(String.format(
						"An error occurred while attempting to create journal citation entry for request: %s",
						requestBody));
			}
		} catch (XmlParsingException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
			msg = e.getMessage();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
			msg = e.getMessage();
		} catch (ResourceNotFoundException e) {
			msg = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (ResourceDeletedException e) {
			msg = e.getMessage();
			response = WebExceptionFactory.makeConflict(e).getResponse();
		} catch (UserErrorException e) {
			msg = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (ServiceUnavailableException e) {
			response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
		} catch (Exception e) {
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
			msg = e.getMessage();
		} finally {
			audit(serviceMethodName, authToken, response, null, msg);
		}

		return response;
	}

		/**
		 * <strong>Update Journal Citation</strong> operation, updates an existing
		 * journal citation entry in PASTA.
		 *
		 * <h4>Request entity:</h4>
		 *
		 * <p>
		 * The request entity should be an XML document (MIME type
		 * <code>application/xml</code>) that contains the journal citation metadata.
		 * For example:
		 * </p>
		 *
		 * <pre>
		 &lt;journalCitation&gt;
		 &lt;packageId&gt;edi.0.3&lt;/packageId&gt;
		 &lt;articleDoi&gt;10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae&lt;/articleDoi&gt;
		 &lt;articleTitle&gt;Tree Survey in Southern Arizona&lt;/articleTitle&gt;
		 &lt;articleUrl&gt;http://swtrees.com/articles/12345&lt;/articleUrl&gt;
		 &lt;journalTitle&gt;Journal of Southwestern Trees&lt;/journalTitle&gt;
		 &lt;/journalCitation&gt;
		 * </pre>
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
		 * <td>201 Update</td>
		 * <td>If the request was successful.</td>
		 * <td>None</td>
		 * <td>N/A</td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the request entity contains an error, such as improperly formatted
		 * XML, EML packageId, or URL.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to create journal citations.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <tr>
		 * <td>404 Not Found</td>
		 * <td>A data package with the specified package identifier was not found in the repository.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>409 Conflict</td>
		 * <td>If a journal citation already exists with the same creator and attributes.</td>
		 * <td>
		 * An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            the HTTP request headers containing the authorization token.
		 * @param citationXml
		 *            the POST request's body, containing XML.
		 *
		 * @return an appropriate HTTP response.
		 */
		@PUT @Path("/citation/eml/{journalCitationId}") @Consumes(
				MediaType.APPLICATION_XML + "; charset=utf-8")
		public Response updateJournalCitation (@Context HttpHeaders headers, String
		citationXml, @PathParam("journalCitationId") Integer journalCitationId)
		{
			AuthToken authToken = null;
			String msg = null;
			Rule.Permission permission = Rule.Permission.write;
			Response response = null;
			final String serviceMethodName = "updateJournalCitation";

			try {
				if (this.readOnly) {
					throw new ServiceUnavailableException("PASTA is now in read-only mode");
				}

				authToken = getAuthToken(headers);
				String userId = authToken.getUserId();

				// Is user authorized to run the 'updateJournalCitation' service method?
				boolean serviceMethodAuthorized =
						isServiceMethodAuthorized(serviceMethodName, permission, authToken);

				if (!serviceMethodAuthorized) {
					throw new UnauthorizedException(
							"User " + userId + " is not authorized to execute service method " +
									serviceMethodName);
				}

				JournalCitation journalCitation = new JournalCitation(citationXml);

				if (journalCitationId != 0 &&
						journalCitationId != journalCitation.getJournalCitationId()) {
					throw new UserErrorException(String.format(
							"Journal citation ID in URL (%d) does not match journal citation ID in XML (%d)",
							journalCitationId, journalCitation.getJournalCitationId()));
				}

				DataPackageManager dpm = new DataPackageManager();
				journalCitation = dpm.updateJournalCitation(userId, citationXml);

				if (journalCitation != null) {
					journalCitationId = journalCitation.getJournalCitationId();
					URI uri = URI.create(String.format("%d", journalCitationId));
					msg =
							String.format("Updated journal citation with journalCitationId value: %d",
									journalCitationId);
					response = Response.created(uri).build();
				}
				else {
					throw new Exception(String.format(
							"An error occurred while attempting to update journal citation entry for request: %s",
							citationXml));
				}
			} catch (XmlParsingException e) {
				response = WebExceptionFactory.makeBadRequest(e).getResponse();
				msg = e.getMessage();
			} catch (UnauthorizedException e) {
				response = WebExceptionFactory.makeUnauthorized(e).getResponse();
				msg = e.getMessage();
			} catch (ResourceNotFoundException e) {
				msg = e.getMessage();
				response = WebExceptionFactory.makeNotFound(e).getResponse();
			} catch (ResourceDeletedException e) {
				msg = e.getMessage();
				response = WebExceptionFactory.makeConflict(e).getResponse();
			} catch (UserErrorException e) {
				msg = e.getMessage();
				response = WebResponseFactory.makeBadRequest(e);
			} catch (ServiceUnavailableException e) {
				response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
			} catch (Exception e) {
				WebApplicationException webApplicationException =
						WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
								e.getMessage());
				response = webApplicationException.getResponse();
				msg = e.getMessage();
			} finally {
				audit(serviceMethodName, authToken, response, null, msg);
			}

			return response;
		}


		/**
		 * <strong>Delete Journal Citation</strong> operation, deletes the journal citation entry
		 * with the specified ID from the journal citation
		 * database.
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
		 * <td>None</td>
		 * <td>N/A</td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the specified identification number cannot be parsed as an integer.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to delete the specified journal citation entry.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>404 Not Found</td>
		 * <td>If a journal citation entry does not exist in the database with
		 * the specified identification number.</td>
		 * <td>
		 * An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            the HTTP request headers containing the authorization token.
		 *
		 * @param journalCitationId
		 *            the ID of the journal citation to be deleted.
		 *
		 * @return an appropriate HTTP response.
		 */
		@DELETE @Path("/citation/eml/{journalCitationId}")
		public Response deleteJournalCitation (@Context HttpHeaders
		headers, @PathParam("journalCitationId") String journalCitationId)
		{
			AuthToken authToken = null;
			String msg = null;
			Rule.Permission permission = Rule.Permission.write;
			Response response = null;
			final String serviceMethodName = "deleteJournalCitation";

			try {
				if (this.readOnly) {
					throw new ServiceUnavailableException("PASTA is now in read-only mode");
				}

				authToken = getAuthToken(headers);
				String userId = authToken.getUserId();

				// Is user authorized to run the 'deleteJournalCitation' service
				// method?
				boolean serviceMethodAuthorized =
						isServiceMethodAuthorized(serviceMethodName, permission, authToken);

				if (!serviceMethodAuthorized) {
					throw new UnauthorizedException(
							"User " + userId + " is not authorized to execute service method " +
									serviceMethodName);
				}

				Integer id = parseJournalCitationId(journalCitationId);
				DataPackageManager dpm = new DataPackageManager();
				Integer deletedId = dpm.deleteJournalCitation(id, userId);
				msg = String.format("Deleted journal citation with id = '%d'.", deletedId);
				response = Response.ok().build();
			} catch (IllegalArgumentException e) {
				response = WebExceptionFactory.makeBadRequest(e).getResponse();
				msg = e.getMessage();
			} catch (UnauthorizedException e) {
				response = WebExceptionFactory.makeUnauthorized(e).getResponse();
				msg = e.getMessage();
			} catch (ResourceNotFoundException e) {
				response = WebExceptionFactory.makeNotFound(e).getResponse();
				msg = e.getMessage();
			} catch (ResourceDeletedException e) {
				response = WebExceptionFactory.makeGone(e).getResponse();
				msg = e.getMessage();
			} catch (WebApplicationException e) {
				response = e.getResponse();
				msg = e.getMessage();
			} catch (ServiceUnavailableException e) {
				response = WebExceptionFactory.makeServiceUnavailable(e).getResponse();
			} catch (Exception e) {
				WebApplicationException webApplicationException =
						WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
								e.getMessage());
				response = webApplicationException.getResponse();
				msg = e.getMessage();
			} finally {
				audit(serviceMethodName, authToken, response, null, msg);
			}

			return response;
		}


		/**
		 * <strong>Get Journal Citation</strong> operation, returns metadata for a journal citation with the specified ID.
		 *
		 * <h4>Responses:</h4>
		 *
		 * <p>
		 * If the request is successful, the response will contain an XML entity
		 * with the following syntax:
		 * </p>
		 *
		 * <pre>
		 &lt;journalCitation&gt;
		 &lt;journalCitationId&gt;15&lt;/journalCitationId&gt;
		 &lt;principalOwner&gt;uid=LNO,o=LTER,dc=ecoinformatics,dc=org&lt;/principalOwner&gt;
		 &lt;packageId&gt;edi.0.3&lt;/packageId&gt;
		 &lt;articleDoi&gt;10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae&lt;/articleDoi&gt;
		 &lt;articleTitle&gt;Tree Survey in Southern Arizona&lt;/articleTitle&gt;
		 &lt;articleUrl&gt;http://swtrees.com/articles/12345&lt;/articleUrl&gt;
		 &lt;journalTitle&gt;Journal of Southwestern Trees&lt;/journalTitle&gt;
		 &lt;/journalCitation&gt;
		 * </pre>
		 *
		 * <p>
		 * The difference between this response entity and the request entity used
		 * to create the citation is the addition of the <code>journalCitationId</code> and
		 * <code>principalOwner</code> elements, which are determined upon journal citation creation.
		 * </p>
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
		 * <td>The specified journal citation's attributes.</td>
		 * <td><code>application/xml</code></td>
		 * </tr>
		 * <tr>
		 * <td>400 Bad Request</td>
		 * <td>If the specified identification number cannot be parsed as an integer.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>401 Unauthorized</td>
		 * <td>If the requesting user is not authorized to read the specified journal citation.</td>
		 * <td>An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * <tr>
		 * <td>404 Not Found</td>
		 * <td>If a journal citation entry does not exist in the database with
		 * the specified identification number.</td>
		 * <td>
		 * An error message.</td>
		 * <td><code>text/plain</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param headers
		 *            the HTTP request headers containing the authorization token.
		 *
		 * @param journalCitationId
		 *            the ID of the journal citation entry to be returned.
		 *
		 * @return an appropriate HTTP response.
		 */
		@GET @Path("/citation/eml/{journalCitationId}") @Produces(
				value = {MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
		public Response getCitationWithId (@Context HttpHeaders
		headers, @PathParam("journalCitationId") Integer journalCitationId){
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "getCitationWithId";
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
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			String journalCitationsXML =
					dataPackageManager.getCitationWithId(journalCitationId, authToken);

			if (journalCitationsXML != null) {
				responseBuilder = Response.ok(journalCitationsXML.trim());
				response = responseBuilder.build();
			}
			else {
				String message = "An unknown error occurred";
				throw new Exception(message);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		/**
		 *
		 * <strong>List Data Package Citations</strong> operation, specifying the scope,
		 * identifier, and revision values to match in the URI.
		 *
		 * <p>
		 * Return all citations for a given data package series if the "?all" query parameter
		 * is appended to the URL.
		 * </p>
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/citations/eml/edi/1/1</code>
		 * </td>
		 * </tr>
		 <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/citations/eml/edi/1/1?all</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>An XML-formatted list representing journal citations</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 * <pre>
		 &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 &lt;journalCitations&gt;
		 &lt;journalCitation&gt;
		 &lt;journalCitationId&gt;15&lt;/journalCitationId&gt;
		 &lt;packageId&gt;edi.0.3&lt;/packageId&gt;
		 &lt;principalOwner&gt;uid=LNO,o=LTER,dc=ecoinformatics,dc=org&lt;/principalOwner&gt;
		 &lt;dateCreated&gt;2017-12-21T14:28:26.235&lt;/dateCreated&gt;
		 &lt;articleDoi&gt;10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae&lt;/articleDoi&gt;
		 &lt;articleTitle&gt;Tree Survey in Rio Rico, Arizona&lt;/articleTitle&gt;
		 &lt;articleUrl&gt;http://myscience.com/articles/12345&lt;/articleUrl&gt;
		 &lt;journalTitle&gt;Arizona Highways&lt;/journalTitle&gt;
		 &lt;/journalCitation&gt;
		 &lt;/journalCitations&gt;
		 * </pre>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error, such as an illegal
		 * identifier or revision value</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of journal citations for the specified data package</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>404 Not Found</td>
		 * <td align=centerThe specified data package was not found</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the
		 * requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param scope
		 *            The scope of the data package
		 * @param identifier
		 *            The identifier of the data package. A string that represents a whole number.
		 * @param revision
		 *            The revision of the data package. A string that represents a
		 *            whole number, or, the symbolic values "oldest" or "newest".
		 * @return a Response, containing an XML-formatted list of journal citations.
		 */
		@GET @Path("/citations/eml/{scope}/{identifier}/{revision}") @Produces(
				"application/xml") public Response listDataPackageCitations
		(@Context HttpHeaders headers, @PathParam("scope") String
		scope, @PathParam("identifier") Integer identifier, @PathParam("revision") String
		revision, @QueryParam("all") String allParam){
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listDataPackageCitations";
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
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			/*
			 * Handle symbolic revisions such as "newest" and "oldest".
			 */
			if (revision != null) {
				if (revision.equals("newest")) {
					Integer newest = dataPackageManager.getNewestRevision(scope, identifier);
					if (newest != null) {
						revision = newest.toString();
					}
				}
				else if (revision.equals("oldest")) {
					Integer oldest = dataPackageManager.getOldestRevision(scope, identifier);
					if (oldest != null) {
						revision = oldest.toString();
					}
				}
			}

			Integer revisionInt = new Integer(revision);
			String journalCitationsXML =
					dataPackageManager.listDataPackageCitations(scope, identifier, revisionInt,
							authToken, allParam);

			if (journalCitationsXML != null) {
				responseBuilder = Response.ok(journalCitationsXML.trim());
				response = responseBuilder.build();
			}
			else {
				String message = "An unknown error occurred";
				throw new Exception(message);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


	@GET
	@Path("/citedby/eml")
	@Produces("application/xml")
	public Response listDataPackagesCitedBy(
		@Context HttpHeaders headers,
		@QueryParam("articleDoi") String articleDoi
	)
	{
		Response response;
		try {
			AuthToken authToken = getAuthToken(headers);
			String userId = authToken.getUserId();
			// Is user authorized to run the service method?
			Rule.Permission permission = Rule.Permission.read;
			final String serviceMethodName = "listDataPackagesCitedBy";
			boolean serviceMethodAuthorized =
				isServiceMethodAuthorized(serviceMethodName, permission, authToken);
			if (!serviceMethodAuthorized) {
				throw new UnauthorizedException(
					"User " + userId + " is not authorized to execute service method " + serviceMethodName);
			}
			DataPackageManager dataPackageManager = new DataPackageManager();
			String packageListXml = dataPackageManager.listDataPackagesCitedBy(articleDoi, authToken);
			ResponseBuilder responseBuilder = Response.ok(packageListXml);
			response = responseBuilder.build();
		} catch (IllegalArgumentException e) {
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			WebApplicationException webApplicationException =
				WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
			response = webApplicationException.getResponse();
		}
		response = stampHeader(response);
		return response;
	}

	/**
		 *
		 * <strong>List Principal Owner Citations</strong> operation, specifying the principal owner of the citations
		 * to be listed.
		 *
		 * <h4>Requests:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Request</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>none</td>
		 * <td align=center>none</td>
		 * <td align=center>
		 * <code>curl -i -X GET https://pasta.lternet.edu/package/citations/eml/principalOwner</code>
		 * </td>
		 * </tr>
		 * </table>
		 *
		 * <h4>Responses:</h4>
		 * <table border="1" cellspacing="0" cellpadding="3">
		 * <tr>
		 * <th><b>Status</b></th>
		 * <th><b>Reason</b></th>
		 * <th><b>Message Body</b></th>
		 * <th><b>MIME type</b></th>
		 * <th><b>Sample Message Body</b></th>
		 * </tr>
		 * <tr>
		 * <td align=center>200 OK</td>
		 * <td align=center>The list request was successful</td>
		 * <td align=center>An XML-formatted list representing journal citations created by the specified
		 * principal owner</td>
		 * <td align=center><code>application/xml</code></td>
		 * <td>
		 * <pre>
		 &lt;?xml version="1.0" encoding="UTF-8"?&gt;
		 &lt;journalCitations&gt;
		 &lt;journalCitation&gt;
		 &lt;journalCitationId&gt;15&lt;/journalCitationId&gt;
		 &lt;packageId&gt;edi.0.3&lt;/packageId&gt;
		 &lt;principalOwner&gt;uid=LNO,o=LTER,dc=ecoinformatics,dc=org&lt;/principalOwner&gt;
		 &lt;dateCreated&gt;2017-12-21T14:28:26.235&lt;/dateCreated&gt;
		 &lt;articleDoi&gt;10.5072/FK2/06dccc7b0cb2a2d5f6fef62cb4b36dae&lt;/articleDoi&gt;
		 &lt;articleTitle&gt;Tree Survey in Rio Rico, Arizona&lt;/articleTitle&gt;
		 &lt;articleUrl&gt;http://myscience.com/articles/12345&lt;/articleUrl&gt;
		 &lt;journalTitle&gt;Arizona Highways&lt;/journalTitle&gt;
		 &lt;/journalCitation&gt;
		 &lt;/journalCitations&gt;
		 * </pre>
		 * </td>
		 * </tr>
		 * <tr>
		 * <td align=center>400 Bad Request</td>
		 * <td align=center>The request contains an error</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>401 Unauthorized</td>
		 * <td align=center>The requesting user is not authorized to access a list
		 * of journal citations for the specified principal owner</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>405 Method Not Allowed</td>
		 * <td align=center>The specified HTTP method is not allowed for the requested resource</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * <tr>
		 * <td align=center>500 Internal Server Error</td>
		 * <td align=center>The server encountered an unexpected condition which
		 * prevented it from fulfilling the request</td>
		 * <td align=center>An error message</td>
		 * <td align=center><code>text/plain</code></td>
		 * <td align=center><code>Error message</code></td>
		 * </tr>
		 * </table>
		 *
		 * @param principalOwner
		 *            The principal owner of the journal citations
		 * @return a Response, containing an XML-formatted list of journal citations.
		 */
		@GET @Path("/citations/eml/{principalOwner}") @Produces("application/xml")
		public Response listPrincipalOwnerCitations (@Context HttpHeaders
		headers, @PathParam("principalOwner") String principalOwner){
		ResponseBuilder responseBuilder = null;
		Response response = null;
		final String serviceMethodName = "listPrincipalOwnerCitations";
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
						"User " + userId + " is not authorized to execute service method " +
								serviceMethodName);
			}

			DataPackageManager dataPackageManager = new DataPackageManager();

			String journalCitationsXML =
					dataPackageManager.listPrincipalOwnerCitations(principalOwner, authToken);

			if (journalCitationsXML != null) {
				responseBuilder = Response.ok(journalCitationsXML.trim());
				response = responseBuilder.build();
			}
			else {
				String message = "An unknown error occurred";
				throw new Exception(message);
			}
		} catch (IllegalArgumentException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeBadRequest(e).getResponse();
		} catch (ResourceNotFoundException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeNotFound(e).getResponse();
		} catch (UnauthorizedException e) {
			entryText = e.getMessage();
			response = WebExceptionFactory.makeUnauthorized(e).getResponse();
		} catch (UserErrorException e) {
			entryText = e.getMessage();
			response = WebResponseFactory.makeBadRequest(e);
		} catch (Exception e) {
			entryText = e.getMessage();
			WebApplicationException webApplicationException =
					WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e,
							e.getMessage());
			response = webApplicationException.getResponse();
		}

		// audit(serviceMethodName, authToken, response, resourceId, entryText);

		response = stampHeader(response);
		return response;
	}


		private Integer parseJournalCitationId (String s){
		try {
			return new Integer(s);
		} catch (NumberFormatException e) {
			String err = String.format(
					"The provided journal citation ID '%s' cannot be parsed as an integer.", s);
			throw new IllegalArgumentException(err);
		}
	}

	}
