/*
 * Copyright 2011-2015 the University of New Mexico.
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

package edu.lternet.pasta.portal.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;

import org.apache.log4j.Logger;

/**
 * DatabaseClient collects together common methods for handling database
 * interactions, such as getting and closing a connection.
 * 
 * @author dcosta
 * 
 */
public class DatabaseClient {

	/*
	 * Class fields
	 */
	private static final Logger logger = Logger.getLogger(DatabaseClient.class);

	/*
	 * Instance fields
	 */

	protected String dbDriver; // audit database driver
	protected String dbURL; // audit database URL
	protected String dbUser; // audit database user name
	protected String dbPassword; // audit database user password


	/*
	 * Constructors
	 */

	public DatabaseClient(String dbDriver, String dbURL, String dbUser,
			String dbPassword) {
		this.dbDriver = dbDriver;
		this.dbURL = dbURL;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
	}


	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	/**
	 * Closes the connection to the database.
	 */
	public void closeConnection(Connection conn) {
		try {
			logger.debug("Closing the database connection");
			conn.close();
		}
		catch (SQLException e) {
			logger.error("Couldn't close the database connection: " + e.getMessage());
		}
	}


	/**
	 * Returns a connection to the database.
	 * 
	 * @return conn the database Connection object or null if a connection
	 *         could not be obtained
	 */
	public Connection getConnection() {
		Connection conn = null;
		SQLWarning warn;

		// Load the JDBC driver
		try {
			Class.forName(dbDriver);
		}
		catch (ClassNotFoundException e) {
			logger.error("Can't load driver " + e);
			return conn;
		}

		// Make the database connection
		try {
			conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);

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
			logger.error("Database access failed " + e.getMessage());
		}

		return conn;
	}

}
