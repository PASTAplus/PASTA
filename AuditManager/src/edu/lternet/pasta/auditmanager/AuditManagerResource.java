/*
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
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

package edu.lternet.pasta.auditmanager;

import java.io.File;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.sun.jersey.api.client.ClientResponse.Status;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.MethodNameUtility;
import edu.lternet.pasta.common.PastaWebService;
import edu.lternet.pasta.common.QueryString;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.WebExceptionFactory;
import edu.lternet.pasta.common.security.access.AccessControllerFactory;
import edu.lternet.pasta.common.security.access.JaxRsHttpAccessController;
import edu.lternet.pasta.common.security.access.UnauthorizedException;


/**
 * <p>
 * The Audit Manager web service allows services to <b>create</b>, and users to
 * <b>read</b> subscriptions to PASTA "logs".
 * </p>
 *
 * <p>
 * Please refer to the pasta-log-entry.xsd for more information as to the
 * content required to submit a log entry.
 * </p>
 * <p>
 * A log entry will always consist of 5 requried attributes.
 * <ul>
 * <li><b>timestamp</b>: roughly indicating the time of the log entry, in ISO
 * 8601 format.</li>
 * <li><b>event-text</b>: a brief explanation of the log entry.</li>
 * <li><b>service</b>: name of the originating service.</li>
 * <li><b>category</b>: the status level of severity.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Other optional attributes are of the following.
 * <ul>
 * <li><b>user-token</b>: the token of user pertaining to the log entry.</li>
 * <li><b>http-status-code</b>: the HTTP response status code from the event
 * related to the log entry.</li>
 * <li><b>service-method</b>: the originating service method pertaining to the
 * creation of the log entry</li>
 * <li><b>resource-id</b>: any available resource IDs related to events
 * surrounding this log entry</li>
 * </ul>
 * </p>
 *
 * @webservicename Audit Manager
 * @baseurl https://audit.lternet.edu/audit
 */
@Path("")
public class AuditManagerResource extends PastaWebService
{
    /*
     * Class variables
     */
  
  // The following provided query parameter keys are not understood by this web service:
  // fromtime, responseStatus, totime.

    // Query parameter for time at which entry was created (ISO 8601 format)
    public static final String AT_TIME = "time";
    // Query parameter for range in time beginning at time (ISO 8601 format)
    public static final String FROM_TIME = "fromTime";
    // Query parameter for range in time ending at time (ISO 8601 format)
    public static final String TO_TIME = "toTime";
    // Query parameter for category
    public static final String CATEGORY = "category";
    // Query parameter for service
    public static final String SERVICE = "service";
    // Query parameter for service
    public static final String SERVICEMETHOD = "serviceMethod";
    // Query parameter for user
    public static final String USER = "user";
    // Query parameter for groups
    public static final String GROUP = "group";
    // Query parameter for authSystem
    public static final String AUTHSYSTEM = "authSystem";
    // Query parameter for status
    public static final String STATUS_CODE = "status";
    // Query parameter for user
    public static final String RESOURCE_ID = "resourceId";
    
    //Query parameter for record limit
    public static final String LIMIT = "limit";
    
    private static Logger logger = Logger.getLogger(AuditManagerResource.class);
    // Set of valid query parameters
    public static final Set<String> VALID_QUERY_KEYS;
    public static final Set<String> VALID_RECENT_UPLOADS_KEYS;
    private static final String SERVICE_OWNER = "pasta";

    static {
        Set<String> set = new TreeSet<String>();
        set.add(AT_TIME);
        set.add(TO_TIME);
        set.add(FROM_TIME);
        set.add(CATEGORY);
        set.add(SERVICE);
        set.add(SERVICEMETHOD);
        set.add(USER);
        set.add(GROUP);
        set.add(AUTHSYSTEM);
        set.add(STATUS_CODE);
        set.add(RESOURCE_ID);
        set.add(LIMIT);
        VALID_QUERY_KEYS = Collections.unmodifiableSet(set);
    }
    
    static {
        Set<String> set = new TreeSet<String>();
        set.add(FROM_TIME);
        set.add(SERVICEMETHOD);
        set.add(LIMIT);
        VALID_RECENT_UPLOADS_KEYS = Collections.unmodifiableSet(set);
    }
    
    
    /*
     * Instance variables
     */
    
    
    /*
     * Constructors
     */
    
    
    /*
     * Class methods
     */
    

    /*
     * Instance methods
     */
    
