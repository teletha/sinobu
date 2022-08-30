/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import java.io.StringReader;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import antibug.profiler.Benchmark;
import kiss.I;
import kiss.JSON;

public class JSONParseBenchmark {

    private static final String json = """
              {
                "jsonrpc": "2.0",
                "method": ["channelMessage"],
                "params": {
                    "channel": "BTC_PERP",
                    "message": [{
                        "id": 1817558716,
                        "side": "BUY",
                        "price": 986358.0,
                        "size": 0.09,
                        "exec_date": "2020-07-03T03:58:43.2166073Z",
                        "buy_child_order_acceptance_id": "JRF20200703-035843-441414",
                        "sell_child_order_acceptance_id": "JRF20200703-035842-128834",
                        "liquidated": true
                    }, {
                        "id": 1817558717,
                        "side": "BUY",
                        "price": 986343.0,
                        "size": 0.1415,
                        "exec_date": "2020-07-03T03:58:43.2166076Z",
                        "buy_child_order_acceptance_id": "JRF20200703-056757-3333666",
                        "sell_child_order_acceptance_id": "JRF20200703-056756-3937435",
                        "liquidated": false
                    }, {
                        "id": 1817558718,
                        "side": "BUY",
                        "price": 986333.0,
                        "size": 1.3555,
                        "exec_date": "2020-07-03T03:58:43.2166076Z",
                        "buy_child_order_acceptance_id": "JRF20200703-3637388-963432",
                        "sell_child_order_acceptance_id": "JRF20200703-035841-907568",
                        "liquidated": false
                    }, {
                        "id": 1817558719,
                        "side": "BUY",
                        "price": 986321.0,
                        "size": 0.4,
                        "exec_date": "2020-07-03T03:58:43.2166334Z",
                        "buy_child_order_acceptance_id": "JRF20200703-035843-4163785",
                        "sell_child_order_acceptance_id": "JRF20200703-604475-7036387",
                        "liquidated": false
                    }, {
                        "id": 1817558720,
                        "side": "BUY",
                        "price": 986329.0,
                        "size": 1,
                        "exec_date": "2020-07-03T03:58:43.2166343Z",
                        "buy_child_order_acceptance_id": "JRF20200703-035843-441414",
                        "sell_child_order_acceptance_id": "JRF20200703-035841-5334780",
                        "liquidated": false
                    }, {
                        "id": 1817558721,
                        "side": "SELL",
                        "price": 0.45452,
                        "size": 0.01710225,
                        "exec_date": "2020-07-03T03:58:43.2166343Z",
                        "buy_child_order_acceptance_id": "JRF20200703-035843-1980383",
                        "sell_child_order_acceptance_id": "JRF20200703-035841-3448929",
                        "liquidated": false
                    }, {
                        "id": 1817558722,
                        "side": "SELL",
                        "price": 986322.0,
                        "size": 0.033,
                        "exec_date": "2020-07-03T03:58:43.734634Z",
                        "buy_child_order_acceptance_id": "JRF20200703-035843-1980383",
                        "sell_child_order_acceptance_id": "JRF20200703-035841-660736",
                        "liquidated": false
                    }]
                }
            }
            """;

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("New Parser", () -> {
            return new FastParser().parse(new StringReader(json), JSON.class);
        });

        benchmark.measure("Sinobu", () -> {
            return I.json(json);
        });

        benchmark.measure("FastJson", () -> {
            return com.alibaba.fastjson.JSON.parseObject(json, JSONObject.class);
        });

        Gson gson = new Gson();
        benchmark.measure("Gson", () -> {
            return gson.fromJson(json, JSONObject.class);
        });

        ObjectMapper mapper = new ObjectMapper();
        benchmark.measure("Jackson", () -> {
            return mapper.readTree(json);
        });

        benchmark.perform();
    }
}