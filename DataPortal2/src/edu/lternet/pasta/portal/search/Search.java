package edu.lternet.pasta.portal.search;

import java.util.ArrayList;
import java.util.List;

/**
 * The Search class contains methods that are common to all search classes,
 * such as SimpleSearch and AdvancedSearch.
 * 
 * @author dcosta
 *
 */
public class Search {
	
	/*
	 * Class variables
	 */
	
	  protected final static String DEFAULT_DEFTYPE = "edismax";
	  protected final static String DEFAULT_Q_STRING = "*:*";
	  protected final static String ECOTRENDS_FILTER = "-scope:ecotrends";
	  protected final static String LANDSAT_FILTER = "-scope:lter-landsat*";
	  protected final static String DEFAULT_FIELDS = "packageid,title,author,organization,pubdate";
	  protected final static int DEFAULT_START = 0;
	  public final static int DEFAULT_ROWS = 10;
	  protected final static String SCORE_SORT = "score,desc";
	  protected final static String PACKAGEID_SORT = "packageid,asc";
	  protected final static String DEFAULT_DEBUG = "true";
	  
	  
	/*
	 * Class methods
	 */
	  
	  /**
	   * Return a list of indexed paths for use in either simple search,
	   * browse search, or subject-based advanced search. The exact contents
	   * of the list returned varies depending on whether we want to include
	   * EML fields for abstracts, keywords, packageIds, and/or titles.
	   * 
	   * @param abstracts   true if we want to search EML abstract fields
	   * @param keywords    true if we want to search EML keyword fields
	   * @param packageIds  true if we want to search EML packageId fields
	   * @param titles      true if we want to search EML title fields
	   * @return a list of indexed fields
	   */
	  protected static List<String> getIndexedPaths(boolean abstracts, boolean keywords, 
			                                  boolean packageIds, boolean titles) {
		  List<String> indexedPaths = new ArrayList<String>();
		  
		  if (abstracts) {
			  indexedPaths.add("dataset/abstract");
			  indexedPaths.add("dataset/abstract/para");
			  indexedPaths.add("dataset/abstract/section/para");
		  }
		  
		  if (keywords) {
			  indexedPaths.add("keyword");
		  }
		  
		  if (packageIds) {
			  indexedPaths.add("@packageId");
		  }
		  
		  if (titles) {
			  indexedPaths.add("dataset/title");
		  }
		  
		  return indexedPaths;		  
	  }

	
	  /**
	   * Adds a string to a list of terms. An auxiliary method to the
	   * parseTermsAdvanced() method.
	   * 
	   * @param terms list of term strings.
	   * @param term  the new string to add to the list, but only if
	   *              it isn't an empty string.
	   */
	  private static void addTerm(List<String> terms, final StringBuffer term) {
	    final String s = term.toString().trim();
	      
	    if (s.length() > 0) {
	      terms.add(s);
	    }
	  }
	  
	  
	/**
	 * Parses search terms from a string. Double-quoted strings that contain
	 * spaces are parsed as a single term.
	 * 
	 * @param value
	 *            The string value as entered by the user.
	 * 
	 * @return terms An ArrayList of String objects. Each string is a term.
	 */
	protected static List<String> parseTerms(String value) {
		char c;
		StringBuffer currentTerm = new StringBuffer();
		boolean keepSpaces = false;
		final int stringLength;
		List<String> terms = new ArrayList<String>();

		value = value.trim();
		stringLength = value.length();

		for (int i = 0; i < stringLength; i++) {
			c = value.charAt(i);

			if (c == '\"') {
				/* Termination of a quote-enclosed term. Add the current term to
				 * list and start a new term.
				 */
				if (keepSpaces) {
					addTerm(terms, currentTerm);
					currentTerm = new StringBuffer();
				}

				keepSpaces = !(keepSpaces); // Toggle keepSpaces
			}
			else
				if (c == ' ') {
					// If we are inside a quote-enclosed term, append the space.
					if (keepSpaces) {
						currentTerm.append(c);
					}
					// Else, add the current term to the list and start a new term.
					else {
						addTerm(terms, currentTerm);
						currentTerm = new StringBuffer();
					}
				}
				else {
					// Append any non-quote, non-space characters to the current term.
					currentTerm.append(c);
				}
		}

		// Add the final term to the list.
		addTerm(terms, currentTerm);

		return terms;
	}

	
	/*
	 * Instance methods
	 */
	
}
