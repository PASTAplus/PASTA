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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Nov 17, 2012
 * 
 */
public class EzidRegistrar {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(edu.lternet.pasta.doi.EzidRegistrar.class);

	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static final String loginUrl = "/ezid/login";

	/*
	 * Instance variables
	 */

	private String host = null;
	private String port = null;
	private String protocol = null;

	private String doiTest = null;
	private String ezidHost = null;
	private String ezidHostPort = null;
	private String ezidHostProtocol = null;
	private String ezidStageHost = null;
	private String ezidStagePort = null;
	private String ezidStageProtocol = null;
	private String ezidUser = null;
	private String ezidPassword = null;
	private String keystore = null;
	private String keystorePassword = null;

	private String sessionId = null;

	/*
	 * Constructors
	 */

	public EzidRegistrar() throws Exception {

		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		this.loadOptions(options);

		// Do special processing if using EZID stage environemnt
		if (this.doiTest.equals("true")) {

			this.host = this.ezidStageHost;
			this.port = this.ezidStagePort;
			this.protocol = this.ezidStageProtocol;
			
			System.setProperty("javax.net.ssl.trustStore", this.keystore);
			System.setProperty("javax.net.ssl.trustStorePassword",
			    this.keystorePassword);
			
		} else {
			
			this.host = this.ezidHost;
			this.port = this.ezidHostPort;
			this.protocol = this.ezidHostProtocol;
			
		}


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
	 * @param options
	 *          Configuration options object.
	 */
	private void loadOptions(Options options) throws Exception {

		if (options != null) {

			// Load EZID options
			this.doiTest = options.getOption("datapackagemanager.doiTest");
			this.ezidHost = options.getOption("datapackagemanager.ezidHost");
			this.ezidHostPort = options.getOption("datapackagemanager.ezidHostPort");
			this.ezidHostProtocol = options
			    .getOption("datapackagemanager.ezidHostProtocol");
			this.ezidStageHost = options
			    .getOption("datapackagemanager.ezidStageHost");
			this.ezidStagePort = options
			    .getOption("datapackagemanager.ezidStagePort");
			this.ezidStageProtocol = options
			    .getOption("datapackagemanager.ezidStageProtocol");
			this.ezidUser = options.getOption("datapackagemanager.ezidUser");
			this.ezidPassword = options.getOption("datapackagemanager.ezidPassword");
			this.keystore = options.getOption("datapackagemanager.keystore");
			this.keystorePassword = options
			    .getOption("datapackagemanager.keystorePassword");

		} else {
			throw new Exception("Configuration options failed to load.");
		}

	}

	/**
	 * Login to the EZID web service API system and return a valid session
	 * identifier.
	 * 
	 * @return The EZID session id
	 */
	public void login() {

		String sessionId = null;

		/*
		 * The following set of code sets up Preemptive Authentication for the HTTP
		 * CLIENT and is done so at the warning stated within the Apache
		 * Http-Components Client tutorial here:
		 * http://hc.apache.org/httpcomponents-
		 * client-ga/tutorial/html/authentication.html#d5e1031
		 */

		// Define host parameters
		HttpHost httpHost = new HttpHost(this.host, Integer.valueOf(this.port),
		    this.protocol);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);

		// Define user authentication credentials that will be used with the host
		AuthScope authScope = new AuthScope(httpHost.getHostName(),
		    httpHost.getPort());
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
		    this.ezidUser, this.ezidPassword);
		httpClient.getCredentialsProvider().setCredentials(authScope, credentials);

		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();

		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(httpHost, basicAuth);

		// Add AuthCache to the execution context
		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

		HttpGet httpGet = new HttpGet(this.getEzidUrl(EzidRegistrar.loginUrl));

		HttpResponse response = null;
		Header[] headers = null;
		Integer statusCode = null;

		try {

			response = httpClient.execute(httpHost, httpGet, localcontext);
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
			httpClient.getConnectionManager().shutdown();
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

		}

		this.sessionId = sessionId;

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
	private String getSessionId(String setCookieHeader) {

		String sessionId = null;

		String[] headerParts = setCookieHeader.split(";");
		String[] headerPart = null;

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

	public void logout() throws Exception {

		HttpClient httpClient = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
		String url = this.getEzidUrl("/ezid/logout");
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.sessionId != null) {
			httpGet.setHeader("Cookie", "sessionid=" + this.sessionId);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				String gripe = "Failed to logout of EZID cleanly.";
				throw new Exception(gripe);
			}

			logger.info("logout: " + entityString);

		} finally {
			httpClient.getConnectionManager().shutdown();
		}

	}

	/**
	 * Builds full URL to EZID services.
	 * 
	 * @param url
	 *          The EZID URL path
	 * @return The full URL
	 */
	private String getEzidUrl(String url) {
		return this.protocol + "://" + this.host + url;
	}

	/**
	 * Gets the EZID service session identifier.
	 * 
	 * @return EZID service session identifier
	 */
	String getSessionId() {
		return this.sessionId;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DataCiteMetadata dataCiteMetadata = null;
		EzidRegistrar ezidRegistrar = null;

		try {
			ezidRegistrar = new EzidRegistrar();
			ezidRegistrar.login();
			System.out.println("SessionId: " + ezidRegistrar.getSessionId());
			ezidRegistrar.logout();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

}
