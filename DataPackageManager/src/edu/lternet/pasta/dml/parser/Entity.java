/**
 *    '$RCSfile: Entity.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2008-06-18 00:22:13 $'
 *   '$Revision: 1.16 $'
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

package edu.lternet.pasta.dml.parser;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import edu.lternet.pasta.dml.database.DelimitedReader;
import edu.lternet.pasta.dml.download.DownloadHandler;
import edu.lternet.pasta.dml.download.EcogridEndPointInterface;
import edu.lternet.pasta.dml.download.GZipDataHandler;
import edu.lternet.pasta.dml.download.TarDataHandler;
import edu.lternet.pasta.dml.download.ZipDataHandler;
import edu.lternet.pasta.dml.quality.EntityReport;
import edu.lternet.pasta.dml.quality.QualityCheck;
import edu.lternet.pasta.dml.quality.QualityReport;
import edu.lternet.pasta.dml.quality.QualityCheck.Status;


/**
 * This object represents an Entity.  An Entity stores
 * information about a table of Attributes.
 * 
 * @author  tao
 */
public class Entity extends DataObjectDescription 
{
    /*
     * Class fields
     */
  
    /** static variable for ROWMAJOR tables **/
    public static String ROWMAJOR = "ROWMAJOR";
    
    /** static variable for COLUMNMAJOR tables **/
    public static String COLUMNMAJOR = "COLUMNMAJOR";
    
    public static String ZIP         = "zip";
    public static String TAR         = "application/x-tar";
    public static String GZIP        = "gzip";

    /**static variable for table type**/
    //public static String TABLEENTITY = "TABLEENTITY";
    
    public static String SPATIALRASTERENTITY = "SPATIALRASTERENTITY";
    public static String SPATIALVECTORENTITY = "SPATIALVECTORENTITY";
    public static String STOREDPROCEDUREENTITY = "STOREDPROCEDUREENTITY";
    public static String VIEWENTITY = "VIEWENTITY";
    public static String OTHERENTITY = "OTHERENTITY";
    
    public static HashMap<String, String> formatStringRegexes;
    
    static {
    	formatStringRegexes = new HashMap<String, String>();
    }
    
    
    /*
     * Instance fields
     */
    
    private AttributeList attributeList = new AttributeList();
    private Boolean      caseSensitive;
    private String       emlNamespace = null;
    private String       orientation;
    private int          numRecords      = 0;
    private Integer      numHeaderLines  = null;
    private Integer      numFooterLines  = null;
    private String       fieldDelimiter  = null;
    private String       recordDelimiter = null;
    
    /*
     * The record delimiter value as stored in the metadata.
     * This might be different from the value used for processing the entity.
     * It's useful to know the original metadata value for quality checking.
     */
    private String       metadataRecordDelimiter = null;
    
    private boolean      multiple        = false; // if true, multiple inputs 
                                                  // can be mapped to one table
    private String fileName;       // filename where Entity data is stored
    private String url;            // distribution URL for this entity
    private String urlFunction;    // value of the URL "function" attribute
    private String urlContentType; // value of URLConnection.getContentType();
    private String format;
    private String dbTableName;    // the unique table name will be stored in DB
    private String compressionMethod = null;
    private boolean externallyDefinedFormat = false;
    private String firstKilobyte = null;
    private boolean hasDistributionOnline = false;
    private boolean hasDistributionOffline = false;
    private boolean hasDistributionInline = false;
    private boolean hasNumberOfRecords = false;
    private boolean hasPhysicalAuthentication = false;
    private boolean isDataTableEntity = false;
    private boolean isImageEntity    = false;
    private boolean isOtherEntity    = false;
    private boolean hasGZipDataFile  = false;
    private boolean hasZipDataFile   = false;
    private boolean hasTarDataFile   = false;
    //private DataCacheObject dataCacheObject = null;
    private boolean simpleDelimited  = true;
    private boolean textFixed        = false;
    private TextComplexDataFormat[] dataFormatArray = null;
    private String physicalLineDelimiter  = null;
    private boolean collapseDelimiters = false;
    private String packageId = null;
    private String quoteCharacter = null;
    private String literalCharacter = null;
    
    private EntityReport entityReport = null;
    private String entityAccessXML = null;
    private HashMap<String, String> physicalAuthenticationMap = 
    		new HashMap<String, String>();
    
    private String md5HashValue = null;
    private String sha1HashValue = null;
    private StringBuffer headerBuffer = new StringBuffer();
    
    private String physicalSize = null;
    private String physicalSizeUnit = null;
    
    /* 
     * Constructors 
     */
    
    
    /**
     * Constructs this object with some extra parameters.
     * 
     * @param name          the name of the Entity
     * @param description   the description of the Entity
     * @param caseSensitive indicates whether this Entity is caseSensitive
     * @param orientation   indicates whether this Entity is column or row
     *                      major
     * @param numRecords    the number of records in this Entity
     * @param emlNamespace  the EML namespace value, e.g. 
     *                      "eml://ecoinformatics.org/eml-2.2.0"
     */
    public Entity(String id, String name, String description,
                       Boolean caseSensitive, String orientation,
                       int numRecords, String emlNamespace)
    {
        this(id, name, description, null);
        attributeList = new AttributeList();
        
        if (caseSensitive != null) {
            this.caseSensitive = caseSensitive;
        }
        
        if (emlNamespace != null) {
            this.emlNamespace = emlNamespace;
        }
        
        if (orientation != null) {
            this.orientation = orientation;
        }
        
        this.numRecords = numRecords;
    }
    

    /**
     * Constructs this object with some extra parameters.
     * 
     * @param name          the name of the Entity
     * @param description   the description of the Entity
     * @param caseSensitive indicates whether this Entity is caseSensitive
     * @param orientation   indicates whether this Entity is column or row
     *                      major
     * @param numRecords    the number of records in this Entity
     */
    public Entity(String id, String name, String description,
                       Boolean caseSensitive, String orientation,
                       int numRecords)
    {
        this(id, name, description, null);
        attributeList = new AttributeList();
        
        if (caseSensitive != null) {
            this.caseSensitive = caseSensitive;
        }
        
        if (orientation != null) {
            this.orientation = orientation;
        }
        
        this.numRecords = numRecords;
    }
    

    /**
     * Construct an Entity, setting the list of attributes.
     */
    public Entity(String id, String name, String description,
            AttributeList attributeList)
    {
        super(id, name, description);
        fileName = "";
        this.attributeList = attributeList;       
        this.caseSensitive = new Boolean(false);
        this.orientation = "";
        this.entityReport = new EntityReport(this);
        
        checkEntityName(name);
        checkEntityDescription(description);
    }
    
    
    
    public void setMd5HashValue(String hashValue) {
    	this.md5HashValue = hashValue;
    	
    	checkIntegrityChecksum("MD5", hashValue);
    }
    
    
    public void setSha1HashValue(String hashValue) {
    	this.sha1HashValue = hashValue;
    	
    	checkIntegrityChecksum("SHA-1", hashValue);
    }
    
    
    /*
     * Class methods
     */
    
