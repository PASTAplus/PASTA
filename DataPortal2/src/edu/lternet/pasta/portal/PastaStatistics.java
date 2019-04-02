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

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;

import org.apache.commons.lang3.text.StrTokenizer;
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

	private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.PastaStatistics.class);

	/*
	 * Instance variables
	 */

	private String uid = null;
	private DataPackageManagerClient dpmClient = null;

	
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
	public PastaStatistics(String uid) throws PastaAuthenticationException,
	    PastaConfigurationException {
		this.uid = uid;
		this.dpmClient = new DataPackageManagerClient(uid);
	}

	
	/*
	 * Class methods
	 */

	
	/*
	 * Instance methods
	 */
	
	/**
	 * Iterates through the list of scopes and identifiers to calculate
	 * the number of data packages in PASTA.
	 * 
	 * @return The number of data packages.
	 */
	public Integer getNumDataPackages() {
		Integer numDataPackages = 0;
		String scopeList = null;

		try {
			scopeList = this.dpmClient.listDataPackageScopes();
		} catch (Exception e) {
			logger.error("PastaStatistics: " + e.getMessage());
			e.printStackTrace();
		}

		StrTokenizer scopes = new StrTokenizer(scopeList);

		while (scopes.hasNext()) {
			String scope = scopes.nextToken();

			String idList = null;

			try {
				idList = this.dpmClient.listDataPackageIdentifiers(scope);
			} catch (Exception e) {
				logger.error("PastaStatistics: " + e.getMessage());
				e.printStackTrace();
			}

			StrTokenizer identifiers = new StrTokenizer(idList);

			numDataPackages += identifiers.size();
		}

		return numDataPackages;
	}
	
	
	/**
	 * Iterates through the list of scopes and identifiers of site contributed
	 * data packages to calculate the total number of site contributed data
	 * packages in PASTA (i.e., excludes EcoTrends and LTER Landsat data
	 * packages).
	 * 
	 * @return The number of site contributed data packages.
	 */
	public Integer getNumDataPackagesSites() {
		Integer numDataPackages = 0;
		String scopeList = null;

		try {
			scopeList = this.dpmClient.listDataPackageScopes();
		} catch (Exception e) {
			logger.error("PastaStatistics: " + e.getMessage());
			e.printStackTrace();
		}

		StrTokenizer scopes = new StrTokenizer(scopeList);

		while (scopes.hasNext()) {
			String scope = scopes.nextToken();

			if (!scope.equals("ecotrends") && 
				!scope.startsWith("lter-landsat")
			   ) {
				String idList = null;

				try {
					idList = this.dpmClient.listDataPackageIdentifiers(scope);
				} catch (Exception e) {
					logger.error("PastaStatistics: " + e.getMessage());
					e.printStackTrace();
				}

				StrTokenizer identifiers = new StrTokenizer(idList);

				numDataPackages += identifiers.size();
			}
		}

		return numDataPackages;
	}
	
}
