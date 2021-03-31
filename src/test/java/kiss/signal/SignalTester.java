/*
 * Copyright (C) 2021 Nameless Production Committee
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
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
import kiss.WiseBiFunction;
import kiss.WiseConsumer;
import kiss.WiseFunction;

public class SignalTester {

    /** The complete state for {@link Observer#accept(Object)} . */
    protected static final Object Complete = new Object();

    /** The error state for {@link Observer#accept(Object)}. */
    protected static final Class Error = Error.class;

    /** default multiplicity */
    private static final int defaultMultiplicity = 2;

    /** The default delay duration. */
    protected static final long delay = 150;

    /** Shorthand for {@link TimeUnit#MILLISECONDS}. */
    protected static final TimeUnit ms = TimeUnit.MILLISECONDS;

    /** The alias of 'this' for DSL. */
    protected final SignalTester Type = this;

    /** READ ONLY */
    protected int multiplicity;

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

    /** The chrono scheduler. */
    protected final Chronus scheduler = new Chronus(() -> Executors.newScheduledThreadPool(16));

    /** The log manager. */
    private Map<String, List> logs;

    /**
     * Create generic error {@link Function}.
     * 
     * @return
     */
    protected final <P, R> WiseFunction<P, R> errorFunction() {
        return e -> {
            throw new Error();
        };
    };

    /**
     * Create generic error {@link Function}.
     * 
     * @return
     */
    protected final <P> WiseFunction<P, P> errorUnaryOperator() {
        return e -> {
            throw new Error();
        };
    };

    /**
     * Create generic error {@link Function}.
     * 
     * @return
     */
    protected final <P, R> WiseFunction<P, R> runtimeExceptionFunction() {
        return e -> {
            throw new RuntimeException();
        };
    };

    /**
     * Create generic error {@link Function}.
     * 
     * @return
     */
    protected final <P, R> WiseFunction<P, R> exceptionFunction() {
        return (WiseFunction<P, R>) e -> {
            throw new Exception();
        };
    };

    /**
     * Create generic error {@link BiFunction}.
     * 
     * @return
     */
    protected final <P1, P2, R> WiseBiFunction<P1, P2, R> errorBiFunction() {
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

    /**
     * Create logging interface for the specified key. Use in {@link Signal#effect(WiseConsumer)}.
     * 
     * @param key A log key.
     * @return Loggin {@link Consumer} interface.
     */
    protected final <T> WiseConsumer<T> log(String key) {
        return e -> {
            logs.computeIfAbsent(key, k -> new ArrayList()).add(e);
        };
    }

    /**
     * Read log for te specified key.
     * 
     * @param key A log key.
     * @return The stored log or empty {@link List}.
     */
    protected final <T> List<T> checkLog(String key) {
        return logs.computeIfAbsent(key, k -> Collections.EMPTY_LIST);
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
     * Shorthand method of {@link I#schedule(long, long, TimeUnit, boolean)}
     * 
     * @param initial
     * @param interval
     * @param unit
     * @return
     */
    protected Signal<Long> signal(long initial, int interval, TimeUnit unit) {
        return I.schedule(initial, interval, unit, true);
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
     * Shorthand method of {@link I#signal(Supplier)}
     * 
     * @param values
     * @return
     */
    protected <T> Signal<T> signal(Supplier<T> values) {
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
        monitor(defaultMultiplicity, signal);
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
            logs = sets[i].logs;
            sets[i].disposer = base.map(v -> v).to(sets[i].result);
        }

        log1 = I.bundle(stream(sets).map(e -> e.log1).collect(toList()));
        log2 = I.bundle(stream(sets).map(e -> e.log2).collect(toList()));
        main.result = I.bundle(stream(sets).map(e -> e.result).collect(toList()));
        main.disposers = stream(sets).map(e -> e.disposer).collect(toList());
        this.multiplicity = multiplicity;
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected <In, Out> void monitor(Function<Signal<In>, Signal<Out>> signal) {
        monitor(defaultMultiplicity, signal);
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected <T> void monitor(Class<T> type, Function<Signal<T>, Signal<T>> signal) {
        monitor(defaultMultiplicity, signal);
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected <T> void monitor(int multiplicity, Class<T> type, Function<Signal<T>, Signal<T>> signal) {
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
        monitor(defaultMultiplicity, signal);
    }

    /**
     * <p>
     * Monitor signal to test.
     * </p>
     * 
     * @param signal
     */
    protected <In, Out> void monitor(int multiplicity, Class<In> in, Class<Out> out, Function<Signal<In>, Signal<Out>> signal) {
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
            logs = sets[i].logs;
            sets[i].disposer = base.to(sets[i].result);
        }

        log1 = I.bundle(stream(sets).map(e -> e.log1).collect(toList()));
        log2 = I.bundle(stream(sets).map(e -> e.log2).collect(toList()));
        main.result = I.bundle(stream(sets).map(e -> e.result).collect(toList()));
        main.disposers = stream(sets).map(e -> e.disposer).collect(toList());
        this.multiplicity = multiplicity;
    }

    /**
     * @version 2017/04/04 12:59:48
     */
    protected static interface Log<T> extends Observer<T>, WiseConsumer<T> {
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
         * Validate the result values. (ignore order)
         * 
         * @param expected
         * @return
         */
        boolean valueInNoParticularOrder(Object... expected);

        /**
         * Validate the result values.
         * 
         * @return A result
         */
        boolean isEmmitted();
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
        public void ACCEPT(Object value) {
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
            assert error == null : error;
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
            assert values.size() == expected.length;

            for (int i = 0; i < expected.length; i++) {
                assert Objects.equals(values.get(i), expected[i]);
            }
            values.clear();
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean valueInNoParticularOrder(Object... expected) {
            assert values.size() == expected.length;

            for (int i = 0; i < expected.length; i++) {
                assert values.contains(expected[i]);
            }
            values.clear();
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmmitted() {
            assert values.isEmpty() == false;
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
        public void ACCEPT(Object t) {
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
        public boolean valueInNoParticularOrder(Object... expected) {
            return log.valueInNoParticularOrder(expected);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmmitted() {
            return log.isEmmitted();
        }
    }

    /**
     * @version 2018/04/13 13:47:38
     */
    private class LogSet {

        Log result = new Logger();

        Log log1 = new Logger();

        Log log2 = new Logger();

        Map<String, List> logs = new HashMap();

        Disposable disposer;
    }

    /**
     * @version 2018/03/22 17:36:32
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
         * Emit the specified values to the managed {@link Signal}.
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
         * Validate the result values. (ignore order)
         * 
         * @param expected
         * @return
         */
        public boolean valueInNoParticularOrder(Object... expected) {
            return result.valueInNoParticularOrder(expected);
        }

        /**
         * Create new managed {@link Signal}.
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
         * Check whether this {@link Signal} source is completed or not.
         * 
         * @return A result.
         */
        public boolean isCompleted() {
            return result == null ? completed : result.isCompleted();
        }

        /**
         * Check whether this {@link Signal} source is completed or not.
         * 
         * @return A result.
         */
        public boolean isNotCompleted() {
            return !isCompleted();
        }

        /**
         * @return
         */
        public boolean isError() {
            return result == null ? errored : result.isError();
        }

        /**
         * @return
         */
        public boolean isNotError() {
            return result == null ? !errored : result.isNotError();
        }

        /**
         * Check whether this {@link Signal} source is disposed or not.
         * 
         * @return A result.
         */
        public boolean isDisposed() {
            if (disposers.isEmpty()) {
                return false;
            }

            for (Disposable disposable : disposers) {
                if (disposable.isDisposed() == false) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Check whether this {@link Signal} source is disposed or not.
         * 
         * @return A result.
         */
        public boolean isNotDisposed() {
            return !isDisposed();
        }

        /**
         * Dispose all managed {@link Signal}.
         */
        public void dispose() {
            for (Disposable disposable : disposers) {
                disposable.dispose();
            }
        }

        /**
         * Count the number of observers.
         * 
         * @return
         */
        public int countObservers() {
            return observers.size() / multiplicity;
        }

        /**
         * Count the number of observers.
         * 
         * @return
         */
        public boolean hasNoObserver() {
            return observers.isEmpty();
        }
    }
}