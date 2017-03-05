/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
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
 * @version 2017/02/11 17:22:03
 */
public abstract class Tree<Name, Node extends Consumer<Node>> {

    /** The condition to filter backtraces. */
    private static final String THIS = Tree.class.getName();

    /** The anonymous root nodes. */
    public final List<Node> root = new ArrayList<>(1);

    /** The named node creator. */
    private final ThrowableTriFunction<Name, Integer, Object, Node> namedNodeBuilder;

    /** The unique key builder. */
    private final IntUnaryOperator uniqueKeyBuilder;

    /** The current writering node. */
    private Node current;

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
     * @param uniqueKeyBuilder A builder for identical key.
     */
    protected Tree(ThrowableTriFunction<Name, Integer, Object, Node> namedNodeBuilder, IntUnaryOperator uniqueKeyBuilder) {
        this.namedNodeBuilder = Objects.requireNonNull(namedNodeBuilder);
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
    protected final void $(Consumer<Node>... nodes) {
        $((Node) null, nodes);
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
    protected final void $(Name name, Runnable writer) {
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
    protected final void $(Name name, Consumer<Node> one, Runnable writer) {
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
    protected final void $(Name name, Consumer<Node> one, Consumer<Node> two, Runnable writer) {
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
    protected final void $(Name name, Consumer<Node> one, Consumer<Node> two, Consumer<Node> three, Runnable writer) {
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
    protected final void $(Name name, Consumer<Node> one, Consumer<Node> two, Consumer<Node> three, Consumer<Node> four, Runnable writer) {
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
    protected final void $(Name name, Consumer<Node>... nodes) {
        $(namedNodeBuilder.apply(name, uniqueKeyBuilder.applyAsInt(modifier), context), nodes);
    }

    /**
     * Helper method to convert {@link Runnable} to {@link Consumer}.
     * 
     * @param run A target {@link Runnable} to convert.
     * @return A converted {@link Consumer}.
     */
    private final Consumer<Node> $(Runnable run) {
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
    private final void $(Node node, Consumer<Node>... followers) {
        // store parent context
        Node parentNode = current;

        if (node != null) {
            if (current == null) {
                root.add(node);
            } else {
                node.accept(current);
            }

            // update context
            current = node;
        }

        if (followers != null) {
            for (Consumer<Node> follower : followers) {
                if (follower != null) {
                    follower.accept(current);
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
    protected final Consumer<Node> foŕ(int size, Consumer<Integer> writer) {
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
    protected final Consumer<Node> foŕ(int startInclusive, int endExclusive, Consumer<Integer> writer) {
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
    protected final <E extends Enum> Consumer<Node> foŕ(Class<E> type, Consumer<E> writer) {
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
    protected final <E extends Enum> Consumer<Node> foŕ(Class<E> type, Function<E, Consumer<Node>> writer) {
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
    protected final <C> Consumer<Node> foŕ(C[] contents, Consumer<C> writer) {
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
    protected final <C> Consumer<Node> foŕ(C[] contents, Function<C, Consumer<Node>> writer) {
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
    protected final <C> Consumer<Node> foŕ(Iterable<C> contents, Consumer<C> writer) {
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
    protected final <C> Consumer<Node> foŕ(Iterable<C> contents, Function<C, Consumer<Node>> writer) {
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
    protected final <C> Consumer<Node> foŕ(Iterable<C> contents, BiConsumer<Integer, C> writer) {
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
    protected final <C> Consumer<Node> foŕ(Iterable<C> contents, BiFunction<Integer, C, Consumer<Node>> writer) {
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
    protected final Consumer<Node> iｆ(ReadOnlyProperty<Boolean> condition, Consumer<Node>... success) {
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
    protected final Consumer<Node> iｆ(Supplier<Boolean> condition, Consumer<Node>... success) {
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
    protected final Consumer<Node> iｆ(boolean condition, Consumer<Node>... success) {
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
    protected final Consumer<Node> either(ReadOnlyProperty<Boolean> condition, Consumer<Node> success, Consumer<Node> failure) {
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
    protected final Consumer<Node> either(Supplier<Boolean> condition, Consumer<Node> success, Consumer<Node> failure) {
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
    protected final Consumer<Node> either(boolean condition, Consumer<Node> success, Consumer<Node> failure) {
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
    private final Consumer<Node> either(boolean condition, Consumer<Node>[] success, Consumer<Node> failure) {
        return current -> {
            if (condition) {
                $(success);
            } else {
                $(failure);
            }
        };
    }

    /**
     * Murmur3 32-bit hash function.
     *
     * @param value A input value.
     * @return A hash value.
     */
    private static int hash(int value) {
        byte[] data = new byte[] {(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};

        int hash = 0;
        final int nblocks = 4 >> 2;

        // body
        for (int i = 0; i < nblocks; i++) {
            int i_4 = i << 2;
            int k = (data[i_4] & 0xff) | ((data[i_4 + 1] & 0xff) << 8) | ((data[i_4 + 2] & 0xff) << 16) | ((data[i_4 + 3] & 0xff) << 24);

            // mix functions
            k *= 0xcc9e2d51;
            k = Integer.rotateLeft(k, 15);
            k *= 0x1b873593;
            hash ^= k;
            hash = Integer.rotateLeft(hash, 13) * 5 + 0xe6546b64;
        }

        // tail
        int idx = nblocks << 2;
        int k1 = 0;
        switch (4 - idx) {
        case 3:
            k1 ^= data[idx + 2] << 16;
        case 2:
            k1 ^= data[idx + 1] << 8;
        case 1:
            k1 ^= data[idx];

            // mix functions
            k1 *= 0xcc9e2d51;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= 0x1b873593;
            hash ^= k1;
        }

        // finalization
        hash ^= 4;
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);

        return hash;
    }
}
