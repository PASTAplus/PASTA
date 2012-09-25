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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to store a collection of comments.
 */
public class Comments {

    public static final String INFO = "INFO: ";
    public static final String WARN = "WARN: ";
    public static final String FATAL = "FATAL: ";

    private final List<String> comments;

    public Comments() {
        comments = new LinkedList<String>();
    }

    /**
     * Stores a comment that describes a fatal flaw or condition.
     *
     * @param comment
     *            a description of a fatal flaw or condition.
     * @throws IllegalArgumentException
     *             if the provided comment is {@code null} or empty.
     */
    public void fatal(String comment) {
        assertNotNullOrEmpty(comment);
        comments.add(FATAL + comment);
    }

    /**
     * Stores a comment that is meant as a warning.
     *
     * @param comment
     *            a warning message.
     * @throws IllegalArgumentException
     *             if the provided comment is {@code null} or empty.
     */
    public void warn(String comment) {
        assertNotNullOrEmpty(comment);
        comments.add(WARN + comment);
    }

    /**
     * Stores a general comment that is meant to be explanatory, for example.
     *
     * @param comment
     *            a general comment.
     * @throws IllegalArgumentException
     *             if the provided comment is {@code null} or empty.
     */
    public void info(String comment) {
        assertNotNullOrEmpty(comment);
        comments.add(INFO + comment);
    }

    private void assertNotNullOrEmpty(String comment) {
        if (comment == null || comment.isEmpty()) {
            throw new IllegalArgumentException("null or empty comment");
        }
    }

    /**
     * Returns the comments currently contained in this object as a list. The
     * returned list reflects the order in which comments were added to this
     * object, and each comment is prefixed with one of "INFO: ", "WARN: ", or
     * "FATAL: ", depending on its type. Modifications made to the list will
     * not be reflected in this object.
     *
     * @return the comments currently contained in this object as a list.
     */
    public List<String> asList() {
        return new ArrayList<String>(comments); // defensive copy
    }
}
