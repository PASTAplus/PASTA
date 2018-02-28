package edu.lternet.pasta.gatekeeper;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class LdapsConnectorTest {

    private static Logger logger = Logger.getLogger(LdapsConnectorTest.class);


    private static final String LTER_LDAP = "ldap.lternet.edu";
    private static String lterDn = null;
    private static String lterDnPasswd = null;

    private static final String EDI_LDAP = "ldap.edirepository.org";
    private static String ediDn = null;
    private static String ediDnPasswd = null;

    private static LdapsConnector ldaps = null;

    @BeforeClass
    public static void setUpClass() {
        ConfigurationListener configurationListener = new ConfigurationListener();
        String cwd = System.getProperty("user.dir");
        String configDir = cwd + "/WebRoot/WEB-INF/conf/";
        configurationListener.initialize(configDir);
        lterDn = ConfigurationListener.getLterDn();
        lterDnPasswd = ConfigurationListener.getLterDnPasswd();
        ediDn = ConfigurationListener.getEdiDn();
        ediDnPasswd = ConfigurationListener.getEdiDnPasswd();
    }


    @After
    public void tearDownTest() {
        if (ldaps != null) {
            ldaps.closeConn();
        }
    }

    @Test
    public void testLterLdapsConnection() {
        try {
            ldaps = new LdapsConnector(LTER_LDAP);
        }
        catch (IllegalStateException e) {
            String msg = String.format("%s: %s", getClass().getSimpleName(), e.getMessage());
            logger.error(msg);
            fail("Failed LDAPS connection to " + LTER_LDAP);
        }
    }

    @Test
    public void testLterLdapsBind() {
        try {
            ldaps = new LdapsConnector(LTER_LDAP);
            ResultCode result = ldaps.bind(lterDn, lterDnPasswd);
            assertEquals(result, ResultCode.SUCCESS);
        }
        catch (IllegalStateException e) {
            logger.error(e.getMessage());
            fail("Failed to create an LDAPS connection to " + LTER_LDAP);
        } catch (LDAPException e) {
            logger.error(e.getMessage());
            fail("Failed on bind exception for user " + lterDn);
        }
    }

    @Test
    public void testIsLterLdapsAuthenticated() {
        try {
            ldaps = new LdapsConnector(LTER_LDAP);
            Boolean isAuthenticated = ldaps.authenticateDn(lterDn, lterDnPasswd);
            assertEquals(isAuthenticated, Boolean.TRUE);
        }
        catch (IllegalStateException e) {
            logger.error(e.getMessage());
            fail("Failed to create an LDAPS connection to " + LTER_LDAP);
        } catch (LDAPException e) {
            logger.error(e.getMessage());
            fail("Failed on authenticate exception for user " + lterDn);
        }
    }

    @Test
    public void testEdiLdapsConnection() {
        try {
            ldaps = new LdapsConnector(EDI_LDAP);
        }
        catch (IllegalStateException e) {
            String msg = String.format("%s: %s", getClass().getSimpleName(), e.getMessage());
            logger.error(msg);
            fail("Failed LDAPS connection to " + EDI_LDAP);
        }
    }

    @Test
    public void testEdiLdapsBind() {
        try {
            ldaps = new LdapsConnector(EDI_LDAP);
            ResultCode result = ldaps.bind(ediDn, ediDnPasswd);
            assertEquals(result, ResultCode.SUCCESS);
        }
        catch (IllegalStateException e) {
            logger.error(e.getMessage());
            fail("Failed to create an LDAPS connection to " + EDI_LDAP);
        } catch (LDAPException e) {
            logger.error(e.getMessage());
            fail("Failed on bind exception for user " + ediDn);
        }
    }
    @Test
    public void testIsEdiLdapsAuthenticated() {
        try {
            ldaps = new LdapsConnector(EDI_LDAP);
            Boolean isAuthenticated = ldaps.authenticateDn(ediDn, ediDnPasswd);
            assertEquals(isAuthenticated, Boolean.TRUE);
        }
        catch (IllegalStateException e) {
            logger.error(e.getMessage());
            fail("Failed to create an LDAPS connection to " + EDI_LDAP);
        } catch (LDAPException e) {
            logger.error(e.getMessage());
            fail("Failed on authenticate exception for user " + ediDn);
        }
    }

}
