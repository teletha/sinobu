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

import java.util.function.Consumer;

import org.junit.Test;

import kiss.Tree;
import kiss.TreeNode;

/**
 * @version 2017/02/14 16:15:35
 */
public class TreeDiffTest {

    @Test
    public void diff() {
        XML prev = new XML() {
            {
                $("div");
            }
        };

        XML next = new XML() {
            {
                $("change");
            }
        };

        TreeNode.diff(null, prev.root, next.root);
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
            super(XMLNode::new, null, null);
        }

        /**
         * @param next
         */
        public void diff(XML next) {
        }
    }

    /**
     * @version 2017/02/14 16:16:54
     */
    private static class XMLNode extends TreeNode<XMLike> implements Consumer<XMLNode> {

        /**
         * 
         */
        public XMLNode(String name, int id, Object object) {
            this.id = id;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(XMLNode parent) {
        }
    }

    /**
     * @version 2017/02/14 16:18:10
     */
    private static class XMLike {

    }
}
