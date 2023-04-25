package edu.lternet.pasta.datapackagemanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ZipStreamer {

  public void zipStream(
      OutputStream os,
      List<ZipMember> entries
  ) throws IOException
  {
    ZipOutputStream zos = new ZipOutputStream(os);
    for (ZipMember zipFile : entries) {
      ZipEntry entry = new ZipEntry(zipFile.getName());
      if (zipFile.isPath()) {
        Path path = zipFile.getPath();
        assert Files.isReadable(path);
        entry.setSize(Files.size(path));
      }
      else if (zipFile.isStream()) {
        entry.setSize(zipFile.getStreamSize());
        // entry.setLastModifiedTime(Instant.now().toEpochMilli());
      }
      else if (zipFile.isBytes()) {
        entry.setSize(zipFile.getBytes().length);
      }
      else {
        throw new RuntimeException("Invalid ZipFile");
      }

      zos.putNextEntry(entry);

      if (zipFile.isPath()) {
        InputStream stream = Files.newInputStream(zipFile.getPath());
        copyBytes(zos, stream);
      }
      else if (zipFile.isStream()) {
        InputStream stream = zipFile.getStream();
        copyBytes(zos, stream);
      }
      else if (zipFile.isBytes()) {
        InputStream stream = new ByteArrayInputStream(zipFile.getBytes());
        copyBytes(zos, stream);
      }

      zos.closeEntry();
    }
    zos.close();
  }

  private static void copyBytes(
      OutputStream dest,
      InputStream source
  )
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
