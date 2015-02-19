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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


/**
 * The HarvestReportCleaner class cleans harvest report files that have passed their
 * expiration date from the harvest report directory tree.
 * 
 * @author dcosta
 *
 */
public class HarvestReportCleaner extends DirectoryWalker<File> {

	private static final Logger logger = Logger
			.getLogger(HarvestReportCleaner.class);
	
	private File topDirectory = null;

	
	/**
	 * Constructs a HarvestReportCleaner 
	 * @param filter        The file filter controls which directories under
	 *                      the top directory are walked. This should be a
	 *                      file filter that filters on age so that directories
	 *                      older than the expiration date can be deleted.
	 * @param depthLimit    The depth limit places a limit on the depth of the
	 *                      directory walk.
	 */
	public HarvestReportCleaner(FileFilter filter, int depthLimit) {
		super(filter, depthLimit);
	}


	/**
	 * Performs the actual cleaning.
	 * 
	 * @param startDirectory      The top-level directory.
	 * @return                    A list of directories and files that were cleaned, 
	 *                            just in case we want to log the results.
	 * @throws IOException
	 */
	public List<File> clean(File startDirectory) throws IOException {
		this.topDirectory = startDirectory;
		List<File> results = new ArrayList<File>();
		walk(startDirectory, results);
		return results;
	}


	/**
	 * Callback method that determines how to process a directory.
	 */
	protected boolean handleDirectory(File directory, int depth,
			Collection<File> results) {
		boolean result = false;

		/*
		 * Don't delete the top-level data directory regardless of how
		 * old it is. Only delete the filtered directories below it.
		 */
		if (!directory.equals(this.topDirectory)) {

			try {
				FileUtils.deleteDirectory(directory);
				results.add(directory);
			}
			catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		else {
			result = true;  // true means that this dir should be walked
		}

		return result;
	}

}
