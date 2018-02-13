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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class ArchiveCleanerServlet
 */
@WebServlet(urlPatterns = { "/desktopcleaner" }, loadOnStartup=10)
public class DesktopCleanerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
			.getLogger(DesktopCleanerServlet.class);
	
	private static final String TTL_IN_MINUTES = "180";
	private static String desktopDataPath = null;
	private static long desktopDataTTL;  // desktop data time to live in milliseconds

	
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
        executeDesktopDataManager();
	}


    /*
     * Spawns off a thread to execute the desktop data manager
     * to purge old desktop data directories at start-up.
     */
    private void executeDesktopDataManager() {
        DesktopDataManager desktopDataManager = new DesktopDataManager(desktopDataPath, desktopDataTTL);
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(desktopDataManager);
        executorService.shutdown();
    }
  
  
    /**
     * Initialization of the servlet. <br>
     * 
     * @throws ServletException
     *             if an error occurs
     */
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
        //Configuration options = ConfigurationListener.getOptions();
        ServletContext servletContext = conf.getServletContext();     
        desktopDataPath = servletContext.getRealPath(HarvesterServlet.DESKTOP_DATA_DIR);
        desktopDataTTL = Long.valueOf(TTL_IN_MINUTES) * 60000L; // Convert minutes to ms

        /*
         * Purge old desktop data directories
         */     
        executeDesktopDataManager();
    }

}
