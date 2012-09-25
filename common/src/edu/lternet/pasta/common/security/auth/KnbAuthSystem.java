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

package edu.lternet.pasta.common.security.auth;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * Used to authenticate and authorize KNB users.
 */
public class KnbAuthSystem implements AuthSystem {

    private final KnbLdap ldap;

    /**
     * Constructs a new KNB {@code authSystem}.
     */
    public KnbAuthSystem(File keystoreFile) {
        ldap = new KnbLdap(keystoreFile);
    }

    /**
     * Returns {@link AuthSystemDef#KNB}.
     * @return {@link AuthSystemDef#KNB}.
     */
    public AuthSystemDef getAuthSystemDef() {
        return AuthSystemDef.KNB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authenticate(String user, String password) {
        return ldap.authenticate(user, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getGroups(String user) {
        return Collections.emptySet();
    }

    /**
     * Returns {@code true} if both strings can be parsed as equivalent
     * distinguished names, and {@code false} otherwise. This method does not
     * verify that the provided IDs refer to a user that actually exists in the
     * KNB LDAP system. It only performs a syntactical comparison.
     *
     * @param userId1
     *            a potential user ID.
     * @param userId2
     *            another potential user ID.
     *
     * @return {@code true} if both strings can be parsed as equivalent
     *         distinguished names; {@code false} otherwise.
     *
     * @see DN#equals(Object)
     */
    public boolean sameUser(String userId1, String userId2) {

        if (!DN.isValidDN(userId1) || !DN.isValidDN(userId2)) {
            return false;
        }

        try {

            DN dn1 = new DN(userId1);
            DN dn2 = new DN(userId2);

            return dn1.equals(dn2);
        }
        catch (LDAPException e) {
            // Should never be reached, because isValidDN()
            // has already been called.
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns {@code true} if both strings can be parsed as equivalent
     * principals, and {@code false} otherwise. If both strings are valid
     * distinguished names, {@link #sameUser(String, String)} is returned.
     * Otherwise, {@link String#equalsIgnoreCase(String)} is returned.
     *
     * @param principal1
     *            a potential principal in the KNB authSystem.
     * @param principal2
     *            another potential principal in the KNB authSystem.
     *
     * @return {@code true} if both strings can be parsed as equivalent
     *         principals; {@code false} otherwise.
     */
    public boolean samePrincipal(String principal1, String principal2) {

        if (DN.isValidDN(principal1) && DN.isValidDN(principal2)) {
            return sameUser(principal1, principal2);
        }

        return principal1.equalsIgnoreCase(principal2);
    }

}
