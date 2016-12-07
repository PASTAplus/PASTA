/*
 *
 * $Date: 2015-11-04 12:23:25 -0700 (Wed, 4 Nov 2015) $
 * $Author: dcosta $
 * $Revision:  $
 *
 * Copyright 2011-2015 the University of New Mexico.
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
package edu.lternet.pasta.portal.search;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.portal.ConfigurationListener;


/**
 * The TemporalList class supports generates lists for temporal data in PASTA.
 * 
 * @author Duane Costa
 *
 */
public class TemporalList extends Search {

	/*
	 * Class fields
	 */

	private static final Logger logger = Logger.getLogger(TemporalList.class);

	protected final static String BEGIN_DATE_Q_STRING = "begindate:*";
	protected final static String SINGLE_DATE_Q_STRING = "singledate:*";
	protected final static String TIMESCALE_Q_STRING = "timescale:*";
	protected final static String RANGED_DATE_FIELDS = "packageid,begindate,enddate";
	protected final static String SINGLE_DATE_FIELDS = "packageid,singledate";
	protected final static String TIMESCALE_FIELDS = "packageid,timescale";
	public final static int MAX_ROWS = 100000;
	
	private static HashMap<Integer, String> rangeddates = null;
	private static HashMap<Integer, String> singledates = null;
	private static HashMap<Integer, String> timescales = null;


    /**
     * Builds a query on all begin dates and end dates in PASTA for submission to 
     * the DataPackageManager and then to Solr.
     * 
     * @return the Solr query string to be sent to Solr for searching
     *         begin dates and end dates
     */
  	public static String buildRangedDateQuery() {
  		String solrQuery = null;
  		String qString = BEGIN_DATE_Q_STRING;

  		solrQuery = String.format(
  					"defType=%s&q=%s&fq=%s&fq=%s&fl=%s&debug=%s&start=%d&rows=%d&sort=packageid,asc",
  					DEFAULT_DEFTYPE, qString, ECOTRENDS_FILTER, LANDSAT_FILTER, RANGED_DATE_FIELDS, 
  					DEFAULT_DEBUG, DEFAULT_START, MAX_ROWS);

  		return solrQuery;
  	}
    
	
    /**
     * Builds a query on all single dates in PASTA for submission to 
     * the DataPackageManager and then to Solr.
     * 
     * @return the Solr query string to be sent to Solr for searching
     *         all singledate values
     */
  	public static String buildSingleDateQuery() {
  		String solrQuery = null;
  		String qString = SINGLE_DATE_Q_STRING;

  		solrQuery = String.format(
  					"defType=%s&q=%s&fq=%s&fq=%s&fl=%s&debug=%s&start=%d&rows=%d&sort=packageid,asc",
  					DEFAULT_DEFTYPE, qString, ECOTRENDS_FILTER, LANDSAT_FILTER, SINGLE_DATE_FIELDS, 
  					DEFAULT_DEBUG, DEFAULT_START, MAX_ROWS);

  		return solrQuery;
  	}
    

    /**
     * Builds a query on all timescales in PASTA for submission to 
     * the DataPackageManager and then to Solr.
     * 
     * @return the Solr query string to be sent to Solr for searching
     *         all timescale values
     */
  	public static String buildTimescaleQuery() {
  		String solrQuery = null;
  		String qString = TIMESCALE_Q_STRING;

  		solrQuery = String.format(
  					"defType=%s&q=%s&fq=%s&fq=%s&fl=%s&debug=%s&start=%d&rows=%d&sort=packageid,asc",
  					DEFAULT_DEFTYPE, qString, ECOTRENDS_FILTER, LANDSAT_FILTER, TIMESCALE_FIELDS, 
  					DEFAULT_DEBUG, DEFAULT_START, MAX_ROWS);

  		return solrQuery;
  	}
    

	public static String composeRangedDateList() {
		String rangedDateList = null;
		StringBuffer stringBuffer = new StringBuffer("");
		
		if (rangeddates != null) {
			for (Integer index : rangeddates.keySet()) {
				String datesRecord = rangeddates.get(index);
				if (datesRecord != null && !datesRecord.trim().equals("")) {
					stringBuffer.append(String.format("%s\n", datesRecord.trim()));
				}
			}
		}
		
		rangedDateList = stringBuffer.toString();
		return rangedDateList;
	}

    
	public static String composeSingleDatesList() {
		String singleDatesList = null;
		StringBuffer stringBuffer = new StringBuffer("");
		
		if (singledates != null) {
			for (Integer index : singledates.keySet()) {
				String singleDateRecord = singledates.get(index);
				if (singleDateRecord != null && !singleDateRecord.trim().equals("")) {
					stringBuffer.append(String.format("%s\n", singleDateRecord.trim()));
				}
			}
		}
		
		singleDatesList = stringBuffer.toString();
		return singleDatesList;
	}

    
	public static String composeTimescalesList() {
		String timescalesList = null;
		StringBuffer stringBuffer = new StringBuffer("");
		
		if (timescales != null) {
			for (Integer index : timescales.keySet()) {
				String timescaleRecord = timescales.get(index);
				if (timescaleRecord != null && !timescaleRecord.trim().equals("")) {
					stringBuffer.append(String.format("%s\n", timescaleRecord.trim()));
				}
			}
		}
		
		timescalesList = stringBuffer.toString();
		return timescalesList;
	}


