/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import antibug.profiler.Benchmark;
import kiss.I;

public class JSONParseBenchmark {

    private static final String json = "{\"jsonrpc\":\"2.0\",\"method\":\"channelMessage\",\"params\":{\"channel\":\"lightning_executions_FX_BTC_JPY\",\"message\":[{\"id\":1817558716,\"side\":\"BUY\",\"price\":986398.0,\"size\":0.09,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035842-128834\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"}]}}";

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("Sinobu", () -> {
            return I.json(json);
        });

        Gson gson = new Gson();
        benchmark.measure("Gson", () -> {
            return gson.fromJson(json, JSONObject.class);
        });

        ObjectMapper mapper = new ObjectMapper();
        benchmark.measure("Jackson", () -> {
            return mapper.readTree(json);
        });

        benchmark.measure("FastJson", () -> {
            return com.alibaba.fastjson.JSON.parseObject(json, JSONObject.class);
        });

        benchmark.perform();
    }
}