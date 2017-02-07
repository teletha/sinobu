/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lang;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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
        assert html.root.toString().equals("<html/>");
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
        assert html.root.toString().equals("<html><body/></html>");
    }

    @Test
    public void elements() {
        HTML html = new HTML() {
            {
                e("div");
                e("div");
            }
        };
        assert html.root.toString().equals("<div/><div/>");
    }

    @Test
    public void attribute() {
        HTML html = new HTML() {
            {
                e("div", attr("id", "test"));
            }
        };
        assert html.root.toString().equals("<div id='test'/>");
    }

    @Test
    public void attributeNullName() {
        HTML html = new HTML() {
            {
                e("div", attr(null, "ok"));
            }
        };
        assert html.root.toString().equals("<div/>");
    }

    @Test
    public void attributeEmptyName() {
        HTML html = new HTML() {
            {
                e("div", attr("", "ok"));
            }
        };
        assert html.root.toString().equals("<div/>");
    }

    @Test
    public void attributeNullValue() {
        HTML html = new HTML() {
            {
                e("input", attr("checked", null));
            }
        };
        assert html.root.toString().equals("<input checked/>");
    }

    @Test
    public void attributeEmptyValue() {
        HTML html = new HTML() {
            {
                e("div", attr("id", ""));
            }
        };
        assert html.root.toString().equals("<div id=''/>");
    }

    @Test
    public void attributeWithoutValue() {
        HTML html = new HTML() {
            {
                e("input", attr("checked"));
            }
        };
        assert html.root.toString().equals("<input checked/>");
    }

    @Test
    public void contentsIterable() {
        HTML html = new HTML() {
            {
                e("ol", $$(list("A", "B"), item -> {
                    e("li", () -> text(item));
                }));
            }
        };
        assert html.root.toString().equals("<ol><li>A</li><li>B</li></ol>");
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
    static class Id implements Declarable {

        private final String id;

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
        public void declare() {
        }
    }
}
