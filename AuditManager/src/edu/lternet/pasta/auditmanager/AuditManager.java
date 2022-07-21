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

import edu.lternet.pasta.common.audit.AuditRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
* @author dcosta
* @version 1.0
* @created 16-Aug-2011 1:40:03 PM
*
* The DataPackageRegistry class maintains the current state of data package
* resources in PASTA by reading from and writing to a database.
*/
public class AuditManager {

  /*
   * Class fields
   */

  public static final String AUDIT_OPENING_TAG = "<auditReport>\n";
  public static final String AUDIT_CLOSING_TAG = "</auditReport>\n";
  private static Logger logger = Logger.getLogger(AuditManager.class);


  /*
   * Instance fields
   */

  // Name of the database table where audits are logged
  private final String AUDIT_MANAGER_SCHEMA = "auditmanager";
  private final String AUDIT_MANAGER_TABLE = "EVENTLOG";
  private final String AUDIT_MANAGER_TABLE_QUALIFIED = AUDIT_MANAGER_SCHEMA + "." + AUDIT_MANAGER_TABLE;
  private Properties properties = null;

  /*
   *  Temporary directory for storing audit query XML results file.
   */
  private String tmpDir;

  private String dbDriver;           // database driver
  private String dbURL;              // database URL
  private String dbUser;             // database user name
  private String dbPassword;         // database user password


  /*
   * Constructors
   */

  /**
   * Constructs a new DataPackageRegistry object.
   *
   * @return  an EMLDataCache object
   */
  public AuditManager(Properties p)
         throws ClassNotFoundException, SQLException {
    this.properties = p;
    this.dbDriver = ConfigurationListener.getProperty(p, "dbDriver");
    this.dbURL = ConfigurationListener.getProperty(p, "dbURL");
    this.dbUser = ConfigurationListener.getProperty(p, "dbUser");
    this.dbPassword = ConfigurationListener.getProperty(p, "dbPassword");

    /* Initialize the tmpDir value and create the directory */
    String tmpDirValue = ConfigurationListener.getTmpDir();
    if (tmpDirValue != null && !tmpDirValue.equals("")) {
    	this.tmpDir = tmpDirValue;
    	File tmpDirFile = new File(tmpDirValue);
    	tmpDirFile.mkdirs();
    }

    /*
     * Check for existence of Data Package Registry table.
     * Create it if it does not already exist.
     */
    if (!isAuditTablePresent()) {
      String message = "The audit table was not found in the PASTA database.";
      throw new SQLException(message);
    }
  }


  /*
   * Class methods
   */

  /**
   * Converts a date string to a valid timestamp string by appending a
   * default time to the date. Useful when querying the audit database
   * with precise start times and end times.
   *
   * @param s               The date string.
   * @param isToTime        True if the goal is to compose a "to-time", 
   *                        else compose a "from-time". 
   * @return                The timestamp string.
   */
  public static String dateToTimestamp(String s, boolean isToTime) {
    if (s == null || s.isEmpty()) throw new IllegalStateException();

    /*
     *  Add a timestamp if it is missing from the date string.
     *  If isToTime is true, set the time to the last second of the day.
     */
    if (s.indexOf('T') == -1) {
      if (isToTime) {
        s += "T23:59:59";
      }
      else {
        s += "T00:00:00";
      }
    }

    return s;
  }


  public static void main(String[] args)
         throws ClassNotFoundException, SQLException {
	  ConfigurationListener.loadPropertiesFile("auditmanager.properties");
	  Properties properties = ConfigurationListener.getProperties();
	  AuditManager auditManager = new AuditManager(properties);
	  auditManager.fixArchiveRecords();
  }


  /**
   * Constructs a Date object from a date string.
   *
   * @param s               The date string.
   * @param isToTime        True if the goal is to compose a "to-time", 
   *                        else compose a "from-time". 
   * @return                The Date object.
   */
  public static Date strToDate(String s, boolean isToTime) {
    if (s == null || s.isEmpty()) throw new IllegalStateException();

    /*
     *  Add a timestamp if it is missing from the date string.
     *  If isToTime is true, set the time to the last second of the day.
     */
    if (s.indexOf('T') == -1) {
      if (isToTime) {
        s += "T23:59:59";
      }
      else {
        s += "T00:00:00";
      }
    }

    Date returnDate = DatatypeConverter.parseDateTime(s).getTime();
    logger.debug("returnDate: " + returnDate.toString());
    return returnDate;
  }


  /*
   * Instance methods
   */

  /**
   * Adds a new audit entry to the audit resource registry.
   *
   * @param service
   * @param category
   * @param serviceMethod
   * @param entryText
   * @param resourceId
   * @param statusCode
   * @param userId
   * @param userAgent
   * @param groups
   * @param authSystem
   */
  public int addAuditEntry (
     String service,
     String category,
     String serviceMethod,
     String entryText,
     String resourceId,
     int statusCode,
     String userId,
     String userAgent,
     String groups,
     String authSystem
     )
          throws ClassNotFoundException, SQLException {
    int auditId = 0;
    Connection connection = null;
    java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());

