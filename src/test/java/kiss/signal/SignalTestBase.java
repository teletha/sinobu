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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.junit.Before;

import kiss.Disposable;
import kiss.I;
import kiss.Observer;
import kiss.Signal;

/**
 * @version 2017/04/01 21:46:44
 */
public class SignalTestBase {

    protected final Log result = new Log();

    protected final Log log1 = new Log();

    protected final Log log2 = new Log();

    protected final Log log3 = new Log();

    protected final Log log4 = new Log();

    protected final Log log5 = new Log();

    @Before
    public void setup() {
        result.reset();
        log1.reset();
        log2.reset();
        log3.reset();
        log4.reset();
        log5.reset();
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
     * Shorthand method of {@link Collections#enumeration(java.util.Collection)}.
     * 
     * @param values
     * @return
     */
    protected <T> Enumeration<T> enume(T... values) {
        return Collections.enumeration(I.list(values));
    }

    /**
     * Shorthand method of {@link Signal#from}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(T... values) {
        return Signal.from(values);
    }

    /**
     * Shorthand method of {@link Signal#from}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(Iterable values) {
        return Signal.from(values);
    }

    /**
     * Shorthand method of {@link Signal#from}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(Enumeration values) {
        return Signal.from(values);
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected void monitor(Supplier<Signal> signal) {
        result.disposable = signal.get().to(result);
    }

    /**
     * @version 2017/04/02 1:14:02
     */
    protected static class Log implements Observer {

        private Disposable disposable;

        private List values = new ArrayList();

        private boolean completed;

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
        }

        /**
         * <p>
         * Validate disposer.
         * </p>
         * 
         * @return
         */
        protected final boolean completed() {
            assert completed;
            return true;
        }

        /**
         * <p>
         * Validate disposer.
         * </p>
         * 
         * @return
         */
        protected final boolean disposed() {
            assert disposable.isDisposed();
            return true;
        }

        /**
         * <p>
         * Validate disposer.
         * </p>
         * 
         * @return
         */
        protected final boolean completedAndDisposed() {
            return completed() && disposed();
        }

        /**
         * Validate the result values.
         * 
         * @param expected
         * @return
         */
        protected final boolean value(Object... expected) {
            assert values.size() == expected.length;

            for (int i = 0; i < expected.length; i++) {
                assert Objects.equals(values.get(i), expected[i]);
            }
            return true;
        }

        private void reset() {
            disposable = null;
            values.clear();
            completed = false;
        }
    }
}
