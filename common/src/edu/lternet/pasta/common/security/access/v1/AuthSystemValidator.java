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

package edu.lternet.pasta.common.security.access.v1;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import edu.lternet.pasta.common.validate.Validator;
import edu.lternet.pasta.common.validate.ValidatorResultsImpl;

/**
 * <p>
 * Validates an authentication system in the form of a string as defined by the
 * {@code authSystem} attribute.
 * </p>
 * <p>
 *
 * During validation, the provided string is trimmed of leading and trailing
 * white space and compared to the canonical name of the PASTA authentication
 * system: {@code ldap://ldap.lternet.edu}. If an exact match is not found, the
 * string is compared to a set of equivalent authentication systems: {@code
 * KNB}, {@code knb}, {@code PASTA}, {@code pasta}, {@code LTER}, {@code lter},
 * {@code ldap.lternet.edu}, {@code ldaps://ldap.lternet.edu}, {@code
 * ldaps://ldap.lternet.edu:636}. If a match is not found, the provided string
 * is considered invalid. The canonical string is included in all results.
 * </p>
 *
 */
public class AuthSystemValidator implements Validator<String> {

    /**
     * The canonical value for the {@code authSystem} attribute.
     */
    public static final String CANONICAL = "ldap://ldap.lternet.edu";

    /**
     * The non-canonical values for the {@code authSystem} attribute.
     */
    public static final Set<String> NON_CANONICAL;

    static {

        Set<String> set = new TreeSet<String>();

        set.add("KNB");
        set.add("knb");
        set.add("PASTA");
        set.add("pasta");
        set.add("pasta:knb");
        set.add("LTER");
        set.add("lter");
        set.add("ldap.lternet.edu");
        set.add("ldaps://ldap.lternet.edu");
        set.add("ldaps://ldap.lternet.edu:636");
        set.add("https://pasta.lternet.edu/authentication");
        set.add("https://environmentaldatainitiative.org/authentication");

        NON_CANONICAL = Collections.unmodifiableSet(set);
    }

    private ValidatorResultsImpl<String> results;

    /**
     * Returns {@code true}.
     * @return {@code true}.
     */
    @Override
    public boolean canonicalizes() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorResults<String> validate(String entity) {

        results = new ValidatorResultsImpl<String>();
        results.setValid(true);
        results.setEntity(entity);
        results.setCanonicalEntity(CANONICAL);

        String trimmed = entity.trim();

        if (trimmed.equals(CANONICAL)) {
            return results;
        }

        for (String nonCanonical : NON_CANONICAL) {

            if (trimmed.equals(nonCanonical)) {

                String s = "The specified authSystem '" + entity +
                           "' is not an exact match to '" + CANONICAL +
                           "', but it will be considered as such.";
                results.warn(s);

                return results;
            }
        }

        String s = "The specified authSystem '" + entity + "' is invalid.";
        results.fatal(s);

        return results;
    }

}