    public static boolean isPreferredFormatString(String formatString) {
        Set<String> preferredFormatStrings = getPreferredFormatStrings();
        return preferredFormatStrings.contains(formatString);
    }
    
    
    public static Set<String> getPreferredFormatStrings() {
        return formatStringRegexes.keySet();
    }
    
    
    public static String getFormatStringRegex(String formatString) { 
        String regex = null;
        
        if (formatString != null && getPreferredFormatStrings().contains(formatString)) {
            regex = formatStringRegexes.get(formatString);
        }
        
        return regex;
    }
    
    
    /**
     * Find a preferred format string that differs only by case from the
     * formatString that was specified in the EML document. This will allow
     * the quality checker to suggest changing the case of the formatString
     * value in the EML.
     * 
     * @param formatString       
     *           the formatString as documented in the EML
     * @return caseMismatch     
     *           a preferred format string that differs only by case from the 
     *           format string found in the EML, or null if no such string
     *           can be found
     */
    public static String caseMismatchFormatString(String formatString) {
    	String caseMismatch = null;
        Set<String> preferredFormatStrings = getPreferredFormatStrings();
    	
		if (formatString != null && preferredFormatStrings != null) {
			for (String preferred : preferredFormatStrings) {
				if (!formatString.equals(preferred) && 
					formatString.equalsIgnoreCase(preferred)
				) {
					caseMismatch = preferred;
					break;
				}
			}
		}

    	return caseMismatch;
    }
    
    
    /**
     * Find a preferred format string that differs only by dash-versus-slash
     * characters from the formatString that was specified in the EML document. 
     * This will allow the quality checker to suggest changing the 
     * formatString by replacing slashes ("/") with dashes ("-") in the EML.
     * 
     * @param formatString       
     *           the formatString as documented in the EML
     * @return slashMismatch     
     *           a preferred format string that differs only by the
     *           fact that is uses dashes (ISO-8601) instead of slashes
     */
    public static String slashMismatchFormatString(String formatString) {
    	String slashMismatch = null;
        Set<String> preferredFormatStrings = getPreferredFormatStrings();
    	
    	/*
    	 * Only go through the list of preferred when the formatString
    	 * contains one or more slashes.
    	 */
		if (preferredFormatStrings != null && formatString != null && formatString.contains("/")) {
			for (String preferred : preferredFormatStrings) {
				if (!formatString.equals(preferred) && 
					formatString.replace('/', '-').equals(preferred)
				) {
					slashMismatch = preferred;
					break;
				}
			}
		}

    	return slashMismatch;
    }
    
    
   public static void addFormatString(String formatString, String regex) {
        if (formatString != null) {
            formatStringRegexes.put(formatString, regex);
        }
    }
   
  
    /*
     * Instance methods
     */
    
    /**
     * Add an Attribute to this table.
     * 
     * @param  a  the Attribute object to be added.
     */
    public void add(Attribute a)
    {
      this.attributeList.add(a);
      //a.setParent(this);
    }

    
    /**
     * Adds a physical authentication entry, mapping a hash method to a 
     * hash value.
     * 
     * @param method     The hash method, e.g. "MD5"
     * @param hashValue  The 32-character hash value.
     */
    public void addPhysicalAuthentication(String method, String hashValue) {
    	if (method != null) {
    		if (method.equalsIgnoreCase("MD5")) {
    			physicalAuthenticationMap.put("MD5", hashValue);
    		}
    		else if (method.equalsIgnoreCase("SHA1") ||
    				 method.equalsIgnoreCase("SHA-1")) {
    			physicalAuthenticationMap.put("SHA-1", hashValue);
    		}
    	}
    }
    
    
    /**
     * Retrieves the physical authentication hash value stored for a specified method
     * such as "MD5" or "SHA-1".
     * 
     * @param method     the method, e.g. "MD5" or "SHA-1".
     * @return the associated hash value, as parsed from the EML metadata document.
     *         For MD5, a 32 character string. For SHA-1, a 40 character string.
     *         Returns null if the EML did not document a value for the specified method.
     */
    public String getPhysicalAuthentication(String method) {
    	String hashValue = null;
    	
    	hashValue = physicalAuthenticationMap.get(method);
    	
    	return hashValue;
    }
    
    
    /**
     * Adds a quality check to the entity's associated entityReport object.
     * 
     * @param qualityCheck    the new quality check to add to the list
     */
    public void addQualityCheck(QualityCheck qualityCheck) {
      if (entityReport != null) {
        entityReport.addQualityCheck(qualityCheck);
        System.err.printf("  Entity: %s  Quality Check: %23s  Status: %5s\n", this, qualityCheck.getIdentifier(), qualityCheck.getStatus());
      }
    }
    
    
    /**
     * Gets the list of attributes for this Entity. 
     * 
     * @return  an array of Attribute objects
     */
    public Attribute[] getAttributes()
    {
        Attribute[] attrList = attributeList.getAttributes();
       
        return attrList;
    }

    
    /**
     * Indicates whether the Entity is case sensitive or not.
     * 
     * @return  true if case sensitive, else false.
     */
    public Boolean getCaseSensitive()
    {
      return caseSensitive;
    }

    
    /**
     * Gets the firstKilobyte string of the entity
     * @return
     */
    public String getFirstKilobyte() {
      return firstKilobyte;
    }


    /**
     * Gets the orientation of the table entity.
     * 
     * @return   a string representing the orientation
     */
    public String getOrientation()
    {
      return orientation;
    }

    
    /**
     * Gets the number of records in the entity.
     * 
     * @return  the number of records, an int
     */
    public int getNumRecords()
    {
      return numRecords;
    }

    
    /**
     * Sets the number of header lines in the entity.
     * 
     * @param  numHeaderLines  the value of the number of header lines to be set
     */
    public void setNumHeaderLines(Integer numHeaderLines)
    {
      this.numHeaderLines = numHeaderLines;

      /*
       *  Do a quality check for the presence of numHeaderLines element
       */
      String identifier = "numHeaderLinesPresent";
      QualityCheck qualityCheckTemplate = 
        QualityReport.getQualityCheckTemplate(identifier);
      QualityCheck qualityCheck = 
        new QualityCheck(identifier, qualityCheckTemplate);

      if (QualityCheck.shouldRunQualityCheck(this, qualityCheck)) {
        boolean hasNumHeaderLines = (numHeaderLines != null);
        String found = null;
        if (hasNumHeaderLines) {
            found = String.format("'numHeaderLines' element: %s", numHeaderLines.toString());
        	qualityCheck.setExplanation("");
        	qualityCheck.setSuggestion("");
        }
        else {
            found = "No 'numHeaderLines' element found";
        }
        qualityCheck.setFound(found);
        addQualityCheck(qualityCheck);
      }
    }
    
    
    /**
     * Perform a quality check on a dateTime/formatString value found in
     * the attribute list of this entity.
     * 
     * @param  formatString  the dateTime/formatString value
     */
    public void checkDateTimeFormatString(String formatString) {
      String identifier = "dateTimeFormatString";
      QualityCheck qualityCheckTemplate = QualityReport.getQualityCheckTemplate(identifier);
      QualityCheck qualityCheck = new QualityCheck(identifier, qualityCheckTemplate);

      if (QualityCheck.shouldRunQualityCheck(this, qualityCheck)) {
        boolean isPreferred = isPreferredFormatString(formatString);
        String explanation = null;
        String suggestion = null;
        
        if (isPreferred) {
            qualityCheck.setFound(formatString);
            explanation = "A preferred format string was found.";
        }
        else {
            qualityCheck.setFound(
            		String.format("'%s' is not in the set of preferred values.", formatString));
        	String caseMismatch = caseMismatchFormatString(formatString);
        	if (caseMismatch != null) {
        		explanation = 
        			String.format(
        				"The format string '%s' differs only by case with preferred format string '%s'.",
        				formatString, caseMismatch);
        		suggestion = String.format("Use '%s' in place of '%s'.",
        				caseMismatch, formatString);
        	}
        	else {
        		String slashMismatch = slashMismatchFormatString(formatString);
        		if (slashMismatch != null) {
            		explanation = 
                			String.format(
                				"The format string '%s' contains slashes instead of dashes.",
                				formatString);
            		suggestion = String.format("Use '%s' in place of '%s'.",
                                               slashMismatch, formatString);
        		}
        	}
        }
        
        if (isPreferred) {
        	qualityCheck.setStatus(Status.valid);
        }
        else {
        	qualityCheck.setFailedStatus();
        }

        if (explanation != null) qualityCheck.setExplanation(explanation);   
        if (suggestion != null) qualityCheck.setSuggestion(suggestion);
        
        addQualityCheck(qualityCheck);
      }
    }
    
    
    /**
     * Sets the number of footer lines in the entity.
     * 
     * @param numFooterLines  the value of the number of footer lines to be set
     */
	public void setNumFooterLines(Integer numFooterLines) {
		this.numFooterLines = numFooterLines;

		/*
		 * Do a quality check for the presence of numFooterLines element
		 */
		String identifier = "numFooterLinesPresent";
		QualityCheck qualityCheckTemplate = QualityReport
				.getQualityCheckTemplate(identifier);
		QualityCheck qualityCheck = new QualityCheck(identifier,
				qualityCheckTemplate);

		if (QualityCheck.shouldRunQualityCheck(this, qualityCheck)) {
			boolean hasNumFooterLines = (numFooterLines != null);
			String found = null;
			if (hasNumFooterLines) {
				found = String.format("'numFooterLines' element: %s",
						numFooterLines.toString());
				qualityCheck.setExplanation("");
				qualityCheck.setSuggestion("");
			}
			else {
				found = "No 'numFooterLines' element found";
			}
			qualityCheck.setFound(found);
			addQualityCheck(qualityCheck);
		}
	}

    
    /**
     * Gets the number of header lines in the entity.
     * 
     * @return  a value indicating the number of header lines
     */
	public int getNumHeaderLines() {
		if (numHeaderLines == null) {
			return 0;
		}
		else {
			return this.numHeaderLines.intValue();
		}
	}


