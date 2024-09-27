/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.sample.bean.Person;
import kiss.sample.bean.StringMap;

class ExpressionTest {

    @Test
    void text() {
        KVS context = $("lang", "the minimum language");

        assert I.express("I want plain text.", context).equals("I want plain text.");
    }

    /**
     * @see I#express(String, Object...)
     */
    @Test
    void variable() {
        KVS context = $("lang", "the minimum language");

        assert I.express("I want {lang}.", context).equals("I want the minimum language.");
    }

    @Test
    void variables() {
        KVS context = $("multiple", "All").$("variables", "values");

        assert I.express("{multiple} {variables} are accepted.", context).equals("All values are accepted.");
    }

    @Test
    void variableNotFound() {
        KVS context = new KVS();

        assert I.express("unknown variable is {ignored}", context).equals("unknown variable is ");
    }

    @Test
    void variableLocation() {
        assert I.express("{variableOnly}", $("variableOnly", "ok")).equals("ok");
        assert I.express("{head} is accepted", $("head", "this")).equals("this is accepted");
        assert I.express("tail is {accepted}", $("accepted", "ok")).equals("tail is ok");
        assert I.express("middle {is} fine too", $("is", "is")).equals("middle is fine too");
    }

    @Test
    void whitespaceInside() {
        assert I.express("{ spaceAtHead}", $("spaceAtHead", "ok")).equals("ok");
        assert I.express("{spaceAtTail }", $("spaceAtTail", "ok")).equals("ok");
        assert I.express("{ space }", $("space", "ok")).equals("ok");
        assert I.express("{\t spaceLike　}", $("spaceLike", "ok")).equals("ok");
        assert I.express("{separator . withSpace}", $("separator", () -> $("withSpace", "ok"))).equals("ok");
    }

    @Test
    void whitespaceOutside() {
        assert I.express(" {spaceAtHead}", $("spaceAtHead", "ok")).equals(" ok");
        assert I.express("{spaceAtTail} ", $("spaceAtTail", "ok")).equals("ok ");
        assert I.express(" {space} ", $("space", "ok")).equals(" ok ");
        assert I.express("\t{spaceLike}　", $("spaceLike", "ok")).equals("\tok　");
    }

    @Test
    void emptyPath() {
        KVS context = $("lang", "the minimum language");

        assert I.express("I want {}.", context).equals("I want {}.");
    }

    @Test
    void emptyLine() {
        assert I.express("""

                {emptyLine}

                """, $("emptyLine", "ok")).equals("""

                ok

                """);
    }

    @Test
    void emptyLineWithNull() {
        assert I.express("""

                {emptyLine}

                """, $("emptyLine", (String) null)).equals("""



                """);
    }

    @Test
    void sectionEmptyLine() {
        assert I.express("""
                |
                {#emptyLine}

                {/emptyLine}
                |
                """, $("emptyLine", "ok")).equals("""
                |

                |
                """);
    }

    @Test
    void sectionNoLine() {
        assert I.express("""
                |
                {#noLine}
                {/noLine}
                |
                """, $("noLine", "ok")).equals("""
                |
                |
                """);
    }

    @Test
    void escapedLine() {
        assert I.express("|\r\n{#emptyLine}\r\n{/emptyLine}\r\n|", $("emptyLine", "ok")).equals("|\r\n|");
    }

    @Test
    void contextNull() {
        assert I.express("null context is {ignored}", Collections.singletonList(null)).equals("null context is ");
        assert I.express("null context is {ignored}", (Object[]) null).equals("null context is ");
        assert I.express("null context is {ignored}", new Object[0]).equals("null context is ");
    }

    @Test
    void nestedVariable() {
        Object context = $("acceptable", () -> {
            $("value", "ok");
        });

        assert I.express("nested variable is {acceptable.value}", context).equals("nested variable is ok");
    }

