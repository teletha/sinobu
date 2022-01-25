/*
 * Copyright (C) 2022 The SINOBU Development Team
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
public class Signaling<V> implements Observer<V> {

    /** The internal listeners. */
    final CopyOnWriteArrayList<Observer<? super V>> observers = new CopyOnWriteArrayList();

    /** The exposed interface. */
    public final Signal<V> expose = new Signal(observers);

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(V value) {
        for (Observer<? super V> observer : observers) {
            observer.accept(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() {
        for (Observer<? super V> observer : observers) {
            observer.complete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Throwable error) {
        for (Observer<? super V> observer : observers) {
            observer.error(error);
        }
    }
}