	/**
	 * Gets the number of footer lines in the entity.
	 * 
	 * @return a value indication the number of footer lines
	 */
	public int getNumFooterLines() {
		if (numFooterLines == null) {
			return 0;
		}
		else {
			return this.numFooterLines.intValue();
		}
	}

    
    /**
     * Sets the fieldDelimiter used with this entity.
     * 
     * @param  delimiter   the delimiter string to be set
     */
    public void setFieldDelimiter(String delimiter)
    {
      this.fieldDelimiter = delimiter;
      
      /*
       *  Check the validity of the fieldDelimiter value
       */
      String fieldDelimiterIdentifier = "fieldDelimiterValid";
      QualityCheck fieldDelimiterTemplate = 
        QualityReport.getQualityCheckTemplate(fieldDelimiterIdentifier);
      QualityCheck fieldDelimiterQualityCheck = 
        new QualityCheck(fieldDelimiterIdentifier, fieldDelimiterTemplate);

      if (QualityCheck.shouldRunQualityCheck(this, fieldDelimiterQualityCheck)) {
        boolean isValidDelimiter = true;
        String found = delimiter;
        String explanation = fieldDelimiterQualityCheck.getExplanation();
        
        // Check for bad field delimiters
        if (!this.isTextFixed()) {
          if (delimiter == null || delimiter.equals("")) {
            isValidDelimiter = false;
            explanation += " The fieldDelimiter value is null or empty string.";
          }
          else {
            int delimiterLength = delimiter.length();    
            if (delimiterLength > 1) {
              String unescapedDelimiter = DelimitedReader.unescapeDelimiter(delimiter);
              if (delimiter.equals(unescapedDelimiter)) {
                isValidDelimiter = false;
                explanation += " The specified delimiter, '" + 
                             delimiter + "'," +
                             " is not a recognized fieldDelimiter value.";
              }
            }
          }
        }
          
        fieldDelimiterQualityCheck.setFound(found);
        
        if (this.isTextFixed()) {
          explanation = "A fieldDelimiter value is not used when describing fixed text entities";
          fieldDelimiterQualityCheck.setStatus(Status.info);
          fieldDelimiterQualityCheck.setSuggestion("");
        }
        else if (isValidDelimiter) {
          explanation = "A valid fieldDelimiter value was found";
          fieldDelimiterQualityCheck.setStatus(Status.valid);
          fieldDelimiterQualityCheck.setSuggestion("");
        }
        else if (getIsImageEntity() || isOtherEntity() || isExternallyDefinedFormat()) {
          explanation = "A fieldDelimiter value is not checked for binary entities or entities with an externally defined format";
          fieldDelimiterQualityCheck.setStatus(Status.info);
          fieldDelimiterQualityCheck.setSuggestion("");
        }
        else {
          fieldDelimiterQualityCheck.setFailedStatus();
        } 
        
        fieldDelimiterQualityCheck.setExplanation(explanation);
        addQualityCheck(fieldDelimiterQualityCheck);
      }
    }


    /**
     * Gets the fieldDelimiter used with this entity.
     * 
     * @return  the fieldDelimiter string value
     */
    public String getFieldDelimiter()
    {
      return this.fieldDelimiter;
    }
    
    /**
     * Gets the value of quote character. Quote character specifies a character 
     * to be used in the data file for quoting values so that field delimeters can 
     * be used within the value.
     * 
     * @return  a value indicating the value of quote character.
     */
    public String getQuoteCharacter()
    {
      return this.quoteCharacter;
    }
    
    /**
     * Sets the quote character in the entity.
     * 
     * @param quoteCharacter  the value of quote character to be set
     */
    public void setQuoteCharacter(String quoteCharacter)
    {
    	this.quoteCharacter = quoteCharacter;
    }
    
    
    /**
     * Gets the value of literal character. Literal character specifies a 
     * character to be used for escaping special character values so that 
     * they are treated as literal values 
     * 
     * @return  a value indicating the value of quote character.
     */
    public String getLiteralCharacter()
    {
      return this.literalCharacter;
    }
    
    /**
     * Sets the literal character in the entity.
     * 
     * @param literalCharacter  the value of literal character to be set
     */
    public void setLiteralCharacter(String literalCharacter)
    {
    	this.literalCharacter = literalCharacter;
    }

    
    /**
     * Sets the record delimiter used with this entity.
     * 
     * @param  delim  the record delimiter string value to be set
     */
    public void setRecordDelimiter(String delim)
    {
      this.recordDelimiter = delim;
    }
    
   
    /**
     * Gets the metadata record delimiter value that was specified for
     * this entity. May be null or an empty string.
     * 
     * @returns  the metadataRecordDelimiter value
     */
    public String getMetadataRecordDelimiter() {
      return this.metadataRecordDelimiter;
    }


