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

    try {
      this.dbm = new DatabaseManager(dbUrl, dbUser, dbPassword);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }


  }

  /* Instance methods */

  private ArrayList<String> getPackageIds() {

    String sql = "SELECT distinct(package_id) from "
      + "datapackagemanager.resource_registry";

    ArrayList<String[]> result = null;

    try {
      ResultSet rs = dbm.doQuery(sql);
      result = dbm.resultSetAsString(rs);
    }
    catch (SQLException e) {
      e.printStackTrace();
    }

    ArrayList<String> packageIds = null;
    for (String[] tuple: result) {
      packageIds.add(tuple[0]);
    }

    return packageIds;

  }

  /* Class methods */

  public static void main(String[] args) {

    String dbUrl = args[0];
    String dbUser = args[1];
    String dbPassword = args[2];

    FullPublic fp = new FullPublic(dbUrl, dbUser, dbPassword);
    ArrayList<String> packageIds = fp.getPackageIds();

    for (String packageId: packageIds) {
      System.out.printf("%s\n", packageId);
    }

  }

  /*  Instance variables */

  DatabaseManager dbm = null;

  private String dbUrl = null;
  private String dbUser = null;
  private String dbPassword = null;

  // Name of the database table where data packages are registered
  private final String ACCESS_MATRIX_TABLE = "ACCESS_MATRIX";
  private final String ACCESS_MATRIX = "datapackagemanager.ACCESS_MATRIX";
  private final String DATA_PACKAGE_MANAGER_SCHEMA = "datapackagemanager";
  private final String RESOURCE_REGISTRY = "datapackagemanager.RESOURCE_REGISTRY";
  private final String RESOURCE_REGISTRY_TABLE = "RESOURCE_REGISTRY";

  /* Class variables */

}
