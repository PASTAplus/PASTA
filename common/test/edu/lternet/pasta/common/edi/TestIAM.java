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

package edu.lternet.pasta.common.edi;

import org.junit.Before;
import org.junit.Test;

public class TestIAM {
    private IAM iam;
    private String ediToken;
    private String resourceId;
    private String permission;
    private String baseUrl = "https://localhost:5443";

    @Before
    public void setUp() {
        ediToken = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJFREktYjI3NTdmZWUxMjYzNGNjY2E0MGQyZDY4OWY1YzA1NDMiLCJjbiI6IlB1YmxpYyBBY2Nlc3MiLCJlbWFpbCI6bnVsbCwicHJpbmNpcGFscyI6W10sImlzRW1haWxFbmFibGVkIjpmYWxzZSwiaXNFbWFpbFZlcmlmaWVkIjpmYWxzZSwiaWRlbnRpdHlJZCI6LTEsImlkcE5hbWUiOiJVbmtub3duIiwiaWRwVWlkIjoiVW5rbm93biIsImlkcENuYW1lIjoiVW5rbm93biIsImlzcyI6Imh0dHBzOi8vYXV0aC5lZGlyZXBvc2l0b3J5Lm9yZyIsImhkIjoiZWRpcmVwb3NpdG9yeS5vcmciLCJpYXQiOjE3NTMzMDIzMzYsIm5iZiI6MTc1MzMwMjMzNiwiZXhwIjoxNzUzMzMxMTM2fQ.PD3Lv1tyZUaAUHbqOQpGXAsamTU8yts51-dJuL5t_-rqbJAVMoAsZnQFKwos6uDeaa_vA7n8CGObTJ498PEPUA";
    }

    @Test
    public void testIAMConstructor() {
        iam = new IAM("https", "localhost", 5443, ediToken);
        assert(iam.getBaseUrl().equals(baseUrl));
    }

    @Test
    public void testPing() {
        iam = new IAM("https", "localhost", 5443, ediToken);
        String response;
        try {
            response = iam.ping();
            assert response.equals("pong");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assert false;
        }
    }

    @Test
    public void testIsAuthorized() {
        iam = new IAM("https", "localhost", 5443, ediToken);

        boolean isAuthorized = false;

        try {
            isAuthorized = iam.isAuthorized("http://localhost:8088/package/metadata/eml/knb-lter-vcr/70/24", "READ");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            assert false;
        }

        assert isAuthorized;
    }
}
