/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.sample.bean.Person;
import kiss.sample.bean.StringList;
import kiss.sample.bean.StringMap;

/**
 * 
 */
class ExpressionTest {

    @Test
    void variable() {
        Object context = define("item", "minimum language");

        assert I.express("I want {item}.", context).equals("I want minimum language.");
    }

    @Test
    void variableNotFound() {
        Object context = define();

        assert I.express("unknown variable is {ignored}", context).equals("unknown variable is ");
    }

    @Test
    void variables() {
        Object context = define("multiple", "all").define("variable", "values");

        assert I.express("{multiple} {variable} are accepted", context).equals("all values are accepted");
    }

    @Test
    void variableLocation() {
        assert I.express("{variableOnly}", define("variableOnly", "ok")).equals("ok");
        assert I.express("{head} is accepted", define("head", "this")).equals("this is accepted");
        assert I.express("tail is {accepted}", define("accepted", "ok")).equals("tail is ok");
        assert I.express("middle {is} fine too", define("is", "is")).equals("middle is fine too");
    }

    @Test
    void variableDescription() {
        assert I.express("{ spaceAtHead}", define("spaceAtHead", "ok")).equals("ok");
        assert I.express("{spaceAtTail }", define("spaceAtTail", "ok")).equals("ok");
        assert I.express("{ space }", define("space", "ok")).equals("ok");
        assert I.express("{\t spaceLike　}", define("spaceLike", "ok")).equals("ok");
        assert I.express("{separator . withSpace}", define("separator", define("withSpace", "ok"))).equals("ok");
    }

    @Test
    void contextNull() {
        assert I.express("null context is {ignored}", Collections.singletonList(null)).equals("null context is ");
        assert I.express("null context is {ignored}", (Object[]) null).equals("null context is {ignored}");
        assert I.express("null context is {ignored}", new Object[0]).equals("null context is {ignored}");
    }

    @Test
    void nestedVariable() {
        Object context = define("acceptable", $ -> {
            $.define("value", "ok");
        });

        assert I.express("nested variable is {acceptable.value}", context).equals("nested variable is ok");
    }

    @Test
    void nestedVariableNotFound() {
        Object context = define("is", $ -> {
        });

        assert I.express("nested unknown variable {is.ignored}", context).equals("nested unknown variable ");
    }

    @Test
    void object() {
        Person context = new Person();
        context.setAge(15);
        context.setLastName("Kahu");
        context.setFirstName("Tino");

        assert I.express("{firstName}({age}) : It's noisy.", context).equals("Tino(15) : It's noisy.");
    }

    @Test
    void list() {
        StringList context = new StringList();
        context.add("one");
        context.add("two");
        context.add("three");

        assert I.express("{0} {1} {2}", context).equals("one two three");
    }

    @Test
    void map() {
        StringMap context = new StringMap();
        context.put("1", "one");
        context.put("2", "two");
        context.put("3", "three");

        assert I.express("{1} {2} {3}", context).equals("one two three");
    }

    @Test
    void method() {
        StringList context = new StringList();
        context.add("one");
        context.add("two");
        context.add("three");

        assert I.express("list size is {size}", new Object[] {context}, (m, o, e) -> m.type.getMethod(e).invoke(o))
                .equals("list size is 3");
    }

    @Test
    void methodNotFound() {
        StringList context = new StringList();
        context.add("one");
        context.add("two");
        context.add("three");

        assert I.express("unknown method is {ignored}", context).equals("unknown method is ");
    }

    @Test
    void contexts() {
        Object c1 = define("first context", "will be ignored");
        Object c2 = define("value", "variable");

        assert I.express("{value} is not found in first context", c1, c2).equals("variable is not found in first context");
        assert I.express("{$} is not found in both contexts", c1, c2).equals(" is not found in both contexts");
    }

    @Test
    void contextsHaveSameProperty() {
        Object c1 = define("highPriority", "value");
        Object c2 = define("highPriority", "unused");

        assert I.express("first context has {highPriority}", c1, c2).equals("first context has value");
    }

    @Test
    void userResolver() {
        Person context = new Person();
        context.setAge(15);
        context.setLastName("Kahu");
        context.setFirstName("Tino");

        assert I.express("{nooo}", new Object[] {context}, (m, o, e) -> "fail").equals("fail");
    }

    /**
     * Shorthand to create empty {@link Context}.
     * 
     * @return
     */
    private Context define() {
        return new Context();
    }

    /**
     * Shorthand to create {@link Context}.
     * 
     * @param key
     * @param value
     * @return
     */
    private Context define(String key, Object value) {
        return new Context().define(key, value);
    }

    /**
     * Shorthand to create {@link Context}.
     * 
     * @param key
     * @param value
     * @return
     */
    private Context define(String key, Consumer<Context> nested) {
        Context nest = new Context();
        nested.accept(nest);

        return new Context().define(key, nest);
    }

    /**
     * 
     */
    @SuppressWarnings("serial")
    private static class Context extends HashMap<String, Object> {

        Context define(String key, Object value) {
            put(key, value);
            return this;
        }
    }
}
