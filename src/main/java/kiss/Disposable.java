/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * <p>
 * The {@link Disposable} interface is used when components need to deallocate and dispose resources
 * prior to their destruction.
 * </p>
 * 
 * @version 2018/03/21 09:22:17
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
        // dispose children
        Subscriber<Disposable> subscriber = Subscriber.of(this);

        if (subscriber.list != null) {
            for (int i = subscriber.list.size() - 1; 0 <= i; i--) {
                subscriber.list.remove(i).dispose();
            }
            subscriber.list = null;
        }

        // dispose self
        vandalize();

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
                subscriber.list = new ArrayList();
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
        return add(next == null ? null : () -> next.cancel(true));
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