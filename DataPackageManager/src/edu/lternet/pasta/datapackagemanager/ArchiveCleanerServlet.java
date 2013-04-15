package edu.lternet.pasta.datapackagemanager;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class ArchiveCleanerServlet
 */
public class ArchiveCleanerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
	    .getLogger(ArchiveCleanerServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ArchiveCleanerServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		String ttlString = request.getParameter("ttl");

		if (ttlString != null && !ttlString.isEmpty()) {

			Long ttl = Long.valueOf(ttlString) * 60000L; // Convert minutes to
																									 // milliseconds

			ArchiveCleaner ac = null;

			try {
				ac = new ArchiveCleaner();
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

			ac.doClean(ttl);

		}

	}

}
