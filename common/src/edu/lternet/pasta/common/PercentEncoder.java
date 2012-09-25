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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Used for percent encoding and decoding of strings.
 *
 */
public final class PercentEncoder {

    private PercentEncoder() {
        // preventing instantiation
    }

    private static final String CHAR_ENCODING = "UTF-8";

    /**
     * Returns a percent encoded version of the provided string.
     *
     * @param s
     *            the string to be percent encoded.
     *
     * @return a percent encoded version of the provided string.
     */
    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, CHAR_ENCODING).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e); // should never be reached
        }
    }

    /**
     * Returns a decoded version of the provided percent encoded string.
     *
     * @param s
     *            the percent encoded string to be decoded.
     *
     * @return a decoded version of the provided percent encoded string.
     */
    public static String decode(String s) {
        try {
            return URLDecoder.decode(s, CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e); // should never be reached
        }
    }

}
