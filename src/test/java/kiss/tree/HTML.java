/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import kiss.Tree;
import kiss.tree.TreeTest.Id;
import kiss.tree.TreeTest.ListItem;

public abstract class HTML extends Tree<String, HTML.ElementNode> {

    /**
     * 
     */
    public HTML() {
        super(HTML.ElementNode::new, null);
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
    protected final Consumer<HTML.ElementNode> attr(Object name, String value) {
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

        for (HTML.ElementNode node : root) {
            builder.append(node);
        }
        return builder.toString();
    }

    /**
     * @version 2017/02/06 16:02:42
     */
    public static class ElementNode implements Consumer<HTML.ElementNode> {

        protected String name;

        private List<HTML.AttributeNode> attrs = new ArrayList();

        private List children = new ArrayList();

        /**
         * @param name
         */
        private ElementNode(String name, int id, Object context) {
            this.name = name;
        }

        /**
         * @param item
         */
        public void add(ListItem item) {
            HTML.ElementNode e = new ElementNode("li", 0, item);
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
        public void accept(HTML.ElementNode context) {
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

            for (HTML.AttributeNode attr : attrs) {
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
    private static class TextNode implements Consumer<HTML.ElementNode> {

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
        public void accept(HTML.ElementNode context) {
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