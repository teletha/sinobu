/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @version 2017/02/08 9:12:12
 */
public abstract class Tree<N extends Declarable<N>> {

    /** The root nodes. */
    private final List<N> root = new ArrayList(1);

    /** The named node creator. */
    private final ThrowableTriFunction<String, Integer, Object, N> namedNodeBuilder;

    /** The child node creator. */
    private final BiConsumer<N, Declarable> relationshipBuilder;

    /** The current processing node. */
    private N current;

    /** The current context object. */
    private Object context;

    /** The current context id. */
    private int contenxtModifier = 31;

    /**
     * <p>
     * Create tree structure DSL.
     * </p>
     *
     * @param namedNodeBuilder A builder for special named node. {@link Tree} provides name and
     *            unique id.
     * @param relationshipBuilder A builder for parent-child node relationship.
     */
    protected Tree(ThrowableTriFunction<String, Integer, Object, N> namedNodeBuilder, BiConsumer<N, Declarable> relationshipBuilder) {
        this.namedNodeBuilder = Objects.requireNonNull(namedNodeBuilder);
        this.relationshipBuilder = Objects.requireNonNull(relationshipBuilder);
    }

    public List<N> root() {
        return root;
    }

    /**
     * <p>
     * Declare nodes.
     * </p>
     * 
     * @param nodes A list of following {@link Declarable} node.
     */
    protected final void $(Declarable<N>... nodes) {
        $((N) null, nodes);
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
     * @param nodes A list of following {@link Declarable} node.
     */
    protected final void $(String name, Declarable<N>... nodes) {
        $(namedNodeBuilder.apply(name, contenxtModifier, context), nodes);
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
     * @param nest A list of following {@link Declarable} node by lambda expression.
     */
    protected final void $(String name, Runnable nest) {
        $(name, null, nest);
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
     * @param one A following node.
     * @param nest A list of following {@link Declarable} node by lambda expression.
     */
    protected final void $(String name, Declarable<N> one, Runnable nest) {
        $(name, one, null, null, null, nest);
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
     * @param one A following node.
     * @param two A following node.
     * @param nest A list of following {@link Declarable} node by lambda expression.
     */
    protected final void $(String name, Declarable<N> one, Declarable<N> two, Runnable nest) {
        $(name, one, two, null, null, nest);
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
     * @param one A following node.
     * @param two A following node.
     * @param three A following node.
     * @param nest A list of following {@link Declarable} node by lambda expression.
     */
    protected final void $(String name, Declarable<N> one, Declarable<N> two, Declarable<N> three, Runnable nest) {
        $(name, one, two, three, null, nest);
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
     * @param one A following node.
     * @param two A following node.
     * @param three A following node.
     * @param four A following node.
     * @param nest A list of following {@link Declarable} node by lambda expression.
     */
    protected final void $(String name, Declarable<N> one, Declarable<N> two, Declarable<N> three, Declarable<N> four, Runnable nest) {
        $(namedNodeBuilder.apply(name, contenxtModifier, context), new Declarable[] {one, two, three, four, e -> {
            if (nest != null) nest.run();
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
    private final void $(N node, Declarable<N>... followers) {
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
        return $(children, (index, child) -> {
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
    protected final <C> Declarable<N> $(Iterable<C> children, Function<C, Declarable<N>> generator) {
        return $(children, (index, child) -> {
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
    protected final <C> Declarable<N> $(Iterable<C> children, BiConsumer<Integer, C> generator) {
        return $(children, (index, child) -> {
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
    protected final <C> Declarable<N> $(Iterable<C> children, BiFunction<Integer, C, Declarable<N>> generator) {
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
