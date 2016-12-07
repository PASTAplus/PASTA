/**
 *    '$RCSfile: DocumentDataPackageParserTest.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2008-08-12 23:30:27 $'
 *   '$Revision: 1.2 $'
 *
 *  For Details: http://ecoinformatics.org
 *
 * Copyright (c) 2008 The Regents of the University of California.
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
package edu.lternet.pasta.dml.parser.document;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.lternet.pasta.dml.DataManager;
import edu.lternet.pasta.dml.database.DatabaseConnectionPoolInterface;
import edu.lternet.pasta.dml.database.Join;
import edu.lternet.pasta.dml.database.Query;
import edu.lternet.pasta.dml.database.SelectionItem;
import edu.lternet.pasta.dml.database.TableItem;
import edu.lternet.pasta.dml.database.WhereClause;
import edu.lternet.pasta.dml.database.pooling.DatabaseConnectionPoolFactory;
import edu.lternet.pasta.dml.download.ConfigurableEcogridEndPoint;
import edu.lternet.pasta.dml.download.EcogridEndPointInterface;
import edu.lternet.pasta.dml.download.document.DocumentDataPackageHandler;
import edu.lternet.pasta.dml.parser.Attribute;
import edu.lternet.pasta.dml.parser.DataPackage;
import edu.lternet.pasta.dml.parser.Entity;

import edu.ucsb.nceas.utilities.OrderedMap;


public class DocumentDataPackageParserTest extends TestCase {

	private static final String TEST_SERVER = "http://knb.ecoinformatics.org/knb/metacat";

	//private static final String TEST_DOCID = "leinfelder.799.1";
	private static final String TEST_DOCID = "tao.1.1";

	/*
	 * Class fields
	 */
	public static Log log = LogFactory.getLog(DocumentDataPackageParserTest.class);

	/*
	 * Instance fields
	 */
	private DataManager dataManager;
	private DatabaseConnectionPoolInterface connectionPool = null;
	private ConfigurableEcogridEndPoint endPointInfo = null;
	
	/*
	 * Constructors
	 */

	/**
	 * Because DataManagerTest is a subclass of TestCase, it must provide a
	 * constructor with a String parameter.
	 * 
	 * @param name
	 *            the name of a test method to run
	 */
	public DocumentDataPackageParserTest(String name) {
		super(name);
	}

	/*
	 * Class methods
	 */

	/**
	 * Create a suite of tests to be run together.
	 */
	public static Test suite() {
		TestSuite testSuite = new TestSuite();

		testSuite.addTest(new DocumentDataPackageParserTest("initialize"));
		testSuite.addTest(new DocumentDataPackageParserTest("testLoadAndMerge"));


		return testSuite;
	}

	/*
	 * Instance methods
	 */

	/**
	 * Run an initial test that always passes to check that the test harness is
	 * working.
	 */
	public void initialize() {
		assertTrue(1 == 1);
	}

	/**
	 * Establish a testing framework by initializing appropriate objects.
	 */
	public void setUp() throws Exception {
		super.setUp();
		endPointInfo = new ConfigurableEcogridEndPoint();
		endPointInfo.setSessionId("badSession");
		connectionPool = 
			DatabaseConnectionPoolFactory.getDatabaseConnectionPoolInterface();
			//new PostgresDatabaseConnectionPool();
		String dbAdapterName = connectionPool.getDBAdapterName();
		dataManager = DataManager.getInstance(connectionPool, dbAdapterName);
	}

	/**
	 * Release any objects after tests are complete.
	 */
	public void tearDown() throws Exception {
		connectionPool = null;
		dataManager = null;
		endPointInfo = null;
		super.tearDown();
	}
	
	public void testLoadAndMerge() {
		
		DocumentDataPackageHandler ddph = new DocumentDataPackageHandler(connectionPool);
		
		//the "columns" to extract from the metadata document 
		Map attributeMap = new OrderedMap();
		attributeMap.put("id", "//@packageId");
		attributeMap.put("title", "//title");
		attributeMap.put("author", "//surName");
		attributeMap.put("badColumn", "//does/not/exist");
				
		ddph.setDocId(TEST_DOCID);
		ddph.setEcogridEndPointInterface(endPointInfo);
		ddph.setAttributeMap(attributeMap);
		
		DataPackage dataPackage = null;
		DataPackage metadataPackage = null;
		try {
			dataPackage = loadDataPackage();
			metadataPackage = ddph.loadDataToDB();
			mergeDataPackages(dataPackage, metadataPackage);
		}
		catch (Exception e) {
			log.error("Problem while testing document data package merge " + e.getMessage());
			e.printStackTrace();

		}
	}
	
	private void mergeDataPackages(DataPackage dataPackage, DataPackage metadataPackage) throws Exception {
		
		//build query
		Query query = new Query();
		ResultSet resultSet = null;
		
		/******* get the data entity ******/
		Entity dataEntity = dataPackage.getEntityList()[0];
		Attribute[] dataAttributes = dataEntity.getAttributeList().getAttributes();
		//all attributes			
		for (int j=0; j < dataAttributes.length; j++) {
			Attribute attribute = dataAttributes[j];
			/* SELECT clause */
			SelectionItem selectionItem = 
				new SelectionItem(dataEntity, attribute);
			query.addSelectionItem(selectionItem);
		}
		/* FROM clause */
		TableItem dataTableItem = new TableItem(dataEntity);
		query.addTableItem(dataTableItem);
		
		/******* get the metadata entity ******/
		Entity metadataEntity = metadataPackage.getEntityList()[0];
		Attribute[] metadataAttributes = metadataEntity.getAttributeList().getAttributes();
		//all attributes			
		for (int j=0; j < metadataAttributes.length; j++) {
			Attribute attribute = metadataAttributes[j];
			/* SELECT clause */
			SelectionItem selectionItem = 
				new SelectionItem(metadataEntity, attribute);
			query.addSelectionItem(selectionItem);
		}
		/* FROM clause */
		TableItem metadataTableItem = new TableItem(metadataEntity);
		query.addTableItem(metadataTableItem);
		
		/* WHERE clause */
		Join idJoin = 
			new Join(
					dataEntity, dataEntity.getAttributes()[0],
					metadataEntity, metadataEntity.getAttributes()[0]);
		WhereClause where = new WhereClause(idJoin);
		//TODO join condition! right now it's on the first column - no bueno!
		//query.setWhereClause(where);
		
		log.debug("Query SQL = " + query.toSQLString());
				
		try {
			//try to get the results
			log.debug("about to select data");
		
			//make a list out of the two input DPs
			DataPackage[] dataPackages = new DataPackage[2];
			dataPackages[0] = dataPackage;
			dataPackages[1] = metadataPackage;
			
			//get the data
			resultSet = dataManager.selectData(query, dataPackages);
						
			if (resultSet != null) {

				int j = 1;

				while (resultSet.next()) {
					log.debug("row " + j + ":");
					for (int col = 1; col <= resultSet.getMetaData().getColumnCount(); col++) {
						Object column = resultSet.getObject(col);
						
						log.debug("\t" + resultSet.getMetaData().getColumnName(col) + "=" + column);
					}	
					j++;
				}
			} 
			else {
				throw new Exception("resultSet is null");
			}
		} 
		catch (Exception e) {
			log.error("Exception: "	+ e.getMessage());
			e.printStackTrace();
			throw (e);
		} 
		finally {
			if (resultSet != null) {
				resultSet.close();
			}
			
		}
	}
	
	private DataPackage loadDataPackage() throws Exception {
		
		DataPackage dataPackage = null;
		String documentURL = TEST_SERVER + 
		                     "?action=read&qformat=xml&docid=" + 
		                     TEST_DOCID;
		InputStream inputStream = null;
		boolean success;
		URL url;
		
		//create the DataPackage with data table Entity
		try {
		  url = new URL(documentURL);
		  inputStream = url.openStream();
		  dataPackage = dataManager.parseMetadata(inputStream);
		  //load the data table to the database
		  success = dataManager.loadDataToDB(dataPackage, endPointInfo);
		}
		catch (MalformedURLException e) {
		  e.printStackTrace();
		  throw(e);
		}
		catch (IOException e) {
		  e.printStackTrace();
		  throw(e);
		}
		catch (Exception e) {
		  e.printStackTrace();
		  throw(e);
		}
		return dataPackage;
	}
	
	public static void main(String args[]) {
		DocumentDataPackageParserTest upt = new DocumentDataPackageParserTest("EntityUtilTest");
		try {
			upt.setUp();
			upt.tearDown();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
