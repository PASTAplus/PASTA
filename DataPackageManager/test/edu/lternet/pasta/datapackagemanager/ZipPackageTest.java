package edu.lternet.pasta.datapackagemanager;

import edu.lternet.pasta.common.security.token.AuthToken;
import edu.ucsb.nceas.utilities.Options;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

// import edu.lternet.pasta.datapackagemanager.ZipPackage;

public class ZipPackageTest {
  private static final String CONFIG_PATH = "WebRoot/WEB-INF/conf";
  private static final String testUser = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
  static AuthToken authToken;

  private static ConfigurationListener configurationListener = null;
  private static Options options = null;

  @BeforeClass
  public static void setUpClass()
  {
    configurationListener = new ConfigurationListener();
    configurationListener.initialize(CONFIG_PATH);
    // configurationListener = new ConfigurationListener();
    // configurationListener.initialize(dirPath);
    options = ConfigurationListener.getOptions();
    if (options == null) {
      Assert.fail("Failed to load DataPackageManager properties file");
    }
    // else {
    //   testPath = options.getOption("datapackagemanager.test.path");
    //   if (testPath == null) {
    //     fail("No value found for DataPackageManager property 'datapackagemanager.test.path'");
    //   }
    // }

    DummyCookieHttpHeaders httpHeadersUser = new DummyCookieHttpHeaders(testUser);
    authToken = DataPackageManagerResource.getAuthToken(httpHeadersUser);
    String userDN = authToken.getUserId();
    assertEquals(testUser, userDN);
  }

  @AfterClass
  public static void tearDownClass()
  {
    configurationListener = null;
    options = null;
    // testPath = null;
  }

  @Test
  public void testCreateZipPackage() throws Exception
  {
    getZipPackage();
  }

  @Test
  public void testCreate() throws Exception
  {
    ZipPackage zipPackage = getZipPackage();
		List<ZipMember> zipMemberList = zipPackage.create();
    // System.out.println(manifestStr);
  }

  private ZipPackage getZipPackage() throws Exception
  {
    DataPackageManager dataPackageManager = new DataPackageManager();
    return new ZipPackage(dataPackageManager, "knb-lter-nin", 1, 1,
        testUser, authToken
    );
  }

  private void initConfig()
  {
  }


}
