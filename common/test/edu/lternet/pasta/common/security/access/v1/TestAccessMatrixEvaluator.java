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

package edu.lternet.pasta.common.security.access.v1;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.lternet.pasta.common.security.access.v1.AccessMatrix.Order;
import edu.lternet.pasta.common.security.access.v1.AccessMatrix.Permission;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AttrListAuthTokenV1;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.BasicAuthToken;

@RunWith(value=Enclosed.class)
public class TestAccessMatrixEvaluator {

    public static void assertPermissions(AccessMatrix matrix, 
                                         AuthToken token,
                                         boolean read,
                                         boolean write,
                                         boolean change,
                                         boolean all) {

        AccessMatrixEvaluator eval = new AccessMatrixEvaluator();
        
        assertSame("read", read, eval.canRead(token, matrix));
        assertSame("write", write, eval.canWrite(token, matrix));
        assertSame("change", change, eval.canChangePermission(token, matrix));
        assertSame("all", all, eval.canAll(token, matrix));
    }
    
    public static class TestCornerCases {
        
        private static final String PUBLIC = "public";
        private String user;
        private String group;
        private AuthToken token;
        private AccessMatrix matrix;

        @Before
        public void init() {
            user = "me";
            group = "group";
            Set<String> groups = Collections.singleton(group);
            token = new AttrListAuthTokenV1(user, AuthSystemDef.KNB, 0, groups);
        }

        @Test
        public void testAllowUserWriteDenyUserWrite() {

            matrix = new AccessMatrix(Order.ALLOW_FIRST);

            matrix.addAllowRule(user, Permission.WRITE);
            assertPermissions(matrix, token, true, true, false, false);

            matrix.addDenyRule(user, Permission.WRITE);
            assertPermissions(matrix, token, true, false, false, false);
        }

        @Test
        public void testAllowUserChangePermissionDenyUserChangePermission() {

            matrix = new AccessMatrix(Order.ALLOW_FIRST);

            matrix.addAllowRule(user, Permission.CHANGE_PERMISSION);
            assertPermissions(matrix, token, true, true, true, true);

            matrix.addDenyRule(user, Permission.CHANGE_PERMISSION);
            assertPermissions(matrix, token, true, true, false, false);
        }

        @Test
        public void testAllowUserChangePermissionDenyUserRead() {

            matrix = new AccessMatrix(Order.ALLOW_FIRST);

            matrix.addAllowRule(user, Permission.CHANGE_PERMISSION);
            assertPermissions(matrix, token, true, true, true, true);

            matrix.addDenyRule(user, Permission.READ);
            assertPermissions(matrix, token, false, false, false, false);
        }

        @Test
        public void testDenyUserAllowPublic() {

            matrix = new AccessMatrix(Order.DENY_FIRST);
            assertPermissions(matrix, token, false, false, false, false);
            
            matrix.addDenyRule(user, Permission.READ);
            assertPermissions(matrix, token, false, false, false, false);
            
            matrix.addAllowRule(PUBLIC, Permission.READ);
            assertPermissions(matrix, token, true, false, false, false);
        }

        @Test
        public void testDenyPublicAllowUser() {

            matrix = new AccessMatrix(Order.DENY_FIRST);
            assertPermissions(matrix, token, false, false, false, false);
            
            matrix.addDenyRule(PUBLIC, Permission.READ);
            assertPermissions(matrix, token, false, false, false, false);
            
            matrix.addAllowRule(user, Permission.READ);
            assertPermissions(matrix, token, true, false, false, false);
        }

        @Test
        public void testAllowUserDenyPublic() {

            matrix = new AccessMatrix(Order.ALLOW_FIRST);
            
            matrix.addAllowRule(user, Permission.WRITE);
            assertPermissions(matrix, token, true, true, false, false);
            
            matrix.addDenyRule(PUBLIC, Permission.READ);
            assertPermissions(matrix, token, true, true, false, false);
        }

        @Test
        public void testAllowPublicDenyUser() {

            matrix = new AccessMatrix(Order.ALLOW_FIRST);
            
            matrix.addAllowRule(PUBLIC, Permission.WRITE);
            assertPermissions(matrix, token, true, true, false, false);
            
            matrix.addDenyRule(user, Permission.READ);
            assertPermissions(matrix, token, true, true, false, false);
        }
        
