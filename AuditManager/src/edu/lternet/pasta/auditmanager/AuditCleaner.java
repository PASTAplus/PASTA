/*
 *
 * Copyright 2011, 2012, 2013 the University of New Mexico.
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

package edu.lternet.pasta.auditmanager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * @author servilla
 * @since Apr 15, 2013
 * 
 *        Cleans the data package archive temporary directory of expired
 *        archive files.
 * 
 */
public class AuditCleaner {

	/*
	 * Class variables
	 */


	/*
	 * Instance variables
	 */

	private final Logger logger = Logger.getLogger(AuditCleaner.class);
	private String tmpDir = null;

	/*
	 * Constructors
	 */

	public AuditCleaner() throws Exception {
		
		tmpDir = ConfigurationListener.getTmpDir();

		if (tmpDir == null || tmpDir.isEmpty()) {
			String gripe = "Temporary directory property is not set in the Audit Manager properties file.";
			throw new Exception(gripe);
		}

	}
	
	
	/*
	 * Class methods
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		AuditCleaner ac = null;

		try {
			ac = new AuditCleaner();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		ac.doClean(60000L);

	}

	
	/*
	 * Instance methods
	 */

	/**
	 * Removes any audit records XML file that is older than the specified 
	 * time-to-live (ttl).
	 * 
	 * @param ttl
	 *            The time-to-live value in milliseconds.
	 */
	public void doClean(Long ttl) {
		File tmpDir = new File(this.tmpDir);
		String[] ext = { "xml" };
		Long time = new Date().getTime();
		Long lastModified = null;

		Collection<File> files = FileUtils.listFiles(tmpDir, ext, false);

		for (File file : files) {
			if (file != null && file.exists()) {
				lastModified = file.lastModified();
				// Remove file if older than the ttl
				if (lastModified + ttl <= time) {
					try {
						FileUtils.forceDelete(file);
					}
					catch (IOException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
	}
	
}
