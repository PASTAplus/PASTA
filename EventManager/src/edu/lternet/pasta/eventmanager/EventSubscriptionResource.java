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

package edu.lternet.pasta.eventmanager;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;
import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.MethodNameUtility;
import edu.lternet.pasta.common.QueryString;
import edu.lternet.pasta.common.ResourceDeletedException;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.WebExceptionFactory;
import edu.lternet.pasta.common.XmlParsingException;
import edu.lternet.pasta.common.audit.AuditManagerClient;
import edu.lternet.pasta.common.audit.AuditRecord;
import edu.lternet.pasta.common.security.access.AccessControllerFactory;
import edu.lternet.pasta.common.security.access.JaxRsHttpAccessController;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.eventmanager.EmlSubscription.SubscriptionBuilder;

/**
 * <p>
 * The Event Manager subscription web service allows users to <b>create</b>,
 * <b>read</b>, and <b>delete</b> subscriptions to PASTA "events". When an
 * event occurs, subscribers to that event will be automatically notified by
 * the Event Manager. The only event currently handled by the Event Manager
 * is when an EML document in PASTA's Metadata Catalog is created or "updated"
 * (which can also be considered a form of creation).
 * </p>
 *
 * <p>
 * An event subscription contains the following attributes:
 * <ul>
 * <li><b>identification number</b> - a number that is generated automatically
 * when a subscription is created</li>
 * <li><b>creator</b> - the user that sent the request for subscription
 * creation</li>
 * <li><b>EML packageId</b> - the EML document(s) to which the subscription is
 * associated</li>
 * <li><b>URL</b> - specifies the web address to which a notification will be
 * sent when an event occurs</li>
 * </ul>
 * </p>
 *
 * <p>
 * The <i>EML packageId</i> can be complete (containing a scope, identifier and
 * revision) or partial (containing a scope and identifier or a just a scope) or
 * empty. If a subscription contains a complete packageId, the subscriber will
 * be notified when an EML document with the same scope, identifier, and
 * revision is created. If a subscription contains a partial EML packageId, the
 * subscriber will be notified every time an EML document is created with that
 * scope and identifier or just the scope. If a subscription contains an empty
 * EML packageId, the subscriber will be notified every time an EML document
 * is created in PASTA.
 * </p>
 *
 * <p>
 * The Event Manager notifies a subscriber of an event by sending an HTTP POST
 * request to the URL defined in the corresponding subscription attribute. The
 * body of that request will contain the complete packageId of the EML document
 * that prompted the notification regardless of whether the subscription's
 * packageId attribute is complete or partial. It is recommended that the web
 * service that receives the request respond with a HTTP response code of 200
 * "OK", thereby indicating that the notification was received (note that the
 * Event Manager will wait no longer than 30 seconds for a response). A future
 * version of the Event Manager subscription service will continue attempts at
 * notification if a 200 response status is not received.
 * </p>
 *
 * @webservicename Event Manager subscription
 * @baseurl https://event.lternet.edu/eventmanager/subscription
 */
@Path("subscription")
public final class EventSubscriptionResource extends EventManagerResource {

    /**
     * Query parameter for subscription creator's.
     */
    public static final String CREATOR = "creator";

    /**
     * Query parameter for packageId scopes.
     */
    public static final String SCOPE = "scope";

    /**
     * Query parameter for packageId identifiers.
     */
    public static final String IDENTIFIER = "identifier";

    /**
     * Query parameter for packageId revisions.
     */
    public static final String REVISION = "revision";

    /**
     * Query parameter for URLs.
     */
    public static final String URL = "url";

    /**
     * Valid query parameters.
     */
    public static final Set<String> VALID_QUERY_KEYS;

    private static final String SERVICE_OWNER = "pasta";
    
    private static final Logger logger =
        Logger.getLogger(EventSubscriptionResource.class);

    static {
        Set<String> set = new TreeSet<String>();
        set.add(CREATOR);
        set.add(SCOPE);
        set.add(IDENTIFIER);
        set.add(REVISION);
        set.add(URL);
        VALID_QUERY_KEYS = Collections.unmodifiableSet(set);
    }

    private final XmlSubscriptionFormatV1 formatter;

    /**
     * Constructs a new event subscription web service using the provided entity
     * manager factory.
     *
     * @param emf
     *            the entity manager factory for persisting subscriptions.
     */
    public EventSubscriptionResource(EntityManagerFactory emf) {
        super(emf);
        formatter = new XmlSubscriptionFormatV1();
    }

    /**
     * Constructs a new event subscription web service using the entity manager
     * factory specified in {@link ConfigurationListener}.
     *
     * @see ConfigurationListener#getPersistenceUnit()
     */
    public EventSubscriptionResource() {
        super();
        formatter = new XmlSubscriptionFormatV1();
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }


