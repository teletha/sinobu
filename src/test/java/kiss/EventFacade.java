/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @version 2015/05/13 10:07:56
 */
public class EventFacade<E> implements Observer<E> {

    /** The listener holder. */
    private final List<Listener> listeners = new CopyOnWriteArrayList();

    /** The event holder. */
    private final List<E> events = new ArrayList();

    /** The dispose count. */
    private int disposed;

    /**
     * <p>
     * Observe EventEmitter.
     * </p>
     */
    public Events<E> observe() {

        return new Events<>(observer -> {
            Listener<E> listener = event -> {
                observer.accept(event);
            };
            add(listener);

            return () -> {
                disposed++;
                remove(listener);
            };
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(E event) {
        events.add(event);
    }

    /**
     * <p>
     * Add event listener.
     * </p>
     */
    public void add(Listener<E> listner) {
        if (listner != null) {
            listeners.add(listner);
        }
    }

    /**
     * <p>
     * Remove event listener.
     * </p>
     */
    public void remove(Listener<E> listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * <p>
     * Emit the specified event.
     * </p>
     */
    public void emit(E... events) {
        if (events == null || events.length == 0) {
            return;
        }

        for (Listener<E> listener : listeners) {
            for (int i = 0; i < events.length; i++) {
                listener.listen(events[i]);
            }
        }
    }

    /**
     * <p>
     * Retrieve the oldest event.
     * </p>
     */
    public E retrieve() {
        return events.isEmpty() ? null : events.remove(0);
    }

    /**
     * <p>
     * Retrieve the oldest event.
     * </p>
     */
    public E retrieveLast() {
        E event = events.isEmpty() ? null : events.remove(events.size() - 1);
        events.clear();
        return event;
    }

    /**
     * <p>
     * Helper method to emit the specified event and retrieve the oldest event.
     * </p>
     */
    public E emitAndRetrieve(E event) {
        emit(event);

        return retrieve();
    }

    /**
     * @version 2014/01/05 10:11:55
     */
    public static interface Listener<E> {

        /**
         * <p>
         * Event listener.
         * </p>
         * d
         */
        public void listen(E event);
    }

    /**
     * <p>
     * Check state.
     * </p>
     */
    public boolean isSubscribed() {
        return !listeners.isEmpty();
    }

    /**
     * <p>
     * Check state.
     * </p>
     */
    public boolean isUnsubscribed() {
        return listeners.isEmpty();
    }

    /**
     * <p>
     * Check event queue.
     * </p>
     * 
     * @return A result.
     */
    public boolean isEmpty() {
        return events.isEmpty();
    }

    /**
     * @param value
     * @param i
     * @return
     */
    public Events<E> stream(E... values) {
        return new Events<E>(observer -> () -> disposed++).startWith(values);
    }

    /**
     * @return
     */
    public int countDisposed() {
        return disposed;
    }

    /**
     * @param i
     * @param j
     * @return
     */
    public boolean retrieve(Object... expected) {
        if (expected.length != 1) {
            Object next = retrieve();

            if (next instanceof List) {
                List list = (List) next;

                assert list.size() == expected.length;

                for (int i = 0; i < expected.length; i++) {
                    assert Objects.equals(list.get(i), expected[i]);
                }
            }
        }
        return true;
    }

    /**
     * @return
     */
    public boolean retrieveNull() {
        return Objects.isNull(retrieve());
    }
}
