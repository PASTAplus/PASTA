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
import java.util.ArrayList;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.lternet.pasta.portal.codegeneration.CodeGenerationClient;
import edu.lternet.pasta.portal.codegeneration.CodeGenerationClient.StatisticalFileType;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.portal.DataPortalServlet;
import edu.lternet.pasta.portal.MapBrowseServlet;

public class CodeGenerationServlet extends DataPortalServlet {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
			.getLogger(edu.lternet.pasta.portal.codegeneration.CodeGenerationServlet.class);
	private static final long serialVersionUID = 1L;
	private static final String forward = "./codeGeneration.jsp";
	
    private static final String pythonInstructions =
        "Download the Python program and run it as you would any other Python program. Alternatively, you can " +
        "copy and paste the program code into the Python IDLE editor and run it from there.<br/><br/>" +
        "For datasets that require authenticated access to data tables, you may need to download the " +
        "data separately and alter the<br/><code class='nis'>infile <-</code> lines to reflect where the data " +
        "is stored on your computer.<br/>&nbsp;";
     
    private static final String rInstructions =
        "Download the R program and open it in R to run. Alternatively, you can " +
        "copy and paste the program code into the R console.<br/><br/>For datasets that " +
        "require authenticated access to data tables, you may need to download the " +
        "data separately and alter the<br/><code class='nis'>infile <-</code> lines to reflect where the data " +
        "is stored on your computer.<br/>&nbsp;";
        
    private static final String tidyrInstructions =
        "Download the R program and open it in R to run. Alternatively, you can " +
        "copy and paste the program code into the R console. " +
        "Unless it is already installed, the program will install the R Tidyverse package, " +
        "which in turn installs a number of dependencies.<br/><br/>" +
        "For datasets that require authenticated access to data tables, you may need to download the " +
        "data separately and alter the<br/><code class='nis'>infile <-</code> lines to reflect where the data " +
        "is stored on your computer.<br/>&nbsp;";
        
	private static final String sasInstructions =
		"Download the .sas program and open it in SAS to run. Alternatively the " +
		"code may be cut and pasted into the SAS program editor.<br/><br/>For datasets " +
		"that require authenticated access to data tables, you may need to download " +
		"the data separately and alter the <code class='nis'>filename datafile</code> lines to reflect " +
		"where the data is stored on your computer.<br/>&nbsp;";
			
	private static final String spssInstructions =
		"Download the .spss program and the data files. Open the .spss file as a " +
		"syntax file and edit it so that the <code class='nis'>FILE=\"PUT-PATH-TO-DATA-FILE-HERE\"</code> " +
		"statements contain the path to the appropriate data file on your computer. " +
		"Alternatively the code may be cut and pasted into an SPSS Syntax Editor " +
		"and altered there.<br/>&nbsp;";

	private static final String matlabInstructions =
		"Download the .m file and save it to a directory in the MATLAB path, then " +
		"start MATLAB and run it using the syntax:<br/>" + 
		"&nbsp;&nbsp;&nbsp;&nbsp;<code class='nis'>[data,msg] = knb_lter_XX_YY(pn,cachedata,username,password,entities)</code><br/><br/>" +
		"Where:<br/>" +
		"&nbsp;&nbsp;&nbsp;&nbsp;<code class='nis'>knb_lter_XX_YY</code> is the function m-file name derived from the packageID of the dataset (e.g. knb_lter_knz_95_5)<br/>" +
		"&nbsp;&nbsp;&nbsp;&nbsp;<code class='nis'>pn</code> = file system path for saving temporary files (default = pwd)<br/>" + 
		"&nbsp;&nbsp;&nbsp;&nbsp;<code class='nis'>cachedata</code> = option to use cached entity files if they exist in pn (0 = no/default, 1 = yes)<br/>" +
		"&nbsp;&nbsp;&nbsp;&nbsp;<code class='nis'>username</code> = username for HTTPS authentication (default = for anonymous)<br/>" +
		"&nbsp;&nbsp;&nbsp;&nbsp;<code class='nis'>password</code> = password for HTTPS authentication (default = for anonymous)<br/>" +
		"&nbsp;&nbsp;&nbsp;&nbsp;<code class='nis'>entities</code> = cell array of entities to retrieve (default = for all)<br/>" +
		"&nbsp;&nbsp;&nbsp;&nbsp;<code class='nis'>data</code> = structure containing fields for parsed metadata and data arrays<br/>" +
		"&nbsp;&nbsp;&nbsp;&nbsp;<code class='nis'>msg</code> = text of any error messages ( for no errors)<br/><br/>" +
		"Note that username and password are only required for data tables where " +
		"authenticated access is required, and the cURL executable must be present " +
		"in the MATLAB path (see <a href=\"http://curl.haxx.se/\">http://curl.haxx.se/</a>).<br/>&nbsp;";

	/*
	 * Constructors
	 */

	public CodeGenerationServlet() {
		super();
	}
	
	
	/*
	 * Class methods
	 */
	
