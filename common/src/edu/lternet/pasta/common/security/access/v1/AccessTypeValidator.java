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

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import edu.lternet.pasta.common.validate.Validator;
import edu.lternet.pasta.common.validate.ValidatorResultsImpl;
import eml.ecoinformatics_org.access_2_1.AccessRule;
import eml.ecoinformatics_org.access_2_1.AccessType;

/**
 * <p>
 * Validates {@code <access>} elements.
 * </p>
 * <p>
 * An instance of {@link AuthSystemValidator} is used to validate and
 * canonicalize the {@code authSystem} attribute. If the {@code authSystem} is
 * invalid, the entire {@code <access>} element is considered invalid.
 * </p>
 * <p>
 * An instance of {@link PermissionStringValidator} is used to validate and
 * canonicalize each of the permissions in each {@code <allow>} and {@code
 * <deny>} element. If a permission is invalid, the entire {@code
 * <access>} element is considered invalid.
 * </p>
 * <p>
 * An instance of {@link PrincipalValidator} is used to validate and
 * canonicalize each of the principals in each {@code <allow>} and {@code
 * <deny>} element.
 * </p>
 * <p>
 * If the {@code order} attribute is specified as either {@code allowFirst} or
 * {@code denyFirst}, but no {@code <allow>} or {@code <deny>} elements exist,
 * respectively, the {@code <access>} element is still considered valid, but the
 * canonical version will contain the other attribute.
 * </p>
 * <p>
 * During canonicalization, the provided {@link AccessType} object is modified
 * directly.
 * </p>
 */
public class AccessTypeValidator implements Validator<AccessType> {

    public static final String ALLOW_FIRST = "allowFirst";
    public static final String DENY_FIRST = "denyFirst";
    public static final String ALLOW = "allow";
    public static final String DENY = "deny";

    private ValidatorResultsImpl<AccessType> results;

    /**
     * Returns {@code true}.
     * @return {@code true}.
     */
    @Override
    public boolean canonicalizes() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidatorResults<AccessType> validate(AccessType entity) {

        if (entity == null) {
            throw new IllegalArgumentException("null entity");
        }

        results = new ValidatorResultsImpl<AccessType>();
        results.setValid(true);
        results.setEntity(entity);
        results.setCanonicalEntity(entity);

        validateOrder(entity);
        validateAuthSystem(entity);
        validateAccessRules(entity.getAllowOrDeny());

        return results;
    }

    private void validateOrder(AccessType entity) {

        String order = entity.getOrder();
        String oppositeOrder = null;
        String allowOrDeny = null;

        if (order.equals(ALLOW_FIRST)) {
            oppositeOrder = DENY_FIRST;
            allowOrDeny = ALLOW;
        }
        else if (order.equals(DENY_FIRST)) {
            oppositeOrder = ALLOW_FIRST;
            allowOrDeny = DENY;
        }
        else {
            throw new IllegalArgumentException("invalid order: " + order);
        }

        if (hasAllowOrDeny(entity, allowOrDeny)) {
            return; // everything is fine, nothing more to do
        }

        String s = "The order '" + order + "' was specified, but no '"
                + allowOrDeny + "' elements were specified.";
        results.warn(s);

        entity.setOrder(oppositeOrder);

        s = "The order '" + order + "' was replaced with '" + oppositeOrder
                + "' in the canonical version.";
        results.info(s);
    }

    private boolean hasAllowOrDeny(AccessType entity, String allowOrDeny) {

        for (JAXBElement<AccessRule> jaxb : entity.getAllowOrDeny()) {
            if (jaxb.getName().getLocalPart().equalsIgnoreCase(allowOrDeny)) {
                return true;
            }
        }

        return false;
    }

    private void validateAccessRules(List<JAXBElement<AccessRule>> rules) {

        // Iterating thru each allow/deny element
        for (JAXBElement<AccessRule> jaxb : rules) {
            AccessRule rule = jaxb.getValue();
            validatePermissions(rule);
            validatePrincipals(rule);
        }

    }

    private void validatePermissions(AccessRule rule) {

        PermissionStringValidator validator = new PermissionStringValidator();

        List<String> permissions = rule.getPermission();
        List<String> canonical = new LinkedList<String>();

        for (String permission : permissions) {

            ValidatorResults<String> vr = validator.validate(permission);

            results.addResults(vr);

            if (!vr.isValid()) {
                results.setValid(false);
            }

            canonical.add(vr.getCanonicalEntity());
        }

        permissions.clear();
        permissions.addAll(canonical);
    }

    private void validatePrincipals(AccessRule rule) {

        PrincipalValidator validator = new PrincipalValidator();

        List<String> principals = rule.getPrincipal();
        List<String> canonical = new LinkedList<String>();

        for (String principal : principals) {

            ValidatorResults<String> vr = validator.validate(principal);

            results.addResults(vr);

            if (!vr.isValid()) {
                results.setValid(false);
            }

            canonical.add(vr.getCanonicalEntity());
        }

        principals.clear();
        principals.addAll(canonical);
    }

    private void validateAuthSystem(AccessType entity) {

        Validator<String> validator = new AuthSystemValidator();

        String specifiedAuthSystem = entity.getAuthSystem();

        ValidatorResults<String> vr = validator.validate(specifiedAuthSystem);

        results.addResults(vr);

        if (!vr.isValid()) {
            results.setValid(false);
        }

        String authSystem = vr.getCanonicalEntity();

        entity.setAuthSystem(authSystem);
    }
}
