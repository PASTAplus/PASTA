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
import java.sql.Timestamp;
import java.util.*;

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


 /* Instance variables */

  private DatabaseManager dbm = null;

  private Long millis;
  private Long seconds;
  private Long minutes;
  private Long hours;
  private Long days;
  private Long weeks;
  private Long months;
  private Long years;

  private final GregorianCalendar now;

 /* Class variables */

  private static String RESOURCE_REGISTRY = "datapackagemanager.resource_registry";
  private static int STARTDAY = 1;
  private static String STARTHOUR = "00:00:00";
  private static int ENDDAY = 7;
  private static String ENDHOUR = "23:59:59";
  private static int DAYSINWEEK = 7;
  private static int WEEKSINYEAR = 52;

  private static final Long milliPerSec = 1000L;
  private static final Long secPerMin = 60L;
  private static final Long minPerHour = 60L;
  private static final Long hourPerDay = 24L;


  // Create new calendar for PASTA origin at 2013-01-01 00:00:00
  private static final GregorianCalendar origin = new GregorianCalendar(2013, 1, 1, 0, 0, 0);

 /* Constructors */

  public GrowthStats(String dbUrl, String dbUser, String dbPassword, String scale) {

    try {
      dbm = new DatabaseManager(dbUrl, dbUser, dbPassword);
    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

    now = new GregorianCalendar();

  }

 /* Instance methods */

  /**
   * Returns the Database Manager object.
   *
   * @return Database manager object
   */
  public DatabaseManager getDbm() {
    return dbm;
  }

  private HashMap<String, Long> buildPackageHashMap(DatabaseManager dbm) {

    HashMap<String, Long> map = new HashMap<String, Long>();

    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("SELECT scope,identifier,date_created FROM ");
    strBuilder.append(RESOURCE_REGISTRY);
    strBuilder.append(" WHERE resource_type='dataPackage' AND ");
    strBuilder.append("date_deactivated IS NULL AND ");
    strBuilder.append("scope LIKE 'knb-lter-%' AND NOT scope='knb-lter-nwk' ");
    strBuilder.append("ORDER BY date_created ASC;");

    String sql = strBuilder.toString();

    ResultSet rs = null;

    try {
      rs = dbm.doQuery(sql);
    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

    String pkg;
    Long date_created;

    try {
      while(rs.next()) {

        pkg = rs.getString("scope") + "." + rs.getString("identifier");
        date_created = rs.getDate("date_created").getTime();

        if (!map.containsKey(pkg)) {
          map.put(pkg,date_created);
        }

      }
    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

    return map;

  }

  private Long[] buildList(HashMap<String, Long> map) {

    Long[] list = new Long[map.size()];

    int i = 0;
    for (Map.Entry<String, Long> entry: map.entrySet()) {
      list[i++] = entry.getValue();
    }

    return list;

  }

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

    String startDate = this.weekStartToDate(year, week);
    String endDate = this.weekEndToDate(year, week);

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

    String startDate = this.weekStartToDate(year, week);
    String endDate = this.weekEndToDate(year, week);

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

    String date = "'" + yearStr + "-" + monthStr + "-" + dayStr + " 23:59:59'";
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

    String date = "'" + yearStr + "-" + monthStr + "-" + dayStr + " 00:00:00'";
    return date;

  }

 /* Class methods */

  public static void main(String[] args) {

    String dbUrl = args[0];
    String dbUser = args[1];
    String dbPassword = args[2];
    String scale = args[3];
    Integer upToWeek;

    Calendar now = Calendar.getInstance();
    upToWeek = now.get(Calendar.WEEK_OF_YEAR);

    GrowthStats gs = new GrowthStats(dbUrl, dbUser, dbPassword, scale);

    HashMap<String, Long> map = gs.buildPackageHashMap(gs.getDbm());
    Long[] pkgList = gs.buildList(map);

    System.out.printf("List length: %d", pkgList.length);

  }

}
