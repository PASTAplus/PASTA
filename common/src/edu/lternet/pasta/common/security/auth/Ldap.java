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

import java.security.GeneralSecurityException;

import javax.net.SocketFactory;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

/**
 * Used to authenticate users using an LDAP server. Instances of this class
 * attempt direct authentications with their specified server and they ignore
 * any referral URLs that it might return. This class can be extended to
 * "lock in" a particular server and port by implementing a single, no-arg
 * constructor.
 *
 * @see LterLdap
 */
public class Ldap implements AuthenticationSystem {

    private final String server;
    private final int port;

    /**
     * Constructs a new LDAP authentication system using the specified LDAP
     * server and port. Invalid servers or ports will only be detected upon
     * invocation of {@link #authenticate(String, String)}.
     *
     * @param server
     *            the LDAP server to be used for authentication, such as {@code
     *            ldap.lternet.edu}.
     *
     * @param port
     *            the port to be used, such as 389.
     */
    public Ldap(String server, int port) {
        this.server = server;
        this.port = port;
    }

    /**
     * Returns the server used by this object.
     * @return the server used by this object.
     */
    public String getServer() {
        return server;
    }

    /**
     * Returns the port used by this object.
     * @return the port used by this object.
     */
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return server + ":" + port;
    }

    /**
     * Indicates if the specified user can be authenticated by this LDAP
     * authentication system.
     *
     * @param user
     *            the user's distinguished name.
     * @param password
     *            the user's password.
     *
     * @return {@code true} if the user was authenticated; {@code false} if the
     *         user was not authenticated because the provided ID does not exist
     *         in this LDAP or the provided password is invalid.
     *
     * @throws IllegalStateException
     *             if anything unexpected occurs during communication with the
     *             LDAP server that prevents a definitive authentication
     *             determination. These exceptions might have an instance of
     *             {@link LDAPException} as their cause.
     */
    public boolean authenticate(String user, String password) {

        LDAPConnection connection = makeConnection();

        try {

            connection.connect(server, port);

            ResultCode code = connection.bind(user, password).getResultCode();

            if (code.intValue() == ResultCode.SUCCESS_INT_VALUE) {
                return true;
            }

            String s = "LDAPConnection.bind() did not throw an exception, " +
                       "but the ResultCode was not 0 (success): " +
                       code.toString();
            throw new IllegalStateException(s);
        }

        catch (LDAPException e) {

            int code = e.getResultCode().intValue();

            if (code == ResultCode.INVALID_DN_SYNTAX_INT_VALUE) {
                return false;
            }

            if (code == ResultCode.INVALID_CREDENTIALS_INT_VALUE) {
                return false;
            }

            String s = "The LDAP result code '" + e.getResultCode() +
                       "' could not be interpretted.";
            throw new IllegalStateException(s, e);
        }

        finally {
            if (connection != null && connection.isConnected()) {
                connection.close();
                connection = null;  // allowing garbage collection
            }
        }

    }

    private LDAPConnection makeConnection() {

        if (port != 636) {
            return new LDAPConnection();
        }

        SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());

        SocketFactory socketFactory = null;

        try {
            socketFactory = sslUtil.createSSLSocketFactory();
        }
        catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }

        return new LDAPConnection(socketFactory);
    }

}
