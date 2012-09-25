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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.lternet.pasta.common.EmlPackageId;

@RunWith(Enclosed.class)
public class TestEmlPackageId {

    public static class SimpleTests {
        
        @Test
        public void testGetIllegalCharactersWithEmptyString() {
            String test = "";
            assertEquals(0, EmlPackageId.getIllegalCharacters(test).size());
        }
        
        @Test
        public void testGetIllegalCharactersWithIllegalCharacters() {
            String test = "aa.$#@(%$)bbb_--c";
            assertEquals(9, EmlPackageId.getIllegalCharacters(test).size());
        }
        
        @Test
        public void testGetIllegalCharactersWithoutIllegalCharacters() {
            String test = "abc123---";
            assertEquals(0, EmlPackageId.getIllegalCharacters(test).size());
        }
        
        @Test(expected=NullPointerException.class)
        public void testGetIllegalCharactersWithNullString() {
            EmlPackageId.getIllegalCharacters(null);
        }
        
        @Test
        public void testIsLevelOne() {
          final String LEVEL_ZERO = "knb-lter-abc";
          final String LEVEL_ONE = LEVEL_ZERO + EmlPackageId.getLevelOneSuffix();
          EmlPackageId epiLevelZero = new EmlPackageId(LEVEL_ZERO, 1, 1);
          assertFalse(epiLevelZero.isLevelOne());
          EmlPackageId epiLevelOne = new EmlPackageId(LEVEL_ONE, 1, 1);
          assertTrue(epiLevelOne.isLevelOne());
        }
        
        @Test
        public void testToLevelOne() {
          final String LEVEL_ZERO = "knb-lter-abc";
          final String LEVEL_ONE =  LEVEL_ZERO + EmlPackageId.getLevelOneSuffix();
          EmlPackageId levelZero = new EmlPackageId(LEVEL_ZERO, 1, 1);
          EmlPackageId levelOne = new EmlPackageId(LEVEL_ONE, 1, 1);
          EmlPackageId levelZeroToLevelOne = levelZero.toLevelOne();
          assertEquals(levelZeroToLevelOne, levelOne);
        }
        
    }
    
    @RunWith(value = Parameterized.class)
    public static class TestParameterizedSuccesses {

        @Parameters
        public static Collection<?> data() {

            // scope, id, revision, full packageId, non-null elements
            return Arrays.asList(new Object[][] {
                    { null, null, null, false, 0 },
                    {  "x", null, null, false, 1 }, 
                    { "x1",    1, null, false, 2 },
                    { "x-",    1,    2,  true, 3 } 
            });

        }

        private String scope;
        private Integer id;
        private Integer revision;
        private boolean allElements;
        private int nonNulls;

        public TestParameterizedSuccesses(String scope, 
                                          Integer id, 
                                          Integer revision,
                                          boolean allElements, 
                                          int nonNulls) {
            this.scope = scope;
            this.id = id;
            this.revision = revision;
            this.allElements = allElements;
            this.nonNulls = nonNulls;
        }

        @Test
        public void testGetScope() {
            EmlPackageId epi = new EmlPackageId(scope, id, revision);
            assertEquals(scope, epi.getScope());
        }

        @Test
        public void testGetId() {
            EmlPackageId epi = new EmlPackageId(scope, id, revision);
            assertEquals(id, epi.getIdentifier());
        }

        @Test
        public void testGetRevision() {
            EmlPackageId epi = new EmlPackageId(scope, id, revision);
            assertEquals(revision, epi.getRevision());
        }

        @Test
        public void testNonNullElements() {
            EmlPackageId epi = new EmlPackageId(scope, id, revision);
            assertEquals(nonNulls, epi.nonNullElements());
        }

        @Test
        public void testHasAllElements() {
            EmlPackageId epi = new EmlPackageId(scope, id, revision);
            assertEquals(allElements, epi.allElementsHaveValues());
        }
    }
    
    @RunWith(value=Parameterized.class)
    public static class TestEmlPackageIdExceptions {
        
        @Parameters
        public static Collection<?> data() {
            
            // scope, id, revision
            return Arrays.asList( new Object[][] {
                    { null, null,    2 }, // bad combination
                    { null,    1,    2 }, // bad combination
                    {  "x", null,    2 }, // bad combination
                    { null,    1, null }, // bad combination
                    {   "", null, null }, // bad scope
                    {  ".", null, null }, // bad scope
                    {  "/", null, null }, // bad scope
                    {  "x",   -1, null }, // bad identifier
                    {  "x",    1,   -2 }  // bad revision
            });
            
        }
        
        private String scope;
        private Integer id;
        private Integer revision;
        
        public TestEmlPackageIdExceptions(String scope,
                                          Integer id, 
                                          Integer revision) {
            this.scope = scope;
            this.id = id;
            this.revision = revision;
        }
        
        @Test(expected=IllegalArgumentException.class)
        public void testConstructorFailure() {
            new EmlPackageId(scope, id, revision);
        }
    }
}
