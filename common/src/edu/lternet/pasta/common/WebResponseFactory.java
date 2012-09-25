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

package edu.lternet.pasta.common;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.sun.jersey.api.client.ClientResponse;

import edu.lternet.pasta.common.proxy.BufferedClientResponse;

/**
 * Used as a factory for web responses.
 *
 */
public final class WebResponseFactory {

    private WebResponseFactory() {
        // preventing instantiation
    }

    /**
     * Returns a web response with the provided response status and entity.
     *
     * @param status
     *            the HTTP response status.
     * @param entity
     *            the entity of the returned response.
     *
     * @return a web response with the provided response status and entity.
     */
    public static Response make(Response.StatusType status, String entity) {

        ResponseBuilder rb = Response.status(status);
        rb.entity(entity);
        rb.type(MediaType.TEXT_PLAIN);

        return rb.build();
    }

    /**
     * Returns a web response with the provided response status and entity.
     *
     * @param status
     *            the HTTP response status.
     * @param entity
     *            the entity of the returned response.
     *
     * @return a web response with the provided response status and entity.
     */
    public static Response make(int status, String entity) {

        ResponseBuilder rb = Response.status(status);
        rb.entity(entity);
        rb.type(MediaType.TEXT_PLAIN);

        return rb.build();
    }

    /**
     * Returns a '400 Bad Request' web response with the provided message as the
     * entity.
     *
     * @param message
     *            the entity.
     *
     * @return a '400 Bad Request' web response.
     */
    public static Response makeBadRequest(String message) {
        return make(Response.Status.BAD_REQUEST, message);
    }

    /**
     * Returns a '400 Bad Request' web response with the message of the provided
     * cause as the entity.
     *
     * @param cause
     *            the cause.
     *
     * @return a '400 Bad Request' web response.
     *
     * @see Throwable#getMessage()
     */
    public static Response makeBadRequest(Throwable cause) {
        return make(Response.Status.BAD_REQUEST, cause.getMessage());
    }

    /**
     * Returns a '401 Unauthorized' web response with the message of the
     * provided cause as the entity.
     *
     * @param cause
     *            the cause.
     *
     * @return a '401 Unauthorized' web response.
     *
     * @see Throwable#getMessage()
     */
    public static Response makeUnauthorized(Throwable cause) {
        return make(Response.Status.UNAUTHORIZED, cause.getMessage());
    }

    /**
     * Returns a '401 Unauthorized' web response with the provided message as
     * the entity.
     *
     * @param message
     *            the entity of the returned response.
     *
     * @return a '401 Unauthorized' web response.
     */
    public static Response makeUnauthorized(String message) {
        return make(Response.Status.UNAUTHORIZED, message);
    }

    /**
     * Returns a '410 Gone' web response with the message of the provided cause
     * as the entity.
     *
     * @param cause
     *            the cause.
     *
     * @return a '410 Gone' web response.
     *
     * @see Throwable#getMessage()
     */
    public static Response makeGone(Throwable cause) {
        return make(Response.Status.GONE, cause.getMessage());
    }

    /**
     * Returns a '404 Not Found' web response with the message of the provided
     * cause as the entity.
     *
     * @param cause
     *            the cause.
     *
     * @return a '404 Not Found' web response.
     *
     * @see Throwable#getMessage()
     */
    public static Response makeNotFound(Throwable cause) {
        return make(Response.Status.NOT_FOUND, cause.getMessage());
    }

    /**
     * Returns a '404 Not Found' web response with the provided message as
     * the entity.
     *
     * @param message
     *            the entity of the returned response.
     *
     * @return a '404 Not Found' web response.
     */
    public static Response makeNotFound(String message) {
        return make(Response.Status.NOT_FOUND, message);
    }

    /**
     * Returns a '409 Conflict' web response with the message of the provided
     * cause as the entity.
     *
     * @param cause
     *            the cause.
     *
     * @return a '409 Conflict' web response.
     *
     * @see Throwable#getMessage()
     */
    public static Response makeConflict(Throwable cause) {
        return make(Response.Status.CONFLICT, cause.getMessage());
    }

    /**
     * Returns a '417 Expectation Failed' web response. This response can be
     * returned to a client when a web service is acting as a proxy, and the
     * request it makes to another web service fails. The entity of the response
     * will contain the provided error message, and appended to the end of that,
     * a description of the response from the failed request.
     *
     * @param message
     *            a description of the error that occurred and the web service
     *            that produced the response that indicates failure.
     *
     * @param response
     *            the response of the web service to which the failed request
     *            was sent.
     *
     * @return a '417 Expectation Failed' web response.
     */
    public static Response
        makeExpectationFailed(String message,
                              BufferedClientResponse<String> response) {

        StringBuilder sb = new StringBuilder();

        sb.append(message);
        sb.append("\n\n");

        ClientResponse.Status status = response.getClientResponseStatus();

        // Appending response status
        sb.append("Response status:\n");
        sb.append(status.getStatusCode() + " " + status.getReasonPhrase());
        sb.append("\n\n");

        // Appending response headers
        sb.append("Response headers:\n");

        for (Entry<String, List<String>> e : response.getHeaders().entrySet()) {
            sb.append(e.getKey());
            sb.append(": ");
            appendList(sb, e.getValue());
            sb.append('\n');
        }

        sb.append('\n');

        // Appending response body
        sb.append("Response body:\n");
        if (response.hasEntity()) {
            sb.append(response.getEntity());
            sb.append('\n');
        }

        return make(417, sb.toString());
    }

    private static void appendList(StringBuilder sb, List<String> list) {

        Iterator<String> i = list.iterator();

        while (i.hasNext()) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(' ');
            }
        }
    }

    /**
     * Returns a '417 Expectation Failed' web response. This response can be
     * returned to a client when a web service is acting as a proxy, and the
     * request it makes to another web service fails. The entity of the response
     * will contain the provided error message, and appended to the end of that,
     * the stack trace of the exception thrown.
     *
     * @param message
     *            a description of the error that occurred and the web service
     *            that was being contacted.
     *
     * @param cause
     *            the exception thrown when a request was made to the web
     *            service.
     *
     * @return a '417 Expectation Failed' web response.
     */
    public static Response makeExpectationFailed(String message,
                                                 Throwable cause) {

        StringBuilder sb = new StringBuilder();

        sb.append(message);
        sb.append("\n\n");
        sb.append(cause.getMessage());

        for (StackTraceElement e : cause.getStackTrace()) {
            sb.append(e.toString());
        }

        return make(417, sb.toString());
    }

}
