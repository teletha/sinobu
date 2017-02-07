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

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @version 2017/02/07 11:09:59
 */
public abstract class Structure<N extends Declarable<N>, Self extends Structure> {

    /** The root node. */
    public final N root;

    /** The root node. */
    private final BiConsumer<N, Declarable> process;

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
     * @param root A root node.
     */
    protected Structure(N root, Function<Self, BiConsumer<N, Declarable>> process) {
        this.root = current = Objects.requireNonNull(root);
        this.process = Objects.requireNonNull(process).apply((Self) this);
    }

    protected final void $(Declarable declarable) {
        process.accept(current, declarable);
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

        $(node);

        // update context
        current = node;

        for (Declarable<N> follower : followers) {
            process.accept(current, follower);
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
    protected final <C> Declarable<N> $$(Iterable<C> children, Consumer<C> generator) {
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
