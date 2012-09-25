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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.eventmanager.EmlSubscription.SubscriptionBuilder;

/**
 * Used to test the subscription service from the command line.
 *
 */
public class SubscriptionTest {

    public static void main(String[] args) {

        String persistenceUnit = ConfigurationListener.getJUnitPersistenceUnit();

        EntityManagerFactory emf =
            Persistence.createEntityManagerFactory(persistenceUnit);
        EntityManager em = emf.createEntityManager();

        AuthToken token = AuthTokenFactory.makeAuthToken("blah");

        SubscriptionBuilder sb = new SubscriptionBuilder();
        sb.setCreator("user5");
        sb.setEmlPackageId(new EmlPackageId("test", 1, 2));
        sb.setUrl(new SubscribedUrl("http://test"));

        EmlSubscription s = sb.build();
        System.out.println("ID: " + s.getSubscriptionId());

        SubscriptionService ss = new SubscriptionService(em);
        ss.create(s, token);
        //ss.delete(s.getPackageId(), s.getUri());
        //ss.create(s);
        //ss.create("user1", "pk1", "http://localhost");
        //ss.create("user1", "pk2", "http://localhost");
        //ss.create("user2", "pk1", "http://localhost");
        //ss.create("user2", "pk3", "http://localhost");

        System.out.println("ID: " + s.getSubscriptionId());
        //System.out.println(ss.getAll());
        //System.out.println(ss.getWithPackageId("pk1", 1, 1));
        //System.out.println(ss.getWithUri("some uri"));
        //System.out.println(ss.getWithUserId("user2"));
        //System.out.println(ss.exists("pk1", "some uri"));
        //System.out.println(ss.exists("pk1", "http://localhost"));

        // ss.deleteWithUri("http://localhost");
        /*
        TestJpa jpa = new TestJpa();

        System.out.println("ID: " + jpa.getId());
        em.getTransaction().begin();
        em.persist(jpa);
        em.getTransaction().commit();

        System.out.println("ID: " + jpa.getId());
        */

        em.close();
        emf.close();
    }

}
