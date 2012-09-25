/* 
 * $Date$ 
 * $Author$ 
 * $Revision$
 * 
 * Copyright 2010 the University of New Mexico.
 * 
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.common.proxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;

public class TestBufferedClientResponse {

    private ClientResponse response;
    private BufferedClientResponse<String> buffered;
    
    private ClientResponse makeResponse() {
        String url = "http://www.lternet.edu";
        return Client.create().resource(url).get(ClientResponse.class);
    }
    
    @Test
    public void testJerseyGetEntityOnce() {
        response = makeResponse();
        response.getEntity(String.class);
    }
    
    @Test(expected=ClientHandlerException.class)
    public void testJerseyGetEntityTwice() {
        response = makeResponse();
        response.getEntity(String.class);
        response.getEntity(String.class);
    }
    
    @Test
    public void testJerseyGetEntityOnceWithBuffering() {
        response = makeResponse();
        response.bufferEntity();
        response.getEntity(String.class);
    }
    
    @Test
    public void testJerseyGetEntityTwiceWithBuffering() {
        response = makeResponse();
        response.bufferEntity();
        assertFalse(response.getEntity(String.class).isEmpty());
        assertTrue(response.getEntity(String.class).isEmpty());
    }
    
    @Test
    public void testConstructor() {
        response = makeResponse();
        buffered = new BufferedClientResponse<String>(response, String.class);
    }
    
    @Test(expected=ClientHandlerException.class)
    public void testConstructorAfterCallingJerseysGetEntity() {
        response = makeResponse();
        response.getEntity(String.class);
        buffered = new BufferedClientResponse<String>(response, String.class);
    }
    
    @Test
    public void testGetEntityTwice() {
        response = makeResponse();
        buffered = new BufferedClientResponse<String>(response, String.class);
        assertFalse(buffered.getEntity().isEmpty());
        assertFalse(buffered.getEntity().isEmpty());
    }
}
