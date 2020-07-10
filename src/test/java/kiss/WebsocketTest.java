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

import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import antibug.WebSocketServer;
import antibug.WebSocketServer.Validator;

@Execution(ExecutionMode.SAME_THREAD)
class WebsocketTest {

    HttpClient original;

    WebSocketServer server;

    Validator validator = new Validator();

    Observer<String> observer = new Observer<>() {

        @Override
        public void accept(String value) {
            validator.onText(null, value, true);
        }

        @Override
        public void complete() {
            validator.onClose(null, 1000, "");
        }

        @Override
        public void error(Throwable e) {
            validator.onError(null, e);
        }
    };

    Consumer<WebSocket> open = validator::onOpen;

    @BeforeEach
    void setup() {
        server = new WebSocketServer();
        original = I.client;
        I.client = server.client();
    }

    @AfterEach
    void cleanup() {
        I.client = original;
    }

    @Test
    void connectToServer() {
        I.http("ws://websocket-server", open).to(observer);

        assert validator.isOpened();
        assert validator.isNotError();
        assert validator.isNotClosed();
    }

    @Test
    void unknownServerWillThrowError() {
        server.rejectConnectionBy(new UnknownHostException());

        I.http("ws://unknown-websocket-server", open).to(observer);

        assert validator.isNotClosed();
        assert validator.isError(UnknownHostException.class);
    }

    @Test
    void response() {
        server.send("Welcome!");

        I.http("ws://websocket-server", open).to(observer);

        assert validator.hasMessage("Welcome!");
    }

    @Test
    void request() {
        server.replyWhen("Ping", () -> {
            server.send("Pong");
        });

        I.http("ws://websocket-server", open).to(observer);
        validator.send("Ping");

        assert validator.hasMessage("Pong");
    }

    @Test
    void close() {
        server.replyWhen("Close", () -> {
            server.sendClose(1000, "Bye");
        });

        I.http("ws://websocket-server", open).to(observer);
        validator.send("Close");

        assert validator.hasNoMessage();
        assert validator.isClosed();
        assert validator.isNotError();
    }
}
