/**
 *
 * $Date: 2012-04-02 11:09:28 -0700 (Mon, 02 Apr 2012) $
 * $Author: dcosta $
 * $Revision: 1890 $
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

package edu.lternet.pasta.doi;

/**
 * @author mservilla
 * @since April 5, 2012
 * 
 */
public class DOIException extends Exception {

	private static final long serialVersionUID = 1L;

  /*
   * Constructors
   */

  /**
   * DOI registration exception.
   */
  public DOIException() {

  }

  /**
   * DOI registration exception.
   * 
   * @param gripe
   *          The cause of the exception in natural language text as a String
   *          object.
   */
  public DOIException(String gripe) {
    super(gripe);
  }

}
