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

import com.unboundid.ldap.sdk.LDAPException;

/**
 * Used to authenticate users with the LDAP server of the Long Term Ecological
 * Research (LTER) Network Office. {@code ldaps://ldap.lternet.edu:636}.
 *
 */
public final class LterLdap extends Ldap {

    private static final String server = "ldap.lternet.edu";
    private static final int port = 636;

    /**
     * Constructs a new LTER LDAP authentication system.
     */
    public LterLdap() {
        super(server, port);
    }

    public static void main(String[] args) throws LDAPException {

		if (args.length != 2) {
			System.err.println("Please enter LTER LDAP username and password as command line arguments.");
			System.exit(1);
		}
		
        LterLdap ldap = new LterLdap();

		String user = args[0];
		String password = args[1];

        System.out.println(ldap.authenticate(user, password));
    }
}
