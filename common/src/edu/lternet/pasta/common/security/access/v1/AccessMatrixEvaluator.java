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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import edu.lternet.pasta.common.security.access.v1.AccessMatrix.Order;
import edu.lternet.pasta.common.security.access.v1.AccessMatrix.Permission;
import edu.lternet.pasta.common.security.access.v1.AccessMatrix.RuleType;
import edu.lternet.pasta.common.security.token.AuthToken;

/**
 * <p>
 * Used to evaluate access matrices as described in <a
 * href="doc-files/discuss_authorization.html">this document</a>.
 * </p>
 *
 */
public final class AccessMatrixEvaluator  {

    /**
     * Indicates if the user with the provided attributes has permission to
     * perform the {@code read} operation.
     *
     * @param token
     *            the token containing the user's attributes.
     * @param matrix
     *            the access matrix to be used for determining permissions.
     *
     * @return {@code true} if the access matrix allows the specified user to
     *         {@code read}; {@code false} if the user is denied.
     */
    public boolean canRead(AuthToken token, AccessMatrix matrix) {
        return canDoRequest(token, matrix, Permission.READ);
    }

    /**
     * Indicates if the user with the provided attributes has permission to
     * perform the {@code write} operation.
     *
     * @param token
     *            the token containing the user's attributes.
     * @param matrix
     *            the access matrix to be used for determining permissions.
     *
     * @return {@code true} if the access matrix allows the specified user to
     *         {@code write}; {@code false} if the user is denied.
     */
    public boolean canWrite(AuthToken token, AccessMatrix matrix) {
        return canDoRequest(token, matrix, Permission.WRITE);
    }

    /**
     * Indicates if the user with the provided attributes has permission to
     * perform the {@code change permission} operation.
     *
     * @param token
     *            the token containing the user's attributes.
     * @param matrix
     *            the access matrix to be used for determining permissions.
     *
     * @return {@code true} if the access matrix allows the specified user to
     *         {@code change permission}; {@code false} if the user is denied.
     */
    public boolean canChangePermission(AuthToken token, AccessMatrix matrix) {
        return canDoRequest(token, matrix, Permission.CHANGE_PERMISSION);
    }

    /**
     * Indicates if the user with the provided attributes has permission to
     * perform {@code all of the above} operations.
     *
     * @param token
     *            the token containing the user's attributes.
     * @param matrix
     *            the access matrix to be used for determining permissions.
     *
     * @return {@code true} if the access matrix allows the specified user to
     *         perform {@code all of the above}; {@code false} if the user is
     *         denied.
     */
    public boolean canAll(AuthToken token, AccessMatrix matrix) {
        return canDoRequest(token, matrix, Permission.CHANGE_PERMISSION);
    }

    private static final Set<String> PUBLIC;

    static {
        PUBLIC = Collections.singleton("public");
    }

    private boolean canDoRequest(AuthToken token,
                                 AccessMatrix matrix,
                                 Permission action) {

        RuleType ruleType = RuleType.ALLOW;
        Map<String, Permission> rules = matrix.getRules(ruleType);

        // Dealing with "denyFirst"
        if (matrix.getOrder().equals(Order.DENY_FIRST)) {
            return canDoRequest(token, rules, ruleType, action) ||
                    canDoRequest(PUBLIC, rules, ruleType, action);
        }

        // Step a
        if (canDoRequest(token, rules, ruleType, action)) {

            // Step b
            ruleType = RuleType.DENY;
            rules = matrix.getRules(ruleType);

            if (canDoRequest(token, rules, ruleType, action)) {
                return true;
            }

            // Step c
            return canPublicDoRequest(matrix, action);
        }

        // Step c
        return canPublicDoRequest(matrix, action);
    }

    private boolean canPublicDoRequest(AccessMatrix matrix,
                                       Permission action) {

        RuleType ruleType = RuleType.ALLOW;
        Map<String, Permission> rules = matrix.getRules(ruleType);

        // Step a
        if (canDoRequest(PUBLIC, rules, ruleType, action)) {

            // Step b
            ruleType = RuleType.DENY;
            rules = matrix.getRules(ruleType);

            return canDoRequest(PUBLIC, rules, ruleType, action);
        }

        return false;
    }

    private boolean canDoRequest(AuthToken token,
                                  Map<String, Permission> rules,
                                  RuleType ruleType,
                                  Permission request) {

        Set<String> principals = new TreeSet<String>();

        principals.add(token.getUserId());
        principals.addAll(token.getGroups());

        return canDoRequest(principals, rules, ruleType, request);
    }

    private boolean canDoRequest(Set<String> principals,
                                  Map<String, Permission> rules,
                                  RuleType ruleType,
                                  Permission requestedAction) {

        for (Entry<String, Permission> entry : rules.entrySet()) {

            String principalFromRule = entry.getKey();
            Permission permissionFromRule = entry.getValue();

            // TODO: This equality test must be improved
            if (principals.contains(principalFromRule)) {

                switch(ruleType) {

                case ALLOW: {

                    if (permissionFromRule.isEqualOrHigher(requestedAction)) {
                        return true;
                    }
                    break;
                }

                case DENY: {

                    if (!permissionFromRule.isHigher(requestedAction)) {
                        return false;
                    }
                    break;
                }

                default: throw new IllegalStateException("invalid rule type");

                }

            }
        }

        // The principals were not explicitly allowed, therefore
        // they are denied.
        if (ruleType == RuleType.ALLOW) {
            return false;
        }

        // At least one of the principals was first allowed and not explicitly
        // denied, therefore a principal is allowed, because deny rules are
        // only used herein to "undo" allow rules.
        return true;
    }

}
