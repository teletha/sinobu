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
     * Create tree structure DSL.
     *
     * @param namedNodeBuilder A builder for special named node. {@link Tree} provides name and
     *            unique id.
     * @param uniqueKeyBuilder A builder for identical key.
     */
    protected Tree(WiseTriFunction<Name, Integer, Object, Node> namedNodeBuilder, IntUnaryOperator uniqueKeyBuilder) {
        this(namedNodeBuilder, uniqueKeyBuilder, null);
    }

    /**
     * Create tree structure DSL.
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
     * Declare nodes.
     * 
     * @param nodes A list of following {@link Consumer} node.
     */
    protected final void $(Consumer<Node>... nodes) {
        $((Node) null, nodes);
    }

    /**
     * Declare node with name.
     * <p>
     * Generic named node builder because named node is frequently used in tree structure.
     * 
     * @param name A name of new node.
     * @param writer A content writer that lambda expression make us readable on nested structure.
     */
    protected final void $(Name name, WiseRunnable writer) {
        $(name, new Consumer[] {I.wiseC(writer)});
    }

    /**
     * Declare node with name.
     * <p>
     * Generic named node builder because named node is frequently used in tree structure.
     * 
     * @param name A name of new node.
     * @param one A following node.
     * @param writer A content writer that lambda expression make us readable on nested structure.
     */
    protected final void $(Name name, Consumer<Node> one, WiseRunnable writer) {
        $(name, new Consumer[] {one, I.wiseC(writer)});
    }

    /**
     * Declare node with name.
     * <p>
     * Generic named node builder because named node is frequently used in tree structure.
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
     * Declare node with name.
     * <p>
     * Generic named node builder because named node is frequently used in tree structure.
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
     * Declare node with name.
     * <p>
     * Generic named node builder because named node is frequently used in tree structure.
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
     * Declare node with name.
     * <p>
     * Generic named node builder because named node is frequently used in tree structure.
     * 
     * @param name A name of new node.
     * @param nodes A list of following {@link Consumer} node.
     */
    protected final void $(Name name, Consumer<Node>... nodes) {
        $(namedNodeBuilder.apply(name, uniqueKeyBuilder.applyAsInt(modifier), context), nodes);
    }

    /**
     * Make parent-child relationship between the current node and the specified node.
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
     * Nest-like range writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
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
     * Nest-like range writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
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
     * Nest-like collection writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * 
     * @param type A type of {@link Enum} contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <E extends Enum> Consumer<Node> foŕ(Class<E> type, Consumer<E> writer) {
        return foŕ(type.getEnumConstants(), writer);
    }

    /**
     * Nest-like collection writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * 
     * @param type A type of {@link Enum} contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <E extends Enum> Consumer<Node> foŕ(Class<E> type, Function<E, Consumer<Node>> writer) {
        return foŕ(type.getEnumConstants(), writer);
    }

    /**
     * Nest-like collection writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * 
     * @param contents A list of child contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <C> Consumer<Node> foŕ(C[] contents, Consumer<C> writer) {
        return foŕ(Arrays.asList(contents), writer);
    }

    /**
     * Nest-like collection writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
     * 
     * @param contents A list of child contents.
     * @param writer A content writer.
     * @return A declaration of contents.
     */
    protected final <C> Consumer<Node> foŕ(C[] contents, Function<C, Consumer<Node>> writer) {
        return foŕ(Arrays.asList(contents), writer);
    }

    /**
     * Nest-like collection writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
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
     * Nest-like collection writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
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
     * Nest-like collection writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
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
     * Nest-like collection writer.
     * <p>
     * Each item is identified by id and its object, you can receive them on node builder.
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
     * Represents an 'if' statement. If the condition is true, then the 'then' clause will be
     * executed.
     * 
     * @param condition A conditional expression.
     * @param then If the condition is true, it will execute the given node under the current parent
     *            node.
     * @return Node that performs the equivalent of an 'if' statement.
     */
    protected final Consumer<Node> iｆ(Supplier<Boolean> condition, Consumer<Node>... then) {
        return either(condition, I.bundle(then), null);
    }

    /**
     * Represents an 'if' statement. If the condition is true, then the 'then' clause will be
     * executed.
     * 
     * @param condition A condition.
     * @param then If the condition is true, it will execute the given node under the current parent
     *            node.
     * @return Node that performs the equivalent of an 'if' statement.
     */
    protected final Consumer<Node> iｆ(boolean condition, Consumer<Node>... then) {
        return either(condition, then, null);
    }

    /**
     * Represents an 'if-else' statement. If the condition is true, then the 'then' clause will be
     * executed. If the condition is false, then the 'else' clause will be executed.
     * 
     * @param condition A conditional expression.
     * @param then If the condition is true, it will execute the given node under the current parent
     *            node.
     * @param elze If the condition is false, it will execute the given node under the current
     *            parent node.
     * @return Node that performs the equivalent of an 'if-else' statement.
     */
    protected final Consumer<Node> either(Supplier<Boolean> condition, Consumer<Node> then, Consumer<Node> elze) {
        return either(condition != null && Boolean.TRUE.equals(condition.get()), then, elze);
    }

    /**
     * Represents an 'if-else' statement. If the condition is true, then the 'then' clause will be
     * executed. If the condition is false, then the 'else' clause will be executed.
     * 
     * @param condition A condition.
     * @param then If the condition is true, it will execute the given node under the current parent
     *            node.
     * @param elze If the condition is false, it will execute the given node under the current
     *            parent node.
     * @return Node that performs the equivalent of an 'if-else' statement.
     */
    protected final Consumer<Node> either(boolean condition, Consumer<Node> then, Consumer<Node> elze) {
        return either(condition, new Consumer[] {then}, elze);
    }

    /**
     * Represents an 'if-else' statement. If the condition is true, then the 'then' clause will be
     * executed. If the condition is false, then the 'else' clause will be executed.
     * 
     * @param condition A condition.
     * @param then If the condition is true, it will execute the given node under the current parent
     *            node.
     * @param elze If the condition is false, it will execute the given node under the current
     *            parent node.
     * @return Node that performs the equivalent of an 'if-else' statement.
     */
    private final Consumer<Node> either(boolean condition, Consumer<Node>[] then, Consumer<Node> elze) {
        return current -> {
            if (condition) {
                $(then);
            } else {
                $(elze);
            }
        };
    }
}