/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

import kiss.I;
import kiss.XML;

public class XMLManipulationTest {

    @Test
    public void append() {
        XML root = I.xml("<m><Q><P/></Q><Q><P/></Q></m>");

        assert root.find("Q").append("<R/><R/>").find("R").size() == 4;
        assert root.find("Q > R").size() == 4;
        assert root.find("Q > R:first-child").size() == 0;
    }

    @Test
    public void appendText() {
        XML root = I.xml("<m><Q><P/></Q><Q><P/></Q></m>");

        assert root.find("Q").append("text").find("P").size() == 2;
        assert root.find("Q").text().equals("texttext");
    }

    @Test
    public void prepend() {
        String xml = "<m><Q><P/></Q><Q><P/></Q></m>";

        XML e = I.xml(xml);
        assert e.find("Q").prepend("<R/><R/>").find("R").size() == 4;
        assert e.find("Q > R").size() == 4;
        assert e.find("Q > R:first-child").size() == 2;
    }

    @Test
    public void prependText() {
        String xml = "<m><Q><P/></Q><Q><P/></Q></m>";

        XML e = I.xml(xml);
        assert e.find("Q").prepend("text").find("P").size() == 2;
        assert e.find("Q").text().equals("texttext");
    }

    @Test
    public void before() {
        String xml = "<m><P/><Q/></m>";

        XML e = I.xml(xml);
        e.find("Q").before("<R/>");

        assert e.find("R:first-child").size() == 0;
        assert e.find("R:last-child").size() == 0;
        assert e.find("R:nth-child(2)").size() == 1;
    }

    @Test
    public void beforeText() {
        String xml = "<m><P/><Q/></m>";

        XML e = I.xml(xml);
        e.find("Q").before("text");

        assert e.find("P,Q").size() == 2;
        assert e.text().equals("text");
    }

    @Test
    public void after() {
        String xml = "<m><P/><Q/></m>";

        XML e = I.xml(xml);
        e.find("P").after("<R/>");

        assert e.find("R:first-child").size() == 0;
        assert e.find("R:last-child").size() == 0;
        assert e.find("R:nth-child(2)").size() == 1;
    }

    @Test
    public void afterRoot() {
        String xml = "<m/>";

        XML e = I.xml(xml);
        e.after("<after/>");

        assert e.next().name().equals("after");
    }

    @Test
    public void afterText() {
        String xml = "<m><P/><Q/></m>";

        XML e = I.xml(xml);
        e.find("P").before("text");

        assert e.find("P,Q").size() == 2;
        assert e.text().equals("text");
    }

    @Test
    public void empty() {
        XML root = I.xml("<Q><P/><P/></Q>");
        root.empty();

        assert root.find("P").size() == 0;
    }

    @Test
    public void remove() {
        XML root = I.xml("<Q><S/><T/><S/></Q>");
        assert root.find("*").remove().size() == 3;

        assert root.find("S").size() == 0;
        assert root.find("T").size() == 0;
    }

    @Test
    public void wrap() {
        String xml = "<m><Q/><Q/></m>";

        XML e = I.xml(xml);
        e.find("Q").wrap("<P/>");

        assert e.find("P > Q").size() == 2;
        assert e.find("P").size() == 2;
        assert e.find("Q").size() == 2;
    }

    @Test
    public void wrapAll() {
        String xml = "<m><Q/><Q/></m>";

        XML e = I.xml(xml);
        e.find("Q").wrapAll("<P/>");

        assert e.find("P > Q").size() == 2;
        assert e.find("P").size() == 1;
        assert e.find("Q").size() == 2;
    }

    @Test
    public void textGet() {
        String xml = "<Q>ss<P>a<i>a</i>a</P><P> b </P><P>c c</P>ss</Q>";

        assert I.xml(xml).find("P").text().equals("aaa b c c");
    }

    @Test
    public void textSet() {
        String xml = "<Q><P>aaa</P></Q>";

        XML e = I.xml(xml);
        e.find("P").text("set");

        assert e.find("P:contains(set)").size() == 1;
    }

    @Test
    public void attrGet() {
        String xml = "<Q name='value' key='map'/>";

        assert I.xml(xml).attr("name").equals("value");
        assert I.xml(xml).attr("key").equals("map");
    }

    @Test
    public void attrGetNone() {
        String xml = "<Q/>";

        assert I.xml(xml).attr("name").equals("");
        assert I.xml(xml).attr("key").equals("");
    }

    @Test
    public void attrGetNS() {
        String xml = "<Q xmlns:P='p' xmlns:z='z' z:name='fail' P:name='value' name='fail'/>";

        XML e = I.xml(xml);

        assert e.attr("P:name").equals("value");
    }

    @Test
    public void attrSet() {
        String xml = "<Q name='value' key='map'/>";

        XML e = I.xml(xml);
        e.attr("name", "set");

        assert e.attr("name").equals("set");
        assert e.attr("key").equals("map");
        assert e.attr("name", null).find("Q[name]").size() == 0;
    }

