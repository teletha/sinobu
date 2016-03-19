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
}
