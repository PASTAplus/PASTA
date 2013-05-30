package edu.lternet.pasta.common;


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
		if (!hasNonValidHTMLCharacter(in)) {
			// If no invalid characters are detected, return the original string
			return in;
		}
		else {
			StringBuffer out = new StringBuffer();
			char current; // current character

			if (in == null || ("".equals(in)))
				return ""; // vacancy test
			
			for (int i = 0; i < in.length(); i++) {
				current = in.charAt(i);
				if (!isInvalidHTMLCharacter(current)) {
					out.append(current);
				}
				else {
					Integer anInteger = new Integer(current);
					out.append(String.format("?%d?", anInteger));
				}
			}
			String outString = out.toString();
			return outString;
		}
	}
	
	
	/*
	 * Looks at a character to determine whether it's invalid for HTML.
	 */
	private static boolean isInvalidHTMLCharacter(char c) {
		boolean invalid = false;
		
		if (c >= 127 && c < 160) {
			invalid = true;
        }
		
		return invalid;
	}

}
