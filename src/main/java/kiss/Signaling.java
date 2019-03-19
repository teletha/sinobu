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

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple {@link Signal} support subject.
 * 
 * @version 2018/04/28 1:58:42
 */
public class Signaling<T> implements Observer<T> {

    /** The internal listeners. */
    final CopyOnWriteArrayList<Observer<? super T>> observers = new CopyOnWriteArrayList();

    /** The exposed interface. */
    public final Signal<T> expose = new Signal<>((observer, disposer) -> {
        observers.add(observer);

        return disposer.add(() -> {
            observers.remove(observer);
        });
    });

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        for (Observer<? super T> observer : observers) {
            observer.accept(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() {
        for (Observer<? super T> observer : observers) {
            observer.complete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Throwable error) {
        for (Observer<? super T> observer : observers) {
            observer.error(error);
        }
    }
}
