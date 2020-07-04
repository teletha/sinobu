/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import antibug.profiler.Benchmark;
import kiss.I;
import kiss.JSON;

public class JSONBenchmark {

    private static final String json = "{\"jsonrpc\":\"2.0\",\"method\":\"channelMessage\",\"params\":{\"channel\":\"lightning_executions_FX_BTC_JPY\",\"message\":[{\"id\":1817558716,\"side\":\"BUY\",\"price\":986398.0,\"size\":0.09,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035842-128834\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"},{\"id\":1817558717,\"side\":\"BUY\",\"price\":986399.0,\"size\":0.01710225,\"exec_date\":\"2020-07-03T03:58:43.2166073Z\",\"buy_child_order_acceptance_id\":\"JRF20200703-035843-441414\",\"sell_child_order_acceptance_id\":\"JRF20200703-035841-660736\"}]}}";

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        benchmark.measure("Sinobu", () -> {
            List list = new ArrayList();

            I.json(json).get("params", JSON.class).get("message", JSON.class).find(JSON.class, "*").forEach(list::add);
            return list;
        });

        benchmark.measure("SinobuFind", () -> {
            List list = new ArrayList();

            for (JSON e : I.json(json).find(JSON.class, "params", "message", "*")) {
                list.add(e);
            }
            return list;
        });

        Gson gson = new Gson();
        benchmark.measure("Gson", () -> {
            List list = new ArrayList();

            for (JsonElement e : gson.fromJson(json, JsonObject.class).getAsJsonObject("params").getAsJsonArray("message")) {
                list.add(e);
            }
            return list;
        });

        ObjectMapper mapper = new ObjectMapper();
        benchmark.measure("Jackson", () -> {
            List list = new ArrayList();

            for (JsonNode e : mapper.readTree(json).get("params").get("message")) {
                list.add(e);
            }
            return list;
        });

        benchmark.perform();
    }
}
