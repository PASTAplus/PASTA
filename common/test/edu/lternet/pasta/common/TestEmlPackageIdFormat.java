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

package edu.lternet.pasta.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;

@RunWith(Enclosed.class)
public class TestEmlPackageIdFormat {

    public static class SimpleTests {
        
        private EmlPackageIdFormat f;

        @Before
        public void before() {
            f = new EmlPackageIdFormat();
        }

        @Test
        public void testDefaultDelimiterEquality() {
            f = new EmlPackageIdFormat();
            assertEquals(f.getDelimiter(), EmlPackageIdFormat.DEFAULT_DELIMITER);
        }

        @Test
        public void testProvidedDelimiterEquality() {
            for (Delimiter d : Delimiter.values()) {
                f = new EmlPackageIdFormat(d);
                assertEquals(d, f.getDelimiter());
            }
        }

        @Test(expected = NullPointerException.class)
        public void testNullDelimiter() {
            new EmlPackageIdFormat(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testParse1ArgWithTooManyElements() {
            f.parse("x.1.1.1");
        }

        @Test(expected = NullPointerException.class)
        public void testParse1ArgWithNull() {
            f.parse(null);
        }

        @Test
        public void testParse1ArgWithEmptyString() {
            EmlPackageId epi1 = f.parse("");
            assertNull(epi1.getScope());
            assertNull(epi1.getIdentifier());
            assertNull(epi1.getRevision());
        }
        
        @Test
        public void testParse1ArgWithDifferentDelimiters() {
            EmlPackageId epi1 = f.parse("x.1.2");
            f = new EmlPackageIdFormat(Delimiter.FORWARD_SLASH);
            EmlPackageId epi2 = f.parse("x/1/2");
            assertEquals(epi1, epi2);
        }

        @Test
        public void testParse1ArgWithTrailingDelimiters() {
            EmlPackageId epi = f.parse("x.1.2....");
            assertEquals("x", epi.getScope());
            assertEquals(new Integer(1), epi.getIdentifier());
            assertEquals(new Integer(2), epi.getRevision());
        }
        
        @Test
        public void testParseScope() {
            String test = "test-scope";
            assertEquals(test, f.parseScope(test, null, null));
            assertSame(test, f.parseScope(test, null, null));
            assertNull(f.parseScope("", null, null));
            assertNull(f.parseScope(null, null, null));
        }
        
        @Test(expected = IllegalArgumentException.class)
        public void testParseScopeWithIllegalCharacters() {
            String test = "test-scope.?/!@#$%^&*()";
            f.parseScope(test, null, null);
        }

        @Test
        public void testParseIdentifierNullAndEmpty() {
            assertNull(f.parseIdentifier(null, null, null));
            assertNull(f.parseIdentifier(null, null, ""));
        }

        @Test
        public void testParseRevisionNullAndEmpty() {
            assertNull(f.parseRevision(null, null, null));
            assertNull(f.parseRevision(null, null, ""));
        }

        @Test(expected = IllegalArgumentException.class)
        public void testParseIdentifierWithNonNumber() {
            f.parseIdentifier(null, "not a number", null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testParseRevisionWithNonNumber() {
            f.parseRevision(null, null, "not a number");
        }

        @Test(expected = IllegalArgumentException.class)
        public void testParseIdentifierWithNegativeNumber() {
            f.parseIdentifier(null, "-1", null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void testParseRevisionWithNegativeNumber() {
            f.parseRevision(null, null, "-1");
        }

    }
    
    @RunWith(value=Parameterized.class)
    public static class TestParameterizedSuccesses {

        @Parameters
        public static Collection<?> data() {

            // scope, id, revision, full string, sting elements to be parsed
            return Arrays.asList( new Object[][] {
                    { null, null, null,      "",   "",   "",   "" },
                    { null, null, null,      "", null, null, null },
                    {  "x", null, null,     "x",  "x",   "",   "" },
                    {  "x", null, null,     "x",  "x", null, null },
                    {  "x",    1, null,   "x.1",  "x",  "1",   "" },
                    {  "x",    1, null,   "x.1",  "x",  "1", null },
                    {  "x",    1,    2, "x.1.2",  "x",  "1",  "2" }
            });
            
        }
        
        private EmlPackageId epi;
        private String full;
        private String scopeString;
        private String idString;
        private String revisionString;
        private EmlPackageIdFormat f;
        
        public TestParameterizedSuccesses(String scope, 
                                          Integer id, 
                                          Integer revision,
                                          String full,
                                          String scopeString,
                                          String idString,
                                          String revisionString) {
            epi = new EmlPackageId(scope, id, revision);
            this.full = full;
            this.scopeString = scopeString;
            this.idString = idString;
            this.revisionString = revisionString;
        }
        
        @Before
        public void before() {
            f = new EmlPackageIdFormat();
        }
        
        @Test
        public void testFormat() {
            assertEquals(full, f.format(epi));
        }
        
        @Test
        public void testParse1Arg() {
            assertEquals(epi, f.parse(full));
        }
        
        @Test
        public void testParse3Arg() {
            
            EmlPackageId parsed = 
                f.parse(scopeString, idString, revisionString);
            
            assertEquals(epi.getScope(), parsed.getScope());
            assertEquals(epi.getIdentifier(), parsed.getIdentifier());
            assertEquals(epi.getRevision(), parsed.getRevision());
        }
        
        @Test
        public void testParseId() {
            Integer x = f.parseIdentifier(null, idString, null);
            assertEquals(epi.getIdentifier(), x);
        }
        
        @Test
        public void testParseRevision() {
            Integer x = f.parseRevision(null, null, revisionString);
            assertEquals(epi.getRevision(), x);
        }
        
        @Test
        public void testTransitivity() {
            assertEquals(epi, f.parse(f.format(epi)));
            assertEquals(full, f.format(f.parse(full)));
        }
    }
    
    @RunWith(value=Parameterized.class)
    public static class TestParameterizedExceptions {
        
        @Parameters
        public static Collection<?> data() {
            
            // scope, id, revision to be parsed
            return Arrays.asList( new Object[][] {
                    { null, null,  "2" },
                    {   "",   "",  "2" },
                    { null,  "1",  "2" },
                    {   "",  "1",  "2" },
                    {  "x", null,  "2" },
                    {  "x",   "",  "2" },
                    { null,  "1", null },
                    {   "",  "1",   "" },
                    {   "", null,  "2" },
                    {   "",   "",  "2" },
                    {   "",  "1", null }
            });
            
        }
        
        private String scope;
        private String id;
        private String revision;
        
        public TestParameterizedExceptions(String scope,
                                           String id, 
                                           String revision) {
            this.scope = scope;
            this.id = id;
            this.revision = revision; 
        }
        
        @Test(expected=IllegalArgumentException.class)
        public void testParse3ArgWithBadArgCombinations() {
            EmlPackageIdFormat f = new EmlPackageIdFormat();
            f.parse(scope, id, revision);
        }
    }
}
