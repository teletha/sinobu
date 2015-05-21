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

/**
 * <p>
 * The {@link Disposable} interface is used when components need to deallocate and dispose resources
 * prior to their destruction.
 * </p>
 * 
 * @version 2014/07/22 15:54:50
 */
public interface Disposable {

    /** The task to do nothing. */
    public static final Disposable Φ = () -> {
        // do nothing
    };

    /**
     * <p>
     * The dispose operation is called at the end of a components lifecycle. Components use this
     * method to release and destroy any resources that the Component owns.
     * </p>
     */
    void dispose();

    /**
     * <p>
     * Returns a composed {@link Disposable}.
     * </p>
     * 
     * @param next An next {@link Disposable} to execute.
     * @return A composed {@link Disposable}.
     */
    default Disposable and(Disposable next) {
        if (next == null) {
            return this;
        }

        return () -> {
            dispose();
            next.dispose();
        };
    }

    static Disposable empty() {
        return new Agent();
    }

    // /**
    // * <p>
    // * Apply parameter partially for the given function.
    // * </p>
    // *
    // * @param function An actual function to apply parameter partially. If this function is
    // * <code>null</code>, empty fuction ({@link #Φ}) will be returned.
    // * @param param A input paramter to bind.
    // * @return The parameter binded function.
    // */
    // public static <Param> Procedure call(Consumer<Param> function, Param param) {
    // if (function == null) {
    // return Φ;
    // }
    // return () -> function.accept(param);
    // }
    //
    // /**
    // * <p>
    // * Apply parameter partially for the given function.
    // * </p>
    // *
    // * @param function An actual function to apply parameter partially. If this function is
    // * <code>null</code>, empty fuction ({@link #φ}) will be returned.
    // * @param param First input paramter to bind.
    // */
    // public static <Param1, Param2> Consumer<Param2> call(BiConsumer<Param1, Param2> function,
    // Param1 param) {
    // return param2 -> function.accept(param, param2);
    // }
    //
    // /**
    // * <p>
    // * Apply parameter partially for the given function.
    // * </p>
    // *
    // * @param function An actual function to apply parameter partially. If this function is
    // * <code>null</code>, empty fuction ({@link #Φ}) will be returned.
    // * @param param1 First input paramter to bind.
    // * @param param2 Second input paramter to bind.
    // * @return The parameter binded function.
    // */
    // public static <Param1, Param2> Procedure call(BiConsumer<Param1, Param2> function, Param1
    // param1, Param2 param2) {
    // return call(call(function, param1), param2);
    // }
}
