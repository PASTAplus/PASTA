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

package edu.lternet.pasta.datapackagemanager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Apr 15, 2013
 * 
 *        Cleans the data package archive temporary directory of expired
 *        archive files.
 * 
 */
public class ArchiveCleaner {

	/*
	 * Class variables
	 */

	private static final String dirPath = "WebRoot/WEB-INF/conf";

	/*
	 * Instance variables
	 */

	private final Logger logger = Logger.getLogger(ArchiveCleaner.class);
	private String archiveDir = null;

	/*
	 * Constructors
	 */

	public ArchiveCleaner() throws Exception {
		
		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		archiveDir = options.getOption("datapackagemanager.archiveDir");

		if (archiveDir == null || archiveDir.isEmpty()) {
			String gripe = "Archive directory property not set!";
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

		ArchiveCleaner ac = null;

		try {
	    ac = new ArchiveCleaner();
    } catch (Exception e) {
	    System.out.println(e.getMessage());
	    e.printStackTrace();
    }
		
		ac.doClean(60000L);
		
	}

	
	/*
	 * Instance methods
	 */

	/**
	 * Removes any archive file that is older than the specified time-to-live (ttl).
	 * 
	 * @param ttl The time-to-live value in milliseconds.
	 * @return    the number of archive files that were removed
	 */
	public int doClean(Long ttl) {
		File archiveDir = new File(this.archiveDir);
		String[] ext = { "zip" };
		Long time = new Date().getTime();
		Long lastModified = null;
		int deleteCount = 0;

		Collection<File> files = FileUtils.listFiles(archiveDir, ext, false);

		for (File file : files) {
            logger.info("ArchiveCleaner: " + file.getAbsolutePath());
			if (file != null && file.exists()) {
				lastModified = file.lastModified();
				// Remove file if older than the ttl
				if (lastModified + ttl <= time) {
					try {
						FileUtils.forceDelete(file);
						deleteCount++;
					}
					catch (IOException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
		
		return deleteCount;
	}
	
}
