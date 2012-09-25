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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.Consumes;
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
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;

import pasta.pasta_lternet_edu.log_entry_0.LogEntry;
import edu.lternet.pasta.auditmanager.LogItem.LogItemBuilder;
import edu.lternet.pasta.common.MethodNameUtility;
import edu.lternet.pasta.common.PastaWebService;
import edu.lternet.pasta.common.QueryString;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.WebExceptionFactory;
import edu.lternet.pasta.common.security.access.AccessControllerFactory;
import edu.lternet.pasta.common.security.access.JaxRsHttpAccessController;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;

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
public class AuditServiceResource extends PastaWebService
{
    /**
     * Query parameter for time at which entry was created (ISO 8601 format).
     */
    public static final String AT_TIME = "time";

    /**
     * Query parameter for range in time beginning at time (ISO 8601 format).
     */
    public static final String FROM_TIME = "fromTime";

    /**
     * Query parameter for range in time ending at time (ISO 8601 format).
     */
    public static final String TO_TIME = "toTime";

    /**
     * Query parameter for category.
     */
    public static final String CATEGORY = "category";

    /**
     * Query parameter for service.
     */
    public static final String SERVICE = "service";

    /**
     * Query parameter for service.
     */
    public static final String SERVICEMETHOD = "serviceMethod";

    /**
     * Query parameter for user.
     */
    public static final String USER = "user";

    /**
     * Query parameter for groups.
     */
    public static final String GROUP = "group";

    /**
     * Query parameter for authSystem.
     */
    public static final String AUTHSYSTEM = "authSystem";

    /**
     * Query parameter for status.
     */
    public static final String STATUS_CODE = "status";

    /**
     * Query parameter for user.
     */
    public static final String RESOURCE_ID = "resourceId";

    private static Logger logger =
            Logger.getLogger(AuditServiceResource.class);

