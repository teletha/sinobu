/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import java.util.List;
import java.util.Map;

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

        assert json.get(int.class, "number") == 10;
        assert json.get(boolean.class, "bool") == false;
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
    public void readNestedValueBySequentialKeys() {
        JSON json = I.json("""
                [
                    {
                        "type" : {
                            "name" : "first"
                        }
                    },
                    {
                        "type" : {
                            "name" : "second"
                        }
                    }
                ]
                """);

        List<String> found = json.find(String.class, "*", "type", "name");
        assert found.size() == 2;
        assert found.get(0).equals("first");
        assert found.get(1).equals("second");
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
    public void setValue() {
        JSON json = I.json("""
                { "key" : "value" }
                """);
        assert json.set("newKey", "newValue") == json;
        assert json.get(String.class, "newKey").equals("newValue");
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
        assert json.has("key", 12);
    }

    @Test
    void hasNegative() {
        JSON json = I.json("""
                { "key" : -12 }
                """);
        assert json.has("key", -12);
    }

    @Test
    void hasDecimal() {
        JSON json = I.json("""
                { "key" : 1.2 }
                """);
        assert json.has("key", 1.2);
    }

    @Test
    void hasTrue() {
        JSON json = I.json("""
                { "key" : true }
                """);
        assert json.has("key", true);
    }

    @Test
    void hasFalse() {
        JSON json = I.json("""
                { "key" : false }
                """);
        assert json.has("key", false);
    }

    @Test
    void asMap() {
        JSON json = I.json("""
                { "one" : "1" }
                """);

        Map<String, String> map = json.asMap(String.class);
        assert map.get("one").equals("1");
    }

    @Test
    void asMapWithTypeTransformation() {
        JSON json = I.json("""
                { "one" : "1" }
                """);

        Map<String, Integer> map = json.asMap(int.class);
        assert map.get("one") == 1;
    }

    @Test
    void asMapWithNullType() {
        JSON json = I.json("""
                { "one" : "1" }
                """);

        Assertions.assertThrows(NullPointerException.class, () -> json.asMap(null));
    }
}