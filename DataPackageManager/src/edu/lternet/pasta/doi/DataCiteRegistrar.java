/*
 *
 * Copyright 2017 the University of New Mexico.
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
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import edu.ucsb.nceas.utilities.Options;

/**
 * @author dcosta
 * @since Nov 17, 2012
 * 
 * The DataCiteRegistrar class extends the Registrar class. Its main job is to
 * load properties appropriate for connecting to the DataCite DOI Registry.
 * 
 */
public class DataCiteRegistrar extends Registrar {

	/*
	 * Class variables
	 */


	/*
	 * Instance variables
	 */


	/*
	 * Constructors
	 */

	public DataCiteRegistrar() throws ConfigurationException {
		super();    
	}

	
	/*
	 * Class methods
	 */

	
	/*
	 * Instance methods
	 */

	/**
	 * Loads Data Manager options from a configuration file.
	 * 
	 * @param options Configuration options object.
	 * @throws ConfigurationException
	 */
	protected void loadOptions(Options options) throws ConfigurationException {
		if (options != null) {

			this.host = options.getOption("datapackagemanager.dataciteHost");
			this.port = options.getOption("datapackagemanager.datacitePort");
			this.protocol = options.getOption("datapackagemanager.dataciteProtocol");
			this.registrarUser = options.getOption("datapackagemanager.dataciteUser");
			this.registrarPassword = options.getOption("datapackagemanager.datacitePassword");
		} 
		else {
			throw new ConfigurationException("Configuration options failed to load");
		}

	}
	
	
	private String getMintDoiUrl() {
		String url = getRegistrarUrl("/doi");
		
		return url;
	}

	
	private String getPostMetadataUrl() {
		String url = getRegistrarUrl("/metadata");
		
		return url;
	}

	
	/**
	 * Mints a new DOI.
	 * 
	 * @throws RegistrarException
	 */
	private void mintDOI(String doi, String landingPageURL)
			throws Exception {
		if (doi == null) {
			String gripe = "doi parameter is null.";
			throw new RegistrarException(gripe);
		}
		
		if (landingPageURL == null) {
			String gripe = "landingPageURL parameter is null.";
			throw new RegistrarException(gripe);
		}
		
		HttpHost httpHost = new HttpHost(this.host, Integer.valueOf(this.port), this.protocol);
	    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		AuthScope authScope = new AuthScope(httpHost.getHostName(), httpHost.getPort());
		UsernamePasswordCredentials credentials = 
				new UsernamePasswordCredentials(this.registrarUser, this.registrarPassword);
	    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	    credentialsProvider.setCredentials(authScope, credentials);

	    // Create AuthCache instance
	    AuthCache authCache = new BasicAuthCache();

	    // Generate BASIC scheme object and add it to the local auth cache
	    BasicScheme basicAuth = new BasicScheme();
	    authCache.put(httpHost, basicAuth);

	    // Add AuthCache to the execution context
	    HttpClientContext context = HttpClientContext.create();
	    context.setCredentialsProvider(credentialsProvider);
	    context.setAuthCache(authCache);

		StringBuffer requestBodyBuffer = new StringBuffer("");
		requestBodyBuffer.append("doi=" + doi + "\n");
		requestBodyBuffer.append("url=" + landingPageURL + "\n");
		String mintDoiUrl = getMintDoiUrl();
		HttpPost httpPost = new HttpPost(mintDoiUrl);
		httpPost.setHeader("Content-type", "text/plain");
		HttpEntity stringEntity = null;
		Integer statusCode = null;
		String entityString = null;
		String requestBody = requestBodyBuffer.toString();

		try {
			stringEntity = new StringEntity(requestBody);
			httpPost.setEntity(stringEntity);
			logger.info("mintDOI request body:\n" + requestBody);
			HttpResponse httpResponse = httpClient.execute(httpHost, httpPost, context);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			logger.info("mintDOI response body:\n" + entityString);
		} 
		finally {
			closeHttpClient(httpClient);
		}
		
		String msg = null;

		// Test for DOI collision or DOI registration failure
		if (statusCode == HttpStatus.SC_BAD_REQUEST) {
			msg = "Bad request. Identifier already exists: ";
		} 
		else if (statusCode != HttpStatus.SC_UNAUTHORIZED) {
			msg = "Unauthorized request to mint DOI: ";
		}
		else if (statusCode != HttpStatus.SC_FORBIDDEN) {
			msg = "Forbidden: login problem, quota exceeded: ";
		}
		else if (statusCode != HttpStatus.SC_PRECONDITION_FAILED) {
			msg = "Metadata must be uploaded first: ";
		}
		else if (statusCode != HttpStatus.SC_INTERNAL_SERVER_ERROR) {
			msg = "Internal server error: ";
		}
		
		if (statusCode != HttpStatus.SC_CREATED) {
			if (msg == null) msg = "Minting of DOI failed for unknown reason: ";
			msg += doi;
			throw new RegistrarException(msg);
		}
	}

	
	/**
	 * Registers the resource DOI based on the DataCite metadata object.
	 * 
	 * @throws RegistrarException
	 */
	@Override
	public void registerDataCiteMetadata(DataCiteMetadata dataCiteMetadata) 
			throws Exception {

		if (dataCiteMetadata == null) {
			String gripe = "DataCiteMetadata object is null.";
			throw new RegistrarException(gripe);
		}

		HttpHost httpHost = new HttpHost(this.host, Integer.valueOf(this.port),
		    this.protocol);
	    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		AuthScope authScope = new AuthScope(httpHost.getHostName(),
		    httpHost.getPort());
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
		    this.registrarUser, this.registrarPassword);
	    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	    credentialsProvider.setCredentials(authScope, credentials);

