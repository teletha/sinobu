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

import java.net.http.HttpClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

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
        client.onGet().doReturn("<root>yes</root>");

        XML xml = I.http("http://test", XML.class).to().exact();
        assert xml.name().equals("root");
        assert xml.text().equals("yes");
    }

    @Test
    void httpJSON() {
        client.onGet().doReturn(text("{'state' : 'ok'}"));

        JSON json = I.http("http://test", JSON.class).to().exact();
        assert json.get("state", String.class).equals("ok");
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