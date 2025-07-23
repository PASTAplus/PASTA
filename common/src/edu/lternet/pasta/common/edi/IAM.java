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
 * The IAM (Identity and Access Management) class provides methods to interact with the EDI IAM service.
 */
public class IAM {

    private final String baseUrl;
    private final String ediToken;

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
     * Constructs an IAM client with a specific protocol, host, and port.
     * This is the designated constructor for the class.
     *
     * @param protocol The protocol, e.g., "http" or "https".
     * @param host     The hostname, e.g., "auth.edirepository.org".
     * @param port     The port number, e.g., 8443. Use a non-positive value (<=0) to omit the port from the URL.
     */
    public IAM(String protocol, String host, int port, String ediToken) {
        if (protocol == null || protocol.trim().isEmpty()) {
            throw new IllegalArgumentException("Protocol cannot be null or empty.");
        }
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(protocol.trim()).append("://").append(host.trim());
        if (port > 0) {
            sb.append(":").append(port);
        }
        this.baseUrl = sb.toString();

        if (ediToken == null || ediToken.trim().isEmpty()) {
            throw new IllegalArgumentException("EDI token cannot be null or empty.");
        }
        this.ediToken = ediToken;
    }

    public String ping() throws IOException {
        String urlString = String.format("%s/auth/v1/ping", this.baseUrl);
        return sendRequest(urlString, "GET", "{}");
    }

    /**
     * Determines if a user is authorized to access a resource with a specific permission level.
     *
     * @param ediToken         The identifier for the user (e.g., "uid=EDI-X,o=EDI,dc=edirepository,dc=org").
     * @param resourceId     The identifier for the resource (e.g., "edi.1.1").
     * @param permission     The permission level to check ("READ", "WRITE", or "CHANGEPERMISSION").
     * @return true if the user is authorized, false otherwise.
     * @throws IOException if an I/O error occurs when communicating with the auth service.
     * @throws IllegalArgumentException if the provided permission string is invalid.
     */
    public boolean isAuthorized(String ediToken, String resourceId, String permission) throws IOException {
        Permission permissionEnum;
        try {
            permissionEnum = Permission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid permission level specified: " + permission, e);
        }
        return isAuthorized(ediToken, resourceId, permissionEnum);
    }

    /**
     * Determines if a user is authorized to access a resource with a specific permission level.
     *
     * @param userId         The identifier for the user.
     * @param resourceId     The identifier for the resource.
     * @param permission     The permission level to check as a {@link Permission} enum.
     * @return true if the user is authorized, false otherwise.
     * @throws IOException if an I/O error occurs when communicating with the auth service.
     */
    public boolean isAuthorized(String userId, String resourceId, Permission permission) throws IOException {
        String encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8.name());
        String encodedResourceId = URLEncoder.encode(resourceId, StandardCharsets.UTF_8.name());
        String encodedPermission = URLEncoder.encode(permission.name().toLowerCase(), StandardCharsets.UTF_8.name());

        String urlString = String.format(
                "%s/auth/authorized?uid=%s&resource_id=%s&permission=%s",
                this.baseUrl, encodedUserId, encodedResourceId, encodedPermission
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

            if (responseCode == HttpURLConnection.HTTP_OK) {
                isAuthorized = true;
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                isAuthorized = false;
            } else {
                String responseMessage = readResponse(connection);
                throw new IOException(
                        String.format(
                                "Unexpected response from EDI IAM service for URL '%s': %d %s. Response body: %s",
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
     * Helper method to read the response body from an HttpURLConnection.
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
            return "[Could not read response body: " + e.getMessage() + "]";
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private String sendRequest(String urlString, String method, String payload) throws IOException {

        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty.");
        }

        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Method cannot be null or empty.");
        }

        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null.");
        }

        URL url = new URL(urlString);
        HttpURLConnection connection = null;
        InputStream inputStream;
        String response = null;

        String cookie = String.format("edi-token=%s", this.ediToken);

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000);    // 5 seconds
            connection.setRequestProperty("Cookie", cookie);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                if (inputStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder responseBody = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line);
                    }
                    reader.close();
                    response = responseBody.toString();
                }

            } else {
                String responseMessage = readResponse(connection);
                throw new IOException(
                        String.format(
                                "Unexpected response from EDI IAM service for URL '%s': %d %s. Response body: %s",
                                urlString, responseCode, connection.getResponseMessage(), responseMessage
                        )
                );
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return response;
    }
}
