package com.example.rundeck.plugin.example;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.google.gson.Gson;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The following implementation Http notification plugin will post requests to the target url.
 * The user of this plugin can pass body content, content type, and HTTP method type.
 */
@Plugin(service = "Notification", name = "http-notification")
@PluginDescription(title = "HTTP Notification Plugin", description = "Rundeck's http notification plugin.")
public class HttpNotificationPlugin {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final Gson gson = new Gson();

    /*
        Plugin default request timeout lengths.
     */
    public static final Long DEFAULT_CONNECTION_TIMEOUT = 10000L;
    public static final Long DEFAULT_SOCKET_TIMEOUT = 60000L;

    private Long connectionTimeout;
    private Long socketTimeout;

    /*
        Supported HTTP methods.
     */
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";

    public static final List<String> SUPPORTED_HTTP_METHODS = Arrays.asList(HTTP_METHOD_POST, HTTP_METHOD_PUT);

    /*
        Supported content types.
     */
    public static final String CONTENT_JSON = "application/json";
    public static final String CONTENT_TEXT = "text/plain";
    public static final String CONTENT_XML = "text/xml";

    public static final List<String> SUPPORTED_CONTENT_TYPES = Arrays.asList(CONTENT_JSON, CONTENT_TEXT, CONTENT_XML);

    public HttpNotificationPlugin(Long connectionTimeout, Long socketTimeout) {

        this.connectionTimeout = connectionTimeout != null && connectionTimeout > 0L ? connectionTimeout : DEFAULT_CONNECTION_TIMEOUT;

        this.socketTimeout = socketTimeout != null && socketTimeout > 0L ? socketTimeout : DEFAULT_SOCKET_TIMEOUT;

        Unirest.setTimeouts(this.connectionTimeout, this.socketTimeout);

    }

