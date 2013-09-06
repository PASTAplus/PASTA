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

}
