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

package edu.lternet.pasta.common.validate;

/**
 * Used for null validation. Instances of this validator class assume that all
 * provided entities are valid, and no validation actually occurs. The provided
 * entity is both the entity and canonical entity in the validation results.
 *
 * @param <T>
 *            the entity type.
 */
public class NullValidator<T> implements Validator<T> {

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
    public ValidatorResults<T> validate(T entity) {

        ValidatorResultsImpl<T> results = new ValidatorResultsImpl<T>();

        results.setEntity(entity);
        results.setCanonicalEntity(entity);
        results.setValid(true);

        return results;
    }


}
