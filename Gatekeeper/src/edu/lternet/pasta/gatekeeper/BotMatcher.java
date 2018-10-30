/*
 *
 * $Author: dcosta $
 *
 * Copyright 2010-2018 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.gatekeeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;


/**
 * @author Duane Costa
 * 
 * Class to detect bots by matching the User-Agent header to a set of regular expression patterns.
 *
 */
public class BotMatcher {
	
    private static Logger logger = Logger.getLogger(BotMatcher.class);
	private static ArrayList<Pattern> regexPatterns = new ArrayList<Pattern>(300);
	private static boolean initialized = false;
	
	public static void initializeRobotPatterns(String path) 
		throws IOException {
		if (!BotMatcher.initialized) {
			File regexFile = new File(path);

			if (regexFile.exists()) {
				try (FileInputStream fis = new FileInputStream(regexFile)) {
					InputStreamReader isr = new InputStreamReader(fis);
					BufferedReader br = new BufferedReader(isr);
					String line = null;
					while ((line = br.readLine()) != null) {
						String regexStr = line.trim();
						if (!regexStr.startsWith("^")) {
							regexStr = ".*" + regexStr;
						}
						if (!regexStr.endsWith("$")) {
							regexStr = regexStr + ".*";
						}
						Pattern p = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);
						
						regexPatterns.add(p);
					}
					String absolutePath = regexFile.getAbsolutePath();
					logger.info(String.format("Loaded %d robot patterns from file %s.", 
							regexPatterns.size(), absolutePath));
					BotMatcher.initialized = true;
				} catch (IOException e) {
					System.err.println("Error opening file: " + path);
					throw(e);
				}
			}
			else {
				String msg = "File not found: " + regexFile.getAbsolutePath();
				throw new IOException(msg);
			}
		}
		else {
			logger.warn("BotMatcher was previously initialized.");
		}
	}
	
	
	public static String findRobot(HttpServletRequest httpServletRequest) {
		String robot = null;

		/*
		 * The Data Portal sets a "robot" cookie value whenever it checks for robots.
		 * If the cookie value is set to "No robot" then we know that the request has 
		 * already been cleared by the Data Portal so we don't need to re-check here,
		 * we can just return null.
		 */
	    Cookie[] cookies = httpServletRequest.getCookies();
	    if (cookies != null) {
	        for (Cookie cookie : cookies) {
	            if (cookie.getName().equals("robot")) {
	                byte[] cookieValueBytes = Base64.decodeBase64((cookie.getValue()).getBytes());
	                String cookieValue = new String(cookieValueBytes);
	                if (!cookieValue.equalsIgnoreCase("No robot")) {
                        logger.info(String.format("Data Portal matched bot pattern to User-Agent value '%s'",
                                                  cookieValue));
                        robot = cookieValue;
	                }
	                return robot;
	            }
	        }
	    }
	    // Otherwise check the User-Agent field
	    else {
	        final String headerName = "User-Agent";
		    Enumeration<?> values = httpServletRequest.getHeaders(headerName);

		    if (values != null) {
			    while (values.hasMoreElements()) {
				    String value = (String) values.nextElement();
				    for (Pattern botPattern : regexPatterns) {
					    if (botPattern.matcher(value).matches()) {
						    logger.info(String.format("Gatekeeper matched bot pattern '%s' to User-Agent value '%s'",
								                  botPattern.pattern(), value));
						    return value;
					    }
				    }
				}
			}
		}

		return robot;
	}
	
}
