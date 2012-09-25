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

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pasta.pasta_lternet_edu.log_entry_0.LogEntry;
import edu.lternet.pasta.common.LogEntryFactory;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;

public class AuditServiceResourceTest
{
    private EntityManagerFactory emf;
    private AuditServiceResource resource;
    private JAXBElement<LogEntry> entry;
    private HttpHeaders headers;
    private Integer oid;
    private Date d = new Date();

    @Before
    public void init() {
        new ConfigurationListener().setContextSpecificProperties();
        String persistenceUnit =
                ConfigurationListener.getJUnitPersistenceUnit();
        emf = Persistence.createEntityManagerFactory(persistenceUnit);

        resource = new AuditServiceResource(emf);
        headers = new DummyCookieHttpHeaders("anonymous");
        Set<String> s = new HashSet<String>();
        s.add("authenticated");
        String user = "ucarroll";
        long EXPIRATION = -1;
        AuthToken attr =
          AuthTokenFactory.makeCookieAuthToken(user, AuthSystemDef.KNB, EXPIRATION, s);
//        LogEntry le = new LogEntryBuilder(CategoryType.DEBUG, "AuditManager", "Test")
//                                         .setToken(attr).build();
        LogEntry le = LogEntryFactory.makeDebug("AuditManager", null, attr, null, null, "JUnit Test");
        QName q = new QName("");
        entry = new JAXBElement<LogEntry>(q, LogEntry.class, le);
    }

    @After
    public void cleanUp() {
        emf.close();
    }

    private void setOid(Response r) {
        MultivaluedMap<String, Object> map = r.getMetadata();
        List<Object> loc = map.get(HttpHeaders.LOCATION);
        URI uri = (URI) loc.get(0);
        String str = uri.getPath();
        oid = Integer.parseInt(str.substring(str.lastIndexOf('/') + 1, str.length()));
    }

    @Test
    public void testCreate() {
        Response r = resource.create(headers, entry);
        assertEquals(Response.Status.CREATED.getStatusCode(), r.getStatus());
    }

    @Test
    public void testGetReport() {
        setOid(resource.create(headers, entry));
        Response r = resource.getReport(headers, oid);
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
    }

    @Test
    public void testGetAllReports() {
        Map<String, String> query = Collections.EMPTY_MAP;
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = resource.getReports(headers, uriInfo);
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
    }

    @Test
    public void testGetAllOids() {
        Map<String, String> query = Collections.EMPTY_MAP;
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = resource.getOids(headers, uriInfo);
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
    }

    @Test
    public void testGetReportsByCategory() {
        Map<String, String> query = Collections.singletonMap("category", "debug");
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = resource.getReports(headers, uriInfo);
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
    }

    @Test
    public void testGetReportsByMissingCategory() {
        Map<String, String> query = Collections.singletonMap("category", "nothing");
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = resource.getReports(headers, uriInfo);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), r.getStatus());
    }

    @Test
    public void testGetReportsByInvalidQuery() {
        Map<String, String> query = Collections.singletonMap("nothing", "nothing");
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = resource.getReports(headers, uriInfo);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }

    @Test
    public void testGetReportsByUser() {
        Map<String, String> query = Collections.singletonMap("user", "ucarroll");
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = resource.getReports(headers, uriInfo);
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
    }

    @Test
    public void testGetReportsByTime() {
        Calendar c = GregorianCalendar.getInstance();c.setTime(d);
        String str = DatatypeConverter.printDateTime(c);
        System.out.println(str);
        Map<String, String> query = Collections.singletonMap("fromTime", str);
        UriInfo uriInfo = new DummyUriInfo(query);
        Response r = resource.getReports(headers, uriInfo);
        assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
        System.out.println(r.getEntity());
    }

}
