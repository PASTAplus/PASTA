package edu.lternet.pasta.auditmanager;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * Servlet implementation class AuditCleanerServlet, a utility
 * servlet for cleaning up old audit record XML files from the
 * temporary directory in which they're created. This servlet
 * is typically invoked by a cron job.
 */
public class AuditCleanerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
	    .getLogger(AuditCleanerServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AuditCleanerServlet() {
		super();
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
		AuditCleaner ac = null;

		/*
		 *  'ttl' is the time-to-live, in number of minutes, for the audit record XML files
		 */
		String ttlString = request.getParameter("ttl"); 

		if (ttlString == null || ttlString.isEmpty()) {
			ttlString = "60"; // default to 60 minutes
		}
		
		// Convert minutes to milliseconds
		Long ttl = Long.valueOf(ttlString) * 60000L; 
		
		try {
			ac = new AuditCleaner();
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		ac.doClean(ttl);
	}

}
