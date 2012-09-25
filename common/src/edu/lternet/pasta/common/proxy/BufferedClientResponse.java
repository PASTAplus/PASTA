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

package edu.lternet.pasta.common.proxy;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResourceLinkHeaders;

/**
 * Used to represent and buffer the response of a server back to a requesting
 * client. This class buffers the entity of a response, so it can be accessed
 * any number of times, unlike some other client response classes, such as
 * {@link ClientResponse} (even when {@link ClientResponse#bufferEntity()} is
 * called).
 *
 * @param <T>
 *            the entity type.
 */
public final class BufferedClientResponse<T> {

    private final ClientResponse response;
    private final T entity;
    private final Class<T> entityType;

    /**
     * Wraps the provided Jersey client response. If {@code
     * ClientResponse.getEntity(Class)} has already been called on the provided
     * object, buffering the entity will not be possible.
     *
     *
     * @param response
     *            the client response to be wrapped.
     *
     * @param entityType
     *            the type of the entity.
     *
     * @throws ClientHandlerException
     *             if {@code ClientResponse.getEntity(Class)} has already been
     *             called, but {@code ClientResponse.bufferEntity()} has not.
     */
    public BufferedClientResponse(ClientResponse response,
                                  Class<T> entityType) {

        this.response = response;
        this.entityType = entityType;

        if (response.hasEntity()) {
            entity = response.getEntity(entityType);
        }
        else {
            entity = null;
        }
    }

    /**
     * Returns the entity of the client response, if one exists.
     *
     * @return the entity of the client response, if one exists; {@code null}
     *         otherwise.
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Returns the entity type.
     *
     * @return the entity type.
     */
    public Class<T> getEntityType() {
        return entityType;
    }

    /**
     * Get the allowed HTTP methods from the Allow HTTP header. Note that the
     * Allow HTTP header will be returned from an OPTIONS request.
     *
     * @return the allowed HTTP methods, all methods will returned as upper case
     *         strings.
     */
    public Set<String> getAllow() {
        return response.getAllow();
    }

    /**
     * Get the list of cookies.
     *
     * @return the cookies.
     */
    public List<NewCookie> getCookies() {
        return response.getCookies();
    }

    /**
     * Get the status code.
     *
     * @return the status code, or null if the underlying status code was set
     *         using the method setStatus(int) and there is no mapping between
     *         the integer value and the Response.Status enumeration value.
     */
    public ClientResponse.Status getClientResponseStatus() {
        return response.getClientResponseStatus();
    }

    /**
     * Get the HTTP headers of the response.
     *
     * @return the HTTP headers of the response.
     */
    public MultivaluedMap<String, String> getHeaders() {
        return response.getHeaders();
    }

    /**
     * Get the language.
     *
     * @return the language, otherwise null if not present.
     */
    public String getLanguage() {
        return response.getLanguage();
    }

    /**
     * Get Content-Length.
     *
     * @return Content-Length as integer if present and valid number. In other
     *         cases returns -1.
     */
    public int getLength() {
        return response.getLength();
    }

    public WebResourceLinkHeaders getLinks() {
        return response.getLinks();
    }

    /**
     * Get the map of response properties.
     *
     * A response property is an application-defined property that may be added
     * by the user, a filter, or the handler that is managing the connection.
     *
     * @return the map of response properties.
     */
    public Map<String, Object> getProperties() {
        return response.getProperties();
    }

    /**
     * Get response date (server side).
     *
     * @return the server side response date, otherwise null if not present.
     */
    public Date getResponseDate() {
        return response.getResponseDate();
    }

    /**
     * Get the location.
     *
     * @return the location, otherwise null if not present.
     */
    public URI getLocation() {
        return response.getLocation();
    }

    /**
     * Get the status code.
     * @return the status code.
     */
    public int getStatus() {
        return response.getStatus();
    }

    /**
     * Get the media type of the response.
     *
     * @return the media type.
     */
    public MediaType getType() {
        return response.getType();
    }

    /**
     * Checks if there is an entity available.
     *
     * @return true if there is an entity present in the response.
     */
    public boolean hasEntity() {
        return response.hasEntity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return response.toString();
    }
}
