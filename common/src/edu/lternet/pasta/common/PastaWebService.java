/*
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.common;

import java.io.File;
import java.util.ResourceBundle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * Used to provide common functionality for PASTA web services. This abstract
 * class can be extended by a web service class to gain functionality that
 * all PASTA web services should have. This currently includes:
 *
 * <ul>
 * <li>Adding the following header to all responses from the web service:
 * {@code Web-Service: <web-service>},</li>
 * <li>Returning the web service's welcome page upon an HTTP GET request to the
 * URL {@code http://<host>/<web-service>/},</li>
 * <li>Returning the web service's API documentation upon an HTTP GET request to
 * the URL {@code http://<host>/<web-service>/docs/api/},</li>
 * <li>Returning the web service's version upon an HTTP GET request to the URL
 * {@code http://<host>/<web-service>/version/}.</li>
 * </ul>
 *
 */
public abstract class PastaWebService implements ContainerRequestFilter,
                                                 ContainerResponseFilter,
                                                 ResourceFilter {

    /**
     * The key used in {@code PastaWebService.properties} to define the HTTP
     * header used to indicate the web service, such as {@code Web-Service}.
     */
    public static final String WEB_SERVICE_HEADER_KEY =
        "web.service.header";

    /**
     * The web service header read from the properties file.
     */
    public static final String WEB_SERVICE_HEADER;

    static {

        // Reading properties file
        String name = PastaWebService.class.getName();
        ResourceBundle bundle = ResourceBundle.getBundle(name);

        WEB_SERVICE_HEADER = bundle.getString(WEB_SERVICE_HEADER_KEY);
    }

    /**
     * Serves files from the provided directory. This method ensures that users
     * cannot access files outside of the provided directory by using relative
     * paths in the URL, e.g. '../../passwords.txt'.
     *
     * @param directory
     *            the directory in which the specified file must exist.
     * @param fileName
     *            the file to be served.
     *
     * @return if the file can be served, an HTTP response with status code 200
     *         OK, the specified file as the entity, and the MIME-type
     *         text/plain, text/html, or text/xml, depending on the file's
     *         extension; otherwise, an HTTP response with status code 404 Not
     *         Found, an error message as the entity, and the MIME-type
     *         text/plain.
     *
     */
    public static Response serveFileFromDirectory(File directory,
                                                  String fileName) {

        directory = directory.getAbsoluteFile();
        File file = new File(directory, fileName);
        String err = "The requested file '" + fileName +
                     "' could not be found.";

        try {
            file = FileUtility.assertCanRead(file);
        } catch (ResourceNotFoundException e) {
            return WebResponseFactory.makeNotFound(err);
        }

        // If the user tried to access a file in a different directory
        // using a relative path. e.g '../../passwords.txt'.
        if (!file.getParentFile().equals(directory)) {
            return WebResponseFactory.makeNotFound(err);
        }

        String contents = FileUtility.fileToString(file);

        String mediaType = MediaType.TEXT_PLAIN;

        if (fileName.endsWith(".xml")) {
            mediaType = MediaType.TEXT_XML;
        } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            mediaType = MediaType.TEXT_HTML;
        }

        return Response.ok(contents, mediaType).build();
    }

    /**
     * Returns the welcome page for this web service. By default, this method
     * returns the API document, but it can be overridden.
     *
     * @return the welcome page for this web service.
     */
    public File getWelcomePage() {
        return getApiDocument();
    }

    /**
     * Returns the tutorial document of this web service.
     *
     * @return the tutorial document of this web service.
     */
    public abstract File getTutorialDocument();

    /**
     * Returns the API document of this web service.
     *
     * @return the API document of this web service.
     */
    public abstract File getApiDocument();

    /**
     * Returns the version of this web service.
     *
     * @return the version of this web service.
     */
    public abstract String getVersionString();

    /**
     * Responds with the tutorial document of this web service upon an HTTP GET
     * request.
     *
     * @return an HTTP response with status code 200 'OK', MIME type {@code
     *         text/html}, and the tutorial documentation as the entity.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/docs/tutorial")
    public Response respondWithHtmlTutorialDocument() {

        File file = getTutorialDocument();

        String apiString = FileUtility.fileToString(file);

        return Response.ok(apiString, MediaType.TEXT_HTML).build();
    }

    /**
     * Responds with the API documentation of this web service upon an HTTP GET
     * request.
     *
     * @return an HTTP response with status code 200 'OK', MIME type {@code
     *         text/html}, and the API documentation as the entity.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/docs/api")
    public Response respondWithHtmlApiDocument() {

        File file = getApiDocument();

        String apiString = FileUtility.fileToString(file);

        return Response.ok(apiString, MediaType.TEXT_HTML).build();
    }

    /**
     * Responds with the welcome page of this web service upon an HTTP GET
     * request.
     *
     * @return an HTTP response with status code 200 'OK', MIME type {@code
     *         text/html}, and the welcome page as the entity.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/")
    public Response respondWithWelcomePage() {

        File file = getWelcomePage();

        String apiString = FileUtility.fileToString(file);

        return Response.ok(apiString, MediaType.TEXT_HTML).build();
    }

    /**
     * Responds with the version of this web service upon an HTTP GET request.
     *
     * @return an HTTP response with status code 200 'OK', MIME type {@code
     *         text/plain}, and the web service version as the entity.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/version")
    public Response respondWithWebServiceVersion() {
        String version = getVersionString();
        return Response.ok(version, MediaType.TEXT_PLAIN).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerResponse filter(ContainerRequest request,
                                    ContainerResponse response) {

        MultivaluedMap<String, Object> headers = response.getHttpHeaders();
        headers.add("Cache-Control", "no-cache");

        String version = getVersionString();
        headers.add(WEB_SERVICE_HEADER, version);

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerRequest filter(ContainerRequest request) {
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerRequestFilter getRequestFilter() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerResponseFilter getResponseFilter() {
        return this;
    }
}
