/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://clientsource.org/licenses/mit-license.php
 */
package kiss.web;

import java.net.UnknownHostException;
import java.net.http.HttpClient;

import org.junit.jupiter.api.Test;

import antibug.WebSocketServer;
import antibug.WebSocketServer.WebSocketClient;
import kiss.I;
import kiss.Observer;

class WebsocketTest {

    WebSocketServer server = new WebSocketServer();

    HttpClient httpClient = server.httpClient();

    WebSocketClient client = server.websocketClient();

    Observer<String> observer = new Observer<>() {

        @Override
        public void accept(String value) {
            client.onNext(value);
        }

        @Override
        public void complete() {
            client.onComplete();
        }

        @Override
        public void error(Throwable e) {
            client.onError(e);
        }
    };

    @Test
    void connectToServer() {
        I.http("ws://websocket-server", client, httpClient).to(observer);

        assert client.isOpened();
        assert client.isNotError();
        assert client.isNotClosed();
    }

    @Test
    void unknownServerWillThrowError() {
        server.rejectConnectionBy(new UnknownHostException());

        I.http("ws://unknown-websocket-server", client, httpClient).to(observer);

        assert client.isNotClosed();
        assert client.isError(UnknownHostException.class);
    }

    @Test
    void response() {
        server.send("Welcome!");

        I.http("ws://websocket-server", client, httpClient).to(observer);

        assert client.hasMessage("Welcome!");
    }

    @Test
    void request() {
        server.replyWhen("Ping", () -> {
            server.send("Pong");
        });

        I.http("ws://websocket-server", client, httpClient).to(observer);
        client.send("Ping");

        assert client.hasMessage("Pong");
    }

    @Test
    void close() {
        server.replyWhen("Close", () -> {
            server.sendClose(1000, "Bye");
        });

        I.http("ws://websocket-server", client, httpClient).to(observer);
        client.send("Close");

        assert client.hasNoMessage();
        assert client.isClosed();
        assert client.isNotError();
    }
}
