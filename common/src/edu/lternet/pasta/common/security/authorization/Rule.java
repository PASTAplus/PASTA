/**
 *
 * $Date$
 * $Author$
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

package edu.lternet.pasta.common.security.authorization;

import java.util.*;

/**
 * @author servilla
 *
 * The Rule object defines the basic access rule tuple, which includes: 1)
 * access type, 2) order, 3) principal, and 4) permission.
 */

public class Rule {


	/*
	 * Class fields
	 */
	
	public static final String READ = "read";
	public static final String WRITE = "write";
	public static final String CHANGE = "changePermission";

	/*
	 * Instance fields
	 */

	private String accessType = null;
	private String order      = null;
	private String principal  = null;
	private Permission permission = null;

	/*
	 * Constructors
	 */

	/*
	 * Class Methods
	 */

	/**
	 * Setter method for access type of the rule.
	 *
	 * @param accessType The access type value to be set (either "allow" or
	 *                   "deny").
	 */
	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	/**
	 * Setter method for order type of the rule.
	 *
	 * @param order The order for processing allow and deny rules (either
	 *              "allowFirst" or "denyFirst").
	 */
	public void setOrder(String order) {
		this.order = order;
	}

	/**
	 * Setter for principal identifier of the rule.
	 *
	 * @param principal The principal identifier distinguished name (e.g.,
	 *                  uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org) or a
	 *                  simple string value representing a group identifier
	 *                  (e.g., authenticated).
	 */
	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	/**
	 * Setter for permission type of the rule.
	 *
	 * @param permission The permission value (e.g., read, write, changePermission,
	 *                   or all).
	 */
	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	/**
	 * Getter for access type of the rule.
	 *
	 * @return The access type; either "allow" or "deny".
	 */
	public String getAccessType() {
		return this.accessType;
	}

	/**
	 * Getter for order type of the rule.
	 *
	 * @return The order type; either "allowFirst" or "denyFirst".
	 */
	public String getOrder() {
		return this.order;
	}

	/**
	 * Getter for the principal identifier of a rule.
	 *
	 * @return The principal identifier for a given rule as a string.
	 */
	public String getPrincipal() {
		return this.principal;
	}

	/**
	 * Getter for the permission value of a rule.
	 *
	 * @return The permission value of a rule as a string.
	 */
	public Permission getPermission() {
		return this.permission;
	}

	/**
	 * Permission enumeration that corresponds to permissions recognized by PASTA
	 * access control rules.
	 */
	public enum Permission
	{
	   read(1), write(2), changePermission(3);

	   private Permission(int rank) { this.rank = rank; }
	   public int getRank() { return rank; }

	   private int rank;
	}
}
