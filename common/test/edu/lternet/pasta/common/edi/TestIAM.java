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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class TestIAM {
    private IAM iam;
    private String ediToken;
    private String resourceId;
    private String permission;
    private String baseUrl = "https://localhost:5443";

    @Test
    public void testIAMConstructor() {
        iam = new IAM("https", "localhost", 5443);
        assert(iam.getBaseUrl().equals(baseUrl));
    }

    @Test
    public void testCreateEdiToken() {
        iam = new IAM("https", "localhost", 5443);

        String publicId = "EDI-b2757fee12634ccca40d2d689f5c0543";
        String key = "2d69dda41af84bbe9b1ed4fba5479def";

        try {
            JSONObject newEdiToken = iam.createEdiToken(publicId, key);
            System.out.println(newEdiToken.getString("token"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            assert false;
        }
    }

    @Test
    public void testIsAuthorized() {
        String resourceKey = "http://localhost:8088/package/metadata/eml/knb-lter-vcr/70/24";
        String permission = "READ";
        String publicId = "EDI-b2757fee12634ccca40d2d689f5c0543";

        iam = new IAM("https", "localhost", 5443);

        String token = newToken(publicId);
        iam.setEdiToken(token);

        try {
            iam.isAuthorized(resourceKey, permission);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            assert false;
        }

    }

    private String newToken(String ediId) {
        iam = new IAM("https", "localhost", 5443);

        String key = "2d69dda41af84bbe9b1ed4fba5479def";
        String token = null;

        try {
            JSONObject newEdiToken = iam.createEdiToken(ediId, key);
            token = newEdiToken.getString("token");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return token;
    }

}
