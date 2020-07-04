/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import com.pgssoft.httpclient.Action;
import com.pgssoft.httpclient.HttpClientMock;

@Execution(ExecutionMode.SAME_THREAD)
class HTTPTest {

    HttpClient original;

    HttpClientMock client;

    @BeforeEach
    void setup() {
        original = I.client;
        I.client = client = new HttpClientMock();
    }

    @AfterEach
    void cleanup() {
        I.client = original;
    }

    @Test
    void httpString() {
        client.onGet().doReturn("ok");

        assert I.http("http://test", String.class).to().is("ok");
    }

    @Test
    void httpXML() {
        client.onGet().doReturnXML("<root>yes</root>");

        XML xml = I.http("http://test", XML.class).to().exact();
        assert xml.name().equals("root");
        assert xml.text().equals("yes");
    }

    @Test
    void httpJSON() {
        client.onGet().doReturnJSON(text("{'state' : 'ok'}"));

        JSON json = I.http("http://test", JSON.class).to().exact();
        assert json.get("state").to(String.class).equals("ok");
    }

    @Test
    void httpType() {
        client.onGet().doReturn(text("{'state' : 'ok'}"));

        Server server = I.http("http://test", Server.class).to().exact();
        assert server.state.equals("ok");
    }

    static class Server {
        public String state;
    }

    @Test
    void httpGzip() {
        client.onGet().doAction(gzip("<root>gzip</root>"));

        XML xml = I.http("http://test", XML.class).to().exact();
        assert xml.name().equals("root");
        assert xml.text().equals("gzip");
    }

    @Test
    void httpDeflate() {
        client.onGet().doAction(deflate("<root>deflate</root>"));

        XML xml = I.http("http://test", XML.class).to().exact();
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

    /**
     * Unescape the special characters.
     * 
     * @param json
     * @return
     */
    private String text(String json) {
        return json.replace('\'', '"');
    }
}