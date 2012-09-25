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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Parameter;
import javax.persistence.Table;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import pasta.pasta_lternet_edu.log_entry_0.CategoryType;
import pasta.pasta_lternet_edu.log_entry_0.LogEntry;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;

/**
 * This class is used to represent LogEntrys as they will be persisted in a
 * database using the {@link LogService} class.
 * 
 * <p>
 * This class utilizes the Java Persistence API (JPA) 2.0. Instances of this
 * class are immutable.
 */
@Entity
@Table(schema="auditmanager", name="eventlog")
public class LogItem
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int oid;
    @Column(columnDefinition="not null")
    private Date entryTime;
    @Column(columnDefinition="not null", length=32)
    private String service;
    @Column(columnDefinition="not null", length=8)
    private String category;
    @Column(length=128)
    private String serviceMethod;
    @Column(length=2147483647)
    private String entryText;
    @Column(length=128)
    private String resourceId;
    private Integer statusCode;
    @Column(length=128, name="userid")
    private String user;
    @Column(length=512)
    private String groups;
    @Column(length=128)
    private String authSystem;

    private static Logger logger = Logger.getLogger(LogService.class);
    /**
     * Constructs an empty subscription. A no-arg constructor is required for
     * JPA Entities. Use the builder class to construct subscriptions with
     * content.
     */
    protected LogItem() {}

    /**
     * Returns the entry time of this log item.
     * @return the Date of this log item.
     */
    public Date getEntryTime() {
        return entryTime;
    }

    /**
     * Returns the originating service of this log item.
     * @return the originating service of this log item.
     */
    public String getService() {
        return service;
    }

    /**
     * Returns the category of this log item.
     * @return the category of this log item.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the service method of this log item.
     * @return the service method of this log item.
     */
    public String getServiceMethod() {
        return serviceMethod;
    }

    /**
     * Returns the resource ID of this log item.
     * @return the resource ID of this log item.
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Returns the response status of this log item.
     * @return the response status of this log item.
     */
    public Integer getResponseStatus() {
        return statusCode;
    }

    /**
     * Returns the user of this log item.
     * @return the user of this log item.
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the groups of this log item.
     * @return the groups of this log item.
     */
    public String getGroups() {
        return groups;
    }

    /**
     * Returns the auth system of this log item.
     * @return the auth system of this log item.
     */
    public String getAuthSystem() {
        return authSystem;
    }

    /**
     * Returns the entry text of this log item.
     * @return the entry text of this log item.
     */
    public String getEntryText() {
        return entryText;
    }

    /**
     * Returns the OID of this log item.
     * @return the OID of this log item.
     */
    public Integer getOid() {
        return oid;
    }

    /**
     * Returns a string representation of this subscription.
     * @return a string representation of this subscription.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("entryTime=" + entryTime.toString());
        sb.append(",service=" + service);
        sb.append(",category=" + category);
        sb.append(",resourceId=" + resourceId);
        sb.append(",statusCode=" + statusCode);
        sb.append(",user=" + user);
        sb.append(",groups=" + groups);
        sb.append(",authSystem=" + authSystem);
        sb.append(",entryText=" + entryText);
        return sb.toString();
    }

    /**
     * Used to build log items.
     */
    public static final class LogItemBuilder {
        private List<String> users;
        private List<String> grouplist;
        private List<String> authSystems;
        private List<String> categories;
        private List<Date> atTimes;
        private List<String> services;
        private List<String> serviceMethods;
        private List<Integer> statusCodes;
        private List<String> resourceIds;
        private Date fromTime;
        private Date toTime;

        private Integer oid;
        private AuthToken token;
        private String category;
        private Date entryTime;
        private String service;
        private String serviceMethod;
        private Integer statusCode;
        private String resourceId;
        private String entryText;

        public LogItemBuilder() {}
        public LogItemBuilder(LogEntry entry) {

            setCategory(entry.getCategory());
            setService(entry.getService());
            setEntryTime(entry.getTimestamp());
            setEntryText(entry.getEventText());
            setResourceId(entry.getResourceId());
            setToken(entry.getUserToken());
            setStatusCode(entry.getHttpStatusCode());
            setServiceMethod(entry.getServiceMethod());
        }

        public void setOid(Integer i) {
            oid = i;
        }

        public Integer getOid() {
            return oid;
        }

        public LogItemBuilder setToken(String s) {
            if (s != null && !s.isEmpty())
                token = AuthTokenFactory.makeCookieAuthToken(s);
            else token = null;
            return this;
        }

        public String getUser() {
            if (token == null) return null;
            return token.getUserId();
        }

        public String getGroups() {
            if (token == null) return null;
            StringBuilder sb = new StringBuilder();
            for (String s : token.getGroups()) {
                if (sb.length() != 0) sb.append(",");
                sb.append(s);
            }
            return sb.toString();
        }

        public String getAuthSystem() {
            if (token == null) return null;
            return token.getAuthSystem().getCanonicalName();
        }

        public LogItemBuilder setCategory(CategoryType ct) {

            if (ct == null)
                throw new IllegalArgumentException("Category is null.");

            category = ct.value();
            return this;
        }

        public CategoryType getCategory() {
            return CategoryType.fromValue(category);
        }

        public LogItemBuilder setEntryTime(XMLGregorianCalendar gc) {
            entryTime = gc.toGregorianCalendar().getTime();
            return this;
        }

        public Date getEntryTime() {
            return entryTime;
        }

        public LogItemBuilder setService(String s) {
            if (s == null || s.isEmpty())
                throw new IllegalArgumentException("Service is null.");
            service = s;
            return this;
        }

        public String getService() {
            return service;
        }

        public LogItemBuilder setStatusCode(BigInteger i) {

            if (i != null) statusCode = i.intValue();
            return this;
        }

        public Integer getStatusCode() {
            return statusCode;
        }

        public LogItemBuilder setResourceId(String s) {
            resourceId = s;
            return this;
        }

        public String getResourceId() {
            return resourceId;
        }

        public LogItemBuilder setEntryText(String s) {
            entryText = s;
            return this;
        }

        public String getEntryText() {
            return entryText;
        }

        public LogItemBuilder setServiceMethod(String s) {
            serviceMethod = s;
            return this;
        }

        public String getServiceMethod() {
            return serviceMethod;
        }

        public LogItem build() {
            LogItem li = new LogItem();
            li.entryTime = getEntryTime();
            li.category = getCategory().value();
            li.entryText = getEntryText();
            li.resourceId = getResourceId();
            li.service = getService();
            li.serviceMethod = getServiceMethod();
            li.statusCode = getStatusCode();
            li.user = getUser();
            li.groups = getGroups();
            li.authSystem = getAuthSystem();

            return li;
        }

        public void setUsers(List<String> list) {
            users = list;
        }

        public void setGroups(List<String> list) {
            grouplist = list;
        }

        public void setAuthSystem(List<String> list) {
            authSystems = list;
        }

        public void setCategories(List<String> list) {
            categories = list;
        }

        public void setAtTimes(List<Date> list) {
            atTimes = list;
        }

        public void setServices(List<String> list) {
            services = list;
        }

        public void setServiceMethods(List<String> list) {
            serviceMethods = list;
        }

        public void setStatusCodes(List<Integer> list) {
            statusCodes = list;
        }

        public void setResourceIds(List<String> list) {
            resourceIds = list;
        }

        public void setFromTime(Date time) {
            fromTime = time;
        }

        public void setToTime(Date time) {
            toTime = time;
        }

        public List<String> getUsers() {
            return users;
        }

        public List<String> getGroupList() {
            return grouplist;
        }

        public List<String> getAuthSystems() {
            return authSystems;
        }

        public List<String> getCategories() {
            return categories;
        }

        public List<Date> getAtTimes() {
            return atTimes;
        }

        public List<String> getServices() {
            return services;
        }

        public List<String> getServiceMethods() {
            return serviceMethods;
        }

        public List<Integer> getStatusCodes() {
            return statusCodes;
        }

        public List<String> getResourceIds() {
            return resourceIds;
        }

        public Date getFromTime() {
            return fromTime;
        }

        public Date getToTime() {
            return toTime;
        }
    }

    public static final class TypedQueryFactory {

        public static final String TOKEN = "token";
        public static final String STATUSCODE = "statusCode";
        public static final String RESOURCEID = "resourceId";
        public static final String ENTRYTEXT = "entryText";
        public static final String CATEGORY = "category";
        public static final String ENTRYTIME = "entryTime";
        public static final String SERVICE = "service";
        public static final String SERVICEMETHOD = "serviceMethod";
        public static final String USER = "user";
        public static final String GROUPS = "groups";
        public static final String AUTHSYSTEM = "authSystem";
        public static final String OID = "oid";

        public static TypedQuery<LogItem> read(LogItem logItem, EntityManager em) {

            StringBuilder sb = new StringBuilder();
            sb.append(baseStmt());

            List<String> criteria = new ArrayList<String>();
            if (logItem.getCategory() != null) {
                criteria.add("x." + CATEGORY + " = :" + CATEGORY);
            }
            else {
                criteria.add("x." + CATEGORY + " is null");
            }

            if (logItem.getUser() != null) {
                criteria.add("x." + USER + " = :" + USER);
            }
            else {
                criteria.add("x." + USER + " is null");
            }

            if (logItem.getGroups() != null) {
                criteria.add("x." + GROUPS + " = :" + GROUPS);
            }
            else {
                criteria.add("x." + GROUPS + " is null");
            }

            if (logItem.getAuthSystem() != null) {
                criteria.add("x." + AUTHSYSTEM + " = :" + AUTHSYSTEM);
            }
            else {
                criteria.add("x." + AUTHSYSTEM + " is null");
            }

            if (logItem.getResponseStatus() != null) {
                criteria.add("x." + STATUSCODE + " = :" + STATUSCODE);
            }
            else {
                criteria.add("x." + STATUSCODE + " is null");
            }

            if (logItem.getResourceId() != null) {
                criteria.add("x." + RESOURCEID + " = :" + RESOURCEID);
            }
            else {
                criteria.add("x." + RESOURCEID + " is null");
            }

            if (logItem.getEntryText() != null) {
                criteria.add("x." + ENTRYTEXT + " = :" + ENTRYTEXT);
            }
            else {
                criteria.add("x." + ENTRYTEXT + " is null");
            }

            if (logItem.getEntryTime() != null) {
                criteria.add("x." + ENTRYTIME + " = :" + ENTRYTIME);
            }
            else {
                criteria.add("x." + ENTRYTIME + " is null");
            }

            if (logItem.getService() != null) {
                criteria.add("x." + SERVICE + " = :" + SERVICE);
            }
            else {
                criteria.add("x." + SERVICE + " is null");
            }

            if (criteria.size() > 0) sb.append(" where ");
            for (int i = 0; i < criteria.size(); i++) {
                if (i > 0) sb.append(" and ");
                sb.append(criteria.get(i));
            }

            TypedQuery<LogItem> q = em.createQuery(sb.toString(), LogItem.class);

            Set<Parameter<?>> s = q.getParameters();
            Iterator<Parameter<?>> it = s.iterator();

            while (it.hasNext()) {
                Parameter<?> p = it.next();

                if (p.getName().equals(CATEGORY)) {
                    q.setParameter(p.getName(), logItem.getCategory());
                } else if (p.getName().equals(USER)) {
                    q.setParameter(p.getName(), logItem.getUser());
                } else if (p.getName().equals(GROUPS)) {
                    q.setParameter(p.getName(), logItem.getGroups());
                } else if (p.getName().equals(AUTHSYSTEM)) {
                    q.setParameter(p.getName(), logItem.getAuthSystem());
                } else if (p.getName().equals(STATUSCODE)) {
                    q.setParameter(p.getName(), logItem.getResponseStatus());
                } else if (p.getName().equals(RESOURCEID)) {
                    q.setParameter(p.getName(), logItem.getResourceId());
                } else if (p.getName().equals(ENTRYTEXT)) {
                    q.setParameter(p.getName(), logItem.getEntryText());
                } else if (p.getName().equals(ENTRYTIME)) {
                    q.setParameter(p.getName(), logItem.getEntryTime(), TemporalType.TIMESTAMP);
                } else if (p.getName().equals(SERVICE)) {
                    q.setParameter(p.getName(), logItem.getService());
                }
                else {
                    throw new IllegalStateException("Unknown Parameter: " + p.getName());
                }
            }

            return q;
        }

        public static TypedQuery<LogItem> read(LogItemBuilder lib, EntityManager em) {

            StringBuilder sb = new StringBuilder();
            sb.append(baseStmt());

            List<String> criteria = new ArrayList<String>();
            List<String> categories = null;
            List<Integer> statusCodes = null;
            List<String> resourceIds = null;
            List<Date> entryTimes = null;
            List<String> services = null;
            List<String> serviceMethods = null;
            List<String> users = null;
            List<String> groups = null;
            List<String> authSystems = null;
            Integer oid = null;

            if (lib.getOid() != null) {
                oid = lib.getOid();
                List<Integer> oidlist = new ArrayList<Integer>();
                oidlist.add(oid);
                criteria.add(subListBuilder(OID, oidlist));
            }

            if (lib.getCategories() != null && lib.getCategories().size() > 0) {
                categories = lib.getCategories();
                criteria.add(subListBuilder(CATEGORY, categories));
            }

            if (lib.getStatusCodes() != null && lib.getStatusCodes().size() > 0) {
                statusCodes = lib.getStatusCodes();
                criteria.add(subListBuilder(STATUSCODE, statusCodes));
            }

            if (lib.getResourceIds() != null && lib.getResourceIds().size() > 0) {
                resourceIds = lib.getResourceIds();
                criteria.add(subListBuilder(RESOURCEID, resourceIds));
            }

            if (lib.getAtTimes() != null && lib.getAtTimes().size() > 0) {
                entryTimes = lib.getAtTimes();
                criteria.add(subListBuilder(ENTRYTIME, entryTimes));
            }

            if (lib.getServiceMethods() != null && lib.getServiceMethods().size() > 0) {
                serviceMethods = lib.getServiceMethods();
                criteria.add(subListBuilder(SERVICEMETHOD, serviceMethods));
            }

            if (lib.getServices() != null && lib.getServices().size() > 0) {
                services = lib.getServices();
                criteria.add(subListBuilder(SERVICE, services));
            }

            if (lib.getUsers() != null && lib.getUsers().size() > 0) {
                users = lib.getUsers();
                criteria.add(subListBuilder(USER, users));
            }

            if (lib.getGroupList() != null && lib.getGroupList().size() > 0) {
                groups = lib.getGroupList();
                criteria.add(subListBuilder(GROUPS, groups));
            }

            if (lib.getAuthSystems() != null && lib.getAuthSystems().size() > 0) {
                authSystems = lib.getAuthSystems();
                criteria.add(subListBuilder(AUTHSYSTEM, authSystems));
            }

            if (lib.getFromTime() != null) {
                entryTimes = new ArrayList<Date>();
                entryTimes.add(lib.getFromTime());
                StringBuilder time = new StringBuilder();
                time.append("( x." + ENTRYTIME + " > :" + ENTRYTIME + 1);
                if (lib.getToTime() != null) {
                    entryTimes.add(lib.getToTime());
                    time.append(" and x." + ENTRYTIME + " < :" + ENTRYTIME + 2 );
                }

                time.append(" )");
                criteria.add(time.toString());
            }

            if (criteria.size() > 0) sb.append(" where ");
            for (int i = 0; i < criteria.size(); i++) {
                if (i > 0) sb.append(" and ");
                sb.append(criteria.get(i));
            }

            TypedQuery<LogItem> q = em.createQuery(sb.toString(), LogItem.class);
            Set<Parameter<?>> s = q.getParameters();
            Iterator<Parameter<?>> it = s.iterator();

            while (it.hasNext()) {
                Parameter<?> p = it.next();

                if (p.getName().contains(CATEGORY)) {
                    q.setParameter(p.getName(), categories.get(0));
                    categories.remove(0);
                } else if (p.getName().contains(STATUSCODE)) {
                    q.setParameter(p.getName(), statusCodes.get(0));
                    statusCodes.remove(0);
                } else if (p.getName().contains(RESOURCEID)) {
                    q.setParameter(p.getName(), resourceIds.get(0));
                    resourceIds.remove(0);
                } else if (p.getName().contains(ENTRYTIME)) {
                    q.setParameter(p.getName(), entryTimes.get(0), TemporalType.TIMESTAMP);
                    entryTimes.remove(0);
                } else if (p.getName().contains(SERVICEMETHOD)) {
                    q.setParameter(p.getName(), serviceMethods.get(0));
                    serviceMethods.remove(0);
                } else if (p.getName().contains(SERVICE)) {
                    q.setParameter(p.getName(), services.get(0));
                    services.remove(0);
                } else if (p.getName().contains(USER)) {
                    q.setParameter(p.getName(), users.get(0));
                    users.remove(0);
                } else if (p.getName().contains(GROUPS)) {
                    q.setParameter(p.getName(), groups.get(0));
                    groups.remove(0);
                } else if (p.getName().contains(AUTHSYSTEM)) {
                    q.setParameter(p.getName(), authSystems.get(0));
                    authSystems.remove(0);
                } else if (p.getName().contains(OID)) {
                    q.setParameter(p.getName(), oid);
                }
                else {
                    throw new IllegalStateException("Unknown Parameter: " + p.getName());
                }
            }
            return q;
        }

        private static String subListBuilder(final String COLUMN, final List<?> list) {
            if (list == null) return null;
            StringBuilder sb = new StringBuilder();
            if (list.size() > 0) sb.append("( ");
            for (int i = 0; i < list.size(); i++) {
                if (i != 0) sb.append(" or ");
                sb.append("x." + COLUMN + " = :" + COLUMN + i);
            }
            if (list.size() > 0) sb.append(" )");
            return sb.toString();
        }

        private static String baseStmt() {
            StringBuilder sb = new StringBuilder();
            sb.append("select x from ");
            sb.append(LogItem.class.getSimpleName());
            sb.append(" x" );
            return sb.toString();
        }

    }
}