    /**
     * Sets the metadata record delimiter value that was specified for
     * this entity. May be null or an empty string.
     * 
     * @param  delim  the record delimiter string value to be set
     */
    public void setMetadataRecordDelimiter(String metadataRecordDelimiter) {
      this.metadataRecordDelimiter = metadataRecordDelimiter;
      
      checkRecordDelimiter(metadataRecordDelimiter);
    }
    
    
    /**
     * Do a quality check on the entityDescription metadata value
     * 
     * @param description  The 'entityDescription' string as specified in the 
     *                     metadata
     */
    private void checkEntityDescription(String description) {
      String qualityCheckIdentifier = "entityDescriptionPresent";
      QualityCheck qualityCheckTemplate = 
        QualityReport.getQualityCheckTemplate(qualityCheckIdentifier);
      QualityCheck qualityCheck = 
        new QualityCheck(qualityCheckIdentifier, qualityCheckTemplate);

      if (QualityCheck.shouldRunQualityCheck(this, qualityCheck)) {
        Boolean hasEntityDescription = ((description != null) && (description.length() > 0));
        qualityCheck.setFound(hasEntityDescription.toString());
        if (hasEntityDescription) {
          qualityCheck.setStatus(Status.valid);
        }
        else {
          qualityCheck.setFailedStatus();
        }
        
        addQualityCheck(qualityCheck);
      }
    }


	/**
	 * Do a quality check for the presence of a size element in this entity.
	 */
	public void checkEntitySizePresence() {
		String qualityCheckIdentifier = "entitySizePresence";
		QualityCheck qualityCheckTemplate = QualityReport.getQualityCheckTemplate(qualityCheckIdentifier);
		QualityCheck qualityCheck = new QualityCheck(qualityCheckIdentifier, qualityCheckTemplate);

		if (QualityCheck.shouldRunQualityCheck(this, qualityCheck)) {
			String physicalSize = this.getPhysicalSize();
			Boolean hasPhysicalSize = (physicalSize != null) && 
					                  !physicalSize.isEmpty();
			String physicalSizeUnit = this.getPhysicalSizeUnit();
			Boolean isValid = new Boolean(false);
			if (hasPhysicalSize) {
				isValid = true;
			}
			if (isValid) {
				String foundMsg = String.format(
					"Found entity size element with value of %s ",
					physicalSize);
				if (physicalSizeUnit != null && !physicalSizeUnit.isEmpty()) {
					foundMsg += "and size unit specified as '" + 
				                physicalSizeUnit + "'.";
					if (!physicalSizeUnit.equals("byte")) {
						foundMsg += 
						" Please note that a size element specified with a unit" +
						" other than 'byte' will not be tested for congruence.";
					}
				}
				else {
					foundMsg += "with no unit specified (defaults to 'byte').";
				}
				qualityCheck.setFound(foundMsg);
				qualityCheck.setStatus(Status.valid);
			} 
			else {
				qualityCheck.setFound("No entity size value was found.");
				qualityCheck.setFailedStatus();
			}

			addQualityCheck(qualityCheck);
		}
	}   
	
	
	/**
	 * Do a quality check for the presence of a least one
	 * physical/authentication element in this entity that
	 * specifies a method attribute with a value of MD5
	 * or SHA-1.
	 * 
	 */
	public void checkIntegrityChecksumPresence() {
		String qualityCheckIdentifier = "integrityChecksumPresence";
		QualityCheck qualityCheckTemplate = QualityReport.getQualityCheckTemplate(qualityCheckIdentifier);
		QualityCheck qualityCheck = new QualityCheck(qualityCheckIdentifier, qualityCheckTemplate);

		if (QualityCheck.shouldRunQualityCheck(this, qualityCheck)) {
			Boolean hasIntegrityChecksum = this.hasPhysicalAuthentication();
			Boolean hasMethodAttribute = this.hasMethodAttribute();
			Boolean isValid = new Boolean(false);
			if (hasIntegrityChecksum && hasMethodAttribute) {
				isValid = true;
			}
			qualityCheck.setFound(isValid.toString());
			if (isValid) {
				qualityCheck.setStatus(Status.valid);
			} else {
				qualityCheck.setFailedStatus();
				if (!hasIntegrityChecksum) {
					qualityCheck.setFound("No authentication element was found.");
				} else if (!hasMethodAttribute) {
					qualityCheck.setFound(
							"One or more authentication elements were found, but no method attribute with value MD5 or SHA-1 was found.");
				}
			}

			addQualityCheck(qualityCheck);
		}
	}    
    
    private boolean hasMethodAttribute() {
       	boolean hasMD5 = physicalAuthenticationMap.get("MD5") != null;
    	boolean hasSHA1 = physicalAuthenticationMap.get("SHA-1") != null;
    	boolean hasMethodAttribute= hasMD5 || hasSHA1;
    	
    	return hasMethodAttribute;
    }


    /**
     * Do a quality check matching the congruency of an actual integrity
     * hash value to the value (if any) documented in the metadata.
     * 
     */
	public void checkIntegrityChecksum(String actualHashMethod, String actualHashValue) {
		String qualityCheckIdentifier = "integrityChecksum";
		QualityCheck qualityCheckTemplate = QualityReport.getQualityCheckTemplate(qualityCheckIdentifier);
		QualityCheck qualityCheck = new QualityCheck(qualityCheckIdentifier, qualityCheckTemplate);

		if (QualityCheck.shouldRunQualityCheck(this, qualityCheck)) {
			boolean hasIntegrityChecksum = this.hasPhysicalAuthentication();
			if (hasIntegrityChecksum) {
				boolean congruent = false;

				for (String hashMethod : physicalAuthenticationMap.keySet()) {
					if (hashMethod.equals(actualHashMethod)) {
						String metadataHashValue = physicalAuthenticationMap.get(hashMethod);
						if (metadataHashValue != null) {
							qualityCheck.setExpected(actualHashValue);
							qualityCheck.setFound(metadataHashValue);
							if (actualHashValue.equalsIgnoreCase(metadataHashValue)) {
								congruent = true;
							}
						}

						if (congruent) {
							qualityCheck.setStatus(Status.valid);
						} 
						else {
							qualityCheck.setExplanation(qualityCheck.getExplanation());
							qualityCheck.setSuggestion(qualityCheck.getSuggestion());
							qualityCheck.setFailedStatus();
						}

						addQualityCheck(qualityCheck);
					}
				}
			}
		}
	}

	
	/**
	 * Do a quality check for the presence of the numberOfRecords element in
	 * this entity.
	 */
	public void checkNumberOfRecordsPresence() {
		String qualityCheckIdentifier = "numberOfRecordsPresence";
		QualityCheck qualityCheckTemplate = 
				QualityReport.getQualityCheckTemplate(qualityCheckIdentifier);
		QualityCheck qualityCheck = 
				new QualityCheck(qualityCheckIdentifier, qualityCheckTemplate);

		if (QualityCheck.shouldRunQualityCheck(this, qualityCheck)) {
			// Run this quality check only on dataTable entities
			if (isDataTableEntity()) {
				if (this.hasNumberOfRecords) {
					qualityCheck.setFound("numberOfRecords element found");
					qualityCheck.setStatus(Status.valid);
					qualityCheck.setSuggestion("");
				} else {
					qualityCheck.setFound("numberOfRecords element not found");
					qualityCheck.setFailedStatus();
				}

				addQualityCheck(qualityCheck);
			}
		}
	}

	
    /**
     * Do a quality check on the entityName metadata value
     * 
     * @param entityName   The 'entityName' string as specified in the 
     *                     metadata
     */
    private void checkEntityName(String entityName) {
      /*
       *  Do a quality check on the entityName value
       */
      final int ENTITY_NAME_MAX_LENGTH = 100;
      String entityNameIdentifier = "entityNameLength";
      QualityCheck entityNameTemplate = 
        QualityReport.getQualityCheckTemplate(entityNameIdentifier);
      QualityCheck entityNameQualityCheck = 
        new QualityCheck(entityNameIdentifier, entityNameTemplate);

      if (QualityCheck.shouldRunQualityCheck(this, entityNameQualityCheck)) {
        boolean isValidLength = false;
        int nameLength = (entityName == null) ? 0 : entityName.length();
        Integer found = new Integer(nameLength);
        if (found <= ENTITY_NAME_MAX_LENGTH) {
          isValidLength = true;
        }
        
        entityNameQualityCheck.setFound(found.toString());
        if (isValidLength) {
          entityNameQualityCheck.setStatus(Status.valid);
        }
        else {
          entityNameQualityCheck.setFailedStatus();
        }
        
        addQualityCheck(entityNameQualityCheck);
      }
    }


