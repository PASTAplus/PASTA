/*
 * Copyright 2011-2013 the University of New Mexico.
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
 */

package edu.lternet.pasta.utilities.statistics;

import java.sql.*;
import java.util.ArrayList;

/**
 * User: servilla
 * Date: 8/15/13
 * Time: 10:08 AM
 * <p/>
 * Project: Utilities
 * Package: edu.lternet.pasta.utilities.statistics
 * <p/>
 * Manage connections to PASTA databases
 */
public class DatabaseManager {

 /* Constructors */

  public DatabaseManager(String dbUrl, String dbUser, String dbPassword)
  throws SQLException {

    this.dbUrl = dbUrl;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;

    this.conn = getConnection(this.dbUrl, this.dbUser, this.dbPassword);

  }

 /* Instance methods */

  private Connection getConnection(String dbUrl, String dbUser, String dbPassword)
      throws SQLException {

    Connection conn = null;
    SQLWarning warn;

    conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

    // If a SQLWarning object is available, print its warning(s).
    // There may be multiple warnings chained.
    warn = conn.getWarnings();

    if (warn != null) {
      while (warn != null) {
        System.err.println("SQLState: " + warn.getSQLState());
        System.err.println("Message:  " + warn.getMessage());
        System.err.println("Vendor: " + warn.getErrorCode());
        warn = warn.getNextWarning();
      }
    }

    return conn;
  }

  public ResultSet doQuery(String select) throws SQLException {

    Statement stmnt = this.conn.createStatement();
    ResultSet rs = stmnt.executeQuery(select);

    return rs;
  }

  public ArrayList<String[]> resultSetAsString(ResultSet rs)
      throws SQLException {

    ArrayList<String[]> result = new ArrayList<String[]>();

    ResultSetMetaData rsmd = rs.getMetaData();
    int colCount = rsmd.getColumnCount();
    String[] tuple = null;

    // Skip header row of column names
    tuple = new String[colCount];
    for (int i = 0; i < colCount; i++) {
      tuple[i] = rsmd.getColumnName(i + 1);
    }

    // Iterate and add each tuple
    while (rs.next()) {
      tuple = new String[colCount];
      for (int i = 0; i < colCount; i++) {
        tuple[i] = rs.getString(i + 1);
      }
      result.add(tuple);
    }

    return result;
  }


 /* Class methods */

  public static void main(String[] args) {

    System.out.println("Made it this far...");

    Connection conn = null;

    String dbUrl = args[0];
    String dbUser = args[1];
    String dbPassword = args[2];
    String sql = "select * from datapackagemanager.resource_registry";

    try {
      DatabaseManager dbm = new DatabaseManager(dbUrl, dbUser, dbPassword);
      ResultSet rs = dbm.doQuery(sql);
      ArrayList<String[]> result = dbm.resultSetAsString(rs);

      for (String[] tuple: result) {
        int i = tuple.length;
        for (int j = 0; j < i; j++) {
          System.out.printf("%s ", tuple[j]);
        }
        System.out.printf("\n");
      }

    }
    catch (SQLException e) {
      System.err.printf("PASTA Database error - %s\n", e.getMessage());
      e.printStackTrace();
    }


  }
 
 /* Instance variables */

  private String dbUrl = null;
  private String dbUser = null;
  private String dbPassword = null;

  private Connection conn = null;

 /* Class variables */

}
