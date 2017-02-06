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

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import kiss.I;
import kiss.model.Model;

/**
 * @version 2017/02/06 13:57:33
 */
public abstract class Structure<BUILDER extends StructureBuilder> {

    /** The associated builder. */
    private final BUILDER builder = I.make((Class<BUILDER>) Model.collectParameters(getClass(), Structure.class)[0]);

    /** The node tree. */
    private final ArrayDeque nodes = new ArrayDeque();

    private Object context;

    private int contenxtModifier;

    /**
     * <p>
     * Declare node with name.
     * </p>
     * 
     * @param name A node name.
     */
    protected final void $(String name, Declarable... declarables) {
        Object node = builder.enterNode(nodes.peekLast(), name);

        for (Declarable declarable : declarables) {
            if (declarable != null) {
                declarable.declare(node);
            }
        }

        builder.leaveNode(name);
    }

    protected final Declarable $(Declarable declarable) {
        declarable.declare(builder);
        return declarable;
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
                builder.attribute(nodes.peekLast(), name, value);
            }
        };
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
    protected final <C> Declarable $$(Iterable<C> children, Consumer<C> generator) {
        return $$(children, (index, child) -> {
            return () -> generator.accept(child);
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
    protected final <C> Declarable $$(Iterable<C> children, Function<C, Declarable> generator) {
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
    protected final <C> Declarable $$(Iterable<C> children, BiConsumer<Integer, C> generator) {
        return $$(children, (index, child) -> {
            return () -> generator.accept(index, child);
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
    protected final <C> Declarable $$(Iterable<C> children, BiFunction<Integer, C, Declarable> generator) {
        return () -> {
            // store parent context
            Object parentContext = context;
            int parentModifier = contenxtModifier;
            int index = 0;

            for (C child : children) {
                context = child;
                contenxtModifier = (Objects.hash(child) + 117) ^ 31;
                generator.apply(index++, child).declare();
            }

            // restore parent context
            context = parentContext;
            contenxtModifier = parentModifier;
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return builder.toString();
    }
}
