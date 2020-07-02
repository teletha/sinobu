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

import java.util.HashMap;
import java.util.Map;

public class HTTPWebSocketTest {

    public static void main(String[] args) throws InterruptedException {
        Signal<String> socket = I.http("wss://ws.lightstream.bitflyer.com/json-rpc", input -> {
            JsonRPC rpc = new JsonRPC();
            rpc.method = "subscribe";
            rpc.params.put("channel", "lightning_executions_FX_BTC_JPY");
            input.sendText(I.write(rpc), true);

            rpc = new JsonRPC();
            rpc.method = "subscribe";
            rpc.params.put("channel", "lightning_board_FX_BTC_JPY");
            input.sendText(I.write(rpc), true);
        }).share();

        Disposable disposable = socket.to(v -> {
            System.out.println(v);
        }, e -> {
            e.printStackTrace();
        }, () -> {
            System.out.println("COMPLETE");
        });

        Thread.sleep(1000 * 2);
        socket.to(v -> {
            System.out.println(v);
        }, e -> {
            e.printStackTrace();
        }, () -> {
            System.out.println("COMPLETE");
        });

        Thread.sleep(1000 * 60);
        disposable.dispose();
        System.out.println("Dispose");
        Thread.sleep(1000 * 5);
    }

    private static class JsonRPC {

        public long id = 123;

        public String jsonrpc = "2.0";

        public String method;

        public Map<String, String> params = new HashMap();
    }

}
