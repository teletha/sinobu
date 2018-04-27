/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple {@link Signal} support subject.
 * 
 * @version 2018/04/28 1:58:42
 */
@SuppressWarnings("serial")
public class Signaler<T> extends CopyOnWriteArrayList<Observer<T>> implements Observer<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T value) {
        for (Observer<? super T> observer : this) {
            observer.accept(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void complete() {
        for (Observer<? super T> observer : this) {
            observer.complete();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Throwable error) {
        for (Observer<? super T> observer : this) {
            observer.error(error);
        }
    }
}
