/**
 *    '$RCSfile: Attribute.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2014-05-30 13:50:46 -0700 (Fri, 30 May 2014) $'
 *   '$Revision: 2365 $'
 *
 *  For Details: http://kepler.ecoinformatics.org
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */

package edu.lternet.pasta.dml.parser;

import java.util.List;

/**
 * @author Leinfelder
 * 
 * This object represents a Party - a person and/or organization
 */
public class Party {

	/*
	 * Instance fields
	 */
	private String surName;
	private List<String> givenNames;
	private String organization;


	/**
	 * Constructs a Party with fields initialized to given values
	 */
	public Party(String surName, List<String> givenNames, String organization) {
		this.surName = surName;
		this.givenNames = givenNames;
		this.organization = organization;
	}


	public String getSurName() {
		return surName;
	}


	public void setSurName(String surName) {
		this.surName = surName;
	}


	public List<String> getGivenNames() {
		return givenNames;
	}


	public void setGivenNames(List<String> givenNames) {
		this.givenNames = givenNames;
	}


	public String getOrganization() {
		return organization;
	}


	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	
}