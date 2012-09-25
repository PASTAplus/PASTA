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

import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import edu.lternet.pasta.common.security.token.AuthToken;

import pasta.pasta_lternet_edu.log_entry_0.CategoryType;
import pasta.pasta_lternet_edu.log_entry_0.LogEntry;

public final class LogEntryFactory
{

    private static final int LOWEST_WARN = 400;
    private static final int LOWEST_ERROR = 500;

    private LogEntryFactory() {}

    static public LogEntry make(String service, String serviceMethod,
                                AuthToken token, Response response,
                                String resourceId, String eventText) {

        if (response == null) throw new IllegalStateException("response is null.");
        CategoryType category = categoryFromResponse(response);
        return make(category, service, serviceMethod, token, response.getStatus(), resourceId, eventText);
    }

    static public LogEntry make(CategoryType category, String service,
                                String serviceMethod, AuthToken token,
                                Integer status, String resourceId,
                                String eventText) {

        LogEntry le = new LogEntry();

        if (category == null) throw new IllegalStateException("category is null.");
        le.setCategory(category);

        if (service == null || service.isEmpty()) {
            throw new IllegalStateException("service is null.");
        }
        le.setService(service);

        if (eventText != null && !eventText.isEmpty()) {
            le.setEventText(eventText);
        }

        if (serviceMethod != null && !serviceMethod.isEmpty()) {
            le.setServiceMethod(serviceMethod);
        }

        if (status != null) {
            le.setHttpStatusCode(new BigInteger(status.toString()));
        }
        
        if (resourceId != null) {
            le.setResourceId(resourceId);
        }
        if (token != null) {
            le.setUserToken(token.getTokenString());
        }

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date;
        try {
            date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        }
        catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Invalid date attempted to be set.");
        }
        le.setTimestamp(date);

        return le;        
    }

    static public LogEntry makeDebug(String service, String serviceMethod,
                                     AuthToken token, Response response,
                                     String resourceId, String eventText) {
        LogEntry le = make(service, serviceMethod, token, response,
                           resourceId, eventText);
        le.setCategory(CategoryType.DEBUG);
        return le;
    }

    static public LogEntry makeDebug(String service, String serviceMethod,
                                     AuthToken token, String resourceId,
                                     String eventText) {
        Response r = Response.ok().build();
        LogEntry le =
                make(service, serviceMethod, token, r, resourceId, eventText);
        le.setHttpStatusCode(null);
        le.setCategory(CategoryType.DEBUG);
        return le;
    }

    static private CategoryType categoryFromResponse(Response response) {
        int status = response.getStatus();
        if (status < LOWEST_WARN) {
            return CategoryType.INFO;
        }
        else if (status < LOWEST_ERROR) {
            return CategoryType.WARN;
        }
        return CategoryType.ERROR;
    }

}
