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

package edu.lternet.pasta.eventmanager;

import java.util.Date;

public class NotificationLog {

    public enum Outcome {

        RESPONSE_RECEIVED("RESPONSE_RECEIVED"), ERROR("ERROR");

        private final String name;

        private Outcome(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    private EmlSubscription subscription;
    private Date requestTime;
    private Outcome outcome;
    private int statusCode;
    private String statusLine;
    private String headers;
    private String message;

    public EmlSubscription getSubscription() {
        return subscription;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public String getHeaders() {
        return headers;
    }

    /**
     * If a response was received, the response body is returned. If an
     * exception was thrown, its message is returned.
     *
     * @return a string indicating the message.
     */
    public String getMessage() {
        return message;
    }

    public void setSubscription(EmlSubscription subscription) {
        this.subscription = subscription;
    }

    public void setRequestTime(Date requestTime) {
        if (requestTime == null) {
            throw new NullPointerException("Null requestTime.");
        }
        this.requestTime = requestTime;
    }

    public void setOutcome(Outcome outcome) {
        if (outcome == null) {
            throw new NullPointerException("Null outcome.");
        }
        this.outcome = outcome;
    }

    public void setStatusLine(String statusLine) {
        if (statusLine == null) {
            throw new NullPointerException("Null statusLine.");
        }
        this.statusLine = statusLine;
    }

    public void setHeaders(String headers) {
        if (headers == null) {
            throw new NullPointerException("Null headers.");
        }
        this.headers = headers;
    }

    public void setMessage(String message) {
        if (message == null) {
            throw new NullPointerException("Null message.");
        }
        this.message = message;
    }

}
