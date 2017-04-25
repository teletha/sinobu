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

/**
 * <p>
 * The {@link Disposable} interface is used when components need to deallocate and dispose resources
 * prior to their destruction.
 * </p>
 * 
 * @version 2017/03/18 17:22:17
 */
public interface Disposable {

    /**
     * <p>
     * Functional interface method to implement actual dispose action, this operation should be
     * idempotent.
     * </p>
     * <p>
     * User SHOULD NOT call this method directly.
     * </p>
     */
    void vandalize();

    /**
     * <p>
     * Dispose the resource, this operation should be idempotent.
     * </p>
     */
    default void dispose() {
        // dispose self
        vandalize();

        // dispose children
        Subscriber<Disposable> subscriber = I.associate(this, Subscriber.class);

        if (subscriber.list != null) {
            for (Disposable child : subscriber.list) {
                child.dispose();
            }
        }

        // mark as disposed
        subscriber.index++;
    }

    /**
     * <p>
     * Check the state of operation. (default : false)
     * </p>
     * 
     * @return A result.
     */
    default boolean isDisposed() {
        return I.associate(this, Subscriber.class).index != 0;
    }

    /**
     * <p>
     * Append child {@link Disposable}.
     * </p>
     * 
     * @param next An next {@link Disposable} to execute.
     * @return A composed {@link Disposable}.
     */
    default Disposable add(Disposable next) {
        if (next != null && next != this) {
            Subscriber subscriber = I.associate(this, Subscriber.class);

            if (subscriber.list == null) {
                subscriber.list = new ArrayList();
            }
            subscriber.list.add(next);
        }
        return this;
    }

    /**
     * <p>
     * Create child {@link Disposable}.
     * </p>
     * 
     * @return A child {@link Disposable}.
     */
    default Disposable sub() {
        Disposable sub = empty();
        add(sub);
        return sub;
    }

    /**
     * <p>
     * Create new empty {@link Disposable}.
     * </p>
     * 
     * @return A created empty {@link Disposable}.
     */
    static Disposable empty() {
        return new Subscriber();
    }
}
