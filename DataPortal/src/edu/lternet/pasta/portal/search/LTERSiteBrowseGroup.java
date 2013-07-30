package edu.lternet.pasta.portal.search;

import java.util.ArrayList;

public class LTERSiteBrowseGroup extends BrowseGroup {

	public LTERSiteBrowseGroup(String s) {
		super(s);
	}


	/**
	 * Get local browse terms, i.e. at the same level of this browse group.
	 * Overrides method in the parent class.
	 * 
	 * @return A list of all BrowseTerm objects that are descendants of this
	 *         browse group.
	 */
	public ArrayList<BrowseTerm> getLocalBrowseTerms() {
		ArrayList<BrowseTerm> arrayList = new ArrayList<BrowseTerm>();

		for (BrowseTerm browseTerm : browseTerms) {
			arrayList.add(browseTerm);
		}

		return arrayList;
	}


	  /**
	   * Convert this browse group into a HTML <table> used for displaying the name
	   * of this browse group, e.g. "Habitat".
	   * 
	   * @return     a HTML string holding a <table> element
	   */
		public String toHTML() {
			int indent = calculateIndent();
			StringBuffer sb = new StringBuffer("");
			
			for (BrowseGroup browseGroup : browseGroups) {
				sb.append(browseGroup.htmlLevel1());
			}
			
			String htmlString = sb.toString();
			return htmlString;	
		}
		
		
	protected String htmlLevel1() {
		String html = null;
		StringBuffer sb = new StringBuffer("");

		sb.append("<div class='searchsubcat'>\n");
		sb.append("<ul>\n");

		for (BrowseTerm browseTerm : browseTerms) {
			String termHTML = String.format("  <li>%s</li>\n",
					browseTermHTML(browseTerm));
			sb.append(termHTML);
		}

		sb.append("</ul>\n");
		sb.append("</div>\n");
		html = sb.toString();
		return html;
	}


	/**
	 * Create the HTML to display this browse term on the browse page. If this
	 * browse term matches at least one document, then display it as a link;
	 * otherwise, just display it as a text value.
	 * 
	 * @return htmlString, the HTML string to be displayed on the browse page.
	 */
	public String browseTermHTML(BrowseTerm browseTerm) {
		String htmlString;
		StringBuffer stringBuffer = new StringBuffer("");
		String value = browseTerm.getValue();
		String siteName = new LTERSite(value).getSiteName();
		int matchCount = browseTerm.getMatchCount();

		if (matchCount > 0) {
			stringBuffer
					.append(String
							.format("<a href=\'./browseServlet?searchValue=%s&amp;type=ltersite'",
									value));
			stringBuffer.append(" class=\"searchsubcat\">");
			stringBuffer.append(siteName);
			stringBuffer.append(" (" + matchCount + ")");
			stringBuffer.append("</a>");
		}
		else {
			stringBuffer.append(siteName);
		}

		htmlString = stringBuffer.toString();
		return htmlString;
	}

}
