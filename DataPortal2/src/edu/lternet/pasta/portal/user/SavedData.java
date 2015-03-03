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

package edu.lternet.pasta.portal.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.portal.ConfigurationListener;
import edu.lternet.pasta.portal.database.DatabaseClient;


/**
 * The SavedData class manages saved data packages for the user.
 * 
 * @author dcosta
 * 
 */
public class SavedData {

	/*
	 * Class fields
	 */

	private static final Logger logger = Logger.getLogger(SavedData.class);

	/*
	 * Instance fields
	 */

	DatabaseClient databaseClient;
	private final String TABLE_NAME = "authtoken.saved_data";
	private String uid;


	/*
	 * Constructors
	 */

	public SavedData(String uid) {
		this.uid = uid;
		Configuration options = ConfigurationListener.getOptions();		
		String dbDriver = options.getString("db.Driver");
		String dbURL = options.getString("db.URL");
		String dbUser = options.getString("db.User");
		String dbPassword = options.getString("db.Password");
		this.databaseClient = new DatabaseClient(dbDriver, dbURL, dbUser, dbPassword);
	}


	/*
	 * Class methods
	 */

    /**
     * @param args
     */
	public static void main(String[] args) {
		String uid = "ucarroll";
		String scope = "knb-lter-nin";
		Integer identifier1 = new Integer(1);
		Integer identifier2 = new Integer(2);
		Integer revision = new Integer(1);

		ConfigurationListener.configure();
		SavedData savedData = new SavedData(uid);

		savedData.addDocid(scope, identifier1, revision);
		savedData.addDocid(scope, identifier2, revision);
		List<String> dataStore = savedData.fetchDataStore();
		savedData.removeDocid(scope, identifier2);
		dataStore = savedData.fetchDataStore();
		savedData.removeAllDataPackages();
	}

	
	/*
	 * Instance methods
	 */

	/**
	 * Fetches the full list of saved data packages for this user.
	 * 
	 * @return a list of docid strings, where each string is a value 
	 *         such as "knb-lter-nin.1"
	 */
	public List<String> fetchDataStore() {
		ArrayList<String> docids = new ArrayList<String>();
		
		Connection conn = databaseClient.getConnection();		
		if (conn != null) {
			String sqlQuery = String.format("SELECT DISTINCT scope, identifier FROM %s WHERE user_id=?",
				                        	TABLE_NAME);
			try {
				PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
				pstmt.setString(1, uid);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					String scope = rs.getString(1);
					int identifier = rs.getInt(2);
					String docid = String.format("%s.%d", scope, identifier);
					docids.add(docid);
				}
			}
			catch (SQLException e) {
				logger.error("Data store load failed. SQLException: " + e.getMessage());
			}
			finally {
				databaseClient.closeConnection(conn);
			}
		}
		else {
			logger.error("Data store load failed. Error getting connection.");
		}

		return docids;
	}


	/**
	 * Adds a data package to saved data for this user.
	 * 
	 * @param scope         the scope of the data package to be saved
	 * @param identifer     the identifier value of the data package to be saved
	 * @param revision      the revision value of the data package to be saved
	 */
	public void addDocid(String scope, Integer identifier, Integer revision) {
		Connection conn = databaseClient.getConnection();
		String docid = String.format("%s.%s", scope, identifier.toString());
		
		if (conn != null && uid != null) {
			boolean hasDocid = false;
			String SQL_QUERY = ("SELECT scope, identifier FROM " + TABLE_NAME + 
						        " WHERE user_id=? AND scope=? AND identifier=?");
			logger.debug("SQL_QUERY: " + SQL_QUERY);

			try {
				PreparedStatement pstmt = conn.prepareStatement(SQL_QUERY);
				pstmt.setString(1, uid);
				pstmt.setString(2, scope);
				pstmt.setInt(3, identifier);
				ResultSet rs = pstmt.executeQuery();
				while (rs.next()) {
					hasDocid = true;
					break;
				}
			}
			catch (SQLException e) {
				logger.error("SQLException: " + e.getMessage());
			}

			logger.debug("hasDocid: " + hasDocid);
			
			// If not already saved, then insert the entry for this user and docid
			if (!hasDocid) {
				String updateSQL = String.format("INSERT INTO %s VALUES(?, ?, ?, ?)",
												TABLE_NAME);
				logger.debug("INSERT statement: " + updateSQL);

				try {
					PreparedStatement pstmt = conn.prepareStatement(updateSQL);
					pstmt.setString(1, uid);
					pstmt.setString(2, scope);
					pstmt.setInt(3, identifier);
					pstmt.setInt(4, revision);
					pstmt.executeUpdate();
					pstmt.close();
				}
				catch (SQLException e) {
					logger.error(String.format("%s insert failed: %s", TABLE_NAME, e.getMessage()));
				}
				finally {
					databaseClient.closeConnection(conn);
				}
			}
			else {
				logger.warn(String.format("Not inserting new entry: %s already has %s in table %s",
										  uid, docid, TABLE_NAME));
			}
		}
		else {
			logger.error(TABLE_NAME + " insert failed. Error getting connection.");
		}
	}


	/**
	 * Remove all data packages (i.e. all docids) from the saved data for this user.
	 */
	public void removeAllDataPackages() {
		Connection conn = databaseClient.getConnection();

		if (conn != null) {
			if (uid != null) {
				String updateSQL = String.format("DELETE FROM %s WHERE user_id=?", TABLE_NAME);
				logger.debug("DELETE statement: " + updateSQL);

				try {
					PreparedStatement pstmt = conn.prepareStatement(updateSQL);
					pstmt.setString(1, uid);
					pstmt.executeUpdate();
					pstmt.close();
				}
				catch (SQLException e) {
					logger.error(String.format("Delete from %s failed. SQLException: %s",
							                   TABLE_NAME, e.getMessage()));
				}
				finally {
					databaseClient.closeConnection(conn);
				}
			}
		}
		else {
			logger.error("Error getting connection.");
		}
	}


	/**
	 * Removes a data package from the saved data for this user.
	 * 
	 * @param scope   the scope value of the data package to be removed
	 * @param identifer   the identifier value of the data package to be removed
	 */
	public void removeDocid(String scope, Integer identifier) {
		Connection conn = databaseClient.getConnection();

		if (conn != null) {
			String updateSQL = 
				String.format("DELETE FROM %s WHERE user_id=? AND scope=? AND identifier=?", 
							   TABLE_NAME);
			logger.debug("DELETE statement: " + updateSQL);

			try {
				PreparedStatement pstmt = conn.prepareStatement(updateSQL);
				pstmt.setString(1, uid);
				pstmt.setString(2, scope);
				pstmt.setInt(3, identifier);
				pstmt.executeUpdate();
				pstmt.close();
			}
			catch (SQLException e) {
				logger.error(String.format("Delete from %s failed. SQLException: %s",
							 TABLE_NAME, e.getMessage()));
			}
			finally {
				databaseClient.closeConnection(conn);
			}
		}
		else {
			logger.error("Error getting connection.");
		}
	}

}