    StringBuffer insertSQL = new StringBuffer("INSERT INTO " +
                                              AUDIT_MANAGER_TABLE_QUALIFIED +
                                              "(");
    insertSQL.append("entrytime, service, category, servicemethod, " +
                     "entrytext, resourceid, statuscode, userid, userAgent, groups, authsystem) " +
                     "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
    String insertString = insertSQL.toString();
    logger.debug("insertString: " + insertString);

    try {
        connection = getConnection();
        PreparedStatement pstmt = connection.prepareStatement(insertString, Statement.RETURN_GENERATED_KEYS);
        pstmt.setTimestamp(1, ts);
        pstmt.setString(2, service);
        pstmt.setString(3, category);
        pstmt.setString(4, serviceMethod);
        pstmt.setString(5, entryText);
        pstmt.setString(6, resourceId);
        pstmt.setInt(7, statusCode);
        pstmt.setString(8, userId);
        pstmt.setString(9, userAgent);
        pstmt.setString(10, groups);
        pstmt.setString(11, authSystem);
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();
        while (rs.next()) {
          auditId = rs.getInt(1);
        }

        if (pstmt != null) {
          pstmt.close();
        }
      }
    catch (SQLException e) {
        logger.error("Error inserting record for resource " + resourceId
            + " into the resource registry (" + AUDIT_MANAGER_TABLE_QUALIFIED + ")");
        logger.error("SQLException: " + e.getMessage());
        throw (e);
      }
    finally {
        returnConnection(connection);
    }

    /*
     * If the service method matches one of the resource read types and the read succeeded with a 200 status,
     * then register this resource read with the ReadsManager.
     */
    if (statusCode == 200) {
        ReadsManager rm = new ReadsManager(this.properties);
        ReadsManager.ResourceType resourceType = rm.resourceTypeFromServiceMethod(serviceMethod);
        if (resourceType != null) {
            boolean isNonRobotRead = rm.isNonRobotRead(userId);
            rm.registerResourceRead(resourceId, resourceType, isNonRobotRead);
        }
    }

    return auditId;
  }


  private String composeWhereClause(Map<String, List<String>> queryParams, boolean orderBy) {
    String whereClause = null;
    String limit = null;

    StringBuffer stringBuffer = new StringBuffer(" WHERE category IS NOT NULL");

    boolean orderDesc = false;

    for (String key : queryParams.keySet()) {
      if (!key.equalsIgnoreCase("limit")) {
        stringBuffer.append(" AND ");

        List<String> values = queryParams.get(key);

        if (key.equalsIgnoreCase("roid")) {
          orderDesc = true;
        }

        if (key.equalsIgnoreCase("fromtime")) {
          String value = values.get(0);
          String timestamp = dateToTimestamp(value, false);
          stringBuffer.append(String.format(" entrytime >= '%s'", timestamp));
        }
        else if (key.equalsIgnoreCase("totime")) {
          String value = values.get(0);
          String timestamp = dateToTimestamp(value, true);
          stringBuffer.append(String.format(" entrytime <= '%s'", timestamp));
        }
        else if (key.equalsIgnoreCase("group")) {
          String orClause = composeORClause("groups", values);
          stringBuffer.append(orClause);
        }
        else if (key.equalsIgnoreCase("userAgent")) {
          stringBuffer.append(String.format(" useragent ILIKE '%%%s%%'", values.get(0)));
        }
        else if (key.equalsIgnoreCase("userAgentNegate")) {
          stringBuffer.append(String.format(" useragent NOT ILIKE '%%%s%%'", values.get(0)));
        }
        else {
          String orClause = composeORClause(key, values);
          stringBuffer.append(orClause);
        }
      }
    }

    /*
     * If orderBy is true, the audit records will be ordered by oid (identifier) value in
     * ascending order.
     */
    if (orderBy) { stringBuffer.append(" ORDER BY oid " + (orderDesc ? "DESC" : "ASC")); }

    /*
     * Append a record limit value if specified
     */
    for (String key : queryParams.keySet()) {
      List<String> values = queryParams.get(key);
      if (key.equalsIgnoreCase("limit")) {
        String limitStr = values.get(0);
        try {
          Integer limitInt = new Integer(limitStr);
          if (limitInt > 0) {
            limit = limitInt.toString();
          }
        }
        catch (NumberFormatException e) {
        }
        if (limit != null) {
          stringBuffer.append(String.format(" LIMIT %s", limit));
        }
      }
    }

    whereClause = stringBuffer.toString();

    return whereClause;
  }

