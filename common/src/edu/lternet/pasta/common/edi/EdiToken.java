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

import java.util.Base64;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

/**
 * Provides interface to the EDI Token claims.
 */
public final class EdiToken {

    private final String token;
    private final String header;
    private final String payload;
    private final String signature;
    private final JSONObject jsonHeader;
    private final JSONObject jsonPayload;

    public EdiToken(String tokenBase64) {
        this.token = tokenBase64;

        String[] jwtParts = tokenBase64.split("\\.");
        if (jwtParts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }

        Base64.Decoder decoder = Base64.getUrlDecoder();

        byte[] header = decoder.decode(jwtParts[0]);
        this.header = new String(header, StandardCharsets.UTF_8);
        this.jsonHeader = new JSONObject(this.header);

        byte[] payload = decoder.decode(jwtParts[1]);
        this.payload = new String(payload, StandardCharsets.UTF_8);
        this.jsonPayload = new JSONObject(this.payload);

        byte[] signature = decoder.decode(jwtParts[2]);
        this.signature = new String(signature, StandardCharsets.UTF_8);

    }

    public String getTokenString() {
        return token;
    }

    public String getHeader() {
        return header;
    }

    public String getPayload() {
        return payload;
    }

    public String getSignature() {
        return  signature;
    }

    public String getSubject() {
        return jsonPayload.getString("sub");
    }

    public String getCommonName() {
        return jsonPayload.getString("cn");
    }
}
