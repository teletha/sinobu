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
import java.util.List;

import kiss.lang.HTML.ElementNode;
import kiss.lang.test.StructureTest.Id;

/**
 * @version 2017/02/06 14:01:17
 */
public abstract class HTML extends Structure<ElementNode, HTML> {

    /**
     * 
     */
    public HTML() {
        super(that -> (context, declarable) -> {
            if (declarable instanceof Id) {
                that.attr("id", ((Id) declarable).id);
            } else {
                declarable.declare(context);
            }
        });
    }

    /**
     * <p>
     * Declare node with name.
     * </p>
     * 
     * @param name A node name.
     */
    protected final <D extends Declarable<ElementNode>> void e(String name) {
        e(name, null, null);
    }

    protected final void e(String name, Runnable children) {
        e(name, null, children);
    }

    protected final <D extends Declarable<ElementNode>> void e(String name, D one) {
        e(name, one, null);
    }

    protected final <D extends Declarable<ElementNode>> void e(String name, D one, Runnable children) {
        $(new ElementNode(name), new Declarable[] {one}, children);
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
        return (context) -> {
            if (name != null && !name.isEmpty()) {
                $(new AttributeNode(name, value));
            }
        };
    }

    protected void text(String text) {
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

        private List<ElementNode> children = new ArrayList();

        /**
         * @param name
         */
        private ElementNode() {
            this("");
        }

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
                for (ElementNode child : children) {
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
                for (ElementNode child : children) {
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
        public String toString() {
            return name;
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