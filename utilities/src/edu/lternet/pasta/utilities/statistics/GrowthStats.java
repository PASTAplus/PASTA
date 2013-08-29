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

  private HashMap<String, Integer> buildPackageHashMapForYear(int year) {

    HashMap<String, Integer> map = new HashMap<String, Integer>();

    for (Integer week = 1; week <= WEEKSINYEAR; week++) {
      for (String pkg: dataPackagesForWeek(year, week)) {
        if (!map.containsKey(pkg)) {
          map.put(pkg, week);
        }
      }
    }

    return map;

  }

  private HashMap<String, Integer> buildSiteHashMapForYear(int year) {

    HashMap<String, Integer> map = new HashMap<String, Integer>();

    for (Integer week = 1; week <= WEEKSINYEAR; week++) {
      for (String site: sitesForWeek(year, week)) {
        if (!map.containsKey(site)) {
          map.put(site, week);
        }
      }
    }

    return map;

  }

  private int[] buildPackageStats(HashMap<String, Integer> packageHashMap) {

    int[] packageStats = new int[WEEKSINYEAR];

    // Initialize each cell to 0
    for (int i = 0; i < WEEKSINYEAR; i++) {
      packageStats[i] = 0;
    }

    for (Map.Entry<String, Integer> entry: packageHashMap.entrySet()) {
      packageStats[entry.getValue() - 1]++;
    }

    return packageStats;

  }

  private ArrayList<String> dataPackagesForWeek(int year, int week) {

    String startDate = "'" + this.weekStartToDate(year, week) + " " + STARTHOUR + "'";
    String endDate = "'" + this.weekEndToDate(year, week) + " " + ENDHOUR + "'";

    String sql = "SELECT distinct scope, identifier FROM "
        + RESOURCE_REGISTRY + " WHERE date_created >= " + startDate + " AND "
        + " date_created <= " + endDate + " AND scope LIKE 'knb-lter-%' "
        + "AND date_deactivated IS NULL AND NOT scope='knb-lter-nwk'";

    ResultSet rs = null;

    try {
      rs = this.dbm.doQuery(sql);
    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

    ArrayList<String> dataPackages = new ArrayList<String>();

    try {
      for (String[] pkg: dbm.resultSetAsString(rs)) {
        dataPackages.add(pkg[0] + "." + pkg[1]);
      }

    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

    return dataPackages;

  }

  private ArrayList<String> sitesForWeek(int year, int week) {

    String startDate = "'" + this.weekStartToDate(year, week) + " " + STARTHOUR + "'";
    String endDate = "'" + this.weekEndToDate(year, week) + " " + ENDHOUR + "'";

    String sql = "SELECT distinct scope FROM "
                     + RESOURCE_REGISTRY + " WHERE date_created >= " + startDate + " AND "
                     + " date_created <= " + endDate + " AND scope LIKE 'knb-lter-%' "
                     + " AND date_deactivated IS NULL AND NOT scope='knb-lter-nwk'";

    ResultSet rs = null;

    try {
      rs = this.dbm.doQuery(sql);
    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

    ArrayList<String> sites = new ArrayList<String>();

    try {
      for (String[] site: dbm.resultSetAsString(rs)) {
        sites.add(site[0]);
      }
    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

    return sites;

  }

  private String weekEndToDate(int year, int week) {

    int dayOfYear = ((week - 1) * DAYSINWEEK) + ENDDAY;

    Calendar now = Calendar.getInstance();
    now.set(Calendar.YEAR, year);
    now.set(Calendar.DAY_OF_YEAR, dayOfYear);

    String yearStr = Integer.toString(now.get(Calendar.YEAR));
    String monthStr = Integer.toString(now.get(Calendar.MONTH) + 1);
    String dayStr = Integer.toString(now.get(Calendar.DAY_OF_MONTH));

    String date = yearStr + "-" + monthStr + "-" + dayStr;
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

    String date = yearStr + "-" + monthStr + "-" + dayStr;
    return date;

  }

 /* Class methods */

  public static void main(String[] args) {

    String dbUrl = args[0];
    String dbUser = args[1];
    String dbPassword = args[2];

    GrowthStats gs = new GrowthStats(dbUrl, dbUser, dbPassword);

    HashMap<String, Integer> packageMap = gs.buildPackageHashMapForYear(2013);
    int[] packageStats = gs.buildPackageStats(packageMap);

    HashMap<String, Integer> siteMap = gs.buildSiteHashMapForYear(2013);
    int[] siteStats = gs.buildPackageStats(siteMap);

    int pkgCount = 0;
    int siteCount = 0;
    for (int week = 0; week < WEEKSINYEAR; week++) {
      pkgCount += packageStats[week];
      siteCount += siteStats[week];
      System.out.printf("[%2d, %6d, %2d],%n", week + 1, pkgCount, siteCount);
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
  private static String STARTHOUR = "00:00:00";
  private static int ENDDAY = 7;
  private static String ENDHOUR = "23:59:59";
  private static int DAYSINWEEK = 7;
  private static int WEEKSINYEAR = 52;


}