    /**
     * Do a quality check on the recordDelimiter metadata value
     * 
     * @param metadataValue   The record delimiter string as specified in the 
     *                        metadata
     */
    public void checkRecordDelimiter(String metadataValue) {
      /*
       *  Do a quality check on the recordDelimiter value
       */
      String recordDelimiterIdentifier = "recordDelimiterPresent";
      QualityCheck recordDelimiterTemplate = 
        QualityReport.getQualityCheckTemplate(recordDelimiterIdentifier);
      QualityCheck recordDelimiterQualityCheck = 
        new QualityCheck(recordDelimiterIdentifier, recordDelimiterTemplate);

      if (QualityCheck.shouldRunQualityCheck(this, recordDelimiterQualityCheck)) {
        boolean isValidDelimiter = true;
        String found = metadataValue;
        String explanation = recordDelimiterQualityCheck.getExplanation();
        
        // Check for unusual record delimiter values
        if (metadataValue == null || metadataValue.equals("")) {
          isValidDelimiter = false;
          explanation += " The recordDelimiter value is null or an empty string.";
        }
        else if (!isSuggestedRecordDelimiter(metadataValue)) {
          isValidDelimiter = false;
          explanation += 
            " The specified recordDelimiter, '" + 
            metadataValue + "'," +
            " is not in the list of suggested recordDelimiter values.";
        }
          
        recordDelimiterQualityCheck.setFound(found);
        if (isValidDelimiter) {
          explanation = "A valid recordDelimiter value was found";
          recordDelimiterQualityCheck.setStatus(Status.valid);
          recordDelimiterQualityCheck.setSuggestion("");
        }
        else if (getIsImageEntity() || isOtherEntity() || isExternallyDefinedFormat()) {
          explanation = "A recordDelimiter value is not checked for binary entities or entities with an externally defined format";
          recordDelimiterQualityCheck.setStatus(Status.info);
          recordDelimiterQualityCheck.setSuggestion("");
        }
        else {
          recordDelimiterQualityCheck.setFailedStatus();
        } 
        recordDelimiterQualityCheck.setExplanation(explanation);
        addQualityCheck(recordDelimiterQualityCheck);
      }
      
    }
    
    
    /*
     * Boolean method used for quality check on record delimiter value
     */
    public boolean isSuggestedRecordDelimiter(String recordDelimiter) {
      boolean isSuggested = false;
      
      if (recordDelimiter != null) {
        TreeSet<String> treeSet = suggestedRecordDelimiters();
        isSuggested = treeSet.contains(recordDelimiter);
      }
      
      return isSuggested;
    }
    
    
    /**
     * Returns a set of suggested record delimiter values.
     * 
     * @return  treeSet, a set of suggested record delimiter values
     */
    private TreeSet<String> suggestedRecordDelimiters() {
      TreeSet<String> treeSet = new TreeSet<String>();
      treeSet.add("\\n");
      treeSet.add("\\r");
      treeSet.add("\\r\\n");
      treeSet.add("#x0A");
      treeSet.add("#x0D");
      treeSet.add("#x0D#x0A");
      return treeSet;
    }
    
 
    /**
     * Gets the recordDelimiter used with this entity.
     * 
     * @return  the recordDelimiter string value for this entity
     */
    public String getRecordDelimiter()
    {
      return this.recordDelimiter;
    }

    
    /**
     * Sets the url value for this entity.
     * 
     * @param url    the url string value to be set
     */
    public void setURL(String url)
    {
      this.url = url;
    }

    
    /**
     * Sets the urlContentType value for this entity.
     * 
     * @param url  the urlContentType string value to be set
     */
    public void setUrlContentType(String urlContentType) {
      this.urlContentType = urlContentType;
    }


    /**
     * Sets the urlFunction value for this entity.
     * 
     * @param url    the urlFunction string value to be set
     */
    public void setURLFunction(String urlFunction) {
      this.urlFunction = urlFunction;
    }


    /**
     * Gets the url value for this entity.
     * 
     * @return  the url string value for this entity.
     */
    public String getURL()
    {
      return this.url;
    }
    
    
    /**
     * Gets the urlContentType value for this entity.
     * 
     * @return  the urlContentType string value for this entity.
     */
    public String getUrlContentType() {
      return urlContentType;
    }


