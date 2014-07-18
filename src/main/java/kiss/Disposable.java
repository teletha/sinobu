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
 * @version 2014/01/31 16:28:17
 */
public interface Disposable {

    public static final Disposable NONE = () -> {
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
     * @param other An another {@link Disposable} to compose.
     * @return A composed {@link Disposable}.
     */
    default Disposable and(Disposable other) {
        return () -> {
            dispose();
            other.dispose();
        };
    }
}