	    // Create AuthCache instance
	    AuthCache authCache = new BasicAuthCache();

	    // Generate BASIC scheme object and add it to the local auth cache
	    BasicScheme basicAuth = new BasicScheme();
	    authCache.put(httpHost, basicAuth);

	    // Add AuthCache to the execution context
	    HttpClientContext context = HttpClientContext.create();
	    context.setCredentialsProvider(credentialsProvider);
	    context.setAuthCache(authCache);

		String doi = dataCiteMetadata.getDigitalObjectIdentifier().getIdentifier();
		String postMetadataURL = getPostMetadataUrl();
		String landingPageUrl = dataCiteMetadata.getLocationUrl();
		
		String metadataXML = dataCiteMetadata.toDataCiteXml() + "\n";
		HttpPost httpPost = new HttpPost(postMetadataURL);
		httpPost.setHeader("Content-type", "application/xml");
		HttpEntity stringEntity = null;
		Integer statusCode = null;
		String entityString = null;

		try {
			stringEntity = new StringEntity(metadataXML);
			httpPost.setEntity(stringEntity);
			logger.info("Create metadata request body:\n" + metadataXML);
			HttpResponse httpResponse = httpClient.execute(httpHost, httpPost, context);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			logger.info("Create metadata response body:\n" + entityString);
		} 
		finally {
			closeHttpClient(httpClient);
		}

		/*
		 * If we succeeded in registering the metadata, the next step is to mint the DOI.
		 */
		if (statusCode == HttpStatus.SC_CREATED) {
			logger.info(String.format("Created DataCite metadata for DOI: %s", doi));
			mintDOI(doi, landingPageUrl);
		}
		else {
			String msgHeader = "";

			// Test for DOI collision or DOI registration failure
			if (statusCode == HttpStatus.SC_BAD_REQUEST) {
				msgHeader = "Bad request. Identifier already exists: ";
			} 
			else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
				msgHeader = "Unauthorized request to mint DOI: ";
			}
			else if (statusCode == HttpStatus.SC_FORBIDDEN) {
				msgHeader = "Forbidden: login problem, quota exceeded: ";
			}
			else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
				msgHeader = "Internal server error: ";
			}
			else if (statusCode == HttpStatus.SC_GATEWAY_TIMEOUT) {
				msgHeader = "Gateway Timeout error: ";
			}
		
            String msg = String.format(
               "%sCreation of DataCite metadata for DOI '%s' failed with status code '%d'",
               msgHeader, doi, statusCode);
            
			throw new RegistrarException(msg);
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DataCiteRegistrar dataciteRegistrar = null;

		try {
			dataciteRegistrar = new DataCiteRegistrar();

			String mintDoiUrl = dataciteRegistrar.getMintDoiUrl();
			System.out.println("mintDoiUrl: " + mintDoiUrl);

			String postMetadataUrl = dataciteRegistrar.getPostMetadataUrl();
			System.out.println("postMetadataUrl: " + postMetadataUrl);

			System.out.println("host: " + dataciteRegistrar.host);
			System.out.println("port: " + dataciteRegistrar.port);
			System.out.println("protocol: " + dataciteRegistrar.protocol);
			System.out.println("registrarUser: " + dataciteRegistrar.registrarUser);
			System.out.println("registrarPassword: " + dataciteRegistrar.registrarPassword);			
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

}
