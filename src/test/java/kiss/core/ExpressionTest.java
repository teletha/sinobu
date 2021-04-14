/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
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

    /**
     * @see I#express(String, Object...)
     */
    @Test
    void variable() {
        Object context = kvs("lang", "the minimum language");

        assert I.express("I want {lang}.", context).equals("I want the minimum language.");
    }

    /**
     * @see I#express(String, Object...)
     */
    @Test
    void variables() {
        Object context = kvs("multiple", "All").kvs("variables", "values");

        assert I.express("{multiple} {variables} are accepted.", context).equals("All values are accepted.");
    }

    @Test
    void variableNotFound() {
        Object context = kvs();

        assert I.express("unknown variable is {ignored}", context).equals("unknown variable is ");
    }

    @Test
    void variableLocation() {
        assert I.express("{variableOnly}", kvs("variableOnly", "ok")).equals("ok");
        assert I.express("{head} is accepted", kvs("head", "this")).equals("this is accepted");
        assert I.express("tail is {accepted}", kvs("accepted", "ok")).equals("tail is ok");
        assert I.express("middle {is} fine too", kvs("is", "is")).equals("middle is fine too");
    }

    @Test
    void variableDescription() {
        assert I.express("{ spaceAtHead}", kvs("spaceAtHead", "ok")).equals("ok");
        assert I.express("{spaceAtTail }", kvs("spaceAtTail", "ok")).equals("ok");
        assert I.express("{ space }", kvs("space", "ok")).equals("ok");
        assert I.express("{\t spaceLike　}", kvs("spaceLike", "ok")).equals("ok");
        assert I.express("{separator . withSpace}", kvs("separator", kvs("withSpace", "ok"))).equals("ok");
    }

    @Test
    void contextNull() {
        assert I.express("null context is {ignored}", Collections.singletonList(null)).equals("null context is ");
        assert I.express("null context is {ignored}", (Object[]) null).equals("null context is {ignored}");
        assert I.express("null context is {ignored}", new Object[0]).equals("null context is {ignored}");
    }

    @Test
    void nestedVariable() {
        Object context = kvs("acceptable", $ -> {
            $.kvs("value", "ok");
        });

        assert I.express("nested variable is {acceptable.value}", context).equals("nested variable is ok");
    }

    @Test
    void nestedVariableNotFound() {
        Object context = kvs("is", $ -> {
        });

        assert I.express("nested unknown variable {is.ignored}", context).equals("nested unknown variable ");
    }

    /**
     * @see I#express(String, Object...)
     */
    @Test
    void bean() {
        Person bean = new Person();
        bean.setAge(15);
        bean.setLastName("Kahu");
        bean.setFirstName("Tino");

        assert I.express("{firstName}({age}) : It's noisy.", bean).equals("Tino(15) : It's noisy.");
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
        Object c1 = kvs("first context", "will be ignored");
        Object c2 = kvs("value", "variable");

        assert I.express("{value} is not found in first context", c1, c2).equals("variable is not found in first context");
        assert I.express("{$} is not found in both contexts", c1, c2).equals(" is not found in both contexts");
    }

    @Test
    void contextsHaveSameProperty() {
        Object c1 = kvs("highPriority", "value");
        Object c2 = kvs("highPriority", "unused");

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
     * @see I#express(String, Object...)
     */
    @Test
    void section() {
        StringList context = new StringList();
        context.add("one");
        context.add("two");
        context.add("three");

        assert I.express("""
                <ul>
                    {#this}
                    <li>{.}</li>
                    {/this}
                </ul>
                """, context).equals("""
                <ul>
                    <li>one</li>
                    <li>two</li>
                    <li>three</li>
                </ul>
                """);
    }

    @Test
    void sectionDeep() {
        Context context = kvs("root", root -> {
            root.kvs("sub", sub -> {
                sub.kvs("1", c -> {
                    c.kvs("type", "sub");
                    c.kvs("no", "1");
                }).kvs("2", c -> {
                    c.kvs("type", "sub");
                    c.kvs("no", "2");
                });
            });
        });

        assert I.express("""
                <ul>
                    {#root.sub}
                    <li>{type} {no}</li>
                    {/root.sub}
                </ul>
                """, context).equals("""
                <ul>
                    <li>sub 1</li>
                    <li>sub 2</li>
                </ul>
                """);
    }

    @Test
    void sectionMultiVariables() {
        Context context = kvs("1", c -> {
            c.kvs("type", "sub");
            c.kvs("no", "1");
        }).kvs("2", c -> {
            c.kvs("type", "sub");
            c.kvs("no", "2");
        });

        assert I.express("""
                <ul>
                    {#this}
                    <li>{type} {no}</li>
                    {/this}
                </ul>
                """, context).equals("""
                <ul>
                    <li>sub 1</li>
                    <li>sub 2</li>
                </ul>
                """);
    }

    @Test
    void sectionByEmptyCollection() {
        StringList context = new StringList();

        assert I.express("""
                <ul>
                    {#this}
                    <li>{.}</li>
                    {/this}
                </ul>
                """, context).equals("""
                <ul>
                </ul>
                """);
    }

    @Test
    void sectionByTrue() {
        boolean context = true;

        assert I.express("""
                <ul>
                    {#this}
                    <li>{.}</li>
                    {/this}
                </ul>
                """, context).equals("""
                <ul>
                    <li>true</li>
                </ul>
                """);
    }

    @Test
    void sectionByFalse() {
        boolean context = false;

        assert I.express("""
                <ul>
                    {#this}
                    <li>{.}</li>
                    {/this}
                </ul>
                """, context).equals("""
                <ul>
                </ul>
                """);
    }

    @Test
    void sectionInvertByList() {
        StringList context = new StringList();
        context.add("one");
        context.add("two");
        context.add("three");

        assert I.express("""
                <ul>
                    {#this}
                    <li>{.}</li>
                    {/this}
                    {^this}
                    <li>No Items</li>
                    {/this}
                </ul>
                """, context).equals("""
                <ul>
                    <li>one</li>
                    <li>two</li>
                    <li>three</li>
                </ul>
                """);
    }

    /**
     * @see I#express(String, Object...)
     */
    @Test
    void sectionInvertByEmptyList() {
        StringList context = new StringList();

        assert I.express("""
                <ul>
                    {#this}
                    <li>{.}</li>
                    {/this}
                    {^this}
                    <li>No Items</li>
                    {/this}
                </ul>
                """, context).equals("""
                <ul>
                    <li>No Items</li>
                </ul>
                """);
    }

    @Test
    void sectionInvertByTrue() {
        boolean context = true;

        assert I.express("""
                <ul>
                    {#this}
                    <li>{.}</li>
                    {/this}
                    {^this}
                    <li>No Items</li>
                    {/this}
                </ul>
                """, context).equals("""
                <ul>
                    <li>true</li>
                </ul>
                """);
    }

    @Test
    void sectionInvertByFalse() {
        boolean context = false;

        assert I.express("""
                <ul>
                    {#this}
                    <li>{.}</li>
                    {/this}
                    {^this}
                    <li>No Items</li>
                    {/this}
                </ul>
                """, context).equals("""
                <ul>
                    <li>No Items</li>
                </ul>
                """);
    }

    /**
     * Shorthand to create empty {@link Context}.
     * 
     * @return
     */
    private Context kvs() {
        return new Context();
    }

    /**
     * Shorthand to create {@link Context}.
     * 
     * @param key
     * @param value
     * @return
     */
    private Context kvs(String key, Object value) {
        return new Context().kvs(key, value);
    }

    /**
     * Shorthand to create {@link Context}.
     * 
     * @param key
     * @param value
     * @return
     */
    private Context kvs(String key, Consumer<Context> nested) {
        Context nest = new Context();
        nested.accept(nest);

        return new Context().kvs(key, nest);
    }

    /**
     * 
     */
    @SuppressWarnings("serial")
    private static class Context extends HashMap<String, Object> {

        Context kvs(String key, Object value) {
            put(key, value);
            return this;
        }

        Context kvs(String key, Consumer<Context> nested) {
            Context nest = new Context();
            nested.accept(nest);

            return kvs(key, nest);
        }
    }
}