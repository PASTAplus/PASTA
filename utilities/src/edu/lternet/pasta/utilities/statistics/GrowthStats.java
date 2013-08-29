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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * User: servilla
 * Date: 8/20/13
 * Time: 1:53 PM
 * <p/>
 * Project: Utilities
 * Package: edu.lternet.pasta.utilities.statistics
 * <p/>
 * <class description>
 */
public class GrowthStats {

 /* Constructors */

  public GrowthStats(String dbUrl, String dbUser, String dbPassword) {

    this.dbUrl = dbUrl;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;

    try {
      this.dbm = new DatabaseManager(this.dbUrl, this.dbUser, this.dbPassword);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }

  }

 /* Instance methods */

  private HashMap<String, Integer> buildPackageHashMap(int year) {

    HashMap<String, Integer> map = new HashMap<String, Integer>();

    for (Integer week = 1; week <= YEARWEEKS; week++) {
      for (String pkg: dataPackagesForWeek(year, week)) {
        if (!map.containsKey(pkg)) {
          map.put(pkg, week);
        }
      }
    }

    return map;

  }

  private int[] buildPackageStats(HashMap<String, Integer> packageHashMap) {

    int[] packageStats = new int[52];

    // Initialize each cell to 0
    for (int i = 0; i < YEARWEEKS; i++) {
      packageStats[i] = 0;
    }

    for (Map.Entry<String, Integer> entry: packageHashMap.entrySet()) {
      packageStats[entry.getValue() - 1]++;
    }

    return packageStats;

  }

  private ArrayList<String> dataPackagesForWeek(int year, int week) {

    String startDate = this.weekStartToDate(year, week);
    String endDate = this.weekEndToDate(year, week);

    String sql = "SELECT distinct scope, identifier FROM "
        + RESOURCE_REGISTRY + " WHERE date_created >= " + startDate + " AND "
        + " date_created <= " + endDate + " AND scope LIKE 'knb-lter-%'";

    ResultSet rs = null;

    try {
      rs = this.dbm.doQuery(sql);
    }
    catch (SQLException e) {
      System.err.printf("%s\n", e.getMessage());
      e.printStackTrace();
    }

    ArrayList<String> dataPackages = new ArrayList<String>();

    try {
      for (String[] pkg: dbm.resultSetAsString(rs)) {
        dataPackages.add(pkg[0] + "." + pkg[1]);
      }

    }
    catch (SQLException e) {
      System.err.printf("%s\n", e.getMessage());
      e.printStackTrace();
    }

    return dataPackages;

  }

  private String weekEndToDate(int year, int week) {

    int dayOfYear = ((week - 1) * DAYSINWEEK) + ENDDAY;

    Calendar now = Calendar.getInstance();
    now.set(Calendar.YEAR, year);
    now.set(Calendar.DAY_OF_YEAR, dayOfYear);

    String yearStr = Integer.toString(now.get(Calendar.YEAR));
    String monthStr = Integer.toString(now.get(Calendar.MONTH) + 1);
    String dayStr = Integer.toString(now.get(Calendar.DAY_OF_MONTH));

    String date = "'" + yearStr + "-" + monthStr + "-" + dayStr + "'";
    return date;

  }

  private String weekStartToDate(int year, int week) {

    int dayOfYear = ((week - 1) * DAYSINWEEK) + STARTDAY;

    Calendar now = Calendar.getInstance();
    now.set(Calendar.YEAR, year);
    now.set(Calendar.DAY_OF_YEAR, dayOfYear);

    String yearStr = Integer.toString(now.get(Calendar.YEAR));
    String monthStr = Integer.toString(now.get(Calendar.MONTH) + 1);
    String dayStr = Integer.toString(now.get(Calendar.DAY_OF_MONTH));

    String date = "'" + yearStr + "-" + monthStr + "-" + dayStr + "'";
    return date;

  }

 /* Class methods */

  public static void main(String[] args) {

    String dbUrl = args[0];
    String dbUser = args[1];
    String dbPassword = args[2];

    GrowthStats gs = new GrowthStats(dbUrl, dbUser, dbPassword);

    HashMap<String, Integer> map = gs.buildPackageHashMap(2013);
    int[] packageStats = gs.buildPackageStats(map);

    int count = 0;
    System.out.printf("WEEK - COUNT - TOTAL%n");
    for (int i = 0; i < YEARWEEKS; i++) {
      count += packageStats[i];
      System.out.printf(" %d,%d,%d%n", i + 1, packageStats[i], count);
    }
  }
 
 /* Instance variables */

  DatabaseManager dbm = null;

  private String dbUrl = null;
  private String dbUser = null;
  private String dbPassword = null;

 /* Class variables */

  private static String RESOURCE_REGISTRY = "datapackagemanager.resource_registry";
  private static int STARTDAY = 1;
  private static int ENDDAY = 7;
  private static int DAYSINWEEK = 7;
  private static int YEARWEEKS = 52;


}
