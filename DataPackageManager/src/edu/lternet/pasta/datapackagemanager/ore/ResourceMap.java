package edu.lternet.pasta.datapackagemanager.ore;

import java.util.ArrayList;
import java.util.List;

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;

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
	
	private static final String RDF_END_TAG =
      "</rdf:RDF>\n";
	
	private static final String PASTA_URI = "https://pasta-d.lternet.edu/package/eml";
	private static final String RESOURCE_MAP_TERM = "http://www.openarchives.org/ore/terms/ResourceMap";
	private static final String AGGREGATION_TERM = "http://www.openarchives.org/ore/terms/Aggregation";
	private static final String ORE_AGGREGATES = "ore:aggregates";
	private static final String RDF_RESOURCE = "rdf:resource";
	private static final String CITO_DOCUMENTS = "cito:documents";
	private static final String CITO_IS_DOCUMENTED_BY = "cito:isDocumentedBy";
	private static final String DCTERMS_IDENTIFIER = "dcterms:identifier";
	private static final String CREATOR_URL = "http://environmentaldatainitiative.org";
	private static final String CREATOR_NAME = "Environmental Data Initiative";
	private static final String CREATOR_MBOX = "info@environmentaldatainitiative.org";
	private static final String RDFS1_LABEL = "rdfs1:label";
	private static final String RDFS1_IS_DEFINED_BY = "rdfs1:isDefinedBy";
	private static final String ORE_TERMS = "http://www.openarchives.org/ore/terms/";

	private String scope;
	private Integer identifier;
	private Integer revision;
	private Description resourceMapDescription;
	private Description aggregationDescription;
	private Description metadataDescription;
	private List<Description> entityDescriptions;
	private Description reportDescription;
	private Description ediDescription;
	private Description resourceMapLabelDescription;
	private Description aggregationLabelDescription;
	private String metadataAbout;
	private String reportAbout;
	
	
	public static void main(String[] args) {
		try {
			ResourceMap resourceMap = new ResourceMap("edi", 8, 2);
			String xml = resourceMap.toXML();
			System.out.println(xml);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ResourceMap(String scope, Integer identifier, Integer revision) 
			throws Exception {
		this.scope = scope;
		this.identifier = identifier;
		this.revision = revision;
		List<String> entityURIs = new ArrayList<String>();
		this.entityDescriptions = new ArrayList<Description>();
		
		ConfigurationListener configurationListener = new ConfigurationListener();
		configurationListener.initialize("WebRoot/WEB-INF/conf");
		DataPackageManager dpm = new DataPackageManager();
		DataPackageRegistry dpr = DataPackageManager.makeDataPackageRegistry();
		
		String resourceId = String.format("%s/%s/%d/%d", 
				                           PASTA_URI, scope, identifier, revision);
		final String resourceMapId = String.format("%s/%s/%d/%d#resourceMap",
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
			String entityURI = String.format("%s/data/%s/%s/%d/%s",
					             PASTA_URI, scope, identifier, revision, entity);
			entityURIs.add(entityURI);
		}
				
		/*
		 * Build the ResourceMap description
		 */
		resourceMapDescription = new Description(resourceMapId);

		EmptyElement rdfType = new EmptyElement("rdf:type", "rdf:resource", RESOURCE_MAP_TERM);
		resourceMapDescription.addElement(rdfType);
		
		String dateCreated = dpr.getResourceDateCreated(resourceId);
		TextElement created = new TextElement("dcterms:created", dateCreated);
		resourceMapDescription.addElement(created);
		TextElement modified = new TextElement("dcterms:modified", dateCreated);
		resourceMapDescription.addElement(modified);
		
		EmptyElement creatorElement = new EmptyElement("dcterms:creator", "rdf:resource", CREATOR_URL);
		resourceMapDescription.addElement(creatorElement);
		
		EmptyElement aggregationElement = new EmptyElement("ore:describes", "rdf:resource", aggregationId);
		resourceMapDescription.addElement(aggregationElement);
		
		String doi = dpr.getDoi(resourceId);
		if (doi != null) {
			TextElement doiElement = new TextElement("dcterms:identifier", doi);
			resourceMapDescription.addElement(doiElement);
		}
		
		final String rdfXmlFormat = "application/rdf+xml";
		TextElement dcFormatElement = new TextElement("dc:format", rdfXmlFormat);
		resourceMapDescription.addElement(dcFormatElement);
		

		/*
		 * Build the Aggregation description
		 */
		aggregationDescription = new Description(aggregationId);

		EmptyElement aggregationTypeElement = 
				new EmptyElement("rdf:type", RDF_RESOURCE, AGGREGATION_TERM);
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
		TextElement metadataIdentifierElement = new TextElement("dcterms:identifier", metadataURI);
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
		 * Build the report resource descriptions
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
		TextElement nameElement = new TextElement("foaf:name", CREATOR_NAME);
		ediDescription.addElement(nameElement);
		TextElement mboxElement = new TextElement("foaf:mbox", CREATOR_MBOX);
		ediDescription.addElement(mboxElement);
		
		/*
		 * Build the description label for ResourceMap
		 */
		resourceMapLabelDescription = new Description(RESOURCE_MAP_TERM);
		TextElement remLabelElement = new TextElement(RDFS1_LABEL, "ResourceMap");
		resourceMapLabelDescription.addElement(remLabelElement);
		TextElement remIsDefinedByElement = new TextElement(RDFS1_IS_DEFINED_BY, ORE_TERMS);
		resourceMapLabelDescription.addElement(remIsDefinedByElement);

		/*
		 * Build the description label for Aggregation
		 */
		aggregationLabelDescription = new Description(AGGREGATION_TERM);
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
