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

/**
 * User: servilla
 * Date: 8/13/13
 * Time: 7:48 PM
 * <p/>
 * Project: Utilities
 * Package: edu.lternet.pasta.utilities.statistics
 * <p/>
 * Calculate statistics for data packages that are fully public - that is, all
 * resource objects for the data package are publicly accessible.
 */
public class FullPublic {

  /* Constructors */

  public FullPublic(String dbUrl, String dbUser, String dbPassword) {

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

  public int getFullPublicPackages() {

    int fullPublicPackages = 0;

    ArrayList<String> packageIds = getPackageIds();

    boolean ipr;
    boolean fail;

    for (String packageId: packageIds) {
      ArrayList<String> resources = getResources(packageId);
      fail = false;
      for (String resource: resources) {
        ipr = isPublicResource(resource);
        if(!ipr) {
          fail = true;
          break;
        }
      }
      if (!fail) fullPublicPackages++;
    }

    return fullPublicPackages;

  }

  private ArrayList<String> getPackageIds() {

    String sql = "SELECT distinct(package_id) FROM "
      + RESOURCE_REGISTRY;

    ArrayList<String[]> result = null;

    try {
      ResultSet rs = dbm.doQuery(sql);
      result = dbm.resultSetAsString(rs);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }

    ArrayList<String> packageIds = new ArrayList<String>();
    for (String[] tuple: result) {
      packageIds.add(tuple[0]);
    }

    return packageIds;

  }

  private ArrayList<String> getResources(String packageId) {

    String sql = "SELECT resource_id FROM " + RESOURCE_REGISTRY
        + " WHERE package_id='" + packageId + "'";

    ArrayList<String[]> result = null;
    ArrayList<String> resources = new ArrayList<String>();

    try {
      ResultSet rs = dbm.doQuery(sql);
      result = dbm.resultSetAsString(rs);
      for (String[] tuple: result) {
        resources.add(tuple[0]);
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }

    return resources;

  }

  private boolean isPublicResource(String resource) {

    boolean isPublicResource = false;

    String sql = "SELECT access_matrix_id FROM " + ACCESS_MATRIX
        + " WHERE resource_id='" + resource + "' AND "
        + " principal='public' AND access_type='allow' AND "
        + " permission='read'";

    ArrayList<String[]> result = null;

    try {
      ResultSet rs = dbm.doQuery(sql);
      result = dbm.resultSetAsString(rs);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }

    for (String[] id: result) {
      isPublicResource = true;
    }

    return isPublicResource;

  }

  /* Class methods */

  public static void main(String[] args) {

    String dbUrl = args[0];
    String dbUser = args[1];
    String dbPassword = args[2];

    FullPublic fp = new FullPublic(dbUrl, dbUser, dbPassword);
    ArrayList<String> packageIds = fp.getPackageIds();

    System.out.printf("Total data packages: %d%n", packageIds.size());
    System.out.printf("Public data packages: %d%n", fp.getFullPublicPackages());

  }

  /*  Instance variables */

  DatabaseManager dbm = null;

  private String dbUrl = null;
  private String dbUser = null;
  private String dbPassword = null;

  /* Class variables */

  private static String ACCESS_MATRIX = "datapackagemanager.access_matrix";
  private static String RESOURCE_REGISTRY = "datapackagemanager.resource_registry";

}
