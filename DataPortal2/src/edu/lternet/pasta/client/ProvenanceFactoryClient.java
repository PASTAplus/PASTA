/*
 *
 * $Date:$
 * $Author:$
 * $Revision:$
 *
 * Copyright 2011,2012 the University of New Mexico.
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

package edu.lternet.pasta.client;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.portal.ConfigurationListener;

/**
 * @author servilla
 * @since August 7, 2012
 * 
 *        The ProvenanceFactoryClient provides an interface to the PASTA
 *        Provenance Factory service.
 * 
 */
public class ProvenanceFactoryClient extends PastaClient {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(edu.lternet.pasta.client.ProvenanceFactoryClient.class);
	
	/*
	 * Instance variables
	 */
	
  private final String BASE_URL;
	
	
	/*
	 * Constructors
	 */

	/**
	 * @param uid
	 * @throws PastaAuthenticationException
	 * @throws PastaConfigurationException
	 */
	public ProvenanceFactoryClient(String uid)
	    throws PastaAuthenticationException, PastaConfigurationException {
		super(uid);
    String pastaUrl = PastaClient.composePastaUrl(this.pastaProtocol, this.pastaHost, this.pastaPort);
    this.BASE_URL = pastaUrl + "/package/provenance/eml";
	}
	
	
	/**
	 * Returns the EML provenance metadata fragment as an XML string enclosed
	 * by a <methodStep> element for the provided package identifier.
	 * 
	 * @param packageId The packageId of the parent data package whose provenance
	 *                  metadata is being generated and returned.
	 * @return an XML string containing the generated provenance metadata inside
	 *         a <methodStep> element
	 * @throws PastaEventException 
	 */
	public String getProvenanceByPid(String packageId) throws Exception {
		String provenanceXml = null;
		Integer statusCode = null;
		HttpEntity responseEntity = null;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String urlFragment = packageId.replace('.', '/');
		String provenanceURL = String.format("%s/%s", BASE_URL, urlFragment);
		HttpGet httpGet = new HttpGet(provenanceURL);

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			response = httpClient.execute(httpGet);
			statusCode = (Integer) response.getStatusLine().getStatusCode();
			responseEntity = response.getEntity();

			if (responseEntity != null) {
				provenanceXml = EntityUtils.toString(responseEntity);
			}
		}
		catch (ClientProtocolException e) {
			logger.error(e);
			e.printStackTrace();
		}
		catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
		finally {
			closeHttpClient(httpClient);
		}

		if (statusCode != HttpStatus.SC_OK) {
			// Something went wrong; return message from the response entity
			String gripe = String.format(
			    "The Metadata Factory responded with response code %d and message '%s'.\n", 
				statusCode, provenanceXml);
			throw new PastaEventException(gripe);
		}

		return provenanceXml;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConfigurationListener.configure();

		try {
			ProvenanceFactoryClient pfc = new ProvenanceFactoryClient(
					"ucarroll");
			String provenanceXml = pfc.getProvenanceByPid("lter-landsat.7.1");
			logger.info("Provenance XML: \n" + provenanceXml);

		}
		catch (PastaAuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (PastaConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (PastaEventException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
