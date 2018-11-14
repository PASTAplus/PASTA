package edu.lternet.pasta.gatekeeper;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.IOException;


public class BotMatcherTest {

    private static ConfigurationListener configurationListener = null;
    private static final String dirPath = "WebRoot/WEB-INF/conf";
    private static Logger logger = Logger.getLogger(BotMatcherTest.class);
    private static String robotPatternsPath = null;
    
    private static final String[] nonRobotAgents = {"abcdef", "curl/"};
    private static final String[] robotAgents = {
    		"aria2/5", "bot", "com.plumanalytics", "Dispatch/767", "EZID link checker", "EZID-link-checker", "pastabot", "spiderman", "voyager/2"
    };

    /**
     * Initialize objects before any tests are run.
     */
	@BeforeClass
	public static void setUpClass() {
		configurationListener = new ConfigurationListener();
		configurationListener.initialize(dirPath);
		robotPatternsPath = String.format("%s/robotPatterns.txt", dirPath);

		try {
            BotMatcher.initializeRobotPatterns(robotPatternsPath);
        }
        catch (IOException e) {
            String msg = e.getMessage();
            logger.error(msg);
            fail("Failed to load robot patterns from " + robotPatternsPath);
        }
	}

	
    @Test
    public void testNonRobotAgents() {
    	for (String agent : nonRobotAgents) {
    		String userAgent = BotMatcher.findRobotAux(agent);
    		assertTrue(userAgent == null);
    	}
    }


    @Test
    public void testRobotAgents() {
    	for (String agent : robotAgents) {
    		String userAgent = BotMatcher.findRobotAux(agent);
    		assertTrue(userAgent != null);
    	}
    }

}
