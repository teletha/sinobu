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

import java.util.ArrayList;
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
                e("ol", $$(list("A", "B"), item -> {
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
     * @version 2017/02/06 14:01:17
     */
    public static abstract class HTML extends Structure<HTMLBuilder, ElementNode> {

        /**
         * <p>
         * Declare node with name.
         * </p>
         * 
         * @param name A node name.
         */
        protected final void e(String name, Declarable... declarables) {
            ElementNode e = new ElementNode(name);

            $(e);

            for (Declarable declarable : declarables) {
                if (declarable != null) {
                    $(declarable);
                }
            }
        }

        /**
         * <p>
         * Declare node attribute with name.
         * </p>
         * 
         * @param name An attribute name.
         * @return
         */
        protected final Declarable attr(String name) {
            return attr(name, null);
        }

        /**
         * <p>
         * Declare node attribute with name.
         * </p>
         * 
         * @param name An attribute name.
         * @return
         */
        protected final Declarable attr(String name, String value) {
            return () -> {
                if (name != null && !name.isEmpty()) {
                    $(new AttributeNode(name, value));
                }
            };
        }

        protected void text(String text) {
            $(new TextNode(text));
        }
    }

    /**
     * @version 2017/02/06 16:02:42
     */
    private static class ElementNode implements Declarable<ElementNode> {

        private String name;

        private List<AttributeNode> attrs = new ArrayList();

        private List<ElementNode> children = new ArrayList();

        /**
         * @param name
         */
        private ElementNode(String name) {
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void declare() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void declare(ElementNode parent) {
            parent.children.add(this);
        }
    }

    /**
     * @version 2017/02/06 15:52:47
     */
    private static class TextNode extends ElementNode {

        /**
         * @param text
         */
        private TextNode(String text) {
            super(text);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void declare() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void declare(ElementNode parent) {
            parent.children.add(this);
        }
    }

    /**
     * @version 2017/02/06 16:12:23
     */
    private static class AttributeNode implements Declarable<ElementNode> {

        private final String name;

        private final String value;

        /**
         * @param name
         * @param value
         */
        private AttributeNode(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void declare() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void declare(ElementNode parent) {
            parent.attrs.add(this);
        }
    }

    /**
     * @version 2017/02/06 14:10:16
     */
    private static class HTMLBuilder extends StructureBuilder<ElementNode> {

        private final StringBuilder builder = new StringBuilder();

        /**
         * {@inheritDoc}
         */
        @Override
        public StringBuilder enterNode(StringBuilder parent, String name) {
            builder.append("<" + name + ">");

            return builder;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void leaveNode(String name) {
            int length = builder.length() - 1;

            if (builder.charAt(length) == '>' && builder.charAt(length - 1) != '/') {
                builder.deleteCharAt(length);
                builder.append("/>");
            } else {
                builder.append("</" + name + ">");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return builder.toString();
        }
    }
}
