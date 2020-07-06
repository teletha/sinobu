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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.JSON;

class ManipulateTest {

    @Test
    void get() {
        JSON json = write("{ 'key':'value' }");
        assert json.get("key").as(String.class).equals("value");
    }

    @Test
    void getNullKey() {
        JSON json = write("{ 'key':'value' }");
        assert json.get(null) == null;
    }

    @Test
    void getUnknownKey() {
        JSON json = write("{ 'key':'value' }");
        assert json.get("unknown") == null;
    }

    @Test
    void getTyped() {
        JSON json = write("{ 'key':'value' }");
        assert json.get(String.class, "key").equals("value");
    }

    @Test
    void getAsNullKey() {
        JSON json = write("{ 'key':'value' }");
        assert json.get(String.class, null) == null;
    }

    @Test
    void getAsUnkwnonKey() {
        JSON json = write("{ 'key':'value' }");
        assert json.get(String.class, "unknown") == null;
    }

    @Test
    void getAsNullType() {
        JSON json = write("{ 'key':'value' }");
        Assertions.assertThrows(NullPointerException.class, () -> json.get(null, "key"));
    }

    @Test
    void set() {
        JSON json = write("{ 'key':'value' }");
        assert json.set("set", "new") == json;
        assert json.get(String.class, "set").equals("new");
    }

    @Test
    void setNullKey() {
        JSON json = write("{ 'key':'value' }");
        assert json.set(null, "fail") == json;
        assert json.get(String.class, "set") == null;
    }

    @Test
    void setNullValue() {
        JSON json = write("{ 'key':'value' }");
        assert json.set("fail", null) == json;
        assert json.get(String.class, "fail") == null;
    }

    @Test
    void setExistKey() {
        JSON json = write("{ 'key':'value' }");
        assert json.set("key", "update") == json;
        assert json.get(String.class, "key").equals("update");
    }

    @Test
    void has() {
        JSON json = write("{ 'key':'value' }");
        assert json.has("key", "value");
        assert json.has("key", "diff") == false;
    }

    @Test
    void hasUnknownKey() {
        JSON json = write("{ 'key':'value' }");
        assert json.has("unknown", null);
        assert json.has("unknown", "value") == false;
    }

    @Test
    void hasNullKey() {
        JSON json = write("{ 'key':'value' }");
        assert json.has(null, null);
        assert json.has(null, "value") == false;
    }

    @Test
    void hasDifferentType() {
        JSON json = write("{ 'key':'12' }");
        assert json.has("key", "12");
        assert json.has("key", 12) == false;

        json = write("{ 'key':'true' }");
        assert json.has("key", "true");
        assert json.has("key", true) == false;
    }

    /**
     * JSON writer.
     * 
     * @param json
     * @return
     */
    private JSON write(String json) {
        return I.json(json.replace('\'', '"'));
    }
}