        @Test
        public void testDenyUserAllowHisGroup() {

            matrix = new AccessMatrix(Order.DENY_FIRST);
            
            matrix.addDenyRule(user, Permission.READ);
            assertPermissions(matrix, token, false, false, false, false);
            
            matrix.addAllowRule(group, Permission.CHANGE_PERMISSION);
            assertPermissions(matrix, token, true, true, true, true);
        }
        
        @Test
        public void testDenyGroupAllowUser() {

            matrix = new AccessMatrix(Order.DENY_FIRST);
            
            matrix.addDenyRule(group, Permission.READ);
            assertPermissions(matrix, token, false, false, false, false);
            
            matrix.addAllowRule(user, Permission.CHANGE_PERMISSION);
            assertPermissions(matrix, token, true, true, true, true);
        }
        
        @Test
        public void testAllowUserDenyHisGroup() {

            matrix = new AccessMatrix(Order.ALLOW_FIRST);
            
            matrix.addAllowRule(user, Permission.CHANGE_PERMISSION);
            assertPermissions(matrix, token, true, true, true, true);
            
            matrix.addDenyRule(group, Permission.READ);
            assertPermissions(matrix, token, false, false, false, false);
        }
        
        @Test
        public void testAllowGroupDenyUser() {

            matrix = new AccessMatrix(Order.ALLOW_FIRST);
            
            matrix.addAllowRule(group, Permission.CHANGE_PERMISSION);
            assertPermissions(matrix, token, true, true, true, true);
            
            matrix.addDenyRule(user, Permission.READ);
            assertPermissions(matrix, token, false, false, false, false);
        }
    }

    @RunWith(value = Parameterized.class)
    public static class TestCommonUseCases {
        

        @Parameters
        public static Collection<?> data() {

            Collection<Object[]> data = new LinkedList<Object[]>();
            
            add(data, Permission.READ, true, false, false, false);
            add(data, Permission.WRITE, true, true, false, false);
            add(data, Permission.CHANGE_PERMISSION, true, true, true, true);
            
            return data;
        }
        
        private static void add(Collection<Object[]> data, 
                                Permission permission,
                                boolean read,
                                boolean write,
                                boolean change,
                                boolean all) {
            
            String permitted = "me";
            String blocked = "you";
            
            AuthToken pToken = new BasicAuthToken(permitted, "password");
            AuthToken bToken = new BasicAuthToken(blocked, "password");
            
            AccessMatrix allowFirst = new AccessMatrix(Order.ALLOW_FIRST);
            AccessMatrix denyFirst = new AccessMatrix(Order.DENY_FIRST);
            
            allowFirst.addAllowRule(permitted, permission);
            denyFirst.addAllowRule(permitted, permission);
            
            Object[] d = null;
            
            d = new Object[] { allowFirst, pToken, read, write, change, all};
            data.add(d);
            
            d = new Object[] { allowFirst, bToken, false, false, false, false};
            data.add(d);
            
            d = new Object[] { denyFirst, pToken, read, write, change, all};
            data.add(d);
            
            d = new Object[] { denyFirst, bToken, false, false, false, false};
            data.add(d);
        }
        
        private final AccessMatrixEvaluator eval;
        private final AccessMatrix matrix;
        private final AuthToken token;
        private final boolean read;
        private final boolean write;
        private final boolean change;
        private final boolean all;
        
        public TestCommonUseCases(AccessMatrix matrix,
                               AuthToken token,
                               boolean read,
                               boolean write,
                               boolean changePermission,
                               boolean all) {
            
            eval = new AccessMatrixEvaluator();
            this.matrix = matrix;
            this.token = token;
            this.read = read;
            this.write = write;
            this.change = changePermission;
            this.all = all;
        }
        
        @Test
        public void testRead() {
            assertSame(read, eval.canRead(token, matrix));
        }
        
        @Test
        public void testWrite() {
            assertSame(write, eval.canWrite(token, matrix));
        }
        
        @Test
        public void testChangePermission() {
            assertSame(change, eval.canChangePermission(token, matrix));
        }
        
        @Test
        public void testAll() {
            assertSame(all, eval.canAll(token, matrix));
        }
    }
}
