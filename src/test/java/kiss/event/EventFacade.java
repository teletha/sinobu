/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

import javafx.beans.property.Property;

import kiss.Disposable;
import kiss.Events;
import kiss.I;
import kiss.Observer;

/**
 * @version 2015/05/23 9:07:18
 */
public class EventFacade<V, R> {

    /** The multiplier. */
    private static final int multiplier = 2;

    /** The observer container. */
    private final List<Observer<? super V>> observers = new CopyOnWriteArrayList();

    /** The listener container. */
    private final List<Listener<R>> listeners = new ArrayList();

    /** The listener container. */
    private final List<Disposable> disposables = new ArrayList();

    /** The counter. */
    private int disposed = 0;

    /**
     * 
     */
    public EventFacade() {
        assert 1 < multiplier;
    }

    /**
     * <p>
     * Declare event stream definition to test.
     * </p>
     * 
     * @param declaration
     */
    public EventFacade(Function<Events<V>, Events<R>> declaration) {
        this();

        for (int i = 0; i < multiplier; i++) {
            Listener<R> listener = new Listener<>();
            listeners.add(listener);

            disposables.add(declaration.apply(observe()).to(listener));
        }
    }

    /**
     * <p>
     * Declare event stream definition to test.
     * </p>
     * 
     * @param declaration
     */
    public EventFacade(BiFunction<Events<V>, EventFacade<V, R>, Events<R>> declaration) {
        this();

        for (int i = 0; i < multiplier; i++) {
            Listener<R> listener = new Listener<>();
            listeners.add(listener);

            disposables.add(declaration.apply(observe(), this).to(listener));
        }
    }

    /**
     * @param property
     */
    public EventFacade(Property<R> property) {
        this();

        for (int i = 0; i < multiplier; i++) {
            Listener<R> listener = new Listener<>();
            listeners.add(listener);

            disposables.add(I.observe(property).to(listener));
        }
    }

    /**
     * <p>
     * Create event stream.
     * </p>
     * 
     * @return
     */
    public Events<V> observe() {
        return new Events<>((observer, disposer) -> {
            if (observer != null) observers.add(observer);

            return disposer.and(() -> {
                disposed++;
                if (observer != null) observers.remove(observer);
            });
        });
    }

    /**
     * <p>
     * Create event stream with some values.
     * </p>
     * 
     * @return
     */
    public Events<V> observeWith(V... values) {
        return observe().startWith(values);
    }

    /**
     * <p>
     * Helper method to emit the specified event.
     * </p>
     */
    public void emit(V... values) {
        if (values == null || values.length == 0) {
            return;
        }

        for (Observer<? super V> observer : observers) {
            for (V value : values) {
                observer.accept(value);
            }
        }
    }

    /**
     * <p>
     * Helper method to retrieve the oldest event.
     * </p>
     * 
     * @return
     */
    /**
     * <p>
     * Retrieve the oldest event.
     * </p>
     */
    public R retrieve() {
        List<R> results = new ArrayList();

        for (Listener<R> listener : listeners) {
            results.add(listener.values.isEmpty() ? null : listener.values.remove(0));
        }

        // all result values must be same
        if (results.isEmpty()) {
            return null;
        }
        R value = results.get(0);

        for (R result : results) {
            assert Objects.equals(value, result);
        }
        return value;
    }

    /**
     * <p>
     * Helper method to retrieve the oldest event as {@link List}.
     * </p>
     */
    public boolean retrieveAsList(V... expecteds) {
        R retrieved = retrieve();
        assert retrieved instanceof List;
        List list = (List) retrieved;
        assert list.size() == expecteds.length;

        for (int i = 0; i < expecteds.length; i++) {
            assert list.get(i) == expecteds[i];
        }
        return true;
    }

    /**
     * <p>
     * Helper method to emit the specified event and retrieve the oldest event.
     * </p>
     */
    public R emitAndRetrieve(V value) {
        emit(value);

        return retrieve();
    }

    /**
     * <p>
     * Helper method to emit the specified event and retrieve the oldest event.
     * </p>
     */
    public boolean emitAndRetrieve(V value, V expected) {
        emit(value);

        assert retrieve() == expected;

        return true;
    }

    /**
     * <p>
     * Helper method to emit the specified event and retrieve the oldest event.
     * </p>
     */
    public boolean emitAndRetrieveAsList(V value, V... expecteds) {
        emit(value);

        return retrieveAsList(expecteds);
    }

    /**
     * <p>
     * Dispose all associated event streams.
     * </p>
     */
    public boolean dispose() {
        return disposeWithCountAlreadyDisposed(0);
    }

    /**
     * <p>
     * Dispose all associated event streams.
     * </p>
     */
    public boolean disposeWithCountAlreadyDisposed(int alreadyDisposed) {
        assert disposed / multiplier == alreadyDisposed;

        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();

        if (disposed != 0) assert disposed / multiplier >= alreadyDisposed;
        // assert observers.isEmpty() == true;

        return isCompleted();
    }

    /**
     * <p>
     * Helper method to check whether the related event observers are disposed completely or not.
     * </p>
     */
    public boolean isCompleted() {
        // assert observers.isEmpty();
        return true;
    }

    /**
     * <p>
     * Helper method to check whether the related event observers are disposed completely or not.
     * </p>
     */
    public boolean isCompleted(int disposedCount) {
        assert observers.isEmpty();
        assert disposedCount / multiplier == disposed;
        return true;
    }

    /**
     * @return
     */
    public boolean isNotCompleted() {
        assert observers.isEmpty() == false;
        return true;
    }

    /**
     * @version 2015/05/23 9:24:51
     */
    private static class Listener<V> implements Observer<V> {

        /** The value holder. */
        private final List<V> values = new ArrayList();

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(V value) {
            values.add(value);
        }
    }
}
