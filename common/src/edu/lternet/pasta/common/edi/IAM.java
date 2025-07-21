package edu.lternet.pasta.common.edi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The IAM (Identity and Access Management) class provides methods to interact
 * with an external authentication service to check user permissions.
 */
public class IAM {

    private static final String AUTH_HOST = "https://auth-d.edirepository.org";

    /**
     * Defines the possible permission levels for a resource. Using an enum is a
     * best practice for type safety and clarity.
     */
    public enum Permission {
        READ,
        WRITE,
        CHANGEPERMISSION
    }

    /**
     * Determines if a user is authorized to access a resource with a specific permission level.
     * This method fulfills the request to use a String for the permission level.
     *
     * @param userId         The identifier for the user (e.g., "uid=EDI-X,o=EDI,dc=edirepository,dc=org").
     * @param resourceId     The identifier for the resource (e.g., "edi.1.1").
     * @param permission     The permission level to check ("READ", "WRITE", or "CHANGEPERMISSION").
     * @return true if the user is authorized, false otherwise.
     * @throws IOException if an I/O error occurs when communicating with the auth service.
     * @throws IllegalArgumentException if the provided permission string is invalid.
     */
    public boolean isAuthorized(String userId, String resourceId, String permission) throws IOException {
        Permission permissionEnum;
        try {
            // Validate and convert string permission to the enum to ensure it's a valid value.
            permissionEnum = Permission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid permission level specified: " + permission, e);
        }
        // Delegate to the type-safe method
        return isAuthorized(userId, resourceId, permissionEnum);
    }

    /**
     * Determines if a user is authorized to access a resource with a specific permission level.
     * This is an overloaded, type-safe method that uses the {@link Permission} enum.
     *
     * @param userId         The identifier for the user (e.g., "uid=EDI-X,o=EDI,dc=edirepository,dc=org").
     * @param resourceId     The identifier for the resource (e.g., "edi.1.1").
     * @param permission     The permission level to check as a {@link Permission} enum.
     * @return true if the user is authorized, false otherwise.
     * @throws IOException if an I/O error occurs when communicating with the auth service.
     */
    public boolean isAuthorized(String userId, String resourceId, Permission permission) throws IOException {
        // URL-encode parameters to ensure they are safe for transit
        String encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8.name());
        String encodedResourceId = URLEncoder.encode(resourceId, StandardCharsets.UTF_8.name());
        // The auth service expects lowercase permission names
        String encodedPermission = URLEncoder.encode(permission.name().toLowerCase(), StandardCharsets.UTF_8.name());

        String urlString = String.format(
                "%s/auth/authorized?uid=%s&resource_id=%s&permission=%s",
                AUTH_HOST, encodedUserId, encodedResourceId, encodedPermission
        );

        URL url = new URL(urlString);
        HttpURLConnection connection = null;
        boolean isAuthorized = false;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000);    // 5 seconds

            int responseCode = connection.getResponseCode();

            // The auth service returns 200 OK for authorized, 401 Unauthorized otherwise.
            if (responseCode == HttpURLConnection.HTTP_OK) {
                isAuthorized = true;
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                isAuthorized = false;
            } else {
                // Handle other unexpected HTTP responses by throwing an exception with details.
                String responseMessage = readResponse(connection);
                throw new IOException(
                        String.format(
                                "Unexpected response from auth service for URL '%s': %d %s. Response body: %s",
                                urlString, responseCode, connection.getResponseMessage(), responseMessage
                        )
                );
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return isAuthorized;
    }

    /**
     * Helper method to read the response body from an HttpURLConnection, useful for debugging.
     * It checks both the error stream and the input stream.
     *
     * @param connection The active connection.
     * @return The response body as a String.
     */
    private String readResponse(HttpURLConnection connection) {
        try (InputStream stream = connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (IOException e) {
            // This might happen if the connection is already closed. Return a diagnostic message.
            return "[Could not read response body: " + e.getMessage() + "]";
        }
    }

    /**
     * Example usage. This main method can be run to test the functionality.
     */
    public static void main(String[] args) {
        IAM iam = new IAM();
        // Note: The user ID is typically a full Distinguished Name (DN)
        String publicUser = "public";
        String testResourceId = "knb-lter-lno.1.1"; // A public resource

        System.out.println("--- Testing Authorization ---");

        try {
            // Test Case 1: Public user trying to read a public resource (should be true)
            boolean canRead = iam.isAuthorized(publicUser, testResourceId, "READ");
            System.out.printf("Public user can READ '%s'? %s\n", testResourceId, canRead);

            // Test Case 2: Public user trying to write to a resource (should be false)
            boolean canWrite = iam.isAuthorized(publicUser, testResourceId, Permission.WRITE);
            System.out.printf("Public user can WRITE to '%s'? %s\n", testResourceId, canWrite);

            // Test Case 3: Invalid permission string
            try {
                iam.isAuthorized(publicUser, testResourceId, "DELETE");
            } catch (IllegalArgumentException e) {
                System.out.println("Correctly caught invalid permission: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("An error occurred during the authorization check:");
            e.printStackTrace();
        }
    }
}
