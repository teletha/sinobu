/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml.traverse;

import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.XML;

public class ParentUntilTest {

    @Test
    public void parentUntilBasic() {
        XML root = I.xml("""
                <div class='ancestor'>
                    <p><span id='start'>text</span></p>
                </div>
                """);

        XML result = root.find("#start").parentUntil(".ancestor");
        assert result.size() == 1;
        assert result.first().name().equals("p");

        root = I.xml("""
                <div class='ancestor'>
                    <p id='start'><span>text</span></p>
                </div>
                """);

        result = root.find("#start").parentUntil(".ancestor");
        assert result.size() == 0;
    }

    @Test
    public void parentUntilStopperIsTag() {
        XML root = I.xml("""
                <stopperTag>
                    <level1>
                        <level2>
                            <startNode/>
                        </level2>
                    </level1>
                </stopperTag>
                """);
        XML result = root.find("startNode").parentUntil("stopperTag");
        assert result.size() == 2;
        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("level2");
        assert list.get(1).name().equals("level1");
    }

    @Test
    public void parentUntilStopperIsId() {
        XML root = I.xml("""
                <div id='stopHere'>
                    <level1>
                        <level2>
                            <startNode/>
                        </level2>
                    </level1>
                </div>
                """);
        XML result = root.find("startNode").parentUntil("#stopHere");
        assert result.size() == 2;
        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("level2");
        assert list.get(1).name().equals("level1");
    }

    @Test
    public void parentUntilStopperIsAttribute() {
        XML root = I.xml("""
                <div data-stop='true'>
                    <level1>
                        <level2>
                            <startNode/>
                        </level2>
                    </level1>
                </div>
                """);
        XML result = root.find("startNode").parentUntil("[data-stop=true]");
        assert result.size() == 2;
        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("level2");
        assert list.get(1).name().equals("level1");
    }

    @Test
    public void parentUntilStopperIsCombinedSelector() {
        XML root = I.xml("""
                <div class='ancestor final' id='top' data-role='stopper'>
                    <level1>
                        <level2>
                            <startNode/>
                        </level2>
                    </level1>
                </div>
                """);
        XML result = root.find("startNode").parentUntil("div.ancestor#top[data-role=stopper]");
        assert result.size() == 2;
        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("level2");
        assert list.get(1).name().equals("level1");
    }

    @Test
    public void parentUntilThroughMultipleLevels() {
        XML root = I.xml("""
                <div class='grandparent'>
                    <div class='parent'>
                        <p class='child'><span id='start'/></p>
                    </div>
                </div>
                """);

        XML result = root.find("#start").parentUntil(".grandparent");
        assert result.size() == 2;

        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("p");
        assert list.get(1).name().equals("div");
        assert list.get(1).attr("class").equals("parent");
    }

    @Test
    public void parentUntilNoStopper() {
        XML root = I.xml("""
                <div>
                    <p>
                        <span>
                            <b id='start'/>
                        </span>
                    </p>
                </div>
                """);

        XML result = root.find("#start").parentUntil(".nonexistent");
        assert result.size() == 4;

        List<XML> list = I.signal(result).toList();
        assert list.get(0).name().equals("span");
        assert list.get(1).name().equals("p");
        assert list.get(2).name().equals("div");
    }

    @Test
    public void parentUntilStopperIsOneOfMultipleCssSelectors() {
        XML root = I.xml("""
                <div id='anc1'>
                  <section class='anc2'>
                    <article data-anc='3'>
                      <p>
                        <span id='start'/>
                      </p>
                    </article>
                  </section>
                </div>
                """);

        // Stop at article[data-anc='3']
        XML result1 = root.find("#start").parentUntil("article[data-anc='3'], .anc2, #anc1");
        assert result1.size() == 1;
        assert result1.first().name().equals("p");

        // Stop at section.anc2
        XML result2 = root.find("#start").parentUntil(".anc2, #anc1");
        assert result2.size() == 2;
        List<XML> list2 = I.signal(result2).toList();
        assert list2.get(0).name().equals("p");
        assert list2.get(1).name().equals("article");

        // Stop at div#anc1
        XML result3 = root.find("#start").parentUntil("#anc1");
        assert result3.size() == 3;
        List<XML> list3 = I.signal(result3).toList();
        assert list3.get(0).name().equals("p");
        assert list3.get(1).name().equals("article");
        assert list3.get(2).name().equals("section");
    }

    @Test
    public void parentUntilMultipleStartNodes() {
        XML root = I.xml("""
                <div class="stopper">
                    <section>
                        <p><span id="s1" class="start"/></p>
                    </section>
                    <section>
                        <figure><b id="s2" class="start"/> </figure>
                    </section>
                </div>
                """);
        XML result = root.find(".start").parentUntil(".stopper");
        assert result.size() == 4;

        List<String> names = I.signal(result).map(XML::name).toList();
        assert names.contains("p");
        assert names.contains("section");
        assert names.contains("figure");

        assert names.stream().filter(name -> name.equals("p")).count() == 1;
        assert names.stream().filter(name -> name.equals("section")).count() == 2;
        assert names.stream().filter(name -> name.equals("figure")).count() == 1;
    }

    @Test
    public void parentUntilOnEmptySet() {
        XML root = I.xml("<root/>");
        XML result = root.find("nonexistent").parentUntil(".stop");
        assert result.size() == 0;
    }
}