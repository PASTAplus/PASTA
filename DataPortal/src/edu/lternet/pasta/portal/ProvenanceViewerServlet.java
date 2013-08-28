/*
 *
 * $Date: 2012-05-09 22:10:39 -0600 (Wed, 09 May 2012) $
 * $Author: mservilla $
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

package edu.lternet.pasta.portal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.owasp.esapi.codecs.XMLEntityCodec;

import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.client.ProvenanceFactoryClient;

public class ProvenanceViewerServlet extends DataPortalServlet {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(edu.lternet.pasta.portal.ProvenanceViewerServlet.class);
	private static final long serialVersionUID = 1L;
	private static final String forward = "./provenanceViewer.jsp";

	/*
	 * Constructors
	 */

	public ProvenanceViewerServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *          the request send by the client to the server
	 * @param response
	 *          the response send by the server to the client
	 * @throws ServletException
	 *           if an error occurred
	 * @throws IOException
	 *           if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		doPost(request, response);

	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request
	 *          the request send by the client to the server
	 * @param response
	 *          the response send by the server to the client
	 * @throws ServletException
	 *           if an error occurred
	 * @throws IOException
	 *           if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		try {
		HttpSession httpSession = request.getSession();
		String uid = (String) httpSession.getAttribute("uid");

		if (uid == null || uid.isEmpty())
			uid = "public";

		String message = null;
		String type = null;

		String packageId = request.getParameter("packageid");

		if (packageId != null) {

			try {

				ProvenanceFactoryClient pfc = new ProvenanceFactoryClient(uid);
				message = pfc.getProvenanceByPid(packageId);
				message = "<pre>" + this.xmlEncode(message) + "</pre>";

			} catch (PastaAuthenticationException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				message = e.getMessage();
				type = "warning";
			} 
		} else {
			message = "Package Identifier is null.";
			type = "warning";
		}
		
		request.setAttribute("message", message);
		request.setAttribute("type", type);
		request.setAttribute("packageid", packageId);

		RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
		requestDispatcher.forward(request, response);
		}
	    catch (IOException e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	        throw(e);
	    }
	    catch (Exception e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	        throw new ServletException(e.getMessage());
	    }
		

	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *           if an error occurs
	 */
	public void init() throws ServletException {

		PropertiesConfiguration options = ConfigurationListener.getOptions();

	}
	
  private String xmlEncode(String rawXml) {

    String encodedXml = null;

    if (rawXml == null) {
      encodedXml = "";
    } else {

      // Encodings for XML
      XMLEntityCodec xmlEntityCodec = new XMLEntityCodec();
      char[] immune = new char[0];

      StringBuffer xml = new StringBuffer();

      for (int a = 0; a < rawXml.length(); a++) {
        xml.append(xmlEntityCodec.encodeCharacter(immune, rawXml.charAt(a)));
      }
      
      encodedXml = xml.toString();

    }
      
      return encodedXml;
      
    }


}
