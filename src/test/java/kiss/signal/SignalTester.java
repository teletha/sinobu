/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import antibug.Chronus;
import kiss.Disposable;
import kiss.I;
import kiss.Observer;
import kiss.Signal;
import kiss.WiseFunction;

/**
 * @version 2018/03/19 19:45:55
 */
public class SignalTester {

    /** The complete state for {@link #emit(Object...)}. */
    protected static final Object Complete = new Object();

    /** The error state for {@link #emit(Object...)}. */
    protected static final Class Error = Error.class;

    private static final Chronus clock = new Chronus(I.class);

    /** default multiplicity */
    private static final int multiplicity = 2;

    /** Shorthand for {@link TimeUnit#MILLISECONDS}. */
    protected static final TimeUnit ms = TimeUnit.MILLISECONDS;

    /** The alias of 'this' for DSL. */
    protected final SignalTester Type = this;

    /** READ ONLY : DON'T MODIFY in test case */
    protected Log log1;

    /** READ ONLY : DON'T MODIFY in test case */
    protected Log log2;

    /** READ ONLY */
    protected final SignalSource main = new SignalSource();

    /** READ ONLY */
    protected final SignalSource other = new SignalSource();

    /** READ ONLY */
    protected final SignalSource another = new SignalSource();

    /** READ ONLY : DON'T MODIFY in test case */
    private final List<Await> awaits = new CopyOnWriteArrayList();

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
     * Create generic error {@link Function}.
     * 
     * @return
     */
    protected final <P, R> Function<P, R> runtimeExceptionFunction() {
        return e -> {
            throw new RuntimeException();
        };
    };

    /**
     * Create generic error {@link Function}.
     * 
     * @return
     */
    protected final <P, R> Function<P, R> exceptionFunction() {
        return (WiseFunction<P, R>) e -> {
            throw new Exception();
        };
    };

