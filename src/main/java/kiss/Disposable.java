/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

/**
 * The {@link Disposable} interface is used when components need to deallocate and dispose resources
 * prior to their destruction.
 */
public interface Disposable {

    /**
     * Functional interface method to implement actual dispose action, this operation should be
     * idempotent.
     * <p>
     * User SHOULD NOT call this method directly.
     */
    void vandalize();

    /**
     * Dispose the resource, this operation should be idempotent.
     */
    default void dispose() {
        // dispose children
        Subscriber<Disposable> subscriber = Subscriber.of(this);

        if (subscriber.index == 0) {
            // mark as disposed
            subscriber.index++;

            if (subscriber.list != null) {
                subscriber.list.forEach(Disposable::dispose);
                subscriber.list = null;
            }

            // dispose self
            vandalize();
        }
    }

    /**
     * Check the state of operation. (default : false)
     * 
     * @return A result.
     */
    default boolean isDisposed() {
        return Subscriber.of(this).index != 0;
    }

    /**
     * Append companion {@link Disposable}.
     * 
     * @param next A next {@link Disposable} to execute.
     * @return A composed {@link Disposable}.
     */
    default Disposable add(Disposable next) {
        if (next != null && next != this) {
            Subscriber subscriber = Subscriber.of(this);

            if (subscriber.list == null) {
                subscriber.list = new CopyOnWriteArrayList();
            }
            subscriber.list.add(next);
        }
        return this;
    }

    /**
     * Append as companion {@link Disposable}. {@link Future} will be canceled.
     * 
     * @param next A next {@link Disposable} to execute.
     * @return A composed {@link Disposable}.
     */
    default Disposable add(Future next) {
        return next == null ? this : add(() -> {
            next.cancel(false);
        });
    }

    /**
     * Create child {@link Disposable}.
     * 
     * @return A child {@link Disposable}.
     */
    default Disposable sub() {
        Disposable sub = empty();
        add(sub);
        sub.add(() -> Subscriber.of(this).list.remove(sub));
        return sub;
    }

    /**
     * Create new empty {@link Disposable}.
     * 
     * @return A created empty {@link Disposable}.
     */
    static Disposable empty() {
        return new Subscriber();
    }
}