    @Test
    public void attrSetNS() {
        String xml = "<Q xmlns:P='p' xmlns:z='z' z:name='fail' P:name='value' name='fail'/>";

        XML e = I.xml(xml);
        e.attr("P:name", "set");

        assert e.attr("P:name").equals("set");
    }

    @Test
    public void attrCreate() {
        String xml = "<Q/>";

        XML e = I.xml(xml);

        assert e.attr("name").equals("");
        e.attr("name", "set");
        assert e.attr("name").equals("set");
    }

    @Test
    public void attrCreateNs() {
        String xml = "<Q/>";

        XML e = I.xml(xml);
        e.attr("P:name", "set");

        assert e.attr("P:name").equals("set");
        assert e.attr("name").equals("");
    }

    @Test
    public void attrNull() {
        String xml = "<Q/>";

        XML e = I.xml(xml);
        e.attr(null, "one"); // no error
        e.attr("", "two"); // no error

        assert e.attr("").equals("");
    }

    @Test
    public void attrInvalidName() {
        String xml = "<Q/>";

        XML e = I.xml(xml);
        e.attr("0", "one"); // no error
        e.attr("$", "one"); // no error

        assert e.attr("0").equals("");
        assert e.attr("$").equals("");
    }

    @Test
    public void addClass() {
        assert I.xml("<a/>").addClass("add").attr("class").equals("add");
        assert I.xml("<a class='base'/>").addClass("add").attr("class").equals("base add");
        assert I.xml("<a class='base'/>").addClass("base").attr("class").equals("base");
        assert I.xml("<a class='base'/>").addClass("add", "base", "ad").attr("class").equals("base add ad");
    }

    @Test
    public void removeClass() {
        assert I.xml("<a class='base'/>").removeClass("base").attr("class").equals("");
        assert I.xml("<a class='one two'/>").removeClass("one").attr("class").equals("two");
        assert I.xml("<a class='one two'/>").removeClass("on", "two", "one").attr("class").equals("");
    }

    @Test
    public void hasClass() {
        assert I.xml("<a class='base'/>").hasClass("base");
        assert I.xml("<a class='one two'/>").hasClass("one");
        assert !I.xml("<a class='base'/>").hasClass("none");
        assert !I.xml("<a class='base'/>").hasClass("");
    }

    @Test
    public void toggleClass() {
        assert I.xml("<a class='base'/>").toggleClass("base").attr("class").equals("");
        assert I.xml("<a class='one two'/>").toggleClass("one").attr("class").equals("two");
        assert I.xml("<a class='one two'/>").toggleClass("three").attr("class").equals("one two three");
    }

    @Test
    public void child() {
        XML e = I.xml("<Q/>");

        assert e.find("child").size() == 0;
        XML child = e.child("child");
        assert e.find("child").size() == 1;

        assert e.find("child.check").size() == 0;
        child.addClass("check");
        assert e.find("child.check").size() == 1;
    }

    @Test
    public void childNest() {
        XML e = I.xml("<Q/>");

        assert e.find("child item").size() == 0;
        e.child("child", c -> {
            c.child("item");
        });
        assert e.find("child item").size() == 1;
    }

    @Test
    public void childNullNest() {
        XML e = I.xml("<Q/>");

        e.child("child", null);
        assert e.find("child").size() == 1;
    }

    @Test
    public void childWithSameName() {
        XML e = I.xml("<Q><child/></Q>");

        assert e.find("child").size() == 1;
        XML child = e.child("child");
        assert e.find("child").size() == 2;

        assert e.find("child.check").size() == 0;
        child.addClass("check");
        assert e.find("child.check").size() == 1;
    }

    @Test
    public void childWithSameNameRoot() {
        XML e = I.xml("<Q><Q/><P/></Q>");

        assert e.find("Q").size() == 1;
        XML child = e.child("Q");
        assert child.size() == 1;
        assert e.find("Q").size() == 2;

        assert e.find("Q.check").size() == 0;
        child.addClass("check");
        assert e.find("Q.check").size() == 1;
    }

    @Test
    public void childMultiple() {
        XML e = I.xml("<root><Q/><Q/><Q/></root>");

        assert e.find("child").size() == 0;
        XML children = e.find("Q").child("child");
        assert e.find("child").size() == 3;

        children.child("nest");
        assert e.find("nest").size() == 3;
    }

    @Test
    public void childCreateElementNS() {
        Node e = I.xml("<Q/>").child("child").to();
        assert e.getLocalName().equals("child");
        assert e.getNodeName().equals("child");
    }

    @Test
    public void effect() {
        XML e = I.xml("<Q/>");

        assert e.find("P").size() == 0;
        e.effect(x -> x.append("<P/>"));
        assert e.find("P").size() == 1;
    }

    @Test
    public void effectNull() {
        Assertions.assertThrows(NullPointerException.class, () -> I.xml("Q").effect(null));
    }
}