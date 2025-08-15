package edu.lternet.pasta.common.edi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;


/**
 * The IAM (Identity and Access Management) class provides methods to interact with the EDI IAM service.
 */
public class IAM {

    private final String baseUrl;
    private String ediToken;
    private String ediId;

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
    public IAM(String protocol, String host, int port) {
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

    }

    /**
     * Class IAM Setters and Getters
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setEdiToken(String ediToken) {
        this.ediToken = ediToken;
    }

    public String getEdiToken() {
        return this.ediToken;
    }

    public void setEdiId(String ediId) {
        this.ediId = ediId;
    }

    public String getEdiId() {
        return this.ediId;
    }

    /**
     * Adds the access control rules declared in an EML (Ecological Metadata
     * Language) document by sending a request to the authentication service.
     *
     * @param eml The EML document as a string. This should be in a valid EML format.
     * @return A JSONObject of the successful request.
     * @throws IOException If an I/O error occurs while communicating with the auth server.
     */
    public JSONObject addEml(String eml, String keyPrefix) throws IOException {
        String urlString = String.format("%s/auth/v1/eml", baseUrl);
        JSONObject payload = new JSONObject();
        payload.put("eml", eml);
        payload.put("key_prefix", keyPrefix);
        return sendRequest(urlString, "POST", payload.toString());
    }

    /**
     * Wrapper for isAuthorized method to test for correct permissions.
     *
     * @param resourceKey     The identifier for the resource (e.g., "edi.1.1").
     * @param permission     The permission level to check ("READ", "WRITE", or "CHANGEPERMISSION").
     * @return JSONObject of the successful request.
     * @throws IOException if an I/O error occurs when communicating with the auth service.
     * @throws IllegalArgumentException if the provided permission string is invalid.
     */
    public JSONObject isAuthorized(String resourceKey, String permission) throws IOException {
        Permission permissionEnum;
        try {
            permissionEnum = Permission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid permission level specified: " + permission, e);
        }
        return isAuthorized(resourceKey, permissionEnum);
    }

    /**
     * Determines if a user is authorized to access a resource with a specific permission level.
     *
     * @param resourceKey     The identifier for the resource.
     * @param permission     The permission level to check as a {@link Permission} enum.
     * @return JSONObject of the successful request.
     * @throws IOException if an I/O error occurs when communicating with the auth service.
     */
    public JSONObject isAuthorized(String resourceKey, Permission permission) throws IOException {
        String encodedResourceId = URLEncoder.encode(resourceKey, StandardCharsets.UTF_8.name());
        String encodedPermission = URLEncoder.encode(permission.getPermission(), StandardCharsets.UTF_8.name());

        String urlString = String.format(
                "%s/auth/v1/authorized?resource_key=%s&permission=%s",
                this.baseUrl, encodedResourceId, encodedPermission
        );

        return sendRequest(urlString, "GET", null);
    }

    public JSONObject createEdiToken(String ediId, String key) throws IOException {
        String urlString = String.format("%s/auth/v1/token/%s", this.baseUrl, ediId);
        String payload = String.format("{\"key\": \"%s\"}", key);

        return sendRequest(urlString, "POST", payload);
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
         * @return JSONObject of the successful request.
         * @throws IOException if a network error occurs or if the server returns a non-200 status code.
         * @throws IllegalArgumentException if the urlString, method, or payload are invalid.
         */

    private JSONObject sendRequest(String urlString, String method, String payload) throws IOException {

        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty.");
        }

        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Method cannot be null or empty.");
        }

        if ((method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) && payload == null) {
            throw new IllegalArgumentException("Payload cannot be null for POST or PUT requests.");
        }

        URL url = new URL(urlString);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000);    // 5 seconds
            connection.setRequestProperty("Accept", "application/json");

            if (this.ediToken != null) {
                String cookie = String.format("edi-token=%s", this.ediToken);
                connection.setRequestProperty("Cookie", cookie);
            }

            if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }
                reader.close();
                return new JSONObject(response.toString());
            }
            else {
                // Build an error message from the response message
                StringBuilder error = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line.trim());
                }
                reader.close();
                throw new IOException(error.toString());
            }
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
