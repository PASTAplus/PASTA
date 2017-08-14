/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager.checksum;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * A small wrapper class that uses the Apache Commons Codec DigestUtils class to
 * calculate SHA-1 checksum. This wrapper could easily be extended to support
 * other checksums available through the DigestUtils class.
 * 
 * @author dcosta
 * 
 */
public class DigestUtilsWrapper {

	/**
	 * Gets the MD5 checksum of a file object 
	 * 
	 * @param file  the file object whose checksum is being calculated
	 * @return the MD5 checksum, a 32-character string
	 * @throws Exception
	 */
	public static String getMD5Checksum(File file) throws Exception {
		InputStream fis = new FileInputStream(file);
		String md5Hex = DigestUtils.md5Hex(fis);
		fis.close();
		return md5Hex;
	}


	/**
	 * Gets the SHA-1 checksum of a file object 
	 * 
	 * @param file  the file object whose checksum is being calculated
	 * @return the SHA-1 checksum, a 40-character string
	 * @throws Exception
	 */
	public static String getSHA1Checksum(File file) throws Exception {
		InputStream fis = new FileInputStream(file);
		String shaHex = DigestUtils.shaHex(fis);
		fis.close();
		return shaHex;
	}


	/**
	 * Gets the SHA-1 checksum of a file object based on its filename
	 * 
	 * @param filename  filename of the file object whose checksum is being calculated
	 * @return the SHA-1 checksum, a 40-character string
	 * @throws Exception
	 */
	public static String getSHA1Checksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);
		fis.close();
		String shaHex = DigestUtils.shaHex(fis);
		return shaHex;
	}


	/**
	 * Main method to test the getSHA1Checksum() method. Pass in the full path
	 * to the filename as the sole command-line argument.
	 * 
	 * @param args    Takes one command-line argument, the full path to the
	 *                file object whose checksum is being calculated
	 */
	public static void main(String args[]) {
		try {
			System.out.println(getSHA1Checksum(args[0]));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
