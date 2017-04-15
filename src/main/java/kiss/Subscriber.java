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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Internal subscription.
 * 
 * @version 2017/04/01 15:42:33
 */
class Subscriber<T> implements Observer<T>, Disposable, WatchEvent {

    /** Generic object. */
    T object;

    /** Generic counter. */
    int index;

    /** Generic list. */
    List<T> list;

    /**
     * {@link Subscriber} must have this constructor only. Dont use instance field initialization to
     * reduce creation cost.
     */
    Subscriber() {
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
        index++;

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
        if (index == 0) {
            if (error != null) {
                error.accept(e);
            } else if (observer != null) {
                observer.error(e);
            } else {
                Thread.currentThread().getThreadGroup().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        if (index == 0) {
            if (next != null) {
                next.accept(value);
            } else if (observer != null) {
                observer.accept(value);
            }
        }
    }

    private List<Subscriber> children;

    boolean isCompleted() {
        if (index == 0) {
            return false;
        }

        if (children != null) {
            for (Subscriber child : children) {
                if (child.isCompleted() == false) {
                    return false;
                }
            }
        }
        return true;
    }

    Subscriber<T> child() {
        Subscriber sub = new Subscriber();
        sub.observer = observer;
        sub.next = next;
        sub.error = error;
        sub.complete = complete;

        if (children == null) {
            children = new ArrayList();
        }
        children.add(sub);

        return sub;
    }

    // ============================================================
    // For Disposable
    // ============================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
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
}
