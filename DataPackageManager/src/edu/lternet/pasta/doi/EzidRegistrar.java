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

package edu.lternet.pasta.doi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
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
 * @author servilla
 * @author dcosta
 * @since Nov 17, 2012
 * 
 * 
 * The EzidRegistrar class extends the Registrar class. Its main job is to
 * load properties appropriate for connecting to the EZID DOI Registry.
 */
public class EzidRegistrar extends Registrar {

	/*
	 * Class variables
	 */


	/*
	 * Instance variables
	 */

	private String sessionId = null;

	
	/*
	 * Constructors
	 */

	public EzidRegistrar() throws ConfigurationException {
		super();    
	}

	
	/*
	 * Class methods
	 */

	/**
	 * EZID percent encoding of reserved characters: '%', '\n', '\r', and ':'.
	 * 
	 * @param s String to encode
	 * @return Encoded string
	 */
	private static String escape(String s) {
	  return s.replace("%", "%25").replace("\n", "%0A").
	      replace("\r", "%0D").replace(":", "%3A");
	  }
	
	
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

			this.host = options.getOption("datapackagemanager.ezidHost");
			this.port = options.getOption("datapackagemanager.ezidPort");
			this.protocol = options.getOption("datapackagemanager.ezidProtocol");
			this.registrarUser = options.getOption("datapackagemanager.ezidUser");
			this.registrarPassword = options.getOption("datapackagemanager.ezidPassword");

		} else {
			throw new ConfigurationException("Configuration options failed to load");
		}

	}

	
	/**
	 * Login to the DOI Registrar web service API system and return a valid session
	 * identifier.
	 * 
	 * @return The session id
	 * @throws RegistrarException
	 */
	public void login() throws RegistrarException {

		String sessionId = null;

		/*
		 * The following set of code sets up Preemptive Authentication for the HTTP
		 * CLIENT and is done so at the warning stated within the Apache
		 * Http-Components Client tutorial here:
		 * http://hc.apache.org/httpcomponents-
		 * client-ga/tutorial/html/authentication.html#d5e1031
		 */

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

	    // Generate BASIC scheme object and add it to the auth cache
	    BasicScheme basicAuth = new BasicScheme();
	    authCache.put(httpHost, basicAuth);

	    // Add AuthCache to the execution context
	    HttpClientContext context = HttpClientContext.create();
	    context.setCredentialsProvider(credentialsProvider);
	    context.setAuthCache(authCache);

		HttpGet httpGet = new HttpGet(this.getRegistrarUrl("/login"));

		HttpResponse response = null;
		Header[] headers = null;
		Integer statusCode = null;

		try {

			response = httpClient.execute(httpHost, httpGet, context);
			headers = response.getAllHeaders();
			statusCode = (Integer) response.getStatusLine().getStatusCode();
			logger.info("STATUS: " + statusCode.toString());

		} catch (UnsupportedEncodingException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		} finally {
			closeHttpClient(httpClient);
		}

		if (statusCode == HttpStatus.SC_OK) {

			String headerName = null;
			String headerValue = null;

			// Loop through all headers looking for the "Set-Cookie" header.
			for (int i = 0; i < headers.length; i++) {
				headerName = headers[i].getName();

				if (headerName.equals("Set-Cookie")) {
					headerValue = headers[i].getValue();
					sessionId = this.getSessionId(headerValue);
					logger.info("Session: " + sessionId);
				}

			}

		} else {
			String gripe = "login: failed DOI Registrar login.";
			throw new RegistrarException(gripe);
		}

		this.sessionId = sessionId;

	}

	
	/**
	 * Logout of the DOI Registrar service session.
	 * 
	 * @throws RegistrarException
	 */
	public void logout() throws RegistrarException {

	    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String url = this.getRegistrarUrl("/logout");
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;
		Integer statusCode = null;

		// Set header content
		if (this.sessionId != null) {
			httpGet.setHeader("Cookie", "sessionid=" + this.sessionId);
		}

		try {

			HttpResponse httpResponse = httpClient.execute(httpGet);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);

		} catch (ClientProtocolException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			closeHttpClient(httpClient);
		}

		if (statusCode != HttpStatus.SC_OK) {
			String gripe = "Failed to logout of DOI registrar cleanly.";
			throw new RegistrarException(gripe);
		}

		logger.info("logout: " + entityString);

	}

	
	/**
	 * Parse the "Set-Cookie" header looking for the "sessionid" key-value pair
	 * and return the session identifier.
	 * 
	 * @param setCookieHeader
	 *          The full "Set-Cookie" header.
	 * 
	 * @return The session identifier
	 */
	protected String getSessionId(String setCookieHeader) {

		String sessionId = null;

		String[] headerParts = setCookieHeader.split(";");

		for (int i = 0; i < headerParts.length; i++) {

			// Extract token value from the key-value pair.
			if (headerParts[i].startsWith("sessionid=")) {
				int start = "sessionid=".length();
				int end = headerParts[i].length() - 1;
				sessionId = headerParts[i].substring(start, end);
			}

		}

		return sessionId;

	}

	
	/**
	 * Gets the service session identifier.
	 * 
	 * @return service session identifier
	 */
	protected String getSessionId() {
		return this.sessionId;
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
			String gripe = "registerDataCiteMetadata: DataCite metadata object is null.";
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

		String doi = dataCiteMetadata.getDigitalObjectIdentifier().getDoi();
		String url = this.getRegistrarUrl("/id/" + doi);
		StringBuffer metadata = new StringBuffer("");
		String dataCiteXMLUnescaped = dataCiteMetadata.toDataCiteXml();
		String dataCiteXML = EzidRegistrar.escape(dataCiteXMLUnescaped);
		metadata
		    .append("datacite: " + dataCiteXML + "\n");
		metadata
		    .append("_target: " + dataCiteMetadata.getLocationUrl() + "\n");
		HttpPut httpPut = new HttpPut(url);
		httpPut.setHeader("Content-type", "text/plain");
		HttpEntity stringEntity = null;
		Integer statusCode = null;
		String entityString = null;

		try {
			stringEntity = new StringEntity(metadata.toString());
			httpPut.setEntity(stringEntity);
			HttpResponse httpResponse = httpClient.execute(httpHost, httpPut, context);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			closeHttpClient(httpClient);
		}

		logger.info("registerDataCiteMetadata: " + dataCiteMetadata.getLocationUrl() + "\n" + entityString);

		// Test for DOI collision or DOI registration failure
		if ((statusCode == HttpStatus.SC_BAD_REQUEST) && 
			(entityString != null) && 
			(entityString.contains("identifier already exists"))
		   ) {
			String gripe = "identifier already exists";
			throw new RegistrarException(gripe);
		} else if (statusCode != HttpStatus.SC_CREATED) {
			String gripe = "DOI registration failed for: " + doi;
			throw new RegistrarException(gripe);
		}

	}

	
	/**
	 * Make the DOI obsolete by setting the Datacite metadata field "_status" to
	 * "unavailable".
	 * 
	 * @param doi The DOI to obsolete
	 * @throws RegistrarException
	 */
	public void obsoleteDoi(String doi) throws RegistrarException {

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
		
		String url = this.getRegistrarUrl("/id/" + doi);

		StringBuffer metadata = new StringBuffer("");
		metadata.append("_status: unavailable | withdrawn by author\n");
		
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Content-type", "text/plain");
		HttpEntity stringEntity = null;
		Integer statusCode = null;
		String entityString = null;

		try {
			stringEntity = new StringEntity(metadata.toString());
			httpPost.setEntity(stringEntity);
			HttpResponse httpResponse = httpClient.execute(httpHost, httpPost, context);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			closeHttpClient(httpClient);
		}

		logger.info("obsoleteDoi: " + entityString);

	 if (statusCode != HttpStatus.SC_OK) {
			String gripe = "DOI obsoletion failed for: " + doi;
			throw new RegistrarException(gripe);
		}

	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		EzidRegistrar ezidRegistrar = null;

		try {
			ezidRegistrar = new EzidRegistrar();
			ezidRegistrar.obsoleteDoi("doi:10.6073/pasta/dcbd7c1aab57af6a65672aa917bb3faf");
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

}
