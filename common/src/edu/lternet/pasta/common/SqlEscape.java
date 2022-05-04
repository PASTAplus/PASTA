package edu.lternet.pasta.common;

import org.postgresql.core.Utils;

import java.sql.SQLException;

/*
Make user provided values safe for use in dynamically generated SQL queries.
*/
public class SqlEscape {
  /*
  Sanitize and quote string literal.
  */
  public static String str(String s) throws SQLException
  {
    return Utils.escapeLiteral(null, s, true).toString();
  }

  /*
  Sanitize and quote string literal that holds an integer.

  The returned string does not include quotes.
  */
  public static String integer(String s) throws SQLException
  {
    return Integer.toString(Integer.parseInt(s));
  }

  /*
  Quote names that are part of the database schema, such as table and column names.
  */
  public static String name(String s) throws SQLException
  {
    return Utils.escapeIdentifier(null, s).toString();
  }
}
