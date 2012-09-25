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

import edu.lternet.pasta.common.validate.Validator;
import edu.lternet.pasta.common.validate.ValidatorResultsImpl;

/**
 * <p>
 * Validates principals from {@code <allow>} and {@code <deny>} elements. The
 * strings provided to instances of this class are assumed to already comply
 * with the XML schema: they are not empty. Validation and canonicalization
 * involves comparing the provided string to the canonical name of the
 * pseudo-user "public".
 * </p>
 * <p>
 * Each principal string is trimmed and compared to the canonical string of the
 * pseudo-principal "public", ignoring case. If the strings match, but they are
 * not identical, a warning will be reported and the canonical name of "public"
 * will be included in the results. Otherwise, the provided string will be
 * considered as canonical.
 * </p>
 */
public class PrincipalValidator implements Validator<String> {


    /**
     * The canonical string used to indicate the pseudo-principal "public".
     */
    public static final String PUBLIC = "public";

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
        results.setEntity(entity);
        results.setValid(true);

        String canonical = canonicalizePrincipal(entity);

        results.setCanonicalEntity(canonical);

        return results;
    }

    private String canonicalizePrincipal(String principal) {

        if (!matchesPublic(principal)) {
            return principal; // leave it as is
        }

        // If the principal matches public, is it canonical?
        if (!principal.equals(PUBLIC)) {

            String s = "The principal '" + principal
                    + "' was replaced with the pseudo-principal '" + PUBLIC
                    + "' in the canonical version. The remaining "
                    + "validation will assume that is correct.";
            results.warn(s);
        }

        return PUBLIC;
    }

    private boolean matchesPublic(String principal) {
        return principal.trim().equalsIgnoreCase(PUBLIC);
    }


}
