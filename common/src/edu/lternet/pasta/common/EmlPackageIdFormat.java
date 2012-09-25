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

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * <p>
 * Used to format and parse EML packageIds. The definitive description of 'EML
 * packagIds' can be found in {@link EmlPackageId}.
 * </p>
 * <p>
 * This class uses the following syntax to represent packageIds as strings:<br>
 * <center>{@code <scope><delimiter><identifier><delimiter><revision>}</center>
 * <p>
 * For the canonical case, where all tuples (scope, identifier, revision) have
 * values:
 * </p>
 * <p>
 * The 'scope' is unaltered during parsing and formatting, because it is already
 * a string, but it is checked for the presence of illegal characters during
 * parsing. The 'identifier' and 'revision' must be parsable as integers (&ge
 * 0). The delimiter can be specified as either a dot '.' or forward slash '/'
 * in the constructor of this class.
 * </p>
 *
 * <p>
 * For the non-canonical case, involving "partial packageIds", all of the above
 * is true, except that null elements and their preceding delimiter are omitted
 * from the string representation:
 * </p>
 *
 * <table align="center">
 * <tr>
 * <td>
 * scope, identifier, revision</td>
 * <td>&harr</td>
 * <td>
 * {@code <scope><delimiter><identifier><delimiter><revision>}</td>
 * </tr>
 * <tr>
 * <td>scope, identifier, {@code null}</td>
 * <td>&harr</td>
 * <td>{@code <scope><delimiter><identifier>}</td>
 * </tr>
 * <tr>
 * <td>scope, {@code null}, {@code null}</td>
 * <td>&harr</td>
 * <td>{@code <scope>}</td>
 * </tr>
 * <tr>
 * <td>{@code null}, {@code null}, {@code null}</td>
 * <td>&harr</td>
 * <td>empty string.</td>
 * </tr>
 * </table>
 *
 * <p>
 * If a parsing error occurs within any method of this class, an
 * {@link IllegalArgumentException} will be thrown that contains a
 * descriptive error message that is suitable for end-users. These exceptions
 * can be caught and wrapped as {@link WebApplicationException} (which can
 * be thrown directly from web services) using the method
 * {@link #wrapAsBadRequest(IllegalArgumentException)}.
 * </p>
 *
 * <p>
 * Examples: The packageId ("lter-lno", 1, null) would be represented as
 * "lter-lno.1". The packageId (null, null, null) would be "", an empty string.
 * </p>
 *
 *
 */
public final class EmlPackageIdFormat {

    /**
     * Used to define all delimiters allowed in string representations of
     * EML packageIds.
     */
    public enum Delimiter {

        DOT(".", "\\."), FORWARD_SLASH("/", "/");

        private final String string;
        private final String regex;

        private Delimiter(String string, String regex) {
            this.string = string;
            this.regex = regex;
        }

        /**
         * Returns the delimiter used for formatting.
         * @return the delimiter used for formatting.
         */
        public String getString() {
            return string;
        }

        /**
         * Returns the regex used for parsing.
         * @return the regex used for parsing.
         */
        public String getRegex() {
            return regex;
        }
    }

    /**
     * The default delimiter for elements of the tuple (scope, identifier,
     * revision).
     */
    public static final Delimiter DEFAULT_DELIMITER = Delimiter.DOT;

    private final Delimiter delimiter;

    /**
     * Constructs an EML packageId format object with the default delimiter.
     */
    public EmlPackageIdFormat() {
        this(DEFAULT_DELIMITER);
    }

    /**
     * Constructs an EML packageId format object with the provided delimiter.
     *
     * @throws NullPointerException
     *             if the provided delimiter is {@code null}.
     */
    public EmlPackageIdFormat(Delimiter delimiter) {
        if (delimiter == null) {
            throw new NullPointerException("null delimiter");
        }
        this.delimiter = delimiter;
    }

    /**
     * Returns the delimiter used for parsing and formatting tuples.
     *
     * @return the delimiter used for parsing and formatting tuples.
     */
    public Delimiter getDelimiter() {
        return delimiter;
    }

    /**
     * Returns a formatted string of the provided EML packageId.
     *
     * @param packageId
     *            the packageId to be formatted.
     * @return a formatted string of the provided EML packageId.
     */
    public String format(EmlPackageId packageId) {
        return format(packageId.getScope(),
                      packageId.getIdentifier(),
                      packageId.getRevision());
    }

    private String format(String scope, Integer identifier, Integer revision) {
        return format(scope,
                      identifier == null ? null : identifier.toString(),
                      revision == null   ? null : revision.toString());
    }

    private String format(String scope, String identifier, String revision) {

        StringBuilder sb = new StringBuilder();

        if (scope != null) {
            sb.append(scope);
        }

        if (identifier != null) {
            sb.append(delimiter.getString());
            sb.append(identifier);
        }

        if (revision != null) {
            sb.append(delimiter.getString());
            sb.append(revision);
        }

        return sb.toString();
    }

    /**
     * Parses the provided string and returns a corresponding EML packageId
     * object. Trailing delimiters are ignored.
     *
     * @param packageId
     *            a parsable (scope, identifier, revision) tuple, or any of the
     *            allowed partial forms.
     *
     * @return an EML packageId object corresponding to the provided string.
     *
     * @throws NullPointerException if the provided string is {@code null}.
     */
    public EmlPackageId parse(String packageId) {

        String[] elements = packageId.split(delimiter.getRegex());

        if (elements.length > 3) {
            String s = "The EML packageId '" + packageId
                    + "' does not conform to the standard "
                    + format("<scope>", "<identifier>", "<revision>")
                    + " syntax.";
            throw new IllegalArgumentException(s);
        }

        return parse(elements);
    }

    private EmlPackageId parse(String[] elements) {

        EmlPackageId epi = null;

        switch (elements.length) {
        case 0:
            epi = parse(null, null, null);
            break;
        case 1:
            epi = parse(elements[0], null, null);
            break;
        case 2:
            epi = parse(elements[0], elements[1], null);
            break;
        case 3:
            epi = parse(elements[0], elements[1], elements[2]);
            break;
        }

        return epi;
    }

    /**
     * Parses the provided tuple elements and returns a corresponding EML
     * packageId object.
     *
     * @param scope
     *            the scope string.
     * @param identifier
     *            the identifier string.
     * @param revision
     *            the revision string.
     *
     * @return an EML packageId object corresponding to the provided tuple
     *         elements.
     */
    public EmlPackageId parse(String scope,
                              String identifier,
                              String revision) {

        String parsedScope = parseScope(scope, identifier, revision);
        Integer parsedId = parseIdentifier(scope, identifier, revision);
        Integer parsedRevision = parseRevision(scope, identifier, revision);

        if (parsedScope == null && parsedId != null) {
            String s = "The EML packageId '" +
                       format(scope, identifier, revision) +
                       "' contains an identifier, but not a scope.";
            throw new IllegalArgumentException(s);
        }

        if (parsedId == null && parsedRevision != null) {
            String s = "The EML packageId '" +
                       format(scope, identifier, revision) +
                       "' contains a revision, but not an identifier.";
            throw new IllegalArgumentException(s);
        }

        return new EmlPackageId(parsedScope, parsedId, parsedRevision);
    }

    private Integer parseInteger(String s) throws NumberFormatException {

        if (s == null || s.isEmpty()) {
            return null;
        }

        return Integer.parseInt(s);
    }

    /**
     * Returns the provided string or {@code null} if the provided string is
     * empty or {@code null}. The other tuple elements are used only to compose
     * descriptive error messages.
     *
     * @param scope
     *            the scope.
     * @param identifier
     *            the identifier.
     * @param revision
     *            the revision.
     *
     * @return the provided string or {@code null} if the provided string is
     *         empty or {@code null}.
     */
    public String parseScope(String scope, String identifier, String revision) {

        if (scope == null || scope.isEmpty()) {
            return null;
        }

        List<Character> illegals = EmlPackageId.getIllegalCharacters(scope);

        if (!illegals.isEmpty()) {

            StringBuilder sb = new StringBuilder();
            sb.append("The scope term in the EML packageId '");
            sb.append(format(scope, identifier, revision));
            sb.append("' contains the following illegal characters: ");

            for (Character c : illegals) {
                sb.append(c);
            }
            sb.append(".");

            throw new IllegalArgumentException(sb.toString());
        }

        return scope;
    }

    /**
     * Parses the provided identifier as an integer. The other tuple
     * elements are used only to compose descriptive error messages.
     *
     * @param scope
     *            the scope.
     * @param identifier
     *            the identifier.
     * @param revision
     *            the revision.
     *
     * @return the provided identifier as an integer, or {@code null} if an
     *         empty or {@code null} identifier string is provided.
     */
    public Integer parseIdentifier(String scope,
                                   String identifier,
                                   String revision) {

        Integer x = null;

        try {
            x = parseInteger(identifier);
        } catch (NumberFormatException e) {

            String s = "The identifier term '" +
                       identifier +
                       "' in the EML packageId '" +
                       format(scope, identifier, revision) +
                       "' cannot be parsed as an integer.";
            throw new IllegalArgumentException(s, e);
        }

        if (x != null && x < 0) {
            String s = "The identifier term '" +
                       identifier +
                       "' in the EML packageId '" +
                       format(scope, identifier, revision) +
                       "' is a negative number.";
            throw new IllegalArgumentException(s);
        }

        return x;
    }

    /**
     * Parses the provided revision as an integer. The other tuple elements
     * are used only to compose descriptive error messages.
     *
     * @param scope
     *            the scope.
     * @param identifier
     *            the identifier.
     * @param revision
     *            the revision.
     *
     * @return the provided revision as an integer, or {@code null} if an empty
     *         or {@code null} revision string is provided.
     */
    public Integer parseRevision(String scope,
                                 String identifier,
                                 String revision) {

        Integer x = null;

        try {
            x = parseInteger(revision);
        } catch (NumberFormatException e) {

            String s = "The revision term '" +
                       revision +
                       "' in the EML packageId '" +
                       format(scope, identifier, revision) +
                       "' cannot be parsed as an integer.";
            throw new IllegalArgumentException(s, e);
        }

        if (x != null && x < 0) {
            String s = "The revision term '" +
                       revision +
                       "' in the EML packageId '" +
                       format(scope, identifier, revision) +
                       "' is a negative number.";
            throw new IllegalArgumentException(s);
        }

        return x;
    }

    /**
     * Wraps an {@code IllegalArgumentException} thrown by this class with a
     * {@code WebApplicationException} that can be thrown from a web service.
     * The web application exception is constructed as a '400 Bad Request' with
     * the error message from the illegal argument exception as the entity. This
     * is suitable when input cannot be parsed due to user error.
     *
     * @param e
     *            an illegal argument exception thrown by this class.
     * @return the wrapped exception.
     */
    @Deprecated
    public WebApplicationException
                wrapAsBadRequest(IllegalArgumentException e) {

        ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
        rb.entity(e.getMessage());

        return new WebApplicationException(e, rb.build());
    }
}
