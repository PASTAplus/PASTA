/**
 *    '$RCSfile: PostgresAdapter.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2008-03-01 00:31:48 $'
 *   '$Revision: 1.13 $'
 *
 *  For Details: http://kepler.ecoinformatics.org
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package edu.lternet.pasta.dml.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.lternet.pasta.dml.parser.Attribute;
import edu.lternet.pasta.dml.parser.AttributeList;
import edu.lternet.pasta.dml.parser.DateTimeDomain;
import edu.lternet.pasta.dml.parser.Domain;
import edu.lternet.pasta.dml.parser.EnumeratedDomain;
import edu.lternet.pasta.dml.parser.NumericDomain;
import edu.lternet.pasta.dml.parser.TextDomain;

public class PostgresAdapter extends DatabaseAdapter {
  
  /*
   * Class Fields
   */
  public static final String AND    = "AND";
  public static final String BOOLEAN = "Boolean";
  public static final String CREATETABLE = "CREATE TABLE";
  public static final String DATETIME = "Timestamp";
  public static final String DELETE = "DELETE";
  public static final String DOUBLE = "Double";
  public static final String DROPTABLE = "DROP TABLE";
  public static final String FLOAT = "Float";
  public static final String FROM = "FROM";
  
  public static final String INTEGER = "Integer";
  //public static final String IFEXISTS = Config.getValue(IFEXISTSPATH);
  public static final String LIKE = "LIKE";
  public static final String LONG = "Long";
  public static final String QUESTION = "?";
  public static final String QUOTE = "\"";
  //public static final String FIELDSEPATATOR = Config.getValue(FIELDSPEPATH);
  public static final String SELECT = "SELECT";
  
  //public static final String SETTABLE = Config.getValue(SETTABLEPATH);
  //public static final String SOURCE = Config.getValue(SOURCEPATH);
  //public static final String IGNOREFIRST = Config.getValue(IGNOREFIRSTPATH);
  public static final String STRING = "String";
  
  public static final String WHERE = "WHERE";
  
  
  private static final String[][] datetimeTransformationTable =
	  { 
	    {"YYYY-MM-DD", "YYYY-MM-DD"},
	    {"YYYY-MM-DD hh:mm:ss.sss", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DDThh:mm:ss.sss", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DD hh:mm:ss.sssZ", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DDThh:mm:ss.sssZ", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DD hh:mm:ss.sss+hh:mm", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DDThh:mm:ss.sss+hh:mm", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DD hh:mm:ss.sss+hhmm", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DDThh:mm:ss.sss+hhmm", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DD hh:mm:ss.sss+hh", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DDThh:mm:ss.sss+hh", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DD hh:mm:ss.sss-hh:mm", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DDThh:mm:ss.sss-hh:mm", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DD hh:mm:ss.sss-hhmm", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DDThh:mm:ss.sss-hhmm", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DD hh:mm:ss.sss-hh", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DDThh:mm:ss.sss-hh", "YYYY-MM-DD HH24:MI:SS.US"},
	    {"YYYY-MM-DD hhmmss.sss", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DDThhmmss.sss", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DD hhmmss.sssZ", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DDThhmmss.sssZ", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DD hhmmss.sss+hh:mm", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DDThhmmss.sss+hh:mm", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DD hhmmss.sss+hhmm", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DDThhmmss.sss+hhmm", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DD hhmmss.sss+hh", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DDThhmmss.sss+hh", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DD hhmmss.sss-hh:mm", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DDThhmmss.sss-hh:mm", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DD hhmmss.sss-hhmm", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DDThhmmss.sss-hhmm", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DD hhmmss.sss-hh", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DDThhmmss.sss-hh", "YYYY-MM-DD HH24MISS.US"},
	    {"YYYY-MM-DD hh:mm:ss", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DDThh:mm:ss", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DD hh:mm:ssZ", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DDThh:mm:ssZ", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DD hh:mm:ss+hh:mm", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DDThh:mm:ss+hh:mm", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DD hh:mm:ss+hhmm", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DDThh:mm:ss+hhmm", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DD hh:mm:ss+hh", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DDThh:mm:ss+hh", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DD hh:mm:ss-hh:mm", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DDThh:mm:ss-hh:mm", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DD hh:mm:ss-hhmm", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DDThh:mm:ss-hhmm", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DD hh:mm:ss-hh", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DDThh:mm:ss-hh", "YYYY-MM-DD HH24:MI:SS"},
	    {"YYYY-MM-DD hhmmss", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DDThhmmss", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DD hhmmssZ", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DDThhmmssZ", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DD hhmmss+hh:mm", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DDThhmmss+hh:mm", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DD hhmmss+hhmm", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DDThhmmss+hhmm", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DD hhmmss+hh", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DDThhmmss+hh", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DD hhmmss-hh:mm", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DDThhmmss-hh:mm", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DD hhmmss-hhmm", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DDThhmmss-hhmm", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DD hhmmss-hh", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DDThhmmss-hh", "YYYY-MM-DD HH24MISS"},
	    {"YYYY-MM-DD hh:mm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DDThh:mm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DD hh:mmZ", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DDThh:mmZ", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DD hh:mm+hh:mm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DDThh:mm+hh:mm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DD hh:mm+hhmm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DDThh:mm+hhmm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DD hh:mm+hh", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DDThh:mm+hh", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DD hh:mm-hh:mm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DDThh:mm-hh:mm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DD hh:mm-hhmm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DDThh:mm-hhmm", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DD hh:mm-hh", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DDThh:mm-hh", "YYYY-MM-DD HH24:MI"},
	    {"YYYY-MM-DD hhmm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DDThhmm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DD hhmmZ", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DDThhmmZ", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DD hhmm+hh:mm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DDThhmm+hh:mm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DD hhmm+hhmm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DDThhmm+hhmm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DD hhmm+hh", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DDThhmm+hh", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DD hhmm-hh:mm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DDThhmm-hh:mm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DD hhmm-hhmm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DDThhmm-hhmm", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DD hhmm-hh", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DDThhmm-hh", "YYYY-MM-DD HH24MI"},
	    {"YYYY-MM-DD hh", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DDThh", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DD hhZ", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DDThhZ", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DD hh+hh:mm", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DDThh+hh:mm", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DD hh+hhmm", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DDThh+hhmm", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DD hh+hh", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DDThh+hh", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DD hh-hh:mm", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DDThh-hh:mm", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DD hh-hhmm", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DDThh-hhmm", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DD hh-hh", "YYYY-MM-DD HH24"},
	    {"YYYY-MM-DDThh-hh", "YYYY-MM-DD HH24"},
	    {"YYYY", "YYYY"},
	    {"YYYYMMDD", "YYYYMMDD"},
	    {"YYYYMMDD hh:mm:ss.sss", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDDThh:mm:ss.sss", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDD hh:mm:ss.sssZ", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDDThh:mm:ss.sssZ", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDD hh:mm:ss.sss+hh:mm", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDDThh:mm:ss.sss+hh:mm", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDD hh:mm:ss.sss+hhmm", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDDThh:mm:ss.sss+hhmm", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDD hh:mm:ss.sss+hh", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDDThh:mm:ss.sss+hh", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDD hh:mm:ss.sss-hh:mm", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDDThh:mm:ss.sss-hh:mm", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDD hh:mm:ss.sss-hhmm", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDDThh:mm:ss.sss-hhmm", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDD hh:mm:ss.sss-hh", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDDThh:mm:ss.sss-hh", "YYYYMMDD HH24:MI:SS.US"},
	    {"YYYYMMDD hhmmss.sss", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDDThhmmss.sss", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDD hhmmss.sssZ", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDDThhmmss.sssZ", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDD hhmmss.sss+hh:mm", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDDThhmmss.sss+hh:mm", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDD hhmmss.sss+hhmm", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDDThhmmss.sss+hhmm", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDD hhmmss.sss+hh", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDDThhmmss.sss+hh", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDD hhmmss.sss-hh:mm", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDDThhmmss.sss-hh:mm", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDD hhmmss.sss-hhmm", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDDThhmmss.sss-hhmm", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDD hhmmss.sss-hh", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDDThhmmss.sss-hh", "YYYYMMDD HH24MISS.US"},
	    {"YYYYMMDD hh:mm:ss", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDDThh:mm:ss", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDD hh:mm:ssZ", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDDThh:mm:ssZ", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDD hh:mm:ss+hh:mm", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDDThh:mm:ss+hh:mm", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDD hh:mm:ss+hhmm", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDDThh:mm:ss+hhmm", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDD hh:mm:ss+hh", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDDThh:mm:ss+hh", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDD hh:mm:ss-hh:mm", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDDThh:mm:ss-hh:mm", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDD hh:mm:ss-hhmm", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDDThh:mm:ss-hhmm", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDD hh:mm:ss-hh", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDDThh:mm:ss-hh", "YYYYMMDD HH24:MI:SS"},
	    {"YYYYMMDD hhmmss", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDDThhmmss", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDD hhmmssZ", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDDThhmmssZ", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDD hhmmss+hh:mm", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDDThhmmss+hh:mm", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDD hhmmss+hhmm", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDDThhmmss+hhmm", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDD hhmmss+hh", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDDThhmmss+hh", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDD hhmmss-hh:mm", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDDThhmmss-hh:mm", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDD hhmmss-hhmm", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDDThhmmss-hhmm", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDD hhmmss-hh", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDDThhmmss-hh", "YYYYMMDD HH24MISS"},
	    {"YYYYMMDD hh:mm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDDThh:mm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDD hh:mmZ", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDDThh:mmZ", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDD hh:mm+hh:mm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDDThh:mm+hh:mm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDD hh:mm+hhmm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDDThh:mm+hhmm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDD hh:mm+hh", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDDThh:mm+hh", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDD hh:mm-hh:mm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDDThh:mm-hh:mm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDD hh:mm-hhmm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDDThh:mm-hhmm", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDD hh:mm-hh", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDDThh:mm-hh", "YYYYMMDD HH24:MI"},
	    {"YYYYMMDD hhmm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDDThhmm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDD hhmmZ", "YYYYMMDD HH24MI"},
	    {"YYYYMMDDThhmmZ", "YYYYMMDD HH24MI"},
	    {"YYYYMMDD hhmm+hh:mm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDDThhmm+hh:mm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDD hhmm+hhmm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDDThhmm+hhmm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDD hhmm+hh", "YYYYMMDD HH24MI"},
	    {"YYYYMMDDThhmm+hh", "YYYYMMDD HH24MI"},
	    {"YYYYMMDD hhmm-hh:mm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDDThhmm-hh:mm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDD hhmm-hhmm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDDThhmm-hhmm", "YYYYMMDD HH24MI"},
	    {"YYYYMMDD hhmm-hh", "YYYYMMDD HH24MI"},
	    {"YYYYMMDDThhmm-hh", "YYYYMMDD HH24MI"},
	    {"YYYYMMDD hh", "YYYYMMDD HH24"},
	    {"YYYYMMDDThh", "YYYYMMDD HH24"},
	    {"YYYYMMDD hhZ", "YYYYMMDD HH24"},
	    {"YYYYMMDDThhZ", "YYYYMMDD HH24"},
	    {"YYYYMMDD hh+hh:mm", "YYYYMMDD HH24"},
	    {"YYYYMMDDThh+hh:mm", "YYYYMMDD HH24"},
	    {"YYYYMMDD hh+hhmm", "YYYYMMDD HH24"},
	    {"YYYYMMDDThh+hhmm", "YYYYMMDD HH24"},
	    {"YYYYMMDD hh+hh", "YYYYMMDD HH24"},
	    {"YYYYMMDDThh+hh", "YYYYMMDD HH24"},
	    {"YYYYMMDD hh-hh:mm", "YYYYMMDD HH24"},
	    {"YYYYMMDDThh-hh:mm", "YYYYMMDD HH24"},
	    {"YYYYMMDD hh-hhmm", "YYYYMMDD HH24"},
	    {"YYYYMMDDThh-hhmm", "YYYYMMDD HH24"},
	    {"YYYYMMDD hh-hh", "YYYYMMDD HH24"},
	    {"YYYYMMDDThh-hh", "YYYYMMDD HH24"},
	    {"YYYY-MM", "YYYY-MM"},
	    {"YYYYMM", "YYYYMM"},
	    {"YYYY-DDD", "YYYY-DDD"},
	    {"YYYY-DDD hh:mm:ss.sss", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDDThh:mm:ss.sss", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDD hh:mm:ss.sssZ", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDDThh:mm:ss.sssZ", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDD hh:mm:ss.sss+hh:mm", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDDThh:mm:ss.sss+hh:mm", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDD hh:mm:ss.sss+hhmm", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDDThh:mm:ss.sss+hhmm", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDD hh:mm:ss.sss+hh", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDDThh:mm:ss.sss+hh", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDD hh:mm:ss.sss-hh:mm", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDDThh:mm:ss.sss-hh:mm", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDD hh:mm:ss.sss-hhmm", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDDThh:mm:ss.sss-hhmm", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDD hh:mm:ss.sss-hh", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDDThh:mm:ss.sss-hh", "YYYY-DDD HH24:MI:SS.US"},
	    {"YYYY-DDD hhmmss.sss", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDDThhmmss.sss", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDD hhmmss.sssZ", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDDThhmmss.sssZ", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDD hhmmss.sss+hh:mm", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDDThhmmss.sss+hh:mm", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDD hhmmss.sss+hhmm", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDDThhmmss.sss+hhmm", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDD hhmmss.sss+hh", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDDThhmmss.sss+hh", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDD hhmmss.sss-hh:mm", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDDThhmmss.sss-hh:mm", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDD hhmmss.sss-hhmm", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDDThhmmss.sss-hhmm", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDD hhmmss.sss-hh", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDDThhmmss.sss-hh", "YYYY-DDD HH24MISS.US"},
	    {"YYYY-DDD hh:mm:ss", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDDThh:mm:ss", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDD hh:mm:ssZ", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDDThh:mm:ssZ", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDD hh:mm:ss+hh:mm", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDDThh:mm:ss+hh:mm", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDD hh:mm:ss+hhmm", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDDThh:mm:ss+hhmm", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDD hh:mm:ss+hh", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDDThh:mm:ss+hh", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDD hh:mm:ss-hh:mm", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDDThh:mm:ss-hh:mm", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDD hh:mm:ss-hhmm", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDDThh:mm:ss-hhmm", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDD hh:mm:ss-hh", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDDThh:mm:ss-hh", "YYYY-DDD HH24:MI:SS"},
	    {"YYYY-DDD hhmmss", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDDThhmmss", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDD hhmmssZ", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDDThhmmssZ", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDD hhmmss+hh:mm", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDDThhmmss+hh:mm", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDD hhmmss+hhmm", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDDThhmmss+hhmm", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDD hhmmss+hh", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDDThhmmss+hh", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDD hhmmss-hh:mm", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDDThhmmss-hh:mm", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDD hhmmss-hhmm", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDDThhmmss-hhmm", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDD hhmmss-hh", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDDThhmmss-hh", "YYYY-DDD HH24MISS"},
	    {"YYYY-DDD hh:mm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDDThh:mm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDD hh:mmZ", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDDThh:mmZ", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDD hh:mm+hh:mm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDDThh:mm+hh:mm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDD hh:mm+hhmm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDDThh:mm+hhmm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDD hh:mm+hh", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDDThh:mm+hh", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDD hh:mm-hh:mm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDDThh:mm-hh:mm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDD hh:mm-hhmm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDDThh:mm-hhmm", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDD hh:mm-hh", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDDThh:mm-hh", "YYYY-DDD HH24:MI"},
	    {"YYYY-DDD hhmm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDDThhmm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDD hhmmZ", "YYYY-DDD HH24MI"},
	    {"YYYY-DDDThhmmZ", "YYYY-DDD HH24MI"},
	    {"YYYY-DDD hhmm+hh:mm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDDThhmm+hh:mm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDD hhmm+hhmm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDDThhmm+hhmm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDD hhmm+hh", "YYYY-DDD HH24MI"},
	    {"YYYY-DDDThhmm+hh", "YYYY-DDD HH24MI"},
	    {"YYYY-DDD hhmm-hh:mm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDDThhmm-hh:mm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDD hhmm-hhmm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDDThhmm-hhmm", "YYYY-DDD HH24MI"},
	    {"YYYY-DDD hhmm-hh", "YYYY-DDD HH24MI"},
	    {"YYYY-DDDThhmm-hh", "YYYY-DDD HH24MI"},
	    {"YYYY-DDD hh", "YYYY-DDD HH24"},
	    {"YYYY-DDDThh", "YYYY-DDD HH24"},
	    {"YYYY-DDD hhZ", "YYYY-DDD HH24"},
	    {"YYYY-DDDThhZ", "YYYY-DDD HH24"},
	    {"YYYY-DDD hh+hh:mm", "YYYY-DDD HH24"},
	    {"YYYY-DDDThh+hh:mm", "YYYY-DDD HH24"},
	    {"YYYY-DDD hh+hhmm", "YYYY-DDD HH24"},
	    {"YYYY-DDDThh+hhmm", "YYYY-DDD HH24"},
	    {"YYYY-DDD hh+hh", "YYYY-DDD HH24"},
	    {"YYYY-DDDThh+hh", "YYYY-DDD HH24"},
	    {"YYYY-DDD hh-hh:mm", "YYYY-DDD HH24"},
	    {"YYYY-DDDThh-hh:mm", "YYYY-DDD HH24"},
	    {"YYYY-DDD hh-hhmm", "YYYY-DDD HH24"},
	    {"YYYY-DDDThh-hhmm", "YYYY-DDD HH24"},
	    {"YYYY-DDD hh-hh", "YYYY-DDD HH24"},
	    {"YYYY-DDDThh-hh", "YYYY-DDD HH24"},
	    {"YYYYDDD", "YYYYDDD"},
	    {"YYYYDDD hh:mm:ss.sss", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDDThh:mm:ss.sss", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDD hh:mm:ss.sssZ", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDDThh:mm:ss.sssZ", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDD hh:mm:ss.sss+hh:mm", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDDThh:mm:ss.sss+hh:mm", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDD hh:mm:ss.sss+hhmm", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDDThh:mm:ss.sss+hhmm", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDD hh:mm:ss.sss+hh", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDDThh:mm:ss.sss+hh", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDD hh:mm:ss.sss-hh:mm", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDDThh:mm:ss.sss-hh:mm", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDD hh:mm:ss.sss-hhmm", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDDThh:mm:ss.sss-hhmm", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDD hh:mm:ss.sss-hh", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDDThh:mm:ss.sss-hh", "YYYYDDD HH24:MI:SS.US"},
	    {"YYYYDDD hhmmss.sss", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDDThhmmss.sss", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDD hhmmss.sssZ", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDDThhmmss.sssZ", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDD hhmmss.sss+hh:mm", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDDThhmmss.sss+hh:mm", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDD hhmmss.sss+hhmm", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDDThhmmss.sss+hhmm", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDD hhmmss.sss+hh", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDDThhmmss.sss+hh", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDD hhmmss.sss-hh:mm", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDDThhmmss.sss-hh:mm", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDD hhmmss.sss-hhmm", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDDThhmmss.sss-hhmm", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDD hhmmss.sss-hh", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDDThhmmss.sss-hh", "YYYYDDD HH24MISS.US"},
	    {"YYYYDDD hh:mm:ss", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDDThh:mm:ss", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDD hh:mm:ssZ", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDDThh:mm:ssZ", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDD hh:mm:ss+hh:mm", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDDThh:mm:ss+hh:mm", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDD hh:mm:ss+hhmm", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDDThh:mm:ss+hhmm", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDD hh:mm:ss+hh", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDDThh:mm:ss+hh", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDD hh:mm:ss-hh:mm", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDDThh:mm:ss-hh:mm", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDD hh:mm:ss-hhmm", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDDThh:mm:ss-hhmm", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDD hh:mm:ss-hh", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDDThh:mm:ss-hh", "YYYYDDD HH24:MI:SS"},
	    {"YYYYDDD hhmmss", "YYYYDDD HH24MISS"},
	    {"YYYYDDDThhmmss", "YYYYDDD HH24MISS"},
	    {"YYYYDDD hhmmssZ", "YYYYDDD HH24MISS"},
	    {"YYYYDDDThhmmssZ", "YYYYDDD HH24MISS"},
	    {"YYYYDDD hhmmss+hh:mm", "YYYYDDD HH24MISS"},
	    {"YYYYDDDThhmmss+hh:mm", "YYYYDDD HH24MISS"},
	    {"YYYYDDD hhmmss+hhmm", "YYYYDDD HH24MISS"},
	    {"YYYYDDDThhmmss+hhmm", "YYYYDDD HH24MISS"},
	    {"YYYYDDD hhmmss+hh", "YYYYDDD HH24MISS"},
	    {"YYYYDDDThhmmss+hh", "YYYYDDD HH24MISS"},
	    {"YYYYDDD hhmmss-hh:mm", "YYYYDDD HH24MISS"},
	    {"YYYYDDDThhmmss-hh:mm", "YYYYDDD HH24MISS"},
	    {"YYYYDDD hhmmss-hhmm", "YYYYDDD HH24MISS"},
	    {"YYYYDDDThhmmss-hhmm", "YYYYDDD HH24MISS"},
	    {"YYYYDDD hhmmss-hh", "YYYYDDD HH24MISS"},
	    {"YYYYDDDThhmmss-hh", "YYYYDDD HH24MISS"},
	    {"YYYYDDD hh:mm", "YYYYDDD HH24:MI"},
	    {"YYYYDDDThh:mm", "YYYYDDD HH24:MI"},
	    {"YYYYDDD hh:mmZ", "YYYYDDD HH24:MI"},
	    {"YYYYDDDThh:mmZ", "YYYYDDD HH24:MI"},
	    {"YYYYDDD hh:mm+hh:mm", "YYYYDDD HH24:MI"},
	    {"YYYYDDDThh:mm+hh:mm", "YYYYDDD HH24:MI"},
	    {"YYYYDDD hh:mm+hhmm", "YYYYDDD HH24:MI"},
	    {"YYYYDDDThh:mm+hhmm", "YYYYDDD HH24:MI"},
	    {"YYYYDDD hh:mm+hh", "YYYYDDD HH24:MI"},
	    {"YYYYDDDThh:mm+hh", "YYYYDDD HH24:MI"},
	    {"YYYYDDD hh:mm-hh:mm", "YYYYDDD HH24:MI"},
	    {"YYYYDDDThh:mm-hh:mm", "YYYYDDD HH24:MI"},
	    {"YYYYDDD hh:mm-hhmm", "YYYYDDD HH24:MI"},
	    {"YYYYDDDThh:mm-hhmm", "YYYYDDD HH24:MI"},
	    {"YYYYDDD hh:mm-hh", "YYYYDDD HH24:MI"},
	    {"YYYYDDDThh:mm-hh", "YYYYDDD HH24:MI"},
	    {"YYYYDDD hhmm", "YYYYDDD HH24MI"},
	    {"YYYYDDDThhmm", "YYYYDDD HH24MI"},
	    {"YYYYDDD hhmmZ", "YYYYDDD HH24MI"},
	    {"YYYYDDDThhmmZ", "YYYYDDD HH24MI"},
	    {"YYYYDDD hhmm+hh:mm", "YYYYDDD HH24MI"},
	    {"YYYYDDDThhmm+hh:mm", "YYYYDDD HH24MI"},
	    {"YYYYDDD hhmm+hhmm", "YYYYDDD HH24MI"},
	    {"YYYYDDDThhmm+hhmm", "YYYYDDD HH24MI"},
	    {"YYYYDDD hhmm+hh", "YYYYDDD HH24MI"},
	    {"YYYYDDDThhmm+hh", "YYYYDDD HH24MI"},
	    {"YYYYDDD hhmm-hh:mm", "YYYYDDD HH24MI"},
	    {"YYYYDDDThhmm-hh:mm", "YYYYDDD HH24MI"},
	    {"YYYYDDD hhmm-hhmm", "YYYYDDD HH24MI"},
	    {"YYYYDDDThhmm-hhmm", "YYYYDDD HH24MI"},
	    {"YYYYDDD hhmm-hh", "YYYYDDD HH24MI"},
	    {"YYYYDDDThhmm-hh", "YYYYDDD HH24MI"},
	    {"YYYYDDD hh", "YYYYDDD HH24"},
	    {"YYYYDDDThh", "YYYYDDD HH24"},
	    {"YYYYDDD hhZ", "YYYYDDD HH24"},
	    {"YYYYDDDThhZ", "YYYYDDD HH24"},
	    {"YYYYDDD hh+hh:mm", "YYYYDDD HH24"},
	    {"YYYYDDDThh+hh:mm", "YYYYDDD HH24"},
	    {"YYYYDDD hh+hhmm", "YYYYDDD HH24"},
	    {"YYYYDDDThh+hhmm", "YYYYDDD HH24"},
	    {"YYYYDDD hh+hh", "YYYYDDD HH24"},
	    {"YYYYDDDThh+hh", "YYYYDDD HH24"},
	    {"YYYYDDD hh-hh:mm", "YYYYDDD HH24"},
	    {"YYYYDDDThh-hh:mm", "YYYYDDD HH24"},
	    {"YYYYDDD hh-hhmm", "YYYYDDD HH24"},
	    {"YYYYDDDThh-hhmm", "YYYYDDD HH24"},
	    {"YYYYDDD hh-hh", "YYYYDDD HH24"},
	    {"YYYYDDDThh-hh", "YYYYDDD HH24"},
	    {"hh+hh", "HH24"},
	    {"hh+hh:mm", "HH24"},
	    {"hh+hhmm", "HH24"},
	    {"hh", "HH24"},
	    {"hh-hh", "HH24"},
	    {"hh-hh:mm", "HH24"},
	    {"hh-hhmm", "HH24"},
	    {"hh:mm+hh", "HH24:MI"},
	    {"hh:mm+hh:mm", "HH24:MI"},
	    {"hh:mm+hhmm", "HH24:MI"},
	    {"hh:mm", "HH24:MI"},
	    {"hh:mm-hh", "HH24:MI"},
	    {"hh:mm-hh:mm", "HH24:MI"},
	    {"hh:mm-hhmm", "HH24:MI"},
	    {"hh:mm:ss+hh", "HH24:MI:SS"},
	    {"hh:mm:ss+hh:mm", "HH24:MI:SS"},
	    {"hh:mm:ss+hhmm", "HH24:MI:SS"},
	    {"hh:mm:ss", "HH24:MI:SS"},
	    {"hh:mm:ss-hh", "HH24:MI:SS"},
	    {"hh:mm:ss-hh:mm", "HH24:MI:SS"},
	    {"hh:mm:ss-hhmm", "HH24:MI:SS"},
	    {"hh:mm:ss.sss+hh", "HH24:MI:SS.US"},
	    {"hh:mm:ss.sss+hh:mm", "HH24:MI:SS.US"},
	    {"hh:mm:ss.sss+hhmm", "HH24:MI:SS.US"},
	    {"hh:mm:ss.sss", "HH24:MI:SS.US"},
	    {"hh:mm:ss.sss-hh", "HH24:MI:SS.US"},
	    {"hh:mm:ss.sss-hh:mm", "HH24:MI:SS.US"},
	    {"hh:mm:ss.sss-hhmm", "HH24:MI:SS.US"},
	    {"hh:mm:ss.sssZ", "HH24:MI:SS.US"},
	    {"hh:mm:ssZ", "HH24:MI:SS"},
	    {"hh:mmZ", "HH24:MI"},
	    {"hhZ", "HH24"},
	    {"hhmm+hh", "HH24MI"},
	    {"hhmm+hh:mm", "HH24MI"},
	    {"hhmm+hhmm", "HH24MI"},
	    {"hhmm", "HH24MI"},
	    {"hhmm-hh", "HH24MI"},
	    {"hhmm-hh:mm", "HH24MI"},
	    {"hhmm-hhmm", "HH24MI"},
	    {"hhmmZ", "HH24MI"},
	    {"hhmmss+hh", "HH24MISS"},
	    {"hhmmss+hh:mm", "HH24MISS"},
	    {"hhmmss+hhmm", "HH24MISS"},
	    {"hhmmss", "HH24MISS"},
	    {"hhmmss-hh", "HH24MISS"},
	    {"hhmmss-hh:mm", "HH24MISS"},
	    {"hhmmss-hhmm", "HH24MISS"},
	    {"hhmmss.sss+hh", "HH24MISS.US"},
	    {"hhmmss.sss+hh:mm", "HH24MISS.US"},
	    {"hhmmss.sss+hhmm", "HH24MISS.US"},
	    {"hhmmss.sss", "HH24MISS.US"},
	    {"hhmmss.sss-hh", "HH24MISS.US"},
	    {"hhmmss.sss-hh:mm", "HH24MISS.US"},
	    {"hhmmss.sss-hhmm", "HH24MISS.US"},
	    {"hhmmss.sssZ", "HH24MISS.US"},
	    {"hhmmssZ", "HH24MISS"},
        {"YYYY-WWW-DD", "YYYY-Mon-DD"},
        {"YYYY/WWW/DD", "YYYY/Mon/DD"},
        {"DD WWW YYYY", "DD Mon YYYY"},
        {"YYYYWWWDD", "YYYYMonDD"},
        {"MM/DD/YYYY hh:mm", "MM/DD/YYYY HH24:MI"},
	  };
 
  
  /*
   * Instance Fields
   */
  
  
  /*
   * Constructors
   */
  public PostgresAdapter() {
	  super();
	  //override the superclass version
	  this.TO_DATE_FUNCTION = "to_timestamp";
  }
  
  
  /*
   * Class Methods
   */
  
  
  /*
   * Instance Methods
   */

  /**
   * Create a SQL command to generate a table
   * 
   * @param  attributeList   List of attributes that determine the table columns
   * @param  tableName       The table name.
   * @return A string containing the DDL needed to create the table with
   *         its columns
   */
  public String generateDDL(AttributeList attributeList, String tableName)
          throws SQLException {
   String attributeSQL;
   StringBuffer stringBuffer = new StringBuffer();
   //String textFileName   = table.getFileName();
   //int    headLineNumber = table.getNumHeaderLines();
   //String orientation    = table.getOrientation();
   //String delimiter      = table.getDelimiter();
   
   stringBuffer.append(CREATETABLE);
   stringBuffer.append(SPACE);
   stringBuffer.append(tableName);
   stringBuffer.append(LEFTPARENTH);
   attributeSQL = parseAttributeList(attributeList);
   stringBuffer.append(attributeSQL);
   stringBuffer.append(RIGHTPARENTH);
   stringBuffer.append(SEMICOLON);
   String sqlStr = stringBuffer.toString();

   //if (isDebugging) { 
   //  log.debug("The command to create tables is "+sqlStr);
   // }
   return sqlStr;
  }

  
  /**
   * Create a drop table SQL command.
   * 
   * @param tableName  Name of the table to be dropped
   * @return   A SQL string that can be used to drop the table.
   */
  public String generateDropTableSQL(String tableName)
  {
    String sqlString = DROPTABLE + SPACE + tableName + SPACE + SEMICOLON;
    
    return sqlString;
  }
  
  
  /**
   * Gets attribute type for a given attribute. Attribute types include:
   *   "datetime"
   *   "string"
   * or, for numeric attributes, one of the allowed EML NumberType values:
   *   "natural"
   *   "whole"
   *   "integer"
   *   "real"
   * 
   * @param  attribute   The Attribute object whose type is being determined.
   * @return a string value representing the attribute type
   */
  protected String getAttributeType(Attribute attribute) {
    String attributeType = null;
    
    // Check whether attributeType has already been stored for this attribute
    attributeType = attribute.getAttributeType();
    if (attributeType != null) {
      // If the attribute already knows its attributeType, return it now
      return attributeType;
    }
    
    String className = this.getClass().getName();
    attributeType = getAttributeTypeFromStorageType(attribute, className);
    if (attributeType != null) {
      // If the attributeType can be derived from the storageType(s),
      // store it in the attribute object and return it now
      attribute.setAttributeType(attributeType);
      return attributeType;
    }
    
    // Derive the attributeType from the domain type
    attributeType = "string";
    Domain domain = attribute.getDomain();

    if (domain instanceof DateTimeDomain) {
    	attributeType = "datetime";
    }
    else if (domain instanceof EnumeratedDomain
            || domain instanceof TextDomain) {
          attributeType = "string";
        } 
    else if (domain instanceof NumericDomain) {
      NumericDomain numericDomain = (NumericDomain) domain;
      attributeType = numericDomain.getNumberType();
    }

    // Store the attributeType in the attribute so that it doesn't
    // need to be recalculated with every row of data.
    if (attribute != null) {
      attribute.setAttributeType(attributeType);
    }
    
    return attributeType;
  }
		  
	 
  /**
   * Gets the postgresql database type based on a given attribute type.
   * 
   * @param  attributeType   a string indicating the attribute type
   * @return  a string indicating the corresponding data type in Postgres
   */
  protected String mapDataType(String attributeType) {
    String dbDataType;
    Map<String, String> map = new HashMap<String, String>();

    map.put("string", "TEXT");
    map.put("integer", "BIGINT");
    map.put("real", "FLOAT");
    map.put("whole", "BIGINT");
    map.put("natural", "BIGINT");
    map.put("datetime", "TIMESTAMP");

    dbDataType = map.get(attributeType.toLowerCase());

    return dbDataType;
  }

      
  /**
   * Transforms a datetime string value for compatibility
   * with a database timestamp field. 
   *
   *  @param   datetimeString  A date string as found in a data table
   *  @return  datetimeString  The transformed datetime string.
   */
  protected String transformDatetime(String datetimeString) {
    if (datetimeString != null) {
		Pattern pattern = Pattern.compile(".+\\d+T\\d+.+");
		Matcher matcher = pattern.matcher(datetimeString);
		if (matcher.matches()) {
    	  // Postgres 8.4 and higher doesn't like the "T" character so replace with space
    	  datetimeString = datetimeString.replace('T', ' ');
		}
    }
    
    return datetimeString;
  }
	  
	  
  /**
   * This method was contributed by M. Gastil-Buhl ("Gastil"),
   * Moorea Coral Reef LTER. (The implementation has been
   * slightly modified from the original code.)
   * 
   * Transforms an EML 'datetime' format string for use with a
   * Postgres 'TIMESTAMP' field. 
   * 
   * These are examples of valid EML:
   *  
   *                    Format string          Example value
   *                    -------------------    ------------------
   *     ISO Date       YYYY-MM-DD             2002-10-14
   *     ISO Datetime   YYYY-MM-DDThh:mm:ss    2002-10-14T09:13:45
   *     ISO Time       hh:mm:ss               17:13:45
   *     ISO Time       hh:mm:ss.sss           09:13:45.432
   *     ISO Time       hh:mm.mm               09:13.42
   *     Non-standard   DD/MM/YYYY             14/10/2002
   *     Non-standard   MM/DD/YYYY             10/14/2002
   *     Non-standard   MM/DD/YY               10/14/02
   *     Non-standard   YYYY-WWW-DD            2002-OCT-14
   *     Non-standard   YYYYWWWDD              2002OCT14
   *     Non-standard   YYYY-MM-DD hh:mm:ss    2002-10-14 09:13:45
   *     
   *  The transformation is needed because Postgres,
   *  as of Postgres 8.4, is much more picky about timestamp
   *  formats.
   *     
   *  @param   emlFormatString  EML format string for datetime
   *  @return  pgFormatString   Postgres format string for TIMESTAMP
   */
  protected String transformFormatString(String emlFormatString) {
    String pgFormatString = emlFormatString; //default
    
    for (int i = 0; i < datetimeTransformationTable.length; i++) {
    	String emlFormat = datetimeTransformationTable[i][0];
    	String pgFormat = datetimeTransformationTable[i][1];    	
        String emlFormatZulu = emlFormat + "Z";
    	if (emlFormatString.equalsIgnoreCase(emlFormat) || 
    	    emlFormatString.equalsIgnoreCase(emlFormatZulu)
    	   ) {
    		pgFormatString = pgFormat;
    		break;
    	}
    }
          
    return pgFormatString;
  }
  
  
  /**
   * Transform ANSI selection sql to a native db sql command.
   * Not yet implemented.
   * 
   * @param ANSISQL   ANSI SQL string.
   * @return          Native Postgres string.
   */
  public String transformSelectionSQL(String ANSISQL) {
    String sqlString = "";

    return sqlString;
  }
	
    
  /**
   * Get the sql command to count how many rows are in a given table.
   * 
   * @param  tableName  the given table name
   * @return the sql string which can count how many rows
   */
  public String getCountingRowNumberSQL(String tableName) {
    return String.format("SELECT COUNT(*) FROM %s", tableName);
  }
	
}