  private String composeORClause(String key, List<String> values) {
    StringBuffer stringBuffer = new StringBuffer("( ");
    boolean firstValue = true;

    if (values.size() == 0) return "";

    for (String value : values) {
      if (!firstValue) { stringBuffer.append(" OR "); }
      String fieldName = getFieldName(key);

      if (fieldName.equals("resourceid")) {
        stringBuffer.append(String.format("%s like '%%%s%%'", fieldName, value));
      }
      else if (fieldName.equals("startoid")) {
        stringBuffer.append(String.format("oid>%s", value));
      }
      else if (fieldName.equals("roid")) {
        stringBuffer.append(String.format("oid<%s", value));
      }
      else {
        stringBuffer.append(String.format("%s='%s'", fieldName, value));
      }

      firstValue = false;
    }

    stringBuffer.append(" )");
    String orClause = stringBuffer.toString();

    return orClause;
  }


	private String composeWhereClauseRecentUploads(Map<String, List<String>> queryParams) {
		String whereClause = null;
		String limit = null;
	    final String ORDER_CLAUSE = " ORDER BY entrytime DESC";
		StringBuffer stringBuffer = new StringBuffer(" WHERE category='info'");

		for (String key : queryParams.keySet()) {
			if (!key.equalsIgnoreCase("limit")) {
				stringBuffer.append(" AND ");
				List<String> values = queryParams.get(key);

				if (key.equalsIgnoreCase("fromtime")) {
					String value = values.get(0);
					String timestamp = dateToTimestamp(value, false);
					stringBuffer.append(String.format(" entrytime >= '%s'",
							timestamp));
				}
				else if (key.equalsIgnoreCase("serviceMethod")) {
					String value = values.get(0);
					stringBuffer.append(String.format(" servicemethod='%s'",
							value));
				}
			}
		}

		stringBuffer.append(ORDER_CLAUSE);

		/*
		 * Append a record limit value if specified
		 */
		for (String key : queryParams.keySet()) {
			List<String> values = queryParams.get(key);
			if (key.equalsIgnoreCase("limit")) {
				String limitStr = values.get(0);
				try {
					Integer limitInt = new Integer(limitStr);
					if (limitInt > 0) {
						limit = limitInt.toString();
					}
				}
				catch (NumberFormatException e) {
				}
				if (limit != null) {
					stringBuffer.append(String.format(" LIMIT %s", limit));
				}
			}
		}

		whereClause = stringBuffer.toString();

		return whereClause;
	}


  /**
   * Creates a new audit entry and returns its identifier.
   *
   * @param  auditEntry    the audit entry XML string
   * @return auditId   the audit entry identifier
   */
  public int create(String auditEntry)
           throws ClassNotFoundException, SQLException {
    int auditId = 0;

    AuditRecord auditRecord = new AuditRecord(auditEntry);

    String service = auditRecord.getService();
    String category = auditRecord.getCategory();
    String serviceMethod = auditRecord.getServiceMethod();
    String entryText = auditRecord.getEntryText();
    String resourceId = auditRecord.getResourceId();
    int statusCode = auditRecord.getResponseStatus();
    String userId = auditRecord.getUser();
    String userAgent = auditRecord.getUserAgent();
    String groups = auditRecord.getGroups();
    String authSystem = auditRecord.getAuthSystem();

    try {
      auditId = addAuditEntry (service, category, serviceMethod,
                     entryText, resourceId, statusCode, userId,
                     userAgent, groups, authSystem);
    }
    finally {

    }

    return auditId;
  }