    private void assertAuthorizedToRead(HttpHeaders headers, String serviceMethod) {

        String acr = AccessControlRuleFactory.getServiceAcr(serviceMethod);
        JaxRsHttpAccessController controller =
            AccessControllerFactory.getDefaultHttpAccessController();

        if (controller.canRead(headers, acr, SERVICE_OWNER)) {
            return;
        }
        String s = "This request is not authorized to retreive entries from " +
                   "the audit manager. Please check your authorization " + 
                   "credentials.";

        throw WebExceptionFactory.makeUnauthorized(s);
    }


    private void assertAuthorizedToWrite(HttpHeaders headers, String serviceMethod) {

        String acr = AccessControlRuleFactory.getServiceAcr(serviceMethod);
        JaxRsHttpAccessController controller =
                AccessControllerFactory.getDefaultHttpAccessController();

        if (controller.canWrite(headers, acr, SERVICE_OWNER)) {
            return;
        }

        String s = "This request is not authorized to submit entries to the " +
                   "audit manager. Please check your authorization " + 
                   "credentials.";

        throw WebExceptionFactory.makeUnauthorized(s);
    }


    /**
	 * <strong>Create Audit Record</strong> operation, 
	 * creates a new logged entry in the Audit Manager's logging database.
	 * 
     * <h4>Request entity:</h4>
     *
     * <p>
     * The request entity should be an XML document (MIME type
     * <code>application/xml</code>) that is demonstrated below.
     * </p>
     *
     * <pre>
     *    &lt;log-entry timestamp=<em>timestamp</em>&gt;
     *       &lt;service&gt;<em>service</em>&lt;/service&gt;
     *       &lt;event-text&gt;<em>event text</em>&lt;/event-text&gt;
     *       &lt;category&gt;<em>error, warn, info, or debug&lt;/category&gt;
     *       &lt;user-token&gt;<em>user-token</em>&lt;/user-token&gt;
     *       &lt;http-status-code&gt;<em>HTTP response status code</em>&lt;/http-status-code&gt;
     *       &lt;service-method&gt;<em>Service Method</em>&lt;/service-method&gt;
     *       &lt;resource-id&gt;<em>Resource ID</em>&lt;/resource-id&gt;
     *    &lt;/log-entry&gt;
     * </pre>
     *
     * <p>
     * The service, event-text, category elements and timestamp attribute are mandatory.
     * </p>
     *
     * <h4>Responses:</h4>
     *
     * <table border="1" cellspacing="0" cellpadding="3">
     *   <tr>
     *     <td><b>Status</b></td>
     *     <td><b>Reason</b></td>
     *     <td><b>Entity</b></td>
     *     <td><b>MIME type</b></td>
     *   </tr>
     *   <tr>
     *     <td>201 Created</td>
     *     <td>If the request to create an audit entry was successful.</td>
     *     <td>None, but the <code>Location</code> header will contain a URL that
     *         references the new subscription.</td>
     *     <td>N/A</td>
     *   </tr>
     *   <tr>
     *     <td>400 Bad Request</td>
     *     <td>If the request entity contains an error, such as improperly formatted XML.</td>
     *     <td>An error message.</td>
     *     <td><code>text/plain</code></td>
     *   </tr>
     *   <tr>
     *     <td>401 Unauthorized</td>
     *     <td>If the requesting user is not authorized to create log entries.</td>
     *     <td>An error message.</td>
     *     <td><code>text/plain</code></td>
     *   </tr>
     *   <tr>
     *     <td>500 Internal Server Error</td>
     *     <td>The server encountered an unexpected condition which prevented 
     *     it from fulfilling the request. For example, a SQL error occurred, 
     *     or an unexpected condition was encountered.</td>
     *     <td>An error message</td>
     *     <td><code>text/plain</code></td>
     *   </tr>
     * </table>
     *
     * @param headers
     *            the HTTP request headers containing the authorization token.
     * @param logEntry
     *            the POST request's body, of XML representing a log entry.
     * @return an appropriate HTTP response.
     */
    @POST
    public Response create(@Context HttpHeaders headers, String auditEntry) {
      try {
        assertAuthorizedToWrite(headers, MethodNameUtility.methodName());    
        Properties properties = ConfigurationListener.getProperties();
        AuditManager auditManager = new AuditManager(properties);      
        int auditId = auditManager.create(auditEntry);
        String uriString = String.format("http://audit.lternet.edu/audit/%d", auditId);
        URI uri = URI.create(uriString);
        return Response.created(uri).build();
      }
      catch (UnauthorizedException e) {
        return WebExceptionFactory.makeUnauthorized(e).getResponse();
      }
      catch (WebApplicationException e) {
        return e.getResponse();
      }
      catch (Exception e) {
        WebApplicationException webApplicationException = 
          WebExceptionFactory.make(Response.Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
        return webApplicationException.getResponse();
      }
      finally {
      }
    }

    
    /**
     * Returns the API documentation for the Audit Manager.
     */
    @Override
    public File getApiDocument() {
        return ConfigurationListener.getApiDocument();
    }

    
    /**
	 * <strong>Get Audit Record</strong> operation, retrieves a single audit record 
	 * based on the audit identifier value specified in the path.
     *
     * <h4>Responses:</h4>
     *
     * <p>
     * If the request is successful, the response will contain plain text.
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
     * <td><code>text/plain</code></td>
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
     * <td>If the requested log entry does not exist.</td>
     * <td>An error message.</td>
     * <td><code>text/plain</code></td>
     * </tr>
     * </table>
     *
     * @param headers
     *             the HTTP request headers containing the authorization token.
     * @param uriInfo
     *             the POST request's body, of XML representing a log entry.
     * @return an appropriate HTTP response.
     */
    @GET
    @Path("report/{oid}")
    public Response getAuditRecord(@Context HttpHeaders headers,
                                   @PathParam(value = "oid") int oid) {
        try {
            Properties properties = ConfigurationListener.getProperties();
            assertAuthorizedToRead(headers, MethodNameUtility.methodName());
            AuditManager auditManager = new AuditManager(properties);
            Integer oidInteger = new Integer(oid);
            String oidString = oidInteger.toString();
            List<String> oidList = new ArrayList<String>();
            oidList.add(oidString);            
            Map<String, List<String>> queryParams = new HashMap<String, List<String>>();
            queryParams.put("oid", oidList);
            String xmlString = auditManager.getAuditRecords(queryParams);
            if (xmlString.length() == (AuditManager.AUDIT_OPENING_TAG.length() + 
            		                   AuditManager.AUDIT_CLOSING_TAG.length())) { 
              throw new ResourceNotFoundException(String.format("Oid %d does not exist.", oid));
            }
            return Response.ok(xmlString).build();
        }
        catch (ClassNotFoundException e) {
          return WebExceptionFactory.make(Status.INTERNAL_SERVER_ERROR, e, e.getMessage()).getResponse();
        }
        catch (SQLException e) {
          return WebExceptionFactory.make(Status.INTERNAL_SERVER_ERROR, e, e.getMessage()).getResponse();
        }
        catch (UnauthorizedException e) {
            return WebExceptionFactory.makeUnauthorized(e).getResponse();
        }
        catch (ResourceNotFoundException e) {
            return WebExceptionFactory.makeNotFound(e).getResponse();
        }
        catch (WebApplicationException e) {
            return e.getResponse();
        }
        catch (IllegalStateException e) {
            return WebExceptionFactory.makeBadRequest(e).getResponse();
        }
    }


    /**
     * <strong>Get Audit Report</strong> operation, gets a list of zero or more 
     * audit records matching the query parameters as specified in the request.
     *
     * <h4>Query Parameters:</h4>
     * <table border="1" cellspacing="0" celpadding="3">
     *   <tr>
     *     <td><b>Parameter</b></td>
     *     <td><b>Value Constraints</b></td>
     *   </tr>
     *   <tr>
     *     <td>category</td>
     *     <td>debug, info, error, warn</td>
     *   </tr>
     *   <tr>
     *     <td>service</td>
     *     <td>Any of the PASTA services.</td>
     *   </tr>
     *   <tr>
     *     <td>serviceMethod</td>
     *     <td>Any of the PASTA service Resource class JAX-RS methods.</td>
     *   </tr>
     *   <tr>
     *     <td>user</td>
     *     <td>Any user.</td>
     *   </tr>
     *   <tr>
     *     <td>group</td>
     *     <td>Any group.</td>
     *   </tr>
     *   <tr>
     *     <td>authSystem</td>
     *     <td>A valid auth system identifier.</td>
     *   </tr>
     *   <tr>
     *     <td>status</td>
     *     <td>A valid HTTP Response Code.</td>
     *   </tr>
     *   <tr>
     *     <td>resourceId</td>
     *     <td>A Resource Id.</td>
     *   </tr>
     *   <tr>
     *     <td>time</td>
     *     <td>An ISO8601 timestamp</td>
     *   </tr>
     *   <tr>
     *     <td>fromTime</td>
     *     <td>An ISO8601 timestamp</td>
     *   </tr>
     *   <tr>
     *     <td>toTime</td>
     *     <td>An ISO8601 timestamp</td>
     *   </tr>
     *   <tr>
     *     <td>limit</td>
     *     <td>A positive whole number</td>
     *   </tr>
     * </table>
     * <br/>
     * The query parameters <code>fromTime</code> and optionally
     * <code>toTime</code> should be used to indicate a time span. When
     * <code>toTime</code> is absent, the report will consist of all matching
     * records up to the current time. Either of these parameters may only be
     * used once.
     * <br/>
     * The query parameter <code>time</code> may not be used in conjunction
     * with <code>fromTime</code>. All other parameters may be used multiple
     * times.
     * <br/>
     * The query parameter <code>limit</code> sets an upper limit on the number
     * of audit records returned. For example, "limit=1000".
     *
     * <h4>Responses:</h4>
     *
     * <p>If the request is successful, the response will contain XML text.</p>
     *
     * <table border="1" cellspacing="0" cellpadding="3">
     *   <tr>
     *     <td><b>Status</b></td>
     *     <td><b>Reason</b></td>
     *     <td><b>Entity</b></td>
     *     <td><b>MIME type</b></td>
     *   </tr>
     *   <tr>
     *     <td>200 OK</td>
     *     <td>If the request was successful.</td>
     *     <td>The specified subscription's attributes.</td>
     *     <td><code>application/xml</code></td>
     *   </tr>
     *   <tr>
     *     <td>400 Bad Request</td>
     *     <td>If the specified identification number cannot be parsed as an integer.</td>
     *     <td>An error message.</td>
     *     <td><code>text/plain</code></td>
     *   </tr>
     *   <tr>
     *     <td>401 Unauthorized</td>
     *     <td>If the requesting user is not authorized to read the specified subscription.</td>
     *     <td>An error message.</td>
     *     <td><code>text/plain</code></td>
     *   </tr>
     * </table>
     *
     * @param headers  the HTTP request headers containing the authorization token.
     * @param uriInfo  the POST request's body, of XML representing a log entry.
     * @return an appropriate HTTP response.
     */
    @GET
    @Path("report")
    public Response getAuditRecords(@Context HttpHeaders headers,
                                    @Context UriInfo uriInfo) {
		ResponseBuilder responseBuilder = null;
		Response response = null;

		try {
            Properties properties = ConfigurationListener.getProperties();
            assertAuthorizedToRead(headers, MethodNameUtility.methodName());
            AuditManager auditManager = new AuditManager(properties);
            QueryString queryString = new QueryString(uriInfo);
            queryString.checkForIllegalKeys(VALID_QUERY_KEYS);
            Map<String, List<String>> queryParams = queryString.getParams();
            File xmlFile = auditManager.getAuditRecordsFile(queryParams);
			if (xmlFile != null && xmlFile.exists()) {
				Long size = FileUtils.sizeOf(xmlFile);
				responseBuilder = Response.ok(xmlFile, MediaType.APPLICATION_XML);
				responseBuilder.header("Content-Length", size.toString());
				response = responseBuilder.build();
				String logMessage = String.format("getAuditRecords service method finished processing: returning %d bytes", size);
				logger.warn(logMessage);
			}
			else {
				ResourceNotFoundException e = new ResourceNotFoundException(
				    String.format("Unable to process audit query with query parameters: %s", queryParams));
				throw (e);
			}
            return response;
        }
        catch (ClassNotFoundException e) {
          return WebExceptionFactory.make(Status.INTERNAL_SERVER_ERROR, e, e.getMessage()).getResponse();
        }
		catch (ResourceNotFoundException e) {
		  return WebExceptionFactory.makeNotFound(e).getResponse();
		}
        catch (SQLException e) {
          return WebExceptionFactory.make(Status.INTERNAL_SERVER_ERROR, e, e.getMessage()).getResponse();
        }
        catch (UnauthorizedException e) {
            return WebExceptionFactory.makeUnauthorized(e).getResponse();
        }
        catch (WebApplicationException e) {
            return e.getResponse();
        }
        catch (IllegalStateException e) {
            return WebExceptionFactory.makeBadRequest(e).getResponse();
        }
    }
    
    
    /**
     * <strong>Get Recent Uploads</strong> operation, gets a list of zero or more audit 
     * records of either recently inserted or recently updated data packages, as specified
     * in the request.
     *
     * <h4>Query Parameters:</h4>
     * <table border="1" cellspacing="0" celpadding="3">
     *   <tr>
     *     <td><b>Parameter</b></td>
     *     <td><b>Value Constraints</b></td>
     *   </tr>
     *   <tr>
     *     <td>serviceMethod</td>
     *     <td>Either of &quot;createDataPackage&quot; or &quot;updateDataPackage&quot;
     *     </td>
     *   </tr>
     *   <tr>
     *     <td>fromTime</td>
     *     <td>An ISO8601 timestamp</td>
     *   </tr>
     *   <tr>
     *     <td>limit</td>
     *     <td>A positive whole number</td>
     *   </tr>
     * </table>
     * <br/>
     * The query parameter <code>serviceMethod</code> should have the value
     * &quot;createDataPackage&quot; (to retrieve recent inserts) or &quot;updateDataPackage&quot;
     * (to retrieve recent updates)
     * <br/>
     * The query parameter <code>fromTime</code> is used to specify the
     * date/time in the past that represents the oldest audit records that should be
     * returned. Data packages uploaded prior to that time are not considered
     * recent uploads and are thus filtered from the query results.
     * <br/>
     * The query parameter <code>limit</code> sets an upper limit on the number
     * of audit records returned. For example, "limit=3".
     *
     * <h4>Responses:</h4>
     *
     * <p>If the request is successful, the response will contain XML text.</p>
     *
     * <table border="1" cellspacing="0" cellpadding="3">
     *   <tr>
     *     <td><b>Status</b></td>
     *     <td><b>Reason</b></td>
     *     <td><b>Entity</b></td>
     *     <td><b>MIME type</b></td>
     *   </tr>
     *   <tr>
     *     <td>200 OK</td>
     *     <td>If the request was successful.</td>
     *     <td>The specified subscription's attributes.</td>
     *     <td><code>application/xml</code></td>
     *   </tr>
     *   <tr>
     *     <td>400 Bad Request</td>
     *     <td>If the specified identification number cannot be parsed as an integer.</td>
     *     <td>An error message.</td>
     *     <td><code>text/plain</code></td>
     *   </tr>
     *   <tr>
     *     <td>401 Unauthorized</td>
     *     <td>If the requesting user is not authorized to read the specified subscription.</td>
     *     <td>An error message.</td>
     *     <td><code>text/plain</code></td>
     *   </tr>
     * </table>
     *
     * @param headers  the HTTP request headers containing the authorization token.
     * @param uriInfo  the POST request's body, of XML representing a log entry.
     * @return an appropriate HTTP response.
     */
    @GET
    @Path("recent-uploads")
    public Response getRecentUploads(@Context HttpHeaders headers,
                                     @Context UriInfo uriInfo) {
		try {
            Properties properties = ConfigurationListener.getProperties();
            assertAuthorizedToRead(headers, MethodNameUtility.methodName());
            AuditManager auditManager = new AuditManager(properties);
            QueryString queryString = new QueryString(uriInfo);
            queryString.checkForIllegalKeys(VALID_RECENT_UPLOADS_KEYS);
            Map<String, List<String>> queryParams = queryString.getParams();
            String xmlString = auditManager.getRecentUploads(queryParams);
            return Response.ok(xmlString).build();
        }
        catch (ClassNotFoundException e) {
          return WebExceptionFactory.make(Status.INTERNAL_SERVER_ERROR, e, e.getMessage()).getResponse();
        }
		catch (ResourceNotFoundException e) {
		  return WebExceptionFactory.makeNotFound(e).getResponse();
		}
        catch (SQLException e) {
          return WebExceptionFactory.make(Status.INTERNAL_SERVER_ERROR, e, e.getMessage()).getResponse();
        }
        catch (UnauthorizedException e) {
            return WebExceptionFactory.makeUnauthorized(e).getResponse();
        }
        catch (WebApplicationException e) {
            return e.getResponse();
        }
        catch (IllegalStateException e) {
            return WebExceptionFactory.makeBadRequest(e).getResponse();
        }
    }
    
    
    /**
     * Returns the tutorial document for the Audit Manager.
     */
    @Override
    public File getTutorialDocument() {
        return ConfigurationListener.getTutorialDocument();
    }


    /**
     * Returns the Audit Manager's version, such as {@code auditmanager-0.1}.
     */
    @Override
    public String getVersionString() {
        return ConfigurationListener.getWebServiceVersion();
    }

}
