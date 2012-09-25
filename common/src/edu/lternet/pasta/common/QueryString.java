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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

/**
 * Used to represent URI query strings. This class assists with processing and
 * validating parameter keys and values. If an error is detected in the content
 * of parameters, a {@link WebApplicationException} is thrown as a '400 Bad
 * Request' with a descriptive error message as the entity that is suitable for
 * end-users.
 */
public final class QueryString {

    private final Map<String, List<String>> queryParams;

    /**
     * Constructs a query string from the provided URI information.
     * @param uriInfo contains the query string parameters.
     */
    public QueryString(UriInfo uriInfo) {
        this(uriInfo.getQueryParameters());
    }

    /**
     * Constructs a new query string from the provided query parameters.
     * A '400 Bad Request' exception is thrown if a key is {@code null} or
     * empty. Values that are {@code null} or empty are ignored, that is, they
     * are not retained during construction. If a list of values is
     * {@code null}, it is converted to an empty list.
     *
     * @param queryParams keys mapped to a list of values.
     */
    public QueryString(Map<String, List<String>> queryParams) {

        this.queryParams = new LinkedHashMap<String, List<String>>();

        for (Entry<String, List<String>> e : queryParams.entrySet()) {

            String key = e.getKey();
            List<String> values = e.getValue();

            if (key == null || key.isEmpty()) {

                StringBuilder sb = new StringBuilder();
                sb.append("A query parameter was provided with a value ");
                sb.append("but no key: ");
                appendCollection(sb, values);
                sb.append(".");

                throw WebExceptionFactory.makeBadRequest(sb.toString());
            }

            List<String> valuesCopy = new LinkedList<String>();

            if (values != null) {
                for (String v : values) {
                    if (v != null && !v.isEmpty()) {
                        valuesCopy.add(v);
                    }
                }
            }

            this.queryParams.put(key, valuesCopy);
        }

    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append('?');

        Iterator<Entry<String, List<String>>> i1 =
            queryParams.entrySet().iterator();

        // Iterating over the keys
        while (i1.hasNext()) {

            Entry<String, List<String>> e = i1.next();
            String key = e.getKey();
            List<String> values = e.getValue();

            // Appending the key separately, because it might
            // not have any associated values.
            sb.append(key);

            Iterator<String> i2 = values.iterator();

            // Iterating over the key's values
            while (i2.hasNext()) {
                sb.append('=');
                sb.append(i2.next());

                // If another value exists
                if (i2.hasNext()) {
                    sb.append('&');
                    sb.append(key);
                }
            }

            // If another key exists
            if (i1.hasNext()) {
                sb.append('&');
            }

        }

        return sb.toString();
    }

    /**
     * Returns the parameters of this query string. If a key exists without
     * a corresponding value, it is mapped to an empty list.
     * @return the parameters of this query string.
     */
    public Map<String, List<String>> getParams() {

        Map<String, List<String>> m = new LinkedHashMap<String, List<String>>();
        m.putAll(queryParams);

        return m;
    }

    /**
     * Returns the list of values associated with the provided key, if it
     * exists.
     * @param key the key for which values are returned.
     * @return the list of values associated with the provided key, or
     * {@code null} if the key does not exist.
     */
    public List<String> getOptionalValues(String key) {
        return queryParams.get(key);
    }

    /**
     * Returns the value associated with the provided key, if the key exists.
     * If the key exists, but it has no associated value, or it has more
     * than one associated value, a '400 Bad Request' exception is thrown.
     *
     * @param key the key for which a value is returned.
     *
     * @return the value associated with the provided key, or {@code null} if
     * if the key does not exist.
     */
    public String getOptionalValue(String key) {

        List<String> values = getOptionalValues(key);

        if (values == null) {
            return null;
        }

        assertNotEmpty(key, values);
        assertNotMultiple(key, values);

        return values.get(0);
    }

    /**
     * Returns the list of values associated with the provided key. If the key
     * is not present, or it has no associated values, a '400 Bad Request'
     * exception is thrown.
     * @param key the key for which values are returned.
     * @return the list of values associated with the provided key.
     */
    public List<String> getRequiredValues(String key) {

        List<String> values = queryParams.get(key);

        if (values == null) {
            String s = "The query parameter '" + key +
                       "' is required, but it was not provided.";
            throw WebExceptionFactory.makeBadRequest(s);
        }

        assertNotEmpty(key, values);

        return values;
    }

    /**
     * Returns the value associated with the provided key. If the key is not
     * present, it has no associated value, or it has more than one associated
     * value, a '400 Bad Request' exception is thrown.
     * @param key the key for which a value is returned.
     * @return the value associated with the provided key.
     */
    public String getRequiredValue(String key) {

        List<String> values = getRequiredValues(key);

        assertNotMultiple(key, values);

        return values.get(0);
    }

    /**
     * Throws a '400 Bad Request' exception if this query string contains keys
     * that are not present in the provided set.
     *
     * @param validKeys the set of valid keys.
     */
    public void checkForIllegalKeys(Set<String> validKeys) {

        Set<String> illegalKeys = getIllegalKeys(validKeys);

        if (illegalKeys.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("The following provided query parameter keys are not ");
        sb.append("understood by this web service: ");
        appendCollection(sb, illegalKeys);
        sb.append(".");

        throw WebExceptionFactory.makeBadRequest(sb.toString());
    }

    private Set<String> getIllegalKeys(Set<String> validKeys) {

        Set<String> providedKeys = new TreeSet<String>(queryParams.keySet());

        providedKeys.removeAll(validKeys); // only illegals remain

        return providedKeys;
    }

    private void appendCollection(StringBuilder sb, Collection<String> c) {

        Iterator<String> i = c.iterator();

        while (i.hasNext()) {

            sb.append(i.next());

            if (i.hasNext()) {
                sb.append(", ");
            }
        }

    }

    private void assertNotEmpty(String key, List<String> values) {

        if (values.isEmpty()) {
            String s = "No value was provided for the query parameter '" +
                       key + "'.";
            throw WebExceptionFactory.makeBadRequest(s);
        }

    }

    private void assertNotMultiple(String key, List<String> values) {

        if (values.size() > 1) {

            StringBuilder sb = new StringBuilder();

            sb.append("The query parameter '");
            sb.append(key);
            sb.append("' requires a single value, but multiple values ");
            sb.append("were provided: ");
            appendCollection(sb, values);
            sb.append(".");

            throw WebExceptionFactory.makeBadRequest(sb.toString());
        }

    }
}
