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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.lternet.pasta.common.security.authorization.Rule.Permission;

/**
 * @author servilla
 *
 * Consume an access element XML string and create the corresponding access
 * matrix rule set.
 */
public class AccessElement {

	/*
	 * Class fields
	 */

	/*
	 * Instance fields
	 */
	private Element root   = null;
	private Hashtable<String, Rule> allowRules = new Hashtable<String, Rule>();
	private Hashtable<String, Rule> denyRules  = new Hashtable<String, Rule>();
	private ArrayList<Rule> ruleList = new ArrayList<Rule>();


	/*
	 * Constructors
	 */

	/**
	 * Creates an AccessElement object based on the XML access element string. <br/>
	 *
	 * @param accessElement The XML string representation of an eml or pasta
	 *                      access element.
	 * @throws InvalidPermissionException
	 */
	public AccessElement(String accessElement) throws InvalidPermissionException {

		if (accessElement == null) {
			this.ruleList = null;
		} else {
			InputStream is = this.StringToInputStream(accessElement);
			this.root = this.parseXMLFile(is).getDocumentElement();  //Returns "root" element of the DOM
			this.buildRuleList();
		}

	}

	/*
	 * Class Methods
	 */

	/**
	 * Converts the eml-access element String to an Input Stream, which
	 * is required by the document buidler.
	 *
	 * @param accessElement
	 * @return InputStream is - The eml-access element as an Input Stream
	 */
	private InputStream StringToInputStream(String accessElement) {

		InputStream is = null;

	    try {
	        is = new ByteArrayInputStream(accessElement.getBytes("UTF-8"));
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    }

	    return is;
	 }

