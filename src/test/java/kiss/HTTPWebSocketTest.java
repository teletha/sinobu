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

import java.net.http.WebSocket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class HTTPWebSocketTest {

    static WebSocket web;

    public static void main(String[] args) throws InterruptedException {
        WS ws = new WS("wss://ws.lightstream.bitflyer.com/json-rpc");
        Disposable disposable = ws.invoke(new JsonRPC("subscribe", "lightning_executions_FX_BTC_JPY")).effectOnDispose(() -> {
            ws.invoke(new JsonRPC("unsubscribe", "lightning_executions_FX_BTC_JPY"));
        }).to(v -> {
            System.out.println(v);
        });

        Thread.sleep(1000 * 2);
        Disposable disposable2 = ws.invoke(new JsonRPC("subscribe", "lightning_board_FX_BTC_JPY")).effectOnDispose(() -> {
            ws.invoke(new JsonRPC("unsubscribe", "lightning_board_FX_BTC_JPY"));
        }).to(v -> {
            System.out.println(v);
        });

        Thread.sleep(1000 * 10);
        disposable.dispose();
        System.out.println("Dispose1");
        Thread.sleep(1000 * 10);
        disposable2.dispose();
        System.out.println("Dispose2");

        Thread.sleep(1000 * 10);
    }

    private static class JsonRPC {

        public long id = 123;

        public String jsonrpc = "2.0";

        public String method;

        public Map<String, String> params = new HashMap();

        private JsonRPC(String method, String channel) {
            this.method = method;
            this.params.put("channel", channel);
        }
    }

    public static Function<Object, Signal<String>> webscoket(String uri) {
        Deque queued = new ArrayDeque();
        WebSocket[] ws = new WebSocket[1];
        Signal<String> shared = I.http(uri, web -> {
            ws[0] = web;
            for (Object command : queued) {
                web.sendText(I.write(command), true);
            }
            queued.clear();
        }).share();

        return command -> {
            if (ws[0] == null) {
                queued.add(command);
            } else {
                ws[0].sendText(I.write(command), true);
            }
            return shared;
        };
    }

    public static class WS {

        private WebSocket ws;

        private Deque queued = new ArrayDeque();

        public final Signal<String> expose;

        private WS(String uri) {
            this.expose = I.http(uri, ws -> {
                synchronized (this) {
                    this.ws = ws;
                    for (Object command : queued) {
                        invoke(command);
                    }
                    queued = null;
                }
            }).share();
        }

        public synchronized Signal<String> invoke(Object command) {
            if (ws == null) {
                queued.add(command);
            } else {
                ws.sendText(I.write(command), true);
            }
            return expose;
        }

        public void close() {
            if (ws != null) {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "");
            }
        }
    }

}
