/*
 * Copyright 2011-2013 the University of New Mexico.
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
 */

package edu.lternet.pasta.token;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;

import edu.lternet.pasta.portal.ConfigurationListener;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.codec.binary.Base64;

import org.apache.log4j.Logger;

/**
 * @author servilla
 * @since Mar 9, 2012
 *        <p/>
 *        The TokenManager manages the NIS Data Portal "tokenstore" for
 *        storing user
 *        authentication tokens provided by PASTA.
 */
public class TokenManager {
    
    /*
     * Class variables 
     */

    private static final Logger logger = Logger.getLogger(edu.lternet.pasta
                                                              .token
                                                              .TokenManager
                                                              .class);
    
    /*
     * Instance variables
     */

    private String dbDriver;   // database driver
    private String dbURL;      // database URL
    private String dbUser;     // database user name
    private String dbPassword; // database user password

    /*
     * Constructors
     */

    public TokenManager() {

        Configuration options = ConfigurationListener.getOptions();

        this.dbDriver = options.getString("db.Driver");
        this.dbURL = options.getString("db.URL");
        this.dbUser = options.getString("db.User");
        this.dbPassword = options.getString("db.Password");

    }
    
    /*
     * Methods
     */

    /**
     * @param args
     */
    public static void main(String[] args) {

        ConfigurationListener.configure();

        String token = "sz46tDcFxqLby2TtlBARREdqGFSSRFbjSHPvMw0hgXLsG2uGlDW" +
                       "rOzjf/zM7Yd7g4n8pK5qKzohvP9UdYqf/xyx/RBAUU1QYwmUXTA" +
                       "5NnUZ5qHjYCtx3Y+DgwyNsQPoz6dQqR92BWWsWb39BilwfaRoyg" +
                       "8vRbmJ3CFRslvB5WfUqEI2OIhD2h3VyYXq8V7f8X4IZDSHWMWNX" +
                       "YMuxC3eQ+A==";

        String uid = "ucarroll";

        TokenManager tokenManager = new TokenManager();

        try {
            tokenManager.setToken(uid, token);
        }
        catch (SQLException e) {
            logger.error(e);
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            logger.error(e);
            e.printStackTrace();
        }

        try {
            System.out.println(tokenManager.getToken(uid));
        }
        catch (SQLException e) {
            logger.error(e);
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            logger.error(e);
            e.printStackTrace();
        }

    }

    /**
     * Returns a connection to the database.
     *
     * @return The database Connection object.
     */
    private Connection getConnection() throws ClassNotFoundException {

        Connection conn = null;

        SQLWarning warn;

        // Load the jdbc driver.
        try {
            Class.forName(this.dbDriver);
        }
        catch (ClassNotFoundException e) {
            logger.error("Can't load driver " + e.getMessage());
            throw (e);
        }

        // Make the database connection
        try {
            conn = DriverManager.getConnection(this.dbURL, this.dbUser,
                                                  this.dbPassword);

            // If a SQLWarning object is available, print its warning(s).
            // There may be multiple warnings chained.
            warn = conn.getWarnings();

            if (warn != null) {
                while (warn != null) {
                    logger.warn("SQLState: " + warn.getSQLState());
                    logger.warn("Message:  " + warn.getMessage());
                    logger.warn("Vendor: " + warn.getErrorCode());
                    warn = warn.getNextWarning();
                }
            }
        }
        catch (SQLException e) {
            logger.error("Database access failed " + e);
        }

        return conn;

    }

