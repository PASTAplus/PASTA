/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2017 the University of New Mexico.
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
package edu.lternet.pasta.datapackagemanager.ore;

import java.util.ArrayList;
import java.util.List;

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;

/**
 * 
 * @author Duane Costa
 *
 *
 * ResourceMap is the top-level class in this package. It assembles
 * all of the components needed to produce an RDF XML resource map for a
 * given data package.
 */
public class ResourceMap {
	
	private static final String XML_DECLARATION =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	
	private static final String RDF_START_TAG = 
      "<rdf:RDF\n" +
      "   xmlns:cito=\"http://purl.org/spar/cito/\"\n" +
      "   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
      "   xmlns:dcterms=\"http://purl.org/dc/terms/\"\n" +
      "   xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n" +
      "   xmlns:ore=\"http://www.openarchives.org/ore/terms/\"\n" +
      "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
      "   xmlns:rdfs1=\"http://www.w3.org/2001/01/rdf-schema#\"\n" +
      ">\n";
	
	private static final String RDF_END_TAG = "</rdf:RDF>\n";
	
	private static final String CONFIG_DIR = "WebRoot/WEB-INF/conf";

	private static final String CITO_DOCUMENTS = "cito:documents";
	private static final String CITO_IS_DOCUMENTED_BY = "cito:isDocumentedBy";
	private static final String CREATOR_MBOX = "info@environmentaldatainitiative.org";
	private static final String CREATOR_NAME = "Environmental Data Initiative";
	private static final String CREATOR_URL = "http://environmentaldatainitiative.org";
	private static final String DC_FORMAT = "dc:format";
	private static final String DCTERMS_CREATED = "dcterms:created";
	private static final String DCTERMS_CREATOR = "dcterms:creator";
	private static final String DCTERMS_MODIFIED = "dcterms:modified";
	private static final String DCTERMS_IDENTIFIER = "dcterms:identifier";
	private static final String FOAF_MBOX = "foaf:mbox";
	private static final String FOAF_NAME = "foaf:name";
	private static final String ORE_AGGREGATES = "ore:aggregates";
	private static final String ORE_AGGREGATION_TERM = "http://www.openarchives.org/ore/terms/Aggregation";
	private static final String ORE_DESCRIBES = "ore:describes";
	private static final String ORE_RESOURCE_MAP_TERM = "http://www.openarchives.org/ore/terms/ResourceMap";
	private static final String ORE_TERMS = "http://www.openarchives.org/ore/terms/";
	private static final String RDF_RESOURCE = "rdf:resource";
	private static final String RDF_XML_FORMAT = "application/rdf+xml";
	private static final String RDF_TYPE = "rdf:type";
	private static final String RDFS1_IS_DEFINED_BY = "rdfs1:isDefinedBy";
	private static final String RDFS1_LABEL = "rdfs1:label";

