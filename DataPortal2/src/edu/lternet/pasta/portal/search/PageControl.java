package edu.lternet.pasta.portal.search;


public class PageControl {
	
	/*
	 * Class variables
	 */
	
	
	/*
	 * Instance variables
	 */
	
	int currentPage = 1;
	int numberFound;	
	int rowsPerPage;
	
	/*
	 * Constructor
	 */
	
	PageControl(int numberFound, int rowsPerPage) {
		this.numberFound = numberFound;
		this.rowsPerPage = rowsPerPage;
	}
	
	
	/*
	 * Class methods
	 */
	
	
	public static void main(String[] args) {
		int numberFound = new Integer(args[0]);
		int rowsPerPage = new Integer(args[1]);
		int currentPage = new Integer(args[2]);
		
		PageControl pageControl = new PageControl(numberFound, rowsPerPage);
		pageControl.setCurrentPage(currentPage);
		String html = pageControl.composeHTML();
		System.out.println(html);
	}
	
	
	/*
	 * Intance methods
	 */
	
	public String composeHTML() {
		String html = null;
		StringBuilder sb = new StringBuilder("");
		
		int max = highestPage();
		for (int i = 1; i <= max; i++) {
			String boldStartTag = "<b>";
			String boldEndTag = "</b>";
			if (i == currentPage) {
				boldStartTag = "";
				boldEndTag = "";
			}
			sb.append(String.format("<a href='#'>%s%d%s</a> ", boldStartTag, i, boldEndTag));
		}
		
		html = sb.toString();		
		return html;
	}

	
	public int getRecordsFound() {
		return numberFound;
	}

	
	public int getRecordsPerPage() {
		return rowsPerPage;
	}

	
	public int getCurrentPage() {
		return currentPage;
	}
	
	
	public int getStartRow() {
		int startRow = 0;
		
		startRow = (currentPage - 1) * rowsPerPage;
		
		return startRow;
	}
	
	
	public int highestPage() {
		int highestPage = 0;
		
		highestPage = numberFound / rowsPerPage;
		if (numberFound % rowsPerPage > 0) highestPage++;
		
		return highestPage;
	}

	
	public void setRecordsFound(int recordsFound) {
		this.numberFound = recordsFound;
	}

	
	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

	
	public int setCurrentPage(int newPage) {
		int returnValue = -1;
		
		if ((newPage > 0) && (newPage <= highestPage())) {
			this.currentPage = newPage;
			returnValue = newPage;
		}
		
		return returnValue;
	}

}
