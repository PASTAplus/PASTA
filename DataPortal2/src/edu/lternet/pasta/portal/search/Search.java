package edu.lternet.pasta.portal.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.util.ClientUtils;

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
	  protected final static String DEFAULT_FIELDS = "id,packageid,title,author,organization,pubdate,coordinates";
	  public final static int DEFAULT_START = 0;
	  public final static int DEFAULT_ROWS = 10;
	  protected final static String DEFAULT_DEBUG = "false";
	  
	  // Sort values
	  public final static String CREATORS_SORT = "responsibleParties";
	  public final static String PACKAGEID_SORT = "packageid";
	  public final static String PUBDATE_SORT = "pubdate";
	  public final static String SCORE_SORT = "score";
	  public final static String TITLE_SORT = "titles";
	  public final static String SORT_ORDER_ASC = "asc";
	  public final static String SORT_ORDER_DESC = "desc";
	  public final static String DEFAULT_SORT_ORDER = SORT_ORDER_DESC;
	  public final static String DEFAULT_SORT = String.format("%s,%s", SCORE_SORT, SORT_ORDER_DESC);
	  
	  
	  /*
	   * Instance variables
	   */
	  
	  protected TermsList termsList;
	  
	  
	  /*
	   * Constructors
	   */
	  
	  public Search () {
		  this.termsList = new TermsList();
	  }
	  
	  
	/*
	 * Class methods
	 */
	  
	  /**
	   * Calls the ClientUtils.escapeQueryChars method but does some
	   * post-processing by removing escaped spaces and double-quotes. 
	   * This helps support phrase queries such as "coral reef".
	   * 
	   * @param queryString   The query string
	   * @return escapedString, The escaped query string
	   */
	  public static String escapeQueryChars(String queryString) {
		  String escapedString = ClientUtils.escapeQueryChars(queryString);
		  if (escapedString != null) {
			  escapedString = escapedString.replace("\\ ", " "); // don't escape spaces
			  escapedString = escapedString.replace("\\\"", "\""); // don't escape double quotes
			  escapedString = escapedString.replace("\\(", "("); // don't escape parens
			  escapedString = escapedString.replace("\\)", ")"); // don't escape parens
		  }
		  return escapedString;
	  }
	  

	/**
	 * Adds a string to a list of terms. An auxiliary method to the
	 * parseTermsAdvanced() method.
	 * 
	 * @param terms
	 *            list of term strings.
	 * @param term
	 *            the new string to add to the list, but only if it isn't an
	 *            empty string.
	 */
	private static void addTerm(List<String> terms, final String term) {
		if (term != null) {
			final String s = term.trim();

			if (s.length() > 0) {
				terms.add(s);
			}
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
		boolean keepWhitespace = false;
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
				if (keepWhitespace) {
					String phraseTerm = String.format("\"%s\"", currentTerm.toString());
					addTerm(terms, phraseTerm);
					currentTerm = new StringBuffer();
				}

				keepWhitespace = !(keepWhitespace); // Toggle keepWhitespace
			}
			else
				if (Character.isWhitespace(c)) {
					// If we are inside a quote-enclosed term, append the whitespace char.
					if (keepWhitespace) {
						currentTerm.append(c);
					}
					// Else, add the current term to the list and start a new term.
					else {
						addTerm(terms, currentTerm.toString());
						currentTerm = new StringBuffer();
					}
				}
				else {
					// Append any non-quote, non-space characters to the current term.
					currentTerm.append(c);
				}
		}

		// Add the final term to the list.
		addTerm(terms, currentTerm.toString());

		return terms;
	}
	
	
	/*
	 * Instance methods
	 */
	
	/**
	 * Accessor method for termsList
	 * 
	 * @return termsList
	 */
	public TermsList getTermsList() {
		return termsList;
	}

	
	/**
	 * Wraps parens around a query value if it needs them.
	 * 
	 * For example, the two Solr queries below have different meanings:
	 * 
	 * author:jones bug     matches authors named "jones" or text containing "bug"
	 *                      because the second term is matched against the default
	 *                      field (df), which in our case is the text field
	 * author:(jones bug)   matches authors named "jones" or authors named "bug"
	 * 
	 * @return termsList
	 */
	public String parenthesizeQueryValue(String value) {
		String parenthesizedValue = value.contains(" ") ? String.format("(%s)", value) : value;
		return parenthesizedValue;
	}

}
