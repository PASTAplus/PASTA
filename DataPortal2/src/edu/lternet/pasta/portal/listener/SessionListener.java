/*
 * $Date: 2014-06-10 12:23:25 -0700 (Fri, 10 June 2014) $
 * $Author: dcosta $
 *
 * Copyright 2011-2014 the University of New Mexico.
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

package edu.lternet.pasta.portal.listener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

public class SessionListener implements HttpSessionListener {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
			.getLogger(edu.lternet.pasta.portal.listener.SessionListener.class);

	/*
	 * Instance variables
	 */

	private int sessionCount = 0;


	/*
	 * Instance methods
	 */

	public void sessionCreated(HttpSessionEvent event) {
		synchronized (this) {
			sessionCount++;
		}
		HttpSession httpSession = event.getSession();
		ServletContext servletContext = httpSession.getServletContext();
		String sessionId = httpSession.getId();
		String uid = (String) httpSession.getAttribute("uid");
		String contextPath = servletContext.getContextPath();
		logger.warn(String.format("Session Created: %s; Context Path: %s; User Id: %s; Total Sessions: %d", sessionId, contextPath, uid, sessionCount));
	}


	public void sessionDestroyed(HttpSessionEvent event) {
		synchronized (this) {
			sessionCount--;
		}
		HttpSession httpSession = event.getSession();
		ServletContext servletContext = httpSession.getServletContext();
		String sessionId = httpSession.getId();
		String uid = (String) httpSession.getAttribute("uid");
		String contextPath = servletContext.getContextPath();
		logger.warn(String.format("Session Destroyed: %s; Context Path: %s; User Id: %s; Total Sessions: %d", sessionId, contextPath, uid, sessionCount));
	}
	
}
