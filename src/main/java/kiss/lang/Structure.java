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
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import kiss.lang.HTML.ElementNode;

/**
 * @version 2017/02/08 9:12:12
 */
public abstract class Structure<N extends Declarable<N>> {

    /** The root nodes. */
    private final List<N> root = new ArrayList(1);

    /** The named node creator. */
    private final Function<String, N> namedNodeBuilder;

    /** The child node creator. */
    private final BiConsumer<N, Declarable> relationshipBuilder;

    /** The current processing node. */
    private N current;

    /** The current context object. */
    private Object context;

    /** The current context id. */
    private int contenxtModifier;

    /**
     * <p>
     * Create tree structure DSL.
     * </p>
     * 
     * @param namedNodeBuilder A builder for special named node.
     * @param relationshipBuilder A builder for parent-child node relationship.
     */
    protected Structure(Function<String, N> namedNodeBuilder, BiConsumer<N, Declarable> relationshipBuilder) {
        this.namedNodeBuilder = Objects.requireNonNull(namedNodeBuilder);
        this.relationshipBuilder = Objects.requireNonNull(relationshipBuilder);
    }

    public List<N> root() {
        return root;
    }

    /**
     * <p>
     * Declare node with name.
     * </p>
     * <p>
     * Generic named node builder because named node is frequently used in tree structure.
     * </p>
     * 
     * @param name A name of new node.
     * @param followers A list of following {@link Declarable} node.
     */
    protected final void e(String name, Declarable<N>... followers) {
        $(namedNodeBuilder.apply(name), followers);
    }

    /**
     * <p>
     * Declare node with name.
     * </p>
     * <p>
     * Generic named node builder because named node is frequently used in tree structure.
     * </p>
     * 
     * @param name A name of new node.
     * @param followers A list of following {@link Declarable} node.
     */
    protected final void e(String name, Runnable nest) {
        e(name, null, nest);
    }

    protected final <D extends Declarable<ElementNode>> void e(String name, D one, Runnable nest) {
        e(name, one, null, null, null, null, nest);
    }

    protected final <D extends Declarable<ElementNode>> void e(String name, D one, D two, Runnable nest) {
        e(name, one, two, null, null, null, nest);
    }

    protected final <D extends Declarable<ElementNode>> void e(String name, D one, D two, D three, Runnable nest) {
        e(name, one, two, three, null, null, nest);
    }

    protected final <D extends Declarable<ElementNode>> void e(String name, D one, D two, D three, D four, Runnable nest) {
        e(name, one, two, three, four, null, nest);
    }

    protected final <D extends Declarable<ElementNode>> void e(String name, D one, D two, D three, D four, D five, Runnable children) {
        $(namedNodeBuilder.apply(name), new Declarable[] {one, two, three, four, five, e -> {
            if (children != null) children.run();
        }});
    }

    /**
     * <p>
     * Make parent-child relationship between the current node and the specified node.
     * </p>
     * 
     * @param node A child node.
     * @param followers
     */
    protected final void $(N node, Declarable<N>... followers) {
        // store parent context
        N parentNode = current;

        if (node != null) {
            if (current == null) {
                root.add(node);
            } else {
                relationshipBuilder.accept(current, node);
            }

            // update context
            current = node;
        }

        if (followers != null) {
            for (Declarable<N> declarable : followers) {
                if (declarable != null) {
                    relationshipBuilder.accept(current, declarable);
                }
            }
        }

        // restore parent context
        current = parentNode;
    }

    /**
     * <p>
     * Define children.
     * </p>
     * 
     * @param children A children contents.
     * @param generator A content generator.
     * @return A declaration of contents.
     */
    protected final <C> Declarable<N> $(Iterable<C> children, Consumer<C> generator) {
        return $$(children, (index, child) -> {
            return current -> generator.accept(child);
        });
    }

    /**
     * <p>
     * Define children.
     * </p>
     * 
     * @param children A children contents.
     * @param generator A content generator.
     * @return A declaration of contents.
     */
    protected final <C> Declarable<N> $$(Iterable<C> children, Function<C, Declarable<N>> generator) {
        return $$(children, (index, child) -> {
            return generator.apply(child);
        });
    }

    /**
     * <p>
     * Declare children.
     * </p>
     * 
     * @param children A children contents.
     * @param generator A content generator.
     * @return A declaration of contents.
     */
    protected final <C> Declarable<N> $$(Iterable<C> children, BiConsumer<Integer, C> generator) {
        return $$(children, (index, child) -> {
            return current -> generator.accept(index, child);
        });
    }

    /**
     * <p>
     * Declare children.
     * </p>
     * 
     * @param children A children contents.
     * @param generator A content generator.
     * @return A declaration of contents.
     */
    protected final <C> Declarable<N> $$(Iterable<C> children, BiFunction<Integer, C, Declarable<N>> generator) {
        return current -> {
            // store parent context
            Object parentContext = context;
            int parentModifier = contenxtModifier;
            int index = 0;

            for (C child : children) {
                context = child;
                contenxtModifier = (Objects.hash(child) + 117) ^ 31;
                generator.apply(index++, child).declare(current);
            }

            // restore parent context
            context = parentContext;
            contenxtModifier = parentModifier;
        };
    }
}
