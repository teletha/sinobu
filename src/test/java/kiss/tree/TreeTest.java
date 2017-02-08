/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import kiss.Declarable;
import kiss.Tree;
import kiss.tree.TreeTest.HTML.ElementNode;

/**
 * @version 2017/02/06 14:00:16
 */
public class TreeTest {

    @Test
    public void element() {
        HTML html = new HTML() {
            {
                $("html");
            }
        };
        assert html.toString().equals("<html/>");
    }

    @Test
    public void elementNest() {
        HTML html = new HTML() {
            {
                $("html", () -> {
                    $("body");
                });
            }
        };
        assert html.toString().equals("<html><body/></html>");
    }

    @Test
    public void elements() {
        HTML html = new HTML() {
            {
                $("div");
                $("div");
            }
        };
        assert html.toString().equals("<div/><div/>");
    }

    @Test
    public void attribute() {
        HTML html = new HTML() {
            {
                $("div", attr("id", "test"));
            }
        };
        assert html.toString().equals("<div id='test'/>");
    }

    @Test
    public void attributeNullName() {
        HTML html = new HTML() {
            {
                $("div", attr(null, "ok"));
            }
        };
        assert html.toString().equals("<div/>");
    }

    @Test
    public void attributeEmptyName() {
        HTML html = new HTML() {
            {
                $("div", attr("", "ok"));
            }
        };
        assert html.toString().equals("<div/>");
    }

    @Test
    public void attributeNullValue() {
        HTML html = new HTML() {
            {
                $("input", attr("checked", null));
            }
        };
        assert html.toString().equals("<input checked/>");
    }

    @Test
    public void attributeEmptyValue() {
        HTML html = new HTML() {
            {
                $("div", attr("id", ""));
            }
        };
        assert html.toString().equals("<div id=''/>");
    }

    @Test
    public void attributeWithoutValue() {
        HTML html = new HTML() {
            {
                $("input", attr("checked"));
            }
        };
        assert html.toString().equals("<input checked/>");
    }

    @Test
    public void contentsIterable() {
        HTML html = new HTML() {
            {
                $("ol", $(list("A", "B"), item -> {
                    $("li", () -> text(item));
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

    @Test
    public void outerDefinedDeclarable() {
        HTML html = new HTML() {
            {
                $("div", new Id("ok"));
            }
        };
        assert html.toString().equals("<div id='ok'/>");
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

    /**
     * @version 2017/02/08 9:40:08
     */
    public static abstract class HTML extends Tree<ElementNode> {

        /**
         * 
         */
        public HTML() {
            super(ElementNode::new, (context, declarable) -> {
                if (declarable instanceof Id) {
                    context.attrs.add(new AttributeNode("id", ((Id) declarable).id));
                } else {
                    declarable.declare(context);
                }
            });
        }

        /**
         * <p>
         * Declare node attribute with name.
         * </p>
         * 
         * @param name An attribute name.
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
         */
        protected final Declarable attr(String name, String value) {
            return context -> {
                if (name != null && !name.isEmpty()) {
                    $(new AttributeNode(name, value));
                }
            };
        }

        /**
         * <p>
         * Declare text node.
         * </p>
         * 
         * @param text A text.
         */
        protected final void text(String text) {
            $(new TextNode(text));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            for (ElementNode node : root()) {
                builder.append(node);
            }
            return builder.toString();
        }

        /**
         * @version 2017/02/06 16:02:42
         */
        public static class ElementNode implements Declarable<ElementNode> {

            protected String name;

            private List<AttributeNode> attrs = new ArrayList();

            private List children = new ArrayList();

            /**
             * @param name
             */
            private ElementNode(String name, int id, Object context) {
                this.name = name;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void declare(ElementNode context) {
                context.children.add(this);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();

                if (name.isEmpty()) {
                    for (Object child : children) {
                        builder.append(child);
                    }
                    return builder.toString();
                }

                builder.append("<").append(name);

                for (AttributeNode attr : attrs) {
                    builder.append(" ").append(attr);
                }

                if (children.isEmpty()) {
                    builder.append("/>");
                } else {
                    builder.append(">");
                    for (Object child : children) {
                        builder.append(child);
                    }
                    builder.append("</").append(name).append(">");
                }
                return builder.toString();
            }
        }

        /**
         * @version 2017/02/06 15:52:47
         */
        private static class TextNode implements Declarable<ElementNode> {

            private final String text;

            /**
             * @param text
             */
            private TextNode(String text) {
                this.text = text;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void declare(ElementNode context) {
                context.children.add(this);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return text;
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
            public void declare(ElementNode context) {
                context.attrs.add(this);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append(name);

                if (value != null) {
                    builder.append("='").append(value).append("'");
                }

                return builder.toString();
            }
        }
    }
}
