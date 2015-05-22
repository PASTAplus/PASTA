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

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.portal.ConfigurationListener;
import edu.lternet.pasta.portal.database.DatabaseClient;
import edu.lternet.pasta.portal.search.Search;


/**
 * The SavedData class manages saved data packages for the user.
 * 
 * @author dcosta
 * 
 */
public class SavedData extends Search {

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
		String scope1 = "knb-lter-nin";
		Integer identifier1 = new Integer(1);
		String scope2 = "knb-lter-sbc";
		Integer identifier2 = new Integer(28);
		Integer revision = new Integer(1);
		String savedDataXML = null;

		ConfigurationListener.configure();
		SavedData savedData = new SavedData(uid);

		savedData.addDocid(scope1, identifier1, revision);
		savedData.addDocid(scope2, identifier2, revision);
		List<String> dataStore = savedData.fetchDataStore();
		String savedDataList = savedData.getSavedDataList();
		System.out.println(String.format("Saved data list: %s", savedDataList));
		try {
			savedDataXML = savedData.getSavedDataXML("0", "10", null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//savedData.removeDocid(scope2, identifier2);
		dataStore = savedData.fetchDataStore();
		//savedData.removeAllDataPackages();
	}

	
	/*
	 * Instance methods
	 */
	
	
	/*
	 * Gets a subset of the data packages saved by a particular user and returns
	 * it as XML-formatted search results. The subset returned depends on the
	 * start position and number of items (i.e. rows) to be returned.
	 * 
	 * @param  startStr   The start position, e.g. "0"
	 * @param  rowsStr    The number of rows to return, e.g. "10"
	 * @param  sort       The sort value, e.g. "title,asc"
	 */
	public String getSavedDataXML(String startStr, String rowsStr, String sort) throws Exception {
		String savedData = null;
		ArrayList<String> dataStore = fetchDataStore();
		Integer start, rows;
		
		if (startStr == null) {
			start = DEFAULT_START;
		}
		else {
			start = new Integer(startStr);
		}
		
		if (rowsStr == null) {
			rows = DEFAULT_ROWS;
		}
		else {
			rows = new Integer(rowsStr);
		}
		
		if (sort == null) {
			sort = String.format("%s,%s", Search.PACKAGEID_SORT, Search.SORT_ORDER_ASC);
		}
		
		if (dataStore.size() > 0) {
			String queryString = composeQueryString(dataStore);
			
			if (queryString != null) {
				DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
				String extendedQueryString = String.format("%s&start=%d&rows=%d&sort=%s", queryString, start, rows, sort);
				logger.warn(String.format("query:\n%s", extendedQueryString));
				String resultsetXML = dpmClient.searchDataPackages(extendedQueryString);
				savedData = resultsetXML;
			}
		}
		
		return savedData;
	}
	
	
	/*
	 * Gets the list of all data packages saved by a particular user and returns
	 * it as a comma-separated list.
	 */
	public String getSavedDataList() {
		String savedDataList = null;
		StringBuilder listBuilder = new StringBuilder("");
		ArrayList<String> dataStore = fetchDataStore();
		
		for (String docid : dataStore) {
			listBuilder.append(String.format("%s,", docid));
		}
		
		savedDataList = listBuilder.toString();
		if (savedDataList.length() > 0) {
			//savedDataList = savedDataList.substring(0, savedDataList.length() - 1); // trim off trailing ','
		}
		
		return savedDataList;
	}
	
	
	private String composeQueryString(ArrayList<String> dataStore) {
		String queryString = null;
		StringBuilder queryBuilder = new StringBuilder("id:(");
		
		if (dataStore != null) {
			String plus = "";
			for (String docid : dataStore) {
				queryBuilder.append(String.format("%s%s", plus, docid));
				plus = "+";
			}
		    queryBuilder.append(")");	
		
		    String qString = queryBuilder.toString().trim();
		    queryString = String.format(
				"defType=%s&q=%s&fl=%s&debug=%s",
				DEFAULT_DEFTYPE, qString, DEFAULT_FIELDS, DEFAULT_DEBUG
		    );
		}
		
		return queryString;
	}

	
	/**
	 * Fetches the full list of saved data packages for this user.
	 * 
	 * @return a list of docid strings, where each string is a value 
	 *         such as "knb-lter-nin.1"
	 */
	public ArrayList<String> fetchDataStore() {
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
	 * Boolean to determine whether the specified docid has been saved for this user.
	 * 
	 * @param scope         the scope of the data package to check
	 * @param identifer     the identifier value of the data package to check
	 * @return   true if the user has already saved the data package, else false
	 */
	public boolean hasDocid(String scope, Integer identifier) {
		boolean hasDocid = false;
		Connection conn = databaseClient.getConnection();
		
		if (conn != null && uid != null) {
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
		}
		
		return hasDocid;
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
