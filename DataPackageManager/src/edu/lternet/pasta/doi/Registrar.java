/*
 *
 * Copyright 2011-2017 the University of New Mexico.
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

package edu.lternet.pasta.doi;

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.ucsb.nceas.utilities.Options;

/**
 * 
 * @author dcosta
 * @author servilla
 * 
 *         Registrar is an abstract class. It implements logic common to all DOI
 *         registration sub-classes, such as DataCiteRegistrar and
 *         EzidRegistrar.
 *
 */
public abstract class Registrar {

	/*
	 * Class variables
	 */

	protected static final Logger logger = Logger.getLogger(edu.lternet.pasta.doi.Registrar.class);
	protected static final String dirPath = "WebRoot/WEB-INF/conf";

	/*
	 * Instance variables
	 */

	protected String host = null;
	protected String port = null;
	protected String protocol = null;
	protected String registrarUser = null;
	protected String registrarPassword = null;

	/*
	 * Constructors
	 */

	public Registrar() throws ConfigurationException {
		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		this.loadOptions(options); // implemented by sub-classes
	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	/*
	 * Closes the HTTP client
	 */
	protected void closeHttpClient(CloseableHttpClient httpClient) {
		try {
			httpClient.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Loads Data Manager options from a configuration file.
	 * 
	 * @param options
	 *            Configuration options object.
	 * @throws ConfigurationException
	 */
	protected abstract void loadOptions(Options options) throws ConfigurationException;

	
	/**
	 * Registers DataCiteMetadata with the DOI registrar.
	 * 
	 * @param dataCiteMetadata    
	 * 			  The DataCiteMetadata object containing the metadata
	 * @throws RegistrarException 
	 */
	protected abstract void registerDataCiteMetadata(DataCiteMetadata dataCiteMetadata) throws Exception;

	
	/**
	 * Builds full URL to DOI registrar services.
	 * 
	 * @param url
	 *            The Registrar URL path
	 * @return The full URL
	 */
	protected String getRegistrarUrl(String url) {
		return this.protocol + "://" + this.host + url;
	}

	
	public void registerDataCiteMetadata() throws RegistrarException {

	}
}
