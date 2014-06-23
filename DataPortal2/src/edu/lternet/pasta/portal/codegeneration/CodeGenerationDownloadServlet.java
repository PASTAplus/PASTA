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
import java.io.OutputStream;
import java.io.StringReader;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

@WebServlet(urlPatterns = { "/codegenerationdownload" })
public class CodeGenerationDownloadServlet extends HttpServlet {

	/*
	 * Class variables
	 */
	
	private static final long serialVersionUID = 7583L;
	private static final Logger logger = Logger
			.getLogger(edu.lternet.pasta.portal.codegeneration.CodeGenerationDownloadServlet.class);

	/*
	 * Instance methods
	 */

	/**
	 * Downloads the file containing the program code text.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		if (session == null) {
			RequestDispatcher dispatcher = request
					.getRequestDispatcher("./login.jsp");
			dispatcher.forward(request, response);
			/*
			 * Must return after dispatcher.forward(). Otherwise,
			 * the code below will be executed
			 */
			return;
		}

		String filename = request.getParameter("filename");
		String programCode = (String) session.getAttribute("programCode");
		
		if (filename != null && !filename.equals("") && 
			programCode != null && !programCode.equals("")
			) {
			response.setContentType("text/plain");
			response.addHeader("Content-Disposition",
					String.format("attachment; filename=%s", filename));
			StringReader stringReader = new StringReader(programCode);
			OutputStream os = response.getOutputStream();

			try {
				int ch;
				while ((ch = stringReader.read()) != -1) {
					os.write(ch);
				}
			}
			catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			finally {
				if (stringReader != null) {
					stringReader.close();
				}
				if (os != null) {
					os.flush();
					os.close();
				}
			}
		}
	}

}