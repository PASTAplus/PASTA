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

package edu.lternet.pasta.doi;

/**
 * @author servilla
 * @since Oct 23, 2012
 * 
 * Manages dates in the form YYYY ensuring the value is valid.
 *
 */
public class Year {
	
	/*
	 * Class variables
	 */
	
	/*
	 * Instance variables
	 */
	
	private Integer year = null;
	
	/*
	 * Constructors
	 */
	
	public Year (Integer year) throws Exception {
		this.year = year;
	}
	
	/*
	 * Class Methods
	 */
	
	/*
	 * Instance Methods
	 */
	
	/**
	 * Returns the year value as a String object.
	 * 
	 * @return the year value as a String
	 */
	public String toString() {
		return this.year.toString();
	}

}
