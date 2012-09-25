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

package edu.lternet.pasta.common.security.access.v1;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Used to represent an access matrix. An access matrix contains <em>allow</em>
 * and <em>deny</em> rules and an <em>order</em> for evaluating those rules,
 * where a <em>rule</em> is a <em>principal:permission</em> pair. If multiple
 * <em>allow</em> rules are specified for a principal, only the highest ranking
 * of the specified permissions is recorded. If multiple <em>deny</em> rules are
 * specified for a principal, only the lowest ranking of the specified
 * permissions is recorded.
 *
 * <p>
 * See <a href="doc-files/discuss_authorization.html" >this document</a> for
 * additional information.
 * </p>
 */
public final class AccessMatrix {

    /**
     * Defines the permissions that an access matrix can contain.
     */
    public enum Permission {

        READ(0), WRITE(1), CHANGE_PERMISSION(2);

        private final int rank;

        private Permission(int rank) {
            this.rank = rank;
        }

        /**
         * Indicates if this permission has a higher rank than the provided
         * permission, where {@code CHANGE_PERMISSION} &gt; {@code WRITE} &gt;
         * {@code READ}.
         *
         * @param p
         *            the permission whose rank is to be compared.
         *
         * @return {@code true} if the rank of this permission is higher than
         *         the rank of the provided permission; {@code false} otherwise.
         */
        public boolean isHigher(Permission p) {
            return rank > p.rank;
        }

        /**
         * Indicates if this permission is equal in rank or has a higher rank
         * than the provided permission, where {@code CHANGE_PERMISSION} &gt;
         * {@code WRITE} &gt; {@code READ}.
         *
         * @param p
         *            the permission whose rank is to be compared.
         *
         * @return {@code true} if the rank of this permission is equal to or
         *         higher than the rank of the provided permission; {@code
         *         false} otherwise.
         */
        public boolean isEqualOrHigher(Permission p) {
            return rank >= p.rank;
        }
    }

    /**
     * Defines the rule evaluation orders that an access matrix can have.
     */
    public enum Order {
        ALLOW_FIRST, DENY_FIRST;
    }

    /**
     * Defines the rule types that an access matrix can have.
     */
    public enum RuleType {
        ALLOW, DENY;
    }

    /**
     * Defines the possible authentication systems.
     */
    public enum AuthSystem {
        LTER_LDAP;
    }

    private final Order order;
    private final Map<String, Permission> allow;
    private final Map<String, Permission> deny;

    /**
     * Constructs a new access matrix with the provided rule evaluation order.
     * The authentication system is set to {@link AuthSystem#LTER_LDAP}.
     *
     * @param order
     *            the order of rule evaluation that should be used when
     *            evaluating this access matrix.
     *
     * @throws IllegalArgumentException
     *             if the provided order is {@code null}.
     */
    public AccessMatrix(Order order) {

        if (order == null) {
            throw new IllegalArgumentException("null order");
        }

        this.order = order;
        this.allow = new LinkedHashMap<String, Permission>();
        this.deny = new LinkedHashMap<String, Permission>();
    }

    /**
     * Returns the order of rule evaluation that should be applied for this
     * access matrix.
     *
     * @return the order of rule evaluation that should be applied for this
     *         access matrix.
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Returns the authentication system associated with this access matrix.
     *
     * @return the authentication system associated with this access matrix.
     */
    public AuthSystem getAuthSystem() {
        return AuthSystem.LTER_LDAP;
    }

    private void checkPrincipal(String principal) {
        if (principal == null || principal.isEmpty()) {
            throw new IllegalArgumentException("null or empty principal");
        }
    }

