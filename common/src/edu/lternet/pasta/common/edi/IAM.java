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
     * Defines the possible permission levels for a resource.
     */
    public enum Permission {
        READ("read"),
        WRITE("write"),
        CHANGEPERMISSION("changePermission");

        private final String permission;

        Permission(String permission) {
            this.permission = permission;
        }

        public String getPermission() {
            return permission;
        }
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

    /**
     * Pings the EDI IAM service to check for connectivity and service status.
     *
     * @return The response from the ping endpoint, typically a confirmation message if the service is up.
     * @throws IOException if an I/O error occurs when communicating with the auth service,
     * or if the service returns an unexpected HTTP status code.
     */
    public String ping() throws IOException {
        String urlString = String.format("%s/auth/v1/ping", this.baseUrl);
        return sendRequest(urlString, "GET");
    }

    /**
     * Determines if a user is authorized to access a resource with a specific permission level.
     *
     * @param resourceId     The identifier for the resource (e.g., "edi.1.1").
     * @param permission     The permission level to check ("READ", "WRITE", or "CHANGEPERMISSION").
     * @return true if the user is authorized, false otherwise.
     * @throws IOException if an I/O error occurs when communicating with the auth service.
     * @throws IllegalArgumentException if the provided permission string is invalid.
     */
    public boolean isAuthorized(String resourceId, String permission) throws IOException {
        Permission permissionEnum;
        try {
            permissionEnum = Permission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid permission level specified: " + permission, e);
        }
        return isAuthorized(resourceId, permissionEnum);
    }

    /**
     * Determines if a user is authorized to access a resource with a specific permission level.
     *
     * @param resourceId     The identifier for the resource.
     * @param permission     The permission level to check as a {@link Permission} enum.
     * @return true if the user is authorized, false otherwise.
     * @throws IOException if an I/O error occurs when communicating with the auth service.
     */
    public boolean isAuthorized(String resourceId, Permission permission) throws IOException {
        boolean isAuthorized = true;

        String encodedResourceId = URLEncoder.encode(resourceId, StandardCharsets.UTF_8.name());
        String encodedPermission = URLEncoder.encode(permission.getPermission(), StandardCharsets.UTF_8.name());

        String urlString = String.format(
                "%s/auth/v1/authorized?resource_key=%s&permission=%s",
                this.baseUrl, encodedResourceId, encodedPermission
        );

        try {
            String response = sendRequest(urlString, "GET");
        }
        catch (IOException e) {
            isAuthorized = false;
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

    /**
     * Gets the base URL of the EDI IAM service configured for this client.
     *
     * @return The base URL, e.g., "https://auth.edirepository.org:8443".
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * A private helper method to send an authenticated HTTP request to the EDI IAM service.
     * <p>
     * This method configures the connection with the necessary 'edi-token' cookie for authentication.
     * It handles parameter validation, connection setup, and response parsing. It expects a
     * successful response to have an HTTP 200 OK status.
     * <p>
     * Note: This implementation does not write the {@code payload} to the output stream,
     * so it is currently only suitable for HTTP methods like GET that do not have a request body.
     *
     * @param urlString The full URL endpoint for the request.
     * @param method    The HTTP method to use (e.g., "GET").
     * @return The response body as a String if the request is successful (HTTP 200).
     * @throws IOException if a network error occurs or if the server returns a non-200 status code.
     * @throws IllegalArgumentException if the urlString, method, or payload are invalid.
     */

    private String sendRequest(String urlString, String method) throws IOException {

        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty.");
        }

        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Method cannot be null or empty.");
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
