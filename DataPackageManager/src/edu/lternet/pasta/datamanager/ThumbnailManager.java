package edu.lternet.pasta.datamanager;

import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;

import edu.ucsb.nceas.utilities.Options;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * The ThumbnailManager class is responsible for managing thumbnail files
 * associated with specific resources. It interacts with a data package registry
 * to retrieve resource details and manages thumbnail-related operations such as
 * locating, retrieving, and deleting thumbnail files.
 */
public class ThumbnailManager {

    private final String packageId;
    private final String resourceId;
    private final String thumbnailDir;
    private final String thumbnailFile;
    private final Integer maxThumbnailSize;

    private static final Logger logger = Logger.getLogger(ThumbnailManager.class);

    public  ThumbnailManager(String packageId, String resourceId) throws RuntimeException {

        Options options = ConfigurationListener.getOptions();
        thumbnailDir = options.getOption("datapackagemanager.thumbnailsDir");
        maxThumbnailSize = Integer.valueOf(options.getOption("datapackagemanager.maxThumbnailSize"));
        String dbDriver = options.getOption("dbDriver");
        String dbURL = options.getOption("dbURL");
        String dbUser = options.getOption("dbUser");
        String dbPassword = options.getOption("dbPassword");

        this.packageId = packageId;
        this.resourceId = resourceId;

        try {
            DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
            if (dataPackageRegistry.hasResource(resourceId)) {
                String resourceHash = getResourceHash(resourceId);
                thumbnailFile = String.format("%s/%s/%s", thumbnailDir, packageId, resourceHash);
            }
            else {
                String msg = String.format("Resource '%s' not found in resource registry.", resourceId);
                logger.error(msg);
                throw new UserErrorException(msg);
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public void createThumbnailFile(InputStream imageStream) throws RuntimeException {
        String thumbnailDirPath = String.format("%s/%s", this.thumbnailDir, packageId);
        File thumbnailDir = new File(thumbnailDirPath);
        if (!thumbnailDir.exists()) {
            if (!thumbnailDir.mkdirs()) {
                String msg = String.format("Failed to create thumbnail directory '%s'.", thumbnailDirPath);
                logger.error(msg);
                throw new RuntimeException(msg);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(thumbnailFile)) {
            byte[] thumbnailImage = readAllBytes(imageStream);
            if (thumbnailImage.length > maxThumbnailSize) {
                fos.close();
                deleteThumbnailFile();
                String msg = String.format("Thumbnail image size (%db) exceeds max allowed thumbnail size (%db) for `%s`.", thumbnailImage.length, maxThumbnailSize, resourceId);
                logger.error(msg);
                throw new UserErrorException(msg);
            }
            fos.write(thumbnailImage);
            String imageType = getImageType(thumbnailFile).toLowerCase();
            if (!imageType.equals("jpeg") && !imageType.equals("png")) {
                fos.close();
                deleteThumbnailFile();
                String msg = String.format("Image type '%s' is not supported for '%s'.", imageType, resourceId);
                logger.error(msg);
                throw new UserErrorException(msg);
            }
        }
        catch (IOException e) {
            logger.error(e);
            String msg = String.format("Thumbnail for resource '%s' failed to be created.", resourceId);
            logger.error(msg);
            throw new RuntimeException(msg);
        }
    }

    public File getThumbnailFile() throws ResourceNotFoundException {
        File file = new File(thumbnailFile);
        if (!file.exists()) {
            String msg = String.format("Thumbnail for resource '%s' not found.", resourceId);
            logger.error(msg);
            throw new ResourceNotFoundException(msg);
        }
        return file;
    }

    public void deleteThumbnailFile() throws RuntimeException {
        File file = new File(thumbnailFile);
        boolean deleted;
        if (file.exists()) {
            deleted = file.delete();
            if (!deleted) {
                String msg = String.format("Thumbnail for resource '%s' was not deleted.", resourceId);
                logger.error(msg);
                throw new RuntimeException(msg);
            }
        }
    }

    public String getThumbnailFileType() {
        String imageType = "unknown";
        try {
            imageType = getImageType(thumbnailFile);
            if (!imageType.equalsIgnoreCase("jpeg") && !imageType.equalsIgnoreCase("png")) {
                deleteThumbnailFile();
                String msg = String.format("Image type '%s' is not supported for '%s'.", imageType, resourceId);
                logger.error(msg);
                throw new UserErrorException(msg);
            }
        }
        catch (IOException e) {
          logger.error(e);
        }
        return imageType;
    }

    private String getResourceHash(String resourceId) throws RuntimeException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(resourceId.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int BUFFER_SIZE = 4096;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[BUFFER_SIZE];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private static String getImageType(String file) throws IOException {
        InputStream fileInputStream = Files.newInputStream(Paths.get(file));
        try (ImageInputStream iis = ImageIO.createImageInputStream(fileInputStream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.dispose();
                return reader.getFormatName();
            }
        }
        return "unknown";
    }
}
