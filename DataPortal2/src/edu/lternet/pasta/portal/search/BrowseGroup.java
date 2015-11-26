/*
 *
 * $Date: 2012-06-22 12:23:25 -0700 (Fri, 22 June 2012) $
 * $Author: dcosta $
 * $Revision: 2145 $
 *
 * Copyright 2011,2012 the University of New Mexico.
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
 *
 */

package edu.lternet.pasta.portal.search;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;



/**
 * A BrowseGroup holds a set of browse terms, or, a set of other BrowseGroup
 * objects. (Thus, this class has a recursive aspect to it.)
 * 
 */
public class BrowseGroup {
  
  /*
   * Class fields
   */

	  private static final Logger logger = Logger.getLogger(BrowseGroup.class);
	  
  
  /*
   * Instance fields
   */
  
  //List of browse groups
  protected ArrayList<BrowseGroup> browseGroups = null;   
  
  //List of browse terms                                        
  protected ArrayList<BrowseTerm> browseTerms = null;    
       
  //The name of this browse group, e.g. "Habitat"
  private final String value; 
  
  // The term ID in the LTER Controlled Vocabulary
  private String termId = null;
  
  protected int level = 0;
  
  private String hasMoreDown = null;
                                           
  protected String ITEM_EXPANDED = "false";


  /*
   * Constructors
   */
  
  	/**
  	 * This constructor is needed for the subclass to compile.
  	 * Without it the subclass has an error, "Implicit super constructor BrowseGroup() 
  	 * is undefined for default constructor. Must define an explicit constructor."
  	 */
	public BrowseGroup() {
		this(null);
	}


	public BrowseGroup(String s) {
		browseGroups = new ArrayList<BrowseGroup>();
		browseTerms = new ArrayList<BrowseTerm>();
		value = s;
	}


  /*
   * Class methods
   */
  
