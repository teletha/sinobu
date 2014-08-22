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
public class Event<V> {

    /** For reuse. */
    public static final Event NEVER = new Event<>(observer -> Disposable.Φ);

    /** For reuse. */
    private static final Predicate<Boolean> IdenticalPredicate = value -> value;

    /** The subscriber. */
    private Function<Observer<? super V>, Disposable> subscriber;

    /** The unsubscriber. */
    private Disposable unsubscriber;

    /** The observer. */
    private Agent<V> observer;

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
     * Create {@link Event} with the specified subscriber {@link Function} which will be invoked
     * whenever you calls {@link #to(Observer)} related methods.
     * </p>
     * 
     * @param subscriber A subscriber {@link Function}.
     * @see #to(Observer)
     * @see #to(Consumer)
     * @see #to(Consumer, Consumer)
     * @see #to(Consumer, Consumer, Runnable)
     */
    public Event(Function<Observer<? super V>, Disposable> subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * <p>
     * Receive values from this {@link Event}.
     * </p>
     * 
     * @param next A delegator method of {@link Observer#onNext(Object)}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Consumer<? super V> next) {
        return to(next, null);
    }

    /**
     * <p>
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     * 
     * @param next A delegator method of {@link Observer#onNext(Object)}.
     * @param error A delegator method of {@link Observer#onError(Throwable)}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Consumer<? super V> next, Consumer<Throwable> error) {
        return to(next, error, null);
    }

    /**
     * <p>
     * Receive values from this {@link Event}.
     * </p>
     * 
     * @param next A delegator method of {@link Observer#onNext(Object)}.
     * @param error A delegator method of {@link Observer#onError(Throwable)}.
     * @param complete A delegator method of {@link Observer#onCompleted()}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Consumer<? super V> next, Consumer<Throwable> error, Runnable complete) {
        Agent agent = new Agent();
        agent.next = next;
        agent.error = error;
        agent.complete = complete;

        return to(agent);
    }

    /**
     * <p>
     * Receive values from this {@link Event}.
     * </p>
     * 
     * @param observer A value observer of this {@link Event}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Observer<? super V> observer) {
        if (observer instanceof Agent) {
            this.observer = (Agent) observer;
        } else {
            this.observer = new Agent();
            this.observer.observer = observer;
        }
        return unsubscriber = subscriber.apply(this.observer);
    }

    /**
     * <p>
     * Filters the values of an {@link Event} sequence based on the specified type.
     * </p>
     * 
     * @param type The type of result. <code>null</code> throws {@link NullPointerException}.
     * @return Chainable API.
     * @throws NullPointerException If the type is <code>null</code>.
     */
    public final <R> Event<R> as(Class<R> type) {
        Objects.nonNull(type);

        return (Event<R>) filter(type::isInstance);
    }

    /**
     * <p>
     * Indicates each value of an {@link Event} sequence into consecutive non-overlapping buffers
     * which are produced based on value count information.
     * </p>
     * 
     * @param size A length of each buffer.
     * @return Chainable API.
     */
    public final Event<V[]> buffer(int size) {
        return buffer(size, size);
    }

    /**
     * <p>
     * Indicates each values of an {@link Event} sequence into zero or more buffers which are
     * produced based on value count information.
     * </p>
     * 
     * @param size A length of each buffer. Zero or negative number are treated exactly the same way
     *            as 1.
     * @param interval A number of values to skip between creation of consecutive buffers. Zero or
     *            negative number are treated exactly the same way as 1.
     * @return Chainable API.
     */
    public final Event<V[]> buffer(int size, int interval) {
        int creationSize = 0 < size ? size : 1;
        int creationInterval = 0 < interval ? interval : 1;

        return new Event<>(observer -> {
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
    public final Event<V> debounce(long time, TimeUnit unit) {
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
     * Indicates the {@link Event} sequence by due time with the specified source and time.
     * </p>
     * 
     * @param time The absolute time used to shift the {@link Event} sequence. Zero or negative
     *            number will ignore this instruction.
     * @param unit A unit of time for the specified time. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Event<V> delay(long time, TimeUnit unit) {
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
     * Returns an {@link Event} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     * </p>
     * 
     * @return Chainable API.
     */
    public final Event<V> diff() {
        return new Event<>(observer -> {
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
     * Returns an {@link Event} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     * </p>
     * 
     * @return Chainable API.
     */
    public final Event<V> distinct() {
        return new Event<>(observer -> {
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
     * Returns an {@link Event} consisting of the values of this {@link Event} that match the given
     * predicate.
     * </p>
     * 
     * @param predicate A function that evaluates the values emitted by the source {@link Event},
     *            returning {@code true} if they pass the filter. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Event<V> filter(Predicate<? super V> predicate) {
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
     * Returns an {@link Event} that applies the given {@link Predicate} function to each value
     * emitted by an {@link Event} and emits the result.
     * </p>
     * 
     * @param converter A converter function to apply to each value emitted by this {@link Event} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final Event<Boolean> is(Predicate<? super V> converter) {
        // ignore invalid parameters
        if (converter == null) {
            return NEVER;
        }

        return map(value -> converter.test(value));
    }

    /**
     * <p>
     * Returns an {@link Event} that applies the given constant to each item emitted by an
     * {@link Event} and emits the result.
     * </p>
     * 
     * @param constant A constant to apply to each value emitted by this {@link Event}.
     * @return Chainable API.
     */
    public final <R> Event<R> map(R constant) {
        return new Event<>(observer -> {
            return to(value -> {
                observer.onNext(constant);
            });
        });
    }

    /**
     * <p>
     * Returns an {@link Event} that applies the given function to each value emitted by an
     * {@link Event} and emits the result.
     * </p>
     * 
     * @param converter A converter function to apply to each value emitted by this {@link Event} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <R> Event<R> map(Function<? super V, R> converter) {
        // ignore invalid parameters
        if (converter == null) {
            return (Event<R>) this;
        }

        return new Event<>(observer -> {
            return to(value -> {
                observer.onNext(converter.apply(value));
            });
        });
    }

    /**
     * <p>
     * Flattens a sequence of {@link Event} emitted by an {@link Event} into one {@link Event},
     * without any transformation.
     * </p>
     * 
     * @param other A target {@link Event} to merge. <code>null</code> will be ignroed.
     * @return Chainable API.
     */
    public final Event<V> merge(Event<? extends V> other) {
        return merge(Collections.singletonList(other));
    }

    /**
     * <p>
     * Flattens a sequence of {@link Event} emitted by an {@link Event} into one {@link Event},
     * without any transformation.
     * </p>
     * 
     * @param others A target {@link Event} set to merge. <code>null</code> will be ignroed.
     * @return Chainable API.
     */
    public final Event<V> merge(Iterable<? extends Event<? extends V>> others) {
        // ignore invalid parameters
        if (others == null) {
            return this;
        }

        return new Event<>(observer -> {
            Disposable disposable = to(observer);

            for (Event<? extends V> other : others) {
                if (other != null) {
                    disposable = disposable.and(other.to(observer));
                }
            }
            return disposable;
        });
    }

    /**
     * <p>
     * Invokes an action for each value in the {@link Event} sequence.
     * </p>
     * 
     * @param next An action to invoke for each value in the {@link Event} sequence.
     * @return Chainable API.
     */
    public final Event<V> on(BiConsumer<Observer<? super V>, V> next) {
        // ignore invalid parameters
        if (next == null) {
            return this;
        }

        return new Event<>(observer -> {
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
     * Generates an {@link Event} sequence that repeats the given value infinitely.
     * </p>
     * 
     * @return Chainable API.
     */
    public final Event<V> repeat() {
        return new Event<>(observer -> {
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
     * Generates an {@link Event} sequence that repeats the given value finitely.
     * </p>
     * 
     * @param count A number of repeat. Zero or negative number will ignore this instruction.
     * @return Chainable API.
     */
    public final Event<V> repeat(int count) {
        // ignore invalid parameter
        if (count < 1) {
            return this;
        }

        AtomicInteger repeat = new AtomicInteger(count);

        return new Event<>(observer -> {
            Agent agent = new Agent();
            agent.observer = observer;
            agent.complete = () -> {
                if (repeat.decrementAndGet() == 0) {
                    unsubscriber.dispose();
                } else {
                    unsubscriber = unsubscriber.and(to(agent));
                }
            };
            return to(agent);
        });
    }

    /**
     * <p>
     * Bypasses a specified number of values in an {@link Event} sequence and then returns the
     * remaining values.
     * </p>
     * 
     * @param count A number of values to skip. Zero or negative number will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Event<V> skip(int count) {
        // ignore invalid parameter
        if (count <= 0) {
            return this;
        }

        return new Event<>(observer -> {
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
     * Returns the values from the source {@link Event} sequence only after the other {@link Event}
     * sequence produces a value.
     * </p>
     * 
     * @param predicate An {@link Event} sequence that triggers propagation of values of the source
     *            sequence. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final Event<V> skipUntil(Event predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Event<>(observer -> {
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
     * Returns the values from the source {@link Event} sequence only after the other {@link Event}
     * sequence produces a value.
     * </p>
     * 
     * @param predicate An {@link Event} sequence that triggers propagation of values of the source
     *            sequence. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <T> Event<V> skipUntil(Predicate<V> predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Event<>(observer -> {
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
     * Returns a specified number of contiguous values from the start of an {@link Event} sequence.
     * </p>
     * 
     * @param count A number of values to emit. Zero or negative number will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Event<V> take(int count) {
        // ignore invalid parameter
        if (count <= 0) {
            return this;
        }

        return new Event<>(observer -> {
            counter = new AtomicInteger(count);

            return to(value -> {
                long current = counter.decrementAndGet();

                if (0 <= current) {
                    observer.onNext(value);

                    if (0 == current) {
                        observer.onCompleted();
                        unsubscriber.dispose();
                    }
                }
            });
        });
    }

    /**
     * <p>
     * Returns the values from the source {@link Event} sequence until the other {@link Event}
     * sequence produces a value.
     * </p>
     * 
     * @param predicate An {@link Event} sequence that terminates propagation of values of the
     *            source sequence. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final Event<V> takeUntil(Event predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Event<>(observer -> {
            return unsubscriber = to(observer).and(predicate.to(value -> {
                observer.onCompleted();
                unsubscriber.dispose();
            }));
        });
    }

    /**
     * <p>
     * Returns the values from the source {@link Event} sequence until the other {@link Event}
     * sequence produces a value.
     * </p>
     * 
     * @param predicate An {@link Event} sequence that terminates propagation of values of the
     *            source sequence. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <T> Event<V> takeUntil(Predicate<V> predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return on((observer, value) -> {
            if (predicate.test(value)) {
                observer.onNext(value);
                observer.onCompleted();
                unsubscriber.dispose();
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
     * Ignores the values from an {@link Event} sequence which are followed by another value before
     * due time with the specified source and time.
     * </p>
     * 
     * @param time Time to wait before sending another item after emitting the last item. Zero or
     *            negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Event<V> throttle(long time, TimeUnit unit) {
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
     * Retrieve the current value.
     * </p>
     * 
     * @return A current value.
     */
    public final V value() {
        return observer.object;
    }

    /**
     * <p>
     * Retrieve the current value.
     * </p>
     * 
     * @return A current value.
     */
    public final void value(V value) {
        observer.onNext(value);
    }

    /**
     * <p>
     * Returns an {@link Event} that applies the given constant to each item emitted by an
     * {@link Event} and emits the result.
     * </p>
     * 
     * @param constant A constant to apply to each value emitted by this {@link Event}.
     * @return Chainable API.
     */
    public final <R> Event<R> map3(R constant) {
        return new Event<>(observer -> {
            return to(value -> {
                observer.onNext(constant);
            });
        });
    }

    /**
     * <p>
     * Create an {@link Event} that emits true if all specified observables emit true as latest
     * event.
     * </p>
     * 
     * @param observables A list of target {@link Event} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static Event<Boolean> all(Event<Boolean>... observables) {
        return all(IdenticalPredicate, observables);
    }

    /**
     * <p>
     * Create an {@link Event} that emits true if all specified observables emit true as latest
     * event.
     * </p>
     * 
     * @param predicate A test function.
     * @param observables A list of target {@link Event} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static <V> Event<Boolean> all(Predicate<V> predicate, Event<V>... observables) {
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
     * Create an {@link Event} that emits true if any specified observable emits true as latest
     * event.
     * </p>
     * 
     * @param observables A list of target {@link Event} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static Event<Boolean> any(Event<Boolean>... observables) {
        return any(IdenticalPredicate, observables);
    }

    /**
     * <p>
     * Create an {@link Event} that emits true if any specified observable emits true as latest
     * event.
     * </p>
     * 
     * @param predicate A test function.
     * @param observables A list of target {@link Event} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static <V> Event<Boolean> any(Predicate<V> predicate, Event<V>... observables) {
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
     * Create an {@link Event} that emits true if all specified observables emit false as latest
     * event.
     * </p>
     * 
     * @param observables A list of target {@link Event} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static Event<Boolean> none(Event<Boolean>... observables) {
        return none(IdenticalPredicate, observables);
    }

    /**
     * <p>
     * Create an {@link Event} that emits true if all specified observables emit false as latest
     * event.
     * </p>
     * 
     * @param predicate A test function.
     * @param observables A list of target {@link Event} to test.
     * @return Chainable API.
     */
    @SafeVarargs
    public static <V> Event<Boolean> none(Predicate<V> predicate, Event<V>... observables) {
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
     * Helper method to merge the test result of each {@link Event}.
     * </p>
     * 
     * @param condition A test function for result.
     * @param predicate A test function for each {@link Event}.
     * @param observables A list of target {@link Event} to test.
     * @return Chainable API.
     */
    private static <V> Event<Boolean> condition(Predicate<boolean[]> condition, Predicate<V> predicate, Event<V>... observables) {
        if (observables == null || observables.length == 0 || predicate == null) {
            return NEVER;
        }

        return new Event<>(observer -> {
            Disposable base = null;
            boolean[] conditions = new boolean[observables.length];

            for (int i = 0; i < observables.length; i++) {
                int index = i;
                Disposable disposable = observables[index].to(value -> {
                    conditions[index] = !predicate.test(value);

                    observer.onNext(condition.test(conditions));
                });
                base = i == 0 ? disposable : base.and(disposable);
            }
            return base;
        });
    }
}
