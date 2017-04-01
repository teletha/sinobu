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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * The {@link Disposable} interface is used when components need to deallocate and dispose resources
 * prior to their destruction.
 * </p>
 * 
 * @version 2017/03/18 17:22:17
 */
public interface Disposable {

    void run();

    /**
     * <p>
     * The dispose operation is called at the end of a components lifecycle. Components use this
     * method to release and destroy any resources that the Component owns.
     * </p>
     */
    default void dispose() {
        run();

        List<Disposable> children = I.associate(this, List.class);

        for (Disposable child : children) {
            child.dispose();
        }

        I.associate(this, AtomicBoolean.class).set(true);
    }

    /**
     * <p>
     * Check the state of operation. (default : false)
     * </p>
     * 
     * @return A result.
     */
    default boolean isDisposed() {
        return I.associate(this, AtomicBoolean.class).get();
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
        if (next != null && next != this) {
            I.associate(this, List.class).add(next);
        }
        return this;
    }

    default Disposable sub() {
        Disposable sub = empty();
        and(sub);
        return sub;
    }

    static Disposable empty() {
        return new Agent();
    }
}
