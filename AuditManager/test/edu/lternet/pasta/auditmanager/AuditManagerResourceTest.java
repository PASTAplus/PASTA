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

package edu.lternet.pasta.auditmanager;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author dcosta
 * 
 * Tests the operations of the AuditManager class. A single test method,
 * 'testServiceMethods(), tests all three of the Audit Manager's service
 * methods. We are testing the overall functionality of the API at this
 * level, not the lower-level logic of audit management. For example, this
 * class does not attempt to test whether query results are correct for a
 * variety of different queries, only that the basic query operation functions
 * as it should.
 * 
 */
public class AuditManagerResourceTest {

	/*
	 * Class fields
	 */

	private static AuditManagerResource auditManagerResource;
	private static final String testUser = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";

	/*
	 * Instance fields
	 */


	/*
	 * Constructors
	 */

	/*
	 * Class methods
	 */

	/**
	 * Initialize objects before any tests are run.
	 */
	@BeforeClass
	public static void setUpClass() {
		auditManagerResource = new AuditManagerResource();
		ConfigurationListener.setPastaServiceAcr();
		ConfigurationListener
				.loadPropertiesFile(ConfigurationListener.AUDIT_MANAGER_PROPERTIES);
	}


	/*
	 * Instance methods
	 */

	/**
	 * Initialize objects before each test is run.
	 */
	@Before
	public void setUpTest() {
	}


	private String readTestAuditEntry() throws IOException {
		String auditEntry = null;
		String auditEntryPath = "test/data/auditRecord.xml";
		File auditEntryFile = new File(auditEntryPath);
		assertTrue(auditEntryFile != null);
		auditEntry = FileUtils.readFileToString(auditEntryFile);
		return auditEntry;
	}
	
	
	
	/**
	 * Tests all service methods in the web service API.
	 */
	@Test
	public void testServiceMethods() {
		Integer auditId = testCreate();
		Integer badAuditId = new Integer(-999);
		testGetAuditRecord(auditId);
		testGetAuditRecordBad(badAuditId);
		testGetAuditRecords();
	}


	/**
	 * Test the create() service method.
	 */
	private Integer testCreate() {
		Integer auditId = null;
		try {
			String auditEntry = readTestAuditEntry();
			HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);

			// Test CREATE for OK status
			Response response = auditManagerResource.create(httpHeaders,
					auditEntry);
			int statusCode = response.getStatus();
			assertEquals(201, statusCode);

			MultivaluedMap<String, Object> map = response.getMetadata();
	        List<Object> location = map.get(HttpHeaders.LOCATION);
	        URI uri = (URI) location.get(0);
			String uriPath = uri.getPath(); // e.g. "/audit/22196"

			try {
				auditId = Integer.parseInt(uriPath.substring(uriPath.lastIndexOf('/') + 1));
			}
			catch (NumberFormatException e) {
				fail("Failed to return valid audit entry ID value: "
						+ uriPath);
			}		
		}
		catch (IOException e) {
			fail(e.getMessage());
		}
		
		return auditId;
	}


	/**
	 * Test the status and message body of the getAuditRecord() service method.
	 */
	private void testGetAuditRecord(Integer auditId) {
		if (auditId == null) {
			fail("Null auditId value");
		}
		else {
			DummyCookieHttpHeaders httpHeaders = new DummyCookieHttpHeaders(
					testUser);

			// Test Evaluate for OK status
			Response response = auditManagerResource.getAuditRecord(
					httpHeaders, auditId);
			int statusCode = response.getStatus();
			assertEquals(200, statusCode);

			// Check the message body
			String entityString = (String) response.getEntity();
			String auditReport = entityString.trim();
			assertTrue(auditReport.length() > 1);
			assertTrue(auditReport.startsWith("<auditReport>"));
			assertTrue(auditReport.contains(String.format("<oid>%d</oid>", auditId)));
			assertTrue(auditReport.endsWith("</auditReport>"));
		}
	}


	/**
	 * Test the status and message body of the getAuditRecord() service method
	 * when a non-existent auditId is passed to it.
	 */
	private void testGetAuditRecordBad(Integer auditId) {
		if (auditId == null) {
			fail("Null auditId value");
		}
		else {
			DummyCookieHttpHeaders httpHeaders = new DummyCookieHttpHeaders(
					testUser);

			// Test Evaluate for OK status
			Response response = auditManagerResource.getAuditRecord(
					httpHeaders, auditId);
			int statusCode = response.getStatus();
			assertEquals(404, statusCode);
		}
	}


	/**
	 * Test the status and message body of the getAuditRecords() service method.
	 */
	private void testGetAuditRecords() {
        Map<String, String> query = Collections.singletonMap("user", testUser);
        UriInfo uriInfo = new DummyUriInfo(query);
		HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);

		// Test READ for OK status
		Response response = auditManagerResource.getAuditRecords(httpHeaders, uriInfo);
		int statusCode = response.getStatus();
		assertEquals(200, statusCode);

		// Check the message body
		String entityString = (String) response.getEntity();
		String auditReport = entityString.trim();
		assertTrue(auditReport.length() > 1);
		assertTrue(auditReport.startsWith("<auditReport>"));
		assertTrue(auditReport.contains(String.format("<user>%s</user>", testUser)));
		assertTrue(auditReport.endsWith("</auditReport>"));
	}


	/**
	 * Clean up and release any objects after each test is complete.
	 */
	@After
	public void tearDownTest() {
	}


	/**
	 * Release any objects after all tests are complete.
	 */
	@AfterClass
	public static void tearDownClass() {
		auditManagerResource = null;
	}

}
