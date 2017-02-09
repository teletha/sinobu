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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javafx.beans.property.ReadOnlyProperty;

import sun.misc.SharedSecrets;

/**
 * <p>
 * The skeleton of DSL for tree structure.
 * </p>
 * 
 * @version 2017/02/08 11:17:30
 */
public abstract class Tree<N extends Consumer<N>> {

    /** The condition to filter backtraces. */
    private static final String THIS = Tree.class.getName();

    /** The anonymous root nodes. */
    public final List<N> root = new ArrayList<>(1);

    /** The named node creator. */
    private final ThrowableTriFunction<String, Integer, Object, N> namedNodeBuilder;

    /** The child node creator. */
    private final BiConsumer<N, Consumer> relationshipBuilder;

    /** The unique key builder. */
    private final IntUnaryOperator uniqueKeyBuilder;

    /** The current writering node. */
    private N current;

    /** The current context object. */
    private Object context;

    /** The current context id. */
    private int modifier = 31;

    /**
     * <p>
     * Create tree structure DSL.
     * </p>
     *
     * @param namedNodeBuilder A builder for special named node. {@link Tree} provides name and
     *            unique id.
     * @param relationshipBuilder A builder for parent-child node relationship.
     * @param uniqueKeyBuilder A builder for identical key.
     */
    protected Tree(ThrowableTriFunction<String, Integer, Object, N> namedNodeBuilder, BiConsumer<N, Consumer> relationshipBuilder, IntUnaryOperator uniqueKeyBuilder) {
        this.namedNodeBuilder = Objects.requireNonNull(namedNodeBuilder);
        this.relationshipBuilder = Objects.requireNonNull(relationshipBuilder);
        this.uniqueKeyBuilder = uniqueKeyBuilder != null ? uniqueKeyBuilder : id -> {
            Exception e = new Exception();

            for (int i = 2; i < 7; i++) {
                StackTraceElement element = SharedSecrets.getJavaLangAccess().getStackTraceElement(e, i);

                if (!element.getClassName().equals(THIS)) {
                    return hash(element.getLineNumber() ^ id);
                }
            }
            return id;
        };
    }

