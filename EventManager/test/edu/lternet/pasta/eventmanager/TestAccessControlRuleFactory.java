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

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class TestAccessControlRuleFactory {

    private static final String NOTIFY_OF_EVENT = "notifyOfEvent";
    private static final String EXECUTE_SUBSCRIPTION = "executeSubscription";
    private static final String CREATE_SUBSCRIPTION = "createSubscription";
    private static final String GET_SUBSCRIPTION_WITH_ID = "getSubscriptionWithId";
    private static final String GET_MATCHING_SUBSCRIPTIONS = "getMatchingSubscriptions";
    private static final String DELETE_SUBSCRIPTION = "deleteSubscription";

    @Before
    public void init() {
        new ConfigurationListener().setContextSpecificProperties();
    }
    
    @Test
    public void testGetServiceAcr() {
        assertNotNull(AccessControlRuleFactory.getServiceAcr(NOTIFY_OF_EVENT));
        assertNotNull(AccessControlRuleFactory.getServiceAcr(EXECUTE_SUBSCRIPTION));
        assertNotNull(AccessControlRuleFactory.getServiceAcr(CREATE_SUBSCRIPTION));
        assertNotNull(AccessControlRuleFactory.getServiceAcr(GET_SUBSCRIPTION_WITH_ID));
        assertNotNull(AccessControlRuleFactory.getServiceAcr(GET_MATCHING_SUBSCRIPTIONS));
        assertNotNull(AccessControlRuleFactory.getServiceAcr(DELETE_SUBSCRIPTION));
    }
}
