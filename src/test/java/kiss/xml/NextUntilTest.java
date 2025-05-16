/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class NextUntilTest {

    @Test
    public void nextUntilBasic() {
        XML xml1 = I.xml("""
                <root>
                    <n1/>
                    <n2 class='stop'/>
                    <n3/>
                    <n4 class='stop'/>
                </root>
                """);
        XML result1 = xml1.find("n1").nextUntil(".stop");
        assert result1.size() == 0;

        XML xml2 = I.xml("""
                <root>
                    <n1/>
                    <m1/>
                    <m2/>
                    <n2 class='stop'/>
                    <n3/>
                    <n4 class='stop'/>
                </root>
                """);
        XML result2 = xml2.find("n1").nextUntil(".stop");
        assert result2.size() == 2;
        assert result2.first().name().equals("m1");
        assert result2.last().name().equals("m2");
    }

    @Test
    public void nextUntilStopperIsTag() {
        XML xml = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2/>
                    <stopper/>
                    <other/>
                </root>
                """);
        XML result = xml.find("start").nextUntil("stopper");
        assert result.size() == 2;
        assert result.first().name().equals("item1");
        assert result.last().name().equals("item2");
    }

    @Test
    public void nextUntilStopperIsId() {
        XML xml = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2 id='stopHere'/>
                    <item3/>
                </root>
                """);
        XML result = xml.find("start").nextUntil("#stopHere");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void nextUntilStopperIsAttribute() {
        XML xml = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2 data-stop='yes'/>
                    <item3/>
                </root>
                """);
        XML result = xml.find("start").nextUntil("[data-stop=yes]");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void nextUntilStopperIsCombinedSelector() {
        XML xml = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2 class='end'/>
                    <stopper class='end' id='final' data-marker='stop'/>
                    <item3/>
                </root>
                """);
        XML result = xml.find("start").nextUntil(".end#final[data-marker=stop]");
        assert result.size() == 2;
        assert result.first().name().equals("item1");
        assert result.last().name().equals("item2");
    }

    @Test
    public void nextUntilWithTextAndCommentNodes() {
        XML xml = I.xml("""
                <root>
                    <n1/>text<m1/><!-- comment -->more text<m2/>final text<n2 class='stop'/>
                </root>
                """);
        XML result = xml.find("n1").nextUntil(".stop");
        assert result.size() == 2;
        assert result.first().name().equals("m1");
        assert result.last().name().equals("m2");
    }

    @Test
    public void nextUntilNoStopperFound() {
        XML xml = I.xml("""
                <root>
                    <n1/>
                    <m1/>
                    text
                    <m2/>
                </root>
                """);
        XML result = xml.find("n1").nextUntil(".nonexistent");
        assert result.size() == 2;
        assert result.first().name().equals("m1");
        assert result.last().name().equals("m2");
    }

    @Test
    public void nextUntilStopperIsOneOfMultipleCssSelectors() {
        XML xml1 = I.xml("""
                <root>
                    <n1/>
                    <n2/>
                    <n3 class='stop'/>
                </root>
                """);
        XML result1 = xml1.find("n1").nextUntil(".end, .stop, #finish");
        assert result1.size() == 1;
        assert result1.first().name().equals("n2");

        XML xml2 = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2 id='finish'/>
                    <item3 class='stop'/>
                </root>
                """);
        XML result2 = xml2.find("start").nextUntil(".stop, #finish");
        assert result2.size() == 1;
        assert result2.first().name().equals("item1");

        XML xml3 = I.xml("""
                <root>
                    <start/>
                    <item1/>
                    <item2 class='stop'/>
                    <item3 id='finish'/>
                </root>
                """);
        XML result3 = xml3.find("start").nextUntil(".stop, #finish");
        assert result3.size() == 1;
        assert result3.first().name().equals("item1");
    }

    @Test
    public void nextUntilOnEmptySet() {
        XML xml = I.xml("<root/>");
        XML result = xml.find("nonexistent").nextUntil(".stop");
        assert result.size() == 0;
    }

    @Test
    public void nextUntilMultipleStartNodesSameParent() {
        XML xml = I.xml("""
                <root>
                    <p1 class='start'/>
                    <p2/>
                    <p3 class='start'/>
                    <p4/>
                    <stop class='stop'/>
                    <p5/>
                </root>
                """);

        assert xml.find(".start").nextUntil(".stop").size() == 3;
    }

    @Test
    public void nextUntilMultipleStartNodesDifferentParents() {
        XML xml = I.xml("""
                <doc>
                    <parent1>
                        <s1 class='start'/><s2/><s3 class='stop'/>
                    </parent1>
                    <parent2>
                        <s1 class='start'/><s2/><s3/><s4 class='stop'/>
                    </parent2>
                </doc>
                """);
        XML result = xml.find(".start").nextUntil(".stop");
        assert result.size() == 3;
        List<String> names = I.signal(result).map(XML::name).toList();
        long s2Count = names.stream().filter(name -> name.equals("s2")).count();
        long s3Count = names.stream().filter(name -> name.equals("s3")).count();
        assert s2Count == 2;
        assert s3Count == 1;
    }

    @Test
    public void nextUntilNoNextSiblings() {
        XML xml = I.xml("""
                <root>
                    <s1 class='stop'/>
                    <start/>
                </root>
                """);
        XML result = xml.find("start").nextUntil(".stop");
        assert result.size() == 0;
    }

    @Test
    public void nextUntilStopperIsLastSibling() {
        XML xml = I.xml("""
                <root>
                    <start/>
                    <s2/>
                    <s1 class='stop'/>
                </root>
                """);
        XML result = xml.find("start").nextUntil(".stop");
        assert result.size() == 1;
        assert result.first().name().equals("s2");
    }

    @Test
    public void nextUntilStopperIsImmediateNext() {
        XML xml = I.xml("""
                <root>
                    <start/>
                    <s2 class='stop'/>
                    <s1/>
                </root>
                """);
        XML result = xml.find("start").nextUntil(".stop");
        assert result.size() == 0;
    }

    @Test
    public void nextUntilWithXMLStopper() {
        XML xml = I.xml("""
                <root>
                    <start/>
                    <s3/>
                    <s4/>
                    <s2 id="stopperId"/>
                    <s1/>
                </root>
                """);
        XML result = xml.find("start").nextUntil("#stopperId");
        assert result.size() == 2;
        assert result.first().name().equals("s3");
        assert result.last().name().equals("s4");
    }

    @Test
    public void nextUntilWithXMLStopperNotASibling() {
        XML xml = I.xml("""
                <root>
                    <start/>
                    <s1/>
                    <s2/>
                    <elsewhere><stopper id="notSiblingStopper"/></elsewhere>
                </root>
                """);
        assert xml.find("start").nextUntil("#notSiblingStopper").size() == 3;
    }
}