    private static final String REPORT_PREFIX = "report";
    /**
     * Valid query parameters.
     */
    public static final Set<String> VALID_QUERY_KEYS;

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
        VALID_QUERY_KEYS = Collections.unmodifiableSet(set);
    }

    private final EntityManagerFactory entityManagerFactory;

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
     * Constructs a new audit manager web service using the entity manager
     * factory specific in {@link ConfigurationListener}.
     *
     * @see ConfigurationListener#getPersistenceUnit()
     */
    public AuditServiceResource() {
        String persistenceUnit = ConfigurationListener.getPersistenceUnit();
        entityManagerFactory =
            Persistence.createEntityManagerFactory(persistenceUnit);
    }

    /**
     * Constructs a new audit manager web service using the provided entity
     * manager.
     *
     * @param emf
     *          the entity manager factory for persisting log entries.
     */
    public AuditServiceResource(EntityManagerFactory emf) {
        entityManagerFactory = emf;
    }

    /**
     * Closes this resource's entity manager factory and all entity managers
     * created from it.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            // also closes all entity managers from this factory
            entityManagerFactory.close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Creates a new logged entry in the Audit Manager's logging database.
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
     * XML.</td>
     * <td>An error message.</td>
     * <td><code>text/plain</code></td>
     * </tr>
     * <tr>
     * <td>401 Unauthorized</td>
     * <td>If the requesting user is not authorized to create log entries.</td>
     * <td>An error message.</td>
     * <td><code>text/plain</code></td>
     * </tr>
     * </table>
     *
     * @param headers
     *            the HTTP request headers containing the authorization token.
     * @param logEntry
     *            the POST request's body, of XML representing a log entry.
     * @return an appropriate HTTP response.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response create(@Context HttpHeaders headers, JAXBElement<LogEntry> logEntry) {

        LogService service = null;

        try {
            assertAuthorizedToWrite(headers, MethodNameUtility.methodName());

            service = getLogService();
            LogEntry entry = logEntry.getValue();
            int oid = service.create(entry);
            URI uri = URI.create("http://audit.lternet.edu/audit" + "/" + Integer.toString(oid));
            return Response.created(uri).build();
        }
        catch (UnauthorizedException e) {
            return WebExceptionFactory.makeUnauthorized(e).getResponse();
        }
        catch (WebApplicationException e) {
            return e.getResponse();
        }
        finally {
            if (service != null) {
                service.close();
            }
        }

    }

    /**
     * Returns the OIDs of the log entries requested by query parameters.
     * 
     * <h4>Query Parameters:</h4>
     * <table border="1" cellspacing="0" celpadding="3">
     * <tr>
     * <td><b>Parameter</b></td>
     * <td><b>Value Constraints</b></td>
     * </tr>
     * <tr>
     * <td>category</td>
     * <td>debug, info, error, warn</td>
     * </tr>
     * <tr>
     * <td>service</td>
     * <td>Any of the PASTA services.</td>
     * </tr>
     * <tr>
     * <td>serviceMethod</td>
     * <td>Any of the PASTA service Resource class JAX-RS methods.</td>
     * </tr>
     * <tr>
     * <td>user</td>
     * <td>Any user.</td>
     * </tr>
     * <tr>
     * <td>group</td>
     * <td>Any group.</td>
     * </tr>
     * <tr>
     * <td>authSystem</td>
     * <td>A valid auth system identifier.</td>
     * </tr>
     * <tr>
     * <td>status</td>
     * <td>A valid HTTP Response Code.</td>
     * </tr>
     * <tr>
     * <td>resourceId</td>
     * <td>A Resource Id.</td>
     * </tr>
     * <tr>
     * <td>time</td>
     * <td>An ISO8601 timestamp</td>
     * </tr>
     * <tr>
     * <td>fromTime</td>
     * <td>An ISO8601 timestamp</td>
     * </tr>
     * <tr>
     * <td>toTime</td>
     * <td>An ISO8601 timestamp</td>
     * </tr>
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
     *            the HTTP request headers containing the authorization token.
     * @param uriInfo
     *            the POST request's body, of XML representing a log entry.
     * @return an appropriate HTTP response.
     */
    @GET
    @Path("oids")
    public Response getOids(@Context HttpHeaders headers,
                            @Context UriInfo uriInfo) {

        LogService service = null;
        try {
            assertAuthorizedToRead(headers, MethodNameUtility.methodName());
            AuthToken token =
                    AuthTokenFactory.makeAuthToken(headers.getCookies());

            LogItemBuilder query = makeQuery(uriInfo);
            service = getLogService();
            List<Integer> oids = service.getOids(query, token);
            StringBuilder sb = new StringBuilder();
            for (Integer s : oids) sb.append(s + "\n");

            return Response.ok(sb.toString()).build();
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
        finally {
            if (service != null) {
                service.close();
            }
        }
    }

    /**
     * Returns the content of the log entries requested by query parameters.
     *
     * <h4>Query Parameters:</h4>
     * <table border="1" cellspacing="0" celpadding="3">
     * <tr>
     * <td><b>Parameter</b></td>
     * <td><b>Value Constraints</b></td>
     * </tr>
     * <tr>
     * <td>category</td>
     * <td>debug, info, error, warn</td>
     * </tr>
     * <tr>
     * <td>service</td>
     * <td>Any of the PASTA services.</td>
     * </tr>
     * <tr>
     * <td>serviceMethod</td>
     * <td>Any of the PASTA service Resource class JAX-RS methods.</td>
     * </tr>
     * <tr>
     * <td>user</td>
     * <td>Any user.</td>
     * </tr>
     * <tr>
     * <td>group</td>
     * <td>Any group.</td>
     * </tr>
     * <tr>
     * <td>authSystem</td>
     * <td>A valid auth system identifier.</td>
     * </tr>
     * <tr>
     * <td>status</td>
     * <td>A valid HTTP Response Code.</td>
     * </tr>
     * <tr>
     * <td>resourceId</td>
     * <td>A Resource Id.</td>
     * </tr>
     * <tr>
     * <td>time</td>
     * <td>An ISO8601 timestamp</td>
     * </tr>
     * <tr>
     * <td>fromTime</td>
     * <td>An ISO8601 timestamp</td>
     * </tr>
     * <tr>
     * <td>toTime</td>
     * <td>An ISO8601 timestamp</td>
     * </tr>
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
    @Path(REPORT_PREFIX)
    public Response getReports(@Context HttpHeaders headers,
                               @Context UriInfo uriInfo) {

        LogService service = null;
        try {
            assertAuthorizedToRead(headers, MethodNameUtility.methodName());
            AuthToken token =
                    AuthTokenFactory.makeAuthToken(headers.getCookies());

            LogItemBuilder query = makeQuery(uriInfo);
            service = getLogService();
            String logEntity = service.getOidsContent(query, token);
            return Response.ok(logEntity).build();
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
        finally {
            if (service != null) {
                service.close();
            }
        }
    }

    /**
     * Returns the content of the log entries requested by OID.
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
    @Path(REPORT_PREFIX + "/{oid}")
    public Response getReport(@Context HttpHeaders headers,
                              @PathParam(value = "oid") int oid) {

        LogService service = null;
        try {
            assertAuthorizedToRead(headers, MethodNameUtility.methodName());
            AuthToken token =
                    AuthTokenFactory.makeAuthToken(headers.getCookies());

            service = getLogService();
            String logEntry = service.get(new Integer(oid), token);
            return Response.ok(logEntry).build();
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
        finally {
            if (service != null) {
                service.close();
            }
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

    private LogService getLogService() {

        EntityManager em = entityManagerFactory.createEntityManager();
        return new LogService(em);
    }

    private LogItemBuilder makeQuery(UriInfo uriInfo) {

        LogItemBuilder query = new LogItemBuilder();
        if (uriInfo == null) return query;

        QueryString queryString = new QueryString(uriInfo);
        queryString.checkForIllegalKeys(VALID_QUERY_KEYS);

        List<String> users = queryString.getOptionalValues(USER);
        List<String> groups = queryString.getOptionalValues(GROUP);
        List<String> authSystems = queryString.getOptionalValues(AUTHSYSTEM);
        List<String> categories = queryString.getOptionalValues(CATEGORY);
        List<String> services = queryString.getOptionalValues(SERVICE);
        List<String> serviceMethods = queryString.getOptionalValues(SERVICEMETHOD);
        List<String> strTimes = queryString.getOptionalValues(AT_TIME);
        List<String> statusCodes = queryString.getOptionalValues(STATUS_CODE);
        List<String> resourceIds = queryString.getOptionalValues(RESOURCE_ID);

        String strFromTime = queryString.getOptionalValue(FROM_TIME);
        String strToTime = queryString.getOptionalValue(TO_TIME);

        if (strTimes != null && strFromTime != null) {
            String error = "Invalid Query Parameters: " + FROM_TIME + " and " +
                           AT_TIME + " are both set.";
            throw new IllegalStateException(error);
        }

        /*
         * If a to-time is specified but a from-time is not,
         * set a default from-time value.
         */
        if (strToTime != null && strFromTime == null) {
            strFromTime = "2000-01-01T00:00:00";
        }
        
        if (users != null) query.setUsers(users);
        if (groups != null) query.setGroups(groups);
        if (authSystems != null) query.setAuthSystem(authSystems);
        if (categories != null) query.setCategories(categories);
        if (services != null) query.setServices(services);
        if (serviceMethods != null) query.setServiceMethods(serviceMethods);
        if (strTimes != null) query.setAtTimes(strToDate(strTimes));
        if (resourceIds != null) query.setResourceIds(resourceIds);
        if (strFromTime != null) query.setFromTime(strToDate(strFromTime, false));
        if (strToTime != null) query.setToTime(strToDate(strToTime, true));
        if (statusCodes != null)
            query.setStatusCodes(listStrToListInt(statusCodes));

        return query;
    }

    private List<Integer> listStrToListInt(List<String> list) {
        
        List<Integer> ret = new ArrayList<Integer>(list.size());
        Iterator<String> it = list.listIterator();
        while (it.hasNext()) ret.add(Integer.parseInt(it.next()));
        return ret;
    }

    private List<Date> strToDate(List<String> tmpTimes) {

        if (tmpTimes == null || tmpTimes.size() == 0) return null;
        List<Date> dateList = new ArrayList<Date>(tmpTimes.size());
        for (String s : tmpTimes) dateList.add(strToDate(s, false));
        return dateList;
    }

    private Date strToDate(String s, boolean isToTime) {
      if (s == null || s.isEmpty()) throw new IllegalStateException();
      
      /*
       *  Add a timestamp if it is missing from the date string.
       *  If isToTime is true, set the time to the last second of the day.
       */
      if (s.indexOf('T') == -1) {
        if (isToTime) {
          s += "T23:59:59";
        }
        else {
          s += "T00:00:00";
        }
      }
      
      Date returnDate = DatatypeConverter.parseDateTime(s).getTime();
      logger.debug("returnDate: " + returnDate.toString());
      return returnDate;
  }
}