    private void checkPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("null permission");
        }
    }

    /**
     * Adds an <em>allow</em> rule with the provided principal and permission.
     * If this method is called multiple times with a particular principal, only
     * the highest ranking of the provided permissions is recorded.
     *
     * @param principal
     *            the principal to which the <em>allow</em> rule applies.
     * @param permission
     *            the permission of the rule.
     *
     * @throws IllegalArgumentException
     *             if the provided principal or permission are {@code null}, or
     *             if the provided principal is empty.
     */
    public void addAllowRule(String principal, Permission permission) {

        checkPrincipal(principal);
        checkPermission(permission);

        Permission currentHighest = allow.get(principal);

        if (currentHighest == null || permission.isHigher(currentHighest)) {
            allow.put(principal, permission);
        }
    }

    public void addRule(RuleType ruleType, String principal, Permission permission) {

        if (ruleType == null) {
            throw new IllegalArgumentException("null ruleType");
        }

        switch (ruleType) {
        case ALLOW:
            addAllowRule(principal, permission);
            break;

        case DENY:
            addDenyRule(principal, permission);
            break;

        default:
            String s = "invalid rule type: " + ruleType;
            throw new IllegalStateException(s);
        }

    }

    /**
     * Adds a <em>deny</em> rule with the provided principal and permission. If
     * this method is called multiple times with a particular principal, only
     * the lowest ranking of the provided permissions is recorded.
     *
     * @param principal
     *            the principal to which the <em>deny</em> rule applies.
     * @param permission
     *            the permission of the rule.
     *
     * @throws IllegalArgumentException
     *             if the provided principal or permission are {@code null}, or
     *             if the provided principal is empty.
     */
    public void addDenyRule(String principal, Permission permission) {

        checkPrincipal(principal);
        checkPermission(permission);

        Permission currentLowest = deny.get(principal);

        if (currentLowest == null || currentLowest.isHigher(permission)) {
            deny.put(principal, permission);
        }
    }

    /**
     * Returns the set of all <em>allow</em> rules in this access matrix. The
     * returned map is not "live", i.e. modifying it will not modify this access
     * matrix.
     *
     * @return all <em>allow</em> rules with principals as keys and permissions
     *         as values.
     *
     * @see #addAllowRule(String, Permission)
     */
    public Map<String, Permission> getAllowRules() {
        return getRules(RuleType.ALLOW);
    }

    /**
     * Returns the set of all <em>deny</em> rules in this access matrix. The
     * returned map is not "live", i.e. modifying it will not modify this
     * access matrix.
     *
     * @return all <em>deny</em> rules with principals as keys and permissions
     *         as values.
     *
     * @see #addDenyRule(String, Permission)
     */
    public Map<String, Permission> getDenyRules() {
        return getRules(RuleType.DENY);
    }

    /**
     * Returns the set of all rules in this access matrix of the specified type.
     * The returned map is not "live", i.e. modifying it will not modify this
     * access matrix.
     *
     * @param ruleType
     *            the type of rules to be returned.
     *
     * @return all rules of the specified type with principals as keys and
     *         permissions as values.
     */
    public Map<String, Permission> getRules(RuleType ruleType) {

        if (ruleType == null) {
            throw new IllegalArgumentException("null rule type");
        }

        // Making a defensive copy
        Map<String, Permission> copy = new LinkedHashMap<String, Permission>();

        if (ruleType == RuleType.ALLOW) {
            copy.putAll(allow);
        }

        else {
            copy.putAll(deny);
        }

        return copy;
    }

    /**
     * Indicates if this access matrix is equivalent to the one provided. This
     * determination is important if a protected resource contains its own
     * access control rule, and a user is allowed to {@code write} but not
     * {@code changePermission}. In such cases, some attempted {@code write}
     * operations must be denied because they involve modification of the access
     * control rule.
     *
     * @param accessMatrix
     *            the access matrix to be compared to this access matrix.
     *
     * @return {@code true} if both access matrices have the same evaluation
     *         order, authSystem, and <em>allow</em> and <em>deny</em> rules;
     *         {@code false} otherwise.
     */
    public boolean isEquivalent(AccessMatrix accessMatrix) {

        return order.equals(accessMatrix.order) &&
               allow.equals(accessMatrix.allow) &&
               deny.equals(accessMatrix.deny);
    }

}
