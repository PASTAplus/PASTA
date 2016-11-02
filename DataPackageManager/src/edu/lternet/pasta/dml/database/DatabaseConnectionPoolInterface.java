/**
 *    '$RCSfile: DatabaseConnectionPoolInterface.java,v $'
 *
 *     '$Author: costa $'
 *       '$Date: 2006-12-01 22:02:06 $'
 *   '$Revision: 1.4 $'
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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This interface provides an API for how the application class should manage
 * its database connection pool. Any application which will use this library
 * should implement this interface.
 * 
 * @author tao
 *
 */
public interface DatabaseConnectionPoolInterface 
{
   /*
    * Instance methods
    */
  
   /**
    * Gets a database connection from the pool.
    * 
	* @return checked out Connection object
    * @throws SQLException
    */
   public Connection getConnection() 
           throws SQLException, ConnectionNotAvailableException;
  
   
   /**
    * Returns checked out dabase connection to the pool.
    * 
    * @param conn the Connection that needs to be returned
    * @return true if the connection was returned successfully, else false
    */
   public boolean returnConnection(Connection conn);
   
   
   /**
    * Get the database adpater name that this connection pool uses for its
    * connections.
    * 
    * @return database adapter name
    */
   public String getDBAdapterName();
   
}
