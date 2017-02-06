package edu.lternet.pasta.datapackagemanager;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.ucsb.nceas.utilities.Options;

/**
 * The WorkingOnServlet is responsible for detecting interrupted data packages
 * when the server starts up and marking them as interrupted. It calls the
 * WorkingOn method detectInterrupted() at initialization time to accomplish
 * this.
 */
public class WorkingOnServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(WorkingOnServlet.class);

	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WorkingOnServlet() {
		super();
	}

	
	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		Options options = ConfigurationListener.getOptions();
		
		String dbDriver = options.getOption("dbDriver");
		String dbURL = options.getOption("dbURL");
		String dbUser = options.getOption("dbUser");
		String dbPassword = options.getOption("dbPassword");

		try {
			WorkingOn workingOn = new WorkingOn(dbDriver, dbURL, dbUser, dbPassword);
			Map<String, String> active = workingOn.listActiveDataPackages();
			logger.info("The following data packages were still active at the time of last shutdown:");
			System.out.println("  Package ID       Start Date");
			for (String key : active.keySet()) {
				System.out.println(String.format("    %s  %s", key, active.get(key)));
			}
			workingOn.detectInterrupted();
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

}
