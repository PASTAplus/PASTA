/*
 *
 * $Date: 2011-02-10 13:45:37 -0700 (Thu, 10 Feb 2011) $
 * $Author: jwright $
 * $Revision: 696 $
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

package edu.lternet.pasta.metadatafactory.eml210;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.metadatafactory.eml210.ParentEml;

@RunWith(value=Parameterized.class)
public class TestParentEml {

    @Parameters
    public static Collection<?> data() {
        // emlFileName, packageId, 
        // nTitles, nCreators, nContacts, 
        // dataTableIds, entityNames
        return Arrays.asList( new Object[][] {
          { "test_1.xml", new EmlPackageId("test", 1, 0), 
             0, 0, 0, 
             Collections.emptyList(), 
             Collections.emptyList()
          },
          { "test_2.xml", new EmlPackageId("test", 2, 0), 
             1, 1, 1, 
             Collections.singletonList("id1"), 
             Collections.singletonList("name1")
          },
          { "test_3.xml", new EmlPackageId("test", 3, 0), 
             1, 1, 1, 
             Collections.emptyList(),
             Collections.emptyList()
          },
          { "test_4.xml", new EmlPackageId("test", 4, 0), 
             3, 4, 5, 
             Arrays.asList(new String[] { "1", "2" }),
             Arrays.asList(new String[] { "name1", "name2", "name3", "name4" })
          },
          { "test_5.xml", new EmlPackageId("test", 5, 0),
             0, 0, 0, 
             Arrays.asList(new String[] { "1", "1", "1", "1" }),
             Arrays.asList(new String[] { "name", "name", "name", "name" })
          }
        });
    }
    
    private ParentEml parent;
    private Document doc;
    private EmlPackageId packageId;
    private int nTitles;
    private int nCreators;
    private int nContacts;
    private List<?> dataTableIds;
    private List<?> entityNames;
    
    public TestParentEml(String emlFileName,
                         EmlPackageId packageId,
                         int nTitles,
                         int nCreators,
                         int nContacts,
                         List<?> dataTableIds,
                         List<?> entityNames) throws Exception {

        doc = parseEml(emlFileName);
        
        parent = new ParentEml(doc);
        
        this.packageId = packageId;
        this.nTitles = nTitles;
        this.nCreators = nCreators;
        this.nContacts = nContacts;
        this.dataTableIds = dataTableIds;
        this.entityNames = entityNames;
    }
    
    private Document parseEml(String fileName) throws Exception {
        File file = new File("test/data/provenance/" + fileName);
        file = FileUtility.assertCanRead(file);
        return XmlUtility.xmlStringToDoc(FileUtility.fileToString(file));
    }
    
    private void checkList(int expectedSize, List<?> actual) {
        String msg = "Should return list, not null.";
        assertNotNull(msg, actual);
        msg = "Lists not the same size.";
        assertEquals(msg, expectedSize, actual.size());
    }
    
    private void checkList(List<?> expected, List<?> actual) {
        checkList(expected.size(), actual);
        String msg = "List elements differ.";
        assertTrue(msg, actual.containsAll(expected));
    }
    
    @Test
    public void testGetDocument() {
        assertSame(doc, parent.getDocument());
    }
    
    @Test
    public void testGetPackageId() {
        assertEquals(packageId, parent.getPackageId());
    }
    
    @Test
    public void testGetTitlesNumber() {
        List<Node> titles = parent.getTitles();
        checkList(nTitles, titles);
    }
    
    @Test
    public void testGetCreatorsNumber() {
        List<Node> creators = parent.getCreators();
        checkList(nCreators, creators);
    }
    
    @Test
    public void testGetContactsNumber() {
        List<Node> contacts = parent.getContacts();
        checkList(nContacts, contacts);
    }

    @Test
    public void testGetDataTableIds() {
        List<String> ids = parent.getDataTableIds();
        checkList(dataTableIds, ids);
    }

    @Test
    public void testGetEntityNames() {
        List<String> names = parent.getDataTableEntityNames();
        checkList(entityNames, names);
    }
    
}
