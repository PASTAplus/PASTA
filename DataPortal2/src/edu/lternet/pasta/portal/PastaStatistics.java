/*
 *
 * Copyright 2011, 2012, 2013 the University of New Mexico.
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

package edu.lternet.pasta.portal;

import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.portal.ConfigurationListener;
import edu.lternet.pasta.portal.database.DatabaseClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/**
 * @author servilla
 * @since Jan 27, 2013
 * 
 * Provides simple PASTA data package statistics.
 * 
 */
public class PastaStatistics {

	/*
	 * Class variables
	 */

	private static final Logger logger = 
		Logger.getLogger(edu.lternet.pasta.portal.PastaStatistics.class);
	
	
    private static final String RESOURCE_REGISTRY = "datapackagemanager.resource_registry";
    
	private static final String QUERY_CONTRIBUTED_UNIQUE = String.format(
			"SELECT DISTINCT scope, identifier FROM %s WHERE resource_type='dataPackage' AND date_deactivated IS NULL AND scope != 'ecotrends' AND scope NOT LIKE 'lter-landsat%%'",
			RESOURCE_REGISTRY);
	private static final String QUERY_CONTRIBUTED_ALL = String.format(
			"SELECT DISTINCT scope, identifier, revision FROM %s WHERE resource_type='dataPackage' AND date_deactivated IS NULL AND scope != 'ecotrends' AND scope NOT LIKE 'lter-landsat%%'",
			RESOURCE_REGISTRY);
	private static final String QUERY_TOTAL_UNIQUE = String.format(
			"SELECT DISTINCT scope, identifier FROM %s WHERE resource_type='dataPackage' AND date_deactivated IS NULL",
			RESOURCE_REGISTRY);
	private static final String QUERY_TOTAL_ALL = String.format(
			"SELECT DISTINCT scope, identifier, revision FROM %s WHERE resource_type='dataPackage' AND date_deactivated IS NULL",
			RESOURCE_REGISTRY);

	/*
	 * Instance variables
	 */

	private DatabaseClient databaseClient = null;

	
	/*
	 * Constructors
	 */

	/**
	 * Constructs a new PastaStatistic object for user "uid".
	 * 
	 * @param uid The user identifier.
	 * @throws PastaAuthenticationException
	 * @throws PastaConfigurationException
	 */
	public PastaStatistics(String uid) 
			throws PastaAuthenticationException, PastaConfigurationException {
	    Configuration options = ConfigurationListener.getOptions();

	    String dbDriver = options.getString("db.pkg.Driver");
	    String dbUrl = options.getString("db.pkg.URL");
	    String dbUser = options.getString("db.pkg.User");
	    String dbPassword = options.getString("db.pkg.Password");
		this.databaseClient = new DatabaseClient(dbDriver, dbUrl, dbUser, dbPassword);
	}

	
	/*
	 * Class methods
	 */

	
	/*
	 * Instance methods
	 */
	
	private Integer countDataPackages(String sql) throws SQLException {
		Integer packageCount = null;
		Connection conn = databaseClient.getConnection();

		if (conn != null) {
			try {
				Statement stmnt = conn.createStatement();
				ResultSet rs = stmnt.executeQuery(sql);

				int i = 0;
				while (rs.next()) {
					i++;
				}
				packageCount = new Integer(i);
			} 
			finally {
				databaseClient.closeConnection(conn);
			}
		}

		return packageCount;
	}
		 
	
	/**
	 * Iterates through the list of scopes and identifiers to calculate
	 * the number of unique data packages in PASTA (i.e., doesn't count
	 * multiple revisions of the same data package.)
	 * 
	 * @param includeEcotrendsAndLandsat  
	 *          if true, include Ecotrends and Landsat data packages
	 *    
	 * 
	 * @return The number of data packages.
	 */
	public Integer getNumDataPackages(boolean includeEcotrendsAndLandsat) 
		    throws SQLException {
		Integer numDataPackages = 0;
		
		if (includeEcotrendsAndLandsat) {
			numDataPackages = countDataPackages(QUERY_TOTAL_UNIQUE);
		}
		else {
			numDataPackages = countDataPackages(QUERY_CONTRIBUTED_UNIQUE);
		}
		
		return numDataPackages;
	}
	
	
	/**
	 * Iterates through the list of scopes and identifiers and revisions to 
	 * calculate the number of data packages in PASTA (including all revisions).
	 * 
	 * @param includeEcotrendsAndLandsat  
	 *          if true, include Ecotrends and Landsat data packages

	 * @return The number of data packages.
	 */
	public Integer getNumDataPackagesAllRevisions(boolean includeEcotrendsAndLandsat)
		    throws SQLException {
		Integer numDataPackages = 0;
		
		if (includeEcotrendsAndLandsat) {
			numDataPackages = countDataPackages(QUERY_TOTAL_ALL);
		}
		else {
			numDataPackages = countDataPackages(QUERY_CONTRIBUTED_ALL);
		}
		
		return numDataPackages;
	}
	
	
	public static void main(String[] args) {
		ConfigurationListener.configure();

		try {
			PastaStatistics pastaStatistics = new PastaStatistics("public");
			Integer totalUnique = pastaStatistics.getNumDataPackages(true);
			System.out.println("totalUnique: " + totalUnique);
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}