    /**
     * Getter method for connectionTimout.
     *
     * @return
     */
    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Setter method for connectionTimout.
     *
     * @param connectionTimeout
     */
    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        Unirest.setTimeouts(this.connectionTimeout, this.socketTimeout);
    }

    /**
     * Getter method for socketTimeout.
     *
     * @return
     */
    public Long getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Setter method for socketTimout.
     *
     * @param socketTimeout
     */
    public void setSocketTimeout(Long socketTimeout) {
        this.socketTimeout = socketTimeout;
        Unirest.setTimeouts(this.connectionTimeout, this.socketTimeout);

    }

    /**
     * The following method will post an HTTP notification to the passed in url.
     *
     * @param url         The designated endpoint accepting the notification.
     * @param httpMethod  The HTTP method to use.
     * @param contentType Request header content type.
     * @param body        The notification body.
     * @return True if successful, False if not.
     * @throws HttpNotificationException
     */
    public Boolean sendNotification(String url, String httpMethod, String contentType, String body) throws HttpNotificationException {
        if (url == null || url.isEmpty()) {
            throw new HttpNotificationException("Error: URL is required.");
        }

        if (httpMethod == null || httpMethod.isEmpty()) {
            throw new HttpNotificationException("Error: HTTP method is required.");
        } else if (!SUPPORTED_HTTP_METHODS.contains(httpMethod)) {
            throw new HttpNotificationException("Error: HTTP method not supported. The following methods are supported: " + SUPPORTED_HTTP_METHODS.toString());
        }

        if (contentType == null || contentType.isEmpty()) {
            throw new HttpNotificationException("Error: Content type is required.");
        } else if (!SUPPORTED_CONTENT_TYPES.contains(contentType)) {
            throw new HttpNotificationException("Error: Content type not supported. The following methods are supported: " + SUPPORTED_CONTENT_TYPES.toString());
        }

        if (body == null || body.isEmpty()) {
            throw new HttpNotificationException("Error: Notification body is required.");
        }

        if (contentType == CONTENT_JSON && !isValidJson(body)) {
            throw new HttpNotificationException("Error: Json is invalid.");
        }

        if (contentType == CONTENT_XML && !isValidXml(body)) {
            throw new HttpNotificationException("Error: Xml is invalid.");
        }

        if (httpMethod == HTTP_METHOD_POST) {
            return postNotification(url, contentType, body);
        } else if (httpMethod == HTTP_METHOD_PUT) {
            return putNotification(url, contentType, body);
        }

        return false;
    }

    /**
     * The following helper method will POST an HttpNotification (body) to the designated url.
     *
     * @param url         The designated endpoint accepting the notification.
     * @param contentType Request header content type.
     * @param body        The notification body.
     * @return True if successful, False if not.
     * @throws HttpNotificationException
     */
    private Boolean postNotification(String url, String contentType, String body) throws HttpNotificationException {
        Integer statusCode = 0;
        String statusText = "";

        try {
            if (contentType == CONTENT_JSON) {
                HttpResponse<JsonNode> jsonResponse = Unirest.post(url)
                        .header("Content-Type", contentType)
                        .body(body)
                        .asJson();

                statusCode = jsonResponse.getStatus();
                statusText = jsonResponse.getStatusText();

            } else if (contentType == CONTENT_TEXT || contentType == CONTENT_XML) {
                HttpResponse response = Unirest.post(url)
                        .header("Content-Type", contentType)
                        .body(body)
                        .asString();

                statusCode = response.getStatus();
                statusText = response.getStatusText();

            }
        } catch (UnirestException e) {
            LOGGER.log(Level.SEVERE, "Error: Failed to POST Http Notification.");

            return false;
        }

        if (statusCode >= 300) {
            throwStatusCodeException(statusCode, statusText);
        }

        return true;
    }

    /**
     * The following helper method will PUT an HttpNotification (body) to the designated url.
     *
     * @param url         The designated endpoint accepting the notification.
     * @param contentType Request header content type.
     * @param body        The notification body.
     * @return True if successful, False if not.
     * @throws HttpNotificationException
     */
    private Boolean putNotification(String url, String contentType, String body) throws HttpNotificationException {
        Integer statusCode = 0;
        String statusText = "";

        try {
            if (contentType == CONTENT_JSON) {
                HttpResponse<JsonNode> jsonResponse = Unirest.put(url)
                        .header("Content-Type", contentType)
                        .body(body)
                        .asJson();

                statusCode = jsonResponse.getStatus();
                statusText = jsonResponse.getStatusText();

            } else if (contentType == CONTENT_TEXT || contentType == CONTENT_XML) {
                HttpResponse response = Unirest.put(url)
                        .header("Content-Type", contentType)
                        .body(body)
                        .asString();

                statusCode = response.getStatus();
                statusText = response.getStatusText();

            }
        } catch (UnirestException e) {
            LOGGER.log(Level.SEVERE, "Error: Failed to PUT Http Notification.");

            return false;
        }

        if (statusCode >= 300) {
            throwStatusCodeException(statusCode, statusText);
        }

        return true;
    }

    /**
     * This helper method will derive and throw a new HttpNotificationException based on the Http status code.
     *
     * @param code The Http status code returned by the client.
     * @throws HttpNotificationException
     */
    private void throwStatusCodeException(Integer code, String codeText) throws HttpNotificationException {
        if (code >= 300 && code < 400) {
            throw new HttpNotificationException("Error: Server responded with redirection. Code: " + code + ", Text: " + codeText);
        } else if (code >= 400 && code < 500) {
            throw new HttpNotificationException("Error: Server responded with client side error. Code: " + code + ", Text: " + codeText);
        } else if (code >= 500 && code < 600) {
            throw new HttpNotificationException("Error: Server responded with server error. Code: " + code + ", Text: " + codeText);
        }
    }

    /**
     * Simple helper method which determines if the given json (string) is valid.
     *
     * @param jsonString The json object as a string.
     * @return True if valid, False if invalid.
     */
    public static boolean isValidJson(String jsonString) {
        try {
            gson.fromJson(jsonString, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    /**
     * Simple helper method which determines if the given xml (string) is valid.
     *
     * @param xmlString The xml object as a string.
     * @return True if valid, False if invalid.
     */
    public static boolean isValidXml(String xmlString) {
        StringWriter sw;

        try {
            final OutputFormat format = OutputFormat.createPrettyPrint();
            final Document document = DocumentHelper.parseText(xmlString);
            sw = new StringWriter();
            final XMLWriter writer = new XMLWriter(sw, format);
            writer.write(document);
        } catch (DocumentException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}