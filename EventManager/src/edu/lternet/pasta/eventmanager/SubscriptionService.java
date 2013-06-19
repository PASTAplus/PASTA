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

import java.util.ArrayList;
import java.util.List;

import edu.lternet.pasta.common.ResourceDeletedException;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.security.access.AccessControllerFactory;
import edu.lternet.pasta.common.security.access.AuthTokenAccessController;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.eventmanager.EmlSubscription.JpqlFactory;
import edu.lternet.pasta.eventmanager.EmlSubscription.SubscriptionBuilder;

/**
 * An event subscription service class. This class performs all database
 * operations for the EML subscription database
 */
public final class SubscriptionService {

    private static AuthTokenAccessController getAccessController() {
        return AccessControllerFactory.getDefaultAuthTokenAccessController();
    }

    /**
     * Creates a new subscription in the database if it does not already exist
     * and if the provided user credentials allow it. A subscription is
     * considered to already exist if its creator, packageId, and URI match a
     * subscription contained in the database.
     *
     * @param subscription
     *            the subscription to be created.
     * @param token
     *            the requesting user's credentials.
     *            
     * @return the subscriptionId of the subscription created
     *
     * @throws IllegalArgumentException
     *             if the provided subscription is inactive or contains a
     *             non-null ID.
     *
     * @throws UnauthorizedException
     *             if the user is not authorized to create subscriptions.
     *
     * @throws ResourceExistsException
     *             if a subscription already exists in the database with the
     *             provided creator, packageId, and URI.
     */
    public Integer create(EmlSubscription subscription, AuthToken token) {

        if (!subscription.isActive()) {
            String s = "inactive subscription: " + subscription;
            throw new IllegalArgumentException(s);
        }

        if (subscription.getSubscriptionId() != null) {
            String s = "subscription already persisted: " + subscription;
            throw new IllegalArgumentException(s);
        }

        SubscriptionBuilder query = subscription.toBuilder();
        String jpql = JpqlFactory.makeWithPackageIdNulls(query);
        List<EmlSubscription> matches = getMatches(jpql);

        if (!matches.isEmpty()) {
            String s = "An event subscription already exists with the " +
                       "provided creator, packageId, and URI. Its ID is " +
                       matches.get(0).getSubscriptionId() + ".";
            throw new ResourceExistsException(s);
        }

        /*entityManager.getTransaction().begin();
        entityManager.persist(subscription);
        entityManager.getTransaction().commit();*/
        
        Integer subscriptionId = subscription.getSubscriptionId();
        return subscriptionId;
    }

    
    /**
     * Indicates if a subscription exists in the database with the provided ID.
     * @param subscriptionId a potential subscription ID.
     * @return {@code true} if the subscription exists; {@code false} otherwise.
     *
     * @throws IllegalArgumentException if the provided ID is {@code null}.
     */
    public boolean exists(Integer subscriptionId) {

        try {
            get(subscriptionId);
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (ResourceDeletedException e) {
            return false;
        }

        return true;
    }

    /**
     * Returns the subscription with the provided ID, if the user specified in
     * the provided token is authorized to read it.
     *
     * @param subscriptionId the ID of the subscription to be returned.
     * @param token the requesting user's credentials.
     *
     * @return the subscription with the provided ID, if the user specified in
     * the provided token is authorized to read it.
     *
     * @throws IllegalArgumentException if the provided ID is {@code null}.
     *
     * @throws ResourceNotFoundException if a subscription with the provided ID
     * has never existed in the database.
     *
     * @throws ResourceDeletedException if the subscription with the provided ID
     * has been previously deleted from the database.
     *
     * @throws UnauthorizedException
     *
     * @throws UnauthorizedException
     *             if the user specified in the provided token is not authorized
     *             to read subscriptions or to read the specified
     *             subscription according to the coarse- and fine-grained access
     *             control rules.
     *
     */
    public EmlSubscription get(Integer subscriptionId, AuthToken token) {

        EmlSubscription s = get(subscriptionId);

        assertReadAuthorized(s, token);

        return s;
    }

    private EmlSubscription get(Integer subscriptionId) {

        if (subscriptionId == null) {
            throw new IllegalArgumentException("null subscription id");
        }

        EmlSubscription s = null;
        /*EmlSubscription s = entityManager.find(EmlSubscription.class,
                                               subscriptionId); */

        if (s == null) {
            String err = "An event subscription with the specified ID '" +
                         subscriptionId + "' does not exist.";
            throw new ResourceNotFoundException(err);
        }

        if (!s.isActive()) {
            String err = "The event subscription with the ID '" +
                         subscriptionId + "' was previously deleted.";
            throw new ResourceDeletedException(err);
        }

        return s;
    }

    /**
     * Queries the database based on the content of the provided subscription
     * builder. {@code null} attributes are not included in queries, except
     * those contained in the EML packageId, if it is not {@code null}.
     *
     * @param sb
     *            contains the content of the query to be performed.
     *
     * @return a list of subscriptions matching the content of the provided
     *         subscription builder.
     *
     */
    public List<EmlSubscription>
            getWithPackageIdNulls(SubscriptionBuilder sb) {

        String jpql = JpqlFactory.makeWithPackageIdNulls(sb);
        return getMatches(jpql);
    }

    /**
     * Queries the database based on the content of the provided subscription
     * builder. {@code null} attributes are not included in queries, including
     * those contained in the EML packageId, if it is not {@code null}.
     *
     * @param sb
     *            contains the content of the query to be performed.
     * @param token
     *            the requesting user's credentials.
     *
     * @return a list of subscriptions matching the content of the provided
     *         subscription builder for which the requesting user is authorized
     *         to read.
     *
     * @throws UnauthorizedException
     *             if the user specified in the provided token is not authorized
     *             to read subscriptions according to the coarse-grained access
     *             control rule.
     */
    public List<EmlSubscription>
            getWithoutPackageIdNulls(SubscriptionBuilder sb, AuthToken token) {

        String jpql = JpqlFactory.makeWithoutPackageIdNulls(sb);

        return getMatches(jpql, token);
    }

    private List<EmlSubscription> getMatches(String jpql, AuthToken token) {

        List<EmlSubscription> matches = getMatches(jpql);

        List<EmlSubscription> authorized =
            new ArrayList<EmlSubscription>(matches.size());

        for (EmlSubscription s : matches) {
            if (readAuthorized(s, token)) {
                authorized.add(s);
            }
        }

        return authorized;
    }

    
    private List<EmlSubscription> getMatches(String jpql) {
        //return makeQuery(jpql).getResultList();
    	return null;
    }

    
    /*private TypedQuery<EmlSubscription> makeQuery(String jpql) {
        return entityManager.createQuery(jpql, EmlSubscription.class);
    }*/

    
    /**
     * Deletes the subscription with the provided ID, if the user specified in
     * the provided token is authorized to delete it.
     *
     * @param subscriptionId
     *            the ID of the subscription to be deleted.
     * @param token
     *            the requesting user's credentials.
     *
     * @throws IllegalArgumentException
     *             if the provided ID is {@code null}.
     *
     * @throws ResourceNotFoundException
     *             if a subscription with the provided ID has never existed in
     *             the database.
     *
     * @throws ResourceDeletedException
     *             if the subscription with the provided ID has been previously
     *             deleted from the database.
     *
     * @throws UnauthorizedException
     *             if the user specified in the provided token is not authorized
     *             to delete subscriptions or to delete the specified
     *             subscription according to the coarse- and fine-grained access
     *             control rules.
     *
     */
    public void delete(Integer subscriptionId, AuthToken token) {

        EmlSubscription subscription = get(subscriptionId);

        assertDeleteAuthorized(subscription, token);

        subscription.inactivate();

        /*entityManager.getTransaction().begin();
        entityManager.merge(subscription);
        entityManager.getTransaction().commit();*/
    }

    private void assertReadAuthorized(EmlSubscription s, AuthToken token) {

        if (readAuthorized(s, token)) {
            return;
        }

        String err = "The user '" + token.getUserId() +
                     "' is not authorized to read the subscription with ID '"
                     + s.getSubscriptionId() + "'.";
        throw new UnauthorizedException(err);
    }

    private void assertDeleteAuthorized(EmlSubscription s, AuthToken token) {

        if (deleteAuthorized(s, token)) {
            return;
        }

        String err = "The user '" + token.getUserId() +
                     "' is not authorized to delete the subscription with ID '"
                     + s.getSubscriptionId() + "'.";
        throw new UnauthorizedException(err);
    }

    private boolean readAuthorized(EmlSubscription subscription,
                                   AuthToken token) {

        AuthTokenAccessController controller = getAccessController();
        String submitter = subscription.getCreator();

        return controller.canRead(token, null, submitter);
    }

    private boolean deleteAuthorized(EmlSubscription subscription,
                                     AuthToken token) {

        AuthTokenAccessController controller = getAccessController();
        String submitter = subscription.getCreator();

        return controller.canWrite(token, null, submitter);
    }

}
