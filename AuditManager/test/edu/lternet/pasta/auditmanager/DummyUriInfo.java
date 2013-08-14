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

package edu.lternet.pasta.auditmanager;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DummyUriInfo implements UriInfo {

    private final MultivaluedMap<String, String> queryParams;
    
    public DummyUriInfo(Map<String, String> queryParams) {
        this.queryParams = new MultivaluedMapImpl();
        for (Entry<String, String> e : queryParams.entrySet()) {
            this.queryParams.putSingle(e.getKey(), e.getValue());
        }
    }
    
    @Override
    public URI getAbsolutePath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getBaseUri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> getMatchedResources() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getMatchedURIs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getMatchedURIs(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPath(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PathSegment> getPathSegments() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PathSegment> getPathSegments(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return queryParams;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean arg0) {
        return queryParams;
    }

    @Override
    public URI getRequestUri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        throw new UnsupportedOperationException();
    }

}
