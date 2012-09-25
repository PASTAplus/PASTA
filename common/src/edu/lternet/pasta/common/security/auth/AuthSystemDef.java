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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.WorkingDirectory;

/**
 * Defines the {@code authSystems} supported by PASTA. This enumeration must be
 * modified every time a new authSystem is added to PASTA.
 */
public enum AuthSystemDef {

    /**
     * The Knowledge Network of Bioinformatics {@code authSystem}.
     */
    KNB("https://pasta.lternet.edu/authentication");

    private static final Map<AuthSystemDef, List<String>> aliasMap;

    static {

        aliasMap = new TreeMap<AuthSystemDef, List<String>>();

        // Defining aliases for the KNB authSystem
        List<String> knbAliases = new LinkedList<String>();

        String alias = "ldap.ecoinformatics.org";
        knbAliases.add(alias);

        alias = "ldap://" + alias;
        knbAliases.add(alias);

        alias += ":389";
        knbAliases.add(alias);

        alias += "/dc=ecoinformatics,dc=org";
        knbAliases.add(alias);

        aliasMap.put(KNB, knbAliases);
    }

    /**
     * Returns the definition of the specified authSystem.
     *
     * @param authSystemName
     *            the canonical name or alias of an authSystem.
     *
     * @return the definition of the specified authSystem.
     *
     * @throws IllegalArgumentException
     *             if the provided string does not specify an authSystem.
     */
    public static AuthSystemDef getAuthSystemDef(String authSystemName) {

        for (AuthSystemDef def : values()) {
            if (def.isNameOrAlias(authSystemName)) {
                return def;
            }
        }

        // The provided string was not an alias of any authSystem
        String s = "The provided string is not a name or alias of " +
                   "an authSystem: " + authSystemName;
        throw new IllegalArgumentException(s);
    }

    private final String canonicalName;

    /**
     * Constructs a new definition with the specified canonical name.
     *
     * @param canonicalName
     *            the canonical name of this authSystem.
     */
    private AuthSystemDef(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    /**
     * Returns the canonical name of this authSystem.
     * @return the canonical name of this authSystem.
     */
    public String getCanonicalName() {
        return canonicalName;
    }

    /**
     * Returns the aliases of the authSystem referred to by this definition.
     *
     * @return the aliases of the authSystem referred to by this definition.
     */
    public List<String> getAliases() {
        // Making a defensive copy
        return new LinkedList<String>(aliasMap.get(this));
    }

    /**
     * Indicates if the provided string is an alias of this authSystem. The
     * provided string is compared to the canonical name of this authSystem and
     * all of its aliases. Case is ignored during string comparison, and the
     * provided string is trimmed.
     *
     * @param nameOrAlias
     *            the potential name or alias of this authSystem.
     *
     * @return {@code true} if the provided string is an alias of this
     *         authSystem; {@code false} otherwise.
     */
    public boolean isNameOrAlias(String nameOrAlias) {

        nameOrAlias = nameOrAlias.trim();

        if (canonicalName.equalsIgnoreCase(nameOrAlias)) {
            return true;
        }

        for (String alias : aliasMap.get(this)) {
            if (alias.equalsIgnoreCase(nameOrAlias)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Constructs a new authSystem object that this definition refers to.
     *
     * @return a new authSystem object that this definition refers to.
     */
    public AuthSystem makeAuthSystem() {

        AuthSystem authSystem = null;

        switch (this) {

        case KNB:
            authSystem = makeKnbAuthSystem();
            break;

        default:
            String s = "A new authSystem enum was added, but this "
                    + "method was not updated to support it.";
            throw new UnsupportedOperationException(s);
        }

        if (authSystem.getAuthSystemDef() != this) {
            String s = "The authSystem referred to as '" + canonicalName +
                       "' was not implemented properly.";
            throw new IllegalStateException(s);
        }

        return authSystem;
    }

    private KnbAuthSystem makeKnbAuthSystem() {

        String certificateFileName = "keystore.jks";

        File workingDir = WorkingDirectory.getWorkingDirectory();
        File file = new File(workingDir, certificateFileName);
        file = FileUtility.assertCanRead(file);

        // Reading KNB properties file
        return new KnbAuthSystem(file);
    }

}
