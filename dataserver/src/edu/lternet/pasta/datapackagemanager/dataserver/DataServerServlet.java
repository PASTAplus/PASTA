/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011-2015 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager.dataserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
 

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


@WebServlet("/data") 
public class DataServerServlet extends HttpServlet {
 
	/*
	 * Class fields
	 */

	private static Logger logger = Logger.getLogger(DataServerServlet.class);
	private static final long serialVersionUID = 1L;
	
	
	/*
	 * Instance fields
	 */
	
	
	/*
	 * Constructors
	 */
	
	
	/*
	 * Class methods
	 */
	
	
	/*
	 * Instance methods
	 */

	/**
	 * Process a data download HEAD request using information that was generated
	 * by the Data Package Manager service.
	 */
	protected void doHead(HttpServletRequest request, HttpServletResponse response) 
    		throws ServletException {
    	String dataToken = request.getParameter("dataToken");
    	String size = request.getParameter("size");
    	String objectName= request.getParameter("objectName");
    	
    	if (
    		dataToken == null || dataToken.isEmpty() ||
    		size == null || size.isEmpty() ||
    		objectName == null || objectName.isEmpty()
    	   ) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	}
    	else {
    		/*
    		 * Find out which directory the temporary data files are being
    		 * placed in by the Data Package Manager
    		 */
    		PropertiesConfiguration options = ConfigurationListener.getOptions();
    		String tmpDir = options.getString("datapackagemanager.tmpDir");
    	
    		if (tmpDir == null || tmpDir.equals("")) {
    			throw new ServletException("datapackagemanager.tmpDir property value was not specified.");
    		}
    		
    		try { 
    			// reads input file from an absolute path
    			String filePath = String.format("%s/%s", tmpDir, dataToken);
    			File downloadFile = new File(filePath);
    			if (!downloadFile.exists()) {
    				String message = String.format("File not found: %s", filePath);
    				throw new FileNotFoundException(message);
    			}
    			ServletContext context = getServletContext();
         
    			// gets MIME type of the file
    			String mimeType = context.getMimeType(filePath);
    			if (mimeType == null) {        
    				// set to binary type if MIME mapping not found
    				mimeType = "application/octet-stream";
    			}
         
    			// modifies response
    			response.setContentType(mimeType);

        		long length = Long.parseLong(size);
    			if (length <= Integer.MAX_VALUE) {
    				response.setContentLength((int) length);
        		} 
    			else {
        		  response.addHeader("Content-Length", Long.toString(length));
        		}    		
        		
    			// forces download
    			String headerKey = "Content-Disposition";
    			String headerValue = String.format("attachment; filename=\"%s\"", objectName);
    			response.setHeader(headerKey, headerValue);  
    		}
    		catch (FileNotFoundException e) {
    			logger.error(e.getMessage());
    			e.printStackTrace();
    			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		}
    	}
    }
	

	/**
	 * Process a data download request using information that was generated
	 * by the Data Package Manager service.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    		throws ServletException {
    	String dataToken = request.getParameter("dataToken");
    	String size = request.getParameter("size");
    	String objectName= request.getParameter("objectName");
    	
    	if (
    		dataToken == null || dataToken.isEmpty() ||
    		size == null || size.isEmpty() ||
    		objectName == null || objectName.isEmpty()
    	   ) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	}
    	else {
    		/*
    		 * Find out which directory the temporary data files are being
    		 * placed in by the Data Package Manager
    		 */
    		PropertiesConfiguration options = ConfigurationListener.getOptions();
    		String tmpDir = options.getString("datapackagemanager.tmpDir");
    	
    		if (tmpDir == null || tmpDir.equals("")) {
    			throw new ServletException("datapackagemanager.tmpDir property value was not specified.");
    		}
    		
    		logger.info(String.format("Downloading: dataToken: %s; size: %s; objectName: %s", 
    				                  dataToken, size, objectName)); 
    	
    		try { 
    			// reads input file from an absolute path
    			String filePath = String.format("%s/%s", tmpDir, dataToken);
    			File downloadFile = new File(filePath);
    			FileInputStream inStream = new FileInputStream(downloadFile);     
    			ServletContext context = getServletContext();
         
    			// gets MIME type of the file
    			String mimeType = context.getMimeType(filePath);
    			if (mimeType == null) {        
    				// set to binary type if MIME mapping not found
    				mimeType = "application/octet-stream";
    			}
    			logger.debug("MIME type: " + mimeType);
         
    			// modifies response
    			response.setContentType(mimeType);

        		long length = Long.parseLong(size);
    			if (length <= Integer.MAX_VALUE) {
    				response.setContentLength((int) length);
        		} 
    			else {
        		  response.addHeader("Content-Length", Long.toString(length));
        		}    		
        		
    			// forces download
    			String headerKey = "Content-Disposition";
    			String headerValue = String.format("attachment; filename=\"%s\"", objectName);
    			response.setHeader(headerKey, headerValue);
         
    			// obtains response's output stream
    			OutputStream outStream = response.getOutputStream();
         
    			byte[] buffer = new byte[4096];
    			int bytesRead = -1;
         
    			while ((bytesRead = inStream.read(buffer)) != -1) {
    				outStream.write(buffer, 0, bytesRead);
    			}
         
    			inStream.close();
    			outStream.close();
        
    			/*
    			 * Delete the temporary data file after it was downloaded
    			 */
    			try {
    				downloadFile.delete();
    			}
    			catch (Exception e) {
    				logger.warn(String.format("Error deleting temporary data file: %s", e.getMessage()));
    			}
    		}
    		catch (FileNotFoundException e) {
    			logger.error(e.getMessage());
    			e.printStackTrace();
    			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    		}
    		catch (IOException e) {
    			logger.error(e.getMessage());
    			e.printStackTrace();
    			throw new ServletException(e.getMessage());
    		}
    	}
    }
	
}