/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import kiss.Disposable;
import kiss.I;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2017/04/01 21:46:44
 */
public class SignalTestBase {

    /** default multiplicity */
    private static final int multiplicity = 2;

    /** READ ONLY : DON'T MODIFY in test case */
    protected Log result = null;

    /** READ ONLY : DON'T MODIFY in test case */
    protected Log log1 = null;

    /** READ ONLY : DON'T MODIFY in test case */
    protected Log log2 = null;

    /** READ ONLY : DON'T MODIFY in test case */
    protected Log log3 = null;

    /** READ ONLY : DON'T MODIFY in test case */
    protected Log log4 = null;

    /** READ ONLY : DON'T MODIFY in test case */
    protected Log log5 = null;

    /** READ ONLY : DON'T MODIFY in test case */
    protected Disposable disposer = null;

    /**
     * Create generic error {@link Function}.
     * 
     * @return
     */
    protected final <P, R> Function<P, R> errorFunction() {
        return e -> {
            throw new Error();
        };
    };

    /**
     * Create generic error {@link Iterable}.
     * 
     * @return
     */
    protected final <T> Iterable<T> errorIterable() {
        return () -> {
            throw new Error();
        };
    }

    /**
     * Shorthand method of {@link I#list(Object...)}.
     * 
     * @param values
     * @return
     */
    protected <T> List<T> list(T... values) {
        return I.list(values);
    }

    /**
     * Shorthand method of {@link Stream#of}.
     * 
     * @param values
     * @return
     */
    protected <T> Stream<T> stream(T... values) {
        return Stream.of(values);
    }

    /**
     * Shorthand method of {@link Collections#enumeration(java.util.Collection)}.
     * 
     * @param values
     * @return
     */
    protected <T> Enumeration<T> enume(T... values) {
        return Collections.enumeration(I.list(values));
    }

    /**
     * Shorthand method of {@link I#from}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(T... values) {
        return I.signal(values);
    }

    /**
     * Shorthand method of {@link I#from}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(Iterable values) {
        return I.signal(values);
    }

    /**
     * Shorthand method of {@link I#from}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(Enumeration values) {
        return I.signal(values);
    }

    /**
     * Shorthand method of {@link I#from}
     * 
     * @param values
     * @return
     */
    protected <T, S extends BaseStream<T, S>> Signal<T> signal(S values) {
        return I.signal(values);
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected void monitor(Supplier<Signal> signal) {
        monitor(multiplicity, signal);
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected void monitor(int multiplicity, Supplier<Signal> signal) {
        LogSet[] sets = new LogSet[multiplicity];

        for (int i = 0; i < multiplicity; i++) {
            sets[i] = new LogSet();
            log1 = sets[i].log1;
            log2 = sets[i].log2;
            log3 = sets[i].log3;
            log4 = sets[i].log4;
            log5 = sets[i].log5;
            result = sets[i].result;

            sets[i].disposer = signal.get().to(result);
        }

        log1 = I.bundle(stream(sets).map(e -> e.log1).collect(toList()));
        log2 = I.bundle(stream(sets).map(e -> e.log2).collect(toList()));
        log3 = I.bundle(stream(sets).map(e -> e.log3).collect(toList()));
        log4 = I.bundle(stream(sets).map(e -> e.log4).collect(toList()));
        log5 = I.bundle(stream(sets).map(e -> e.log5).collect(toList()));
        result = I.bundle(stream(sets).map(e -> e.result).collect(toList()));
        disposer = I.bundle(Disposable.class, stream(sets).map(e -> e.disposer).collect(toList()));
    }

    /**
     * @version 2017/04/04 12:59:48
     */
    protected static interface Log<T> extends Observer<T> {
        /**
         * Validate the result values.
         * 
         * @param expected
         * @return
         */
        boolean value(Object... expected);

        /**
         * <p>
         * Validate disposer.
         * </p>
         * 
         * @return
         */
        boolean completed();

        /**
         * <p>
         * A number of message.
         * </p>
         * 
         * @return
         */
        int size();

        /**
         * <p>
         * Validate error.
         * </p>
         * 
         * @return
         */
        boolean isError();
    }

    /**
     * @version 2017/04/02 1:14:02
     */
    private static class Logger implements Log {

        private List values = new ArrayList();

        private boolean completed;

        private Throwable error;

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(Object value) {
            values.add(value);
        }

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
        public void error(Throwable error) {
            this.error = error;
        }

        /**
         * <p>
         * Validate disposer.
         * </p>
         * 
         * @return
         */
        @Override
        public boolean completed() {
            assert completed;
            return true;
        }

        /**
         * Validate the result values.
         * 
         * @param expected
         * @return
         */
        @Override
        public boolean value(Object... expected) {
            assert values.size() == expected.length;

            for (int i = 0; i < expected.length; i++) {
                assert Objects.equals(values.get(i), expected[i]);
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return values.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isError() {
            assert error != null;
            assert completed == false;
            return true;
        }
    }

    /**
     * @version 2017/04/04 12:52:06
     */
    private static class LogSet {

        Log result = new Logger();

        Log log1 = new Logger();

        Log log2 = new Logger();

        Log log3 = new Logger();

        Log log4 = new Logger();

        Log log5 = new Logger();

        Disposable disposer;
    }
}