	private Description resourceMapDescription;
	private Description aggregationDescription;
	private Description metadataDescription;
	private List<Description> entityDescriptions;
	private Description reportDescription;
	private Description ediDescription;
	private Description resourceMapLabelDescription;
	private Description aggregationLabelDescription;
	
	
	/*
	 * A quick test program for generating the RDF XML.
	 */
	public static void main(String[] args) {
		try {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(CONFIG_DIR);
			DataPackageManager dpm = new DataPackageManager();
			String pastaUriHead = "https://pasta-d.lternet.edu/package/";
			ResourceMap resourceMap = new ResourceMap(dpm, pastaUriHead, "knb-lter-nin", 1, 1);
			String xml = resourceMap.toXML();
			System.out.println(xml);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Construct a ResourceMap and generate all the components needed for it.
	 * 
	 * @param dpm        A DataPackageManager object
	 * @param scope      The data package scope, e.g. "edi"
	 * @param identifier The data package identifier, e.g. 8
	 * @param revision   The data package revision, e.g. 2
	 * @throws Exception
	 */
	public ResourceMap(DataPackageManager dpm, String pastaUriHead, String scope, Integer identifier, Integer revision) 
			throws Exception {
		List<String> entityURIs = new ArrayList<String>();
		this.entityDescriptions = new ArrayList<Description>();		
		DataPackageRegistry dpr = DataPackageManager.makeDataPackageRegistry();
		
		/*
		 * Compose the PASTA identifiers for the components of this data package
		 */
		final String PASTA_URI = pastaUriHead + "eml";
		String resourceId = String.format("%s/%s/%d/%d", 
				PASTA_URI, scope, identifier, revision);
		final String resourceMapId = String.format("%s/%s/%d/%d",
                PASTA_URI, scope, identifier, revision);
		final String aggregationId = String.format("%s/%s/%d/%d#aggregation",
                PASTA_URI, scope, identifier, revision);
		final String metadataURI = String.format("%s/metadata/%s/%s/%d",
                PASTA_URI, scope, identifier, revision);
		final String reportURI = String.format("%s/report/%s/%s/%d",
                PASTA_URI, scope, identifier, revision);
		String entityList = dpm.listDataEntities(scope, identifier, revision, "");
		String[] entities = entityList.split("\n");
		for (String entity : entities) {
			if (entity.length() > 0) {
				String entityURI = 
						String.format("%s/data/%s/%s/%d/%s", PASTA_URI, scope, identifier, revision, entity);
				entityURIs.add(entityURI);
			}
		}
				
		/*
		 * Build the ResourceMap description
		 */
		resourceMapDescription = new Description(resourceMapId);

		EmptyElement rdfType = new EmptyElement(RDF_TYPE, RDF_RESOURCE, ORE_RESOURCE_MAP_TERM);
		resourceMapDescription.addElement(rdfType);
		
		String dateCreated = dpr.getResourceDateCreated(resourceId);
		TextElement created = new TextElement(DCTERMS_CREATED, dateCreated);
		resourceMapDescription.addElement(created);
		TextElement modified = new TextElement(DCTERMS_MODIFIED, dateCreated);
		resourceMapDescription.addElement(modified);
		
		EmptyElement creatorElement = new EmptyElement(DCTERMS_CREATOR, RDF_RESOURCE, CREATOR_URL);
		resourceMapDescription.addElement(creatorElement);
		
		EmptyElement aggregationElement = new EmptyElement(ORE_DESCRIBES, RDF_RESOURCE, aggregationId);
		resourceMapDescription.addElement(aggregationElement);
		
		String doi = dpr.getDoi(resourceId);
		if (doi != null) {
			TextElement doiElement = new TextElement(DCTERMS_IDENTIFIER, doi);
			resourceMapDescription.addElement(doiElement);
		}
		
		TextElement dcFormatElement = new TextElement(DC_FORMAT, RDF_XML_FORMAT);
		resourceMapDescription.addElement(dcFormatElement);
		

		/*
		 * Build the Aggregation description
		 */
		aggregationDescription = new Description(aggregationId);

		EmptyElement aggregationTypeElement = 
				new EmptyElement(RDF_TYPE, RDF_RESOURCE, ORE_AGGREGATION_TERM);
		aggregationDescription.addElement(aggregationTypeElement);
		
		EmptyElement metadataResourceElement =
				new EmptyElement(ORE_AGGREGATES, RDF_RESOURCE, metadataURI);
		aggregationDescription.addElement(metadataResourceElement);

		for (String entityURI : entityURIs) {
			EmptyElement entityResourceElement =
					new EmptyElement(ORE_AGGREGATES, RDF_RESOURCE, entityURI);
			aggregationDescription.addElement(entityResourceElement);
		}
		
		EmptyElement reportResourceElement =
				new EmptyElement(ORE_AGGREGATES, RDF_RESOURCE, reportURI);
		aggregationDescription.addElement(reportResourceElement);

		
		/*
		 * Build the metadata resource description
		 */
		metadataDescription = new Description(metadataURI);
		TextElement metadataIdentifierElement = new TextElement(DCTERMS_IDENTIFIER, metadataURI);
		metadataDescription.addElement(metadataIdentifierElement);

		for (String entityURI : entityURIs) {
			EmptyElement entityResourceElement =
					new EmptyElement(CITO_DOCUMENTS, RDF_RESOURCE, entityURI);
			metadataDescription.addElement(entityResourceElement);
		}

		reportResourceElement = new EmptyElement(CITO_DOCUMENTS, RDF_RESOURCE, reportURI);
		metadataDescription.addElement(reportResourceElement);
		
		
		/*
		 * Build the entity resource descriptions
		 */
		for (String entityURI : entityURIs) {
			Description entityDescription = new Description(entityURI);
			entityDescriptions.add(entityDescription);
			TextElement entityTextElement = new TextElement(DCTERMS_IDENTIFIER, entityURI);
			entityDescription.addElement(entityTextElement);
			EmptyElement entityEmptyElement = new EmptyElement(CITO_IS_DOCUMENTED_BY, RDF_RESOURCE, metadataURI);
			entityDescription.addElement(entityEmptyElement);
		}

		/*
		 * Build the report resource description
		 */
		reportDescription = new Description(reportURI);
		TextElement reportTextElement = new TextElement(DCTERMS_IDENTIFIER, reportURI);
		reportDescription.addElement(reportTextElement);
		EmptyElement reportEmptyElement = new EmptyElement(CITO_IS_DOCUMENTED_BY, RDF_RESOURCE, metadataURI);
		reportDescription.addElement(reportEmptyElement);
		
		/*
		 * Build the EDI organization description
		 */
		ediDescription = new Description(CREATOR_URL);
		TextElement nameElement = new TextElement(FOAF_NAME, CREATOR_NAME);
		ediDescription.addElement(nameElement);
		TextElement mboxElement = new TextElement(FOAF_MBOX, CREATOR_MBOX);
		ediDescription.addElement(mboxElement);
		
		/*
		 * Build the description label for "ResourceMap"
		 */
		resourceMapLabelDescription = new Description(ORE_RESOURCE_MAP_TERM);
		TextElement remLabelElement = new TextElement(RDFS1_LABEL, "ResourceMap");
		resourceMapLabelDescription.addElement(remLabelElement);
		TextElement remIsDefinedByElement = new TextElement(RDFS1_IS_DEFINED_BY, ORE_TERMS);
		resourceMapLabelDescription.addElement(remIsDefinedByElement);

		/*
		 * Build the description label for "Aggregation"
		 */
		aggregationLabelDescription = new Description(ORE_AGGREGATION_TERM);
		TextElement aggLabelElement = new TextElement(RDFS1_LABEL, "Aggregation");
		aggregationLabelDescription.addElement(aggLabelElement);
		TextElement aggIsDefinedByElement = new TextElement(RDFS1_IS_DEFINED_BY, ORE_TERMS);
		aggregationLabelDescription.addElement(aggIsDefinedByElement);
	}
	
	
	public String toXML() {
		String xml = null;
		StringBuilder sb = new StringBuilder("");
		
		sb.append(XML_DECLARATION);
		sb.append(RDF_START_TAG);
		sb.append(resourceMapDescription.toXML());
		sb.append(aggregationDescription.toXML());
		sb.append(metadataDescription.toXML());
		for (Description entityDescription : entityDescriptions) {
			sb.append(entityDescription.toXML());
		}
		sb.append(reportDescription.toXML());
		sb.append(ediDescription.toXML());
		sb.append(resourceMapLabelDescription.toXML());
		sb.append(aggregationLabelDescription.toXML());
		
		
		sb.append(RDF_END_TAG);
		
		xml = sb.toString();
		return xml;
	}

}
