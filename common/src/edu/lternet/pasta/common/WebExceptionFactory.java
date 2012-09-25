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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import edu.lternet.pasta.common.proxy.BufferedClientResponse;


/**
 * Used as a factory for web application exceptions.
 *
 */
public final class WebExceptionFactory {

    private WebExceptionFactory() {
        // preventing instantiation
    }

    /**
     * Returns a web application exception with the provided response status,
     * cause, and message. The provided message will be used as the entity.
     *
     * @param status
     *            the HTTP response status.
     * @param cause
     *            the cause, or {@code null} if a thrown exception is not the
     *            cause of this web application exception.
     * @param message
     *            the entity of the returned exception.
     * @return a web application exception with the provided response status,
     *         cause, and message.
     */
    public static WebApplicationException make(Response.StatusType status,
                                               Throwable cause,
                                               String message) {

        ResponseBuilder rb = Response.status(status);
        rb.entity(message);
        rb.type(MediaType.TEXT_PLAIN);

        return new WebApplicationException(cause, rb.build());
    }

    /**
     * Returns a web application exception with the provided response status,
     * cause, and message. The provided message will be used as the entity.
     *
     * @param status
     *            the HTTP response status.
     * @param cause
     *            the cause, or {@code null} if a thrown exception is not the
     *            cause of this web application exception.
     * @param message
     *            the entity of the returned exception.
     * @return a web application exception with the provided response status,
     *         cause, and message.
     */
    public static WebApplicationException make(int status,
                                               Throwable cause,
                                               String message) {

        ResponseBuilder rb = Response.status(status);
        rb.entity(message);

        return new WebApplicationException(cause, rb.build());
    }

    /**
     * Returns a '400 Bad Request' web application exception with the provided
     * message as the entity.
     *
     * @param message the entity.
     *
     * @return a '400 Bad Request' web application exception.
     */
    public static WebApplicationException makeBadRequest(String message) {
        return make(Response.Status.BAD_REQUEST, null, message);
    }

    /**
     * Returns a '400 Bad Request' web application exception with the message
     * of the provided cause as the entity.
     *
     * @param cause the cause.
     *
     * @return a '400 Bad Request' web application exception.
     *
     * @see Throwable#getMessage()
     */
    public static WebApplicationException makeBadRequest(Throwable cause) {
        return make(Response.Status.BAD_REQUEST, cause, cause.getMessage());
    }

    /**
     * Returns a '401 Unauthorized' web application exception with the message
     * of the provided cause as the entity.
     *
     * @param cause the cause.
     *
     * @return a '401 Unauthorized' web application exception.
     *
     * @see Throwable#getMessage()
     */
    public static WebApplicationException makeUnauthorized(Throwable cause) {
        return make(Response.Status.UNAUTHORIZED, cause, cause.getMessage());
    }

    /**
     * Returns a '401 Unauthorized' web application exception with the provided
     * message as the entity.
     *
     * @param message the entity of the returned exception.
     *
     * @return a '401 Unauthorized' web application exception.
     */
    public static WebApplicationException makeUnauthorized(String message) {
        return make(Response.Status.UNAUTHORIZED, null, message);
    }

    /**
     * Returns a '410 Gone' web application exception with the provided
     * message as the entity. The provided {@code Throwable} is the cause.
     *
     * @param cause the cause.
     *
     * @return a '410 Gone' web application exception.
     *
     * @see Throwable#getMessage()
     */
    public static WebApplicationException makeGone(Throwable cause) {
        return make(Response.Status.GONE, cause, cause.getMessage());
    }

    /**
     * Returns a '404 Not Found' web application exception with the message
     * of the provided cause as the entity.
     *
     * @param cause the cause.
     *
     * @return a '404 Not Found' web application exception.
     *
     * @see Throwable#getMessage()
     */
    public static WebApplicationException makeNotFound(Throwable cause) {
        return make(Response.Status.NOT_FOUND, cause, cause.getMessage());
    }

    /**
     * Returns a '409 Conflict' web application exception with the message
     * of the provided cause as the entity.
     *
     * @param cause the cause.
     *
     * @return a '409 Conflict' web application exception.
     *
     * @see Throwable#getMessage()
     */
    public static WebApplicationException makeConflict(Throwable cause) {
        return make(Response.Status.CONFLICT, cause, cause.getMessage());
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
    public static WebApplicationException
            makeExpectationFailed(String message,
                                  BufferedClientResponse<String> response) {

        Response r =
            WebResponseFactory.makeExpectationFailed(message, response);

        return new WebApplicationException(r);
    }

}
