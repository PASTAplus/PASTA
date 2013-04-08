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

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Used to represent EML packageIds. An 'EML packageId' is a tuple of the
 * following form: scope, identifier, revision. The three tuple elements are
 * always present in a 'packageId', but they do not always have values. The
 * following "partial packageIds" can also exist:
 * <ul>
 * <li>scope, identifier, null</li>
 * <li>scope, null, null</li>
 * <li>null, null, null</li>
 * </ul>
 * <p>
 * The 'scope', if not null, must be a non-empty string containing only
 * alphanumeric characters and dashes '-'. The 'identifier', if not null, must
 * be an integer &ge 0. The 'revision', if not null, must be an integer &ge 0.
 * </p>
 */
public final class EmlPackageId {

    // The only allowable non-alphanumeric characters in scopes
    private static final Character DASH = '-';
    private static final Character UNDERSCORE = '_';

    // Suffix for Level 1 Metadata
    private static final String LEVEL_ONE_SUFFIX = "-nis";

    /**
     * Returns a list of characters contained in the provided string that are
     * not permitted in a 'scope'. An empty list is returned if no such
     * characters are present.
     *
     * @param scope the string for which illegal characters are returned.
     *
     * @return a list of characters contained in the provided string that are
     * not permitted in a 'scope'.
     *
     * @throws NullPointerException if provided string is {@code null}.
     */
    public static List<Character> getIllegalCharacters(String scope) {

        List<Character> illegals = new LinkedList<Character>();

        for (Character c : scope.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && 
                !DASH.equals(c) &&
                !UNDERSCORE.equals(c)
               ) {
                illegals.add(c);
            }
        }

        return illegals;
    }

    /**
     * Accessor method for the LEVEL_ONE_PREFIX constant.
     *
     * @return  The value of the LEVEL_ONE_PREFIX constant.
     */
    public static String getLevelOneSuffix() {
      return LEVEL_ONE_SUFFIX;
    }

    private final String scope;
    private final Integer identifier;
    private final Integer revision;

    /**
     * Constructs a new EML packageId object with any of the tuple forms
     * described above.
     *
     * @param scope
     *            the EML packageId scope.
     * @param identifier
     *            the EML packageId ID.
     * @param revision
     *            the EML packageId revision.
     *
     * @throws IllegalArgumentException
     *             if the provided scope is empty or contains illegal
     *             characters; if the identifier or revision are < 0; or if the
     *             provided tuple is not allowed (see above).
     */
    public EmlPackageId(String scope, Integer identifier, Integer revision) {

        if (scope != null) {

            if (scope.isEmpty()) {
                String s = "scope is empty";
                throw new IllegalArgumentException(s);
            }

            List<Character> illegals = getIllegalCharacters(scope);

            if (!illegals.isEmpty()) {
                String s = "scope contains illegal chars: " + illegals;
                throw new IllegalArgumentException(s);
            }
        }

        if (scope == null && identifier != null) {
            String s = "scope is null but id is not. id=" + identifier;
            throw new IllegalArgumentException(s);
        }

        if (identifier == null && revision != null) {
            String s = "id is null but revision is not. revision=" + revision;
            throw new IllegalArgumentException(s);
        }

        if (identifier != null && identifier < 0) {
            String s = "id < 0: " + identifier;
            throw new IllegalArgumentException(s);
        }

        if (revision != null && revision < 0) {
            String s = "revision < 0: " + revision;
            throw new IllegalArgumentException(s);
        }

        this.scope = scope;
        this.identifier = identifier;
        this.revision = revision;
    }

    /**
     * Returns this packageId's scope.
     * @return this packageId's scope.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Returns this packageId's identifier.
     * @return this packageId's identifier.
     */
    public Integer getIdentifier() {
        return identifier;
    }

    /**
     * Returns this packageId's revision.
     * @return this packageId's revision.
     */
    public Integer getRevision() {
        return revision;
    }

    /**
     * Indicates if all tuple elements values, that is, they are non-null.
     *
     * @return {@code true} if all elements are non-null; {@code false}
     * otherwise.
     */
    public boolean allElementsHaveValues() {
        return nonNullElements() == 3;
    }

    /**
     * Returns the number of non-null elements contained in this packageId's
     * tuple.
     * @return the number of non-null elements contained in this packageId's
     * tuple.
     */
    public int nonNullElements() {
        return (scope == null      ? 0 : 1) +
               (identifier == null ? 0 : 1) +
               (revision == null   ? 0 : 1);
    }

    /**
     * Returns a string representation of this packageId.
     * @return a string representation of this packageId.
     */
    @Override
    public String toString() {
        //return "scope=" + scope + ",id=" + identifier + ",revision=" + revision;
    	return scope + "." + identifier.toString() + "." + revision.toString();
    }

    /**
     * Indicates if the provided object is equal to this EML packageId.
     * @return {@code true} if scope, identifier, and revision are the same in
     * both objects; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof EmlPackageId)) {
            return false;
        }

        EmlPackageId epi = (EmlPackageId) obj;

        return (scope == null      ? scope == epi.scope
                                   : scope.equals(epi.scope)) &&
               (identifier == null ? identifier == epi.identifier
                                   : identifier.equals(epi.identifier)) &&
               (revision == null   ? revision == epi.revision
                                   : revision.equals(epi.revision));
    }

    /**
     * Returns a content-based hash code.
     * @return a content-based hash code.
     */
    @Override
    public int hashCode() {

        int hash = 1;

        hash *= (scope == null ? 1 : scope.hashCode());
        hash ^= (identifier == null ? 1 : identifier);
        hash *= (revision == null ? 1 : revision);

        return hash * 17;
    }


    /**
     * Boolean to determine whether this EML packageId conforms with the
     * syntax for Level One metadata.
     *
     * @return  true if this packageId conforms with Level One, else false
     */
    public boolean isLevelOne() {
      boolean isLevelOne = false;

      if (scope != null) {
        isLevelOne = scope.endsWith(LEVEL_ONE_SUFFIX);
      }

      return isLevelOne;
    }


    /**
     * Makes a Level One copy of this EmlPackageId object and returns it.
     * A Level One copy has a scope that is prefixed with the Level One
     * prefix value.
     *
     * @return  A copy of this object that complies with the Level One format.
     */
    public EmlPackageId toLevelOne() {
      EmlPackageId levelOneCopy = null;

      if (scope != null) {
        if (this.isLevelOne()) {
          String message = "Attempting to convert an EmlPackageId object to " +
            "Level One. This EmlPackageId is already Level One: " +
            this.toString();
          throw new IllegalStateException(message);
        }
        else {
          String levelOneScope = scope + LEVEL_ONE_SUFFIX;
          levelOneCopy = new EmlPackageId(levelOneScope, identifier, revision);
        }
      }

      return levelOneCopy;
    }

}
