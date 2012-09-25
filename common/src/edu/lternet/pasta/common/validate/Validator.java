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

import java.util.Collection;
import java.util.List;

/**
 * Represents an entity validator, which determines the validity of an object or
 * generic "entity". During validation, a report can be generated, consisting of
 * comments describing what was found, what was expected, and anything else that
 * might be relevant. Some validators can also produce a canonical version of
 * the provided entity during its validation.
 *
 * @param <T>
 *            the type of entity that can be validated.
 */
public interface Validator<T> {

    /**
     * Validates the provided entity.
     *
     * @param entity
     *            the entity to be validated.
     *
     * @return the results of validating the provided entity.
     */
    public ValidatorResults<T> validate(T entity);

    /**
     * Indicates if this validator produces a canonicalized version of provided
     * entities during their validation. If {@code true}, a canonical form of
     * the entity can be obtained with {@link
     * ValidatorResults#getCanonicalEntity()}. If {@code false}, that method will
     * throw an {@code IllegalStateException} when invoked.
     *
     * @return {@code true} if this validator produces canonical entities;
     * {@code false} otherwise.
     */
    public boolean canonicalizes();

    /**
     * Represents the results of validating an entity.
     *
     * @param <T> the type of entity for which validity was determined.
     */
    public static interface ValidatorResults<T> {

        /**
         * Returns the entity for which validity was determined.
         *
         * @return the entity for which validity was determined.
         */
        public T getEntity();

        /**
         * Indicates if validation was successful, that is, the provided entity
         * is considered valid.
         *
         * @return {@code true} if the entity is valid; {@code false}
         *         otherwise.
         */
        public boolean isValid();

        /**
         * Returns the comments produced during validation.
         *
         * @return the comments produced during validation.
         */
        public Collection<String> getComments();

        /**
         * Indicates if these validation results contain a canonicalized version
         * of the provided entity.
         *
         * @return {@code true} if a canonicalized entity was produced during
         *         validation; {@code false} otherwise.
         */
        public boolean hasCanonicalEntity();

        /**
         * Returns a canonicalized version of the provided entity, if one
         * exists. If the provided entity is immutable, a new object is
         * returned. If the provided entity is mutable, either a new object is
         * returned, or the provided entity is transformed to be canonical. If
         * the latter occurs, this method will return the same object as
         * {@link #getEntity()}. Implementations of this interface should always
         * specify which approach is used if entities are mutable.
         *
         * @return the canonicalized version of the validated entity, if it
         *         exists.
         *
         * @throws IllegalStateException
         *             if a canonicalized entity does not exist because the
         *             validator did not produce one.
         *
         * @see Validator#canonicalizes()
         */
        public T getCanonicalEntity();

        /**
         * Returns a collection of sub-results contained within these results.
         * If an entity is composed of other entities, validating it will often
         * require validation of its sub-entities, as well. The results of their
         * validation can be retrieved using this method. If no sub-results
         * exists, an empty list is returned.
         *
         * @return a collection of sub-results contained within these results.
         */
        public List<ValidatorResults<?>> getSubResults();

    }
}
