/*
 *
 * $Date: 2014-06-23 22:10:39 -0600 (Mon, 23 June 2014) $
 * $Author: costa $
 * $Revision: 2178 $
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

package edu.lternet.pasta.portal.codegeneration;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaClient;
import edu.lternet.pasta.client.PastaConfigurationException;

/**
 * @author dcosta
 * @since June 19, 2014
 * 
 *        The CodeGenerationClient provides an interface to the PASTAprog web
 *        service hosted by VCR at:
 *        http://www.vcrlter.virginia.edu/webservice/PASTAprog
 * 
 */
public class CodeGenerationClient extends PastaClient {

	/*
	 * The statistical file types supported by the VCR web service
	 */
	public enum StatisticalFileType {
		m, r, tidyr, sas, sps, spss;
	}


	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
			.getLogger(edu.lternet.pasta.portal.codegeneration.CodeGenerationClient.class);

	/*
	 * Instance variables
	 */

	private final String BASE_URL = "http://www.vcrlter.virginia.edu/webservice/PASTAprog";
	private String downloadFilename = null;
	private String statisticalPackageName = null;
	private String url;


	/*
	 * Constructors
	 */

	/**
	 * Construct a CodeGenerationClient, specifying the statistical file type
	 * and the package ID for which code is to be generated.
	 * 
	 * @param statisticalFileType   the statisical file type, an enumerated type
	 * @param packageId  the package ID string  
	 */
	public CodeGenerationClient(StatisticalFileType statisticalFileType, String packageId)
			throws PastaAuthenticationException, PastaConfigurationException {
	    super("public");
		if (statisticalFileType == null) {
			throw new IllegalArgumentException("null statisticalFileType");
		}
		
		String urlFilename = null;
		
		switch (statisticalFileType) {
		case m:
			urlFilename = String.format("%s.m", packageId);
			
			/* For Matlab programs substitute _ for the periods and hyphens in the
			 * download filename to avoid problems with Matlab's file naming conventions.  
			 * Thus: "knb-lter-vcr.26.20.m" becomes "knb_lter_vcr_26_20.m".
			 */
			packageId = packageId.replace('.', '_');
			packageId = packageId.replace('-', '_');
			this.statisticalPackageName = "Matlab";
			this.downloadFilename = String.format("%s.m", packageId);
			break;
		case r:
			this.statisticalPackageName = "R";
			this.downloadFilename = String.format("%s.r", packageId);
			urlFilename = this.downloadFilename;
			break;
        case tidyr:
            this.statisticalPackageName = "tidyr";
            this.downloadFilename = String.format("%s.tidyr", packageId);
            urlFilename = this.downloadFilename;
            break;
		case sas:
			this.statisticalPackageName = "SAS";
			this.downloadFilename = String.format("%s.sas", packageId);
			urlFilename = this.downloadFilename;
			break;
		case sps:
			this.statisticalPackageName = "SPSS";
			this.downloadFilename = String.format("%s.sps", packageId);
			urlFilename = this.downloadFilename;
			break;
		case spss:
			this.statisticalPackageName = "SPSS";
			this.downloadFilename = String.format("%s.spss", packageId);
			urlFilename = this.downloadFilename;
			break;
		}
		
		String baseUrlSuffix = pastaHost.contains("-s") ? "-S" : "";
		this.url = String.format("%s%s/%s", BASE_URL, baseUrlSuffix, urlFilename);
	}


	/**
	 * Accessor method
	 * 
	 * @return  the filename value
	 */
	public String getDownloadFilename() {
		return downloadFilename;
	}


	/**
	 * Returns the program code composed by the VCR program code
	 * generation service.
	 * 
	 * @return the generated code returned by the web service
	 * @throws Exception
	 */
	public String getProgramCode() 
			throws Exception {
		String programCode = null;
		Integer statusCode = null;
		HttpEntity responseEntity = null;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		HttpGet httpGet = new HttpGet(this.url);

		try {
			response = httpClient.execute(httpGet);
			statusCode = (Integer) response.getStatusLine().getStatusCode();
			responseEntity = response.getEntity();

			if (responseEntity != null) {
				programCode = EntityUtils.toString(responseEntity);
			}
		}
		finally {
			closeHttpClient(httpClient);
		}

		if (statusCode != HttpStatus.SC_OK) {
			// Something went wrong; return message from the response entity
			String gripe = String.format(
				"The code generation service at URL '%s' responded with response code '%s' and message '%s'\n", 
				this.url, statusCode.toString(), programCode);
			throw new Exception(gripe);
		}
		else if (programCode == null || programCode.trim().equals("")) {
			// Although a 200 OK was returned, something went wrong and no
			// no program code is in the entity body; return an error message from the response entity
			String gripe = String.format(
				"The code generation service at URL '%s' responded with response code '%s' but " +
			    "did not generate any program code. A dependent web service may be down.\n", 
				this.url, statusCode.toString(), programCode);
			throw new Exception(gripe);
		}

		return programCode;
	}
	
	
	/**
	 * Accessor method
	 * 
	 * @return  the statisticalPackageName value
	 */
	public String getStatisticalPackageName() {
		return statisticalPackageName;
	}

}
