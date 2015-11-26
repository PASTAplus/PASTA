package edu.lternet.pasta.portal.search;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;

import edu.lternet.pasta.portal.ConfigurationListener;

/**
 * Servlet implementation class BrowseKrawlerServlet
 */
public class BrowseKrawlerServlet extends HttpServlet {
	
	/*
	 * Class variables
	 */
	private static final long serialVersionUID = 1L;
	
	/*
	 * Instance variables
	 */
	
	private String browseDir = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BrowseKrawlerServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		PropertiesConfiguration options = ConfigurationListener.getOptions();
		browseDir = options.getString("browse.dir");

		if ((browseDir == null) || (browseDir.equals(""))) {
			throw new ServletException(
			    "No value defined for 'browse.dir' property.");
		}
		
	}

	/**
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	
	/**
	 * 
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		ServletContext servletContext = httpSession.getServletContext();
		BrowseSearch.setBrowseCacheDir(browseDir);
		BrowseCrawler browseCrawler = new BrowseCrawler();
		BrowseGroup browseCache = null;
		
		browseCache = browseCrawler.crawlKeywordTerms();
		if (browseCache != null) {
			servletContext.setAttribute("browseKeywordHTML", browseCache.toHTML());
		}
	}

}
