package edu.lternet.pasta.datapackagemanager.solr.index;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.ISO8601Utility;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.DataPackage.BoundingCoordinates;
import edu.lternet.pasta.common.eml.DataPackage.DataSource;
import edu.lternet.pasta.common.eml.EMLParser;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class SolrIndex {



	  /*
	   * Class fields
	   */

	private static final Logger logger = Logger.getLogger(SolrIndex.class);
	
	
	/*
	 * Instance fields
	 */
  
	private final String DATE_GRANULARITY = "DAY";
	private SolrClient solrClient = null;
	
	
	/*
	 * Constructors
	 */
	
	public SolrIndex(String serverURL) {
		this.solrClient = new HttpSolrClient.Builder(serverURL).build();
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
		solrClient.commit();
		// solrClient.close();
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

		UpdateResponse updateResponse = solrClient.deleteById(ids);
		int status = updateResponse.getStatus(); // Non-zero indicates failure
		logger.info(String.format("Delete of document id %s; delete status %d", id, status));
	}

	
	private String determineBeginDate(Set<String> beginDates) {
		String lowestDate = ISO8601Utility.formatTimestamp("9999-12-31", DATE_GRANULARITY);
		
		for (String date : beginDates) {
			if (date != null && date.length() == 4) {
				date += "-01-01";
			}
			String timestamp = ISO8601Utility.formatTimestamp(date, DATE_GRANULARITY);
			if (timestamp!=null && (timestamp.compareTo(lowestDate) < 0)) {
				lowestDate = date;
			}
		}
		
		return lowestDate;
	}
	
	
	private String determineEndDate(Set<String> endDates) {
		String highestDate = ISO8601Utility.formatTimestamp("0000-01-01", DATE_GRANULARITY);
		
		for (String date : endDates) {
			if (date != null && date.length() == 4) {
				date += "-12-31";
			}
			String timestamp = ISO8601Utility.formatTimestamp(date, DATE_GRANULARITY);
			if (timestamp != null && (timestamp.compareTo(highestDate) > 0)) {
				highestDate = date;
			}
		}
		
		return highestDate;
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
    	/*
    	 * Do not "eager escape" the content before it is indexed in Solr. 
    	 * We will "lazy escape" all of the text fields later on in the 
    	 * SimpleSolrSearch class when composing the search results XML.
    	 */
    	emlParser.setEagerEscape(false);

    	DataPackage dataPackage = emlParser.parseDocument(emlDocument);
    	
		if (dataPackage != null) {
			List<String> titles = dataPackage.getTitles();
			List<String> projectTitles = dataPackage.getProjectTitles();
			List<String> relatedProjectTitles = dataPackage.getRelatedProjectTitles();
			List<ResponsibleParty> responsibleParties = dataPackage.getCreatorList();
			
			// Get content of several different date nodes
			String pubDate = dataPackage.getPubDate();
			Set<String> beginDates = dataPackage.getBeginDates();
			Set<String> endDates = dataPackage.getEndDates();
			Set<String> singleDateTimes = dataPackage.getSingleDateTimes();
			Set<String> timescales = dataPackage.getAlternativeTimeScales();
			
			List<String> keywords = dataPackage.getKeywords();
			String site = dataPackage.getSite();
			String abstractText = dataPackage.getAbstractText();
			//String projectAbstractText = dataPackage.getProjectAbstractText();
			String fundingText = dataPackage.getFundingText();
			String methodsText = dataPackage.getMethodsText();
			String geographicDescriptionText = dataPackage.getGeographicDescriptionText();
			String taxonomicCoverageText = dataPackage.getTaxonomicCoverageText();
			
			List<DataPackage.BoundingCoordinates> coordinatesList = dataPackage.getCoordinatesList();
			
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", id);
			solrInputDocument.setField("packageid", packageId);
			solrInputDocument.setField("scope", scope);
			
			String pubDateTimestamp = ISO8601Utility.formatTimestamp(pubDate, DATE_GRANULARITY);
			if (pubDateTimestamp != null) {
				solrInputDocument.setField("pubdate", pubDateTimestamp);
			}

			String beginDate = null;
			String beginDateTimestamp = null;
			if (beginDates != null && beginDates.size() > 0) {
				beginDate = determineBeginDate(beginDates);
				if (beginDate != null) {
					beginDateTimestamp = ISO8601Utility.formatTimestamp(beginDate, DATE_GRANULARITY);
					solrInputDocument.setField("begindate", beginDateTimestamp);
				}
			}

			String endDate = null;
			String endDateTimestamp = null;
			if (endDates != null && endDates.size() > 0) {
				endDate = determineEndDate(endDates);
				if (endDate != null) {
					endDateTimestamp = ISO8601Utility.formatTimestamp(endDate, DATE_GRANULARITY);
					solrInputDocument.setField("enddate", endDateTimestamp);
				}
			}
			
			if (beginDateTimestamp != null && endDateTimestamp != null) {
				double duration = calculateTimeSpan(beginDateTimestamp, endDateTimestamp);
				solrInputDocument.setField("duration", duration);
			}

			for (String singleDate : singleDateTimes) {
				String singleDateTimestamp = ISO8601Utility.formatTimestamp(singleDate, DATE_GRANULARITY);
				if (singleDateTimestamp != null) {
					solrInputDocument.addField("singledate", singleDateTimestamp);
				}
			}

			for (String timescale : timescales) {
				solrInputDocument.addField("timescale", timescale);
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
			
			/*
			 *  Index the "projectTitle" field, a multi-valued field.
			 */
			for (String projectTitle : projectTitles) {
				String normalizedTitle = normalizeText(projectTitle);
				// Note how we use addField() for multi-valued fields
				solrInputDocument.addField("projectTitle", normalizedTitle);
			}
			
			/*
			 *  Index the "relatedProjectTitle" field, a multi-valued field.
			 */
			for (String relatedProjectTitle : relatedProjectTitles) {
				String normalizedTitle = normalizeText(relatedProjectTitle);
				solrInputDocument.addField("relatedProjectTitle", normalizedTitle);
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
			boolean foundOrganization = false;
			TreeSet<String> organizationSet = new TreeSet<String>();
			for (ResponsibleParty responsibleParty : responsibleParties) {
				if (responsibleParty.isPerson()) {
					if (hasAuthor) authorBuilder.append("\n");
					String author = responsibleParty.getCreatorName();
					solrInputDocument.addField("author", author);
					hasAuthor = true;
					authorBuilder.append(author);
				}
				if (responsibleParty.hasOrganization()) {
					String organization = responsibleParty.getOrganizationName();
					/*
					 * Don't add duplicate organization values
					 */
					if (!organizationSet.contains(organization)) {
						if (foundOrganization) organizationBuilder.append("\n");
						organizationSet.add(organization);
						solrInputDocument.addField("organization", organization);
						foundOrganization = true;
						organizationBuilder.append(organization);
					}
				}
			}
			String partiesStr = null;
			if (hasAuthor && foundOrganization) {
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
			else if (foundOrganization) {
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

			/*
			 * We (EDI) have decided to not index project abstract. Leaving this
			 * commented-out for now in case we changed our mind.
			 * 
			if (projectAbstractText != null) {
				solrInputDocument.setField("projectAbstract", projectAbstractText);
			}
			*/

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
			
			for (BoundingCoordinates boundingCoordinates : coordinatesList) {
				String west = boundingCoordinates.getWest();
				String south = boundingCoordinates.getSouth();
				String east = boundingCoordinates.getEast();
				String north = boundingCoordinates.getNorth();

				/*
				 * Only index coordinates if all four values are valid doubles.
				 */
				if (isValidDouble(west) &&
					isValidDouble(south) &&
					isValidDouble(east) &&
					isValidDouble(north)
				) {
					/*
					 * Only index coordinates if south is less than north, and neither north or south
					 * are poles and with the same coordinate value.
					 */
					if (isLessThanCoord(north, south)) {
						String gripe = String.format("Unable to index geospatial coordinates for %s because north " +
								"coordinate (%s) is less than south coordinate (%s)", packageId, north, south);
						logger.warn(gripe);
					}
					else if (isSamePole(north, south)) {
						String gripe = String.format("Unable to index geospatial coordinates for %s because north " +
								"and south are poles and have the same coordinates (%s, %s)", packageId, north, south);
						logger.warn(gripe);
					}
					else {
						String value = boundingCoordinates.solrSerialize();
						solrInputDocument.addField("coordinates", value);
					}
				}
			}
			
			/*
			 * Add PASTA identifier values of source data packages to track provenance
			 */
			ArrayList<DataSource> dataSources = dataPackage.getDataSources();
			for (DataSource dataSource : dataSources) {
				String sourceURL = dataSource.getSourceURL();
				if (sourceURL != null && !sourceURL.isEmpty()) {
					solrInputDocument.addField("derivedFrom", sourceURL);
				}
			}

			UpdateResponse updateResponse = solrClient.add(solrInputDocument);
			int status = updateResponse.getStatus(); // Non-zero indicates failure
			logger.info(String.format(
					"Add of id %s; update status %d", id, status));
		}
		else {
			result = String.format("Solr indexing failed with error while parsing docid %s", id);
		}
    	
    	return result;
    }
    
    
    private double calculateTimeSpan(String beginDateTimestamp, 
    		                         String endDateTimestamp) {
    	double timespan = 0.0;
    	String beginDate = stripGranularity(beginDateTimestamp);
    	String endDate = stripGranularity(endDateTimestamp);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        final LocalDate firstDate = LocalDate.parse(beginDate, formatter);
        final LocalDate secondDate = LocalDate.parse(endDate, formatter);
        final long days = ChronoUnit.DAYS.between(firstDate, secondDate);
    	timespan = days / 365.0; // Calculate timespan in years
    	return(timespan);
    }
    
    
    // Strip off the granularity substring such as "/DAY" from the end
    private String stripGranularity(String timestamp) {
    	String stripped = timestamp;
    	
    	if (timestamp != null) {
    		int hyphenIndex = timestamp.indexOf('/');
    		if (hyphenIndex > 0) {
    			stripped = timestamp.substring(0, hyphenIndex);
    		}
    	}
    	
    	return(stripped);
    }
    
    
    /*
     * Is this an empty string?
     */
    private boolean isEmpty(String str) {
    	return (str == null || str.equals(""));
    }
    
    
    /*
     * Is the first coordinate less than the second?
     */
    private boolean isLessThanCoord(String str1, String str2) {
    	Double coord1 = new Double(str1);
    	Double coord2 = new Double(str2);
    	return (coord1 < coord2);
    }


	/*
	 * Is lat and lon both equal to +/-90.0
	 */

	private boolean isSamePole(String str1, String str2) {
		Double coord1 = new Double(str1);
		Double coord2 = new Double(str2);
		return (coord1 == 90.0 && coord2 == 90.0) || (coord1 == -90.0 && coord2 == -90.0);
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

			UpdateResponse updateResponse = solrClient.add(solrInputDocument);
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
