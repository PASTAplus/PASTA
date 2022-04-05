package edu.lternet.pasta.datapackagemanager;

import edu.lternet.pasta.common.security.token.AuthToken;
import org.junit.Test;

public class PublicAuthTokenTest {

   @Test
   public void testMakePublicAuthToken() {
       AuthToken token = PublicAuthToken.makePublicAuthToken();
   }
}
