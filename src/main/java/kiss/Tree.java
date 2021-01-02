/*
 * Copyright (C) 2021 Nameless Production Committee
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

/**
 * The skeleton of DSL for tree structure.
 * 
 * @version 2018/09/10 18:38:20
 */
public abstract class Tree<Name, Node extends Consumer<Node>> {

    /** The condition to filter backtraces. */
    private static final String THIS = Tree.class.getName();

    /** The anonymous root nodes. */
    public final List<Node> root = new ArrayList<>(1);

    /** The named node creator. */
    private final WiseTriFunction<Name, Integer, Object, Node> namedNodeBuilder;

    /** The unique key builder. */
    private final IntUnaryOperator uniqueKeyBuilder;

    /** The follower processor. */
    private final BiConsumer<Consumer<Node>, Node> followerBuilder;

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
    protected Tree(WiseTriFunction<Name, Integer, Object, Node> namedNodeBuilder, IntUnaryOperator uniqueKeyBuilder) {
        this(namedNodeBuilder, uniqueKeyBuilder, null);
    }

    /**
     * <p>
     * Create tree structure DSL.
     * </p>
     *
     * @param namedNodeBuilder A builder for special named node. {@link Tree} provides name and
     *            unique id.
     * @param uniqueKeyBuilder A builder for identical key.
     */
    protected Tree(WiseTriFunction<Name, Integer, Object, Node> namedNodeBuilder, IntUnaryOperator uniqueKeyBuilder, BiConsumer<Consumer<Node>, Node> followerBuilder) {
        this.namedNodeBuilder = Objects.requireNonNull(namedNodeBuilder);
        this.uniqueKeyBuilder = uniqueKeyBuilder != null ? uniqueKeyBuilder
                : id -> StackWalker.getInstance()
                        .walk(s -> s.skip(2)
                                .filter(f -> !f.getClassName().equals(THIS))
                                .findFirst()
                                .map(f -> f.getByteCodeIndex() ^ id)
                                .orElse(id));
        this.followerBuilder = Objects.requireNonNullElse(followerBuilder, Consumer<Node>::accept);
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
    protected final void $(Name name, WiseRunnable writer) {
        $(name, new Consumer[] {I.wiseC(writer)});
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
    protected final void $(Name name, Consumer<Node> one, WiseRunnable writer) {
        $(name, new Consumer[] {one, I.wiseC(writer)});
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
    protected final void $(Name name, Consumer<Node> one, Consumer<Node> two, WiseRunnable writer) {
        $(name, new Consumer[] {one, two, I.wiseC(writer)});
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
    protected final void $(Name name, Consumer<Node> one, Consumer<Node> two, Consumer<Node> three, WiseRunnable writer) {
        $(name, new Consumer[] {one, two, three, I.wiseC(writer)});
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
    protected final void $(Name name, Consumer<Node> one, Consumer<Node> two, Consumer<Node> three, Consumer<Node> four, WiseRunnable writer) {
        $(name, new Consumer[] {one, two, three, four, I.wiseC(writer)});
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
                    followerBuilder.accept(follower, current);
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
                modifier = child == null ? 0 : child.hashCode();
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
}