  /**
   * Gets a list of audit log records from the audit table (named "eventlog")
   * matching the provided criteria.
   *
   * @param queryParams a map of query parameters and the values they should be matched to
   * @return an XML string of audit records
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public MyPair<String, MyPair<Integer, Integer>> getAuditRecords(Map<String, List<String>> queryParams)
      throws ClassNotFoundException, SQLException, IllegalArgumentException
  {
    String xmlString = null;
    int firstOid = 0;
    int lastOid = 0;
    if (queryParams != null) {
      Connection connection = null;
      String selectString =
          "SELECT oid, entrytime, service, category, servicemethod, entrytext," +
              " resourceid, statuscode, userid, userAgent, groups, authsystem " +
              "FROM " + AUDIT_MANAGER_TABLE_QUALIFIED;

      selectString += composeWhereClause(queryParams, true);
      logger.info("WHERE clause: " + selectString);

      Statement stmt = null;
      StringBuilder stringBuffer = new StringBuilder(AUDIT_OPENING_TAG);
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(selectString);
        while (rs.next()) {
          int oid = rs.getInt(1);
          
          if (firstOid == 0 || firstOid > oid) {
            firstOid = oid;
          }
          if (lastOid == 0 || lastOid < oid) {
            lastOid = oid;
          }

          java.sql.Timestamp sqlTimestamp = rs.getTimestamp(2);
          String service = rs.getString(3);
          String category = rs.getString(4);
          String serviceMethod = rs.getString(5);
          String entryText = rs.getString(6);
          String resourceId = rs.getString(7);
          int statusCode = rs.getInt(8);
          String userId = rs.getString(9);
          String userAgent = rs.getString(10);
          String groups = rs.getString(11);
          String authSystem = rs.getString(12);
          AuditRecord auditRecord = new AuditRecord();
          auditRecord.setOid(oid);
          java.util.Date entryTime = new java.util.Date(sqlTimestamp.getTime());
          auditRecord.setEntryTime(entryTime);
          auditRecord.setService(service);
          auditRecord.setCategory(category);
          auditRecord.setServiceMethod(serviceMethod);
          auditRecord.setEntryText(entryText);
          auditRecord.setResourceId(resourceId);
          auditRecord.setResponseStatus(new Integer(statusCode));
          auditRecord.setUser(userId);
          auditRecord.setUserAgent(userAgent);
          auditRecord.setGroups(groups);
          auditRecord.setAuthSystem(authSystem);

          stringBuffer.append(auditRecord.toXML());
        }
      } catch (ClassNotFoundException e) {
        logger.error("ClassNotFoundException: " + e.getMessage());
        throw (e);
      } catch (SQLException e) {
        logger.error("SQLException: " + e.getMessage());
        throw (e);
      } finally {
        stringBuffer.append(AUDIT_CLOSING_TAG);
        xmlString = stringBuffer.toString();
        if (stmt != null) {
          stmt.close();
        }
        returnConnection(connection);
      }
    }

    return new MyPair<>(xmlString, new MyPair<>(firstOid, lastOid));
  }


  /**
   * Get audit records as a CSV stream
   *
   * @param output Output stream that receives the CSV
   * @param queryParams a map of query parameters and the values they should be matched to
   */
  public void getAuditRecordsCsv(OutputStream output, Map<String, List<String>> queryParams)
  {
    try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(output), CSVFormat.EXCEL)) {
      if (queryParams != null) {
        Connection connection = null;
        String selectString =
            "SELECT oid, entrytime, service, category, servicemethod, entrytext," +
                " resourceid, statuscode, userid, userAgent, groups, authsystem " +
                "FROM " + AUDIT_MANAGER_TABLE_QUALIFIED;
        selectString += composeWhereClause(queryParams, false);

        logger.info(String.format("getAuditRecordsCsv() selectString: %s", selectString));
        System.out.printf("getAuditRecordsCsv() selectString: %s", selectString);

        Statement stmt = null;
        try {
          connection = getConnection();
          // To enable server side cursor, we need to turn autocommit off, and set the
          // fetch size.
          // https://jdbc.postgresql.org/documentation/head/query.html#query-with-cursor
          connection.setAutoCommit(false);
          stmt = connection.createStatement();
          final long startTime = System.currentTimeMillis();
          // Set fetch size to enable server side cursor.
          stmt.setFetchSize(10);
          ResultSet rs = stmt.executeQuery(selectString);
          final long endTime = System.currentTimeMillis();
          logger.info("Query execution time: " + (endTime - startTime));
          System.out.printf("Query execution time: %f sec%n", (endTime - startTime) / 1000.0);

          printer.printRecord("Oid", "EntryTime", "Service", "Category",
              "ServiceMethod", "EntryText", "ResourceId", "ResponseStatus", "User",
              "UserAgent", "Groups", "AuthSystem");

          while (rs.next()) {
            int oid = rs.getInt(1);
            java.sql.Timestamp sqlTimestamp = rs.getTimestamp(2);
            String service = rs.getString(3);
            String category = rs.getString(4);
            String serviceMethod = rs.getString(5);
            String entryText = rs.getString(6);
            String resourceId = rs.getString(7);
            int statusCode = rs.getInt(8);
            String userId = rs.getString(9);
            String userAgent = rs.getString(10);
            String groups = rs.getString(11);
            String authSystem = rs.getString(12);
            java.util.Date entryTime = new java.util.Date(sqlTimestamp.getTime());
            printer.printRecord(oid, entryTime, service, category, serviceMethod,
                entryText, resourceId, Integer.toString(statusCode), userId, userAgent,
                groups, authSystem);
          }
        } catch (ClassNotFoundException e) {
          logger.error("ClassNotFoundException: " + e.getMessage());
          throw (e);
        } catch (SQLException e) {
          logger.error("SQLException: " + e.getMessage());
          throw (e);
        } finally {
          if (stmt != null) {
            stmt.close();
          }
          returnConnection(connection);
        }
      }
      // Flush and close are no-ops on OutputStream
      // output.flush();
      // output.close();
    } catch (IOException | ClassNotFoundException | SQLException ex) {
      ex.printStackTrace();
    }
  }


  /**
   * Gets a list of audit log records from the audit table (named "eventlog")
   * matching the provided criteria.
   *
   * @param queryParams    a map of query parameters and the values they should be matched to
   * @return               an XML string of audit records
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public String getRecentUploads(Map<String, List<String>> queryParams)
           throws ClassNotFoundException, SQLException, IllegalArgumentException {
	StringBuffer stringBuffer = new StringBuffer(AUDIT_OPENING_TAG);
    String xmlString = null;

      Connection connection = null;

      final String WHERE_CLAUSE = composeWhereClauseRecentUploads(queryParams);

      String selectString =
        "SELECT oid, entrytime, service, category, servicemethod," +
        " entrytext, resourceid, statuscode, userid, groups, authsystem " +
        "FROM " + AUDIT_MANAGER_TABLE_QUALIFIED +
        WHERE_CLAUSE;

      logger.debug(selectString);

      Statement stmt = null;

      try {
        connection = getConnection();
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(selectString);

        while (rs.next()) {
          int oid = rs.getInt(1);
          java.sql.Timestamp sqlTimestamp = rs.getTimestamp(2);
          String service = rs.getString(3);
          String category = rs.getString(4);
          String serviceMethod = rs.getString(5);
          String entryText = rs.getString(6);
          String resourceId = rs.getString(7);
          int statusCode = rs.getInt(8);
          String userId = rs.getString(9);
          String groups = rs.getString(10);
          String authSystem = rs.getString(11);
          AuditRecord auditRecord = new AuditRecord();
          auditRecord.setOid(oid);
          java.util.Date entryTime = new java.util.Date(sqlTimestamp.getTime());
          auditRecord.setEntryTime(entryTime);
          auditRecord.setService(service);
          auditRecord.setCategory(category);
          auditRecord.setServiceMethod(serviceMethod);
          auditRecord.setEntryText(entryText);
          auditRecord.setResourceId(resourceId);
          auditRecord.setResponseStatus(new Integer(statusCode));
          auditRecord.setUser(userId);
          auditRecord.setGroups(groups);
          auditRecord.setAuthSystem(authSystem);
          stringBuffer.append(auditRecord.toXML());
        }
      }
      catch(ClassNotFoundException e) {
        logger.error("ClassNotFoundException: " + e.getMessage());
        throw(e);
      }
      catch(SQLException e) {
        logger.error("SQLException: " + e.getMessage());
        throw(e);
      }
      finally {
        stringBuffer.append(AUDIT_CLOSING_TAG);
        xmlString = stringBuffer.toString();
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }

    return xmlString;
  }


  /*
   * Saves an XML list of audit records to the file system for subsequent
   * processing.
   */
	private File saveXmlToFile(File tempFile, StringBuffer xmlBuffer) {
		boolean append = true;

		try {
			FileWriter fileWriter = new FileWriter(tempFile, append);
			writeToWriter(xmlBuffer, fileWriter, true);
		}
		catch (IOException e) {
			logger.error("IOException:\n" + e.getMessage());
			e.printStackTrace();
		}

		return tempFile;
	}


  /**
   *  Reads character data from the <code>StringBuffer</code> provided, and 
   *  writes it to the <code>Writer</code> provided, using a buffered write. 
   *
   *  @param  buffer              <code>StringBuffer</code> whose contents are 
   *                              to be written to the <code>Writer</code>
   *
   *  @param  writer              <code>java.io.Writer</code> where contents 
   *                              of StringBuffer are to be written
   *
   *  @param  closeWhenFinished   <code>boolean</code> value to indicate 
   *                              whether Reader should be closed when reading
   *                              finished
   *
   *  @return                     <code>StringBuffer</code> containing  
   *                              characters read from the <code>Reader</code>
   *
   *  @throws IOException if there are problems accessing or using the Writer.
   */
  public void writeToWriter(StringBuffer buffer,
                            Writer writer,
                            boolean closeWhenFinished)
          throws IOException {
    if (writer == null) {
      throw new IOException("writeToWriter(): Writer is null");
    }

    char[] bufferChars = new char[buffer.length()];
    buffer.getChars(0, buffer.length(), bufferChars, 0);

    try {
      writer.write(bufferChars);
      writer.flush();
    }
    catch (IOException ioe) {
      throw ioe;
    }
    finally {
      if (closeWhenFinished) {
        try {
          if (writer != null)
            writer.close();
        }
        catch (IOException ce) {
          ce.printStackTrace();
        }
      }
    }
  }


  /**
   * Returns a count of the number audit log records from the audit table (named "eventlog")
   * matching the provided criteria.
   *
   * @param queryParams    a map of query parameters and the values they should be matched to
   * @return               an integer count value indicating the number of matching audit log records
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
	public Integer getAuditRecordsCount(Map<String, List<String>> queryParams)
			throws ClassNotFoundException, SQLException, IllegalArgumentException {
		Integer matchCount = null;

		if (queryParams != null) {
			Connection connection = null;

			String selectString = "SELECT count(*) FROM " + AUDIT_MANAGER_TABLE_QUALIFIED;
      selectString += composeWhereClause(queryParams, false);
		    logger.info("WHERE clause: " + selectString);

			Statement stmt = null;

			try {
				connection = getConnection();
				stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(selectString);

				while (rs.next()) {
					matchCount = rs.getInt(1);
				}
			}
			catch (ClassNotFoundException e) {
				logger.error("ClassNotFoundException: " + e.getMessage());
				throw (e);
			}
			catch (SQLException e) {
				logger.error("SQLException: " + e.getMessage());
				throw (e);
			}
			finally {
				if (stmt != null)
					stmt.close();
				returnConnection(connection);
			}
		}

		return matchCount;
	}


//  /**
//   * Gets a list of audit log records from the audit table (named "eventlog")
//   * matching the provided criteria.
//   *
//   * @param queryParams    a map of query parameters and the values they should be matched to
//   * @return               an XML string of audit records
//   * @throws ClassNotFoundException
//   * @throws SQLException
//   * @throws IllegalArgumentException
//   */
//  public File getAuditRecordsFile(Map<String, List<String>> queryParams)
//           throws ClassNotFoundException, SQLException, IllegalArgumentException {
//	final int STRING_BUFFER_SIZE = 100000;
//	Date now = new Date();
//	Long mili = now.getTime();
//	String auditFileName = String.format("%s/%s-%d.%s", this.tmpDir, "audit-records", mili, "xml");
//	File auditFile = new File(auditFileName);
//	StringBuffer stringBuffer = new StringBuffer(AUDIT_OPENING_TAG);
//
//    if (queryParams != null) {
//      Connection connection = null;
//
//      String selectString =
//        "SELECT oid, entrytime, service, category, servicemethod, entrytext," +
//        " resourceid, statuscode, userid, useragent, groups, authsystem " +
//        "FROM " + AUDIT_MANAGER_TABLE_QUALIFIED;
//
//      boolean orderBy = true;
//      selectString += composeWhereClause(queryParams, orderBy);
//      logger.info("WHERE clause: " + selectString);
//
//      Statement stmt = null;
//
//      try {
//        connection = getConnection();
//        stmt = connection.createStatement();
//        ResultSet rs = stmt.executeQuery(selectString);
//
//        while (rs.next()) {
//          int oid = rs.getInt(1);
//          java.sql.Timestamp sqlTimestamp = rs.getTimestamp(2);
//          String service = rs.getString(3);
//          String category = rs.getString(4);
//          String serviceMethod = rs.getString(5);
//          String entryText = rs.getString(6);
//          String resourceId = rs.getString(7);
//          int statusCode = rs.getInt(8);
//          String userId = rs.getString(9);
//          String userAgent = rs.getString(10);
//          String groups = rs.getString(11);
//          String authSystem = rs.getString(12);
//          AuditRecord auditRecord = new AuditRecord();
//          auditRecord.setOid(oid);
//          java.util.Date entryTime = new java.util.Date(sqlTimestamp.getTime());
//          auditRecord.setEntryTime(entryTime);
//          auditRecord.setService(service);
//          auditRecord.setCategory(category);
//          auditRecord.setServiceMethod(serviceMethod);
//          auditRecord.setEntryText(entryText);
//          auditRecord.setResourceId(resourceId);
//          auditRecord.setResponseStatus(new Integer(statusCode));
//          auditRecord.setUser(userId);
//          auditRecord.setUserAgent(userAgent);
//          auditRecord.setGroups(groups);
//          auditRecord.setAuthSystem(authSystem);
//          stringBuffer.append(auditRecord.toXML());
//          if (stringBuffer.toString().length() >= STRING_BUFFER_SIZE) {
//        	  saveXmlToFile(auditFile, stringBuffer);
//        	  stringBuffer = new StringBuffer("");
//          }
//        }
//      }
//      catch(ClassNotFoundException e) {
//        logger.error("ClassNotFoundException: " + e.getMessage());
//        throw(e);
//      }
//      catch(SQLException e) {
//        logger.error("SQLException: " + e.getMessage());
//        throw(e);
//      }
//      finally {
//        stringBuffer.append(AUDIT_CLOSING_TAG);
//  	    saveXmlToFile(auditFile, stringBuffer);
//        if (stmt != null) stmt.close();
//        returnConnection(connection);
//      }
//    }
//
//    return auditFile;
//  }


  /**
   * Gets a list of audit log records from the audit table (named "eventlog")
   * matching the provided criteria.
   *
   * @param queryParams a map of query parameters and the values they should be matched to
   * @return an XML string of audit records
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public MyPair<String, MyPair<Integer, Integer>> getAuditRecordsXml(
      Map<String, List<String>> queryParams)
      throws ClassNotFoundException, SQLException, IllegalArgumentException
  {

//    final int STRING_BUFFER_SIZE = 100000;
    Date now = new Date();
//    Long mili = now.getTime();

    StringBuffer stringBuffer = new StringBuffer(AUDIT_OPENING_TAG);

    int firstOid = 0;
    int lastOid = 0;

    if (queryParams != null) {
      Connection connection = null;

      String selectString =
          "SELECT oid, entrytime, service, category, servicemethod, entrytext," +
              " resourceid, statuscode, userid, useragent, groups, authsystem " +
              "FROM " + AUDIT_MANAGER_TABLE_QUALIFIED;

      selectString += composeWhereClause(queryParams, true);
      logger.info("WHERE clause: " + selectString);

      Statement stmt = null;

      try {
        connection = getConnection();
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(selectString);

        while (rs.next()) {
          int oid = rs.getInt(1);

          if (firstOid == 0 || firstOid > oid) {
            firstOid = oid;
          }
          if (lastOid == 0 || lastOid < oid) {
            lastOid = oid;
          }

          java.sql.Timestamp sqlTimestamp = rs.getTimestamp(2);
          String service = rs.getString(3);
          String category = rs.getString(4);
          String serviceMethod = rs.getString(5);
          String entryText = rs.getString(6);
          String resourceId = rs.getString(7);
          int statusCode = rs.getInt(8);
          String userId = rs.getString(9);
          String userAgent = rs.getString(10);
          String groups = rs.getString(11);
          String authSystem = rs.getString(12);
          AuditRecord auditRecord = new AuditRecord();
          auditRecord.setOid(oid);
          java.util.Date entryTime = new java.util.Date(sqlTimestamp.getTime());
          auditRecord.setEntryTime(entryTime);
          auditRecord.setService(service);
          auditRecord.setCategory(category);
          auditRecord.setServiceMethod(serviceMethod);
          auditRecord.setEntryText(entryText);
          auditRecord.setResourceId(resourceId);
          auditRecord.setResponseStatus(new Integer(statusCode));
          auditRecord.setUser(userId);
          auditRecord.setUserAgent(userAgent);
          auditRecord.setGroups(groups);
          auditRecord.setAuthSystem(authSystem);

          stringBuffer.append(auditRecord.toXML());
        }
      }
      catch(ClassNotFoundException e) {
        logger.error("ClassNotFoundException: " + e.getMessage());
        throw(e);
      }
      catch(SQLException e) {
        logger.error("SQLException: " + e.getMessage());
        throw(e);
      }
      finally {
        stringBuffer.append(AUDIT_CLOSING_TAG);
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }
    }

    return new MyPair<>(stringBuffer.toString(), new MyPair<>(firstOid, lastOid));
  }



  /**
   *
   */
  public void fixArchiveRecords()
           throws ClassNotFoundException, SQLException, IllegalArgumentException {
      Connection connection = null;
      String selectString =
    		  String.format(
                     "SELECT oid, servicemethod, resourceid from %s " +
    		         "WHERE servicemethod='readDataPackage' OR " +
                     "      servicemethod='createDataPackageArchive' OR " +
    		         "      servicemethod='readDataPackageArchive' " +
                     "ORDER BY oid",
                     AUDIT_MANAGER_TABLE_QUALIFIED);

      Statement stmt = null;

      try {
        connection = getConnection();
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(selectString);
        String dataPackageResourceId = "";
        int dataPackageOid = 0;

        while (rs.next()) {
          int oid = rs.getInt(1);
          String serviceMethod = rs.getString(2);
          String resourceId = rs.getString(3);

          if (serviceMethod.equals("readDataPackage")) {
        	  dataPackageOid = oid;
        	  dataPackageResourceId = resourceId;
          }
          else if (serviceMethod.endsWith("Archive")) {
        	  if (!resourceId.startsWith("https:")) {
        	    String archiveResourceId = dataPackageResourceId.replace("/package/", "/package/archive/");
        	    System.out.println(String.format("Data package oid: %d; Archive record oid: %d", dataPackageOid, oid));
        	    System.out.println(String.format("Replacing %s with %s", resourceId, archiveResourceId));
                String updateString =
            		  String.format("UPDATE %s SET resourceid='%s' WHERE oid=%d",
                                    AUDIT_MANAGER_TABLE_QUALIFIED, archiveResourceId, oid);
                System.out.println(updateString);

                Connection updateConnection = getConnection();
                Statement updateStmt = updateConnection.createStatement();
                int nRecords = updateStmt.executeUpdate(updateString);
                if (nRecords != 1) throw new SQLException(String.format("%d records updated; expected 1.", nRecords));
                if (updateStmt != null) updateStmt.close();
                returnConnection(updateConnection);

        	  }
          }
        }
      }
      catch(ClassNotFoundException e) {
        logger.error("ClassNotFoundException: " + e.getMessage());
        throw(e);
      }
      catch(SQLException e) {
        logger.error("SQLException: " + e.getMessage());
        throw(e);
      }
      finally {
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }

  }


  /**
   * Returns a connection to the database.
   *
   * @return  conn  the database Connection object
   */
  public Connection getConnection()
           throws ClassNotFoundException {
    Connection conn = null;
    SQLWarning warn;

    // Load the jdbc driver
    try {
      Class.forName(dbDriver);
    }
    catch (ClassNotFoundException e) {
      logger.error("Can't load driver " + e.getMessage());
      throw(e);
    }

    // Make the database connection
    try {
      conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);

      // If a SQLWarning object is available, print its warning(s).
      // There may be multiple warnings chained.
      warn = conn.getWarnings();

      if (warn != null) {
        while (warn != null) {
          logger.warn("SQLState: " + warn.getSQLState());
          logger.warn("Message:  " + warn.getMessage());
          logger.warn("Vendor: " + warn.getErrorCode());
          warn = warn.getNextWarning();
        }
      }
    }
    catch (SQLException e) {
      logger.error("Database access failed " + e);
    }

    return conn;
  }


  private String getFieldName(String key) {
    String fieldName = key.toLowerCase();

    if (key.equalsIgnoreCase("user")) {
      fieldName = "userid";
    }
    else if (key.equalsIgnoreCase("responseStatus") ||
             key.equalsIgnoreCase("status")
            ) {
      fieldName = "statuscode";
    }

    return fieldName;
  }


  /**
   * Boolean to determine whether the specified audit record is in the
   * audit database based on a specified audit id value
   *
   * @param auditEntryId   the audit entry identifier
   * @return  true if the audit record is present, else false
   */
  public boolean hasAuditEntry(String auditEntryId)
          throws ClassNotFoundException, SQLException {
    boolean hasAuditRecord = false;
    Connection connection = null;
    String selectString =
      "SELECT count(*) FROM " + AUDIT_MANAGER_TABLE_QUALIFIED +
      "  WHERE oid='" + auditEntryId + "'";

    Statement stmt = null;

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
        int count = rs.getInt("count");
        hasAuditRecord = (count > 0);
      }

      if (stmt != null) stmt.close();
    }
    catch(ClassNotFoundException e) {
      logger.error("ClassNotFoundException: " + e.getMessage());
      throw(e);
    }
    catch(SQLException e) {
      logger.error("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      returnConnection(connection);
    }

    return hasAuditRecord;
  }


  /**
   * Boolean to determine whether the audit table already exists. 
   * If it isn't present, it will need to be created.
   *
   * @return  isPresent, true if the audit table is present, else false
   */
  private boolean isAuditTablePresent()
            throws ClassNotFoundException, SQLException {
    boolean isPresent = false;
    String catalog = null;          // A catalog name (may be null)
    Connection connection = null;
    DatabaseMetaData databaseMetaData = null; // For getting db metadata
    ResultSet rs;
    String schemaPattern = AUDIT_MANAGER_SCHEMA; // A schema name pattern
    String tableNamePattern = "%";  // Matches all table names in the db
    String[] types = {"TABLE"};     // A list of table types to include

    try {
      connection = getConnection();

      if (connection != null) {
        databaseMetaData = connection.getMetaData();
        rs = databaseMetaData.getTables(catalog, schemaPattern,
                                   tableNamePattern, types);
        while (rs.next()) {
          String TABLE_NAME = rs.getString("TABLE_NAME");

          if (TABLE_NAME.equalsIgnoreCase(AUDIT_MANAGER_TABLE)) {
            isPresent = true;
          }
        }

        if (rs != null) rs.close();
      }
      else {
        SQLException e = new SQLException("Unable to connect to database.");
        throw(e);
      }
    }
    catch (ClassNotFoundException e) {
      throw(e);
    }
    catch (SQLException e) {
      throw(e);
    }
    finally {
      returnConnection(connection);
    }

    return isPresent;
  }


  /**
   * Closes the connection to the database.
   */
  public void returnConnection(Connection conn) {
    try {
      // Close the database connection
      logger.debug("Closing the database connection");

      if (conn != null) {
        conn.close();
      }
      else {
        logger.warn("Unable to close connection, 'conn' object is null");
      }
    }
    // We won't worry about throwing an exception if the connection return
    // fails. Just issue a log error.
    catch (SQLException e) {
      logger.error("Failed to close connection. Database access failed " +
                   e.getMessage());
    }
  }

}