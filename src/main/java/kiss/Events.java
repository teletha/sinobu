/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @version 2014/03/11 14:52:00
 */
@SuppressWarnings("unchecked")
public class Events<V> {

    /** For reuse. */
    public static final Events NEVER = new Events<>(observer -> Procedure.Φ);

    /** For reuse. */
    private static final Predicate<Boolean> IdenticalPredicate = value -> value;

    /** The subscriber. */
    private Function<Observer<? super V>, Procedure> subscriber;

    /** The unsubscriber. */
    private Procedure unsubscriber;

    /** The common value holder. */
    private AtomicReference<V> ref;

    /** The common counter. */
    private AtomicInteger counter;

    /** The common flag. */
    private AtomicBoolean flag;

    /** The common set. */
    private Set<V> set;

    /**
     * <p>
     * Create {@link Events} with the specified subscriber {@link Function} which will be invoked
     * whenever you calls {@link #to(Observer)} related methods.
     * </p>
     * 
     * @param subscriber A subscriber {@link Function}.
     * @see #to(Observer)
     * @see #to(Consumer)
     * @see #to(Consumer, Consumer)
     * @see #to(Consumer, Consumer, Runnable)
     */
    public Events(Function<Observer<? super V>, Procedure> subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * <p>
     * Receive values from this {@link Events}.
     * </p>
     * 
     * @param next A delegator method of {@link Observer#onNext(Object)}.
     * @return Calling {@link Procedure#dispose()} will dispose this subscription.
     */
    public final Procedure to(Consumer<? super V> next) {
        return to(next, null);
    }

    /**
     * <p>
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * 
     * @param next A delegator method of {@link Observer#onNext(Object)}.
     * @param error A delegator method of {@link Observer#onError(Throwable)}.
     * @return Calling {@link Procedure#dispose()} will dispose this subscription.
     */
    public final Procedure to(Consumer<? super V> next, Consumer<Throwable> error) {
        return to(next, error, null);
    }

    /**
     * <p>
     * Receive values from this {@link Events}.
     * </p>
     * 
     * @param next A delegator method of {@link Observer#onNext(Object)}.
     * @param error A delegator method of {@link Observer#onError(Throwable)}.
     * @param complete A delegator method of {@link Observer#onCompleted()}.
     * @return Calling {@link Procedure#dispose()} will dispose this subscription.
     */
    public final Procedure to(Consumer<? super V> next, Consumer<Throwable> error, Runnable complete) {
        Agent agent = new Agent();
        agent.next = next;
        agent.error = error;
        agent.complete = complete;

        return to(agent);
    }

    /**
     * <p>
     * Receive values from this {@link Events}.
     * </p>
     * 
     * @param observer A value observer of this {@link Events}.
     * @return Calling {@link Procedure#dispose()} will dispose this subscription.
     */
    public final Procedure to(Observer<? super V> observer) {
        return unsubscriber = subscriber.apply(observer);
    }

    /**
     * <p>
     * Filters the values of an {@link Events} sequence based on the specified type.
     * </p>
     * 
     * @param type The type of result. <code>null</code> throws {@link NullPointerException}.
     * @return Chainable API.
     * @throws NullPointerException If the type is <code>null</code>.
     */
    public final <R> Events<R> as(Class<R> type) {
        Objects.nonNull(type);

        return (Events<R>) filter(type::isInstance);
    }

    /**
     * <p>
     * Indicates each value of an {@link Events} sequence into consecutive non-overlapping buffers
     * which are produced based on value count information.
     * </p>
     * 
     * @param size A length of each buffer.
     * @return Chainable API.
     */
    public final Events<V[]> buffer(int size) {
        return buffer(size, size);
    }

    /**
     * <p>
     * Indicates each values of an {@link Events} sequence into zero or more buffers which are
     * produced based on value count information.
     * </p>
     * 
     * @param size A length of each buffer. Zero or negative number are treated exactly the same way
     *            as 1.
     * @param interval A number of values to skip between creation of consecutive buffers. Zero or
     *            negative number are treated exactly the same way as 1.
     * @return Chainable API.
     */
    public final Events<V[]> buffer(int size, int interval) {
        int creationSize = 0 < size ? size : 1;
        int creationInterval = 0 < interval ? interval : 1;

        return new Events<>(observer -> {
            Deque<V> buffer = new ArrayDeque();
            AtomicInteger timing = new AtomicInteger();

            return to(value -> {
                buffer.offer(value);

                boolean validTiming = timing.incrementAndGet() == creationInterval;
                boolean validSize = buffer.size() == creationSize;

                if (validTiming && validSize) {
                    observer.onNext((V[]) buffer.toArray());
                }

                if (validTiming) {
                    timing.set(0);
                }

                if (validSize) {
                    buffer.pollFirst();
                }
            });
        });
    }

    /**
     * <p>
     * Drops values that are followed by newer values before a timeout. The timer resets on each
     * value emission.
     * </p>
     * 
     * @param time A time value. Zero or negative number will ignore this instruction.
     * @param unit A time unit. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final Events<V> debounce(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        AtomicReference<Future> latest = new AtomicReference();

        return on((observer, value) -> {
            Future future = latest.get();

            if (future != null) {
                future.cancel(true);
            }

            Runnable task = () -> {
                latest.set(null);
                observer.onNext(value);
            };
            latest.set(I.schedule(time, unit, true, task));
        });
    }

    /**
     * <p>
     * Indicates the {@link Events} sequence by due time with the specified source and time.
     * </p>
     * 
     * @param time The absolute time used to shift the {@link Events} sequence. Zero or negative
     *            number will ignore this instruction.
     * @param unit A unit of time for the specified time. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> delay(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        return on((observer, value) -> {
            I.schedule(time, unit, false, () -> {
                observer.onNext(value);
            });
        });
    }

    /**
     * <p>
     * Returns an {@link Events} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     * </p>
     * 
     * @return Chainable API.
     */
    public final Events<V> diff() {
        return new Events<>(observer -> {
            ref = new AtomicReference();

            return to(value -> {
                V prev = ref.getAndSet(value);

                if (!Objects.equals(prev, value)) {
                    observer.onNext(value);
                }
            });
        });
    }

    /**
     * <p>
     * Returns an {@link Events} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     * </p>
     * 
     * @return Chainable API.
     */
    public final Events<V> distinct() {
        return new Events<>(observer -> {
            set = new HashSet();

            return to(value -> {
                if (set.add(value)) {
                    observer.onNext(value);
                }
            });
        });
    }

    /**
     * <p>
     * Returns an {@link Events} consisting of the values of this {@link Events} that match the
     * given predicate.
     * </p>
     * 
     * @param predicate A function that evaluates the values emitted by the source {@link Events},
     *            returning {@code true} if they pass the filter. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> filter(Predicate<? super V> predicate) {
        // ignore invalid parameters
        if (predicate == null) {
            return this;
        }

        return on((observer, value) -> {
            if (predicate.test(value)) {
                observer.onNext(value);
            }
        });
    }

    /**
     * <p>
     * Returns an {@link Events} that applies the given constant to each item emitted by an
     * {@link Events} and emits the result.
     * </p>
     * 
     * @param constant A constant to apply to each value emitted by this {@link Events}.
     * @return Chainable API.
     */
    public final <R> Events<R> map(R constant) {
        return new Events<>(observer -> {
            return to(value -> {
                observer.onNext(constant);
            });
        });
    }

    /**
     * <p>
     * Returns an {@link Events} that applies the given function to each value emitted by an
     * {@link Events} and emits the result.
     * </p>
     * 
     * @param converter A converter function to apply to each value emitted by this {@link Events} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <R> Events<R> map(Function<? super V, R> converter) {
        // ignore invalid parameters
        if (converter == null) {
            return (Events<R>) this;
        }

        return new Events<>(observer -> {
            return to(value -> {
                observer.onNext(converter.apply(value));
            });
        });
    }

    /**
     * <p>
     * Flattens a sequence of {@link Events} emitted by an {@link Events} into one {@link Events},
     * without any transformation.
     * </p>
     * 
     * @param other A target {@link Events} to merge. <code>null</code> will be ignroed.
     * @return Chainable API.
     */
    public final Events<V> merge(Events<? extends V> other) {
        return merge(Collections.singletonList(other));
    }

    /**
     * <p>
     * Flattens a sequence of {@link Events} emitted by an {@link Events} into one {@link Events},
     * without any transformation.
     * </p>
     * 
     * @param others A target {@link Events} set to merge. <code>null</code> will be ignroed.
     * @return Chainable API.
     */
    public final Events<V> merge(Iterable<? extends Events<? extends V>> others) {
        // ignore invalid parameters
        if (others == null) {
            return this;
        }

        return new Events<>(observer -> {
            Procedure disposable = to(observer);

            for (Events<? extends V> other : others) {
                if (other != null) {
                    disposable = disposable.and(other.to(observer));
                }
            }
            return disposable;
        });
    }

    /**
     * <p>
     * Invokes an action for each value in the {@link Events} sequence.
     * </p>
     * 
     * @param next An action to invoke for each value in the {@link Events} sequence.
     * @return Chainable API.
     */
    public final Events<V> on(BiConsumer<Observer<? super V>, V> next) {
        // ignore invalid parameters
        if (next == null) {
            return this;
        }

        return new Events<>(observer -> {
            Agent<V> agent = new Agent();
            agent.observer = observer;
            agent.next = value -> {
                next.accept(observer, value);
            };
            return to(agent);
        });
    }

    /**
     * <p>
     * Generates an {@link Events} sequence that repeats the given value infinitely.
     * </p>
     * 
     * @return Chainable API.
     */
    public final Events<V> repeat() {
        return new Events<>(observer -> {
            Agent agent = new Agent();
            agent.observer = observer;
            agent.complete = () -> {
                observer.onCompleted();
                to(agent);
            };
            return to(agent);
        });
    }

    /**
     * <p>
     * Generates an {@link Events} sequence that repeats the given value finitely.
     * </p>
     * 
     * @param count A number of repeat. Zero or negative number will ignore this instruction.
     * @return Chainable API.
     */
    public final Events<V> repeat(int count) {
        // ignore invalid parameter
        if (count < 1) {
            return this;
        }

        AtomicInteger repeat = new AtomicInteger(count);

        return new Events<>(observer -> {
            Agent agent = new Agent();
            agent.observer = observer;
            agent.complete = () -> {
                if (repeat.decrementAndGet() == 0) {
                    unsubscriber.call();
                } else {
                    unsubscriber = unsubscriber.and(to(agent));
                }
            };
            return to(agent);
        });
    }

    /**
     * <p>
     * Bypasses a specified number of values in an {@link Events} sequence and then returns the
     * remaining values.
     * </p>
     * 
     * @param count A number of values to skip. Zero or negative number will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> skip(int count) {
        // ignore invalid parameter
        if (count <= 0) {
            return this;
        }

        return new Events<>(observer -> {
            counter = new AtomicInteger();

            return to(value -> {
                if (count < counter.incrementAndGet()) {
                    observer.onNext(value);
                }
            });
        });
    }

    /**
     * <p>
     * Returns the values from the source {@link Events} sequence only after the other
     * {@link Events} sequence produces a value.
     * </p>
     * 
     * @param predicate An {@link Events} sequence that triggers propagation of values of the source
     *            sequence. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final Events<V> skipUntil(Events predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Events<>(observer -> {
            flag = new AtomicBoolean();

            return to(value -> {
                if (flag.get()) {
                    observer.onNext(value);
                }
            }).and(predicate.to(value -> {
                flag.set(true);
            }));
        });
    }

    /**
     * <p>
     * Returns the values from the source {@link Events} sequence only after the other
     * {@link Events} sequence produces a value.
     * </p>
     * 
     * @param predicate An {@link Events} sequence that triggers propagation of values of the source
     *            sequence. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <T> Events<V> skipUntil(Predicate<V> predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Events<>(observer -> {
            flag = new AtomicBoolean();

            return to(value -> {
                if (flag.get()) {
                    observer.onNext(value);
                } else if (predicate.test(value)) {
                    flag.set(true);
                    observer.onNext(value);
                }
            });
        });
    }

    /**
     * <p>
     * Returns a specified number of contiguous values from the start of an {@link Events} sequence.
     * </p>
     * 
     * @param count A number of values to emit. Zero or negative number will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> take(int count) {
        // ignore invalid parameter
        if (count <= 0) {
            return this;
        }

        return new Events<>(observer -> {
            counter = new AtomicInteger(count);

            return to(value -> {
                long current = counter.decrementAndGet();

                if (0 <= current) {
                    observer.onNext(value);

                    if (0 == current) {
                        observer.onCompleted();
                        unsubscriber.call();
                    }
                }
            });
        });
    }

    /**
     * <p>
     * Returns the values from the source {@link Events} sequence until the other {@link Events}
     * sequence produces a value.
     * </p>
     * 
     * @param predicate An {@link Events} sequence that terminates propagation of values of the
     *            source sequence. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final Events<V> takeUntil(Events predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Events<>(observer -> {
            return unsubscriber = to(observer).and(predicate.to(value -> {
                observer.onCompleted();
                unsubscriber.call();
            }));
        });
    }

    /**
     * <p>
     * Returns the values from the source {@link Events} sequence until the other {@link Events}
     * sequence produces a value.
     * </p>
     * 
     * @param predicate An {@link Events} sequence that terminates propagation of values of the
     *            source sequence. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <T> Events<V> takeUntil(Predicate<V> predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return on((observer, value) -> {
            if (predicate.test(value)) {
                observer.onNext(value);
                observer.onCompleted();
                unsubscriber.call();
            } else {
                observer.onNext(value);
            }
        });
    }

    /**
     * <p>
     * Throttles by skipping values until "skipDuration" passes and then emits the next received
     * value.
     * </p>
     * <p>
     * Ignores the values from an {@link Events} sequence which are followed by another value before
     * due time with the specified source and time.
     * </p>
     * 
     * @param time Time to wait before sending another item after emitting the last item. Zero or
     *            negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> throttle(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        AtomicLong latest = new AtomicLong();
        long delay = unit.toMillis(time);

        return filter(value -> {
            long now = System.currentTimeMillis();
            return latest.getAndSet(now) + delay <= now;
        });
    }

    /**
     * <p>
     * Create an {@link Events} that emits true if all specified observables emit true as latest
     * event.
     * </p>
     * 
     * @param observables A list of target {@link Events} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static Events<Boolean> all(Events<Boolean>... observables) {
        return all(IdenticalPredicate, observables);
    }

    /**
     * <p>
     * Create an {@link Events} that emits true if all specified observables emit true as latest
     * event.
     * </p>
     * 
     * @param predicate A test function.
     * @param observables A list of target {@link Events} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static <V> Events<Boolean> all(Predicate<V> predicate, Events<V>... observables) {
        return condition(values -> {
            for (boolean value : values) {
                if (value) {
                    return false;
                }
            }
            return true;
        }, predicate, observables);
    }

    /**
     * <p>
     * Create an {@link Events} that emits true if any specified observable emits true as latest
     * event.
     * </p>
     * 
     * @param observables A list of target {@link Events} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static Events<Boolean> any(Events<Boolean>... observables) {
        return any(IdenticalPredicate, observables);
    }

    /**
     * <p>
     * Create an {@link Events} that emits true if any specified observable emits true as latest
     * event.
     * </p>
     * 
     * @param predicate A test function.
     * @param observables A list of target {@link Events} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static <V> Events<Boolean> any(Predicate<V> predicate, Events<V>... observables) {
        return condition(values -> {
            for (boolean value : values) {
                if (!value) {
                    return true;
                }
            }
            return false;
        }, predicate, observables);
    }

    /**
     * <p>
     * Create an {@link Events} that emits true if all specified observables emit false as latest
     * event.
     * </p>
     * 
     * @param observables A list of target {@link Events} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static Events<Boolean> none(Events<Boolean>... observables) {
        return none(IdenticalPredicate, observables);
    }

    /**
     * <p>
     * Create an {@link Events} that emits true if all specified observables emit false as latest
     * event.
     * </p>
     * 
     * @param predicate A test function.
     * @param observables A list of target {@link Events} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static <V> Events<Boolean> none(Predicate<V> predicate, Events<V>... observables) {
        return condition(values -> {
            for (boolean value : values) {
                if (!value) {
                    return false;
                }
            }
            return true;
        }, predicate, observables);
    }

    /**
     * <p>
     * Helper method to merge the test result of each {@link Events}.
     * </p>
     * 
     * @param condition A test function for result.
     * @param predicate A test function for each {@link Events}.
     * @param observables A list of target {@link Events} to test.
     * @return Chainable API.
     */
    private static <V> Events<Boolean> condition(Predicate<boolean[]> condition, Predicate<V> predicate, Events<V>... observables) {
        if (observables == null || observables.length == 0 || predicate == null) {
            return NEVER;
        }

        return new Events<>(observer -> {
            Procedure base = null;
            boolean[] conditions = new boolean[observables.length];

            for (int i = 0; i < observables.length; i++) {
                int index = i;
                Procedure disposable = observables[index].to(value -> {
                    conditions[index] = !predicate.test(value);

                    observer.onNext(condition.test(conditions));
                });
                base = i == 0 ? disposable : base.and(disposable);
            }
            return base;
        });
    }
}
