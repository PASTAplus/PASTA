/* 
 * $Date$ 
 * $Author$ 
 * $Revision$
 * 
 * Copyright 2010 the University of New Mexico.
 * 
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.common;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class TestWorkingDirectory {

    
    private File workingDir;
    
    @Before
    public void init() {
        workingDir = new File("").getAbsoluteFile();
        WorkingDirectory.clear();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testClear() {
        WorkingDirectory.setWorkingDirectory(workingDir);
        WorkingDirectory.clear();
        WorkingDirectory.getWorkingDirectory();
    }
    
    @Test
    public void testSetWorkingDirectory() {
        WorkingDirectory.setWorkingDirectory(workingDir);
        assertEquals(workingDir, WorkingDirectory.getWorkingDirectory());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testGetWorkingDirectoryWithoutFirstSettingIt() {
        WorkingDirectory.getWorkingDirectory();
    }
    
    @Test
    public void testGetFile() {
        WorkingDirectory.setWorkingDirectory(workingDir);
        File file = WorkingDirectory.getFile("junit");
        assertTrue(file.isAbsolute());
        assertEquals(workingDir, file.getParentFile());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testGetFileWithoutSettingWorkingDirectoryFirst() {
        File file = WorkingDirectory.getFile("junit");
        assertTrue(file.isAbsolute());
        assertEquals(workingDir, file.getParentFile());
    }
}