    /**
     * <p>
     * Declare nodes.
     * </p>
     * 
     * @param nodes A list of following {@link Consumer} node.
     */
    protected final void $(Consumer<N>... nodes) {
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
     * @param writer A content writer that lambda expression make us readable on nested structure.
     */
    protected final void $(String name, Runnable writer) {
        $(name, new Consumer[] {$(writer)});
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
     * @param writer A content writer that lambda expression make us readable on nested structure.
     */
    protected final void $(String name, Consumer<N> one, Runnable writer) {
        $(name, new Consumer[] {one, $(writer)});
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
     * @param writer A content writer that lambda expression make us readable on nested structure.
     */
    protected final void $(String name, Consumer<N> one, Consumer<N> two, Runnable writer) {
        $(name, new Consumer[] {one, two, $(writer)});
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
     * @param writer A content writer that lambda expression make us readable on nested structure.
     */
    protected final void $(String name, Consumer<N> one, Consumer<N> two, Consumer<N> three, Runnable writer) {
        $(name, new Consumer[] {one, two, three, $(writer)});
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
     * @param writer A content writer that lambda expression make us readable on nested structure.
     */
    protected final void $(String name, Consumer<N> one, Consumer<N> two, Consumer<N> three, Consumer<N> four, Runnable writer) {
        $(name, new Consumer[] {one, two, three, four, $(writer)});
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
     * @param nodes A list of following {@link Consumer} node.
     */
    protected final void $(String name, Consumer<N>... nodes) {
        $(namedNodeBuilder.apply(name, uniqueKeyBuilder.applyAsInt(modifier), context), nodes);
    }

    /**
     * Helper method to convert {@link Runnable} to {@link Consumer}.
     * 
     * @param run A target {@link Runnable} to convert.
     * @return A converted {@link Consumer}.
     */
    private final Consumer<N> $(Runnable run) {
        return e -> {
            if (run != null) run.run();
        };
    }

    /**
     * <p>
     * Make parent-child relationship between the current node and the specified node.
     * </p>
     * 
     * @param node A child node.
     * @param followers
     */
    private final void $(N node, Consumer<N>... followers) {
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
            for (Consumer<N> Consumer : followers) {
                if (Consumer != null) {
                    relationshipBuilder.accept(current, Consumer);
                }
            }
        }

        // restore parent context
        current = parentNode;
    }

    /**
     * <p>
     * Nest-like range writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param size An exclusive upper bound.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final Consumer<N> foŕ(int size, Consumer<Integer> writer) {
        // we can optimize code using IntConsumer, but the uniformity has high priority than that
        return foŕ(0, size, writer);
    }

    /**
     * <p>
     * Nest-like range writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param startInclusive An inclusive initial value
     * @param endExclusive An exclusive upper bound.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final Consumer<N> foŕ(int startInclusive, int endExclusive, Consumer<Integer> writer) {
        // we can optimize code using IntConsumer, but the uniformity has high priority than that
        return foŕ(() -> IntStream.range(startInclusive, endExclusive).iterator(), writer);
    }

    /**
     * <p>
     * Nest-like collection writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param type A type of {@link Enum} contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <E extends Enum> Consumer<N> foŕ(Class<E> type, Consumer<E> writer) {
        return foŕ(type.getEnumConstants(), writer);
    }

    /**
     * <p>
     * Nest-like collection writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param type A type of {@link Enum} contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <E extends Enum> Consumer<N> foŕ(Class<E> type, Function<E, Consumer<N>> writer) {
        return foŕ(type.getEnumConstants(), writer);
    }

    /**
     * <p>
     * Nest-like collection writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param contents A list of child contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <C> Consumer<N> foŕ(C[] contents, Consumer<C> writer) {
        return foŕ(Arrays.asList(contents), writer);
    }

    /**
     * <p>
     * Nest-like collection writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param contents A list of child contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <C> Consumer<N> foŕ(C[] contents, Function<C, Consumer<N>> writer) {
        return foŕ(Arrays.asList(contents), writer);
    }

    /**
     * <p>
     * Nest-like collection writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param contents A list of child contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <C> Consumer<N> foŕ(Iterable<C> contents, Consumer<C> writer) {
        return foŕ(contents, (index, child) -> {
            return current -> writer.accept(child);
        });
    }

    /**
     * <p>
     * Nest-like collection writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param contents A list of child contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <C> Consumer<N> foŕ(Iterable<C> contents, Function<C, Consumer<N>> writer) {
        return foŕ(contents, (index, child) -> {
            return writer.apply(child);
        });
    }

    /**
     * <p>
     * Nest-like collection writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param contents A list of child contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <C> Consumer<N> foŕ(Iterable<C> contents, BiConsumer<Integer, C> writer) {
        return foŕ(contents, (index, child) -> {
            return current -> writer.accept(index, child);
        });
    }

    /**
     * <p>
     * Nest-like collection writer.
     * </p>
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * </p>
     * 
     * @param contents A list of child contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <C> Consumer<N> foŕ(Iterable<C> contents, BiFunction<Integer, C, Consumer<N>> writer) {
        return current -> {
            // store parent context
            Object parentContext = context;
            int parentModifier = modifier;
            int index = 0;

            for (C child : contents) {
                context = child;
                modifier = hash(child == null ? 0 : child.hashCode());
                $(writer.apply(index++, child));
            }

            // restore parent context
            context = parentContext;
            modifier = parentModifier;
        };
    }

    /**
     * <p>
     * Conditional writer.
     * </p>
     * 
     * @param condition A condition.
     * @param nodes A list of successible nodes.
     * @return A declaration of contents.
     */
    protected final Consumer<N> iｆ(ReadOnlyProperty<Boolean> condition, Consumer<N>... success) {
        return either(condition, I.bundle(success), null);
    }

    /**
     * <p>
     * Conditional writer.
     * </p>
     * 
     * @param condition A condition.
     * @param nodes A list of successible nodes.
     * @return A declaration of contents.
     */
    protected final Consumer<N> iｆ(Supplier<Boolean> condition, Consumer<N>... success) {
        return either(condition, I.bundle(success), null);
    }

    /**
     * <p>
     * Conditional writer.
     * </p>
     * 
     * @param condition A condition.
     * @param nodes A list of successible nodes.
     * @return A declaration of contents.
     */
    protected final Consumer<N> iｆ(boolean condition, Consumer<N>... success) {
        return either(condition, success, null);
    }

    /**
     * <p>
     * Conditional writer.
     * </p>
     * 
     * @param condition A condition.
     * @param success A success node.
     * @param failure A failure node.
     * @return A declaration of contents.
     */
    protected final Consumer<N> either(ReadOnlyProperty<Boolean> condition, Consumer<N> success, Consumer<N> failure) {
        return either(condition != null && Boolean.TRUE.equals(condition.getValue()), success, failure);
    }

    /**
     * <p>
     * Conditional writer.
     * </p>
     * 
     * @param condition A condition.
     * @param success A success node.
     * @param failure A failure node.
     * @return A declaration of contents.
     */
    protected final Consumer<N> either(Supplier<Boolean> condition, Consumer<N> success, Consumer<N> failure) {
        return either(condition != null && Boolean.TRUE.equals(condition.get()), success, failure);
    }

    /**
     * <p>
     * Conditional writer.
     * </p>
     * 
     * @param condition A condition.
     * @param success A success node.
     * @param failure A failure node.
     * @return A declaration of contents.
     */
    protected final Consumer<N> either(boolean condition, Consumer<N> success, Consumer<N> failure) {
        return either(condition, new Consumer[] {success}, failure);
    }

    /**
     * <p>
     * Conditional writer.
     * </p>
     * 
     * @param condition A condition.
     * @param success A success node.
     * @param failure A failure node.
     * @return A declaration of contents.
     */
    private final Consumer<N> either(boolean condition, Consumer<N>[] success, Consumer<N> failure) {
        return current -> {
            if (condition) {
                $(success);
            } else {
                $(failure);
            }
        };
    }

    /** The bit mask size. */
    private static final int[] MASK = {0, 8, 16, 24};

    /**
     * <p>
     * FNV-1a 32bit hash function.
     * </p>
     * 
     * @param value A target value.
     * @return A hash value.
     */
    private final int hash(int value) {
        int hash = 0x811c9dc5;

        for (int i = 0; i < 4; i++) {
            hash = (hash ^ (0x000000ff & (value >>> MASK[i]))) * 0x01000193;
        }
        return hash;
    }
}
