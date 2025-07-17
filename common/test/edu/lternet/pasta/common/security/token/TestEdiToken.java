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

package edu.lternet.pasta.common.security.token;

import org.junit.Before;
import org.junit.Test;

public class TestEdiToken {

private String token = null;
private String subj = null;
private String iss = null;
private String principals = null;
    
    @Before
    public void init() {
        token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJFREktMzEyZTkwZjI5ZmJlNGUzZGE3MWY1MjFlZmJmMWRlMDYiLCJjbiI6IkVESSIsImVtYWlsIjpudWxsLCJwcmluY2lwYWxzIjpbIkVESS1iMjc1N2ZlZTEyNjM0Y2NjYTQwZDJkNjg5ZjVjMDU0MyIsIkVESS1kM2ZjYTk3NjdhYzU0YzIyOTYyODk2YjAxYTFjMDFiZCJdLCJpc0VtYWlsRW5hYmxlZCI6ZmFsc2UsImlzRW1haWxWZXJpZmllZCI6ZmFsc2UsImlkZW50aXR5SWQiOjMsImlkcE5hbWUiOiJsZGFwIiwiaWRwVWlkIjoidWlkPUVESSxvPUVESSxkYz1lZGlyZXBvc2l0b3J5LGRjPW9yZyIsImlkcENuYW1lIjoiRURJIiwiaXNzIjoiaHR0cHM6Ly9hdXRoLmVkaXJlcG9zaXRvcnkub3JnIiwiaGQiOiJlZGlyZXBvc2l0b3J5Lm9yZyIsImlhdCI6MTc1MjUwNzY5MiwibmJmIjoxNzUyNTA3NjkyLCJleHAiOjE3NTI1MzY0OTJ9.syUA6NedZvSSjYakGWtq5qo4U1H8LzX-R7w0MC5p2fZE8usCqrgu-ig0kLnrYRscBQWrOcwE8E7bbp6yNPTqbQ";
        subj = "EDI-312e90f29fbe4e3da71f521efbf1de06";
        iss = "https://auth.edirepository.org";
        principals = "[\"EDI-b2757fee12634ccca40d2d689f5c0543\",\"EDI-d3fca9767ac54c22962896b01a1c01bd\"]";
    }
    
    @Test
    public void testDecodeToken() {
        EdiToken ediToken = new EdiToken(token);

        assert(ediToken.getTokenString().equals(token));
        assert(ediToken.getSubject().equals(subj));
        assert(ediToken.getIssuer().equals(iss));
        assert(ediToken.getPrincipals().equals(principals));
    }
}
