/*
 *
 * $Date: 2014-05-01 11:10:19 -0700 (Mon, 02 Apr 2012) $
 * $Author: dcosta $
 * $Revision: $
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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ArchiveCleanerServlet
 */
@WebServlet(urlPatterns = { "/desktopcleaner" })
public class DesktopCleanerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
			.getLogger(DesktopCleanerServlet.class);
	
	private static final String TTL_IN_MINUTES = "180";

	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		ServletContext servletContext = httpSession.getServletContext();
		String dataPath = servletContext.getRealPath(HarvesterServlet.DESKTOP_DATA_DIR);
		String ttlString = TTL_IN_MINUTES; // number of minutes for desktop data to live

		if (ttlString != null && !ttlString.isEmpty()) {
			Long ttl = Long.valueOf(ttlString) * 60000L; // Convert minutes to ms
			cleanExpiredData(dataPath, ttl);
		}

	}


	/**
	 * Removes any archive file that is older than the specified time-to-live
	 * (ttl).
	 * 
	 * @param ttl
	 *            The time-to-live value in milliseconds.
	 */
	public void cleanExpiredData(String dataPath, Long ttl) {
		int depthLimit = 2;
		Date now = new Date();
		Date expirationDate = new Date(now.getTime() - ttl);
		boolean acceptOlder = true;
		File desktopDataDir = new File(dataPath);
		logger.info(String.format("Cleaning expired desktop data directories under: %s", dataPath));
		FileFilter ageFileFilter = FileFilterUtils.ageFileFilter(expirationDate, acceptOlder);
		DesktopCleaner desktopCleaner = new DesktopCleaner(ageFileFilter, depthLimit);

		try {
			List<File> cleanedFiles = desktopCleaner.clean(desktopDataDir);
			int dirCount = 0;
			for (File file : cleanedFiles) {
				String pathname = file.getPath();
				logger.info(String.format("  Cleaned directory: %s", pathname));
				dirCount++;
			}
			String nounVerb = (dirCount == 1) ? "directory was" : "directories were";
		    logger.info(String.format("%d %s cleaned on this pass.", dirCount, nounVerb));
		}
		catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
}
