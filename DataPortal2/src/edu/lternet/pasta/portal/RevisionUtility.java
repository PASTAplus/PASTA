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

package edu.lternet.pasta.portal;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author servilla
 * @since Dec 17, 2012
 * 
 *        RevisionUtility provides a set of utility methods for a list of
 *        revision values.
 * 
 */
public class RevisionUtility {

	/*
	 * Class variables
	 */

	/*
	 * Instance variables
	 */
	
	private int[] revisions;
	private Integer size = null;

	/*
	 * Constructors
	 */
	
	/**
	 * Creates a new RevisionList object with a numerically ascending sorted
	 * list of revision.
	 * 
	 * @param list List of numerical revision values
	 */
	public RevisionUtility(String list) {
		
		String[] revisionList = list.split("\\s");
		this.size = revisionList.length;
		
		this.revisions = new int[this.size];
		
		for (int i = 0; i < this.size; i++) {
			this.revisions[i] = Integer.valueOf(revisionList[i]);
		}
		
		Arrays.sort(this.revisions);
		
	}

	/*
	 * Class methods
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/*
	 * Instance methods
	 */
	
	/**
	 * Returns the revision list size.
	 * 
	 * @return Revision list size
	 */
	public Integer getSize() {
		return this.size;
	}
	
	/**
	 * Returns the newest revision.
	 * 
	 * @return Newest revision
	 */
	public Integer getNewest() {
		return Integer.valueOf(this.revisions[this.size - 1]);
	}
	
	/**
	 * Returns the oldest revision.
	 * 
	 * @return Oldest revision
	 */
	public Integer getOldest() {
		return Integer.valueOf(this.revisions[0]);
	}
	
	/**
	 * Returns the predecessor of the current revision or null if one does not
	 * exist.
	 * 
	 * @param current Current revision
	 * @return Predecessor of the current revision
	 */
	public Integer getPredecessor(Integer current) {
		
		Integer predecessor = null;
		
		for (int i = 0; i < this.size; i++) {
			if (Integer.valueOf(revisions[i]).equals(current)) {
				if (i > 0) predecessor = Integer.valueOf(revisions[i - 1]);
				break;
			}
		}
				
		return predecessor;
		
	}
	
	/**
	 * Returns the successor of the current revision or null if one does not
	 * exist.
	 * 
	 * @param current Current revision
	 * @return Successor of the current revision
	 */
	public Integer getSuccessor(Integer current) {
		
		Integer successor = null;
		
		for (int i = 0; i < this.size; i++) {
			if (Integer.valueOf(revisions[i]).equals(current)) {
				if (i < this.size - 1) successor = Integer.valueOf(revisions[i + 1]);
				break;
			}
		}
				
		return successor;
		
	}

}