    /**
     * Gets the urlFunction value for this entity.
     * 
     * @return  the urlFunction string value for this entity.
     */
    public String getUrlFunction() {
      return urlFunction;
    }

  
    /**
     * Sets the format for this entity.
     * 
     * @param format    the format string value to be set
     */
    public void setDataFormat(String format)
    {
      this.format = format;
    }

    
    /**
     * Gets the format value for this entity.
     * 
     * @return  the format string value for this entity.
     */
    public String getDataFormat()
    {
      return this.format;
    }

    
    /**
     * Gets the database field names for the attributes in this entity.
     * 
     * @return   an array of Strings objects, or null if there are no
     *           attributes in the entity's attribute list. 
     */
    public String[] getDBFieldNames()
    {
      if (attributeList != null) {
        return attributeList.getDBFieldNames();
      }
      else {
        return null;
      }
    }

    
    /**
     * Sets the database table name for this entity.
     * 
     * @param dbTableName  the database table name string value to be set.
     */
    public void setDBTableName(String dbTableName)
    {
      this.dbTableName = dbTableName;
    }

    
    /**
     * Gets the database table name for this entity.
     * 
     * @return  the database table name string value
     */
    public String getDBTableName()
    {
      return this.dbTableName;
    }
    
    
    /**
     * Boolean to determine whether this entity can collapse consecutive 
     * delimiters.
     * 
     * @return  true if can collapse consecutive delimiters, else false.
     */
    public boolean getCollapseDelimiters()
    {
    	return this.collapseDelimiters;
    }
  
    
    /**
     * Sets the collapse delimiter value.
     * 
     * @param collapseDelimiters  the value to set for collapseDelimiters, a
     *                            boolean
     */
    public void setCollapseDelimiters(boolean collapseDelimiters)
    {
    	this.collapseDelimiters = collapseDelimiters;
    }
    
    
    /**
     * Gets the file name for this entity.
     * 
     * @return   a string holding the file name
     */
    public String getFileName()
    {
      return fileName;
    }

    
    /**
     * Sets the fileName for this entity.
     * 
     * @param fileName   The fileName value to set
     */
    public void setFileName(String fileName)
    {
      this.fileName = fileName;
    }

    
    /**
     * Sets the multiple value to true.
     */
    public void setMultiple()
    {
      this.multiple = true;
    }

    
    /**
     * Gets the multiple value.
     * 
     * @return   the multiple value, a boolean
     */
    public boolean isMultiple()
    {
      return multiple;
    }

    
    //-----------------------------------------------------------------
    //-- DSTableIFace
    //-----------------------------------------------------------------
    
    
    /**
     * Gets the database table name for this entity.
     * 
     * @return  the database table name string
     */
    public String getMappedName()
    {
        return this.dbTableName;
    }

    
    /**
     * Gets the attribute list for this entity.
     * 
     * @return vector  an array of Attribute objects
     */
    public Attribute[] getFields()
    {
      return attributeList.getAttributes();
    }
    
    
    /**
     * Gets the Primary Key Definition for the table.
     * 
     * @return   A primary key Constraint object. (Currently always null)
     */
    public Constraint getPrimaryKey()
    {
      return null;
    }
    
    
    /**
     * Gets the physical size string value that was found 
     * in the metadata (if any).
     * 
     * @return physicalSize The physicalSize string
     */
    public String getPhysicalSize()
    {
        return this.physicalSize;
    }
    
    
    /**
     * Gets the physical size unit string value that was found 
     * in the metadata (if any).
     * 
     * @return physicalSizeUnit The physicalSizeUnit string
     */
    public String getPhysicalSizeUnit()
    {
        return this.physicalSizeUnit;
    }
    
    
    /**
     * Gets the compression method for the entity distribution file.
     * 
     * @return the compressionMethod string value
     */
    public String getCompressionMethod()
    {
      return this.compressionMethod;
    }
    
    
    /**
     * Sets the compression method for the entity distribution file.
     * 
     * @param compressionMethod  A string representing the compression method.
     */
    public void setCompressionMethod(String compressionMethod)
    {
      this.compressionMethod = compressionMethod;
    }
    
    
    /**
     * Boolean to determine if this entity has at least one distribution 
     * online element.
     * 
     * @return boolean  true if the entity has a distribution online 
     *                  element, else false
     */
    public boolean hasDistributionOnline() {
      return this.hasDistributionOnline;
    }
    
    
    /**
     * Sets the isDistributionOnline field to store whether this entity
     * has at least one distribution online element.
     * 
     * @param distributionOnline   the boolean value to set. true if 
     *        the entity has a distribution online element, else false
     */
    public void setHasDistributionOnline(boolean distributionOnline) {
      this.hasDistributionOnline = distributionOnline;
    }
    
    
    /**
     * Boolean to determine if this entity has at least one distribution 
     * offline element.
     * 
     * @return boolean  true if the entity has a distribution offline 
     *                  element, else false
     */
    public boolean hasDistributionOffline() {
      return this.hasDistributionOffline;
    }
    
    
    /**
     * Sets the isDistributionOffline field to store whether this entity
     * has at least one distribution offline element.
     * 
     * @param distributionOffline   the boolean value to set. true if 
     *        the entity has a distribution offline element, else false
     */
    public void setHasDistributionOffline(boolean distributionOffline) {
      this.hasDistributionOffline = distributionOffline;
    }
    
    
    /**
     * Boolean to determine if this entity has at least one distribution 
     * inline element.
     * 
     * @return boolean  true if the entity has a distribution inline 
     *                  element, else false
     */
    public boolean hasDistributionInline() {
      return this.hasDistributionInline;
    }
    
    
    /**
     * Sets the isDistributionInline field to store whether this entity
     * has at least one distribution inline element.
     * 
     * @param distributionInline   the boolean value to set. true if 
     *        the entity has a distribution inline element, else false
     */
    public void setHasDistributionInline(boolean distributionInline) {
      this.hasDistributionInline = distributionInline;
    }
    
    
    /**
     * Boolean to determine if this entity has at least one
     * physical/authentication element.
     * 
     * @return boolean  true if the entity has a physical/authentication
     *                  element, else false
     */
    public boolean hasPhysicalAuthentication() {
      return this.hasPhysicalAuthentication;
    }
    
    
    /**
     * Sets the hasNumberOfRecords field to store whether this entity
     * has the numberOfRecords element.
     * 
     * @param hasElement   the boolean value to set. true if 
     *        the entity has a numberOfRecords element, else false
     */
    public void setHasNumberOfRecords(boolean hasElement) {
      this.hasNumberOfRecords = hasElement;
      
      /*
       * After setting the boolean, give the quality check an opportunity to
       * run.
       */
      checkNumberOfRecordsPresence();
    }
    
    
    /**
     * Sets the hasPhysicalAuthentication field to store whether this entity
     * has at least one physical/authentication element.
     * 
     * @param physicalAuthentication   the boolean value to set. true if 
     *        the entity has a physical/authentication element, else false
     */
    public void setHasPhysicalAuthentication(boolean physicalAuthentication) {
      this.hasPhysicalAuthentication = physicalAuthentication;
    }
    
    
    /**
     * Boolean to determine if this entity is an image entity for SpatialRaster 
     * or SpatialVector
     * 
     * @return boolean  true if this is an image entity, else false
     */
    public boolean getIsImageEntity()
    {
      return this.isImageEntity;
    }
    
    
    /**
     * Sets the isImageEntity field to store whether this is an image entity
     * 
     * @param isImageEntity   the boolean value to set. true if this is an
     *                        image entity, else false
     */
    public void setIsImageEntity(boolean isImageEntity)
    {
      this.isImageEntity = isImageEntity;
    }
    
    
    /**
     * Gets the isDataTableEntity value.
     * 
     * @return isDataTableEntity  true if this is a dataTable entity, else false
     */
    public boolean isDataTableEntity() {
      return isDataTableEntity;
    }
    
    
    /**
     * Sets the isDataTableEntity value.
     * 
     * @param isDataTableEntity  true if this is a dataTable entity, else false
     */
    public void setIsDataTableEntity(boolean isDataTableEntity) {
      this.isDataTableEntity = isDataTableEntity;
    }
    
    
    /**
     * Gets the isOtherEntity value.
     * 
     * @return isOtherEntity  true if this is an otherEntity entity, else false
     */
    public boolean isOtherEntity() {
      return isOtherEntity;
    }
    
    
    /**
     * Sets the isOtherEntity value.
     * 
     * @param isOtherEntity  true if this is an otherEntity entity, else false
     */
    public void setIsOtherEntity(boolean isOtherEntity) {
      this.isOtherEntity = isOtherEntity;
    }
    
    
    /**
     * Boolean to determine if the data file is zip file.
     * 
     * @return  true if the entity data is in a zip file, else false.
     */
    public boolean getHasZipDataFile()
    {
      return this.hasZipDataFile;
    }
    
    
    /**
     * Gets the value of the headerText field.
     * 
     * @return headerText the text of the entity's header lines
     */
    public String getHeaderText() {
        return this.headerBuffer.toString();
    }
    
    
    public void addHeaderLine(String line) {
        if (line != null) {
            this.headerBuffer.append(line);
        }
    }
    
    
    /**
     * Sets the isZipDataFile boolean field.
     * 
     * @param isZipDataFile the boolean value to set.
     */
    public void setHasZipDataFile(boolean isZipDataFile)
    {
      this.hasZipDataFile = isZipDataFile;
    }
    
    
    /**
     * Gets the value of the hasGZipDataFile field.
     * 
     * @return true if this entity has a gzip data file, else false
     */
    public boolean getHasGZipDataFile()
    {
      return this.hasGZipDataFile;
    }
    
    
    /**
     * Sets the boolean value of the hasGZipDataFile field.
     * 
     * @param hasGZipDataFile  the boolean value to set
     */
    public void setHasGZipDataFile(boolean hasGZipDataFile)
    {
      this.hasGZipDataFile = hasGZipDataFile;
    }
    
    
    /**
     * Gets the value of the hasTarDataFile field.
     * 
     * @return boolean  the boolean value of the hasTarDataFile field
     */
    public boolean getHasTarDataFile()
    {
      return this.hasTarDataFile;
    }
    