    /**
     * Sets the token and user id into the tokenstore.
     *
     * @param uid   The user identifier.
     * @param token The base64 encrypted token.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void setToken(String uid, String token) throws SQLException,
                                                              ClassNotFoundException {

        String sql = "SELECT authtoken.tokenstore.token FROM " +
                         "authtoken.tokenstore WHERE " +
                         "authtoken.tokenstore.user_id='" + uid + "';";

        Connection dbConn = null; // database connection object

        try {
            dbConn = getConnection();
            try {
                Statement stmt = dbConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                if (rs.next()) {

                    // uid already in token store, perform "update".
                    sql = "UPDATE authtoken.tokenstore " +
                              "SET token='" + token + "'," +
                              "date_created=now() " +
                              "WHERE authtoken.tokenstore.user_id='" + uid +
                              "';";

                    if (stmt.executeUpdate(sql) == 0) {
                        SQLException e = new SQLException("setToken: update '"
                                                              + sql + "' " +
                                                              "failed");
                        throw (e);
                    }

                } else {

                    // uid not in token store, perform "insert".
                    sql = "INSERT INTO authtoken.tokenstore VALUES " +
                              "('" + uid + "','" + token + "', now());";

                    if (stmt.executeUpdate(sql) == 0) {
                        SQLException e = new SQLException("setToken: insert '"
                                                              + sql + "' " +
                                                              "failed");
                        throw (e);
                    }

                }

            }
            catch (SQLException e) {
                logger.error("setToken: " + e);
                logger.error(sql);
                e.printStackTrace();
                throw (e);
            }
            finally {
                dbConn.close();
            }
            // Will fail if database adapter class not found.
        }
        catch (ClassNotFoundException e) {
            logger.error("setToken: " + e);
            e.printStackTrace();
            throw (e);
        }

    }

    /**
     * Return the token of the user based on the uid.
     *
     * @param uid The user identifier.
     * @return A String object representing the base64 encrypted token.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public String getToken(String uid) throws SQLException,
                                                  ClassNotFoundException {

        String token = null;
        String sql = "SELECT authtoken.tokenstore.token FROM " +
                         "authtoken.tokenstore WHERE " +
                         "authtoken.tokenstore.user_id='" + uid + "';";

        Connection dbConn = null; // database connection object

        try {
            dbConn = getConnection();
            try {
                Statement stmt = dbConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    token = rs.getString("token");
                } else {
                    SQLException e = new SQLException("getToken: uid '" + uid +
                                                          "' not in authtoken" +
                                                          ".tokenstore");
                    throw (e);
                }
            }
            catch (SQLException e) {
                logger.error("getToken: " + e);
                logger.error(sql);
                e.printStackTrace();
                throw (e);
            }
            finally {
                dbConn.close();
            }
            // Will fail if database adapter class not found.
        }
        catch (ClassNotFoundException e) {
            logger.error("getToken: " + e);
            e.printStackTrace();
            throw (e);
        }

        return token;

    }

    /**
     * Deletes the user token from the tokenstore.
     *
     * @param uid The user identifier.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void deleteToken(String uid) throws SQLException,
                                                   ClassNotFoundException {

        String sql = "DELETE FROM authtoken.tokenstore WHERE " +
                         "authtoken.tokenstore.user_id='" + uid + "';";

        Connection dbConn = null; // database connection object

        try {
            dbConn = getConnection();
            try {
                Statement stmt = dbConn.createStatement();
                if (stmt.executeUpdate(sql) == 0) {
                    SQLException e = new SQLException("deleteToken: delete '"
                                                          + sql + "' failed");
                    throw (e);
                }
            }
            catch (SQLException e) {
                logger.error("deleteToken: " + e);
                logger.error(sql);
                e.printStackTrace();
                throw (e);
            }
            finally {
                dbConn.close();
            }
            // Will fail if database adapter class not found.
        }
        catch (ClassNotFoundException e) {
            logger.error("deleteToken: " + e);
            e.printStackTrace();
            throw (e);
        }

    }

    /**
     * Returns the clear text representation of the authentication token
     * identified by the user
     *
     * @param uid The user identifier
     * @return The clear text representation of the authentication token
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public String getCleartextToken(String uid) throws
        SQLException, ClassNotFoundException {

        String[] token = null;
        String sql = "SELECT authtoken.tokenstore.token FROM " +
                         "authtoken.tokenstore WHERE " +
                         "authtoken.tokenstore.user_id='" + uid + "';";

        Connection dbConn = null; // database connection object

        try {
            dbConn = getConnection();
            try {
                Statement stmt = dbConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    token = rs.getString("token").split("-");
                } else {
                    SQLException e = new SQLException("getToken: uid '" + uid +
                                                          "' not in authtoken" +
                                                          ".tokenstore");
                    throw (e);
                }
            }
            catch (SQLException e) {
                logger.error("getToken: " + e);
                logger.error(sql);
                e.printStackTrace();
                throw (e);
            }
            finally {
                dbConn.close();
            }
            // Will fail if database adapter class not found.
        }
        catch (ClassNotFoundException e) {
            logger.error("getToken: " + e);
            e.printStackTrace();
            throw (e);
        }

        return new String(Base64.decodeBase64(token[0]));

    }

    /**
     * Returns the base64 encoded digital signature of the authentication
     * token identifier by the user
     *
     * @param uid The user identifier
     * @return The base64 encoded digital signature
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public String getTokenSignature(String uid) throws
        SQLException, ClassNotFoundException {

        String[] token = null;
        String sql = "SELECT authtoken.tokenstore.token FROM " +
                         "authtoken.tokenstore WHERE " +
                         "authtoken.tokenstore.user_id='" + uid + "';";

        Connection dbConn = null; // database connection object

        try {
            dbConn = getConnection();
            try {
                Statement stmt = dbConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    token = rs.getString("token").split("-");
                } else {
                    SQLException e = new SQLException("getToken: uid '" + uid +
                                                          "' not in authtoken" +
                                                          ".tokenstore");
                    throw (e);
                }
            }
            catch (SQLException e) {
                logger.error("getToken: " + e);
                logger.error(sql);
                e.printStackTrace();
                throw (e);
            }
            finally {
                dbConn.close();
            }
            // Will fail if database adapter class not found.
        }
        catch (ClassNotFoundException e) {
            logger.error("getToken: " + e);
            e.printStackTrace();
            throw (e);
        }

        return token[1];

    }

    /**
     * Returns the user's LDAP distinguished name from the authentication token
     *
     * @param uid The user identifier
     * @return The user's LDAP distinguished name
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public String getUserDistinguishedName(String uid) throws
        SQLException, ClassNotFoundException {

        String token = this.getCleartextToken(uid);
        String[] tokenParts = token.split("\\*");

        return tokenParts[0];
    }

    /**
     * Returns the authentication system from the authentication token
     *
     * @param uid The user identifier
     * @return The authentication system
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public String getTokenAuthenticationSystem(String uid) throws
        SQLException, ClassNotFoundException {

        String token = this.getCleartextToken(uid);
        String[] tokenParts = token.split("\\*");

        return tokenParts[1];
    }

    /**
     * Returns the time-to-live (TTL) from the authentication token
     *
     * @param uid The user identifier
     * @return The time-to-live
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public Long getTokenTimeToLive(String uid) throws
        SQLException, ClassNotFoundException {

        String token = this.getCleartextToken(uid);
        String[] tokenParts = token.split("\\*");

        return Long.valueOf(tokenParts[2]);
    }

    /**
     * Returns the list of groups from the authentication token
     *
     * @param uid The user identifier
     * @return The list of groups
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public ArrayList<String> getUserGroups(String uid)  throws
        SQLException, ClassNotFoundException {

        ArrayList<String> groups = new ArrayList<String>();

        String token = this.getCleartextToken(uid);
        String[] tokenParts = token.split("\\*");

        for (int i = 3; i < tokenParts.length; i++) {
            groups.add(tokenParts[i]);
        }

        return groups;

    }
}