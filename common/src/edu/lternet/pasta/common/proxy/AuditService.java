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

package edu.lternet.pasta.common.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import pasta.pasta_lternet_edu.log_entry_0.LogEntry;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import edu.lternet.pasta.common.WebResponseFactory;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;

/**
 * Used to interact with PASTA's Audit Service via web service calls.
 *
 */
public final class AuditService
{

    private AuditService() {
        // preventing instantiation
    }

    /**
     * The key used in {@code PastaUrls.properties} to define the Audit
     * Manager's root URL.
     */
    public static final String URL_KEY = "audit.service.url";
    private static List<Thread> serviceList = new ArrayList<Thread>();

    /**
     * Returns the root URL used to interact with PASTA's Audit Service, such
     * as {@code https://audit.lternet.edu/audit/}.
     *
     * @return the root URL used to interact with PASTA's Audit Service.
     */
    public static String getRootUrl() {
        return PastaUrls.getUrl(URL_KEY);
    }

    /**
     * Submits a Log Entry to PASTA's Audit Service.
     *
     * @param entry
     *            the LogEntry to submit.
     *
     * @param token
     *            the authorization credentials used during submission.
     */
    public static void log(final LogEntry entry, final AuthToken token) {

        AuditService a = new AuditService();
        a.logAsRunnable(entry, token);
        
    }

    private void logAsRunnable(LogEntry entry, AuthToken token) {

        String url = getRootUrl();
        Builder request = makeRequest(url, entry, token);
        
        LogThread lt = new LogThread(request);
        Thread t = new Thread(lt);
        t.start();
        serviceList.add(t);
    }

    private final class LogThread implements Runnable {
        private final WebResource.Builder request;

        private LogThread(WebResource.Builder request) {
            this.request = request;
        }

        @Override
        public void run() {
            ClientResponse response = null;

            try {
                response = request.post(ClientResponse.class);
            }
            catch (Throwable t) {
                String s = "An exception was thrown while notifying PASTA's " +
                           "Audit Manager. The stack trace is shown below.";

                Response effect = WebResponseFactory.makeExpectationFailed(s, t);

                throw new JerseyProxyException(t, effect);
            }
            // If the request succeeded
            StatusType status = response.getClientResponseStatus();

            if (status == ClientResponse.Status.CREATED) {
                return;
            }

            // If the response is unexpected
            BufferedClientResponse<String> bufferedResponse =
                    new BufferedClientResponse<String>(response, String.class);

            String s = "A request was sent to PASTA's Audit Manager." +
                       "The response from the Audit Manager is shown below.";

            Response effect =
                    WebResponseFactory.makeExpectationFailed(s, bufferedResponse);

            throw new JerseyProxyException(bufferedResponse, effect);
        }

    }

    private Builder makeRequest(String url, LogEntry entry, AuthToken token) {

        // Building the request
        //WebResource r = ApacheHttpClient.create().resource(url);
        WebResource r = Client.create().resource(url);

        WebResource.Builder b = r.accept(MediaType.APPLICATION_XML_TYPE,
                                         MediaType.TEXT_PLAIN_TYPE);
        b.entity(entry);

        // Ignore incoming token and create new AuthToken as user PASTA
				Set<String> s = new TreeSet<String>();
				s.add("authenticated");
				token = AuthTokenFactory.makeCookieAuthToken("pasta", AuthSystemDef.KNB,
				    2000000000, s);
		
				return AuthTokenFactory.addToken(token, b);
    }

    public static synchronized void joinAll() {

        // join all threads
        try {
            Iterator<Thread> it = serviceList.listIterator();
            while (it.hasNext()) {
                Thread t = it.next();
                t.join();
                it.remove();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