    @Test
    void nestedVariableNotFound() {
        Object context = $("is", () -> {
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
        List context = List.of("one", "two", "three");

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
        List context = new ArrayList();
        context.add("one");
        context.add("two");
        context.add("three");

        assert I.express("list size is {size}", new Object[] {context}, (m, o, e) -> m.type.getMethod(e).invoke(o))
                .equals("list size is 3");
    }

    @Test
    void contexts() {
        Object c1 = $("first context", "will be ignored");
        Object c2 = $("value", "variable");

        assert I.express("{value} is not found in first context", c1, c2).equals("variable is not found in first context");
        assert I.express("{$} is not found in both contexts", c1, c2).equals(" is not found in both contexts");
    }

    @Test
    void contextsHaveSameProperty() {
        Object c1 = $("highPriority", "value");
        Object c2 = $("highPriority", "unused");

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
        List context = List.of("one", "two", "three");

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
        KVS context = $("root", () -> {
            $("sub", () -> {
                $("item1", () -> {
                    $("type", "sub");
                    $("no", "1");
                });
                $("item2", () -> {
                    $("type", "sub");
                    $("no", "2");
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
    void sectionNest() {
        KVS context = $("root1", () -> {
            $("item1", () -> {
                $("type", "item");
                $("no", "1");
            });
            $("item2", () -> {
                $("type", "item");
                $("no", "2");
            });
        }).$("root2", () -> {
            $("item3", () -> {
                $("type", "item");
                $("no", "3");
            });
            $("item4", () -> {
                $("type", "item");
                $("no", "4");
            });
        });

        assert I.express("""
                <div>
                    {#this}
                    <ol>
                        {#this}
                        <li>{type} {no}</li>
                        {/this}
                    </ol>
                    {/this}
                </div>
                """, context).equals("""
                <div>
                    <ol>
                        <li>item 1</li>
                        <li>item 2</li>
                    </ol>
                    <ol>
                        <li>item 3</li>
                        <li>item 4</li>
                    </ol>
                </div>
                """);
    }

    @Test
    void sectionNestWithInvert() {
        KVS context = $("root1", () -> {
            $("item1", () -> {
                $("type", "item");
                $("no", "1");
            });
            $("item2", () -> {
                $("type", "item");
                $("no", "2");
            });
        }).$("root2", () -> {
            $("item3", () -> {
                $("type", "item");
                $("no", "3");
            });
            $("item4", () -> {
                $("type", "item");
                $("no", "4");
            });
        });

        assert I.express("""
                <div>
                    {#this}
                    <ol>
                        {#this}
                        <li>{type} {no}</li>
                        {/this}
                        {^this}
                        <li>No Item</li>
                        {/this}
                    </ol>
                    {/this}
                </div>
                """, context).equals("""
                <div>
                    <ol>
                        <li>item 1</li>
                        <li>item 2</li>
                    </ol>
                    <ol>
                        <li>item 3</li>
                        <li>item 4</li>
                    </ol>
                </div>
                """);
    }

    @Test
    void sectionMultiple() {
        KVS context = $("root", () -> {
            $("item1", () -> {
                $("type", "sub");
                $("no", "1");
            });
            $("item2", () -> {
                $("type", "sub");
                $("no", "2");
            });
        });

        assert I.express("""
                <ul>
                    {#root}
                    <li>{type} {no}</li>
                    {/root}
                    {#root}
                    <li>item {no}</li>
                    {/root}
                </ul>
                """, context).equals("""
                <ul>
                    <li>sub 1</li>
                    <li>sub 2</li>
                    <li>item 1</li>
                    <li>item 2</li>
                </ul>
                """);
    }

    @Test
    void sectionExpressionWithChain() {
        @SuppressWarnings("unused")
        class Anime {
            public Character main = new Character("Lelouch Lamperouge", 17);
        }

        assert I.express("{#main.name}{this} has GEASS.{/main.name}", new Anime()).equals("Lelouch Lamperouge has GEASS.");
    }

    @Test
    void sectionExpressionWithBrokenChain() {
        @SuppressWarnings("unused")
        class Anime {
            public Character main = new Character(null, 17);
        }

        assert I.express("{#main.name}{this} has GEASS.{/main.name}", new Anime()).equals("");
    }

    /**
     * @see I#express(String, Object...)
     */
    @Test
    void sectionByEmptyList() {
        List context = Collections.emptyList();

        assert I.express("{#this}The text here will not be output.{/this}", context).equals("");
    }

    @Test
    void sectionByEmptyMap() {
        Map context = Collections.emptyMap();

        assert I.express("{#this}The text here will not be output.{/this}", context).equals("");
    }

    @Test
    void sectionByTrue() {
        boolean context = true;

        assert I.express("{#this}The text here will be output.{/this}", context).equals("The text here will be output.");
    }

    @Test
    void sectionByTrueOnProperty() {
        @SuppressWarnings("unused")
        class Context {
            public boolean value = true;
        }

        assert I.express("{#value}The text here will be output.{/value}", new Context()).equals("The text here will be output.");
    }

    /**
     * @see I#express(String, Object...)
     */
    @Test
    void sectionByFalse() {
        boolean context = false;

        assert I.express("{#this}The text here will not be output.{/this}", context).equals("");
    }

    @Test
    void sectionByFalseOnProperty() {
        @SuppressWarnings("unused")
        class Context {
            public boolean value = false;
        }

        assert I.express("{#value}The text here will not be output.{/value}", new Context()).equals("");
    }

    @Test
    void sectionByNull() {
        Object context = null;

        assert I.express("{#this}The text here will not be output.{/this}", context).equals("");
    }

    @Test
    void sectionByNullOnProperty() {
        @SuppressWarnings("unused")
        class Context {
            public String value = null;
        }

        assert I.express("{#value}The text here will not be output.{/value}", new Context()).equals("");
    }

    @Test
    void sectionByProperty() {
        @SuppressWarnings("unused")
        class Anime {
            public Character main = new Character("Lelouch Lamperouge", 17);
        }

        assert I.express("{#main}{name} is {age} years old.{/main}", new Anime()).equals("Lelouch Lamperouge is 17 years old.");
    }

    @Test
    void sectionByParentProperty() {
        @SuppressWarnings("unused")
        class Anime {
            public String title = "Code Geass";

            public Character main = new Character("Lelouch Lamperouge", 17);
        }

        assert I.express("{#main}{name} is main character of {title}.{/main}", new Anime())
                .equals("Lelouch Lamperouge is main character of Code Geass.");
    }

    @Test
    void sectionByNestedParentProperty() {
        @SuppressWarnings("unused")
        class Anime {
            public Character main1 = new Character("Lelouch Lamperouge", 17);

            public Character main2 = new Character("枢木 スザク", 17);
        }

        assert I.express("{#main1}{name} and {main2.name} are childhood friends.{/main1}", new Anime())
                .equals("Lelouch Lamperouge and 枢木 スザク are childhood friends.");
    }

    @Test
    void sectionByParentPropertyWithContextSelf() {
        @SuppressWarnings("unused")
        class Anime {
            public String title = "Code Geass";
        }

        assert I.express("{#title}{.} is {title}.{/title}", new Anime()).equals("Code Geass is Code Geass.");
    }

    @Test
    void invertedByList() {
        List context = List.of("one", "two", "three");

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
    void invertedByEmptyList() {
        List context = Collections.emptyList();

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
    void invertedByTrue() {
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
    void invertedByFalse() {
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

    @Test
    void invertedByNull() {
        Object context = null;

        assert I.express("{#this}The text here will not be output.{/this}", context).equals("");
    }

    @Test
    void invertedByNullOnProperty() {
        @SuppressWarnings("unused")
        class Context {
            public String value = null;
        }

        assert I.express("{^value}The text here will be output.{/value}", new Context()).equals("The text here will be output.");
    }

    @Test
    void commentInline() {
        assert I.express("Ignore {!Comments begin with a bang and are ignored.} comment", new Object()).equals("Ignore  comment");
        assert I.express("Ignore {! comment} comment{! comment }", new Object()).equals("Ignore  comment");
    }

    @Test
    void commentBlock() {
        assert I.express("""
                Start
                {!comment}
                End
                """, new Object()).equals("""
                Start

                End
                """);
    }

    @Test
    void commentIndentedBlock() {
        assert I.express("""
                Start
                  {!with head and tail indent}
                End
                """, new Object()).equals("""
                Start
                \s\s
                End
                """);
    }

    @Test
    void commentBlockInSection() {
        assert I.express("""
                Start
                {#this}
                {!comment}
                {/this}
                End
                """, new Object()).equals("""
                Start

                End
                """);
    }

    @Test
    void plain() {
        assert I.express("{!!Plain text begin with double bang!!!}", new Object()).equals("{Plain text begin with double bang!!!}");
        assert I.express("{!! Plain text begin with double bang!!! }", new Object()).equals("{ Plain text begin with double bang!!! }");
    }

    @Test
    void plainWithChangedDelimiter() {
        assert I.express("""
                {=!! !!=}
                !!!! Plain text begin with double bang !!
                """, new Object()).equals("""
                !! Plain text begin with double bang !!
                """);
    }

    // @Test
    // void delimiterInline() {
    // KVS context = $("value", "change delimiter");
    //
    // assert I.express("{=<% %>=}<%value%>", context).equals("change delimiter");
    // }
    //
    // @Test
    // void delimiterInlineInBlock() {
    // KVS context = $("value", "change delimiter");
    //
    // assert I.express("""
    // {=<% %>=}<%value%>
    // """, context).equals("""
    // change delimiter
    // """);
    // }

    @Test
    void setDelimiterOnInitialization() {
        KVS context = $("value", "change delimiter");

        assert I.express("""
                ##value##
                """, "##", "##", new Object[] {context}).equals("""
                change delimiter
                """);
    }

    @Test
    void setDelimiter1() {
        KVS context = $("value", "change delimiter");

        assert I.express("""
                {={{ }}=}
                {{value}}
                """, context).equals("""
                change delimiter
                """);
    }

    @Test
    void setDelimiter2() {
        KVS context = $("prev", "original delimiter").$("next", "changed delimiter").$("last", "final delimiter");

        assert I.express("""
                {prev}
                {={{ }}=}
                {{next}}
                {{={{{ }}}=}}
                {{{last}}}
                """, context).equals("""
                original delimiter
                changed delimiter
                final delimiter
                """);
    }

    @Test
    void setDelimiter3() {
        KVS context = $("prev", "original delimiter").$("next", "changed delimiter").$("last", "final delimiter");

        assert I.express("""
                {prev}
                {=<% %>=}
                <%next%>
                <%=$$ @=%>
                $$last@
                """, context).equals("""
                original delimiter
                changed delimiter
                final delimiter
                """);
    }

    @Test
    void setLongDelimiterWithSection() {
        List context = List.of("one", "two", "three");

        assert I.express("""
                {=[LONGDELIMITER] [LONGDELIMITER]=}
                <ul>
                    [LONGDELIMITER]#this[LONGDELIMITER]
                    <li>[LONGDELIMITER].[LONGDELIMITER]</li>
                    [LONGDELIMITER]/this[LONGDELIMITER]
                </ul>
                """, context).equals("""
                <ul>
                    <li>one</li>
                    <li>two</li>
                    <li>three</li>
                </ul>
                """);
    }

    /** The context stack manager. */
    private static ThreadLocal<Deque<KVS>> context = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * Set String value.
     * 
     * @param key
     * @param value
     */
    private static KVS $(String key, String value) {
        KVS latest = context.get().peekLast();
        if (latest == null) {
            latest = new KVS();
        }
        latest.put(key, value);
        return latest;
    }

    /**
     * Set the nested Map.
     * 
     * @param key
     * @param inner
     * @return
     */
    private static KVS $(String key, Runnable inner) {
        Deque<KVS> deque = context.get();
        KVS latest = deque.peekLast();
        if (latest == null) {
            latest = new KVS();
        }

        KVS child = new KVS();
        latest.put(key, child);

        deque.addLast(child);
        inner.run();
        deque.removeLast();

        return latest;
    }

    /**
     * 
     */
    @SuppressWarnings("serial")
    private static class KVS extends LinkedHashMap<String, Object> {

        private KVS $(String key, String value) {
            put(key, value);
            return this;
        }

        private KVS $(String key, Runnable inner) {
            KVS child = new KVS();
            put(key, child);

            Deque<KVS> deque = context.get();
            deque.addLast(child);
            inner.run();
            deque.removeLast();
            return this;
        }
    }

    record Character(String name, int age) {
    }
}