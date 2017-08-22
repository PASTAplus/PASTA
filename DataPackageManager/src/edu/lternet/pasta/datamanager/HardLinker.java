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

package edu.lternet.pasta.datamanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;


/**
 * A small class to test out the hard link capability on a given system.
 * @author dcosta
 *
 */
public class HardLinker {
	
	/**
	 * Deletes the existing file at the path specified by the first argument
     * and replaces it with a hard link back to the path specified by the
	 * second argument.
	 * 
	 * @param args
	 *              arg[0]  the file path that will be deleted and replaced by a new hard link
	 * 				arg[1]  the existing file that will be hard-linked to
	 */
	public static void main(String[] args) {
		String path1 = args[0];
		String path2 = args[1];
		File file1 = new File(path1);
		
		// Delete the first file
		boolean wasDeleted = file1.delete();
		
		if (wasDeleted || !file1.exists()) {
			HardLinker hardLinker = new HardLinker();
			// Link to the second file
		    try {
				FileSystem fileSystem = FileSystems.getDefault();
				Path firstPath = fileSystem.getPath(path1);
			    Path secondPath = fileSystem.getPath(path2);
			    hardLinker.hardLink(firstPath, secondPath);					
		    }
		    catch (IOException e) {
		    	String msg = String.format("Error creating hard link from %s to %s",
		    			                   path1, path2);
		    	System.err.println(msg);
		    }							    								
		}
	}

	
	/**
	 * Hard links two files. 
	 * 
	 * @param newPath         The path of the new directory entry to be created by hard linking to an existing file.
	 * @param existingPath    The path of the already existing file.
	 * @return                The path to the hard link.
	 * @throws IOException
	 */
	public Path hardLink(Path newPath, Path existingPath) 
			throws IOException {
		Path hardLinkPath = null;
		String newPathStr = newPath.toString();
		String existingPathStr = existingPath.toString();
		String createLinkMsg = String.format("Creating hard link from %s to %s", newPathStr, existingPathStr);
		System.err.println(createLinkMsg);
		hardLinkPath = Files.createLink(newPath, existingPath);
		
		if (hardLinkPath != null) {
			System.err.println("Hard link succeeded");
		} 
		else {
			System.err.println("Hard link failed");
		}

		return hardLinkPath;
	}

}
