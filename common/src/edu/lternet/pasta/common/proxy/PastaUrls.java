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

package edu.lternet.pasta.common.proxy;

import java.net.URI;
import java.util.ResourceBundle;

/**
 * Used to read a properties file containing URLs of all PASTA web services.
 *
 */
public final class PastaUrls {

    private PastaUrls() {
        // prevents instantiation
    }

    private static final ResourceBundle bundle;

    static {
        // Reading properties file
        bundle = ResourceBundle.getBundle(PastaUrls.class.getName());
    }

    /**
     * Returns the URL contained in {@code PastaUrls.properties} that
     * corresponds to the provided key. The returned URL always ends with '/'.
     *
     * @param propertyKey
     *            a key in the properties file.
     *
     * @return the URL contained in {@code PastaUrls.properties} that
     *         corresponds to the provided key.
     *
     * @throws MissingResourceException
     *             if the provided key does not exist in the properties file.
     *
     * @throws IllegalArgumentException
     *             if the URL contained in the properties file cannot be parsed
     *             as a URI.
     *
     * @see URI#create(String)
     */
    public static String getUrl(String propertyKey) {

        String url = bundle.getString(propertyKey);

        URI.create(url); // throws exception if not parsable

        return ( url += url.endsWith("/") ? "" : "/" );
    }
}