    /*
     * Wrapper method for using the audit manager client
     */
    private void audit(String serviceMethodName,
                       AuthToken authToken,
                       Response response,
                       String resourceId,
                       String entryText
                      ) {
      String serviceName = getVersionString();

      try {
        int status = response.getStatus();
        Date date = new Date();
        AuditRecord auditRecord = new AuditRecord(date, serviceName, entryText, authToken, status, serviceMethodName, resourceId);
        String auditHost = ConfigurationListener.getAuditHost();
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
     * Creates a new subscription in the Event Manager's subscription database.
     *
     * <h4>Request entity:</h4>
     *
     * <p>
     * The request entity should be an XML document (MIME type
     * <code>application/xml</code>) that contains the subscription's EML packageId,
     * and URL, with the syntax:
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
    @POST
    @Path("/eml")
    @Consumes(MediaType.APPLICATION_XML)
    public Response createSubscription(@Context HttpHeaders headers,
                                                String requestBody) {

        SubscriptionService service = null;
        String method = MethodNameUtility.methodName();
        AuthToken token = null;
        Response r = null;
        String msg = null, resourceId = null;

        try {

            assertAuthorizedToWrite(headers, method);
            token = AuthTokenFactory.makeAuthToken(headers.getCookies());

            EmlSubscription s = formatter.parse(requestBody)
                                         .setCreator(token.getUserId())
                                         .build();

            service = getSubscriptionService();
            service.create(s, token);

            URI uri = URI.create(s.getSubscriptionId().toString());

            r = Response.created(uri).build();
            resourceId = uri.toString();

            return r;

        } catch (XmlParsingException e) {
            r = WebExceptionFactory.makeBadRequest(e).getResponse();
            msg = e.getMessage();
            return r;

        } catch (UnauthorizedException e) {
            r = WebExceptionFactory.makeUnauthorized(e).getResponse();
            msg = e.getMessage();
            return r;

        } catch (ResourceExistsException e) {
            r = WebExceptionFactory.makeConflict(e).getResponse();
            msg = e.getMessage();
            return r;

        } finally {
            if (service != null) {
                service.close();
            }
            audit(method, token, r, resourceId, msg);
        }
    }

    /**
     * Returns the XML schema for subscription creation request entities.
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
    @GET
    @Path("/eml/schema")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response respondWithSchema() {
        File f = ConfigurationListener.getEmlSubscriptionSchemaFile();
        String s = FileUtility.fileToString(f);
        return Response.ok(s, MediaType.APPLICATION_XML).build();
    }

    /**
     * Returns the subscription with the specified ID.
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
    @GET
    @Path("/eml/{subscriptionId}")
    @Produces(value={MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response getSubscriptionWithId(@Context HttpHeaders headers,
                      @PathParam("subscriptionId") String subscriptionId) {

        SubscriptionService service = null;
        String method = MethodNameUtility.methodName();
        EmlPackageIdFormat format = new EmlPackageIdFormat();
        AuthToken token = null;
        String msg = null, resourceId = null;
        Response r = null;

        try {
            assertAuthorizedToRead(headers, method);
            token = AuthTokenFactory.makeAuthToken(headers.getCookies());

            Long id = parseSubscriptionId(subscriptionId);
            service = getSubscriptionService();
            EmlSubscription s = service.get(id, token);
            String xml = formatter.format(s);
            resourceId = format.format(s.getPackageId());

            r = Response.ok(xml, MediaType.APPLICATION_XML).build();

            return r;

        } catch (IllegalArgumentException e) {
          r = WebExceptionFactory.makeBadRequest(e).getResponse();
          msg = e.getMessage();
          return r;

        } catch (UnauthorizedException e) {
            r = WebExceptionFactory.makeUnauthorized(e).getResponse();
            msg = e.getMessage();
            return r;

        } catch (ResourceNotFoundException e) {
            r = WebExceptionFactory.makeNotFound(e).getResponse();
            msg = e.getMessage();
            return r;

        } catch (ResourceDeletedException e) {
            r = WebExceptionFactory.makeGone(e).getResponse();
            msg = e.getMessage();
            return r;

        } catch (WebApplicationException e) {
            r = e.getResponse();
            msg = e.getMessage();
            return r;

        } finally {
            if (service != null) {
                service.close();
            }
            audit(method, token, r, resourceId, msg);
        }
    }

    /**
     * Returns the subscriptions whose attributes match those specified in
     * the query string. If a query string is omitted, all subscriptions in the
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
     * <td>If the  query string contains an error.</td>
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
    @GET
    @Path("/eml")
    @Produces(value={MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    public Response getMatchingSubscriptions(@Context HttpHeaders headers,
                                             @Context UriInfo uriInfo) {

        SubscriptionService service = null;
        String method = MethodNameUtility.methodName();
        AuthToken token = null;
        Response r = null;
        String msg = null;
        String resourceId = null;

        try {
            assertAuthorizedToRead(headers, method);
            token = AuthTokenFactory.makeAuthToken(headers.getCookies());

            SubscriptionBuilder query = makeQuery(uriInfo);

            service = getSubscriptionService();

            List<EmlSubscription> subscriptions =
                service.getWithoutPackageIdNulls(query, token);

            String xml = formatter.format(subscriptions);

            r = Response.ok(xml, MediaType.APPLICATION_XML).build();
            return r;

        } catch (UnauthorizedException e) {
            r = WebExceptionFactory.makeUnauthorized(e).getResponse();
            msg = e.getMessage();
            return r;

        } catch (WebApplicationException e) {
            r = e.getResponse();
            msg = e.getMessage();
            return r;

        } finally {
            if (service != null) {
                service.close();
            }
            audit(method, token, r, resourceId, msg);
        }
    }

    private SubscriptionBuilder makeQuery(UriInfo uriInfo) {

        QueryString queryString = new QueryString(uriInfo);
        queryString.checkForIllegalKeys(VALID_QUERY_KEYS);

        SubscriptionBuilder query = new SubscriptionBuilder();

        String creator = queryString.getOptionalValue(CREATOR);

        if (creator != null) {
            query.setCreator(creator);
        }

        String url = queryString.getOptionalValue(URL);
        if (url != null) {
            try {
                query.setUrl(new SubscribedUrl(url));
            } catch (IllegalArgumentException e) {
                throw WebExceptionFactory.makeBadRequest(e);
            }
        }

        String scope = queryString.getOptionalValue(SCOPE);
        String identifier = queryString.getOptionalValue(IDENTIFIER);
        String revision = queryString.getOptionalValue(REVISION);

        try {
            EmlPackageIdFormat format = new EmlPackageIdFormat(Delimiter.DOT);
            EmlPackageId epi = format.parse(scope, identifier, revision);
            query.setEmlPackageId(epi);
        } catch (IllegalArgumentException e) {
            throw WebExceptionFactory.makeBadRequest(e);
        }

        return query;
    }

    /**
     * Deletes the subscription with the specified ID from the subscription
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
    @DELETE
    @Path("/eml/{subscriptionId}")
    public Response deleteSubscription(@Context HttpHeaders headers,
            @PathParam("subscriptionId") String subscriptionId) {

        SubscriptionService service = null;
        String method = MethodNameUtility.methodName();
        AuthToken token = null;
        String msg = null;
        Response r = null;
        String resourceId = null;

        try {
            assertAuthorizedToWrite(headers, method);
            token = AuthTokenFactory.makeAuthToken(headers.getCookies());

            Long id = parseSubscriptionId(subscriptionId);

            service = getSubscriptionService();
            service.delete(id, token);

            r = Response.ok().build();
            return r;

        } catch (IllegalArgumentException e) {
          r = WebExceptionFactory.makeBadRequest(e).getResponse();
          msg = e.getMessage();
          return r;

        } catch (UnauthorizedException e) {
            r = WebExceptionFactory.makeUnauthorized(e).getResponse();
            msg = e.getMessage();
            return r;

        } catch (ResourceNotFoundException e) {
            r = WebExceptionFactory.makeNotFound(e).getResponse();
            msg = e.getMessage();
            return r;

        } catch (ResourceDeletedException e) {
            r = WebExceptionFactory.makeGone(e).getResponse();
            msg = e.getMessage();
            return r;

        } catch (WebApplicationException e) {
            r = e.getResponse();
            msg = e.getMessage();
            return r;

        } finally {
            if (service != null) {
                service.close();
            }
            audit(method, token, r, resourceId, msg);
        }
    }

    private void assertAuthorizedToRead(HttpHeaders headers, String serviceMethod) {

        String acr = AccessControlRuleFactory.getServiceAcr(serviceMethod);
        JaxRsHttpAccessController controller =
            AccessControllerFactory.getDefaultHttpAccessController();

        if (controller.canRead(headers, acr, SERVICE_OWNER)) {
            return;
        }
        String s = "This request is not authorized to notify the event " +
                   "manager of events. Please check your authorization " + 
                   "credentials.";

        throw WebExceptionFactory.make(Response.Status.UNAUTHORIZED, null, s);
    }

    private void assertAuthorizedToWrite(HttpHeaders headers, String serviceMethod) {

        String acr = AccessControlRuleFactory.getServiceAcr(serviceMethod);
        JaxRsHttpAccessController controller =
                AccessControllerFactory.getDefaultHttpAccessController();

        if (controller.canWrite(headers, acr, SERVICE_OWNER)) {
            return;
        }

        String s = "This request is not authorized to notify the event " +
                   "manager of events. Please check your authorization " + 
                   "credentials.";

        throw WebExceptionFactory.make(Response.Status.UNAUTHORIZED, null, s);
    }

}
