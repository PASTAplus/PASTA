/**
 *    '$RCSfile: EcogridEndPointInterfaceTest.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2008-02-15 01:48:47 $'
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
package edu.lternet.pasta.dml.download;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * This class implemnts EcogridEndPointInterface for testing. It does not
 * contain a test suite of its own, but is used by other test suites.
 * 
 * @author tao
 *
 */
public class EcogridEndPointInterfaceTest extends TestCase 
                                          implements EcogridEndPointInterface 
{  
	//private static String URL_PREFIX = "http://localhost:8080/knb/services";
	private static String URL_PREFIX = "https://knb.ecoinformatics.org/knb/services";
	 /**
	    * Gets the end point which Metacat implements ecogrid interface.
	    * This end point will be used to handle ecogrid protocol
        * 
	    * @return end point url string
	    */
	   public String getMetacatEcogridEndPoint()
	   {
		   return URL_PREFIX + "/QueryService";
	   }
	   
	   public String getMetacatEcogridAuthEndPoint()
	   {
		   return URL_PREFIX + "/AuthenticationService";
	   }
	   
	   public String getMetacatEcogridPutEndPoint()
	   {
		   return URL_PREFIX + "/PutService";
	   }
	   
	   public String getMetacatEcogridIdentifierEndPoint() {
			return URL_PREFIX + "/IdentifierService";
		}
	   
       
	   /**
	    * Gets the end point which SRB implements ecogrid interface.
	    * This end point will be used to handle srb protocol.
        * 
	    * @return end point url string
	    */
	   public String getSRBEcogridEndPoint()
	   {
		   return 
              "http://srbbrick8.sdsc.edu:8080/SRBImpl/services/SRBQueryService";
	   }

       
	   /**
	    * Gets the machine name which srb protocol will be used.
	    * The default value for this class is "srb-mcat.sdsc.edu".
        * 
	    * @return the machine name of srb server
	    */
	   public String getSRBMachineName()
	   {
		   return "srb-mcat.sdsc.edu";
	   }
    
       
	  /**
	    * This is not a test class, so it is empty here.
	    */
	   public static Test suite()
	   {
	     TestSuite suite = new TestSuite();
	     return suite;
	   }

	
       
}

