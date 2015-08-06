package edu.lternet.pasta.datapackagemanager.solr.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.ISO8601Utility;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EMLParser;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;


public class SolrIndex {

	  /*
	   * Class fields
	   */

	private static final Logger logger = Logger.getLogger(SolrIndex.class);
	
	
	/*
	 * Instance fields
	 */
  
	private final String DATE_GRANULARITY = "DAY";
	private SolrServer solrServer = null;
	
	
	/*
	 * Constructors
	 */
	
	public SolrIndex(String serverURL) {
		this.solrServer = new HttpSolrServer(serverURL);
	}
	
	
	/*
	 * Instance methods
	 */
	
	/**
	 * Executes a commit on the Solr repository.
	 * 
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public void commit() throws IOException, SolrServerException {
		solrServer.commit();
	}
	

	/**
	 * Deletes an EML document from the Solr repository.
	 * 
	 * @param  epid    The EML package id of the doc to be deleted.
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public void deleteEmlDocument(EmlPackageId epid) 
			throws IOException, SolrServerException {
    	String id = String.format("%s.%d", epid.getScope(), epid.getIdentifier());
		List<String> ids = new ArrayList<String>();		
		ids.add(id);

		UpdateResponse updateResponse = solrServer.deleteById(ids);
		int status = updateResponse.getStatus(); // Non-zero indicates failure
		logger.info(String.format("Delete of document id %s; delete status %d", id, status));
	}
	
	
	/**
	 * Indexes an EML document, adding it to the Solr repository.
	 * 
	 * @param epid          the EML package id object
	 * @param emlDocument   the EML document object
	 * @return result       Contains an error message if something went wrong, else null
	 * @throws IOException
	 * @throws SolrServerException
	 */
    public String indexEmlDocument(EmlPackageId epid, String emlDocument)  
    		throws IOException, SolrServerException {
    	String result = null;
    	String scope = epid.getScope();
    	
    	String id = String.format("%s.%d", scope, epid.getIdentifier());
    	String packageId = String.format("%s.%d", id, epid.getRevision());
    	
    	EMLParser emlParser = new EMLParser();
    	DataPackage dataPackage = emlParser.parseDocument(emlDocument);
    	
		if (dataPackage != null) {
			List<String> titles = dataPackage.getTitles();
			List<ResponsibleParty> responsibleParties = dataPackage.getCreatorList();
			
			// Get content of several different date nodes
			String pubDate = dataPackage.getPubDate();
			String beginDate = dataPackage.getBeginDate();
			String endDate = dataPackage.getEndDate();
			String singleDate = dataPackage.getSingleDateTime();
			
			List<String> keywords = dataPackage.getKeywords();
			List<String> timescales = dataPackage.getTimescales();
			String site = dataPackage.getSite();
			String abstractText = dataPackage.getAbstractText();
			String fundingText = dataPackage.getFundingText();
			String methodsText = dataPackage.getMethodsText();
			String geographicDescriptionText = dataPackage.getGeographicDescriptionText();
			String taxonomicCoverageText = dataPackage.getTaxonomicCoverageText();
			
			String westCoord = dataPackage.getWestBoundingCoordinate();
			String southCoord = dataPackage.getSouthBoundingCoordinate();
			String eastCoord = dataPackage.getEastBoundingCoordinate();
			String northCoord = dataPackage.getNorthBoundingCoordinate();
			
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", id);
			solrInputDocument.setField("packageid", packageId);
			solrInputDocument.setField("scope", scope);
			
			String pubDateTimestamp = ISO8601Utility.formatTimestamp(pubDate, DATE_GRANULARITY);
			if (pubDateTimestamp != null) {
				solrInputDocument.setField("pubdate", pubDateTimestamp);
			}

			String beginDateTimestamp = ISO8601Utility.formatTimestamp(beginDate, DATE_GRANULARITY);
			if (beginDateTimestamp != null) {
				solrInputDocument.setField("begindate", beginDateTimestamp);
			}

			String endDateTimestamp = ISO8601Utility.formatTimestamp(endDate, DATE_GRANULARITY);
			if (endDateTimestamp != null) {
				solrInputDocument.setField("enddate", endDateTimestamp);
			}

			String singleDateTimestamp = ISO8601Utility.formatTimestamp(singleDate, DATE_GRANULARITY);
			if (singleDateTimestamp != null) {
				solrInputDocument.setField("singledate", singleDateTimestamp);
			}

			/*
			 *  Index the "title" and "titles" fields. The former is multi-valued and the
			 *  latter is single value. Only single value fields (in this case, "titles")
			 *  can be sorted in search results, and it must be a string field rather
			 *  than a text field.
			 */
			StringBuilder titlesBuilder = new StringBuilder("");
			boolean hasTitle = false;
			for (String title : titles) {
				String normalizedTitle = normalizeText(title);
				// Note how we use addField() for multi-valued fields
				solrInputDocument.addField("title", normalizedTitle);
				titlesBuilder.append(normalizedTitle);
				if (hasTitle) {
					titlesBuilder.append("\n");
				}
				hasTitle = true;
			}
			if (hasTitle) {
				String titlesStr = titlesBuilder.toString();
				solrInputDocument.setField("titles", titlesStr);
			}
					
			for (String keyword : keywords) {
				solrInputDocument.addField("keyword", keyword);
			}
			
			for (String timescale : timescales) {
				solrInputDocument.addField("timescale", timescale);
			}
			
			/*
			 *  Index the "author", "organization", and "responsibleParties" fields. 
			 *  The first two are multi-valued while "responsibleParties" is single value. 
			 *  Only single value fields can be sorted in search results, and it must
			 *  be a string field rather than a text field.
			 */
			StringBuilder authorBuilder = new StringBuilder("");
			StringBuilder organizationBuilder = new StringBuilder("");
			boolean hasAuthor = false;
			boolean hasOrganization = false;
			for (ResponsibleParty responsibleParty : responsibleParties) {
				if (responsibleParty.isPerson()) {
					if (hasAuthor) authorBuilder.append("\n");
					String author = responsibleParty.getCreatorName();
					solrInputDocument.addField("author", author);
					hasAuthor = true;
					authorBuilder.append(author);
				}
				if (responsibleParty.isOrganization()) {
					if (hasOrganization) organizationBuilder.append("\n");
					String organization = responsibleParty.getOrganizationName();
					solrInputDocument.addField("organization", organization);
					hasOrganization = true;
					organizationBuilder.append(organization);
				}
			}
			String partiesStr = null;
			if (hasAuthor && hasOrganization) {
				/*
				 * Note that organizations come ahead of authors in the
				 * responsibleParties string. This is to keep sorting
				 * of search results based on the "responsibleParties" field
				 * in Solr consistent with how the XSLT displays 
				 * the search results in the Data Portal.
				 */
				partiesStr = String.format("%s\n%s", organizationBuilder.toString(), authorBuilder.toString()); 
			}
			else if (hasAuthor) {
				partiesStr = authorBuilder.toString();
			}
			else if (hasOrganization) {
				partiesStr = organizationBuilder.toString();
			}
			
			if (partiesStr != null) {
				solrInputDocument.setField("responsibleParties", partiesStr);
			}
			
			if (site != null) {
				solrInputDocument.setField("site", site);
			}

			if (abstractText != null) {
				solrInputDocument.setField("abstract", abstractText);
			}

			if (fundingText != null) {
				solrInputDocument.setField("funding", fundingText);
			}

			if (methodsText != null) {
				solrInputDocument.setField("methods", methodsText);
			}

			if (geographicDescriptionText != null) {
				solrInputDocument.setField("geographicdescription", geographicDescriptionText);
			}

			if (taxonomicCoverageText != null) {
				solrInputDocument.setField("taxonomic", taxonomicCoverageText);
			}
			
			if (isValidDouble(eastCoord) &&
				isValidDouble(westCoord) &&
				isValidDouble(northCoord) &&
				isValidDouble(southCoord)
			   ) {
				// Only index when north coord >= south coord.
				// However, weat can be greater than east at the International
				// Date Line.
				if (isGreaterThanOrEqualToCoord(northCoord, southCoord)) {
					/*
					 * A rectangle is indexed with four points to represent the
					 * corners. These points should be represented in MinX,
					 * MinY, MaxX, MaxY order.
					 * 
					 * For example:
					 * 
					 * <field name="location_rpt">-74.093 41.042 -69.347 44.558</field>
					 */
					String value = String.format("%s %s %s %s", westCoord,
							southCoord, eastCoord, northCoord);
					solrInputDocument.setField("coordinates", value);
				}
				else {
					logger.warn(
							String.format(
									"Unable to index geospatial coordinates for %s because north " +
							        "coord (%s) is less than south coord (%s)",
									packageId, northCoord, southCoord));
				}
			}
			
			/*
			 * Add PASTA identifier values of source data packages to track provenance
			 */
			ArrayList<String> dataSources = dataPackage.getDataSources();
			for (String dataSourceURL : dataSources) {
				if (DataPackageManager.isPastaDataSource(dataSourceURL)) {
					solrInputDocument.addField("derivedFrom", dataSourceURL);
				}
			}

			UpdateResponse updateResponse = solrServer.add(solrInputDocument);
			int status = updateResponse.getStatus(); // Non-zero indicates failure
			logger.info(String.format(
					"Add of id %s; update status %d", id, status));
		}
		else {
			result = String.format("Solr indexing failed with error while parsing docid %s", id);
		}
    	
    	return result;
    }
    
    
    /*
     * Is this an empty string?
     */
    private boolean isEmpty(String str) {
    	return (str == null || str.equals(""));
    }
    
    
    /*
     * Is the first coordinate greater than or equal to the second?
     */
    private boolean isGreaterThanOrEqualToCoord(String str1, String str2) {
    	Double coord1 = new Double(str1);
    	Double coord2 = new Double(str2);
    	return (coord1 >= coord2);
    }
    
    
    /*
     * Can a valid Double be created from this string?
     */
    private boolean isValidDouble(String str) {
    	boolean isValid = false;
    	
    	if (!isEmpty(str)) {
    		try {
    			Double dbl = new Double(str);
    			isValid = true;
    		}
    		catch (NumberFormatException e) {
    			// No action. Simply return false.
    		}
    	}
    	
    	return isValid;
    }
    
    
	/**
	 * Indexes the DOI for a data package, updating the Solr document's
	 * "doi" field using Solr's atomic update feature.
	 * 
	 * 
	 * @param epid          the EML package id object
	 * @param doi           the DOI (Digital Object Identifier) value to be indexed
	 * @return result       Contains an error message if something went wrong, else null
	 * @throws IOException
	 * @throws SolrServerException
	 */
    public String indexDoi(EmlPackageId epid, String doi)  
    		throws IOException, SolrServerException {
    	String result = null;

    	if (epid != null && doi != null) {
			SolrInputDocument solrInputDocument = new SolrInputDocument();
	    	String scope = epid.getScope();
	    	String id = String.format("%s.%d", scope, epid.getIdentifier());
		    Map<String,String> doiMap = new HashMap<String, String>();
			doiMap.put("set", doi); // Use Solr's atomic update feature
			solrInputDocument.setField("id", id);
			solrInputDocument.setField("doi", doiMap);

			UpdateResponse updateResponse = solrServer.add(solrInputDocument);
			int status = updateResponse.getStatus(); // Non-zero indicates failure
			logger.info(String.format(
					"Update of doi for id %s; update status %d", id, status));
			if (status != 0 ) {
				result = String.format("Solr indexing failed with error while updating doi for id %s", id);
			}
		}
    	else {
    		result = String.format(
    				"Unable to index the doi value: the package id and the doi must be non-null.");
    	}
		
		return result;
    }
    
    
    /*
     * Normalize text to strip off leading whitespace and character entities.
     * Called recursively for cases where there may be more than one
     * leading character entity.
     */
    private static String normalizeText(String text) {
    	String normalized = text;
    	
    	if (normalized != null) {
    		normalized = normalized.trim();
    		if (normalized.startsWith("&#x")) {
    			int beginPosition = normalized.indexOf(";") + 1;
    			if (beginPosition > 0) {
        			normalized = normalized.substring(beginPosition);
        			normalized = normalizeText(normalized);
    			}
    		}
    	}
    	
    	return normalized;
    }
    
    
    public static void main(String[] args) {
    	String unnormalized = args[0];
    	String normalized = normalizeText(unnormalized);
    	System.out.println(String.format("Original: %s; Normalized: %s", unnormalized, normalized));
    }

}