    /**
     * Create generic error {@link BiFunction}.
     * 
     * @return
     */
    protected final <P1, P2, R> BiFunction<P1, P2, R> errorBiFunction() {
        return (p1, p2) -> {
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
     * Create generic error {@link Enumeration}.
     * 
     * @return
     */
    protected final <T> Enumeration<T> errorEnumeration() {
        return new Enumeration<T>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasMoreElements() {
                throw new Error();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public T nextElement() {
                throw new Error();
            }
        };
    }

    /**
     * Create generic error {@link Signal}.
     * 
     * @return
     */
    protected final <T> Signal<T> errorSignal() {
        return I.signalError(new Error());
    }

    protected final Signal completeAfter(int time, TimeUnit unit) {
        Await await = new Await();
        awaits.add(await);

        return new Signal<>((observer, disposer) -> {
            I.schedule(time, unit, false, () -> {
                observer.complete();
                await.completed = true;
            });
            return disposer;
        });
    }

    protected final Log await() {
        clock.await();

        return main.result;
    }

    protected final Log await(int ms) {
        clock.freeze(ms);

        return main.result;
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
     * Shorthand method of {@link I#signal(Object...)}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(T... values) {
        return I.signal(values);
    }

    /**
     * Shorthand method of {@link I#signal(long, long, TimeUnit)}
     * 
     * @param initial
     * @param interval
     * @param unit
     * @return
     */
    protected Signal<Long> signal(long initial, int interval, TimeUnit unit) {
        return I.signal(initial, interval, unit);
    }

    /**
     * Shorthand method of {@link I#signal(Iterable)}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(Iterable<T> values) {
        return I.signal(values);
    }

    /**
     * Shorthand method of {@link I#signal(Enumeration)}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(Enumeration<T> values) {
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
        LogDelegator delegator1, delegator2;
        log1 = delegator1 = new LogDelegator();
        log2 = delegator2 = new LogDelegator();

        Signal base = signal.get();

        for (int i = 0; i < multiplicity; i++) {
            sets[i] = new LogSet();
            delegator1.log = sets[i].log1;
            delegator2.log = sets[i].log2;
            sets[i].disposer = base.map(v -> v).to(sets[i].result);
        }

        // await all awaitable signal
        for (Await awaiter : awaits) {
            awaiter.await();
        }

        log1 = I.bundle(stream(sets).map(e -> e.log1).collect(toList()));
        log2 = I.bundle(stream(sets).map(e -> e.log2).collect(toList()));
        main.result = I.bundle(stream(sets).map(e -> e.result).collect(toList()));
        main.disposers = stream(sets).map(e -> e.disposer).collect(toList());
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected <T> void monitor(Function<Signal<T>, Signal<T>> signal) {
        monitor(multiplicity, signal);
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected <T> void monitor(Class<T> type, Function<Signal<T>, Signal<T>> signal) {
        monitor(multiplicity, signal);
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected <In, Out> void monitor(Class<In> in, Class<Out> out, Function<Signal<In>, Signal<Out>> signal) {
        monitor(multiplicity, signal);
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param builder
     */
    protected <In, Out> void monitor(int multiplicity, Function<Signal<In>, Signal<Out>> builder) {
        LogSet[] sets = new LogSet[multiplicity];
        LogDelegator delegator1, delegator2;
        log1 = delegator1 = new LogDelegator();
        log2 = delegator2 = new LogDelegator();

        Signal base = builder.apply(main.signal());

        for (int i = 0; i < multiplicity; i++) {
            sets[i] = new LogSet();
            delegator1.log = sets[i].log1;
            delegator2.log = sets[i].log2;
            sets[i].disposer = base.map(v -> v).to(sets[i].result);
        }

        // await all awaitable signal
        for (Await awaiter : awaits) {
            awaiter.await();
        }

        log1 = I.bundle(stream(sets).map(e -> e.log1).collect(toList()));
        log2 = I.bundle(stream(sets).map(e -> e.log2).collect(toList()));
        main.result = I.bundle(stream(sets).map(e -> e.result).collect(toList()));
        main.disposers = stream(sets).map(e -> e.disposer).collect(toList());
    }

    /**
     * @version 2017/04/04 12:59:48
     */
    protected static interface Log<T> extends Observer<T> {
        /**
         * <p>
         * Cehck this subscription is completed or not.
         * </p>
         * 
         * @return A result.
         */
        boolean isCompleted();

        /**
         * <p>
         * Cehck this subscription is completed or not.
         * </p>
         * 
         * @return A result.
         */
        boolean isNotCompleted();

        /**
         * <p>
         * Cehck this subscription has error or not.
         * </p>
         * 
         * @return A result.
         */
        boolean isError();

        /**
         * <p>
         * Cehck this subscription has error or not.
         * </p>
         * 
         * @return A result.
         */
        boolean isNotError();

        /**
         * Validate the result values.
         * 
         * @param expected
         * @return
         */
        boolean value(Object... expected);

        /**
         * Validate the size of result values.
         * 
         * @param expected
         * @return
         */
        boolean size(int expectedSize);
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
         * {@inheritDoc}
         */
        @Override
        public boolean isCompleted() {
            return completed == true && error == null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isNotCompleted() {
            return completed == false;
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

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isNotError() {
            assert error == null;
            return true;
        }

        /**
         * Validate the result values and clear them from log.
         * 
         * @param expected
         * @return
         */
        @Override
        public boolean value(Object... expected) {
            assert values.size() == expected.length : new AssertionError(values + "  " + Arrays.toString(expected));

            for (int i = 0; i < expected.length; i++) {
                assert Objects.equals(values.get(i), expected[i]) : values + " " + Arrays.toString(expected);
            }
            values.clear();
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean size(int expectedSize) {
            assert values.size() == expectedSize : new AssertionError(values);
            values.clear();
            return true;
        }
    }

    /**
     * @version 2018/03/02 8:40:33
     */
    private static class LogDelegator implements Log {

        private Log log;

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(Object t) {
            log.accept(t);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Consumer andThen(Consumer after) {
            return log.andThen(after);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void complete() {
            log.complete();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void error(Throwable error) {
            log.error(error);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCompleted() {
            return log.isCompleted();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isNotCompleted() {
            return log.isNotCompleted();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isError() {
            return log.isError();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isNotError() {
            return log.isNotError();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean value(Object... expected) {
            return log.value(expected);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean size(int expectedSize) {
            return log.size(expectedSize);
        }
    }

    /**
     * @version 2017/04/04 12:52:06
     */
    private class LogSet {

        Log result = new Logger();

        Log log1 = new Logger();

        Log log2 = new Logger();

        Disposable disposer;
    }

    /**
     * @version 2017/04/06 12:38:00
     */
    private class Await {

        boolean completed;

        /**
         * Await completed event.
         */
        private void await() {
            int count = 0;

            while (completed == false) {
                try {
                    Thread.sleep(10);

                    if (100 < count) {
                        throw new IllegalThreadStateException("Test must execute within 1 sec.");
                    }
                } catch (InterruptedException e) {
                    throw I.quiet(e);
                }
            }
        }
    }

    /**
     * @version 2018/03/19 19:45:49
     */
    public class SignalSource {

        /** {@link Observer} manager. */
        private List<Observer> observers = new CopyOnWriteArrayList();

        /** {@link Disposable} manager. */
        private List<Disposable> disposers = new CopyOnWriteArrayList();

        private Log result;

        private boolean completed;

        private boolean errored;

        /**
         * <p>
         * Emit the specified values to the managed {@link Signal}.
         * </p>
         * 
         * @param values The values to emit.
         * @return A correspoding {@link Signal} log.
         */
        public Log emit(Object... values) {
            for (Object value : values) {
                for (Observer observer : observers) {
                    if (value == Complete) {
                        observer.complete();
                        completed = true;
                    } else if (value instanceof Class && Throwable.class.isAssignableFrom((Class) value)) {
                        observer.error(I.make((Class<Throwable>) value));
                        errored = true;
                    } else {
                        observer.accept(value);
                    }
                }
            }
            return main.result;
        }

        /**
         * Validate the result values and clear them from log.
         * 
         * @param expected
         * @return
         */
        public boolean value(Object... expected) {
            return result.value(expected);
        }

        /**
         * <p>
         * Create new managed {@link Signal}.
         * </p>
         * 
         * @return
         */
        public Signal signal() {
            return new Signal<>((observer, disposer) -> {
                observers.add(observer);
                disposers.add(disposer);

                return disposer.add(() -> {
                    observers.remove(observer);
                });
            });
        }

        /**
         * <p>
         * Check whether this {@link Signal} source is completed or not.
         * </p>
         * 
         * @return A result.
         */
        public boolean isCompleted() {
            return result == null ? completed : result.isCompleted();
        }

        /**
         * <p>
         * Check whether this {@link Signal} source is completed or not.
         * </p>
         * 
         * @return A result.
         */
        public boolean isNotCompleted() {
            return !isCompleted();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isError() {
            return result == null ? errored : result.isError();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isNotError() {
            return result == null ? !errored : result.isNotError();
        }

        /**
         * <p>
         * Check whether this {@link Signal} source is disposed or not.
         * </p>
         * 
         * @return A result.
         */
        public boolean isDisposed() {
            assert disposers.isEmpty() == false;
            for (Disposable disposable : disposers) {
                if (disposable.isDisposed() == false) {
                    return false;
                }
            }
            return true;
        }

        /**
         * <p>
         * Check whether this {@link Signal} source is disposed or not.
         * </p>
         * 
         * @return A result.
         */
        public boolean isNotDisposed() {
            return !isDisposed();
        }

        /**
         * <p>
         * Dispose all managed {@link Signal}.
         * </p>
         */
        public void dispose() {
            for (Disposable disposable : disposers) {
                disposable.dispose();
            }
        }
    }
}
