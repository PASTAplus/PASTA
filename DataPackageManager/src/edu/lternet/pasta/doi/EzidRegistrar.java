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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
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

	/*
	 * Instance variables
	 */

	private DataCiteMetadata dataCiteMetadata = null;

	private String host = null;
	private String port = null;
	private String protocol = null;

	private String ezidUser = null;
	private String ezidPassword = null;
	private String keystore = null;
	private String keystorePassword = null;

	private String sessionId = null;

	/*
	 * Constructors
	 */

	public EzidRegistrar() throws ConfigurationException {

		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		this.loadOptions(options);

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
	private void loadOptions(Options options) throws ConfigurationException {

		if (options != null) {

			this.host = options.getOption("datapackagemanager.ezidHost");
			this.port = options.getOption("datapackagemanager.ezidPort");
			this.protocol = options.getOption("datapackagemanager.ezidProtocol");
			this.ezidUser = options.getOption("datapackagemanager.ezidUser");
			this.ezidPassword = options.getOption("datapackagemanager.ezidPassword");

		} else {
			throw new ConfigurationException("Configuration options failed to load");
		}

	}

	
	/**
	 * Sets the DataCite metadata object.
	 * 
	 * @param dataCiteMetadata
	 */
	public void setDataCiteMetadata(DataCiteMetadata dataCiteMetadata) {
		this.dataCiteMetadata = dataCiteMetadata;
	}

	/**
	 * Gets the DataCite metadata object.
	 * 
	 * @return DataCite metadata object
	 */
	public DataCiteMetadata getDataCiteMetadata() {
		return this.dataCiteMetadata;
	}

	/**
	 * Login to the EZID web service API system and return a valid session
	 * identifier.
	 * 
	 * @return The EZID session id
	 * @throws EzidException
	 */
	public void login() throws EzidException {

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
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
		AuthScope authScope = new AuthScope(httpHost.getHostName(),
		    httpHost.getPort());
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
		    this.ezidUser, this.ezidPassword);
		httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(httpHost, basicAuth);
		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

		HttpGet httpGet = new HttpGet(this.getEzidUrl("/ezid/login"));

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

		} else {
			String gripe = "login: failed EZID login.";
			throw new EzidException(gripe);
		}

		this.sessionId = sessionId;

	}

	/**
	 * Logout of the EZID service session.
	 * 
	 * @throws EzidException
	 */
	public void logout() throws EzidException {

		HttpClient httpClient = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
		String url = this.getEzidUrl("/ezid/logout");
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
			httpClient.getConnectionManager().shutdown();
		}

		if (statusCode != HttpStatus.SC_OK) {
			String gripe = "Failed to logout of EZID cleanly.";
			throw new EzidException(gripe);
		}

		logger.info("logout: " + entityString);

	}

	/**
	 * Registers the resource DOI based on the DataCite metadata object.
	 * 
	 * @throws EzidException
	 */
	public void registerDataCiteMetadata() throws EzidException {

		if (this.dataCiteMetadata == null) {
			String gripe = "registerDataCiteMetadata: DataCite metadata object is null.";
			throw new EzidException(gripe);
		}

		HttpHost httpHost = new HttpHost(this.host, Integer.valueOf(this.port),
		    this.protocol);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
		AuthScope authScope = new AuthScope(httpHost.getHostName(),
		    httpHost.getPort());
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
		    this.ezidUser, this.ezidPassword);
		httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(httpHost, basicAuth);
		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
		String doi = this.dataCiteMetadata.getDigitalObjectIdentifier().getDoi();
		String url = this.getEzidUrl("/ezid/id/" + doi);
		StringBuffer metadata = new StringBuffer("");
		metadata
		    .append("datacite: " + this.dataCiteMetadata.toDataCiteXml() + "\n");
		metadata
		    .append("_target: " + this.dataCiteMetadata.getLocationUrl() + "\n");
		HttpPut httpPut = new HttpPut(url);
		httpPut.setHeader("Content-type", "text/plain");
		HttpEntity stringEntity = null;
		Integer statusCode = null;
		String entityString = null;

		try {
			stringEntity = new StringEntity(metadata.toString());
			httpPut.setEntity(stringEntity);
			HttpResponse httpResponse = httpClient.execute(httpHost, httpPut,
			    localcontext);
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
			httpClient.getConnectionManager().shutdown();
		}

		logger.info("registerDataCiteMetadata: " + this.dataCiteMetadata.getLocationUrl() + "\n" + entityString);

		// Test for DOI collision or DOI registration failure
		if (statusCode == HttpStatus.SC_BAD_REQUEST
		    && entityString.contains("identifier already exists")) {
			String gripe = "identifier already exists";
			throw new EzidException(gripe);
		} else if (statusCode != HttpStatus.SC_CREATED) {
			logger.error(this.dataCiteMetadata.toDataCiteXml());
			String gripe = "DOI registration failed for: " + doi;
			throw new EzidException(gripe);
		}

	}

	/**
	 * Make the DOI obsolete by setting the EZID metadata field "_status" to
	 * "unavailable".
	 * 
	 * @param doi The DOI to obsolete
	 * @throws EzidException
	 */
	public void obsoleteDoi(String doi) throws EzidException {

		HttpHost httpHost = new HttpHost(this.host, Integer.valueOf(this.port),
		    this.protocol);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
		AuthScope authScope = new AuthScope(httpHost.getHostName(),
		    httpHost.getPort());
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
		    this.ezidUser, this.ezidPassword);
		httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(httpHost, basicAuth);
		BasicHttpContext localcontext = new BasicHttpContext();
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
		
		String url = this.getEzidUrl("/ezid/id/" + doi);

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
			HttpResponse httpResponse = httpClient.execute(httpHost, httpPost,
			    localcontext);
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
			httpClient.getConnectionManager().shutdown();
		}

		logger.info("obsoleteDoi: " + entityString);

	 if (statusCode != HttpStatus.SC_OK) {
			String gripe = "DOI obsoletion failed for: " + doi;
			throw new EzidException(gripe);
		}

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
	private String getSessionId() {
		return this.sessionId;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		EzidRegistrar ezidRegistrar = null;

		try {
			ezidRegistrar = new EzidRegistrar();
			ezidRegistrar.obsoleteDoi("doi:10.6073/pasta/dcbd7c1aab57af6a65672aa917bb3faf");
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

}
