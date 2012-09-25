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
import static org.junit.Assert.assertSame;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.WebExceptionFactory;

public class TestWebExceptionFactory {

    private String message = "test message";
    private WebApplicationException e;
    private Throwable cause;
    
    @Before
    public void init() {
        cause = new IllegalArgumentException(message);
    }
    
    private void checkWithMessage(WebApplicationException e, int status) {
        assertEquals(message, e.getResponse().getEntity());
        assertEquals(status, e.getResponse().getStatus());
    }
    
    private void checkWithCause(WebApplicationException e, int status) {
        checkWithMessage(e, status);
        assertEquals(cause, e.getCause());
        assertSame(cause, e.getCause());
    }
    
    @Test
    public void testMakeBadRequestWithMessage() {
        e = WebExceptionFactory.makeBadRequest(message);
        checkWithMessage(e, 400);
    }
    
    @Test
    public void testMakeBadRequestWithCause() {
        e = WebExceptionFactory.makeBadRequest(cause);
        checkWithCause(e, 400);
    }
    
    @Test
    public void testMakeUnauthorizedWithMessage() {
        e = WebExceptionFactory.makeUnauthorized(message);
        checkWithMessage(e, 401);
    }
    
    @Test
    public void testMakeUnauthorizedWithCause() {
        e = WebExceptionFactory.makeUnauthorized(cause);
        checkWithCause(e, 401);
    }
    
    @Test
    public void testMakeGoneWithCause() {
        e = WebExceptionFactory.makeGone(cause);
        checkWithCause(e, 410);
    }
    
    @Test
    public void testMakeNotFoundWithCause() {
        e = WebExceptionFactory.makeNotFound(cause);
        checkWithCause(e, 404);
    }
    
    @Test
    public void testMakeConflictWithCause() {
        e = WebExceptionFactory.makeConflict(cause);
        checkWithCause(e, 409);
    }
}
