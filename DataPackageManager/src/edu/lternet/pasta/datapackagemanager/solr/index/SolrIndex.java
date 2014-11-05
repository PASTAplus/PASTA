package edu.lternet.pasta.datapackagemanager.solr.index;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EMLParser;
import edu.lternet.pasta.common.eml.ResponsibleParty;


public class SolrIndex {

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
		System.out.println(String.format("Delete of document id %s; delete status %d", id, status));
	}
	
	
	/*
	 * Given a date string, compose an ISO 8601 timestamp string 
	 * that is understandable to Solr.
	 * 
	 * The granularity (e.g. "DAY") is used by Solr. Use the coarsest
	 * granularity needed to improve the performance of date range
	 * searches. For example, don't bother storing publication date
	 * to the nearest minute; instead round down to the nearest day.
	 */
	private String formatTimestamp(String dateStr, String granularity) {
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String timestamp = null;
		
		try {
			if (dateStr == null) {

			}
			else if (dateStr.length() == 4) {
				Date yearDate = yearFormat.parse(dateStr);
				timestamp = iso8601Format.format(yearDate);
			}
			else if (dateStr.length() == 10) {
				Date yearMonthDayDate = yearMonthDayFormat.parse(dateStr);
				timestamp = iso8601Format.format(yearMonthDayDate);
			}
			
			// Append the granularity if it is specified
			if (timestamp != null && granularity != null) {
				timestamp = String.format("%s/%s", timestamp, granularity);
			}
		}
		catch (ParseException e) {
			// Can't parse this date string. Just return null.
		}
		
		return timestamp;
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
    	
    	String id = String.format("%s.%d", epid.getScope(), epid.getIdentifier());
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
			String site = dataPackage.getSite();
			String abstractText = dataPackage.getAbstractText();
			String taxonomicCoverageText = dataPackage.getTaxonomicCoverageText();

			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.setField("id", id);
			solrInputDocument.setField("packageid", packageId);
			
			String pubDateTimestamp = formatTimestamp(pubDate, DATE_GRANULARITY);
			if (pubDateTimestamp != null) {
				solrInputDocument.setField("pubdate", pubDateTimestamp);
			}

			String beginDateTimestamp = formatTimestamp(beginDate, DATE_GRANULARITY);
			if (beginDateTimestamp != null) {
				solrInputDocument.setField("begindate", beginDateTimestamp);
			}

			String endDateTimestamp = formatTimestamp(endDate, DATE_GRANULARITY);
			if (endDateTimestamp != null) {
				solrInputDocument.setField("enddate", endDateTimestamp);
			}

			String singleDateTimestamp = formatTimestamp(singleDate, DATE_GRANULARITY);
			if (singleDateTimestamp != null) {
				solrInputDocument.setField("singledate", singleDateTimestamp);
			}

			for (String title : titles) {
				// Note how we use addField() for multivalued fields
				solrInputDocument.addField("title", title);
			}
			
			for (String keyword : keywords) {
				solrInputDocument.addField("keyword", keyword);
			}
			
			for (ResponsibleParty responsibleParty : responsibleParties) {
				if (responsibleParty.isPerson()) {
					String author = responsibleParty.getCreatorName();
					solrInputDocument.addField("author", author);
				}
				if (responsibleParty.isOrganization()) {
					String organization = responsibleParty.getOrganizationName();
					solrInputDocument.addField("organization", organization);
				}
			}
			
			if (site != null) {
				solrInputDocument.setField("site", site);
			}

			if (abstractText != null) {
				solrInputDocument.setField("abstract", abstractText);
			}

			if (taxonomicCoverageText != null) {
				solrInputDocument.setField("taxonomic", taxonomicCoverageText);
			}

			UpdateResponse updateResponse = solrServer.add(solrInputDocument);
			int status = updateResponse.getStatus(); // Non-zero indicates failure
			System.out.println(String.format(
					"Add of id %s; update status %d", id, status));
		}
		else {
			result = String.format("Solr indexing failed with error while parsing docid %s", id);
		}
    	
    	return result;
    }

}