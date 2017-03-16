package edu.lternet.pasta.gatekeeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


public class BotMatcher {
	
    private static Logger logger = Logger.getLogger(BotMatcher.class);
	private static ArrayList<Pattern> regexPatterns = new ArrayList<Pattern>(300);
	
	public static void initializeRobotPatterns(String path) 
		throws IOException 
	
	{
		File regexFile = new File(path);
		
		if (regexFile.exists()) {
			try (FileInputStream fis = new FileInputStream(regexFile)) {
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					Pattern p = Pattern.compile(line.trim(), Pattern.CASE_INSENSITIVE);
					regexPatterns.add(p);
		        }
				logger.info(String.format("Loaded %d robot patterns.", 
						    regexPatterns.size()));
			}
			catch (IOException e) {
				System.err.println("Error opening file: " + path);
				throw(e);
			}
		}
	}
	
	
	public static String findRobot(HttpServletRequest httpServletRequest) {
		String robot = null;

		final String headerName = "User-Agent";
		Enumeration<?> values = httpServletRequest.getHeaders(headerName);

		if (values != null) {
			while (values.hasMoreElements()) {
				String value = (String) values.nextElement();
				for (Pattern botPattern : regexPatterns) {
					/*System.err.println(String.format("Checking User-Agent '%s' against bot pattern '%s'",
							                         value, botPattern.pattern()));*/
					if (botPattern.matcher(value).matches()) {
						return value;
					}
				}
			}
		}

		return robot;
	}
	
}
