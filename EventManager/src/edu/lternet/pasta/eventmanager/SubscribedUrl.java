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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * <p>
 * Used to parse and validate URLs subscribed by the Event Manager. URLs have
 * the following syntax:<br>
 * <center>{@code scheme:scheme-specific-part}</center><br>
 * URLs supplied to this class' constructor must meet the following
 * requirements:
 * </p>
 * <ul>
 * <li>must be non-null and non-empty strings,</li>
 * <li>must be parsable using {@link URI#URI(String)}.</li>
 * <li>must have {@code http} as their scheme,</li>
 * <li>must have a non-empty scheme-specific part.</li>
 * </ul>
 * If any of these constraints are violated, an
 * {@link IllegalArgumentException} will be thrown that contains a
 * descriptive error message that is suitable for end-users. If the provided URL
 * contains non-ASCII characters, they will be will be encoded as US-ASCII.
 *
 * @see URI#toASCIIString()
 */
public class SubscribedUrl {

    /**
     * The 'http' scheme.
     */
    public static final String HTTP = "http";

    private final String url;

    /**
     * Constructs a new URL to be subscribed by the Event Manager.
     * @param url the URL.
     */
    public SubscribedUrl(String url) {

        if (url == null || url.isEmpty()) {
            String s = "A URL was not provided.";
            throw new IllegalArgumentException(s);
        }

        URI parsedUrl = null;

        try {
            parsedUrl = new URI(url);
        } catch (URISyntaxException e) {

            StringBuilder sb = new StringBuilder();

            sb.append("The provided URL '");
            sb.append(url);
            sb.append("' could not be parsed. The URL parser returned ");
            sb.append("the following error message: ");
            sb.append(e.getMessage());
            sb.append(".");

            throw new IllegalArgumentException(sb.toString(), e);
        }

        if (!parsedUrl.isAbsolute()) {
            String s = "The provided URL '" + url +
                       "' does not contain a scheme, such as 'http:'.";
            throw new IllegalArgumentException(s);
        }

        String scheme = parsedUrl.getScheme();

        if (!scheme.equalsIgnoreCase(HTTP)) {
            String s = "The scheme '" + scheme +
                       "' of the provided URL '" + url +
                       "' is not allowed. It must be 'http'.";
            throw new IllegalArgumentException(s);
        }

        String schemeSpecificPart = parsedUrl.getSchemeSpecificPart();

        if (schemeSpecificPart == null || schemeSpecificPart.isEmpty()) {
            String s = "The provided URL '" + url +
                       "' does not contain a scheme-specific part.";
            throw new IllegalArgumentException(s);
        }

        this.url = parsedUrl.toASCIIString();
    }

    /**
     * Returns this URL as a string.
     * @return this URL as a string.
     */
    @Override
    public String toString() {
        return url;
    }

    /**
     * Indicates if the provided object is equals to this one. Two subscribed
     * URLs are equal if their string representations are equal
     *
     * @return {@code true} if the objects are equals; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof SubscribedUrl)) {
            return false;
        }

        SubscribedUrl su = (SubscribedUrl) o;

        return url.equals(su.url);
    }
}
