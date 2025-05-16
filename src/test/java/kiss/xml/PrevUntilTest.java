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

public class PrevUntilTest {

    @Test
    public void prevUntilBasic() {
        XML xml1 = I.xml("""
                <root>
                    <n1 class='stop'/><n2/><n3/><n4/>
                </root>
                """);
        XML result1 = xml1.find("n4").prevUntil(".stop");
        assert result1.size() == 2;
        assert result1.first().name().equals("n3");
        assert result1.last().name().equals("n2");

        XML xml2 = I.xml("""
                <root>
                    <n1 class='stop'/><n2/><n3/>
                </root>
                """);
        XML result2 = xml2.find("n2").prevUntil(".stop");
        assert result2.size() == 0;
    }

    @Test
    public void prevUntilStopperIsTag() {
        XML xml = I.xml("""
                <root>
                    <other/>
                    <stopper/>
                    <item2/>
                    <item1/>
                    <start/>
                </root>
                """);
        XML result = xml.find("start").prevUntil("stopper");
        assert result.size() == 2;
        assert result.first().name().equals("item1");
        assert result.last().name().equals("item2");
    }

    @Test
    public void prevUntilWithTextAndCommentNodes() {
        XML xml = I.xml("""
                <root>
                    <n1 class='stop'/>text<m1/><!-- comment -->more text<m2/>final text<n3/>
                </root>
                """);
        XML result = xml.find("n3").prevUntil(".stop");
        assert result.size() == 2;
        assert result.first().name().equals("m2");
        assert result.last().name().equals("m1");
    }

    @Test
    public void prevUntilStopperIsId() {
        XML xml = I.xml("""
                <root>
                    <item3/>
                    <item2 id='stopHere'/>
                    <item1/>
                    <start/>
                </root>
                """);
        XML result = xml.find("start").prevUntil("#stopHere");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void prevUntilStopperIsAttribute() {
        XML xml = I.xml("""
                <root>
                    <item3/>
                    <item2 data-stop='yes'/>
                    <item1/>
                    <start/>
                </root>
                """);
        XML result = xml.find("start").prevUntil("[data-stop=yes]");
        assert result.size() == 1;
        assert result.first().name().equals("item1");
    }

    @Test
    public void prevUntilStopperIsCombinedSelector() {
        XML xml = I.xml("""
                <root>
                    <item/>
                    <stopper class='end' id='final' data-marker='stop'/>
                    <item class='end'/>
                    <item id='final'/>
                    <item data-marker='stop'/>
                    <start/>
                </root>
                """);
        XML start = xml.find("start");
        assert start.prevUntil(".end#final[data-marker=stop]").size() == 3;
        assert start.prevUntil("[data-marker=stop].end#final").size() == 3;
    }

    @Test
    public void prevUntilNoStopperFound() {
        XML xml = I.xml("""
                <root>
                    <m1/><m2/>text<m3/><n4/>
                </root>
                """);
        XML result = xml.find("n4").prevUntil(".nonexistent");
        assert result.size() == 3;
    }

    @Test
    public void prevUntilOnEmptySet() {
        XML xml = I.xml("<root/>");
        XML result = xml.find("nonexistent").prevUntil(".stop");
        assert result.size() == 0;
    }

    @Test
    public void prevUntilStopperIsOneOfMultipleCssSelectors() {
        XML xml1 = I.xml("""
                <root>
                    <n3 class='stop'/>
                    <n2/>
                    <n1/>
                </root>
                """);
        XML result1 = xml1.find("n1").prevUntil(".end, .stop, #finish");
        assert result1.size() == 1;
        assert result1.first().name().equals("n2");

        XML xml2 = I.xml("""
                <root>
                    <item4/>
                    <item3 class='stop'/>
                    <item2 id='finish'/>
                    <item1/>
                    <start/>
                </root>
                """);
        XML result2 = xml2.find("start").prevUntil(".stop, #finish");
        assert result2.size() == 1;
        assert result2.first().name().equals("item1");

        XML xml3 = I.xml("""
                <root>
                    <item4/>
                    <item3 id='finish'/>
                    <item2 class='stop'/>
                    <item1/>
                    <start/>
                </root>
                """);
        XML result3 = xml3.find("start").prevUntil(".stop, #finish");
        assert result3.size() == 1;
        assert result3.first().name().equals("item1");
    }

    @Test
    public void prevUntilMultipleStartNodesSameParent() {
        XML xml = I.xml("""
                <root>
                    <stop class='stop'/>
                    <p1/>
                    <p2 class='start'/>
                    <p3/>
                    <p4 class='start'/>
                    <p5/>
                </root>
                """);

        assert xml.find(".start").prevUntil(".stop").size() == 3;
    }

    @Test
    public void prevUntilMultipleStartNodesDifferentParents() {
        XML xml = I.xml("""
                <doc>
                    <parent1>
                        <s3 class='stop'/><s2/><s1 class='start'/>
                    </parent1>
                    <parent2>
                        <s4 class='stop'/><s3/><s2/><s1 class='start'/>
                    </parent2>
                </doc>
                """);
        XML result = xml.find(".start").prevUntil(".stop");
        assert result.size() == 3;
        List<String> names = I.signal(result).map(XML::name).toList();
        long s2Count = names.stream().filter(name -> name.equals("s2")).count();
        long s3Count = names.stream().filter(name -> name.equals("s3")).count();
        assert s2Count == 2;
        assert s3Count == 1;
    }

    @Test
    public void prevUntilNoPreviousSiblings() {
        XML xml = I.xml("""
                <root>
                    <start/>
                    <s1 class='stop'/>
                </root>
                """);
        XML result = xml.find("start").prevUntil(".stop");
        assert result.size() == 0;
    }

    @Test
    public void prevUntilStopperIsFirstSibling() {
        XML xml = I.xml("""
                <root>
                    <s1 class='stop'/>
                    <s2/>
                    <start/>
                </root>
                """);
        XML result = xml.find("start").prevUntil(".stop");
        assert result.size() == 1;
        assert result.first().name().equals("s2");
    }

    @Test
    public void prevUntilStopperIsImmediatePrevious() {
        XML xml = I.xml("""
                <root>
                    <s1/>
                    <s2 class='stop'/>
                    <start/>
                </root>
                """);
        XML result = xml.find("start").prevUntil(".stop");
        assert result.size() == 0;
    }

    @Test
    public void prevUntilWithXMLStopper() {
        XML xml = I.xml("""
                <root>
                    <s1/>
                    <s2 id="stopperId"/>
                    <s3/>
                    <s4/>
                    <start/>
                </root>
                """);
        XML result = xml.find("start").prevUntil("#stopperId");
        assert result.size() == 2;
        assert result.first().name().equals("s4");
        assert result.last().name().equals("s3");
    }

    @Test
    public void prevUntilWithXMLStopperNotASibling() {
        XML xml = I.xml("""
                <root>
                    <elsewhere><stopper id="notSiblingStopper"/></elsewhere>
                    <s1/>
                    <s2/>
                    <start/>
                </root>
                """);
        XML result = xml.find("start").prevUntil("#notSiblingStopper");
        assert result.size() == 3;
    }
}