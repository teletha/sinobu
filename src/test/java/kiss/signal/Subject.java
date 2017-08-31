/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

import kiss.Disposable;
import kiss.I;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2017/03/21 23:11:40
 */
public class Subject<V, R> {

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

    public Recorde recorder;

    /**
     * 
     */
    public Subject() {
        assert 1 < multiplier;
    }

    /**
     * <p>
     * Declare event stream definition to test.
     * </p>
     * 
     * @param declaration
     */
    public Subject(Function<Signal<V>, Signal<R>> declaration) {
        this();

        for (int i = 0; i < multiplier; i++) {
            Listener<R> listener = new Listener<>();
            listeners.add(listener);

            disposables.add(declaration.apply(signal()).to(listener));
        }
    }

    /**
     * <p>
     * Declare event stream definition to test.
     * </p>
     * 
     * @param declaration
     */
    public Subject(BiFunction<Signal<V>, Subject<V, R>, Signal<R>> declaration) {
        this();

        for (int i = 0; i < multiplier; i++) {
            Listener<R> listener = new Listener<>();
            listeners.add(listener);

            disposables.add(declaration.apply(signal(), this).to(listener));
        }
    }

    /**
     * <p>
     * Create {@link Subject} with recoder utility.
     * </p>
     * 
     * @param declaration
     * @return
     */
    public static final <V, R> Subject<V, R> recorde(Function<Recorde<V>, Function<Signal<V>, Signal<R>>> declaration) {
        Subject<V, R> subject = new Subject();
        Recorde<V>[] recordes = new Recorde[multiplier];

        for (int i = 0; i < multiplier; i++) {
            Listener<R> listener = new Listener<>();
            recordes[i] = new Recorder();

            subject.listeners.add(listener);
            subject.disposables.add(declaration.apply(recordes[i]).apply(subject.signal()).to(listener));
        }
        subject.recorder = I.bundle(recordes);
        return subject;
    }

    /**
     * <p>
     * Create event stream.
     * </p>
     * 
     * @return
     */
    public Signal<V> signal() {
        return new Signal<>((observer, disposer) -> {
            if (observer != null) observers.add(observer);

            return disposer.add(() -> {
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
    public Signal<V> observeWith(V... values) {
        return signal().startWith(values);
    }

    /**
     * <p>
     * Helper method to emit the specified values to all monitored {@link Observer}.
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

    public boolean isDisposed() {
        return disposed != 0;
    }

    public boolean isNotDisposed() {
        return disposed == 0;
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
        assert observers.isEmpty() == true;

        return isCompleted();
    }

    /**
     * Complete all observers.
     */
    public void complete() {
        for (Observer<? super V> observer : observers) {
            observer.complete();
        }
    }

    /**
     * <p>
     * Helper method to check whether the related event observers are disposed completely or not.
     * </p>
     */
    public boolean isCompleteEventReceieved() {
        for (Listener listener : listeners) {
            if (listener.completed == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Helper method to check whether the related event observers are disposed completely or not.
     * </p>
     */
    public boolean isCompleted() {
        assert observers.isEmpty();
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

    public void error(Class<? extends Throwable> throwable) {
        assert throwable != null;

        for (Observer<? super V> observer : observers) {
            observer.error(I.make(throwable));
        }
    }

    /**
     * @version 2017/09/01 0:18:53
     */
    private static class Listener<V> implements Observer<V> {

        /** The value holder. */
        private final List<V> values = new ArrayList();

        /** The complete flag. */
        private boolean completed = false;

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(V value) {
            values.add(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void complete() {
            completed = true;
        }
    }

    /**
     * @version 2017/03/21 17:52:42
     */
    public static interface Recorde<V> {

        public void complete();

        public boolean isCompleted();

        public default boolean isNotCompleted() {
            return !isCompleted();
        }

        public void error(Throwable throwable);

        public Throwable error();

        public default boolean hasError() {
            return error() != null;
        }
    }

    /**
     * @version 2017/03/21 18:04:15
     */
    private static class Recorder<V> implements Recorde<V> {

        private boolean completed;

        /**
         * {@inheritDoc}
         */
        @Override
        public void complete() {
            completed = true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCompleted() {
            return completed;
        }

        private Throwable error;

        /**
         * {@inheritDoc}
         */
        @Override
        public void error(Throwable throwable) {
            this.error = throwable;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Throwable error() {
            return error;
        }
    }
}