    /**
     * Sets a boolean value to determine if this entity has a tar data file
     * 
     * @param hasTarDataFile true if this entity has a tar data file, else false
     */
    public void setHasTarDataFile(boolean hasTarDataFile)
    {
      this.hasTarDataFile = hasTarDataFile;
    }
    
    
    /*
     * Gets the EML namespace value, e.g. "eml://ecoinformatics.org/eml-2.2.0"
     */
    public String getEmlNamespace() {
        return emlNamespace;
    }
    
    
    /**
     * Gets the entity <access> XML block and returns it as an XML string.
     * Will be null in cases where no <access> XML was defined for this
     * entity. (In such cases, access should default to that of the dataset.)
     * 
     * @return  an XML string holding the entity <access> block, or null
     */
    public String getEntityAccessXML() {
      return entityAccessXML;
    }
    
    
    /**
     * Sets the value of the entityAccessXML field to a string
     * which should hold a block of <access> element XML.
     * 
     * @param xmlString   the <access> element XML string
     */
    public void setEntityAccessXML(String xmlString) {
      this.entityAccessXML = xmlString;
    }
    
    
    /**
     * Gets the identifier for this entity. Currently we use distribution url
     * as entity identifier.
     * 
     * @return identifier of this entity, a string holding the distribution url
     */
    public String getEntityIdentifier()
    {
    	return url;
    }
    
    
    /**
     * Gets the entityReport object associated with this entity.
     * 
     * @return  the entityReport instance variable
     */
    public EntityReport getEntityReport()
    {
      return entityReport;
    }
    
    
    /**
     * Sets the identifier for this entity. Currently we use distribution url
     * as entity identifier.
     * 
     * @param identifier of this entity, a string holding the distribution url
     */
    public void setEntityIdentifier(String url)
    {
    	this.url = url;
    }
    
   
    /**
     * Sets the firstKilobyte string for the entity. Also,
     * if quality reporting is enabled, performs quality
     * checks on the first kilobyte of data.
     * 
     * @param firstKilobyte   the string value to set
     */
    public void setFirstKilobyte(String firstKilobyte) {
      this.firstKilobyte = firstKilobyte;

      /*
       *  Display the first chunk of data as a quality check
       */
      String displayDownloadIdentifier = "displayDownloadData";
      QualityCheck displayDownloadTemplate = 
        QualityReport.getQualityCheckTemplate(displayDownloadIdentifier);
      QualityCheck displayDownloadQualityCheck = 
        new QualityCheck(displayDownloadIdentifier, displayDownloadTemplate);

      if (QualityCheck.shouldRunQualityCheck(this, displayDownloadQualityCheck)) {
        /* String twoFiftySix = "";
        if (firstKilobyte != null) {
          twoFiftySix = firstKilobyte.substring(0, 256);
        }
        String foundString = "<![CDATA\n" + twoFiftySix + "\n]>"; */
        String foundString = null;
        if (isBinaryData()) {
          foundString = "*** BINARY DATA ***";
        }
        else {
          foundString = "<![CDATA[\n" + firstKilobyte.trim() + "]]>";
        }
        displayDownloadQualityCheck.setFound(foundString);
        displayDownloadQualityCheck.setStatus(Status.info);
        addQualityCheck(displayDownloadQualityCheck);
      }

      /*
       *  Check the veracity of the data returned
       */
      String urlDataIdentifier = "urlReturnsData";
      QualityCheck urlDataTemplate = 
        QualityReport.getQualityCheckTemplate(urlDataIdentifier);
      QualityCheck urlDataQualityCheck = 
        new QualityCheck(urlDataIdentifier, urlDataTemplate);

      if (QualityCheck.shouldRunQualityCheck(this, urlDataQualityCheck)) {
        boolean isHTML = isHTML(firstKilobyte);        
        if (isHTML) {
          String found = "The download URL for this entity returns HTML";
          urlDataQualityCheck.setFound(found);
          String explanation = "Either an HTML declaration string or an 'html' element was detected in the data";
          urlDataQualityCheck.setExplanation(explanation);
          String suggestion = "Specify function=\"information\" in the 'url' element when the URL links to an HTML page";
          urlDataQualityCheck.setSuggestion(suggestion);
          urlDataQualityCheck.setFailedStatus();
        }
        else {
          urlDataQualityCheck.setFound("true");
          urlDataQualityCheck.setStatus(Status.valid);
          urlDataQualityCheck.setSuggestion("");
        }
        addQualityCheck(urlDataQualityCheck);
      }
    }
    

