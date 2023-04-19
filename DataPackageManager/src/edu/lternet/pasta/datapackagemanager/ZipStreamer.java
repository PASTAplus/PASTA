package edu.lternet.pasta.datapackagemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipStreamer {

  public void zipStream(OutputStream os, List<String> entries) throws IOException
  {
    ZipOutputStream zos = new ZipOutputStream(os);
    for (String e : entries) {
      ZipEntry entry = new ZipEntry(e);
      Path path = Paths.get(e);
      FileTime lastModifiedTime = Files.getLastModifiedTime(path);
      entry.setTime(lastModifiedTime.toMillis());
      zos.putNextEntry(entry);
      if (Files.isReadable(path)) {
        copyBytes(zos, Files.newInputStream(path));
      }
      zos.closeEntry();
    }
    zos.close();
  }

  private static void copyBytes(OutputStream dest, InputStream source)
  {
    byte[] buffer = new byte[1024];
    int bytesRead;
    try {
      while ((bytesRead = source.read(buffer)) != -1) {
        dest.write(buffer, 0, bytesRead);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
