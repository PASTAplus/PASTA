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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.lternet.pasta.common.security.access.UnauthorizedException;

/**
 * Used to read and write files to and from the file system. The Java
 * class {@code java.io.File} determines the "current directory" inconsistently,
 * sometimes using the {@code user.dir} System property, and other times
 * "in a system-dependent way". To avoid this problem, all file objects supplied
 * to this class are first converted to <em>absolute</em> files.
 *
 * @see File#getAbsoluteFile()
 */
public final class FileUtility {

    private FileUtility() {
        // preventing instantiation
    }

    /**
     * Returns the absolute version of the specified file if it exists.
     *
     * @param file
     *            the file.
     * @return the absolute version of the provided file.
     * @throws ResourceNotFoundException
     *             if the absolute version of the specified file does not exist.
     */
    public static File assertExists(File file) {

        file = file.getAbsoluteFile();

        if (!file.exists()) {
            String s = "The file '" + file.getAbsolutePath() +
                       "' could not be found.";
            throw new ResourceNotFoundException(s);
        }

        return file;
    }

    /**
     * Returns the absolute version of the provided file if it exists and can be
     * read.
     *
     * @param file
     *            the file.
     * @return the absolute version of the provided file.
     * @throws ResourceNotFoundException
     *             if the absolute version of the specified file does not exist.
     * @throws UnauthorizedException
     *             if the absolute version of the specified file exists, but
     *             cannot be read.
     */
    public static File assertCanRead(File file) {

        file = assertExists(file);

        if (!file.canRead()) {
            String s = "The file '" + file.getAbsolutePath() +
                       "' cannot be read.";
            throw new UnauthorizedException(s);
        }

        return file;
    }

    /**
     * Returns the absolute version of the specified file if it exists.
     *
     * @param fileName
     *            the name of the file.
     * @return the absolute version of the specified file if it exists.
     * @throws ResourceNotFoundException
     *             if the absolute version of the specified file does not exist.
     */
    public static File assertExists(String fileName) {
        return assertExists(new File(fileName));
    }

    /**
     * Returns the absolute version of the specified file if it exists and can
     * be read.
     *
     * @param fileName
     *            the name of the file.
     * @return the absolute version of the specified file if it exists and can
     *         be read.
     * @throws ResourceNotFoundException
     *             if the absolute version of the specified file does not exist.
     * @throws UnauthorizedException
     *             if the absolute version of the specified file exists, but
     *             cannot be read.
     */
    public static File assertCanRead(String fileName) {
        return assertCanRead(new File(fileName));
    }

    /**
     * Reads the absolute version of the provided file and returns its contents
     * as a string.
     *
     * @param file
     *            the file to be read.
     *
     * @return the contents of the absolute version of the specified file.
     *
     * @throws ResourceNotFoundException
     *             if the absolute version of the specified file does not exist.
     * @throws UnauthorizedException
     *             if the absolute version of the specified file exists, but
     *             cannot be read.
     */
    public static String fileToString(File file) {

        file = assertCanRead(file);

        try {

            BufferedReader rdr = new BufferedReader(new FileReader(file));

            StringBuilder sb = new StringBuilder();
            String line = rdr.readLine();

            if (line != null) {
                sb.append(line);
            }

            while ((line = rdr.readLine()) != null) {
                sb.append(System.getProperty("line.separator"));
                sb.append(line);
            }

            rdr.close();

            return sb.toString();

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Reads the absolute version of the file with the specified name and
     * returns its contents as a string.
     *
     * @param fileName
     *            the name of the file to be read.
     *
     * @return the contents of the specified file.
     *
     * @throws ResourceNotFoundException
     *             if the specified file does not exist.
     * @throws UnauthorizedException
     *             if the absolute version of the specified file exists, but
     *             cannot be read.
     */
    public static String fileToString(String fileName) {
        return fileToString(new File(fileName));
    }

}
