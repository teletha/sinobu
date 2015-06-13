/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import org.junit.Test;

/**
 * @version 2014/07/31 23:05:55
 */
public class XMLManipulationTest {

    @Test
    public void append() throws Exception {
        String xml = "<m><Q><P/></Q><Q><P/></Q></m>";

        XML e = I.xml(xml);
        assert e.find("Q").append("<R/><R/>").find("R").size() == 4;
        assert e.find("Q > R").size() == 4;
        assert e.find("Q > R:first-child").size() == 0;
    }

    @Test
    public void appendText() throws Exception {
        String xml = "<m><Q><P/></Q><Q><P/></Q></m>";

        XML e = I.xml(xml);
        assert e.find("Q").append("text").find("P").size() == 2;
        assert e.find("Q").text().equals("texttext");
    }

    @Test
    public void appendXMLLikeText() throws Exception {
        String xml = "<m><Q><P/></Q><Q><P/></Q></m>";

        XML e = I.xml(xml);
        assert e.find("Q").append("<").find("P").size() == 2;
        assert e.find("Q").text().equals("<<");
    }

    @Test
    public void prepend() throws Exception {
        String xml = "<m><Q><P/></Q><Q><P/></Q></m>";

        XML e = I.xml(xml);
        assert e.find("Q").prepend("<R/><R/>").find("R").size() == 4;
        assert e.find("Q > R").size() == 4;
        assert e.find("Q > R:first-child").size() == 2;
    }

    @Test
    public void prependText() throws Exception {
        String xml = "<m><Q><P/></Q><Q><P/></Q></m>";

        XML e = I.xml(xml);
        assert e.find("Q").prepend("text").find("P").size() == 2;
        assert e.find("Q").text().equals("texttext");
    }

    @Test
    public void before() throws Exception {
        String xml = "<m><P/><Q/></m>";

        XML e = I.xml(xml);
        e.find("Q").before("<R/>");

        assert e.find("R:first-child").size() == 0;
        assert e.find("R:last-child").size() == 0;
        assert e.find("R:nth-child(2)").size() == 1;
    }

    @Test
    public void beforeText() throws Exception {
        String xml = "<m><P/><Q/></m>";

        XML e = I.xml(xml);
        e.find("Q").before("text");

        assert e.find("P,Q").size() == 2;
        assert e.text().equals("text");
    }

    @Test
    public void after() throws Exception {
        String xml = "<m><P/><Q/></m>";

        XML e = I.xml(xml);
        e.find("P").after("<R/>");

        assert e.find("R:first-child").size() == 0;
        assert e.find("R:last-child").size() == 0;
        assert e.find("R:nth-child(2)").size() == 1;
    }

    @Test
    public void afterText() throws Exception {
        String xml = "<m><P/><Q/></m>";

        XML e = I.xml(xml);
        e.find("P").before("text");

        assert e.find("P,Q").size() == 2;
        assert e.text().equals("text");
    }

    @Test
    public void empty() throws Exception {
        String xml = "<Q><P/><P/></Q>";

        XML e = I.xml(xml);
        e.empty();

        assert e.find("P").size() == 0;
    }

    @Test
    public void remove() throws Exception {
        String xml = "<Q><S/><T/><S/></Q>";

        XML e = I.xml(xml);
        assert e.find("*").remove().size() == 3;

        assert e.find("S").size() == 0;
        assert e.find("T").size() == 0;
    }

    @Test
    public void wrap() throws Exception {
        String xml = "<m><Q/><Q/></m>";

        XML e = I.xml(xml);
        e.find("Q").wrap("<P/>");

        assert e.find("P > Q").size() == 2;
        assert e.find("P").size() == 2;
    }

    @Test
    public void wrapAll() throws Exception {
        String xml = "<m><Q/><Q/></m>";

        XML e = I.xml(xml);
        e.find("Q").wrapAll("<P/>");

        assert e.find("P > Q").size() == 2;
        assert e.find("P").size() == 1;
        assert e.find("Q").size() == 2;
    }

    @Test
    public void textGet() throws Exception {
        String xml = "<Q>ss<P>a<i>a</i>a</P><P> b </P><P>c c</P>ss</Q>";

        assert I.xml(xml).find("P").text().equals("aaa b c c");
    }

    @Test
    public void textSet() throws Exception {
        String xml = "<Q><P>aaa</P></Q>";

        XML e = I.xml(xml);
        e.find("P").text("set");

        assert e.find("P:contains(set)").size() == 1;
    }

