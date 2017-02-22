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
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.junit.Test;

import kiss.Tree;
import kiss.tree.TreeTest.HTML.ElementNode;

/**
 * @version 2017/02/06 14:00:16
 */
public class TreeTest {

    @Test
    public void node() {
        HTML html = new HTML() {
            {
                $("html");
            }
        };
        assert html.toString().equals("<html/>");
    }

    @Test
    public void nodeNest() {
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
    public void nodes() {
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
    public void attributes2() {
        HTML html = new HTML() {
            {
                $("num", attr(1), attr(2), () -> {
                    $("ok");
                });
            }
        };
        assert html.toString().equals("<num 1 2><ok/></num>");
    }

    @Test
    public void attributes3() {
        HTML html = new HTML() {
            {
                $("num", attr(1), attr(2), attr(3), () -> {
                    $("ok");
                });
            }
        };
        assert html.toString().equals("<num 1 2 3><ok/></num>");
    }

    @Test
    public void attributes4() {
        HTML html = new HTML() {
            {
                $("num", attr(1), attr(2), attr(3), attr(4), () -> {
                    $("ok");
                });
            }
        };
        assert html.toString().equals("<num 1 2 3 4><ok/></num>");
    }

    @Test
    public void ifBoolean() {
        boolean ok = true;
        boolean fail = false;

        HTML html = new HTML() {
            {
                $("num", iｆ(ok, attr(1)), iｆ(fail, attr(2)));
            }
        };
        assert html.toString().equals("<num 1/>");
    }

    @Test
    public void ifSupplier() {
        Supplier<Boolean> ok = () -> true;
        Supplier<Boolean> fail = () -> false;
        Supplier<Boolean> nul = null;
        Supplier<Boolean> empty = () -> null;

        HTML html = new HTML() {
            {
                $("num", iｆ(ok, attr(1)), iｆ(fail, attr(2)), iｆ(nul, attr(3)), iｆ(empty, attr(4)));
            }
        };
        assert html.toString().equals("<num 1/>");
    }

    @Test
    public void ifProperty() {
        BooleanProperty ok = new SimpleBooleanProperty(true);
        BooleanProperty fail = new SimpleBooleanProperty(false);
        BooleanProperty nul = null;
        Property<Boolean> empty = new SimpleObjectProperty(null);

        HTML html = new HTML() {
            {
                $("num", iｆ(ok, attr(1)), iｆ(fail, attr(2)), iｆ(nul, attr(3)), iｆ(empty, attr(4)));
            }
        };
        assert html.toString().equals("<num 1/>");
    }

    @Test
    public void either() {
        HTML html = new HTML() {
            {
                $("num", either(true, attr(1), attr(-1)), either(false, attr(2), attr(-2)));
            }
        };
        assert html.toString().equals("<num 1 -2/>");
    }

    @Test
    public void forRange() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(2, index -> {
                    $("li", () -> text(index));
                }));

            }
        };
        assert html.toString().equals("<ol><li>0</li><li>1</li></ol>");
    }

    @Test
    public void forRangeWithInitial() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(1, 3, index -> {
                    $("li", () -> text(index));
                }));

            }
        };
        assert html.toString().equals("<ol><li>1</li><li>2</li></ol>");
    }

    @Test
    public void forEnumType() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(ENUM.class, value -> {
                    $("li", () -> text(value.name()));
                }));

            }
        };
        assert html.toString().equals("<ol><li>A</li><li>B</li></ol>");
    }

    /**
     * @version 2017/02/08 14:04:08
     */
    private static enum ENUM {
        A, B;
    }

    @Test
    public void forArray() {
        String[] array = {"A", "B"};

        HTML html = new HTML() {
            {
                $("ol", foŕ(array, value -> {
                    $("li", () -> text(value));
                }));

            }
        };
        assert html.toString().equals("<ol><li>A</li><li>B</li></ol>");
    }

    @Test
    public void forIterableConsumer() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(list("A", "B"), item -> {
                    $("li", () -> text(item));
                }));

            }
        };
        assert html.toString().equals("<ol><li>A</li><li>B</li></ol>");
    }

    @Test
    public void forIterableConsumerWithIndex() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(list("A", "B"), (id, item) -> {
                    $("li", () -> text(item + id));
                }));
            }
        };
        assert html.toString().equals("<ol><li>A0</li><li>B1</li></ol>");
    }

    @Test
    public void forIterableFunction() {
        HTML html = new HTML() {
            {
                $("div", foŕ(list("a", "b"), Id::new));
            }
        };
        assert html.toString().equals("<div id='a' id='b'/>");
    }

    @Test
    public void forIterableFunctionWithIndex() {
        HTML html = new HTML() {
            {
                $("ol", foŕ(list("a", "b"), ListItem::new));
            }
        };
        assert html.toString().equals("<ol><li>a0</li><li>b1</li></ol>");
    }

    /**
     * @version 2017/02/08 16:44:25
     */
    private static class ListItem implements Consumer<ElementNode> {

        private int index;

        private String name;

        /**
         * @param index
         * @param name
         */
        private ListItem(int index, String name) {
            this.index = index;
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(ElementNode parent) {
            parent.add(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return name + index;
        }
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
    public void outerDefinedConsumer() {
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
    public static class Id implements Consumer<ElementNode> {

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
        public void accept(ElementNode parent) {
            parent.add(this);
        }
    }

    /**
     * @version 2017/02/08 9:40:08
     */
    public static abstract class HTML extends Tree<String, ElementNode> {

        /**
         * 
         */
        public HTML() {
            super(ElementNode::new, null);
        }

        /**
         * <p>
         * accept node attribute with name.
         * </p>
         * 
         * @param name An attribute name.
         */
        protected final Consumer attr(Object name) {
            return attr(name, null);
        }

        /**
         * <p>
         * accept node attribute with name.
         * </p>
         * 
         * @param name An attribute name.
         */
        protected final Consumer<ElementNode> attr(Object name, String value) {
            return parent -> {
                if (name != null) {
                    String n = String.valueOf(name);

                    if (!n.isEmpty()) {
                        parent.attrs.add(new AttributeNode(n, value));
                    }
                }
            };
        }

        /**
         * <p>
         * accept text node.
         * </p>
         * 
         * @param text A text.
         */
        protected final void text(Object text) {
            $(new TextNode(String.valueOf(text)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            for (ElementNode node : root) {
                builder.append(node);
            }
            return builder.toString();
        }

        /**
         * @version 2017/02/06 16:02:42
         */
        public static class ElementNode implements Consumer<ElementNode> {

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
             * @param listItem
             */
            public void add(ListItem item) {
                ElementNode e = new ElementNode("li", 0, item);
                e.children.add(new TextNode(item));
                children.add(e);
            }

            /**
             * @param id
             */
            public void add(Id id) {
                attrs.add(new AttributeNode("id", id.id));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void accept(ElementNode context) {
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
        private static class TextNode implements Consumer<ElementNode> {

            private final String text;

            /**
             * @param text
             */
            private TextNode(Object text) {
                this.text = String.valueOf(text);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void accept(ElementNode context) {
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
        private static class AttributeNode {

            private String name;

            private String value;

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