	/**
	 * Parses the Solr query results using regular expression matching (as
	 * opposed to XML parsing)
	 * 
	 * @param xml             the Solr query results, an XML document string
	 * @param fieldName       the field name to parse out of the XML, e.g. "timescale"
	 * @return                a String array of field values parsed from the XML
	 */
	private static HashMap<Integer, String> parseQueryResults(String xml, String fieldName) {
		final String packageIdPatternStr = "^\\s*<packageid>(.+)</packageid>\\s*$";
		final String fieldNamePatternStr = String.format("^\\s*<%s>(.+)</%s>\\s*$", fieldName, fieldName);
		Pattern packageIdPattern = Pattern.compile(packageIdPatternStr);
		Pattern fieldNamePattern = Pattern.compile(fieldNamePatternStr);
		HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
		int index = 0;
		
		if (xml != null) {
			String[] lines = xml.split("\n");
			String packageId = "";
			String fieldValue = "";

			for (String line : lines) {
				Matcher packageIdMatcher = packageIdPattern.matcher(line);
				Matcher fieldNameMatcher = fieldNamePattern.matcher(line);

				if (packageIdMatcher.matches()) {
					packageId = packageIdMatcher.group(1);
				}
				else if (fieldNameMatcher.matches()) {
					fieldValue = fieldNameMatcher.group(1);
					String unescapedXML = StringEscapeUtils.unescapeXml(fieldValue);
					Integer indexInt = new Integer(index);
					index++;
					hashMap.put(indexInt, String.format("%s,%s", packageId, unescapedXML));
				}
			}
		}
		
		return hashMap;
	}
	
	
	/**
	 * Parses the Solr query results using regular expression matching (as
	 * opposed to XML parsing)
	 * 
	 * @param xml             the Solr query results, an XML document string
	 * @param beginDateField  the begindate field name to parse out of the XML, e.g. "begindate"
	 * @param endDateField    the enddate field name to parse out of the XML, e.g. "enddate"
	 * @return                a String array of begindate and enddate values parsed from the XML
	 */
	private static HashMap<Integer, String> parseRangedDatesQueryResults(String xml, String beginDateField, String endDateField) {
		final String packageIdPatternStr = "^\\s*<packageid>(.+)</packageid>\\s*$";
		final String beginDatePatternStr = String.format("^\\s*<%s>(.+)</%s>\\s*$", beginDateField, beginDateField);
		final String endDatePatternStr = String.format("^\\s*<%s>(.+)</%s>\\s*$", endDateField, endDateField);
		Pattern packageIdPattern = Pattern.compile(packageIdPatternStr);
		Pattern beginDatePattern = Pattern.compile(beginDatePatternStr);
		Pattern endDatePattern = Pattern.compile(endDatePatternStr);
		HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
		int index = 0;
		
		if (xml != null) {
			String[] lines = xml.split("\n");
			String packageId = "";
			String beginDateValue = "";
			String endDateValue = "";

			for (String line : lines) {
				Matcher packageIdMatcher = packageIdPattern.matcher(line);
				Matcher beginDateMatcher = beginDatePattern.matcher(line);
				Matcher endDateMatcher = endDatePattern.matcher(line);

				if (packageIdMatcher.matches()) {
					packageId = packageIdMatcher.group(1);
				}
				else if (beginDateMatcher.matches()) {
					beginDateValue = beginDateMatcher.group(1);
				}
				else if (endDateMatcher.matches()) {
					endDateValue = endDateMatcher.group(1);
					Integer indexInt = new Integer(index);
					index++;
					try {
						int months = calculateMonths(beginDateValue, endDateValue);
						hashMap.put(indexInt, String.format("%s,%s,%s,%d", 
								                            packageId, beginDateValue, endDateValue, months));
					}
					catch (DateTimeParseException e) {
						String msg = String.format("Error parsing one of the following: %s %s", 
								                   beginDateValue, endDateValue);
						logger.error(msg);
					}
				}
			}
		}
		
		return hashMap;
	}
	
	
	private static int calculateMonths(String beginDate, String endDate) {
		int durationInMonths;
		
		LocalDate localBeginDate = LocalDate.parse(beginDate);
		LocalDate localEndDate = LocalDate.parse(endDate);
		
		Period duration = Period.between(localBeginDate, localEndDate);
		int years = duration.getYears();
		int months = duration.getMonths();
		int days  = duration.getDays();
		durationInMonths = ((years * 12) + months);
		if (days > 15) { durationInMonths++; }

        System.out.printf(
                "Begin Date: %s,  End Date: %s\n",
                beginDate, endDate);
        System.out.printf(
                "The duration is %d years, %d months and %d days\n",
                duration.getYears(), duration.getMonths(), duration.getDays());
        System.out.printf(
                "The total duration in months is %d\n\n",
                durationInMonths);
		
		return durationInMonths;
	}
	
	
	/**
	 * Updates begindate and enddate values by executing a Solr query.
	 * 
	 * @param rangedDatesQuery    the query string to send to Solr
	 * @throws Exception    
	 */
	private static void updateRangedDates(String rangedDatesQuery) throws Exception {
		String rangedDatesXML = executeQuery(rangedDatesQuery);
		HashMap<Integer, String> rangedDateUpdated = parseRangedDatesQueryResults(rangedDatesXML, "begindate", "enddate");
		rangeddates = rangedDateUpdated;
	}
	
	
	/**
	 * Updates the singledate values by executing a Solr query.
	 * 
	 * @param  singleDatesQuery   the query string to send to Solr
	 * @throws Exception    
	 */
	private static void updateSingleDates(String singleDatesQuery) throws Exception {
		String singleDatesXML = executeQuery(singleDatesQuery);
		HashMap<Integer, String> singleDatesUpdated = parseQueryResults(singleDatesXML, "singledate");
		singledates = singleDatesUpdated;
	}
	
	
	/**
	 * Updates the timescale values by executing a Solr query.
	 * 
	 * @param  timescaleQuery   the query string to send to Solr
	 * @throws Exception    
	 */
	private static void updateTimescales(String timescaleQuery) throws Exception {
		String timescaleXML = executeQuery(timescaleQuery);
		HashMap<Integer, String> timescalesUpdated = parseQueryResults(timescaleXML, "timescale");
		timescales = timescalesUpdated;
	}
	
	
	/**
	 * Updates all temporal values by executing a Solr query for each type
	 * of value (ranged dates, single dates, and timescales).
	 */
	public static void updateTemporalLists() {
			try {
				String rangedDateQuery = buildRangedDateQuery();
				updateRangedDates(rangedDateQuery);
				logger.warn("Finished refreshing ranged dates cache.");

				String singleDateQuery = buildSingleDateQuery();
				updateSingleDates(singleDateQuery);
				logger.warn("Finished refreshing single dates cache.");

				String timescaleQuery = buildTimescaleQuery();
				updateTimescales(timescaleQuery);		
				logger.warn("Finished refreshing timescales cache.");
			}
			catch (Exception e) {
				logger.error("Error updating search results: "
						+ e.getMessage());
				e.printStackTrace();
			}
	}
	
	
	/**
	 * Executes a Solr query by using the Data Package Manager's "search" web service
	 * 
	 * @param queryText     the query text sent to Solr
	 * @return              the XML query results return by the web service
	 * @throws Exception    
	 */
	private static String executeQuery(String queryText) throws Exception
	{
		String uid = "public";
		DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
		String xml = dpmClient.searchDataPackages(queryText);
		return xml;
	}
	
	
	public static void main(String[] args) {
		if (args.length == 0) System.exit(1);
		
		ConfigurationListener.configure();

		String directoryName = args[0];
		String rangedDatesFileName = String.format("%s/rangedDates.csv", directoryName);
		String singleDatesFileName = String.format("%s/singleDates.csv", directoryName);
		String timescalesFileName = String.format("%s/timescales.csv", directoryName);
		
		updateTemporalLists();
		
		String rangedDatesList = composeRangedDateList();
		File rangedDatesFile = new File(rangedDatesFileName);
		
		try (FileWriter fileWriter = new FileWriter(rangedDatesFile)) {
			fileWriter.write(rangedDatesList);
		}
		catch (IOException e) {
			
		}
		
		
		String singleDatesList = composeSingleDatesList();
		File singleDatesFile = new File(singleDatesFileName);

		try (FileWriter fileWriter = new FileWriter(singleDatesFile)) {
			fileWriter.write(singleDatesList);
		}
		catch (IOException e) {
			
		}
		
		String timescalesList = composeTimescalesList();
		File timescalesFile = new File(timescalesFileName);

		try (FileWriter fileWriter = new FileWriter(timescalesFile)) {
			fileWriter.write(timescalesList);
		}
		catch (IOException e) {
			
		}
		
	}
      
}