	/**
	 * Get the list of program links that are allowable for interacting with this servlet.
	 * One link is generated for each of the supported statistical file types.
	 * 
	 * @param packageId    the package ID to be used as a request parameter in each link
	 * @return  an array list of strings, each is an anchor tag for use in the JSP page
	 */
	public static ArrayList<String> getProgramLinks(String packageId) {
		ArrayList<String> programLinks = new ArrayList<String>();
		
		String mLink = String.format("<a class='searchsubcat' href='./codeGeneration?packageId=%s&statisticalFileType=m'>Matlab</a>", packageId);
		String pyLink = String.format("<a class='searchsubcat' href='./codeGeneration?packageId=%s&statisticalFileType=py'>Python</a>", packageId);
		String rLink = String.format("<a class='searchsubcat' href='./codeGeneration?packageId=%s&statisticalFileType=r'>R</a>", packageId);
        String tidyrLink = String.format("<a class='searchsubcat' href='./codeGeneration?packageId=%s&statisticalFileType=tidyr'>tidyr</a>", packageId);
		String sasLink = String.format("<a class='searchsubcat' href='./codeGeneration?packageId=%s&statisticalFileType=sas'>SAS</a>", packageId);
		String spssLink = String.format("<a class='searchsubcat' href='./codeGeneration?packageId=%s&statisticalFileType=spss'>SPSS</a>", packageId);
		
		programLinks.add(mLink);
		programLinks.add(pyLink);
		programLinks.add(rLink);
        programLinks.add(tidyrLink);
		programLinks.add(sasLink);
		programLinks.add(spssLink);
		
		return programLinks;
	}
	
	
	public static TreeMap<String, String> getProgramDict(String packageId) {
		TreeMap<String, String> programDict = new TreeMap<String, String>();
		
		String mLink = String.format("./codeGeneration?packageId=%s&statisticalFileType=m", packageId);
		String pyLink = String.format("./codeGeneration?packageId=%s&statisticalFileType=py", packageId);
		String rLink = String.format("./codeGeneration?packageId=%s&statisticalFileType=r", packageId);
        String tidyrLink = String.format("./codeGeneration?packageId=%s&statisticalFileType=tidyr", packageId);
		String sasLink = String.format("./codeGeneration?packageId=%s&statisticalFileType=sas", packageId);
		String spssLink = String.format("./codeGeneration?packageId=%s&statisticalFileType=spss", packageId);
		
		programDict.put("MatLab", mLink);
		programDict.put("Python", pyLink);
		programDict.put("R", rLink);
		programDict.put("tidyr", tidyrLink);
		programDict.put("SAS", sasLink);
		programDict.put("SPSS", spssLink);
		
		return programDict;
	}
	
	
	/*
	 * Instance methods
	 */


	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy();
	}


	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);

	}


	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String filename = null;
			String programCode = null;
			String packageId = request.getParameter("packageId");
			String statisticalFileTypeParam = request.getParameter("statisticalFileType");
			StatisticalFileType statisticalFileType = null;
			String statisticalPackageName = null;
			String mapBrowseURL = MapBrowseServlet.getRelativeURL(packageId);
			String instructions = null;
			
			switch (statisticalFileTypeParam) {
			case "m":
				statisticalFileType = StatisticalFileType.m;
				instructions = CodeGenerationServlet.matlabInstructions;
				break;
			case "py":
				statisticalFileType = StatisticalFileType.py;
				instructions = CodeGenerationServlet.pythonInstructions;
				break;
			case "r":
				statisticalFileType = StatisticalFileType.r;
				instructions = CodeGenerationServlet.rInstructions;
				break;
            case "tidyr":
                statisticalFileType = StatisticalFileType.tidyr;
                instructions = CodeGenerationServlet.tidyrInstructions;
                break;
			case "sas":
				statisticalFileType = StatisticalFileType.sas;
				instructions = CodeGenerationServlet.sasInstructions;
				break;
			case "sps":
				statisticalFileType = StatisticalFileType.sps;
				instructions = CodeGenerationServlet.spssInstructions;
				break;
			case "spss":
				statisticalFileType = StatisticalFileType.spss;
				instructions = CodeGenerationServlet.spssInstructions;
				break;
			}

			if (packageId != null) {
				CodeGenerationClient codeGenerationClient = new CodeGenerationClient(statisticalFileType, packageId);
				filename = codeGenerationClient.getDownloadFilename();
				programCode = codeGenerationClient.getProgramCode();
				statisticalPackageName = codeGenerationClient.getStatisticalPackageName();
				request.setAttribute("filename", filename);
				request.setAttribute("statisticalFileType", statisticalFileTypeParam);
				request.setAttribute("statisticalPackageName", statisticalPackageName);
				request.setAttribute("packageId", packageId);
				request.setAttribute("mapBrowseURL", mapBrowseURL);
				request.setAttribute("instructions", instructions);
				request.setAttribute("programCode", programCode);
				RequestDispatcher requestDispatcher = request
						.getRequestDispatcher(forward);
				requestDispatcher.forward(request, response);
			}
			else {
				throw new UserErrorException("Package identifier is null.");
			}

		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}
	}


	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
	}

}
