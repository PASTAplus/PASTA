package edu.lternet.pasta.datapackagemanager.checksum;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;


/**
 * A small wrapper class that uses the Apache Commons Codec DigestUtils 
 * class to calculate SHA-1 checksum. This wrapper could easily be
 * extended to support other checksums available through the DigestUtils
 * class.
 * 
 * @author dcosta
 *
 */
public class DigestUtilsWrapper {
  
  public static String getSHA1Checksum(File file) throws Exception {
    InputStream fis =  new FileInputStream(file);
    String shaHex = DigestUtils.shaHex(fis);
    return shaHex;
  }

  
  public static String getSHA1Checksum(String filename) throws Exception {
    InputStream fis =  new FileInputStream(filename);
    String shaHex = DigestUtils.shaHex(fis);
    return shaHex;
  }

  
  /**
   * Main method to test the getSHA1Checksum() method. Pass in the full
   * path to the filename as the sole command-line argument.
   * 
   * @param args
   */
  public static void main(String args[]) {
    try {
      System.out.println(getSHA1Checksum(args[0]));
    }
    catch (Exception e) {
        e.printStackTrace();
    }
  }
  
}
