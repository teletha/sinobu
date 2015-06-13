/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lambda;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import kiss.Disposable;

/**
 * @version 2014/07/18 14:29:28
 */
public class Lambda {

    /** The task to do nothing. */
    public static final Consumer φ = value -> {
        // do nothing
    };

    /**
     * <p>
     * Apply parameter partially for the given function.
     * </p>
     * 
     * @param function An actual function to apply parameter partially. If this function is
     *            <code>null</code>, empty fuction ({@link #Φ}) will be returned.
     * @return The parameter binded function.
     */
    public static Disposable run(Supplier function) {
        if (function == null) {
            return Disposable.Φ;
        }
        return () -> function.get();
    }

    /**
     * <p>
     * Apply parameter partially for the given function.
     * </p>
     * 
     * @param function An actual function to apply parameter partially. If this function is
     *            <code>null</code>, empty fuction ({@link #Φ}) will be returned.
     * @param param A input paramter to bind.
     * @return The parameter binded function.
     */
    public static <Param> Disposable run(Consumer<Param> function, Param param) {
        if (function == null) {
            return Disposable.Φ;
        }
        return () -> function.accept(param);
    }

    /**
     * <p>
     * Apply parameter partially for the given function.
     * </p>
     * 
     * @param function An actual function to apply parameter partially. If this function is
     *            <code>null</code>, empty fuction ({@link #Φ}) will be returned.
     * @param param1 First input paramter to bind.
     * @param param2 Second input paramter to bind.
     * @return The parameter binded function.
     */
    public static <Param1, Param2> Disposable run(BiConsumer<Param1, Param2> function, Param1 param1, Param2 param2) {
        return run(consume(function, param1), param2);
    }

    /**
     * <p>
     * Apply parameter partially for the given function.
     * </p>
     * 
     * @param function An actual function to apply parameter partially. If this function is
     *            <code>null</code>, empty fuction ({@link #φ}) will be returned.
     * @param param First input paramter to bind.
     * @return The parameter binded function.
     */
    public static <Param1, Param2> Consumer<Param2> consume(BiConsumer<Param1, Param2> function, Param1 param) {
        if (function == null) {
            return φ;
        }
        return param2 -> function.accept(param, param2);
    }

    /**
     * <p>
     * Apply parameter partially for the given function.
     * </p>
     * 
     * @param function An actual function to apply parameter partially. If this function is
     *            <code>null</code>, {@link NullPointerException} will be thrown.
     * @param param First input paramter to bind.
     * @return The parameter binded function.
     */
    public static <Param, Return> Supplier<Return> supply(Function<Param, Return> function, Param param) {
        return () -> function.apply(param);
    }

    /**
     * <p>
     * Apply parameter partially for the given function.
     * </p>
     * 
     * @param function An actual function to apply parameter partially. If this function is
     *            <code>null</code>, {@link NullPointerException} will be thrown.
     * @param param1 First input paramter to bind.
     * @param param2 Second input paramter to bind.
     * @return The parameter binded function.
     */
    public static <Param1, Param2, Return> Supplier<Return> supply(BiFunction<Param1, Param2, Return> function, Param1 param1, Param2 param2) {
        return supply(function(function, param1), param2);
    }

    /**
     * <p>
     * Apply parameter partially for the given function.
     * </p>
     * 
     * @param function An actual function to apply parameter partially. If this function is
     *            <code>null</code>, {@link NullPointerException} will be thrown.
     * @param param1 First input paramter to bind.
     * @return The parameter binded function.
     */
    public static <Param1, Param2, Return> Function<Param2, Return> function(BiFunction<Param1, Param2, Return> function, Param1 param1) {
        return param2 -> function.apply(param1, param2);
    }

    /**
     * <p>
     * Create recursive function.
     * </p>
     * 
     * @param function A target function to convert.
     * @return A converted recursive function.
     */
    public static <Param, Return> Function<Param, Return> recursive(Function<Function<Param, Return>, Function<Param, Return>> function) {
        Map<Param, Return> memo = new HashMap<>();

        Recursive<Function<Param, Return>> recursive = recursiveFunction -> function.apply(param -> {
            return memo.computeIfAbsent(param, value -> recursiveFunction.apply(recursiveFunction).apply(param));
        });

        return recursive.apply(recursive);
    }

    /**
     * @version 2014/07/20 9:37:18
     */
    private interface Recursive<F> extends Function<Recursive<F>, F> {
    }

    /**
     * <p>
     * Create recursive function.
     * </p>
     * 
     * @param function A target function to convert.
     * @return A converted recursive function.
     */
    public static Runnable recursiveR(Function<Runnable, Runnable> function) {
        Recursive<Runnable> recursive = recursiveFunction -> function.apply(() -> {
            recursiveFunction.apply(recursiveFunction).run();
        });
        return recursive.apply(recursive);
    }
}
