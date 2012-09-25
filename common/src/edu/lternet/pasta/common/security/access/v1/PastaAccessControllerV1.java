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

import javax.xml.bind.JAXBElement;

import edu.lternet.pasta.common.PastaAccessUtility;
import edu.lternet.pasta.common.security.access.AbstractAuthTokenAccessController;
import edu.lternet.pasta.common.security.access.v1.AccessMatrix.Order;
import edu.lternet.pasta.common.security.access.v1.AccessMatrix.Permission;
import edu.lternet.pasta.common.security.access.v1.AccessMatrix.RuleType;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.validate.Validator.ValidatorResults;
import pasta.pasta_lternet_edu.access_0.AccessRule;
import pasta.pasta_lternet_edu.access_0.AccessType;

/**
 * Used to perform access control according to the algorithm described in <a
 * href="doc-files/discuss_authorization.html">this document</a>. If the user in
 * the authToken is the submitter of the requested resource or the user "pasta",
 * all methods associated with authorization will return {@code true}. If that
 * is not the case, the provided {@code <access>} element string is 1) parsed
 * into a JAXB {@link PastaAccess} object, 2) validated using a
 * {@link PastaAccessTypeValidator}, 3) used to construct an {@link AccessMatrix},
 * and 4) evaluated using an {@link AccessMatrixEvaluator}.
 *
 */
public final class PastaAccessControllerV1 extends AbstractAuthTokenAccessController {

    /**
     * The identifier of the pseudo-user "pasta".
     */
    public static final String PASTA =
        "uid=pasta,o=lter,dc=ecoinformatics,dc=org";

    private final PastaAccessTypeValidator validator;
    private final AccessMatrixEvaluator evaluator;

    public PastaAccessControllerV1() {
        super();
        validator = new PastaAccessTypeValidator();
        evaluator = new AccessMatrixEvaluator();
    }

    /**
     * Returns the {@code <access>} element validator used by this object.
     *
     * @return the {@code <access>} element validator used by this object.
     */
    public PastaAccessElementValidator getAcrValidator() {
        return new PastaAccessElementValidator(false);
    }

    /**
     * Returns the access matrix evaluator used by this object.
     *
     * @return the access matrix evaluator used by this object.
     */
    public AccessMatrixEvaluator getAccessMatrixEvaluator() {
        return evaluator;
    }

    /**
     * Returns an access matrix that corresponds to the provided {@code
     * <access>} element string.
     *
     * @param access0_1
     *            a string that is parsable as an Pasta Access 0.1 {@code <access>}
     *            element.
     * @return an access matrix that corresponds to the provided {@code
     *         <access>} element string.
     *
     * @see PastaAccessUtility#getPastaAccess_0(String)
     */
    public AccessMatrix makeAccessMatrix(String access0_1) {

        AccessType access = PastaAccessUtility.getPastaAccess_0(access0_1);

        return makeAccessMatrix(access);
    }

    /**
     * Returns an access matrix that corresponds to the provided JAXB {@code
     * <access>} element.
     *
     * @param access
     *            a JAXB representation of an {@code <access>} element.
     * @return an access matrix that corresponds to the provided JAXB {@code
     *         <access>} element.
     *
     * @throws IllegalArgumentException
     *             if the provided {@code <access>} element does not pass
     *             validation.
     *
     * @see #getValidator()
     */
    public AccessMatrix makeAccessMatrix(AccessType access) {

        ValidatorResults<AccessType> results = validator.validate(access);

        if (!results.isValid()) {
            String s = "The provided <access> element did not pass validation.";
            throw new IllegalArgumentException(s);
        }

        AccessType canonical = results.getCanonicalEntity();

        AccessMatrix matrix = makeEmptyAccessMatrix(canonical.getOrder());

        for (JAXBElement<AccessRule> jaxb : canonical.getAllowOrDeny()) {
            addRules(matrix, getRuleType(jaxb), jaxb.getValue());
        }

        return matrix;
    }

    private RuleType getRuleType(JAXBElement<AccessRule> jaxb) {

        String elementName = jaxb.getName().getLocalPart();

        if (elementName.equalsIgnoreCase(PastaAccessTypeValidator.ALLOW)) {
            return RuleType.ALLOW;
        }
        else if (elementName.equalsIgnoreCase(PastaAccessTypeValidator.DENY)) {
            return RuleType.DENY;
        }
        else {
            String s = "Invalid element name: " + elementName;
            throw new IllegalStateException(s);
        }

    }

    private void addRules(AccessMatrix matrix,
                          RuleType ruleType,
                          AccessRule accessRule) {

        for (String principal : accessRule.getPrincipal()) {
            for (String permission : accessRule.getPermission()) {
                addRule(matrix, ruleType, principal, permission);
            }
        }
    }

