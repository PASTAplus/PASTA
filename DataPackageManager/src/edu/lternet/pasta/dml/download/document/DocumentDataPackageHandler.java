/**
 *    '$RCSfile: DocumentDataPackageHandler.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2008-08-19 22:13:01 $'
 *   '$Revision: 1.3 $'
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
package edu.lternet.pasta.dml.download.document;

import java.io.InputStream;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.lternet.pasta.dml.database.DatabaseConnectionPoolInterface;
import edu.lternet.pasta.dml.database.DatabaseHandler;
import edu.lternet.pasta.dml.database.SimpleDatabaseLoader;
import edu.lternet.pasta.dml.database.VectorReader;
import edu.lternet.pasta.dml.download.EcogridEndPointInterface;
import edu.lternet.pasta.dml.parser.DataPackage;
import edu.lternet.pasta.dml.parser.Entity;
import edu.lternet.pasta.dml.parser.document.DocumentDataPackage;
import edu.lternet.pasta.dml.parser.document.DocumentDataPackageParser;
import edu.lternet.pasta.dml.util.DocumentDownloadUtil;

public class DocumentDataPackageHandler {

	public static Log log = LogFactory.getLog(DocumentDataPackageHandler.class);
	
	private String docId = null;
	private EcogridEndPointInterface ecogridEndPointInterface = null;
	private Map attributeMap = null;
	private boolean loaded = false;
	
	private DatabaseConnectionPoolInterface connectionPool = null;
	private DatabaseHandler databaseHandler = null;
	
	private DocumentDataPackageParser ddpp = null;
	
	
	public DocumentDataPackageHandler(DatabaseConnectionPoolInterface pool) {
	    
		//initialize the database classes
		try {
			connectionPool = pool; 
				//DatabaseConnectionPoolFactory.getDatabaseConnectionPoolInterface();
			databaseHandler = new DatabaseHandler(connectionPool.getDBAdapterName());
		} catch (Exception e) {
			log.debug("could not create DatabaseHandler: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void downloadDocument() throws Exception {
		
		// download the document
		DocumentDownloadUtil ddu = new DocumentDownloadUtil();
		InputStream inputStream = ddu.downloadDocument(docId, ecogridEndPointInterface);
			
		log.debug("done with the download");

		long startTime = System.currentTimeMillis();

		//set up the parser, and parse
		ddpp = new DocumentDataPackageParser(docId);
		ddpp.setAttributeXPathMap(attributeMap);
		ddpp.parse(inputStream);
		
		loaded = true;
		
		long endTime = System.currentTimeMillis();
		log.debug((endTime - startTime) + " ms to parse document data");
		
		log.debug("downloaded data");
		
	}
		
	public DataPackage loadDataToDB() throws Exception {
		
		//do the download/parsing if need be
		if (!isLoaded()) {
			downloadDocument();
		}
		
		//[re]generate the entity from the current attribute map
		ddpp.generateEntity();
		
		//construct the datapackage with an Entity
		DataPackage dataPackage = null;
		Entity entity = null;
		
		dataPackage = ddpp.getDataPackage();
		entity = dataPackage.getEntityList()[0];
		
		//generate/update the table
		databaseHandler.generateTable(entity);
		databaseHandler.dropTable(entity);
		databaseHandler.generateTable(entity);
		log.debug("created table for entity: " + entity.getDBTableName());
		
		//insert the data		
		Vector rowData = ((DocumentDataPackage)dataPackage).getRecordRow();
		VectorReader dataReader = new VectorReader();
		dataReader.addOneRowDataVector(rowData);
		SimpleDatabaseLoader databaseLoader = 
			new SimpleDatabaseLoader(
					connectionPool.getDBAdapterName(), 
					entity, 
					dataReader);
		if (!databaseLoader.doesDataExist(entity.getEntityIdentifier())) {
			databaseLoader.run(); //yes, this is Runnable
		}
		
		log.debug("loaded data to db");
					
		return dataPackage;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public EcogridEndPointInterface getEcogridEndPointInterface() {
		return ecogridEndPointInterface;
	}

	public void setEcogridEndPointInterface(
			EcogridEndPointInterface ecogridEndPointInterface) {
		this.ecogridEndPointInterface = ecogridEndPointInterface;
	}

	public Map getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(Map attributeMap) {
		this.attributeMap = attributeMap;
	}
	
	public boolean isLoaded() {
		return loaded;
	}

}
