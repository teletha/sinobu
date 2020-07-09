/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
class WebsocketTest {

    HttpClient original;

    MockWebSocketServer server;

    Starter starter;

    Listener listener;

    @BeforeEach
    void setup() {
        starter = new Starter();
        listener = new Listener();
        server = new MockWebSocketServer();
        original = I.client;
        I.client = server.client();
    }

    @AfterEach
    void cleanup() {
        I.client = original;
    }

    @Test
    void unknownServer() {
        I.http("ws://unknown-web-socket-server.desu", starter).to(listener);

        assert listener.isError();
    }

    /**
     * 
     */
    private static class Starter implements Consumer<WebSocket> {

        private WebSocket ws;

        @Override
        public void accept(WebSocket socket) {
            this.ws = socket;
        }
    }

    /**
     * 
     */
    private static class Listener implements Observer<String> {

        private final List<String> text = new ArrayList();

        private boolean completed;

        private Throwable error;

        @Override
        public void accept(String text) {
        }

        @Override
        public void complete() {
            this.completed = true;
        }

        @Override
        public void error(Throwable e) {
            this.error = e;
        }

        private boolean isError() {
            return error != null;
        }
    }
}