    /*
     * Boolean to determine whether a data sample is 
     * actually an HTML page. 
     */
    private boolean isHTML(String sampleData) {
      boolean isHTML = false;
      
      if (sampleData != null) {
        String htmlDeclaration = "<!doctype html";
        String htmlElement1 = "<html ";
        String htmlElement2 = "<html>";
        String sampleDataLowerCase = sampleData.toLowerCase();
        
        // First check the MIME type
        if (urlContentType != null &&
            urlContentType.startsWith("text/html")
           ) {
          isHTML = true;
        }
        // else look for an HTML declaration
        else if (sampleDataLowerCase.contains(htmlDeclaration)) {
          isHTML = true;
        }
        // else look for an HTML tag
        else if (sampleDataLowerCase.contains(htmlElement1) ||
                 sampleDataLowerCase.contains(htmlElement2)
                ) {
          isHTML = true;
        }
      }
      
      return isHTML;
    }
    
    
    /**
     * Boolean to determine whether this entity's data is binary data
     * as opposed to character data.
     * 
     * @return  true if we determine that the entity has binary data,
     *          else false
     */
    public boolean isBinaryData() {
      boolean isBinary = false;
      
      /*
       * First check for a binary MIME type
       */
      if (isBinaryUrlContentType()) {
        isBinary = true;
      }
      
      /*
       * Then check to see whether we know this to be
       * binary based on the entity type
       */
      if (hasGZipDataFile ||
          hasTarDataFile ||
          hasZipDataFile ||
          isImageEntity ||
          isOtherEntity
         ) {
          isBinary = true;
      }

      return isBinary;
    }
    
    
    /*
     * Boolean to determine whether the URL contentType specifies
     * a binary data type.
     */
    private boolean isBinaryUrlContentType() {
      boolean isBinary = true;
      
      if (urlContentType != null) {
        /*
         * Check for known text content types
         */
        if (urlContentType.startsWith("text/") ||
            urlContentType.equals("application/xml")
           ) {
          isBinary = false;
        }
      }
      else {
        // Assume it could be text when no content type is specified
        isBinary = false;
      }
      
      return isBinary;
    }

    
    /**
     * Boolean to determine if data file in this entity uses an
     * externally defined format.
     * 
     * @return Returns the externallyDefinedFormat boolean value
     */
    public boolean isExternallyDefinedFormat()
    {
        return externallyDefinedFormat;
    }
    
    
    /**
     * Boolean to determine if data file in this entity is simple delimited.
     * 
     * @return Returns the simpleDelimited boolean value
     */
    public boolean isSimpleDelimited()
    {
        return simpleDelimited;
    }
    
    
    /**
     * Boolean to determine if data file in this entity is fixed text.
     * 
     * @return Returns the textFixed boolean value
     */
    public boolean isTextFixed()
    {
        return textFixed;
    }
    
    
    /**
     * Sets the value of the externallyDefinedFormat field.
     * 
     * @param textFixed The textFixed boolean value to set.
     */
    public void setExternallyDefinedFormat(boolean externallyDefinedFormat)
    {
        this.externallyDefinedFormat = externallyDefinedFormat;
    }
    
    
    /**
     * Sets the value of the simpleDelimited field.
     * 
     * @param simpleDelimited The simpleDelimited boolean value to set.
     */
    public void setSimpleDelimited(boolean simpleDelimited)
    {
        this.simpleDelimited = simpleDelimited;
    }
    
    
    /**
     * Sets the value of the textFixed field.
     * 
     * @param textFixed The textFixed boolean value to set.
     */
    public void setTextFixed(boolean textFixed)
    {
        this.textFixed = textFixed;
    }
    
    
    /**
     * Gets the complex data format array for this entity.
     * 
     * @return An array of TextComplexDataFormat objects
     */
    public TextComplexDataFormat[] getDataFormatArray()
    {
        return dataFormatArray;
    }
    
    
    /**
     * Sets the value of the DataFormatArray field.
     * 
     * @param dataFormatArray An array of TextComplexDataFormat objects
     */
    public void setDataFormatArray(TextComplexDataFormat[] dataFormatArray)
    {
        this.dataFormatArray = dataFormatArray;
    }
    
    
    /**
     * Gets the physical line delimiter string value.
     * 
     * @return Returns the physicalLineDelimiter value, a string
     */
    public String getPhysicalLineDelimiter()
    {
        return physicalLineDelimiter;
    }
    
    
    /**
     * Sets the physical line delimiter string value that was found 
     * in the metadata.
     * 
     * @param physicalLineDelimiter The physicalLineDelimiter string to set.
     */
    public void setPhysicalLineDelimiter(String physicalLineDelimiter)
    {
        this.physicalLineDelimiter = physicalLineDelimiter;
    }
    
    
    /**
     * Sets the physical size string value that was found 
     * in the metadata.
     * 
     * @param physicalSize The physicalSize string to set.
     */
    public void setPhysicalSize(String physicalSize)
    {
        this.physicalSize = physicalSize;
    }
    
    
    /**
     * Sets the physical size unit string value that was found 
     * in the metadata.
     * 
     * @param physicalSizeUnit The physicalSizeUnit string to set.
     */
    public void setPhysicalSizeUnit(String physicalSizeUnit)
    {
        this.physicalSizeUnit = physicalSizeUnit;
    }
    
    
    /**
     * Sets the attribute list for this entity.
     * 
     * @param list   the AttributeList object to set
     */
    public void setAttributeList(AttributeList list)
    {
        this.attributeList = list;

        /*
         *  Check for duplicate attribute names
         */
        String qualityCheckIdentifier = "attributeNamesUnique";
        QualityCheck qualityCheckTemplate = 
          QualityReport.getQualityCheckTemplate(qualityCheckIdentifier);
        QualityCheck qualityCheck = 
          new QualityCheck(qualityCheckIdentifier, qualityCheckTemplate);

        if (QualityCheck.shouldRunQualityCheck(this, qualityCheck)) {
          String duplicateAttributeNames = duplicateAttributeNames(list);
          if (duplicateAttributeNames != null) {
            String found = String.format("Duplicate attributeName values: %s", duplicateAttributeNames);
            qualityCheck.setFound(found);
            qualityCheck.setFailedStatus();
          }
          else {
            qualityCheck.setFound("true");
            qualityCheck.setStatus(Status.valid);
            qualityCheck.setSuggestion("");
          }
          addQualityCheck(qualityCheck);
        }
    }
    
    
    /*
     * Returns a comma-separated list of duplicate attribute names found in this
     * entity's attribute list, or null if no duplicates were discovered.
     * Used by the attributeNamesUnique quality check.
     */
    private String duplicateAttributeNames(AttributeList attributeList) {
    	String duplicates = null;
    	
    	if (attributeList != null) {
    	  String[] attributeNames = attributeList.getNames(); 
    	  if (attributeNames != null) {
    	    StringBuffer stringBuffer = new StringBuffer("");
    	    HashMap<String, Integer> duplicatesMap = new HashMap<String, Integer>();
    	    
    	    for (int i = 0; i < attributeNames.length; i++) {
    		  String attributeName = attributeNames[i];
    		  Integer value = duplicatesMap.get(attributeName);
    		  if (value == null) {
    			  duplicatesMap.put(attributeName, 1);
    		  }
    		  else {
    			  duplicatesMap.put(attributeName, ++value);
    		  }
    	    }
    	    
    	    for (String attributeName : duplicatesMap.keySet()) {
    	    	Integer value = duplicatesMap.get(attributeName);
    	    	if (value > 1) {
    	    		stringBuffer.append(String.format("'%s', ", attributeName));
    	    	}
    	    }
    	    
    	    duplicates = stringBuffer.toString();  
    	    // Trim off trailing comma and space
    	    if (duplicates.length() > 2) {
    	      duplicates = duplicates.substring(0, duplicates.length() - 2);
    	    }
    	    else {
    	    	duplicates = null;
    	    }
    	  }
    	}
    	
    	return duplicates;
    }
    
    
    /**
     * Gets the attributeList field.
     * 
     * @return  the AttributeList object stored in the attributeList field
     */
    public AttributeList getAttributeList()
    {
    	return this.attributeList;
    }
    
    /**
     * Sets the entity package id which it belongs to.
     * @param packageId the package id which the entity belongs to
     */
    public void setPackageId(String packageId)
    {
    	this.packageId = packageId;
    }
    
    /**
     * Gets the package id this entity belongs to 
     * @return pacakge id
     */
    public String getPackageId()
    {
    	return this.packageId;
    }
    
    
    /**
     * Get the DownloadHandler associated with this entity, which may be a 
     * sub-class of DownloadHandler. This version of the method calls the
     * two-parameter version, with 'preserveFormat' set to false as
     * the default behavior.
     * 
     * @param  endPointInfo  the object provides ecogrid end point information
     * @return the DownloadHandler object which will download data for this
     *         entity
     */
    public DownloadHandler getDownloadHandler(EcogridEndPointInterface endPointInfo)
    {
    	boolean preserveFormat = false; // default behavior is to unzip or un-tar
    	return getDownloadHandler(endPointInfo, preserveFormat);
    }

    
    /**
     * Get the DownloadHandler associated with this entity, which may be a 
     * sub-class of DownloadHandler. Currently we only handle one situation, 
     * e.g. one of DownloadHandler, ZipDataHandler, GZipDataHandler, and 
     * TarDataHandler. In the future we will implement to allow for a 
     * combination of the above cases.
     * 
     * @param  endPointInfo  the object provides ecogrid end point information
     * @param  preserveFormat when set to true, do not decompress or un-tar the entity
     * @return the DownloadHandler object which will download data for this
     *         entity
     */
	public DownloadHandler getDownloadHandler(
			EcogridEndPointInterface endPointInfo, boolean preserveFormat) {
		DownloadHandler handler = null;
		
		if (!preserveFormat) {
			if (hasZipDataFile) {
				handler = ZipDataHandler.getZipHandlerInstance(this, url,
						endPointInfo);
			}
			else if (hasGZipDataFile) {
				handler = GZipDataHandler.getGZipHandlerInstance(this, url,
						endPointInfo);
			}
			else if (hasTarDataFile) {
				handler = TarDataHandler.getTarHandlerInstance(this, url,
						endPointInfo);
			}
		}

		if (handler == null) {
			handler = DownloadHandler.getInstance(this, url, endPointInfo);
		}
		
		return handler;
	}
	
}
