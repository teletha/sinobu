/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import static kiss.Element.*;
import kiss.Element;

import org.junit.Test;

/**
 * @version 2012/02/07 11:15:35
 */
public class ElementManipulationTest {

    @Test
    public void append() throws Exception {
        String xml = "<m><Q><P/></Q><Q><P/></Q></m>";

        Element e = $(xml);
        assert e.find("Q").append("<R/><R/>").find("R").size() == 4;
        assert e.find("Q > R").size() == 4;
        assert e.find("Q > R:first-child").size() == 0;
    }

    @Test
    public void prepend() throws Exception {
        String xml = "<m><Q><P/></Q><Q><P/></Q></m>";

        Element e = $(xml);
        assert e.find("Q").prepend("<R/><R/>").find("R").size() == 4;
        assert e.find("Q > R").size() == 4;
        assert e.find("Q > R:first-child").size() == 2;
    }

    @Test
    public void before() throws Exception {
        String xml = "<m><P/><Q/></m>";

        Element e = $(xml);
        e.find("Q").before("<R/>");

        assert e.find("R:first-child").size() == 0;
        assert e.find("R:last-child").size() == 0;
        assert e.find("R:nth-child(2)").size() == 1;
    }

    @Test
    public void after() throws Exception {
        String xml = "<m><P/><Q/></m>";

        Element e = $(xml);
        e.find("P").after("<R/>");

        assert e.find("R:first-child").size() == 0;
        assert e.find("R:last-child").size() == 0;
        assert e.find("R:nth-child(2)").size() == 1;
    }

    @Test
    public void empty() throws Exception {
        String xml = "<Q><P/><P/></Q>";

        Element e = $(xml);
        e.find("Q").empty();

        assert e.find("P").size() == 0;
    }

    @Test
    public void remove() throws Exception {
        String xml = "<Q><S/><T/><S/></Q>";

        Element e = $(xml);
        assert e.find("Q > *").remove().size() == 3;

        assert e.find("S").size() == 0;
        assert e.find("T").size() == 0;
    }

    @Test
    public void wrap() throws Exception {
        String xml = "<m><Q/><Q/></m>";

        Element e = $(xml);
        e.find("Q").wrap("<P/>");

        assert e.find("P > Q").size() == 2;
        assert e.find("P").size() == 2;
    }

    @Test
    public void wrapAll() throws Exception {
        String xml = "<m><Q/><Q/></m>";

        Element e = $(xml);
        e.find("Q").wrapAll("<P/>");

        assert e.find("P > Q").size() == 2;
        assert e.find("P").size() == 1;
        assert e.find("Q").size() == 2;
    }

    @Test
    public void textGet() throws Exception {
        String xml = "<Q>ss<P>a<i>a</i>a</P><P> b </P><P>c c</P>ss</Q>";

        assert $(xml).find("P").text().equals("aaa b c c");
    }

    @Test
    public void textSet() throws Exception {
        String xml = "<Q><P>aaa</P></Q>";

        Element e = $(xml);
        e.find("P").text("set");

        assert e.find("P:contains(set)").size() == 1;
    }

    @Test
    public void attrGet() throws Exception {
        String xml = "<Q name='value' key='map'/>";

        assert $(xml).attr("name").equals("value");
        assert $(xml).attr("key").equals("map");
    }

    @Test
    public void attrGetNS() throws Exception {
        String xml = "<Q xmlns:P='p' xmlns:z='z' z:name='fail' P:name='value' name='fail'/>";

        Element e = $(xml);

        assert e.attr("P:name").equals("value");
    }

    @Test
    public void attrSet() throws Exception {
        String xml = "<Q name='value' key='map'/>";

        Element e = $(xml);
        e.attr("name", "set");

        assert e.attr("name").equals("set");
        assert e.attr("key").equals("map");
        assert e.attr("name", null).find("Q[name]").size() == 0;
    }

    @Test
    public void attrSetNS() throws Exception {
        String xml = "<Q xmlns:P='p' xmlns:z='z' z:name='fail' P:name='value' name='fail'/>";

        Element e = $(xml);
        e.attr("P:name", "set");

        assert e.attr("P:name").equals("set");
    }

    @Test
    public void attrCreate() throws Exception {
        String xml = "<Q/>";

        Element e = $(xml);
        e.attr("name", "set");

        assert e.attr("name").equals("set");
        assert e.find("Q[name]").size() == 1;
    }

    @Test
    public void attrCreateNs() throws Exception {
        String xml = "<Q/>";

        Element e = $(xml);
        e.attr("P:name", "set");

        assert e.attr("P:name").equals("set");
        assert e.attr("name").equals("");
    }

    @Test
    public void addClass() throws Exception {
        assert $("<a/>").addClass("add").attr("class").equals("add");
        assert $("<a class='base'/>").addClass("add").attr("class").equals("base add");
        assert $("<a class='base'/>").addClass("base").attr("class").equals("base");
        assert $("<a class='base'/>").addClass("add base ad").attr("class").equals("base add ad");
    }

    @Test
    public void removeClass() throws Exception {
        assert $("<a class='base'/>").removeClass("base").attr("class").equals("");
        assert $("<a class='one two'/>").removeClass("one").attr("class").equals("two");
        assert $("<a class='one two'/>").removeClass("on two one").attr("class").equals("");
    }

    @Test
    public void hasClass() throws Exception {
        assert $("<a class='base'/>").hasClass("base");
        assert $("<a class='one two'/>").hasClass("one");
        assert !$("<a class='base'/>").hasClass("none");
        assert !$("<a class='base'/>").hasClass("");
    }

    @Test
    public void toggleClass() throws Exception {
        assert $("<a class='base'/>").toggleClass("base").attr("class").equals("");
        assert $("<a class='one two'/>").toggleClass("one").attr("class").equals("two");
        assert $("<a class='one two'/>").toggleClass("three").attr("class").equals("one two three");
    }

    @Test
    public void child() throws Exception {
        Element e = $("<Q/>");

        assert e.find("child").size() == 0;
        Element child = e.child("child");
        assert e.find("child").size() == 1;

        assert e.find("child.check").size() == 0;
        child.addClass("check");
        assert e.find("child.check").size() == 1;
    }

    @Test
    public void childWithSameName() throws Exception {
        Element e = $("<Q><child/></Q>");

        assert e.find("child").size() == 1;
        Element child = e.child("child");
        assert e.find("child").size() == 2;

        assert e.find("child.check").size() == 0;
        child.addClass("check");
        assert e.find("child.check").size() == 1;
    }
}
