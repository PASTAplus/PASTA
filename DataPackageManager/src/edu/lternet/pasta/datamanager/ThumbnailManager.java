package edu.lternet.pasta.datamanager;

import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;

import edu.ucsb.nceas.utilities.Options;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class ThumbnailManager {

    private final String resourceHash;
    private final String resourceId;
    private final String thumbnailDir;
    private final String thumbnailFile;

    private static Logger logger = Logger.getLogger(DataPackageManager.class);

    public  ThumbnailManager(String resourceId) throws UserErrorException, RuntimeException {

        Options options = ConfigurationListener.getOptions();
        String dbDriver = options.getOption("dbDriver");
        String dbURL = options.getOption("dbURL");
        String dbUser = options.getOption("dbUser");
        String dbPassword = options.getOption("dbPassword");
        thumbnailDir = options.getOption("datapackagemanager.thumbnailDir");

        this.resourceId = resourceId;

        try {
            DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
            resourceHash = getResourceHash(resourceId);
            thumbnailFile  = String.format("%s/%s.png", thumbnailDir, resourceHash);
            if (!dataPackageRegistry.hasResource(resourceId)) {
                String msg = String.format("Resource '%s' not found in resource registry.", resourceId);
                logger.error(msg);
                throw new UserErrorException(msg);
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(e);
            throw new RuntimeException(e);
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

    public void deleteThumbnailFile() throws ResourceNotFoundException {
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
    
}
