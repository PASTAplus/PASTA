package edu.lternet.pasta.auditmanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pasta.pasta_lternet_edu.log_entry_0.CategoryType;
import pasta.pasta_lternet_edu.log_entry_0.LogEntry;
import edu.lternet.pasta.auditmanager.LogItem.LogItemBuilder;
import edu.lternet.pasta.common.LogEntryFactory;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.BasicAuthToken;


public class LogServiceTest
{

    private EntityManagerFactory emf;
    private EntityManager em;
    private LogService service;
    private LogEntry entry;
    private AuthToken token;
    private int readableOid;
    private LogItemBuilder lib;

    @Before
    public void init() {
        new ConfigurationListener().setContextSpecificProperties();
        String persistenceUnit = ConfigurationListener.getJUnitPersistenceUnit();
        emf = Persistence.createEntityManagerFactory(persistenceUnit);
        em = emf.createEntityManager();
        service = new LogService(em);

        String s = BasicAuthToken.makeTokenString("anonymous", "password");
        token = new BasicAuthToken(s);
        entry = LogEntryFactory.makeDebug("AuditManager", null, null, null, "Test");
        lib = new LogItemBuilder().setCategory(CategoryType.DEBUG);
    }


    @After
    public void cleanUp() {
        if (em.isOpen()) {
            em.close();
        }
        emf.close();
    }

    @Test
    public void testClose() {
        assertTrue(em.isOpen());
        service.close();
        assertFalse(em.isOpen());
    }

    @Test
    public void testCreate() {
        readableOid = service.create(entry);
        assertNotNull(readableOid);
    }

    @Test
    public void testGet() {
        readableOid = service.create(entry);
        String str = service.get(readableOid, token);
        assertNotNull(str);
    }

    @Test
    public void testGetOids() {
        List<Integer> intlist = service.getOids(lib, token);
        assertTrue(intlist.size() != 0);
    }

    @Test
    public void testGetOidsContent() {
        String strlist = service.getOidsContent(lib, token);
        assertTrue(strlist.length() != 0);
    }
}
