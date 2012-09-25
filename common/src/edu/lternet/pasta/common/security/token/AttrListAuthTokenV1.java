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
import java.util.TreeSet;

import com.sun.jersey.core.util.Base64;

import edu.lternet.pasta.common.security.auth.AuthSystemDef;

/**
 * <p>
 * Used to construct an authorization token from a list of attributes and vice
 * versa. The syntax of an attribute list is:
 * </p>
 * <p align="center">
 * <em>userID*authSystem*expirationDate*group<sub>1</sub>*...*group<sub>n<sub>
 * </em>
 * </p>
 * <p>
 * User IDs and groups can contain spaces, authSystems must be defined in
 * the enum {@link AuthSystemDef}, and expiration dates must be parsable as
 * integers that indicate the number of milliseconds since January 1, 1970
 * 00:00:00 GMT.
 * </p>
 * @see java.util.Date#Date(long)
 */
public final class AttrListAuthTokenV1 implements AuthToken {

    private static final String DELIMITER = "*";
    private static final String REGEX = "[\\*]";

    /**
     * Constructs a list of attributes from the provided token.
     *
     * @param token
     *            the token from which attributes will be obtained.
     *
     * @return a list of attributes from the provided token.
     */
    public static String convert(AuthToken token) {

        StringBuilder sb = new StringBuilder();
        sb.append(assertValidUser(token.getUserId()));
        sb.append(DELIMITER);
        sb.append(token.getAuthSystem().getCanonicalName());
        sb.append(DELIMITER);
        sb.append(token.getExpirationDate());

        for (String group : token.getGroups()) {
            sb.append(DELIMITER);
            sb.append(assertValidGroup(group));
        }

        return new String(Base64.encode(sb.toString()));
    }

    private static String assertValidUser(String user) {

        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("null or empty user");
        }

        if (user.contains(DELIMITER)) {
            String s = "The user '" + user +
                       "' is invalid because it contains '" + DELIMITER + "'.";
            throw new IllegalArgumentException(s);
        }

        return user;
    }

    private static String assertValidGroup(String group) {

        if (group == null || group.isEmpty()) {
            throw new IllegalArgumentException("null or empty group");
        }

        if (group.contains(DELIMITER)) {
            String s = "The group '" + group +
                       "' is invalid because it contains '" + DELIMITER + "'.";
            throw new IllegalArgumentException(s);
        }

        return group;
    }

    private String tokenString;

    private final String user;
    private final AuthSystemDef authSystem;
    private final long expirationDate;
    private final Set<String> groups;

    /**
     * Constructs a new authorization token from the provided list of
     * attributes.
     *
     * @param attributeList
     *            a string that satisfies the syntax of an attribute list all
     *            of which has been Base64 encoded.
     *
     * @throws IllegalArgumentException
     *             if the provided string cannot be parsed.
     */
    public AttrListAuthTokenV1(String b64AttributeList) {
        if (!Base64.isArrayByteBase64(b64AttributeList.getBytes()))
          throw new IllegalArgumentException("Token is not Base64", null);

        String attributeList = new String(Base64.decode(b64AttributeList));
        String[] parts = new String(attributeList).trim().split(REGEX);

        if (parts.length < 3) {
            String s = "Token is missing parts: '" + attributeList + "'";
            throw new IllegalArgumentException(s);
        }

        AuthSystemDef authSystem = AuthSystemDef.getAuthSystemDef(parts[1]);
        long expirationDate;

        try {
            expirationDate = Long.parseLong(parts[2]);
        }
        catch (NumberFormatException e) {
            String s = "Expiration date could not be parsed: " + parts[2];
            throw new IllegalArgumentException(s, e);
        }

        Set<String> groups = new TreeSet<String>();

        for (int i = 3; i < parts.length; i ++) {
            groups.add(assertValidGroup(parts[i]));
        }

        this.tokenString = new String(Base64.encode(attributeList));
        this.user = assertValidUser(parts[0]);
        this.authSystem = authSystem;
        this.expirationDate = expirationDate;
        this.groups = Collections.unmodifiableSet(groups);
    }

    /**
     * Constructs a new authorization token from the provided attributes.
     *
     * @param user
     *            the user's ID.
     * @param authSystem
     *            the user's {@code authSystem}.
     * @param expirationDate
     *            the token's expiration date, in milliseconds since January 1,
     *            1970 00:00:00 GMT.
     * @param groups
     *            the user's groups.
     *
     * @throws IllegalArgumentException
     *             if the provided user is {@code null} or empty; if the
     *             provided authSystem is {@code null}; or if any of the groups
     *             are {@code null} or empty.
     */
    public AttrListAuthTokenV1(String user,
                               AuthSystemDef authSystem,
                               long expirationDate,
                               Set<String> groups) {

        assertValidUser(user);

        if (authSystem == null) {
            throw new IllegalArgumentException("null authSystem");
        }

        for (String group : groups) {
            assertValidGroup(group);
        }

        this.user = user;
        this.authSystem = authSystem;
        this.expirationDate = expirationDate;
        this.groups = Collections.unmodifiableSet(groups);
    }


    /**
     * Returns a string representation of this token that satisfies the defined
     * syntax of an attribute list. If this token was created with
     * {@link #AttrListAuthTokenV1(String)}, the original string is returned.
     *
     * @return a string representation of this token that satisfies the defined
     *         syntax of an attribute list.
     */
    @Override
    public String getTokenString() {

        if (tokenString != null) {
            return tokenString;
        }

        return convert(this);
    }

    /**
     * Returns the user's ID.
     * @return the user's ID.
     */
    @Override
    public String getUserId() {
        return user;
    }

    /**
     * Returns the user's {@code authSystem}.
     * @return the user's {@code authSystem}.
     */
    public AuthSystemDef getAuthSystem() {
        return authSystem;
    }

    /**
     * Returns the user's groups.
     * @return the user's groups.
     */
    @Override
    public Set<String> getGroups() {
        return groups;
    }

    /**
     * Returns the expiration date of this token, in milliseconds since January
     * 1, 1970 00:00:00 GMT.
     *
     * @return the expiration date of this token, in milliseconds since January
     *         1, 1970 00:00:00 GMT.
     */
    @Override
    public long getExpirationDate() {
        return expirationDate;
    }

    /**
     * Returns a string representation of this token that does not necessarily
     * satisfy the defined syntax of an attribute list.
     *
     * @return a string representation of this token that does not necessarily
     *         satisfy the defined syntax of an attribute list.
     */
    @Override
    public String toString() {
        return convert(this);
    }

}