	public static BrowseGroup generateKeywordCache() {
		BrowseGroup controlledVocabulary = new BrowseGroup("Controlled Vocabulary");
		BrowseGroup lterSiteCache = generateLterSiteCache();
		controlledVocabulary.addBrowseGroup(lterSiteCache);

		try {
			String topTermsXML = ControlledVocabularyClient.webServiceFetchTopTerms();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputStream inputStream = IOUtils.toInputStream(topTermsXML, "UTF-8");
			Document document = documentBuilder.parse(inputStream);
			Element documentElement = document.getDocumentElement();
			NodeList documentNodeList = documentElement.getElementsByTagName("term");

			for (int i = 0; i < documentNodeList.getLength(); i++) {
				Node documentNode = documentNodeList.item(i);
				NodeList childNodes = documentNode.getChildNodes();
				String termId = null;
				String value = null;

				for (int j = 0; j < childNodes.getLength(); j++) {

					Node childNode = childNodes.item(j);
					if (childNode instanceof Element) {
						Element childElement = (Element) childNode;
						if (childElement.getTagName().equals("term_id")) {
							Text text = (Text) childElement.getFirstChild();
							termId = text.getData().trim();
						}
						else
							if (childElement.getTagName().equals("string")) {
								Text text = (Text) childElement.getFirstChild();
								value = text.getData().trim();
							}
					}
				}

				BrowseGroup topTerm = new BrowseGroup(value);
				controlledVocabulary.addBrowseGroup(topTerm);
				topTerm.setTermId(termId);
				topTerm.setHasMoreDown("1");
				topTerm.addFetchDownElements();
			}
		}
		catch (Exception e) {
			logger.error("Exception:\n" + e.getMessage());
			e.printStackTrace();
			/*
			 * By returning null, we let callers know that there was a problem
			 * refreshing the browse cache, so callers will know not to
			 * overwrite the previous results.
			 */
			controlledVocabulary = null;
		}

		return controlledVocabulary;
	}
    
    
	private void addFetchDownElements() {
		String fetchDownXML = ControlledVocabularyClient
				.webServiceFetchDown(this.termId);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			InputStream inputStream = IOUtils.toInputStream(fetchDownXML,
					"UTF-8");
			Document document = documentBuilder.parse(inputStream);
			Element documentElement = document.getDocumentElement();
			NodeList documentNodeList = documentElement
					.getElementsByTagName("term");

			for (int i = 0; i < documentNodeList.getLength(); i++) {
				Node documentNode = documentNodeList.item(i);
				NodeList childNodes = documentNode.getChildNodes();
				String termId = null;
				String value = null;
				String hasMoreDown = null;

				for (int j = 0; j < childNodes.getLength(); j++) {

					Node childNode = childNodes.item(j);
					if (childNode instanceof Element) {
						Element childElement = (Element) childNode;
						if (childElement.getTagName().equals("term_id")) {
							Text text = (Text) childElement.getFirstChild();
							termId = text.getData().trim();
						}
						else if (childElement.getTagName().equals("string")) {
								Text text = (Text) childElement.getFirstChild();
								value = text.getData().trim();
						}
						else if (childElement.getTagName().equals("hasMoreDown")) {
							Text text = (Text) childElement.getFirstChild();
							hasMoreDown = text.getData().trim();
					}
					}
				}

				if (hasMoreDown != null && hasMoreDown.equals("1")) {
				  BrowseGroup downTerm = new BrowseGroup(value);
				  this.addBrowseGroup(downTerm);
				  downTerm.setTermId(termId);
				  downTerm.setHasMoreDown(hasMoreDown);
				  downTerm.addFetchDownElements();
				  
				}
				else {
					BrowseTerm downTerm = new BrowseTerm(value);
					downTerm.setTermId(termId);
					this.addBrowseTerm(downTerm);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception:\n" + e.getMessage());
			e.printStackTrace();
		}
		
		if (!isBlacklistedTerm(this.value)) {
			BrowseTerm browseTerm = new BrowseTerm(this.value);
			browseTerm.setLevel(level + 1);
			this.addBrowseTerm(browseTerm);
		}
      
  }
  
  
	public static BrowseGroup generateLterSiteCache() {
		BrowseGroup topGroup = new LTERSiteBrowseGroup("LTER Sites");
		topGroup.setLevel(1);

		String[] lterSiteTerms = LTERSite.sites;

		for (int i = 0; i < lterSiteTerms.length; i++) {
			String term = lterSiteTerms[i];
			BrowseTerm browseTerm = new BrowseTerm(term);
			topGroup.addBrowseTerm(browseTerm);
		}

		return topGroup;
	}
    
    
	public static void main(String[] args) {
		String browseDir = "/home/pasta/local/browse";
		BrowseGroup controlledVocabulary = null;
		String xmlString = null;
		String htmlString = null;
		File browseCacheFile = null;

		controlledVocabulary = generateKeywordCache();
		
		if (controlledVocabulary != null) {
			xmlString = controlledVocabulary.toXML();
			htmlString = controlledVocabulary.toHTML();
			browseCacheFile = new File(String.format("%s/browseKeyword.xml",browseDir));
			try {
				FileUtils.writeStringToFile(browseCacheFile, xmlString);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			browseCacheFile = new File(String.format("%s/browseKeyword.html",
					browseDir));
			try {
				FileUtils.writeStringToFile(browseCacheFile, htmlString);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			System.err.println("Generation of keyword browse cache and HTML completed.");
		}
		else {
			System.err.println("Generation of keyword browse cache failed.");
		}
	}


  /*
   * Instance methods
   */
  
 
  /**
   * Adds a browse group to this browse group's list of browse groups.
   * 
   * @param browseGroup   the BrowseGroup object to be added
   */
  public void addBrowseGroup(BrowseGroup browseGroup) {
	browseGroup.setLevel(this.level + 1);
    browseGroups.add(browseGroup);
  }

  
  /**
   * Adds a browse term to this browse group's list of browse terms.
   * 
   * @param browseTerm    the BrowseTerm object to be added
   */
  public void addBrowseTerm(BrowseTerm browseTerm) {
    browseTerm.setLevel(this.level + 1);
    browseTerms.add(browseTerm);
  }
  
  
  /**
   * Get all browse terms that are descendants of this browse group.
   * 
   * @return   A list of all BrowseTerm objects that are descendants of this
   *           browse group.
   */
	public void getBrowseTerms(ArrayList<BrowseTerm> arrayList) {
		for (BrowseTerm browseTerm : getLocalBrowseTerms()) {
			arrayList.add(browseTerm);
		}
		for (BrowseGroup browseGroup : browseGroups) {
			browseGroup.getBrowseTerms(arrayList);
		}
	}


	/**
	 * Get local browse terms, i.e. at the same level of this browse group.
	 * 
	 * @return A list of all BrowseTerm objects that are descendants of this
	 *         browse group.
	 */
	public ArrayList<BrowseTerm> getLocalBrowseTerms() {
		ArrayList<BrowseTerm> arrayList = new ArrayList<BrowseTerm>();
		for (BrowseTerm browseTerm : browseTerms) {
			String value = browseTerm.getValue();
			if (!isBlacklistedTerm(value)) {
				arrayList.add(browseTerm);
			}
		}

		return arrayList;
	}


  /**
   * Getter method for 'hasMoreDown'.
   * 
   * @return  hasMoreDown, "1" indicates that
   *          more terms are below this one, "0"
   *          indicates that this is a leaf node term
   */
  public String getHasMoreDown() {
      if (hasMoreDown == null) {
    	  return "1";
      }
      else {
    	  return hasMoreDown;
      }
  }

  
  /**
   * Checks all the browse terms that are descendants of this browse group. If
   * one of them matches the browse value, returns the XML that is
   * stored in the browse cache for that browse value. Note that this method
   * will only be called on the top-most browse group, since it is guaranteed
   * to contain all the browse terms in the cache.
   * 
   * @param browseValue  The browse value, e.g. "bird".
   * @return resultSet   An XML string representing the search results for this browse value
   */
  public String getResultSet(String browseValue) {
    ArrayList<BrowseTerm> arrayList = new ArrayList<BrowseTerm>();
    getBrowseTerms(arrayList);
    String resultSet = "<?xml version=\'1.0\'?><resultset></resultset>\n";
    
    for (int i = 0; i < arrayList.size(); i++) {
      BrowseTerm browseTerm = arrayList.get(i);
      String browseTermValue = browseTerm.getValue();
      if (browseValue.equalsIgnoreCase(browseTermValue)) {
        resultSet = browseTerm.readSearchResults();
        break;
      }
    }
    
    return resultSet;
  }

  
  /**
   * Getter method for 'value'.
   * 
   * @return   value
   */
  public String getValue() {
    return value;
  }

  
  /**
   * Boolean to determine whether this browse group contains browse term
   * children.
   * 
   * @return  true if there are immediate browse terms, false if this browse
   *          group is a container for other browse groups
   */
  public boolean hasGroups() {
    return (browseGroups.size() > 0);
  }

  
  /**
   * Boolean to determine whether this browse group contains browse term
   * children.
   * 
   * @return  true if there are immediate browse terms, false if this browse
   *          group is a container for other browse groups
   */
  public boolean hasTerms() {
    return (browseTerms.size() > 0);
  }
  
  
  private boolean isBlacklistedTerm(String value) {
	  if (value.equals("LTER Sites")) return true;
	  return false;
  }

  
  /**
   * Counts the number of datasets matching the browse terms contained
   * within this browse group (whether directly or indirectly).
   * 
   * @return  matchCount, an int indicating the total number of dataset matches.
   */
  public int matchCount() {
    int matchCount = 0;
    
    if (hasTerms()) {
      for (int i = 0; i < browseTerms.size(); i++) {
        BrowseTerm browseTerm = browseTerms.get(i);
        matchCount += browseTerm.getMatchCount();
      }
    }
    else {
      for (int i = 0; i < browseGroups.size(); i++) {
        BrowseGroup browseGroup = browseGroups.get(i);
        matchCount += browseGroup.matchCount();
      }
    }
    
    return matchCount;
  }
  
  
  protected void setHasMoreDown(String s) {
	  this.hasMoreDown = s;
  }
  
 
  protected void setLevel(int n) {
	  this.level = n;
  }
  
 
  protected void setTermId(String id) {
	  this.termId = id;
  }
  
 
  /**
   * Convert this browse group into a HTML <table> used for displaying the name
   * of this browse group, e.g. "Habitat".
   * 
   * @return     a HTML string holding a <table> element
   */
	public String toHTML() {
		StringBuffer sb = new StringBuffer("");
        sb.append("<ul id='browseSearch'>\n");
		
		for (BrowseGroup browseGroup : browseGroups) {
			sb.append(browseGroup.outerHTML());
		}
		
		sb.append("</ul>\n");
		String htmlString = sb.toString();
		return htmlString;	
	}
	
	
	protected String outerHTML() {
		StringBuffer sb = new StringBuffer("");
		
		String innerHTML = innerHTML();
		
		sb.append(String.format("  <li class='browsegroup' item-expanded='%s'>%s\n", ITEM_EXPANDED, getValue()));
		sb.append("    <ul>\n");
        sb.append(String.format("%s\n", innerHTML));		
		sb.append("    </ul>\n");
		sb.append("  </li>\n");
		
		String htmlLevel1 = sb.toString();
	    return htmlLevel1;
	}

	
	private String innerHTML() {
		StringBuffer sb = new StringBuffer("");
		
		sb.append(termsHTML());

		for (BrowseGroup browseGroup : browseGroups) {
			sb.append(browseGroup.outerHTML());
		}
	    
		String innerHTML = sb.toString();
	    return innerHTML;
	}
	
	
	/**
	 * Create the HTML to display this browse term on the browse page. If this
	 * browse term matches at least one document, then display it as a link;
	 * otherwise, just display it as a text value.
	 * 
	 * @return htmlString, the HTML string to be displayed on the browse page.
	 */
	protected String browseTermHTML(BrowseTerm browseTerm) {
		return browseTerm.toHTML();
	}

	
	private String termsHTML() {
		StringBuffer sb = new StringBuffer("");

	    ArrayList<BrowseTerm> localBrowseTerms = getLocalBrowseTerms();
		for (BrowseTerm browseTerm : localBrowseTerms) {
			sb.append(String.format("    <li>%s</li>\n",
					                browseTermHTML(browseTerm)));
		}

		String htmlString = sb.toString();
		return htmlString;
	}


	private String indentXML() {
		int indent = level * 4;
		StringBuffer sb = new StringBuffer("");
		
		for (int i = 0; i < indent; i++) {
			sb.append(" ");
		}
		
		return sb.toString();
	}
 

	  /**
	   * Converts this browse group to a string. Used in writing out the browse
	   * cache to disk.
	   */
	  public String toString() {
	    String browseTermString;
	    String cacheString;
	    StringBuffer stringBuffer = new StringBuffer("");    
	    String xmlIndent = indentXML();
	    
	    stringBuffer.append(String.format("%s<group level='%d' hasMoreDown='%s'>\n", 
	    		                          xmlIndent, level, getHasMoreDown()));

	    stringBuffer.append(String.format("%s    <value>" + value + "</value>\n", xmlIndent));
	    for (int i = 0; i < browseGroups.size(); i++) {
	        BrowseGroup browseGroup = browseGroups.get(i);
	        stringBuffer.append(browseGroup.toString());
	    }
	    
	    if (hasTerms()) {
	      stringBuffer.append(String.format("%s    <terms>\n", xmlIndent));

	      ArrayList<BrowseTerm> arrayList = getLocalBrowseTerms();
	      for (BrowseTerm browseTerm : arrayList) {
	        browseTermString = browseTerm.toXML();
	        if (!browseTermString.equals("")) {
	          stringBuffer.append(browseTermString);
	        }
	      }
	      stringBuffer.append(String.format("%s    </terms>\n", xmlIndent));
	    }

	    stringBuffer.append(String.format("%s</group>\n", xmlIndent));
	    cacheString = stringBuffer.toString();
	    return cacheString;
	  }
	  
	  
	  /**
	   * Converts this browse group to an XML string. Used in writing out the browse
	   * cache to disk.
	   */
	  public String toXML() {
		    StringBuffer stringBuffer = new StringBuffer("");
		    stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		    stringBuffer.append("<browseCache>\n");
		    stringBuffer.append(this.toString());
		    stringBuffer.append("</browseCache>\n");
		    String xmlString = stringBuffer.toString();
		    
		    return xmlString;
	  }
	  
}
