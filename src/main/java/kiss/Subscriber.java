/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Internal subscription.
 * 
 * @version 2018/03/23 10:27:07
 */
class Subscriber<T> implements Observer<T>, Disposable {

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

    /** The delegation. */
    Observer observer;

    /** The delegation. */
    Consumer<? super T> next;

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
            Observer.super.error(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        try {
            if (next != null) {
                next.accept(value);
            } else if (observer != null) {
                observer.accept(value);
            }
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void vandalize() {
    }

    /**
     * Utility to create the specific {@link Signal} for this {@link Subscriber}.
     * 
     * @return
     */
    Signal<T> signal() {
        CopyOnWriteArrayList<Observer<T>> observers = new CopyOnWriteArrayList();
        observer = I.bundle(Observer.class, observers);

        return new Signal<>(observers);
    }

    private static Map<Disposable, Subscriber> cache = new WeakHashMap();

    static Subscriber of(Disposable disposable) {
        if (disposable instanceof Subscriber) {
            return (Subscriber) disposable;
        } else {
            return cache.computeIfAbsent(disposable, k -> new Subscriber());
        }
    }
}
