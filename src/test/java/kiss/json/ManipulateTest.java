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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.JSON;

public class ManipulateTest {

    @Test
    public void readValue() {
        JSON json = I.json("""
                {
                    "number" : 10,
                    "bool" : false
                }
                """);

        assert json.get("number").as(int.class) == 10;
        assert json.get("bool").as(boolean.class) == false;
    }

    @Test
    public void readNestedValue() {
        JSON json = I.json("""
                {
                    "child1" : {
                        "name" : "first"
                    },
                    "child2" : {
                        "name" : "second"
                    }
                }
                """);

        assert json.get("child1").get("name").as(String.class).equals("first");
        assert json.get("child2").get("name").as(String.class).equals("second");
    }

    @Test
    public void get() {
        JSON json = I.json("""
                {
                    "string" : "value",
                    "int"   : "10",
                    "float" : 1.33
                }
                """);

        assert json.get("string").as(String.class).equals("value");
        assert json.get("int").as(int.class) == 10;
        assert json.get("float").as(float.class) == 1.33f;
    }

    @Test
    void getNullKey() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.get(null) == null;
    }

    @Test
    void getUnknownKey() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.get("unknown") == null;
    }

    @Test
    public void getAsYourType() {
        JSON json = I.json("""
                {
                    "string" : "value",
                    "int"   : "10",
                    "float" : 1.33
                }
                """);

        assert json.get(String.class, "string").equals("value");
        assert json.get(int.class, "int") == 10;
        assert json.get(float.class, "float") == 1.33f;
    }

    @Test
    void getAsNullKey() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.get(String.class, null) == null;
    }

    @Test
    void getAsUnkwnonKey() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.get(String.class, "unknown") == null;
    }

    @Test
    void getAsNullType() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        Assertions.assertThrows(NullPointerException.class, () -> json.get(null, "key"));
    }

    @Test
    void set() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.set("set", "new") == json;
        assert json.get(String.class, "set").equals("new");
    }

    @Test
    void setNullKey() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.set(null, "fail") == json;
        assert json.get(String.class, "set") == null;
    }

    @Test
    void setNullValue() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.set("fail", null) == json;
        assert json.get(String.class, "fail") == null;
    }

    @Test
    void setExistKey() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.set("key", "update") == json;
        assert json.get(String.class, "key").equals("update");
    }

    @Test
    void has() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.has("key", "value");
        assert json.has("key", "diff") == false;
    }

    @Test
    void hasUnknownKey() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.has("unknown", null) == false;
        assert json.has("unknown", "value") == false;
    }

    @Test
    void hasNullKey() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.has(null, null) == false;
        assert json.has(null, "value") == false;
    }

    @Test
    void hasIntegral() {
        JSON json = I.json("""
                { "key" : 12 }
                """);
        assert json.has("key", "12");
        assert json.has("key", 12);
    }

    @Test
    void hasNegative() {
        JSON json = I.json("""
                { "key" : -12 }
                """);
        assert json.has("key", "-12");
        assert json.has("key", -12);
    }

    @Test
    void hasDecimal() {
        JSON json = I.json("""
                { "key" : 1.2 }
                """);
        assert json.has("key", "1.2");
        assert json.has("key", 1.2);
    }

    @Test
    void hasTrue() {
        JSON json = I.json("""
                { "key" : true }
                """);
        assert json.has("key", "true");
        assert json.has("key", true);
    }

    void hasFalse() {
        JSON json = I.json("""
                { "key" : false }
                """);
        assert json.has("key", "false");
        assert json.has("key", false);
    }
}