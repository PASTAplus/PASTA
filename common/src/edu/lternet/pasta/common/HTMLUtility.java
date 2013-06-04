/*
 * Copyright 2011-2013 the University of New Mexico.
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
 */

package edu.lternet.pasta.common;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class HTMLUtility {
	
    /**
	   * Detects whether a string contains non-valid HTML control characters.
	   *
	   * @param in The String we want to check for invalid HTML characters.
	   * @return true if one or more invalid characters were found, else false
	   */
	  public static boolean hasNonValidHTMLCharacter(String in) {
		  boolean hasInvalid = false;
	      char current; // current character

	      if (in == null || ("".equals(in))) return false; // vacancy test

	      for (int i = 0; i < in.length(); i++) {
	          current = in.charAt(i);

	          if (isInvalidHTMLCharacter(current)) {
	              hasInvalid = true;
	              break;
	          }
	      }
	      
	      return hasInvalid;
	  }


  /**
   * This method helps to ensure that the output String has only valid HTML
   * characters by stripping out invalid control characters.
   *
   * @param in The String whose non-valid characters we want to remove.
   * @return The input String, stripped of non-valid characters.
   */
  public static String stripNonValidHTMLCharacters(String in) {

    char[] charBuf = null;

    if (in != null && !in.isEmpty()) {

      charBuf = in.toCharArray();
      int charBufSize = charBuf.length;

      for (int i = 0; i < charBufSize; i++) {

        if (isInvalidHTMLCharacter(charBuf[i])) {
          charBuf[i] = '\uFFFD';  // Replace illegal character with replacement character
        }

      }

    }

    return new String(charBuf);

  }


  /*
	 * Looks at a character to determine whether it's invalid for HTML.
	 */
	private static boolean isInvalidHTMLCharacter(char c) {
		boolean invalid = false;

    // C0 control characters
    if ((int) c >= 0 && (int) c <= 31) {
      // Allowed characters tab (9), linefeed (10), and carriage return (13)
      if ((int) c != 9 && (int) c != 10 && (int) c != 13 ) {
        invalid = true;
      }
    }

    // DEL character
    else if ((int) c == 127) {
      invalid = true;
    }

    // C1 control characters
     else if ((int) c >= 128 && (int) c <= 159) {
			invalid = true;
    }

    // UTF-16 surrogate halves
    else if ((int) c >= 55296 && (int) c <= 57343) {
      invalid = true;
    }

    return invalid;
	}


  public static void main(String[] args) {

    File badIn = new File("/Users/servilla/tmp/bad.txt");
    File goodOut = new File("/Users/servilla/tmp/good.txt");

    String bad = null;

    try {
      bad = FileUtils.readFileToString(badIn, "UTF-8");
    } catch (IOException e) {
      System.err.println("HTMLUtility: " + e.getMessage());
      e.printStackTrace();
    }

    String good = stripNonValidHTMLCharacters(bad);

    try {
      FileUtils.writeStringToFile(goodOut, good, "UTF-8");
    } catch (IOException e) {
      System.err.println("HTMLUtility: " + e.getMessage());
      e.printStackTrace();
    }

  }

}
