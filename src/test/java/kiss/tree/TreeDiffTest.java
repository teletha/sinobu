/*
 * Copyright (C) 2018 Nameless Production Committee
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
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import kiss.Tree;
import kiss.TreeNode;

/**
 * @version 2017/02/14 16:15:35
 */
public class TreeDiffTest {

    @Test
    public void noChange() {
        assertDiff(0, state -> new XML() {
            {
                $("div");
            }
        });
    }

    @Test
    public void replaceNode() {
        assertDiff(1, state -> new XML() {
            {
                if (state) {
                    $("div");
                } else {
                    $("change");
                }
            }
        });
    }

    @Test
    public void appendNode() {
        assertDiff(1, state -> new XML() {
            {
                $("div");
                if (state) $("append");
            }
        });
    }

    @Test
    public void prependNode() {
        assertDiff(1, state -> new XML() {
            {
                if (state) $("prepend");
                $("div");
            }
        });
    }

    @Test
    public void insertNode() {
        assertDiff(1, state -> new XML() {
            {
                $("div");
                if (state) $("insert");
                $("div");
            }
        });
    }

    @Test
    public void removeNode() {
        assertDiff(1, state -> new XML() {
            {
                if (state == false) $("remove");
            }
        });
    }

    @Test
    public void addChild() {
        assertDiff(1, state -> new XML() {
            {
                $("div", () -> {
                    if (state) $("child");
                });
            }
        });
    }

    @Test
    public void addChildren() {
        assertDiff(2, state -> new XML() {
            {
                $("div", iｆ(state, foŕ(2, count -> {
                    $(String.valueOf(count));
                })));
            }
        });
    }

    @Test
    public void moveChild() {
        String[] prev = {"1", "2", "3"};
        String[] next = {"2", "3", "1"};

        assertDiff(1, state -> new XML() {
            {
                $("div", foŕ(!state ? prev : next, value -> {
                    $(value);
                }));
            }
        });
    }

    @Test
    public void addAttribute() {
        assertDiff(1, state -> new XML() {
            {
                $("div", iｆ(state, attr("ok")));
            }
        });
    }

    @Test
    public void removeAttribute() {
        assertDiff(1, state -> new XML() {
            {
                $("div", iｆ(state == false, attr("ok")));
            }
        });
    }

    @Test
    public void replaceAttribute() {
        assertDiff(2, state -> new XML() {
            {
                $("div", either(state, attr("yes"), attr("no")));
            }
        });
    }

    @Test
    public void addText() {
        assertDiff(1, state -> new XML() {
            {
                $("div", () -> {
                    if (state) text("ok");
                });
            }
        });
    }

    @Test
    public void removeText() {
        assertDiff(1, state -> new XML() {
            {
                $("div", () -> {
                    if (state == false) text("ok");
                });
            }
        });
    }

    @Test
    public void replaceText() {
        assertDiff(1, state -> new XML() {
            {
                $("div", () -> {
                    text(state ? "yes" : "no");
                });
            }
        });
    }

    /**
     * <p>
     * Assertion helper.
     * </p>
     * 
     * @param expectedPatchSize
     * @param prev
     * @param next
     */
    private void assertDiff(int expectedPatchSize, Function<Boolean, XML> builder) {
        XML prev = builder.apply(false);
        XML next = builder.apply(true);

        List<Runnable> patches = TreeNode.diff(prev.dummyRoot(), prev.root, next.root);
        assert patches.size() == expectedPatchSize : prev;

        for (Runnable patch : patches) {
            patch.run();
        }
        assert prev.toString().equals(next.toString());
    }

    /**
     * @version 2017/02/14 16:18:48
     */
    protected static class XML extends Tree<String, XMLNode> {

        /**
         * @param namedNodeBuilder
         * @param relationshipBuilder
         * @param uniqueKeyBuilder
         */
        protected XML() {
            super(XMLNode::new, null);
        }

        /**
         * <p>
         * Write attribute.
         * </p>
         * 
         * @param name
         */
        protected Consumer<XMLNode> attr(String name) {
            return attr(name, null);
        }

        /**
         * <p>
         * Write attribute.
         * </p>
         * 
         * @param name
         */
        protected Consumer<XMLNode> attr(String name, String value) {
            return context -> $(new AttributeNode(name, null));
        }

        /**
         * <p>
         * Write text.
         * </p>
         * 
         * @param name
         */
        protected void text(String text) {
            $(parent -> {
                parent.nodes().add(new TextNode(text));
            });
        }

        /** The dummy root node. */
        private XMLNode dummy;

        /** The lazy initialization for dummy root. */
        private XMLNode dummyRoot() {
            if (dummy == null) {
                dummy = new XMLNode("", 0, null);
                dummy.nodes().addAll(root);
            }
            return dummy;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Object child : dummyRoot().nodes()) {
                builder.append(child);
            }
            return builder.toString();
        }
    }

    /**
     * @version 2017/02/15 9:22:42
     */
    private static class XMLNode extends TreeNode<XMLNode, XMLNode> {

        /** The element name. */
        private String name;

        /** The list of attributes. */
        private List<AttributeNode> attrs = new ArrayList();

        /**
         * @param name
         */
        private XMLNode(String name, int id, Object context) {
            super(id);

            this.name = name;
            this.context = this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void addTo(XMLNode parent, Object index) {
            if (index == null) {
                parent.nodes().add(this);
            } else {
                parent.nodes().add(parent.nodes().indexOf(index), this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void removeFrom(XMLNode parent) {
            parent.nodes().remove(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void moveTo(XMLNode parent) {
            parent.nodes().remove(this);
            parent.nodes().add(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void replaceFrom(XMLNode parent, XMLNode item) {
            int index = parent.nodes().indexOf(this);
            parent.nodes().set(index, item);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void diff(List<Runnable> patches, XMLNode next) {
            diff(patches, attrs, next.attrs, attrs::add, attrs::remove);
            diff(patches, next.context, nodes(), next.nodes());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            if (name.isEmpty()) {
                for (Object child : nodes()) {
                    builder.append(child);
                }
                return builder.toString();
            }

            builder.append("<").append(name);

            for (AttributeNode attr : attrs) {
                builder.append(" ").append(attr);
            }

            if (nodes().isEmpty()) {
                builder.append("/>");
            } else {
                builder.append(">");
                for (Object child : nodes()) {
                    builder.append(child);
                }
                builder.append("</").append(name).append(">");
            }
            return builder.toString();
        }
    }

    /**
     * @version 2017/02/15 19:09:58
     */
    private static class TextNode extends TreeNode<TextNode, XMLNode> {

        /** The current text. */
        private String text;

        /**
         * @param text
         */
        private TextNode(Object text) {
            super(text.hashCode());

            this.text = String.valueOf(text);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void addTo(XMLNode parent, Object index) {
            if (index == null) {
                parent.nodes().add(this);
            } else {
                parent.nodes().add(parent.nodes().indexOf(index), this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void removeFrom(XMLNode parent) {
            parent.nodes().remove(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void replaceFrom(XMLNode parent, TextNode item) {
            text = item.text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void moveTo(XMLNode parent) {
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
     * @version 2017/02/15 19:09:43
     */
    private static class AttributeNode implements Consumer<XMLNode> {

        private String name;

        private String value;

        private AttributeNode(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(XMLNode parent) {
            parent.attrs.add(this);
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