    @Test
    public void attrGet() throws Exception {
        String xml = "<Q name='value' key='map'/>";

        assert I.xml(xml).attr("name").equals("value");
        assert I.xml(xml).attr("key").equals("map");
    }

    @Test
    public void attrGetNS() throws Exception {
        String xml = "<Q xmlns:P='p' xmlns:z='z' z:name='fail' P:name='value' name='fail'/>";

        XML e = I.xml(xml);

        assert e.attr("P:name").equals("value");
    }

    @Test
    public void attrSet() throws Exception {
        String xml = "<Q name='value' key='map'/>";

        XML e = I.xml(xml);
        e.attr("name", "set");

        assert e.attr("name").equals("set");
        assert e.attr("key").equals("map");
        assert e.attr("name", null).find("Q[name]").size() == 0;
    }

    @Test
    public void attrSetNS() throws Exception {
        String xml = "<Q xmlns:P='p' xmlns:z='z' z:name='fail' P:name='value' name='fail'/>";

        XML e = I.xml(xml);
        e.attr("P:name", "set");

        assert e.attr("P:name").equals("set");
    }

    @Test
    public void attrCreate() throws Exception {
        String xml = "<Q/>";

        XML e = I.xml(xml);

        assert e.attr("name").equals("");
        e.attr("name", "set");
        assert e.attr("name").equals("set");
    }

    @Test
    public void attrCreateNs() throws Exception {
        String xml = "<Q/>";

        XML e = I.xml(xml);
        e.attr("P:name", "set");

        assert e.attr("P:name").equals("set");
        assert e.attr("name").equals("");
    }

    @Test
    public void attrNull() throws Exception {
        String xml = "<Q/>";

        XML e = I.xml(xml);
        e.attr(null, "one"); // no error
        e.attr("", "two"); // no error

        assert e.attr("").equals("");
    }

    @Test
    public void attrInvalidName() throws Exception {
        String xml = "<Q/>";

        XML e = I.xml(xml);
        e.attr("0", "one"); // no error
        e.attr("$", "one"); // no error

        assert e.attr("0").equals("");
        assert e.attr("$").equals("");
    }

    @Test
    public void addClass() throws Exception {
        assert I.xml("<a/>").addClass("add").attr("class").equals("add");
        assert I.xml("<a class='base'/>").addClass("add").attr("class").equals("base add");
        assert I.xml("<a class='base'/>").addClass("base").attr("class").equals("base");
        assert I.xml("<a class='base'/>").addClass("add base ad").attr("class").equals("base add ad");
    }

    @Test
    public void removeClass() throws Exception {
        assert I.xml("<a class='base'/>").removeClass("base").attr("class").equals("");
        assert I.xml("<a class='one two'/>").removeClass("one").attr("class").equals("two");
        assert I.xml("<a class='one two'/>").removeClass("on two one").attr("class").equals("");
    }

    @Test
    public void hasClass() throws Exception {
        assert I.xml("<a class='base'/>").hasClass("base");
        assert I.xml("<a class='one two'/>").hasClass("one");
        assert !I.xml("<a class='base'/>").hasClass("none");
        assert !I.xml("<a class='base'/>").hasClass("");
    }

    @Test
    public void toggleClass() throws Exception {
        assert I.xml("<a class='base'/>").toggleClass("base").attr("class").equals("");
        assert I.xml("<a class='one two'/>").toggleClass("one").attr("class").equals("two");
        assert I.xml("<a class='one two'/>").toggleClass("three").attr("class").equals("one two three");
    }

    @Test
    public void child() throws Exception {
        XML e = I.xml("<Q/>");

        assert e.find("child").size() == 0;
        XML child = e.child("child");
        assert e.find("child").size() == 1;

        assert e.find("child.check").size() == 0;
        child.addClass("check");
        assert e.find("child.check").size() == 1;
    }

    @Test
    public void childWithSameName() throws Exception {
        XML e = I.xml("<Q><child/></Q>");

        assert e.find("child").size() == 1;
        XML child = e.child("child");
        assert e.find("child").size() == 2;

        assert e.find("child.check").size() == 0;
        child.addClass("check");
        assert e.find("child.check").size() == 1;
    }

    @Test
    public void childWithSameNameRoot() throws Exception {
        XML e = I.xml("<Q><Q/><P/></Q>");

        assert e.find("Q").size() == 1;
        XML child = e.child("Q");
        assert child.size() == 1;
        assert e.find("Q").size() == 2;

        assert e.find("Q.check").size() == 0;
        child.addClass("check");
        assert e.find("Q.check").size() == 1;
    }
}
