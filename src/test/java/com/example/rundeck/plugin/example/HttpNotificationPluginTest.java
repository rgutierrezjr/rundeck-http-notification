package com.example.rundeck.plugin.example;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpNotificationPluginTest {

    private final String blank_url = "";
    private final String null_url = null;
    private final String valid_url_post = "https://postman-echo.com/post";
    private final String valid_url_put = "https://postman-echo.com/put";

    private final String blank_content_type = "";
    private final String null_content_type = null;
    private final String unsupported_content_type = "pdf";

    private final String unsupported_method = "DELETE";
    private final String blank_method = "";
    private final String null_method = null;

    private final String blank_body = "";
    private final String null_body = "";

    private final String valid_json = "{\"name\":\"Ruben Gutierrez\", \"message\":\"This is a notification.\"}";
    private final String invalid_json = "{\"name\"::::\"Ruben Gutierrez\", \"message\":\"This is a notification.\"}";
    private final String valid_text = "This is a notification";

    private final String valid_xml = "<notification>\n" +
            "<from>Rundeck</from>\n" +
            "<message>This is a http notification.</message>\n" +
            "</notification>";

    private final String invalid_xml = "<notifications>\n" +
            "<from>Rundeck<from>\n" +
            "<message>This is a http notification.</message>\n" +
            "</notification>";

    HttpNotificationPlugin notificationPlugin = new HttpNotificationPlugin(0L, 0L);

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testBlankURL() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: URL is required.");

        notificationPlugin.sendNotification(blank_url, HttpNotificationPlugin.HTTP_METHOD_PUT, HttpNotificationPlugin.CONTENT_JSON, valid_json);
    }

    @Test
    public void testNullURL() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: URL is required.");

        notificationPlugin.sendNotification(null_url, HttpNotificationPlugin.HTTP_METHOD_PUT, HttpNotificationPlugin.CONTENT_JSON, valid_json);
    }

    @Test
    public void testBlankMethod() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: HTTP method is required.");

        notificationPlugin.sendNotification(valid_url_post, blank_method, HttpNotificationPlugin.CONTENT_JSON, valid_json);
    }

    @Test
    public void testNullMethod() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: HTTP method is required.");

        notificationPlugin.sendNotification(valid_url_post, null_method, HttpNotificationPlugin.CONTENT_JSON, valid_json);
    }

    @Test
    public void testUnsupportedMethod() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: HTTP method not supported. The following methods are supported: " + HttpNotificationPlugin.SUPPORTED_HTTP_METHODS.toString());

        notificationPlugin.sendNotification(valid_url_post, unsupported_method, HttpNotificationPlugin.CONTENT_JSON, valid_json);
    }

    @Test
    public void testBlankContentType() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: Content type is required.");

        notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_PUT, blank_content_type, valid_json);
    }

    @Test
    public void testNullContentType() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: Content type is required.");

        notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_PUT, null_content_type, valid_json);
    }

    @Test
    public void testUnsupportedContentType() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: Content type not supported. The following methods are supported: " + HttpNotificationPlugin.SUPPORTED_CONTENT_TYPES.toString());

        notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_PUT, unsupported_content_type, valid_json);
    }

    @Test
    public void testBlankBody() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: Notification body is required.");

        notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_PUT, HttpNotificationPlugin.CONTENT_JSON, blank_body);
    }

    @Test
    public void testNullBody() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: Notification body is required.");

        notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_PUT, HttpNotificationPlugin.CONTENT_JSON, null_body);
    }

    @Test
    public void testInvalidJsonBody() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: Json is invalid.");

        notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_POST, HttpNotificationPlugin.CONTENT_JSON, invalid_json);
    }

    @Test
    public void testPostJsonNotification() throws HttpNotificationException {
        Boolean result = notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_POST, HttpNotificationPlugin.CONTENT_JSON, valid_json);

        assert result == true;
    }

    @Test
    public void testPutXmlNotification() throws HttpNotificationException {
        Boolean result = notificationPlugin.sendNotification(valid_url_put, HttpNotificationPlugin.HTTP_METHOD_PUT, HttpNotificationPlugin.CONTENT_XML, valid_xml);

        assert result == true;
    }

    @Test
    public void testPostXmlNotification() throws HttpNotificationException {
        Boolean result = notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_POST, HttpNotificationPlugin.CONTENT_XML, valid_xml);

        assert result == true;
    }

    @Test
    public void testInvalidXmlNotification() throws HttpNotificationException {
        exceptionRule.expect(HttpNotificationException.class);
        exceptionRule.expectMessage("Error: Xml is invalid.");

        notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_POST, HttpNotificationPlugin.CONTENT_XML, invalid_xml);
    }

    @Test
    public void testPostTextNotification() throws HttpNotificationException {
        Boolean result = notificationPlugin.sendNotification(valid_url_post, HttpNotificationPlugin.HTTP_METHOD_POST, HttpNotificationPlugin.CONTENT_TEXT, valid_text);

        assert result == true;
    }

    @Test
    public void testPutJsonNotification() throws HttpNotificationException {
        Boolean result = notificationPlugin.sendNotification(valid_url_put, HttpNotificationPlugin.HTTP_METHOD_PUT, HttpNotificationPlugin.CONTENT_JSON, valid_json);

        assert result == true;
    }

    @Test
    public void testPutTextNotification() throws HttpNotificationException {
        Boolean result = notificationPlugin.sendNotification(valid_url_put, HttpNotificationPlugin.HTTP_METHOD_PUT, HttpNotificationPlugin.CONTENT_TEXT, valid_text);

        assert result == true;
    }

    @Test
    public void testConnectionTimeoutSetterAndGetter() {
        // notificationPlugin instantiated with default timeout values;
        Long connectionTimeout = notificationPlugin.getConnectionTimeout();

        assert connectionTimeout == HttpNotificationPlugin.DEFAULT_CONNECTION_TIMEOUT;

        notificationPlugin.setConnectionTimeout(10000L);

        assert notificationPlugin.getConnectionTimeout() == 10000L;
    }

    @Test
    public void testSocketTimeoutSetterAndGetter() {
        // notificationPlugin instantiated with default timeout values;
        Long socketTimeout = notificationPlugin.getSocketTimeout();

        assert socketTimeout == HttpNotificationPlugin.DEFAULT_SOCKET_TIMEOUT;

        notificationPlugin.setSocketTimeout(3000L);

        assert notificationPlugin.getSocketTimeout() == 3000L;
    }
}