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

package edu.lternet.pasta.common;

import com.sun.jersey.api.core.ScanningResourceConfig;

/**
 * @author servilla
 * @since Nov 12, 2012
 * 
 *        A PASTA Resource utility class.
 * 
 */
public class PastaResource {

	/*
	 * Class variables
	 */

	/*
	 * Instance variables
	 */
	
	/*
	 * Constructors
	 */
	
	/*
	 * Class methods
	 */

	/**
	 * Returns the canonical EML package identifier from the object PASTA resource
	 * identifier.  Note: this method assumes that the resourceId contains one of the
	 * following substrings:
	 *    /package/data/eml/
	 *    /package/report/eml/
	 *    /package/eml/
	 *    /package/metadata/eml/
	 * If not, it throws an IllegalArgumentException.
	 * 
	 * @return The canonical EML package identifier
	 */
	static public String getPackageId(String resourceId)
		throws IllegalArgumentException {
		
		String packageId = null;

		String[] canonicalSubstrings = { "/package/data/eml/", "/package/report/eml/", "/package/eml/", "/package/metadata/eml/" };
		int index = -1;

		for (int i = 0; i < canonicalSubstrings.length; i++) {
			index = resourceId.indexOf(canonicalSubstrings[i]);
			if (index > -1) {
				String tail = resourceId.substring(index + canonicalSubstrings[i].length());
				if (tail != null) {
					String[] pathParts = tail.split("/");
					String scope = pathParts[0];
					String identifier = pathParts[1];
					String revision = pathParts[2];
					packageId = scope + "." + identifier + "." + revision;
				}
				break;
			}
		}

		if (index == -1 || packageId == null) {
			throw new IllegalArgumentException("Bad resource id: " + resourceId);
		}

		return packageId;
		
	}


	/**
	 * Returns the canonical EML metadata resourceId corresponding to the given resourceId.
	 * For example, given resourceId
	 *     http://localhost:8088/package/data/eml/knb-lter-nin/1/1/67e99349d1666e6f4955e9dda42c3cc2
	 * returns
	 *     http://localhost:8088/package/metadata/eml/knb-lter-nin/1/1
	 * Note: this method assumes that the given resourceId contains one of the
	 * following substrings:
	 *    /package/data/eml/
	 *    /package/report/eml/
	 *    /package/eml/
	 *    /package/metadata/eml/
	 * If not, it throws an IllegalArgumentException.
	 *
	 * @return The EML metadata resourceId
	 */
	static public String getMetadataResourceId(String resourceId)
			throws IllegalArgumentException {

		String metadataResourceId = null;

		String[] canonicalSubstrings = { "/package/data/eml/", "/package/report/eml/", "/package/eml/", "/package/metadata/eml/" };
		int index = -1;

		for (int i = 0; i < canonicalSubstrings.length; i++) {
			index = resourceId.indexOf(canonicalSubstrings[i]);
			if (index > -1) {
				String head = resourceId.substring(0, index);
				String tail = resourceId.substring(index + canonicalSubstrings[i].length());
				if (head != null && tail != null) {
					String[] pathParts = tail.split("/");
					String scope = pathParts[0];
					String identifier = pathParts[1];
					String revision = pathParts[2];
					String packageId = scope + "/" + identifier + "/" + revision;
					metadataResourceId = head + "/package/metadata/eml/" + packageId;
				}
				break;
			}
		}

		if (index == -1 || metadataResourceId == null) {
			throw new IllegalArgumentException();
		}

		return metadataResourceId;

	}

	/*
	 * Instance methods
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