    private void addRule(AccessMatrix matrix,
                         RuleType ruleType,
                         String principal,
                         String permission) {

        String read = PermissionStringValidator.READ;
        String write = PermissionStringValidator.WRITE;
        String changePermission = PermissionStringValidator.CHANGE_PERMISSION;
        String all = PermissionStringValidator.ALL;

        if (permission.equals(read)) {
            matrix.addRule(ruleType, principal, Permission.READ);
        }
        else if (permission.equals(write)) {
            matrix.addRule(ruleType, principal, Permission.WRITE);
        }
        else if (permission.equals(changePermission)) {
            matrix.addRule(ruleType, principal, Permission.CHANGE_PERMISSION);
        }
        else if (permission.equals(all)) {
            matrix.addRule(ruleType, principal, Permission.READ);
            matrix.addRule(ruleType, principal, Permission.WRITE);
            matrix.addRule(ruleType, principal, Permission.CHANGE_PERMISSION);
        }
        else {
            String s = "invalid permission: " + permission;
            throw new IllegalStateException(s);
        }

    }

    private AccessMatrix makeEmptyAccessMatrix(String orderString) {

        if (orderString.equals(PastaAccessTypeValidator.ALLOW_FIRST)) {
            return new AccessMatrix(Order.ALLOW_FIRST);
        }
        else if (orderString.equals(PastaAccessTypeValidator.DENY_FIRST)) {
            return new AccessMatrix(Order.DENY_FIRST);
        }

        String s = "Invalid order attribute: " + orderString;
        throw new IllegalStateException(s);
    }

    /**
     * Indicates if the user with the provided attributes has permission to
     * perform the {@code read} operation.
     *
     * @param authToken
     *            the token containing the user's attributes.
     * @param accessElement
     *            a string containing the Pasta Access 0.1 {@code <access>} element.
     *
     * @return {@code true} if the access control rule allows the specified user
     *         to {@code read}; {@code false} if the user is denied.
     */
    @Override
    public boolean canRead(AuthToken authToken,
                           String accessElement,
                           String resourceSubmitter) {

        if (isSubmitterOrPasta(authToken, resourceSubmitter)) {
            return true;
        }

        AccessMatrix matrix = makeAccessMatrix(accessElement);

        return evaluator.canRead(authToken, matrix);
    }

    /**
     * Indicates if the user with the provided attributes has permission to
     * perform the {@code write} operation.
     *
     * @param authToken
     *            the token containing the user's attributes.
     * @param accessElement
     *            a string containing the Pasta Access 0.1 {@code <access>} element.
     *
     * @return {@code true} if the access control rule allows the specified user
     *         to {@code write}; {@code false} if the user is denied.
     */
    @Override
    public boolean canWrite(AuthToken authToken,
                            String accessElement,
                            String resourceSubmitter) {

        if (isSubmitterOrPasta(authToken, resourceSubmitter)) {
            return true;
        }

        AccessMatrix matrix = makeAccessMatrix(accessElement);

        return evaluator.canWrite(authToken, matrix);
    }

    /**
     * Indicates if the user with the provided attributes has permission to
     * perform the {@code change permission} operation.
     *
     * @param authToken
     *            the token containing the user's attributes.
     * @param accessElement
     *            a string containing the Pasta Access 0.1 {@code <access>} element.
     *
     * @return {@code true} if the access control rule allows the specified user
     *         to {@code change permission}; {@code false} if the user is
     *         denied.
     */
    @Override
    public boolean canChangePermission(AuthToken authToken,
                                       String accessElement,
                                       String resourceSubmitter) {

        if (isSubmitterOrPasta(authToken, resourceSubmitter)) {
            return true;
        }

        AccessMatrix matrix = makeAccessMatrix(accessElement);

        return evaluator.canChangePermission(authToken, matrix);
    }

    /**
     * Indicates if the user with the provided attributes has permission to
     * perform the {@code all of the above} operation.
     *
     * @param authToken
     *            the token containing the user's attributes.
     * @param accessElement
     *            a string containing the Pasta Access 0.1 {@code <access>} element.
     *
     * @return {@code true} if the access control rule allows the specified user
     *         to {@code all of the above}; {@code false} if the user is denied.
     */
    @Override
    public boolean canAll(AuthToken authToken,
                          String accessElement,
                          String resourceSubmitter) {

        if (isSubmitterOrPasta(authToken, resourceSubmitter)) {
            return true;
        }

        AccessMatrix matrix = makeAccessMatrix(accessElement);

        return evaluator.canAll(authToken, matrix);
    }

    private boolean isSubmitterOrPasta(AuthToken token, String submitter) {

        String user = token.getUserId();

        // TODO: equality test needs to be improved
        return user.equals(submitter) || user.equals(PASTA);
    }
}
