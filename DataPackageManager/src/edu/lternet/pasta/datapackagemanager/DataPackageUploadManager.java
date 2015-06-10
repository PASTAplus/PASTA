package edu.lternet.pasta.datapackagemanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.DataPackageUpload;
import edu.ucsb.nceas.utilities.Options;


public class DataPackageUploadManager {
	
	/*
	 * Class variables
	 */
	
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static Logger logger = Logger.getLogger(DataPackageUploadManager.class);

	private static String dbDriver = null;
	private static String dbURL = null;
	private static String dbUser = null;
	private static String dbPassword = null;

	private static ArrayList<DataPackageUpload> recentInserts = null;
	private static ArrayList<DataPackageUpload> recentUpdates = null;
	
	
	/*
	 * Class methods
	 */
	
	/**
	 * Loads options from a configuration file.
	 */
	private static void loadOptions() throws Exception {
		try {
			Options options = null;
			options = ConfigurationListener.getOptions();

			if (options == null) {
				ConfigurationListener configurationListener = new ConfigurationListener();
				configurationListener.initialize(dirPath);
				options = ConfigurationListener.getOptions();
			}
			
			// Load database connection options
			dbDriver = options.getOption("dbDriver");
			dbURL = options.getOption("dbURL");
			dbUser = options.getOption("dbUser");
			dbPassword = options.getOption("dbPassword");
		} 
		catch (Exception e) {
			logger.error("Error loading options: " + e.getMessage());
			e.printStackTrace();
			throw (e);
		}
	}

	
	/*
	 * Initializes the database properties and the data structures
	 */
	private static void initialize() throws Exception {
		loadOptions();
		DataPackageRegistry dpr = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
		int limit = 10;
		final int DELTA_DAYS = 60;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		long todayTime = date.getTime();
		long deltaTime = 1000L * 60 * 60 * 24 * DELTA_DAYS;
		long pastTime = todayTime - deltaTime;
		Date pastDate = new Date(pastTime);
		String fromDate = sdf.format(pastDate);

		if (recentInserts == null) {
			logger.warn("Initializing recent inserts.");
			recentInserts = new ArrayList<DataPackageUpload>();
			ArrayList<DataPackageUpload> inserts = dpr.getUploads("createDataPackage", fromDate, limit);
			for (int i = inserts.size() - 1; i >= 0; i--) {
				recentInserts.add(inserts.get(i));
			}
		}
		
		if (recentUpdates == null)  {
			logger.warn("Initializing recent updates.");
			recentUpdates = new ArrayList<DataPackageUpload>();
			ArrayList<DataPackageUpload> updates = dpr.getUploads("updateDataPackage", fromDate, limit);
			for (int i = updates.size() - 1; i >= 0; i--) {
				recentUpdates.add(updates.get(i));
			}
		}
	}
	

	/**
	 * Gets an XML string representation of the list of recent inserts
	 * 
	 * @param limit   The max number of data packages to include in the list
	 * @return        an XML string of DataPackageUpload objects of recent inserts
	 * @throws Exception
	 */
	public static String getRecentInserts(int limit) throws Exception {
		StringBuilder xmlStringBuilder = new StringBuilder("<dataPackageUploads>\n");

		if (recentInserts == null) { initialize(); }
		
		int nUploads = recentInserts.size();
		int count = 0;

		for (int i = nUploads - 1; i >= 0; i--) {
			if (count < limit) {
				DataPackageUpload dpu = recentInserts.get(i);
				xmlStringBuilder.append(dpu.toXML());
				count++;
			}
			else {
				break;
			}
		}

		xmlStringBuilder.append("</dataPackageUploads>\n");
		return xmlStringBuilder.toString();
	}
	
	
	/**
	 * Gets an XML string representation of the list of recent updates
	 * 
	 * @param limit   The max number of data packages to include in the list
	 * @return        an XML string of DataPackageUpload objects of recent updates
	 * @throws Exception
	 */
	public static String getRecentUpdates(int limit)  throws Exception {
		StringBuilder xmlStringBuilder = new StringBuilder("<dataPackageUploads>\n");
		
		if (recentUpdates == null) { initialize(); }
		
		int nUploads = recentUpdates.size();
		int count = 0;

		for (int i = nUploads - 1; i >= 0; i--) {
			if (count < limit) {
				DataPackageUpload dpu = recentUpdates.get(i);
				xmlStringBuilder.append(dpu.toXML());
				count++;
			}
			else {
				break;
			}
		}

		xmlStringBuilder.append("</dataPackageUploads>\n");
		return xmlStringBuilder.toString();
	}
	
	
	/**
	 * Adds a DataPackageUpload object to the list of recent inserts.
	 * 
	 * @param dataPackageUpload   the data package to be added
	 * @throws Exception
	 */
	public static void addRecentInsert(DataPackageUpload dataPackageUpload) throws Exception {
		if (recentInserts == null) { initialize(); }
		
		recentInserts.add(dataPackageUpload);
	}
	
	
	/**
	 * Adds a DataPackageUpload object to the list of recent updates.
	 * 
	 * @param dataPackageUpload   the data package to be added
	 * @throws Exception
	 */
	public static void addRecentUpdate(DataPackageUpload dataPackageUpload) throws Exception {
		if (recentUpdates == null) { initialize(); }
			
		recentUpdates.add(dataPackageUpload);
	}
	
	
	public static void main(String[] args) {
		printUploads(5);
	}
	
	
	/**
	 * Prints the list of recent inserts and recent updates as XML.
	 * Each list is limited in length by the specified limit value.
	 * 
	 * @param limit      The maximum number of recent inserts/updates
	 *                   to output.
	 */
	public static void printUploads(int limit) {
		try {
			String recentInsertsStr = getRecentInserts(limit);
			System.out.println("\nRecent Inserts:\n" + recentInsertsStr);
			String recentUpdatesStr = getRecentUpdates(limit);
			System.out.println("\nRecent Updates:\n" + recentUpdatesStr);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
