/*
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.common;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

public class TestWebResponseFactory {

    private String message = "test message";
    private Throwable cause;
    
    @Before
    public void init() {
        cause = new IllegalArgumentException(message);
    }
    
    private void checkWithMessage(Response r, int status) {
        assertEquals(message, r.getEntity());
        assertEquals(status, r.getStatus());
    }
    
    
    @Test
    public void testMake() {
        Response r = WebResponseFactory.make(Response.Status.OK, message);
        checkWithMessage(r, 200);
    }
    
    @Test
    public void testMakeBadRequestWithMessage() {
        Response r = WebResponseFactory.makeBadRequest(message);
        checkWithMessage(r, 400);
    }
    
    @Test
    public void testMakeBadRequestWithCause() {
        Response r = WebResponseFactory.makeBadRequest(cause);
        checkWithMessage(r, 400);
    }
    
    @Test
    public void testMakeUnauthorizedWithMessage() {
        Response r = WebResponseFactory.makeUnauthorized(message);
        checkWithMessage(r, 401);
    }
    
    @Test
    public void testMakeUnauthorizedWithCause() {
        Response r = WebResponseFactory.makeUnauthorized(cause);
        checkWithMessage(r, 401);
    }
    
    @Test
    public void testMakeGoneWithCause() {
        Response r = WebResponseFactory.makeGone(cause);
        checkWithMessage(r, 410);
    }
    
    @Test
    public void testMakeNotFoundWithCause() {
        Response r = WebResponseFactory.makeNotFound(cause);
        checkWithMessage(r, 404);
    }
    
    @Test
    public void testMakeConflictWithCause() {
        Response r = WebResponseFactory.makeConflict(cause);
        checkWithMessage(r, 409);
    }

}
