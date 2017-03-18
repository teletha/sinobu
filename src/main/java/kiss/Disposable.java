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

/**
 * <p>
 * The {@link Disposable} interface is used when components need to deallocate and dispose resources
 * prior to their destruction.
 * </p>
 * 
 * @version 2017/03/18 17:22:17
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
     * Check the state of operation. (default : false)
     * </p>
     * 
     * @return A result.
     */
    default boolean isDisposed() {
        return false;
    }

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
}
