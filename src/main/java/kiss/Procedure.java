/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>
 * The {@link Procedure} interface is a sequence of program instructions that perform a specific
 * task, packaged as a unit. This unit can then be used in programs wherever that particular task
 * should be performed.
 * </p>
 * 
 * @version 2014/07/19 0:48:17
 */
public interface Procedure extends Runnable {

    /** The task to do nothing. */
    public static final Procedure Φ = () -> {
        // do nothing
    };

    /**
     * <p>
     * Returns a composed {@link Procedure}.
     * </p>
     * 
     * @param next An next {@link Procedure} to execute.
     * @return A composed {@link Procedure}.
     */
    default Procedure and(Procedure next) {
        return () -> {
            run();
            next.run();
        };
    }

    /**
     * <p>
     * Apply parameter partially for the given function.
     * </p>
     * 
     * @param function An actual function to apply parameter partially. If this function is
     *            <code>null</code>, empty fuction ({@link #Φ}) will be returned.
     * @return The parameter binded function.
     */
    public static Procedure λ(Supplier function) {
        if (function == null) {
            return Φ;
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
    public static <Param> Procedure λ(Consumer<Param> function, Param param) {
        if (function == null) {
            return Φ;
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
    public static <Param1, Param2> Procedure λ(BiConsumer<Param1, Param2> function, Param1 param1, Param2 param2) {
        if (function == null) {
            return Φ;
        }
        return λ(λ(function, param1), param2);
    }

    /**
     * <p>
     * Apply parameter partially for the given function.
     * </p>
     * 
     * @param function An actual function to apply parameter partially. If this function is
     *            <code>null</code>, empty fuction ({@link #φ}) will be returned.
     * @param param First input paramter to bind.
     */
    public static <Param1, Param2> Consumer<Param2> λ(BiConsumer<Param1, Param2> function, Param1 param) {
        return param2 -> function.accept(param, param2);
    }
}
