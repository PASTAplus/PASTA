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

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import edu.lternet.pasta.common.security.token.*;
import edu.lternet.pasta.common.security.authorization.Rule.Permission;

/**
 * @author servilla
 *
 * Create a PASTA access matrix object based on either an XML access element or
 * an existing access matrix rule set.
 */
public class AccessMatrix {

	/*
	 * Class Fields
	 */

	/*
	 * Instance Fields
	 */

	private String order = "allowFirst";
	private ArrayList<Rule> ruleList;

	// Declare hash tables of "allow" and "deny" rules
	private Hashtable<String, Rule> allowRules = new Hashtable<String, Rule>();
	private Hashtable<String, Rule> denyRules  = new Hashtable<String, Rule>();

	/*
	 * Constructors
	 */

	/**
	 * Generate access matrix from XML access element.
	 *
	 * @param ae The XML access element.
	 * @throws InvalidPermissionException
	 */
	public AccessMatrix(String ae) throws InvalidPermissionException {

			AccessElement accessElement = new AccessElement(ae);
			this.ruleList = accessElement.getRuleList();

	}

	/**
	 * Generate access matrix from existing rule list.
	 *
	 * @param ruleList The existing access matrix rule list.
	 */
	public AccessMatrix(ArrayList<Rule> ruleList) {

		this.ruleList = ruleList;

	}

	/*
	 * Class Methods
	 */

	/**
	 * Determine if the principal user or any of their group affiliations are
	 * authorized for the requested permission.
	 *
	 * @param authToken The authentication token identifying the principal user and
	 *                  their group affiliations.
	 * @param submitter The submitter of the resource being requested.
	 * @param permission The requested permission.
	 * @return The assertion of whether the principal user is authorized to access
	 *         the resource at the requested permission.
	 */
	public boolean isAuthorized(AuthToken authToken, String submitter, Permission permission) {

		boolean isAuthorized = false;

		// Force principal identifier to lower case for hash table comparison.
		String principal = authToken.getUserId().toLowerCase();

		if ((submitter != null && submitter.equalsIgnoreCase(principal)) || principal == "pasta") {
			// The submitter has full access.
			isAuthorized = true;
			return isAuthorized;
		}

		if (this.ruleList != null) {

			// Iterate through rule list adding to either "allow" or "deny" hash tables.
			for (int i = 0; i < this.ruleList.size(); i++) {

				Rule rule = this.ruleList.get(i);

				if (rule.getAccessType().equals("allow")) {
					this.order = rule.getOrder();
					// Set hash key to lower case to simulate case insensitivity.
					this.allowRules.put(rule.getPrincipal().toLowerCase(), rule);
				} else { // Otherwise, "deny".
					this.order = rule.getOrder();
					// Set hash key to lower case to simulate case insensitivity.
					this.denyRules.put(rule.getPrincipal().toLowerCase(), rule);
				}

			}

			// Begin the process to determine if there exists an "allow" rule that
			// allows access to the principal or one of their groups or public.
			if (isAllowed(principal, permission)) {
				isAuthorized = true;
			} else { // Not principal, see if any group affiliation is allowed.

				Set<String> groups = authToken.getGroups();

				// Determine if there a rule that allows a group identifier.
				for (String groupId: groups) {

					// Force all identifiers to lower case for hash table comparison.
					groupId = groupId.toLowerCase();

					if (isAllowed(groupId, permission)) {
						isAuthorized = true;
						break;
					}
				}
			}

			// If order is "allowFirst" and the principal is temporarily authorized,
			// see if there are any "deny" rules that block access.
			if (this.order.equals("allowFirst") && isAuthorized) {

				if (isDenied(principal, permission)) {
					isAuthorized = false;
				} else { // Not principal, see if any group affiliation is denied.

					Set<String> groups = authToken.getGroups();

					// Determine if there a rule that denies a group identifier.
					for (String groupId: groups) {

						// Force all identifiers to lower case for hash table comparison.
						groupId = groupId.toLowerCase();

						if (isDenied(groupId, permission)) {
							isAuthorized = false;
							break;
						}
					}
				}
			}

			// If not authorized, try "public".
			if (!isAuthorized) {
				if (isAllowed("public", permission)) {
					isAuthorized = true;
				}
			}

		}

		return isAuthorized;

	}

	/**
	 * Determine whether the principal identified is allowed access at the requested
	 * permission.
	 *
	 * @param principal The identifier of the principal.
	 * @param permission The requested permission.
	 * @return The boolean value of whether the principal is allowed access to the
	 *         resource at the requested permission.
	 */
	private boolean isAllowed(String principal, Permission permission){

		boolean isAllowed = false;

		if (allowRules.containsKey(principal)) {  // Principal key in hash table.

			Rule rule = allowRules.get(principal);

			// Determine if requested permission is less than or equal to the rule permission.
			if (permission.getRank() <= rule.getPermission().getRank()) {
				isAllowed = true;
			}

		}

		return isAllowed;

	}

	/**
	 * Determine whether the principal identified is denied access at the requested
	 * permission.
	 *
	 * @param principal The identifier of the principal.
	 * @param permission The requested permission.
	 * @return The boolean value of whether the principal is denied access to the
	 *         resource at the requested permission.
	 */
	private boolean isDenied(String principal, Permission permission){

		boolean isDenied = false;

		if (denyRules.containsKey(principal)) {  // Principal key in hash table.

			Rule rule = denyRules.get(principal);

			// Determine if requested permission is greater or equal to the rule permission.
			if (permission.getRank() >= rule.getPermission().getRank()) {
				isDenied = true;
			}

		}

		return isDenied;

	}

	/**
	 * Return the rule list of the access matrix object.
	 *
	 * @return The rule list as an array list.
	 */
	public ArrayList<Rule> getRuleList() {

		return this.ruleList;

	}

}
