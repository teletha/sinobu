/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

import com.pgssoft.httpclient.Action;
import com.pgssoft.httpclient.HttpClientMock;

import kiss.I;
import kiss.JSON;
import kiss.Variable;
import kiss.XML;

class HTTPTest {

    HttpClientMock httpClientMock = new HttpClientMock();

    /**
     * Unescape the special characters.
     * 
     * @param json
     * @return
     */
    private String text(String json) {
        return json.replace('\'', '"');
    }

    /**
     * @see I#http(String, Class, HttpClient...)
     */
    @Test
    void responseString() {
        httpClientMock.onGet().doReturn("ok");

        assert I.http("http://test.com", String.class, httpClientMock).to().is("ok");
    }

    /**
     * @see I#http(String, Class, HttpClient...)
     */
    @Test
    void responseHTML() {
        httpClientMock.onGet().doReturnXML("<html><body>contents</body></html>");

        XML xml = I.http("http://test.com", XML.class, httpClientMock).to().exact();
        assert xml.name().equals("html");
        assert xml.text().equals("contents");
    }

    /**
     * @see I#http(String, Class, HttpClient...)
     */
    @Test
    void responseJSON() {
        httpClientMock.onGet().doReturnJSON(text("{'state' : 'ok'}"));

        JSON json = I.http("http://test.com", JSON.class, httpClientMock).to().exact();
        assert json.get(String.class, "state").equals("ok");
    }

    /**
     * @see I#http(String, Class, HttpClient...)
     */
    @Test
    void responseMappedType() {
        class Response {
            public String state;
        }

        httpClientMock.onGet().doReturnJSON(text("{'state' : 'ok'}"));

        Response response = I.http("http://test.com", Response.class, httpClientMock).to().exact();
        assert response.state.equals("ok");
    }

    @Test
    void responseGzip() {
        httpClientMock.onGet().doAction(gzip("<root>gzip</root>"));

        XML xml = I.http("http://test", XML.class, httpClientMock).to().exact();
        assert xml.name().equals("root");
        assert xml.text().equals("gzip");
    }

    @Test
    void responseDeflate() {
        httpClientMock.onGet().doAction(deflate("<root>deflate</root>"));

        XML xml = I.http("http://test", XML.class, httpClientMock).to().exact();
        assert xml.name().equals("root");
        assert xml.text().equals("deflate");
    }

    /**
     * Build gziped response.
     * 
     * @param response
     * @param text
     */
    private Action gzip(String text) {
        return response -> {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                GZIPOutputStream writer = new GZIPOutputStream(out);
                writer.write(text.getBytes());
                writer.close();

                response.addHeader("Content-Encoding", "gzip");
                response.setBodyBytes(ByteBuffer.wrap(out.toByteArray()));
            } catch (IOException e) {
                throw I.quiet(e);
            }
        };
    }

    /**
     * Build gziped response.
     * 
     * @param response
     * @param text
     */
    private Action deflate(String text) {
        return response -> {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DeflaterOutputStream writer = new DeflaterOutputStream(out);
                writer.write(text.getBytes());
                writer.close();

                response.addHeader("Content-Encoding", "deflate");
                response.setBodyBytes(ByteBuffer.wrap(out.toByteArray()));
            } catch (IOException e) {
                throw I.quiet(e);
            }
        };
    }

    @Test
    void clientErrorResponse400() {
        httpClientMock.onGet().doReturn(400, "Bad Request");

        AtomicReference<Throwable> error = new AtomicReference();
        Variable<JSON> result = I.http("http://test", JSON.class, httpClientMock).effectOnError(error::set).to();
        assert result.isAbsent();
        assert error.get() instanceof HttpRetryException;
        assert error.get().getMessage().equals("Bad Request");
    }

    @Test
    void clientErrorResponse404() {
        httpClientMock.onGet().doReturn(404, "Not Found");

        AtomicReference<Throwable> error = new AtomicReference();
        Variable<JSON> result = I.http("http://test", JSON.class, httpClientMock).effectOnError(error::set).to();
        assert result.isAbsent();
        assert error.get() instanceof HttpRetryException;
        assert error.get().getMessage().equals("Not Found");
    }

    @Test
    void serverErrorResponse500() {
        httpClientMock.onGet().doReturn(500, "Internal Server Error");

        AtomicReference<Throwable> error = new AtomicReference();
        Variable<JSON> result = I.http("http://test", JSON.class, httpClientMock).effectOnError(error::set).to();
        assert result.isAbsent();
        assert error.get() instanceof HttpRetryException;
        assert error.get().getMessage().equals("Internal Server Error");
    }

    @Test
    void serverErrorResponse503() {
        httpClientMock.onGet().doReturn(503, "Service Unavailable");

        AtomicReference<Throwable> error = new AtomicReference();
        Variable<JSON> result = I.http("http://test", JSON.class, httpClientMock).effectOnError(error::set).to();
        assert result.isAbsent();
        assert error.get() instanceof HttpRetryException;
        assert error.get().getMessage().equals("Service Unavailable");
    }

    @Test
    void clientIsNull() {
        assert I.http("http://test", String.class, (HttpClient[]) null).to().isAbsent();
    }

    @Test
    void clientHasNullItem() {
        assert I.http("http://test", String.class, new HttpClient[] {null, null}).to().isAbsent();
    }

    @Test
    void clientIsEmpty() {
        assert I.http("http://test", String.class, new HttpClient[0]).to().isAbsent();
    }
}