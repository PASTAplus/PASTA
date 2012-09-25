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

import java.io.File;

/**
 * Used to make common code aware of the "working" directory. If files must be
 * read or written to a file system from the common code, a path must be
 * specified. Absolute paths can rarely be used, because the common code
 * should be able to run in multiple environments. Therefore, relative paths
 * must be primarily used, but they must specified relative to a particular
 * directory, i.e the "working" directory. That is the purpose of this class.
 *
 *
 */
public final class WorkingDirectory {

    private WorkingDirectory() {
        // prevents instantiation
    }

    private static File workingDir;

    /**
     * Clears the current working directory, as if it has never been set.
     */
    public static void clear() {
        workingDir = null;
    }

    /**
     * Sets the working directory.
     *
     * @param workingDir
     *            the working directory.
     *
     * @throws ResourceNotFoundException
     *             if the provided file does not exist or cannot be found.
     * @throws IllegalArgumentException
     *             if the provided file is not a directory.
     */
    public static void setWorkingDirectory(File workingDir) {

        workingDir = FileUtility.assertCanRead(workingDir);

        if (!workingDir.isDirectory()) {
            String s = "The provided file '" + workingDir.getAbsolutePath() +
                       "' is not a directory.";
            throw new IllegalArgumentException(s);
        }

        WorkingDirectory.workingDir = workingDir;
    }

    /**
     * Returns the working directory.
     *
     * @return the working directory.
     *
     * @throws IllegalStateException
     *             if the working directory was never set.
     */
    public static File getWorkingDirectory() {
        assertDirectoryWasSet();
        return workingDir;
    }

    /**
     * Returns a file with an absolute path by specifying it relative to the
     * working directory.
     *
     * @param fileName
     *            a relative file path.
     * @return the specified file relative to the working directory.
     *
     * @throws IllegalStateException
     *             if the working directory was never set.
     */
    public static File getFile(String fileName) {
        assertDirectoryWasSet();
        return new File(workingDir, fileName).getAbsoluteFile();
    }

    private static void assertDirectoryWasSet() {
        if (workingDir == null) {
            throw new IllegalStateException("working directory never set");
        }
    }
}
