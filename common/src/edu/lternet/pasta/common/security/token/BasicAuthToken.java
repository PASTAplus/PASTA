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

package edu.lternet.pasta.common.security.token;

import java.util.Collections;
import java.util.Set;

import com.sun.jersey.core.util.Base64;

import edu.lternet.pasta.common.WebExceptionFactory;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;

/**
 * Used to represent Basic Access Authentication credentials as an
 * authorization token.
 */
public final class BasicAuthToken implements AuthTokenWithPassword {

    private static final String BASIC = "Basic";

    private static final AuthSystemDef DEFAULT_AUTH_SYSTEM = AuthSystemDef.KNB;

    /**
     * Returns a Basic Access Authentication token string for the provided user
     * and password.
     *
     * @param user
     *            the user's ID.
     * @param password
     *            the user's password.
     * @return a Basic Access Authentication token string for the provided user
     *         and password.
     *
     * @throws IllegalArgumentException
     *             if the provided user or password are {@code null} or empty.
     */
    public static String makeTokenString(String user, String password) {

        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("null or empty user");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("null or empty password");
        }

        return BASIC + " " + new String(Base64.encode(user + ":" + password));
    }

    private final String token;
    private final String user;
    private final String password;

    /**
     * Constructs a new Basic Access Authentication token from the provided
     * user and password.
     *
     * @param user the user's ID.
     * @param password their password.
     */
    public BasicAuthToken(String user, String password) {
        this(BasicAuthToken.makeTokenString(user, password));
    }

    /**
     * Constructs a new Basic Access Authentication token from the provided
     * token string.
     *
     * @param token
     *            the entire field value of the {@code Authorization} header.
     *
     * @throws WebApplicationException
     *             if the provided token string cannot be parsed according to
     *             the Basic Access Authentication scheme.
     */
    public BasicAuthToken(String token) {
        // Storing token in its original form
        this.token = token;

        token = token.trim();

        if (!token.startsWith("Basic")) {
            String s = "The value in the provided 'Authorization' header " +
                       "does not begin with 'Basic'.";
            throw WebExceptionFactory.makeBadRequest(s);
        }

        // Removing 'Basic' prefix
        token = token.substring(BASIC.length()).trim();

        // Decoding the remaining string
        if (!Base64.isBase64(token)) {
            String s = "The provided Basic authentication credentials in " +
                       "the 'Authorization' header are not Base64 encoded.";
            throw WebExceptionFactory.makeBadRequest(s);
        }

        token = Base64.base64Decode(token);

        String[] parts = token.split(":");

        if (parts.length != 2) {
            String s = "The provided Basic authentication credentials in " +
                       "the 'Authorization' header could not be parsed " +
                       "as a Base64 encoded 'user:password' pair.";
            throw WebExceptionFactory.makeBadRequest(s);
        }

        this.user = parts[0];
        this.password = parts[1];
    }

    /**
     * Returns the string used to construct this object.
     * @return the string used to construct this object.
     */
    @Override
    public String getTokenString() {
        return token;
    }

    /**
     * Returns the user ID contained in this token.
     * @return the user ID contained in this token.
     */
    @Override
    public String getUserId() {
        return user;
    }

    /**
     * Returns the password contained in this token.
     * @return the password contained in this token.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns {@code null}.
     * @return {@code null}.
     */
    @Override
    public EncryptionType getEncryptionType() {
        return null;
    }

    /**
     * Returns PASTA's default {@code authSystem}.
     * @return PASTA's default {@code authSystem}.
     */
    @Override
    public AuthSystemDef getAuthSystem() {
        return DEFAULT_AUTH_SYSTEM;
    }

    /**
     * Returns an empty set.
     * @return an empty set.
     */
    @Override
    public Set<String> getGroups() {
        return Collections.emptySet();
    }

    /**
     * Returns {@link Long#MAX_VALUE}.
     * @return {@link Long#MAX_VALUE}.
     */
    @Override
    public long getExpirationDate() {
        return Long.MAX_VALUE;
    }

}
