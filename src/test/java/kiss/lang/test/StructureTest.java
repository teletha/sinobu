/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lang.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import kiss.lang.Declarable;
import kiss.lang.HTML;
import kiss.lang.HTML.ElementNode;

/**
 * @version 2017/02/06 14:00:16
 */
public class StructureTest {

    @Test
    public void element() {
        HTML html = new HTML() {
            {
                e("html");
            }
        };
        assert html.toString().equals("<html/>");
    }

    @Test
    public void elementNest() {
        HTML html = new HTML() {
            {
                e("html", () -> {
                    e("body");
                });
            }
        };
        assert html.toString().equals("<html><body/></html>");
    }

    @Test
    public void elements() {
        HTML html = new HTML() {
            {
                e("div");
                e("div");
            }
        };
        assert html.toString().equals("<div/><div/>");
    }

    @Test
    public void attribute() {
        HTML html = new HTML() {
            {
                e("div", attr("id", "test"));
            }
        };
        assert html.toString().equals("<div id='test'/>");
    }

    @Test
    public void attributeNullName() {
        HTML html = new HTML() {
            {
                e("div", attr(null, "ok"));
            }
        };
        assert html.toString().equals("<div/>");
    }

    @Test
    public void attributeEmptyName() {
        HTML html = new HTML() {
            {
                e("div", attr("", "ok"));
            }
        };
        assert html.toString().equals("<div/>");
    }

    @Test
    public void attributeNullValue() {
        HTML html = new HTML() {
            {
                e("input", attr("checked", null));
            }
        };
        assert html.toString().equals("<input checked/>");
    }

    @Test
    public void attributeEmptyValue() {
        HTML html = new HTML() {
            {
                e("div", attr("id", ""));
            }
        };
        assert html.toString().equals("<div id=''/>");
    }

    @Test
    public void attributeWithoutValue() {
        HTML html = new HTML() {
            {
                e("input", attr("checked"));
            }
        };
        assert html.toString().equals("<input checked/>");
    }

    @Test
    public void contentsIterable() {
        HTML html = new HTML() {
            {
                e("ol", $(list("A", "B"), item -> {
                    e("li", () -> text(item));
                }));
            }
        };
        assert html.toString().equals("<ol><li>A</li><li>B</li></ol>");
    }

    /**
     * <p>
     * Helper method to create name list.
     * </p>
     * 
     * @param names
     * @return
     */
    private <T> List<T> list(T... names) {
        return Arrays.asList(names);
    }

    /**
     * @version 2017/02/07 11:43:11
     */
    public static class Id implements Declarable<ElementNode> {

        public final String id;

        /**
         * @param id
         */
        private Id(String id) {
            this.id = id;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void declare(ElementNode context) {
        }
    }
}
