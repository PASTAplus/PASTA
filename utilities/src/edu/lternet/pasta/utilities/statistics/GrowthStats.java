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
import java.text.SimpleDateFormat;
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

 /* Class variables */

  private static String RESOURCE_REGISTRY = "datapackagemanager.resource_registry";

  // Create new calendar for PASTA origin at 2013-01-01 00:00:00
  private static final GregorianCalendar origin = new GregorianCalendar(2013, 0, 1, 0, 0, 0);

 /* Constructors */

  public GrowthStats(String dbUrl, String dbUser, String dbPassword, String scale) {

    try {
      dbm = new DatabaseManager(dbUrl, dbUser, dbPassword);
    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

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

  private HashMap<String, Long> buildHashMap(DatabaseManager dbm,
                                             String sql) {

    HashMap<String, Long> map = new HashMap<String, Long>();

    ResultSet rs = null;

    try {
      rs = dbm.doQuery(sql);
    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

    try {

      while (rs.next()) {
        String key = rs.getString(1);
        Long date_created = rs.getTimestamp(2).getTime();
        if (!map.containsKey(key)) {
          map.put(key, date_created);
        }
      }
    }
    catch (SQLException e) {
      System.err.printf("%s%n", e.getMessage());
      e.printStackTrace();
    }

    return map;

  }

  private Long[] buildSortedList(HashMap<String, Long> map) {

    Long[] list = new Long[map.size()];

    int i = 0;
    for (Map.Entry<String, Long> entry: map.entrySet()) {
      list[i++] = entry.getValue();
    }

    Arrays.sort(list);

    return list;

  }

  private ArrayList<String> buildLabels(GregorianCalendar start,
                                        GregorianCalendar end,
                                        int scale) {

    ArrayList<String> labels = new ArrayList<String>();

    GregorianCalendar lower = (GregorianCalendar) start.clone();
    GregorianCalendar upper = new GregorianCalendar();
    GregorianCalendar split = new GregorianCalendar();

    while (lower.getTimeInMillis() <= end.getTimeInMillis()) {
      upper.setTime(lower.getTime());
      upper.add(scale, 1);
      split.setTime(new Date(lower.getTimeInMillis() +
                                 (upper.getTimeInMillis() - lower.getTimeInMillis()) / 2));
      /*
      System.out.printf("%s-%s-%s%n", lower.getTime().toString(),
                           split.getTime().toString(),
                           upper.getTime().toString());
       */
      labels.add(getLabel(scale, split));
      lower.setTime(upper.getTime());
    }

    return labels;

  }

  private String getLabel(int scale, GregorianCalendar date) {

    String label = null;

    SimpleDateFormat formatter;

    switch (scale) {
      case Calendar.HOUR:
        formatter = new SimpleDateFormat("kk00 yyyy-MMM-d");
        label = formatter.format(date.getTime());
        break;
      case Calendar.DAY_OF_MONTH:
        formatter = new SimpleDateFormat("yyyy-MMM-d");
        label = formatter.format(date.getTime());
        break;
      case Calendar.WEEK_OF_YEAR:
        formatter = new SimpleDateFormat("w yyyy");
        label = formatter.format(date.getTime());
        break;
      case Calendar.MONTH:
        formatter = new SimpleDateFormat("MMM yyyy");
        label = formatter.format(date.getTime());
        break;
      case Calendar.YEAR:
        formatter = new SimpleDateFormat("yyyy");
        label = formatter.format(date.getTime());
        break;
      default:
        label = date.getTime().toString();
    }

    return label;
  }

  private ArrayList<Integer> buildFrequencies(GregorianCalendar start,
                                              GregorianCalendar end,
                                              int scale, Long[] list) {

    ArrayList<Integer> freqs = new ArrayList<Integer>();

    GregorianCalendar lower = (GregorianCalendar) start.clone();
    GregorianCalendar upper = new GregorianCalendar();

    while (lower.getTimeInMillis() <= end.getTimeInMillis()) {
      upper.setTime(lower.getTime());
      upper.add(scale, 1);

      int freq = 0;

      for (int i = 0; i < list.length; i++) {
        if (lower.getTimeInMillis() <= list[i] &&
            list[i] < upper.getTimeInMillis()) {
          freq++;
          //System.out.printf("%d - %d - %d - %d%n", lower.getTimeInMillis(), list[i], upper.getTimeInMillis(), freq);
        }
      }
      freqs.add(freq);
      lower.setTime(upper.getTime());

    }

    return freqs;

  }

 /* Class methods */

  public static void main(String[] args) {

    String dbUrl = args[0];
    String dbUser = args[1];
    String dbPassword = args[2];
    String scale = args[3];

    GregorianCalendar now = new GregorianCalendar();

    GrowthStats gs = new GrowthStats(dbUrl, dbUser, dbPassword, scale);

    StringBuilder pkgSql = new StringBuilder();
    pkgSql.append("SELECT scope || '.' || identifier,date_created FROM ");
    pkgSql.append(RESOURCE_REGISTRY);
    pkgSql.append(" WHERE resource_type='dataPackage' AND ");
    pkgSql.append("date_deactivated IS NULL AND ");
    pkgSql.append("scope LIKE 'knb-lter-%' AND NOT scope='knb-lter-nwk' ");
    pkgSql.append("ORDER BY date_created ASC;");

    HashMap<String, Long> pkgMap = gs.buildHashMap(gs.getDbm(),
                                                      pkgSql.toString());
    Long[] pkgList = gs.buildSortedList(pkgMap);

    System.out.printf("Packages: %d%n", pkgList.length);

    StringBuilder siteSql = new StringBuilder();
    siteSql.append("SELECT scope,date_created FROM ");
    siteSql.append(RESOURCE_REGISTRY);
    siteSql.append(" WHERE resource_type='dataPackage' AND ");
    siteSql.append("date_deactivated IS NULL AND ");
    siteSql.append("scope LIKE 'knb-lter-%' AND NOT scope='knb-lter-nwk' ");
    siteSql.append("ORDER BY date_created ASC;");

    HashMap<String, Long> siteMap = gs.buildHashMap(gs.getDbm(),
                                                       siteSql.toString());
    Long[] siteList = gs.buildSortedList(siteMap);

    System.out.printf("Sites: %d%n", siteList.length);

    ArrayList<String> labels = gs.buildLabels(origin, now, Calendar.MONTH);

    System.out.printf("Label size: %d%n", labels.size());

    ArrayList<Integer> pkgFreq = gs.buildFrequencies(origin, now, Calendar.MONTH, pkgList);

    System.out.printf("Pkg freq size: %d%n", pkgFreq.size());

    ArrayList<Integer> siteFreq = gs.buildFrequencies(origin, now, Calendar.MONTH, siteList);

    Integer pkgCDist = 0;
    Integer siteCDist = 0;
    int i;

    for (i = 0; i < labels.size() - 1; i++) {
      pkgCDist += pkgFreq.get(i);
      siteCDist += siteFreq.get(i);
      System.out.printf("['%s',%d,%d],%n", labels.get(i), pkgCDist, siteCDist);
    }

    i = labels.size() - 1;
    pkgCDist += pkgFreq.get(i);
    siteCDist += siteFreq.get(i);
    System.out.printf("['%s',%d,%d]%n", labels.get(i), pkgCDist, siteCDist);

  }

}
