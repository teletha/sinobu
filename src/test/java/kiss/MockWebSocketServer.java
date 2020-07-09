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

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

public class MockWebSocketServer {

    private final MockHttpClient client = new MockHttpClient();

    private final MockWebsocketBuilder builder = new MockWebsocketBuilder();

    private final MockWebSocket ws = new MockWebSocket();

    private Listener listener;

    private Duration timeout = Duration.ofMillis(Long.MAX_VALUE);

    /**
     * Return the associated mocked http client.
     * 
     * @return
     */
    public HttpClient client() {
        return client;
    }

    private class MockHttpClient extends HttpClient {

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<CookieHandler> cookieHandler() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Duration> connectTimeout() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Redirect followRedirects() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<ProxySelector> proxy() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SSLContext sslContext() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Authenticator> authenticator() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Version version() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Executor> executor() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler, PushPromiseHandler<T> pushPromiseHandler) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public WebSocket.Builder newWebSocketBuilder() {
            return builder;
        }
    }

    private class MockWebsocketBuilder implements WebSocket.Builder {

        /**
         * {@inheritDoc}
         */
        @Override
        public java.net.http.WebSocket.Builder header(String name, String value) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public java.net.http.WebSocket.Builder connectTimeout(Duration timeout) {
            MockWebSocketServer.this.timeout = timeout;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public java.net.http.WebSocket.Builder subprotocols(String mostPreferred, String... lesserPreferred) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> buildAsync(URI uri, Listener listener) {
            MockWebSocketServer.this.listener = listener;
            return CompletableFuture.completedFuture(ws);
        }
    }

    private class MockWebSocket implements WebSocket {

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void request(long n) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSubprotocol() {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isOutputClosed() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isInputClosed() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void abort() {
            throw new Error();
        }
    }
}
