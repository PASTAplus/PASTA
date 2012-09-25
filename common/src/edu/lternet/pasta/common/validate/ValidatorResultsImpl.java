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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.lternet.pasta.common.validate.Validator.ValidatorResults;

/**
 * Used to store the results of a validator.
 *
 * @param <T> the entity type.
 */
public class ValidatorResultsImpl<T> implements ValidatorResults<T> {

    private T entity;
    private T canonicalEntity;
    private Boolean valid;

    private final Comments comments;
    private final List<ValidatorResults<?>> subResults;

    /**
     * Constructs a new validator results object with empty attributes.
     */
    public ValidatorResultsImpl() {
        comments = new Comments();
        subResults = new LinkedList<ValidatorResults<?>>();
    }

    /**
     * Sets the entity of these results, that is, the one that was validated.
     *
     * @param entity
     *            the entity that was validated.
     * @return these results.
     * @throws IllegalArgumentException
     *             if the provided entity is {@code null}.
     */
    public ValidatorResultsImpl<T> setEntity(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("null entity");
        }
        this.entity = entity;
        return this;
    }

    /**
     * Sets the canonical version of the validated entity.
     *
     * @param canonicalEntity
     *            the canonical version of the validated entity.
     * @return these results.
     * @throws IllegalArgumentException
     *             if the provided entity is {@code null}.
     */
    public ValidatorResultsImpl<T> setCanonicalEntity(T canonicalEntity) {
        if (canonicalEntity == null) {
            throw new IllegalArgumentException("null canonical entity");
        }
        this.canonicalEntity = canonicalEntity;
        return this;
    }

    /**
     * Adds sub-results to these results. Sub-results are produced when
     * validation of an entity required validation of its sub-entities.
     *
     * @param subResult
     *            sub-results to be added to these results.
     * @return these results.
     * @throws IllegalArgumentException
     *             if the provided sub-results are {@code null}.
     */
    public ValidatorResultsImpl<T> addResults(ValidatorResults<?> subResult) {
        if (subResult == null) {
            throw new IllegalArgumentException("null sub-result");
        }
        this.subResults.add(subResult);
        return this;
    }

    /**
     * Sets the validity of the validated entity.
     *
     * @param valid
     *            the validity of the validated entity.
     * @return these results.
     */
    public ValidatorResultsImpl<T> setValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    /**
     * Stores a comment that describes a fatal flaw or condition and sets the
     * validity to {@code false}.
     *
     * @param comment
     *            a description of a fatal flaw or condition.
     * @return these results.
     * @throws IllegalArgumentException
     *             if the provided comment is {@code null} or empty.
     */
    public ValidatorResultsImpl<T> fatal(String comment) {
        comments.fatal(comment);
        return setValid(false);
    }

    /**
     * Stores a comment that is meant as a warning.
     *
     * @param comment
     *            a warning message.
     * @return these results.
     * @throws IllegalArgumentException
     *             if the provided comment is {@code null} or empty.
     */
    public ValidatorResultsImpl<T> warn(String comment) {
        comments.warn(comment);
        return this;
    }

    /**
     * Stores a general comment that is meant to be explanatory, for example.
     *
     * @param comment
     *            a general comment.
     * @return these results.
     * @throws IllegalArgumentException
     *             if the provided comment is {@code null} or empty.
     */
    public ValidatorResultsImpl<T> info(String comment) {
        comments.info(comment);
        return this;
    }

    /**
     * Returns the canonical entity, if one was set.
     *
     * @return the canonical entity.
     *
     * @throws IllegalStateException
     *             if these results do not contain a canonical entity.
     * @see #setCanonicalEntity(Object)
     */
    @Override
    public T getCanonicalEntity() {
        if (canonicalEntity == null) {
            throw new IllegalStateException("no canonicalized entity");
        }
        return canonicalEntity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getComments() {
        return comments.asList();
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if an entity was not set.
     * @see #setEntity(Object)
     */
    @Override
    public T getEntity() {
        if (canonicalEntity == null) {
            throw new IllegalStateException("an entity was not set");
        }
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCanonicalEntity() {
        return canonicalEntity != null;
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if validity was not set.
     * @see #setValid(boolean)
     */
    @Override
    public boolean isValid() {
        if (valid == null) {
            throw new IllegalStateException("validity was not set");
        }
        return valid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ValidatorResults<?>> getSubResults() {
        return Collections.unmodifiableList(subResults);
    }

}
