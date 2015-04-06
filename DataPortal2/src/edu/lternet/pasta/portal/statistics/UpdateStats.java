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

package edu.lternet.pasta.portal.statistics;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.RecentUpload;
import edu.lternet.pasta.portal.ConfigurationListener;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * User: costa Date: 4/6/15 Time: 1:53 PM
 * <p/>
 * Project: Utilities Package: edu.lternet.pasta.utilities.statistics
 * <p/>
 * 
 * This class is intended as a temporary fix for retrieving recent inserts
 * and updates. Its main purpose is to bypass access of the Audit Manager by
 * running a query directly from the Data Package Manager's resource_registry
 * table. The reason we need to do this is because we've been experiencing
 * serious problems with Postgres performance on the Audit Manager server.
 */
public class UpdateStats {

	/* 
	 * Class variables 
	 */

	private static final Logger logger = Logger.getLogger(UpdateStats.class);
	private static final String RESOURCE_REGISTRY = "datapackagemanager.resource_registry";


	/* 
	 * Instance variables 
	 */

	private String dbDriver;
	private String dbUrl;
	private String dbUser;
	private String dbPassword;


	/* 
	 * Constructors 
	 */

	public UpdateStats() {

		Configuration options = ConfigurationListener.getOptions();

		this.dbDriver = options.getString("db.Driver");
		this.dbUrl = options.getString("db.pkg.URL");
		this.dbUser = options.getString("db.User");
		this.dbPassword = options.getString("db.Password");

		try {
			Class.forName(dbDriver);
		}
		catch (ClassNotFoundException e) {
			logger.error("GrowthStats: " + e.getMessage());
			e.printStackTrace();
		}

	}


	/* 
	 * Instance methods 
	 */

	
	/**
	 * Gets a list of recent uploads, either inserts or updates.
	 * 
	 * @param serviceMethod  one of "createDataPackage" or "updateDataPackage"
	 * @param fromTime       the cut-off date for how far back we want to query, e.g. '2015-01-01'
	 * @param limit          a limit on the number of uploads to return, e.g. 2
	 */
	public List<RecentUpload> getRecentUploads(String serviceMethod, String fromTime, Integer limit) 
			throws Exception {
		Connection conn = null;
		List<RecentUpload> recentUploadsList = new ArrayList<RecentUpload>();
		DataPackageManagerClient dpmClient = new DataPackageManagerClient("public");
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT scope, identifier, revision, date_created FROM ");
		sb.append(RESOURCE_REGISTRY);
		sb.append(" WHERE resource_type='dataPackage' AND ");
		sb.append("   date_deactivated IS NULL AND ");
		sb.append("   date_created > '" + fromTime + "'\n");
		sb.append("ORDER BY date_created DESC;");
		String sqlQuery = sb.toString();

		HashMap<String, Long> pkgMap;

		try {
			conn = getConnection(dbUrl, dbUser, dbPassword);

			if (conn != null) {
				Statement stmnt = conn.createStatement();
				ResultSet rs = stmnt.executeQuery(sqlQuery);

				while (rs.next()) {
					String scope = rs.getString(1);
					Integer identifier = rs.getInt(2);
					Integer revision = rs.getInt(3);
					java.sql.Date dateCreated = rs.getDate(4);
					String uploadDate = dateCreated.toString();
					
					String revisions = dpmClient.listDataPackageRevisions(scope, identifier, null);
					if (revisions != null) {
						String[] tokens = revisions.split("\n");
						if (tokens != null) {
							if ((tokens.length == 1) && (serviceMethod.equals("createDataPackage"))) {
								RecentUpload recentInsert = 
										new RecentUpload(dpmClient, uploadDate, serviceMethod, 
										                 scope, identifier.toString(), revision.toString());
								recentUploadsList.add(recentInsert);
							}
							else if ((tokens.length > 1) && (serviceMethod.equals("updateDataPackage"))) {
								RecentUpload recentUpdate = 
										new RecentUpload(dpmClient, uploadDate, serviceMethod, 
										                 scope, identifier.toString(), revision.toString());
								recentUploadsList.add(recentUpdate);
							}
						}
					}
					if (recentUploadsList.size() >= limit) {
						break;
					}
				}
			}

		}
		finally {
			returnConnection(conn);
		}

		return recentUploadsList;
	}


	private Connection getConnection(String dbUrl, String dbUser,
			String dbPassword) throws SQLException {

		Connection conn = null;
		SQLWarning warn;

		conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

		// If a SQLWarning object is available, print its warning(s).
		// There may be multiple warnings chained.
		warn = conn.getWarnings();

		if (warn != null) {
			while (warn != null) {
				logger.error("SQLState: " + warn.getSQLState());
				logger.error("Message:  " + warn.getMessage());
				logger.error("Vendor: " + warn.getErrorCode());
				warn = warn.getNextWarning();
			}
		}

		return conn;
	}


	/**
	 * Closes the connection to the database.
	 */
	public void returnConnection(Connection conn) {
		try {
			// Close the database connection
			logger.debug("Closing the database connection");

			if (conn != null) {
				conn.close();
			}
			else {
				logger.warn("Unable to close connection, 'conn' object is null");
			}
		}
		// We won't worry about throwing an exception if the connection return
		// fails. Just issue a log error.
		catch (SQLException e) {
			logger.error("Failed to close connection. Database access failed "
					+ e.getMessage());
		}
	}

}
