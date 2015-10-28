/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.WebExceptionFactory;
import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;
import edu.lternet.pasta.eventmanager.EmlSubscription;
import edu.lternet.pasta.eventmanager.PostResponseHandler;
import edu.lternet.pasta.eventmanager.SubscriptionRegistry;


/**
 * @author dcosta
 * @version 1.0
 * @created 25-Jan-2012 10:40:03 AM
 * 
 * The EventManagerClient class interacts with the Event Manager web service as
 * a client to notify it of changes to data packages in PASTA.
 */
public class EventManagerClient extends PASTAServiceClient {

  /*
   * Class fields
   */
  
	private static Logger logger = Logger.getLogger(EventManagerClient.class);

	
  /*
   * Instance fields
   */
  

  /*
   * Constructors
   */
  

  /*
   * Class methods
   */
  
	public static void asynchronousNotify(EmlPackageIdFormat emlPackageIdFormat,
			EmlSubscription emlSubscription, EmlPackageId emlPackageId) {
		final int CONNECTION_TIMEOUT = 30000;
		final int REQUEST_TIMEOUT = 30000;
		AsyncHttpClient asyncHttpClient = makeClient(CONNECTION_TIMEOUT,
				REQUEST_TIMEOUT);

		PostResponseHandler postResponseHandler = new PostResponseHandler(
				emlSubscription);

		BoundRequestBuilder boundRequestBuilder = asyncHttpClient
				.preparePost(emlSubscription.getUrl().toString());

		boundRequestBuilder.setHeader("Content-Type", MediaType.TEXT_PLAIN);

		if ((emlPackageIdFormat != null) && (emlPackageId != null)) {
			String packageId = emlPackageIdFormat.format(emlPackageId);
			boundRequestBuilder.setBody(packageId);
		}

		try {
			boundRequestBuilder.execute(postResponseHandler);
		}
		catch (IOException e) {
			logger.error("Subscriber Notification Error", e);
		}
	}


	private static AsyncHttpClient makeClient(int connectionTimeout, int requestTimeout) {
		AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder()
				.setConnectionTimeoutInMs(connectionTimeout)
				.setRequestTimeoutInMs(requestTimeout).build();
		return new AsyncHttpClient(cf);
	}


  /*
   * Instance methods
   */
  
  /**
   * Notify the Event Manager of a change to a data package.
   * 
   * @param scope       The data package scope value, e.g. "knb-lter-lno"
   * @param identifier  The data package identifier value
   * @param revision    The data package revision value
   * @param user        The user
   * 
   */
	public void notifyEventManager(String scope, Integer identifier,
			Integer revision) throws ClassNotFoundException, SQLException {
		EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat(
				Delimiter.DOT);
		EmlPackageId emlPackageId = parseEmlPackageId(scope,
				identifier.toString(), revision.toString());
		SubscriptionRegistry subscriptionRegistry = new SubscriptionRegistry();
		List<EmlSubscription> emlSubscriptionList = subscriptionRegistry
				.getSubscriptions(emlPackageId);
		logger.info(String.format("Event notifications for packageId '%s':\n",
				emlPackageId.toString()));

		for (EmlSubscription emlSubscription : emlSubscriptionList) {
			asynchronousNotify(emlPackageIdFormat, emlSubscription,
					emlPackageId);
		}
	}


	private EmlPackageId parseEmlPackageId(String scope, String identifier,
			String revision) {
		EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat(
				Delimiter.FORWARD_SLASH);

		try {
			return emlPackageIdFormat.parse(scope, identifier, revision);
		}
		catch (IllegalArgumentException e) {
			throw WebExceptionFactory.makeBadRequest(e);
		}
	}

}