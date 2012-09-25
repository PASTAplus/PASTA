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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import pasta.pasta_lternet_edu.log_entry_0.LogEntry;
import edu.lternet.pasta.auditmanager.LogItem.LogItemBuilder;
import edu.lternet.pasta.auditmanager.LogItem.TypedQueryFactory;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.security.access.AccessControllerFactory;
import edu.lternet.pasta.common.security.access.AuthTokenAccessController;
import edu.lternet.pasta.common.security.token.AuthToken;

import org.owasp.esapi.codecs.XMLEntityCodec;

/**
 * A log service class. This class performs all persistence operations for the
 * audit service logging database using the Java Persistence API (JPA) 2.0.
 */
public class LogService
{

    private static Logger logger = Logger.getLogger(LogService.class);

    private static AuthTokenAccessController getAccessController() {
        return AccessControllerFactory.getDefaultAuthTokenAccessController();
    }

    private final EntityManager entityManager;

    /**
     * Constructs a new log service with the provided entity manager.
     * @param em
     *            the entity manager for which all operations are performed.
     * @throws IllegalArgumentException
     *            if the provided entity manager is {@code null}.
     */
    public LogService(EntityManager em) {

        if (em == null) {
            throw new IllegalArgumentException("Null entity manager");
        }
        this.entityManager = em;
    }

    /**
     * Closes the entity manager of this log service.
     */
    public void close() {
        entityManager.close();
    }

    /**
     * Creates a new log entry in the database if the provided user credentials allow it.
     * @param entry
     *              the log entry to be recorded.
     * @return
     *              the OID of the created log entry.
     */
    public int create(LogEntry entry) {

        LogItemBuilder lib = new LogItemBuilder(entry);
        LogItem logItem = lib.build();

        entityManager.getTransaction().begin();
        entityManager.persist(logItem);
        entityManager.getTransaction().commit();

        List<LogItem> list = get(logItem);
        return list.get(0).getOid();
    }

    /**
     * Retreives a log entry according to the provided OID.
     * @param oid a potential log entry OID.
     * @param token authorization credentials.
     * @return the content of the log entry associated with the OID.
     */
    public String get(Integer oid, AuthToken token) {

        LogItemBuilder lib = new LogItemBuilder();
        lib.setOid(oid);
        try {
            String list = getOidsContent(lib, token);
            return list;
        }
        catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Oid " + oid + " does not exist.");
        }
    }

    /**
     * Retreives the log entry OIDs associated with various parameters
     * expressed in the LogItemBuilder object.
     * @param logItemBuilder a set of parameters potentially matching recorded
     *                       log entries.
     * @param token authorization credentials.
     * @return a list of OIDs.
     */
    public List<Integer> getOids(LogItemBuilder logItemBuilder, AuthToken token) {

        TypedQuery<LogItem> jpql =
                TypedQueryFactory.read(logItemBuilder, entityManager);
        List<LogItem> list = jpql.getResultList();
        if (list == null || list.size() == 0)
            throw new ResourceNotFoundException("No Oids found.");
        List<Integer> intList = new ArrayList<Integer>(list.size());
        for (LogItem li : list) intList.add(li.getOid());

        return intList;
    }

    /**
     * Retrieves the log entry content associated with various parameters
     * expressed in the LogItemBuilder object.
     * @param logItemBuilder a set of parameters potentially matching recorded
     *                       log entries.
     * @param token authorization credentials.
     * @return a list of OIDs.
     */
    public String getOidsContent(LogItemBuilder logItemBuilder,
                                       AuthToken token) {

        TypedQuery<LogItem> jpql =
                TypedQueryFactory.read(logItemBuilder, entityManager);
        List<LogItem> list = jpql.getResultList();

        if (list == null || list.size() == 0)
            throw new ResourceNotFoundException("No reports found.");
        
        StringBuilder auditReport = new StringBuilder();
        auditReport.append("<auditReport>\n");

        for (LogItem li : list) {
            StringBuilder sb = new StringBuilder();
            sb.append("  <auditRecord>\n");
            sb.append("    <oid>" + li.getOid().toString() + "</oid>\n");
            sb.append("    <entryTime>" + ISO8601Utilities.formatDateTime(li.getEntryTime()) + "</entryTime>\n");
            sb.append("    <category>" + li.getCategory() + "</category>\n");
            sb.append("    <service>" + li.getService() + "</service>\n");
            sb.append("    <serviceMethod>" + li.getServiceMethod() + "</serviceMethod>\n");
            sb.append("    <responseStatus>" + li.getResponseStatus() + "</responseStatus>\n");
            sb.append("    <resourceId>" + li.getResourceId() + "</resourceId>\n");
            sb.append("    <user>" + li.getUser() + "</user>\n");
            sb.append("    <groups>" + li.getGroups() + "</groups>\n");
            sb.append("    <authSystem>" + li.getAuthSystem() + "</authSystem>\n");
            sb.append("    <entryText>" + this.xmlEncode(li.getEntryText()) + "</entryText>\n");
            sb.append("  </auditRecord>\n");
            auditReport.append(sb.toString());
        }
        
        auditReport.append("</auditReport>\n");
        
        return auditReport.toString();
        
    }

    private List<LogItem> get(LogItem logItem) {

        TypedQuery<LogItem> jpql =
                TypedQueryFactory.read(logItem, entityManager);

        return jpql.getResultList();
    }
    
  private String xmlEncode(String rawXml) {

    String encodedXml = null;

    if (rawXml == null) {
      encodedXml = "";
    } else {

      // Encodings for XML
      XMLEntityCodec xmlEntityCodec = new XMLEntityCodec();
      char[] immune = new char[0];

      StringBuffer xml = new StringBuffer();

      for (int a = 0; a < rawXml.length(); a++) {
        xml.append(xmlEntityCodec.encodeCharacter(immune, rawXml.charAt(a)));
      }
      
      encodedXml = xml.toString();

    }
      
      return encodedXml;
      
    }

}

/**
 * 
 * @author servilla
 * @since May 2, 2012
 *
 *  The ISO8601Utilities converts a Java Data object date/time string to a true
 *  ISO8601 date/time string.  This class is based on the information provide by
 *  http://developer.marklogic.com/learn/2004-09-dates.
 *
 */
class ISO8601Utilities
{
    private static DateFormat m_ISO8601Local =
        new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");

    public static String formatDateTime()
    {
        return formatDateTime (new Date());
    }

    public static String formatDateTime (Date date)
    {
        if (date == null) {
            return formatDateTime (new Date());
        }

        // format in (almost) ISO8601 format
        String dateStr = m_ISO8601Local.format (date);
        return dateStr;
    }
}


