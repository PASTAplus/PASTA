package edu.lternet.pasta.auditmanager;

import org.apache.log4j.Logger;
import org.postgresql.core.Utils;

import java.sql.SQLException;

/*
Make user provided values safe for use in dynamically generated SQL queries.
*/
public class SqlEscape {
  private static final Logger log = Logger.getLogger(
      edu.lternet.pasta.auditmanager.SqlEscape.class);

  /*
  Sanitize and quote string literal.
  */
  public static String str(String s) throws SQLException
  {
    if (s != null) {
      // escapeLiteral() does not add quotes. while escapeIdentifier does add quotes.
      return "'" + Utils.escapeLiteral(null, s, true).toString() + "'";
    }
    log.error("Received null str");
    return null;
  }

  /*
  Sanitize string literal that holds an integer.

  The returned string does not include quotes.
  */
  public static String integer(String s) throws SQLException
  {
    if (s != null) {
      return Integer.toString(Integer.parseInt(s));
    }
    log.error("Received null int");
    return null;
  }

  /*
  Convert integer to string.

  This is included for consistency.
  */
  public static String integer(Integer s) throws SQLException
  {
    if (s != null) {
      return Integer.toString(s);
    }
    log.error("Received null int");
    return null;
  }

  /*
  Escape and quote names that are part of the database schema, such as table and column
  names.
  */
  public static String name(String s) throws SQLException
  {
    // TODO: Before we apply escaping here, we must split up schema and table name strings.
    return s;
    // if (s != null) {
    //   // escapeLiteral() does not add quotes. while escapeIdentifier does add quotes.
    //   return Utils.escapeIdentifier(null, s).toString().toLowerCase();
    // }
    // log.error("Received null name");
    // return null;
  }
}
