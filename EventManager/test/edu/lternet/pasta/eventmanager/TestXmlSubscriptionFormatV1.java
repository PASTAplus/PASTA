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

package edu.lternet.pasta.eventmanager;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.XmlParsingException;
import edu.lternet.pasta.eventmanager.EmlSubscription;
import edu.lternet.pasta.eventmanager.SubscribedUrl;
import edu.lternet.pasta.eventmanager.XmlSubscriptionFormatV1;
import edu.lternet.pasta.eventmanager.EmlSubscription.SubscriptionBuilder;

public class TestXmlSubscriptionFormatV1 {
    
    public static final String HOME = 
        "test/edu/lternet/pasta/eventmanager/META-INF";
    
    public static String makeFileName(String fileName) {
        return new File(HOME, fileName).getAbsolutePath();
    }
    
    private String goodFile;
    private String badFile;
    private XmlSubscriptionFormatV1 formatter;
    
    @Before
    public void init() {
        new ConfigurationListener().setContextSpecificProperties();
        goodFile = makeFileName("good_eml_subscription.xml");
        badFile = makeFileName("bad_eml_subscription.xml");
        formatter = new XmlSubscriptionFormatV1();
    }
    
    @Test
    public void testParseWithGoodXml() {
        
        String xml = FileUtility.fileToString(goodFile);
        
        SubscriptionBuilder sb = formatter.parse(xml);
        EmlPackageId epi = sb.getEmlPackageId();
        
        assertNull(sb.getCreator());
        assertEquals("lter-lno", epi.getScope());
        assertEquals(12, epi.getIdentifier().intValue());
        assertEquals(74, epi.getRevision().intValue());
        assertEquals("http://foo?bar&blah", sb.getUrl().toString());
    }
    
    @Test(expected=XmlParsingException.class)
    public void testParseWithBadXml() {
        String xml = FileUtility.fileToString(badFile);
        formatter.parse(xml);
    }
    
    private EmlSubscription makeSubscription() {
        
        SubscriptionBuilder sb = new SubscriptionBuilder();
        sb.setCreator("jwright");
        sb.setEmlPackageId(new EmlPackageId("lter-lno", 12, 74));
        sb.setUrl(new SubscribedUrl("http://foo?bar&blah")); // with &
        
        return sb.build();
    }
    
    @Test
    public void testFormatSingle() {
        
        String xml = formatter.format(makeSubscription());

        assertTrue(xml.contains("<creator>jwright</creator>"));
        assertTrue(xml.contains("<packageId>lter-lno.12.74</packageId>"));        
        assertTrue(xml.contains("<url>http://foo?bar&amp;blah</url>"));        
        assertFalse(xml.contains("<id>"));
    }
    
    @Test
    public void testFormatCollection() {
        
        Collection<EmlSubscription> c = Collections.singleton(makeSubscription());
        
        String xml = formatter.format(c);
        
        assertTrue(xml.contains("<subscriptions>"));
        assertTrue(xml.contains("<creator>jwright</creator>"));
        assertTrue(xml.contains("<packageId>lter-lno.12.74</packageId>"));        
        assertTrue(xml.contains("<url>http://foo?bar&amp;blah</url>"));        
        assertFalse(xml.contains("<id>"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testFormatSingleWithInactive() {
        EmlSubscription s = makeSubscription();
        s.inactivate();
        formatter.format(s);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFormatCollectionWithInactive() {
        
        EmlSubscription s = makeSubscription();
        s.inactivate();
        
        Collection<EmlSubscription> c = Collections.singleton(s);
        formatter.format(c);
    }
}