	/**
	 * Creates a document object model and parses the eml-access element from
	 * the input stream.
	 *
	 * @param is The eml-access element input stream to be parsed.
	 * @return Document doc - The document object model of the parsed eml-access input stream.
	 */
	private Document parseXMLFile(InputStream is) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;

		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.print("AccessElement.parseXML: " + e);
			e.printStackTrace();
		}

		try {
			doc = builder.parse(is);
		} catch (SAXException e) {
			System.err.print("AccessElement.parseXML: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.print("AccessElement.parseXML: " + e);
			e.printStackTrace();
		}

		return doc;

	}

	/**
	 * Returns the value of the "authSystem" attribute - this attribute is required.
	 *
	 * @return Returns the <em>authSystem</em> attribute as a string.
	 */
	public String getAuthSystem() {

		if (root.hasAttribute("authSystem")) {
			return root.getAttribute("authSystem");
		} else {
			return null;  //This is an error state; "authSystem" is required.
		}
	}

	/**
	 * Returns the value of the "order" attribute, if it exists; otherwise, returns the
	 * default "allowFirst".
	 *
	 * @return Returns the <em>order</em> attribute as a string; defaults to "allowFirst"
	 *         if not set.
	 */
	public String getAccessOrder() {

		String order = "allowFirst";

		if (root.hasAttribute("order")) {
			return root.getAttribute("order");
		} else {
			return order;
		}
	}

	/**
	 * Returns the generated rule list (essentially, the access matrix) from the
	 * access element XML.
	 *
	 * @return The rule list as an array list of rules.
	 */
	public ArrayList<Rule> getRuleList() {
		return this.ruleList;
	}

	/**
	 * Parse the access element and process the "allow" or "deny" blocks.
	 * @throws InvalidPermissionException
	 */
	private void buildRuleList() throws InvalidPermissionException {

		NodeList accessTypeNodes = root.getChildNodes();

		for (int i = 0; i < accessTypeNodes.getLength(); i++) {

			Node accessTypeNode = accessTypeNodes.item(i);

			if (accessTypeNode instanceof Element) {
				Element accessTypeElement = (Element) accessTypeNode;
				String accessType = accessTypeElement.getTagName();  //Either "allow" or "deny"

				if (accessType.equals("allow")) {
					buildAllowRules(accessTypeElement);
				} else { // "deny"
					buildDenyRules(accessTypeElement);
				}
			}
		}

		// Aggregate all rules into single rule list object.
		if (!allowRules.isEmpty()) {

			Enumeration<String> allowKeys = allowRules.keys();

			while(allowKeys.hasMoreElements()) {
				ruleList.add(allowRules.get(allowKeys.nextElement()));
			}

		}

		if (!denyRules.isEmpty()) {

			Enumeration<String> denyKeys = denyRules.keys();

			while(denyKeys.hasMoreElements()) {
				ruleList.add(denyRules.get(denyKeys.nextElement()));
			}
		}
	}

	/**
	 * For all principals in the current "allow" block, determine the "high-watermark"
	 * and add or update the rule for each principal in the "allow" hash table.
	 *
	 * @param allow The current "allow" block element.
	 * @throws InvalidPermissionException
	 */
	private void buildAllowRules(Element allow) throws InvalidPermissionException {

		Rule rule = null;

		// Iterate through all permissions of the current "allow" block and
		// determine the "high-watermark".
		Permission highWaterMark = this.getHighWaterMark(allow);

		NodeList allowChildren = allow.getChildNodes();

		// Iterate through all child elements of the "allow" block looking
		// specifically for principals and build rules.
		for (int i = 0; i < allowChildren.getLength(); i++) {

			Node ruleNode = allowChildren.item(i);

			if (ruleNode instanceof Element) {

				Element ruleElement = (Element) ruleNode;

				if (ruleElement.getTagName().equals("principal")) {

					String principal = ruleElement.getTextContent().trim();

					if (allowRules.containsKey(principal)) {  // Principal key in hash table.

						rule = allowRules.get(principal);

						// If principal in hash table, determine if table
						// "high-watermark" is greater than current.
						if (rule.getPermission().getRank() < highWaterMark.getRank()) {
							allowRules.remove(principal);
							rule.setPermission(highWaterMark);
							allowRules.put(principal, rule);
						}

					} else {  // Principal not in hash table, add new rule.

						rule = new Rule();
						rule.setAccessType("allow");
						rule.setOrder(this.getAccessOrder());
						rule.setPermission(highWaterMark);
						rule.setPrincipal(principal);

						allowRules.put(principal, rule);

					}
				}
			}
		}
	}

	/**
	 * For all principals in the current "deny" block, determine the "low-watermark"
	 * and add or update the rule for each principal in the "deny" hash table.
	 *
	 * @param deny The current "deny" block eleemnt.
	 * @throws InvalidPermissionException
	 */
	private void buildDenyRules(Element deny) throws InvalidPermissionException {

		Rule rule = null;

		// Iterate through all permissions of the current "deny" block and
		// determine the "low-watermark".
		Permission lowWaterMark = this.getLowWaterMark(deny);

		NodeList denyChildren = deny.getChildNodes();

		// Iterate through all child elements of the "allow" block looking
		// specifically for principals and build rules.
		for (int i = 0; i < denyChildren.getLength(); i++) {

			Node ruleNode = denyChildren.item(i);

			if (ruleNode instanceof Element) {

				Element ruleElement = (Element) ruleNode;

				if (ruleElement.getTagName().equals("principal")) {

					String principal = ruleElement.getTextContent().trim();

					if (denyRules.containsKey(principal)) {  // Principal key in hash table.

						rule = denyRules.get(principal);

						// If principal in hash table, determine if table
						// "low-watermark" is greater than current.
						if (rule.getPermission().getRank() > lowWaterMark.getRank()) {
								denyRules.remove(principal);
								rule.setPermission(lowWaterMark);
								denyRules.put(principal, rule);
						}

					} else {  // Principal not in hash table, add new rule.

						rule = new Rule();
						rule.setAccessType("deny");
						rule.setOrder(this.getAccessOrder());
						rule.setPermission(lowWaterMark);
						rule.setPrincipal(principal);

						denyRules.put(principal, rule);

					}
				}
			}
		}
	}

	private Permission getHighWaterMark(Element allow) throws InvalidPermissionException {

		// Start with lowest possible permission.
		Permission highWaterMark = Enum.valueOf(Permission.class, "read");

		NodeList allowChildren = allow.getChildNodes();

		for (int i = 0; i < allowChildren.getLength(); i++) {

			Node allowChild = allowChildren.item(i);

			if (allowChild instanceof Element) {

				Element allowChildElement = (Element) allowChild;
				String allowChildElementTag = allowChildElement.getTagName();

				if (allowChildElementTag.equals("permission")) {

					String permissionElementText = allowChildElement.getTextContent().trim();

					// Adjust for the symbolic "all" permission.
					if (permissionElementText.equals("all")) permissionElementText = "changePermission";

					try {

						Permission permission = Enum.valueOf(Permission.class, permissionElementText);

						if (permission.getRank() > highWaterMark.getRank()) {
							highWaterMark = permission;
						}

					} catch (IllegalArgumentException e) {
						throw new InvalidPermissionException("Ivalid permission: " + permissionElementText);
					}
				}
			}
		}

		return highWaterMark;

	}


	private Permission getLowWaterMark(Element allow) throws InvalidPermissionException {

		// Start with highest possible permission.
		Permission lowWaterMark = Enum.valueOf(Permission.class, "changePermission");;

		NodeList allowChildren = allow.getChildNodes();

		for (int i = 0; i < allowChildren.getLength(); i++) {

			Node allowChild = allowChildren.item(i);

			if (allowChild instanceof Element) {

				Element allowChildElement = (Element) allowChild;
				String allowChildElementTag = allowChildElement.getTagName();

				if (allowChildElementTag.equals("permission")) {

					String permissionElementText = allowChildElement.getTextContent().trim();

					// Adjust for the symbolic "all" permission.
					if (permissionElementText.equals("all")) permissionElementText = "read";

					try {

						Permission permission = Enum.valueOf(Permission.class, permissionElementText);

						if (permission.getRank() < lowWaterMark.getRank()) {
							lowWaterMark = permission;
						}

					} catch (IllegalArgumentException e) {
						throw new InvalidPermissionException("Ivalid permission: " + permissionElementText);
					}
				}
			}
		}

		return lowWaterMark;

	}
}
