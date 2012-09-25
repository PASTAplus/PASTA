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
 * Validates a permission in the form of a string from the {@code <permission>}
 * element.
 * </p>
 * <p>
 * During validation, the provided string is trimmed of leading and trailing
 * whitespace and compared to the set of canonical permission strings ({@code
 * read}, {@code write}, {@code changePermission}, and {@code all}), ignoring
 * case. If a provided string matches a canonical string, it is considered
 * valid, and the canonical string is included in the results. If a match is not
 * found, the provided string is considered invalid.
 * </p>
 */
public class PermissionStringValidator implements Validator<String> {

    /**
     * The canonical string used to indicate the permission to read.
     */
    public static final String READ = "read";

    /**
     * The canonical string used to indicate the permission to write.
     */
    public static final String WRITE = "write";

    /**
     * The canonical string used to indicate the permission to change
     * permissions.
     */
    public static final String CHANGE_PERMISSION = "changePermission";

    /**
     * The canonical string used to indicate all permissions.
     */
    public static final String ALL = "all";

    private static final String[] PERMISSIONS = {
        READ, WRITE, CHANGE_PERMISSION, ALL
    };

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

        String trimmed = entity.trim();

        for (String canonical : PERMISSIONS) {
            if (trimmed.equalsIgnoreCase(canonical)){
                return foundMatching(entity, canonical);
            }
        }

        String s = "The specified permission '" + entity
                + "' could not be interpretted as any of the "
                + "supported permissions.";
        results.fatal(s);

        return results;
    }

    private ValidatorResults<String> foundMatching(String entity,
                                                   String canonical) {

        results.setCanonicalEntity(canonical);

        if (entity.equals(canonical)) {
            return results;
        }

        String s = "The specified permission '" + entity
                + "' is not a literal match to any of the "
                + "supported permissions.";
        results.warn(s);

        s = "The specified permission '" + entity + "' was changed to '"
                + canonical + "' in the canonical version.";
        results.info(s);

        return results;
    }
}
