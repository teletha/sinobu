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

import java.nio.file.WatchEvent;
import java.util.function.Consumer;

/**
 * <p>
 * Versatile wrapper or delegator.
 * </p>
 * 
 * @version 2017/03/31 23:47:11
 */
class Agent<T> implements Observer<T>, WatchEvent, Disposable {

    /** For reuse. */
    T object;

    /**
     * {@link Agent} must have this constructor only. Dont use instance field initialization to
     * reduce creation cost.
     */
    Agent() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
    }

    // ============================================================
    // For Observer
    // ============================================================

    /** The delegation. */
    Observer observer;

    /** The delegation. */
    Consumer<T> next;

    /** The delegation. */
    Consumer<Throwable> error;

    /** The delegation. */
    Runnable complete;

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() {
        if (complete != null) {
            complete.run();
        } else if (observer != null) {
            observer.complete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Throwable e) {
        if (error != null) {
            error.accept(e);
        } else if (observer != null) {
            observer.error(e);
        } else {
            Thread.currentThread().getThreadGroup().uncaughtException(Thread.currentThread(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        if (next != null) {
            next.accept(value);
        } else if (observer != null) {
            observer.accept(value);
        }
    }

    // ============================================================
    // For WatchEvent
    // ============================================================

    /** The event holder. */
    WatchEvent watch;

    /**
     * {@inheritDoc}
     */
    @Override
    public Kind kind() {
        return watch.kind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count() {
        return watch.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T context() {
        return object;
    }

    /** The current location for deserialization process. */
    int index;
}
