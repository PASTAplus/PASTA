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

import org.apache.log4j.Logger;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Response;

import edu.lternet.pasta.eventmanager.NotificationLog.Outcome;

public class PostResponseHandler
    extends AsyncCompletionHandler<NotificationLog> {

    private static final Logger logger =
                                Logger.getLogger(PostResponseHandler.class);

    private final NotificationLog notificationLog;

    public PostResponseHandler(EmlSubscription subscription) {

        notificationLog = new NotificationLog();
        notificationLog.setSubscription(subscription);
        notificationLog.setRequestTime(new Date());
    }

    @Override
    public NotificationLog onCompleted(Response response) throws Exception {

        notificationLog.setOutcome(Outcome.RESPONSE_RECEIVED);

        if (response.hasResponseStatus()) {
            notificationLog.setStatusLine(response.getStatusText());
        }
        if (response.hasResponseHeaders()) {
            logHeaders(response);
        }
        if (response.hasResponseBody()) {
            notificationLog.setMessage(response.getResponseBody());
        }

        sendToAuditService(notificationLog);

        return notificationLog;
    }

    private void logHeaders(Response response) {

        StringBuffer sb = new StringBuffer();

        FluentCaseInsensitiveStringsMap m = response.getHeaders();

        for (String key : m.keySet()) {
            sb.append(key + ": ");
            sb.append(m.getJoinedValue(key, ","));
            sb.append("\n");
        }

        notificationLog.setHeaders(sb.toString());
    }

    @Override
    public void onThrowable(Throwable t) {

        StringBuilder errorMessage = new StringBuilder();

        errorMessage.append(t.toString());
        errorMessage.append('\n');

        for (StackTraceElement e : t.getStackTrace()) {
            errorMessage.append(e.toString());
            errorMessage.append('\n');
        }

        notificationLog.setOutcome(Outcome.ERROR);
        notificationLog.setMessage(errorMessage.toString());

        sendToAuditService(notificationLog);
    }

    private void sendToAuditService(NotificationLog log) {

        StringBuilder sb = new StringBuilder();

        sb.append("The Event Manager notified the subscription '");
        sb.append(log.getSubscription().getSubscriptionId());
        sb.append("' with response: ");
        sb.append(log.getStatusLine());

        logger.info(sb.toString());
    }
}
