/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;

import kiss.signal.StartWithTest;

/**
 * @version 2017/04/04 11:25:38
 */
public final class Signal<V> {

    /**
     * For reuse.
     */
    public static final Signal EMPTY = new Signal<>((observer, disposer) -> {
        observer.complete();
        return disposer;
    });

    /**
     * For reuse.
     */
    public static final Signal NEVER = new Signal<>((observer, disposer) -> disposer);

    /**
     * For reuse.
     */
    private static final Object UNDEFINED = new Object();

    /**
     * The subscriber.
     */
    private final BiFunction<Observer<? super V>, Disposable, Disposable> subscriber;

    /**
     * <p>
     * Create {@link Signal} with the specified subscriber {@link Collection} which will be invoked
     * whenever you calls {@link #to(Observer)} related methods.
     * </p>
     *
     * @param observers A subscriber {@link Function}.
     * @see #to(Observer)
     * @see #to(Consumer, Consumer)
     * @see #to(Consumer, Consumer, Runnable)
     */
    public Signal(Collection<Observer<? super V>> observers) {
        this((observer, disposable) -> {
            observers.add(observer);

            return () -> observers.remove(observer);
        });
    }

    /**
     * <p>
     * Create {@link Signal} with the specified subscriber {@link BiFunction} which will be invoked
     * whenever you calls {@link #to(Observer)} related methods.
     * </p>
     *
     * @param subscriber A subscriber {@link Function}.
     * @see #to(Observer)
     * @see #to(Consumer, Consumer)
     * @see #to(Consumer, Consumer, Runnable)
     */
    public Signal(BiFunction<Observer<? super V>, Disposable, Disposable> subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * <p>
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Runnable next) {
        return to(v -> next.run(), null, (Runnable) null);
    }

    /**
     * <p>
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Consumer<? super V> next) {
        return to(next, null, null);
    }

    /**
     * <p>
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @param error A delegator method of {@link Observer#error(Throwable)}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Consumer<? super V> next, Consumer<Throwable> error) {
        return to(next, error, null);
    }

    /**
     * <p>
     * Receive values from this {@link Signal}.
     * </p>
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @param error A delegator method of {@link Observer#error(Throwable)}.
     * @param complete A delegator method of {@link Observer#complete()}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Consumer<? super V> next, Consumer<Throwable> error, Runnable complete) {
        return to(next, error, complete, null);
    }

    /**
     * <p>
     * Receive values from this {@link Signal}.
     * </p>
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @param error A delegator method of {@link Observer#error(Throwable)}.
     * @param complete A delegator method of {@link Observer#complete()}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Consumer<? super V> next, Consumer<Throwable> error, Runnable complete, Disposable disposer) {
        Subscriber subscriber = new Subscriber();
        subscriber.next = next;
        subscriber.error = error;
        subscriber.complete = complete;

        return to(subscriber, disposer);
    }

    /**
     * <p>
     * Receive values from this {@link Signal}.
     * </p>
     *
     * @param observer A value observer of this {@link Signal}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Observer<? super V> observer) {
        return to(observer, (Disposable) null);
    }

    /**
     * <p>
     * Receive values from this {@link Signal}.
     * </p>
     *
     * @param observer A value observer of this {@link Signal}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    private final Disposable to(Observer<? super V> observer, Disposable disposer) {
        if (disposer == null) {
            disposer = Disposable.empty();
        }

        if (observer instanceof Subscriber == false) {
            Subscriber subscriber = new Subscriber();
            subscriber.observer = observer;
            observer = subscriber;
        }

        if (disposer instanceof Subscriber == false) {
            throw new Error(disposer.getClass().getName());
        }

        try {
            return subscriber.apply(observer, disposer);
        } catch (Throwable e) {
            observer.error(e);
            return disposer;
        }
    }

    /**
     * <p>
     * Receive values as {@link Variable} from this {@link Signal}.
     * </p>
     *
     * @return A {@link Variable} as value receiver.
     */
    public final Variable<V> to() {
        Variable<V> variable = Variable.empty();
        to(variable::set);
        return variable;
    }

    /**
     * <p>
     * Receive values as {@link Collection} type from this {@link Signal}.
     * </p>
     *
     * @return A {@link Collection} as value receiver.
     */
    public final <C extends Collection<V>> C to(Class<C> type) {
        return to(type, Collection::add);
    }

    /**
     * <p>
     * Receive values from this {@link Signal}.
     * </p>
     *
     * @param type A value receiver type.
     * @param assigner A value assigner.
     * @return A value receiver.
     */
    public final <R> R to(Class<R> type, BiConsumer<R, V> assigner) {
        // value receiver
        R receiver = I.make(type);

        // start receiving values
        to(v -> assigner.accept(receiver, v));

        // API definition
        return receiver;
    }

    /**
     * <p>
     * Receive values as {@link Set} from this {@link Signal}. Each value alternates between In and
     * Out.
     * </p>
     *
     * @return A {@link Set} as value receiver.
     */
    public final Set<V> toAlternate() {
        return to(Set.class, (set, value) -> {
            if (!set.add(value)) {
                set.remove(value);
            }
        });
    }

    /**
     * <p>
     * Receive values as boolean {@link Variable} from this {@link Signal}. Each value alternates
     * between true and false.
     * </p>
     *
     * @return A boolean {@link Variable} as value receiver.
     */
    public final Variable<Boolean> toBinary() {
        // value receiver
        Variable<Boolean> receiver = Variable.of(false);

        // start receiving values
        to(v -> receiver.set(!receiver.get()));

        // API definition
        return receiver;
    }

    /**
     * <p>
     * Receive values as {@link List} from this {@link Signal}.
     * </p>
     *
     * @return A {@link List} as value receiver.
     */
    public final List<V> toList() {
        return to(List.class);
    }

    /**
     * <p>
     * Receive values as {@link Map} from this {@link Signal}.
     * </p>
     * 
     * @param keyGenerator A {@link Map} key generator.
     * @return A {@link Map} as value receiver.
     */
    public final <Key> Map<Key, V> toMap(Function<V, Key> keyGenerator) {
        return toMap(keyGenerator, Function.identity());
    }

    /**
     * <p>
     * Receive values as {@link Map} from this {@link Signal}.
     * </p>
     * 
     * @param keyGenerator A {@link Map} key generator.
     * @param valueGenerator A {@link Map} value generator.
     * @return A {@link Map} as value receiver.
     */
    public final <Key, Value> Map<Key, Value> toMap(Function<V, Key> keyGenerator, Function<V, Value> valueGenerator) {
        return to(Map.class, (map, v) -> map.put(keyGenerator.apply(v), valueGenerator.apply(v)));
    }

    /**
     * <p>
     * Receive values as {@link Set} from this {@link Signal}.
     * </p>
     *
     * @return A {@link Set} as value receiver.
     */
    public final Set<V> toSet() {
        return to(Set.class);
    }

    /**
     * <p>
     * Receive values as {@link Table} from this {@link Signal}.
     * </p>
     * 
     * @param keyGenerator A {@link Table} key generator.
     * @return A {@link Table} as value receiver.
     */
    public final <Key> Table<Key, V> toTable(Function<V, Key> keyGenerator) {
        return toTable(keyGenerator, Function.identity());
    }

    /**
     * <p>
     * Receive values as {@link Table} from this {@link Signal}.
     * </p>
     *
     * @param keyGenerator A {@link Table} key generator.
     * @param valueGenerator A {@link Table} value generator.
     * @return A {@link Table} as value receiver.
     */
    public final <Key, Value> Table<Key, Value> toTable(Function<V, Key> keyGenerator, Function<V, Value> valueGenerator) {
        return to(Table.class, (table, v) -> table.push(keyGenerator.apply(v), valueGenerator.apply(v)));
    }

    /**
     * <p>
     * Filters the values of an {@link Signal} sequence based on the specified type.
     * </p>
     *
     * @param type The type of result. <code>null</code> throws {@link NullPointerException}.
     * @return Chainable API.
     * @throws NullPointerException If the type is <code>null</code>.
     */
    public final <R> Signal<R> as(Class<R> type) {
        return (Signal<R>) take(type::isInstance);
    }

    /**
     * <p>
     * Indicates each value of an {@link Signal} sequence into consecutive non-overlapping buffers
     * which are produced based on value count information.
     * </p>
     *
     * @param size A length of each buffer.
     * @return Chainable API.
     */
    public final Signal<List<V>> buffer(int size) {
        return buffer(size, size);
    }

    /**
     * <p>
     * Indicates each values of an {@link Signal} sequence into zero or more buffers which are
     * produced based on value count information.
     * </p>
     *
     * @param size A length of each buffer. Zero or negative number are treated exactly the same way
     *            as 1.
     * @param interval A number of values to skip between creation of consecutive buffers. Zero or
     *            negative number are treated exactly the same way as 1.
     * @return Chainable API.
     */
    public final Signal<List<V>> buffer(int size, int interval) {
        int creationSize = 0 < size ? size : 1;
        int creationInterval = 0 < interval ? interval : 1;

        return new Signal<>((observer, disposer) -> {
            Deque<V> buffer = new ArrayDeque<>();
            AtomicInteger timing = new AtomicInteger();

            return to(value -> {
                buffer.offer(value);

                boolean validTiming = timing.incrementAndGet() == creationInterval;
                boolean validSize = buffer.size() == creationSize;

                if (validTiming && validSize) {
                    observer.accept(new ArrayList<>(buffer));
                }

                if (validTiming) {
                    timing.set(0);
                }

                if (validSize) {
                    buffer.pollFirst();
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Indicates each values of an {@link Signal} sequence into zero or more buffers which are
     * produced based on time count information.
     * </p>
     *
     * @param time Time to collect values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<List<V>> buffer(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return NEVER;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicReference<List<V>> ref = new AtomicReference<>(new ArrayList<>());

            return to(value -> {
                List<V> list = ref.get();
                list.add(value);

                if (list.size() == 1) {
                    I.schedule(time, unit, false, () -> observer.accept(ref.getAndSet(new ArrayList())));
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by this {@link Signal} and the other
     * specified {@link Signal}.
     * </p>
     *
     * @param other An other {@link Signal} to combine.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     */
    public final <O> Signal<Ⅱ<V, O>> combine(Signal<O> other) {
        return combine(other, I::pair);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by this {@link Signal} and the other
     * specified {@link Signal}.
     * </p>
     *
     * @param other An other {@link Signal} to combine.
     * @param another An another {@link Signal} to combine.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     */
    public final <O, A> Signal<Ⅲ<V, O, A>> combine(Signal<O> other, Signal<A> another) {
        return combine(other, I::<V, O> pair).combine(another, Ⅱ<V, O>::<A> append);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by this {@link Signal} and the other
     * specified {@link Signal}.
     * </p>
     *
     * @param other An other {@link Signal} to combine.
     * @param function A function that, when applied to an item emitted by each of the source
     *            {@link Signal}, results in an item that will be emitted by the resulting
     *            {@link Signal}.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     */
    public final <O, R> Signal<R> combine(Signal<O> other, BiFunction<V, O, R> function) {
        return new Signal<>((observer, disposer) -> {
            ArrayDeque<V> baseValue = new ArrayDeque();
            ArrayDeque<O> otherValue = new ArrayDeque();
            Runnable complete = countable(observer::complete, 2);

            return to(value -> {
                if (otherValue.isEmpty()) {
                    baseValue.add(value);
                } else {
                    observer.accept(function.apply(value, otherValue.pollFirst()));
                }
            }, observer::error, complete, disposer).add(other.to(value -> {
                if (baseValue.isEmpty()) {
                    otherValue.add(value);
                } else {
                    observer.accept(function.apply(baseValue.pollFirst(), value));
                }
            }, observer::error, complete, disposer));
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits the results of a function of your choosing applied to
     * combinations of several items emitted, in sequence, by this {@link Signal} and the other
     * specified {@link Signal}.
     * </p>
     *
     * @param others Other {@link Signal} to combine.
     * @param operator A function that, when applied to an item emitted by each of the source
     *            {@link Signal}, results in an item that will be emitted by the resulting
     *            {@link Signal}.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     */
    public final Signal<V> combine(Signal<V>[] others, BinaryOperator<V> operator) {
        Signal<V> base = this;

        if (others != null) {
            for (Signal<V> other : others) {
                base = base.combine(other, operator);
            }
        }
        return base;
    }

    /**
     * <p>
     * Combines two source {@link Signal} by emitting an item that aggregates the latest values of
     * each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param other An other constant {@link Signal} to combine.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public final <O> Signal<Ⅱ<V, O>> combineLatest(O other) {
        return combineLatest(I.signal(other));
    }

    /**
     * <p>
     * Combines two source {@link Signal} by emitting an item that aggregates the latest values of
     * each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param other An other {@link Signal} to combine.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public final <O> Signal<Ⅱ<V, O>> combineLatest(Signal<O> other) {
        return combineLatest(other, I::pair);
    }

    /**
     * <p>
     * Combines two source {@link Signal} by emitting an item that aggregates the latest values of
     * each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param other An other {@link Signal} to combine.
     * @param another An another {@link Signal} to combine.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public final <O, A> Signal<Ⅲ<V, O, A>> combineLatest(Signal<O> other, Signal<A> another) {
        return combineLatest(other, I::<V, O> pair).combineLatest(another, Ⅱ<V, O>::<A> append);
    }

    /**
     * <p>
     * Combines two source {@link Signal} by emitting an item that aggregates the latest values of
     * each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param other An other {@link Signal} to combine.
     * @param function An aggregation function used to combine the items emitted by the source
     *            {@link Signal}.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public final <O, R> Signal<R> combineLatest(Signal<O> other, BiFunction<V, O, R> function) {
        return new Signal<>((observer, disposer) -> {
            AtomicReference<V> baseValue = new AtomicReference(UNDEFINED);
            AtomicReference<O> otherValue = new AtomicReference(UNDEFINED);
            Runnable complete = countable(observer::complete, 2);

            return to(value -> {
                baseValue.set(value);
                O joined = otherValue.get();

                if (joined != UNDEFINED) {
                    observer.accept(function.apply(value, joined));
                }
            }, observer::error, complete, disposer).add(other.to(value -> {
                otherValue.set(value);

                V joined = baseValue.get();

                if (joined != UNDEFINED) {
                    observer.accept(function.apply(joined, value));
                }
            }, observer::error, complete, disposer));
        });
    }

    /**
     * <p>
     * Combines several source {@link Signal} by emitting an item that aggregates the latest values
     * of each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param others Other {@link Signal} to combine.
     * @param operator An aggregation function used to combine the items emitted by the source
     *            {@link Signal}.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public final Signal<V> combineLatest(Signal<V>[] others, BinaryOperator<V> operator) {
        Signal<V> base = this;

        if (others != null) {
            for (Signal<V> other : others) {
                base = base.combineLatest(other, operator);
            }
        }
        return base;
    }

    /**
     * <p>
     * Flattens an array of {@link Signal} into one {@link Signal}, one after the other, without
     * interleaving them.
     * </p>
     * 
     * @param others
     * @return
     */
    public final Signal<V> concat(Signal<? extends V>... others) {
        // ignore invalid parameters
        if (others == null || others.length == 0) {
            return this;
        }

        return new Signal<V>((observer, disposer) -> {
            Iterator<Signal<? extends V>> iterator = I.list(others).iterator();
            Subscriber<V> subscriber = new Subscriber();
            subscriber.observer = observer;
            subscriber.complete = () -> {
                if (iterator.hasNext()) {
                    Signal<? extends V> next = iterator.next();

                    if (next != null) {
                        next.to(subscriber.child(), disposer);
                    } else {
                        subscriber.complete();
                    }
                } else {
                    observer.complete();
                }
            };

            return to(subscriber.child(), disposer);
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
    public final Signal<V> debounce(long time, TimeUnit unit) {
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
                observer.accept(value);
            };
            latest.set(I.schedule(time, unit, true, task));
        });
    }

    /**
     * <p>
     * Indicates the {@link Signal} sequence by item count with the specified source and time.
     * </p>
     *
     * @param count The positive time used to shift the {@link Signal} sequence. Zero or negative
     *            number will ignore this instruction.
     * @return Chainable API.
     */
    public final Signal<V> delay(long count) {
        // ignore invalid parameters
        if (count <= 0) {
            return this;
        }
        return new Signal<>((observer, disposer) -> {
            Deque<V> queue = new ArrayDeque<>();

            return disposer.add(to(value -> {
                if (count <= queue.size()) {
                    observer.accept(queue.pollFirst());
                }
                queue.addLast(value);
            }));
        });
    }

    /**
     * <p>
     * Indicates the {@link Signal} sequence by due time with the specified source and time.
     * </p>
     *
     * @param time The absolute time used to shift the {@link Signal} sequence. Zero or negative
     *            number will ignore this instruction.
     * @param unit A unit of time for the specified time. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> delay(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(value -> {
                Future<?> future = I.schedule(time, unit, false, () -> {
                    if (disposer.isDisposed() == false) {
                        observer.accept(value);
                    }
                });

                disposer.add(() -> future.cancel(true));
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     * </p>
     *
     * @return Chainable API.
     */
    public final Signal<V> diff() {
        return take(null, ((BiPredicate) Objects::equals).negate());
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     * </p>
     *
     * @return Chainable API.
     */
    public final Signal<V> distinct() {
        return new Signal<>((observer, disposer) -> {
            HashSet set = new HashSet();

            return to(value -> {
                if (set.add(value)) {
                    observer.accept(value);
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Invokes an action for each value in the {@link Signal} sequence.
     * </p>
     *
     * @param effect An action to invoke for each value in the {@link Signal} sequence.
     * @return Chainable API.
     */
    public final Signal<V> effect(Consumer<? super V> effect) {
        if (effect == null) {
            return this;
        }

        return new Signal<V>((observer, disposer) -> {
            return to(value -> {
                effect.accept(value);
                observer.accept(value);
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Invokes an action for each value in the {@link Signal} sequence.
     * </p>
     *
     * @param effect An action to invoke for each value in the {@link Signal} sequence.
     * @return Chainable API.
     */
    public final Signal<V> effectOnComplete(Runnable effect) {
        if (effect == null) {
            return this;
        }

        return effectOnComplete((observer, disposer) -> effect.run());
    }

    /**
     * <p>
     * Invokes an action for each value in the {@link Signal} sequence.
     * </p>
     *
     * @param effect An action to invoke for each value in the {@link Signal} sequence.
     * @return Chainable API.
     */
    public final Signal<V> effectOnComplete(Supplier<Signal<V>> effect) {
        if (effect == null) {
            return this;
        }

        return effectOnComplete((observer, disposer) -> effect.get().to(observer, disposer));
    }

    /**
     * <p>
     * Invokes an action for each value in the {@link Signal} sequence.
     * </p>
     *
     * @param effect An action to invoke for each value in the {@link Signal} sequence.
     * @return Chainable API.
     */
    public final Signal<V> effectOnComplete(BiConsumer<Observer<? super V>, Disposable> effect) {
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, observer::error, I.bundle(() -> effect.accept(observer, disposer), observer::complete), disposer);
        });
    }

    /**
     * <p>
     * Invokes an action for each value in the {@link Signal} sequence.
     * </p>
     *
     * @param effect An action to invoke for each value in the {@link Signal} sequence.
     * @return Chainable API.
     */
    public final Signal<V> effectOnError(Consumer<Throwable> effect) {
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, I.bundle(effect, observer::error), observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Instructs an Observable to emit an item (returned by a specified function) rather than
     * invoking onError if it encounters an error.
     * </p>
     * 
     * @param resumer
     * @return
     */
    public final Signal<V> errorResume(Signal<? extends V> resumer) {
        return errorResume(e -> resumer);
    }

    /**
     * <p>
     * Instructs an Observable to emit an item (returned by a specified function) rather than
     * invoking onError if it encounters an error.
     * </p>
     * 
     * @param resumer
     * @return
     */
    public final Signal<V> errorResume(Function<? super Throwable, Signal<? extends V>> resumer) {
        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, e -> resumer.apply(e).to(observer, disposer), observer::complete, disposer);
        });
    }

    public final Signal<V> first() {
        return new Signal<>((observer, disposer) -> {

            return to(value -> {
                observer.accept(value);
                observer.complete();
                disposer.dispose();
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatArray(Function<V, R[]> function) {
        return flatMap(function.andThen(I::signal));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatArray(WiseFunction<V, R[]> function) {
        return flatArray(I.quiet(function));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatEnum(Function<V, ? extends Enumeration<R>> function) {
        return flatMap(function.andThen(I::signal));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatEnum(WiseFunction<V, ? extends Enumeration<R>> function) {
        return flatEnum(I.quiet(function));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatIterable(Function<V, ? extends Iterable<R>> function) {
        return flatMap(function.andThen(I::signal));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatIterable(WiseFunction<V, ? extends Iterable<R>> function) {
        return flatIterable(I.quiet(function));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatMap(Function<V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            return to(value -> {
                function.apply(value).to(observer::accept, observer::error, null, disposer.sub());
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatMap(WiseFunction<V, Signal<R>> function) {
        return flatMap(I.quiet(function));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatVariable(Function<V, Variable<R>> function) {
        return flatMap(function.andThen(I::signal));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatVariable(WiseFunction<V, Variable<R>> function) {
        return flatVariable(I.quiet(function));
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given {@link Predicate} function to each value
     * emitted by an {@link Signal} and emits the result.
     * </p>
     *
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final Signal<Boolean> is(Predicate<? super V> converter) {
        // ignore invalid parameters
        if (converter == null) {
            return NEVER;
        }
        return map(converter::test);
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given function to each value emitted by an
     * {@link Signal} and emits the result.
     * </p>
     *
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <R> Signal<R> map(Function<? super V, R> converter) {
        // ignore invalid parameters
        if (converter == null) {
            return (Signal<R>) this;
        }

        return new Signal<R>((observer, disposer) -> {
            return to(value -> observer.accept(converter.apply(value)), observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given function to each value emitted by an
     * {@link Signal} and emits the result.
     * </p>
     *
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <R> Signal<R> map(WiseFunction<? super V, R> converter) {
        return map(I.quiet(converter));
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given function to each value emitted by an
     * {@link Signal} and emits the result.
     * </p>
     *
     * @param init A initial value.
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <R> Signal<R> map(V init, BiFunction<V, V, R> converter) {
        // ignore invalid parameters
        if (converter == null) {
            return (Signal<R>) this;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicReference<V> ref = new AtomicReference(init);

            return to(value -> observer
                    .accept(converter.apply(ref.getAndSet(value), value)), observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given function to each value emitted by an
     * {@link Signal} and emits the result.
     * </p>
     *
     * @param init A initial value.
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <R> Signal<R> map(V init, WiseBiFunction<V, V, R> converter) {
        return map(init, I.quiet(converter));
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given constant to each item emitted by an
     * {@link Signal} and emits the result.
     * </p>
     *
     * @param constant A constant to apply to each value emitted by this {@link Signal}.
     * @return Chainable API.
     */
    public final <R> Signal<R> mapTo(R constant) {
        return map(v -> constant);
    }

    /**
     * <p>
     * Flattens a sequence of {@link Signal} emitted by an {@link Signal} into one {@link Signal},
     * without any transformation.
     * </p>
     *
     * @param others A target {@link Signal} to merge. <code>null</code> will be ignored.
     * @return Chainable API.
     */
    @SafeVarargs
    public final Signal<V> merge(Signal<? extends V>... others) {
        return merge(Arrays.asList(others));
    }

    /**
     * <p>
     * Flattens a sequence of {@link Signal} emitted by an {@link Signal} into one {@link Signal},
     * without any transformation.
     * </p>
     *
     * @param others A target {@link Signal} set to merge. <code>null</code> will be ignored.
     * @return Chainable API.
     */
    public final Signal<V> merge(Iterable<? extends Signal<? extends V>> others) {
        // ignore invalid parameters
        if (others == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Subscriber<V> subscriber = new Subscriber();
            subscriber.index--;
            subscriber.observer = observer;
            subscriber.complete = () -> {
                if (subscriber.isCompleted()) {
                    observer.complete();
                }
            };

            disposer = to(subscriber.child(), disposer);

            for (Signal<? extends V> other : others) {
                if (other != null && disposer.isDisposed() == false) {
                    disposer = disposer.add(other.to(subscriber.child(), disposer));
                }
            }

            subscriber.index++;
            subscriber.complete();

            return disposer;
        });
    }

    /**
     * <p>
     * Invokes an action for each value in the {@link Signal} sequence.
     * </p>
     *
     * @param next An action to invoke for each value in the {@link Signal} sequence.
     * @return Chainable API.
     */
    private Signal<V> on(BiConsumer<Observer<? super V>, V> next) {
        return new Signal<>((observer, disposer) -> {
            Subscriber<V> subscriber = new Subscriber();
            subscriber.observer = observer;
            subscriber.next = value -> next.accept(observer, value);

            return to(subscriber, disposer);
        });
    }

    /**
     * <p>
     * Switch event stream context.
     * </p>
     * 
     * @param scheduler A new context
     * @return Chainable API.
     */
    public final Signal<V> on(Consumer<Runnable> scheduler) {
        // ignore invalid parameters
        if (scheduler == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> to(v -> {
            scheduler.accept(() -> {
                observer.accept(v);
            });
        }, observer::error, observer::complete, disposer));
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that repeats the given value infinitely.
     * </p>
     *
     * @return Chainable API.
     */
    public final Signal<V> repeat() {
        return repeatUntil(NEVER);
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that repeats the given value finitely.
     * </p>
     *
     * @param count A number of repeat. Zero or negative number will ignore this instruction.
     * @return Chainable API.
     */
    public final Signal<V> repeat(int count) {
        // ignore invalid parameter
        if (count < 1) {
            return this;
        }
        return repeatUntil(new AtomicInteger(count), v -> v.decrementAndGet() > 0);
    }

    /**
     * <p>
     * Returns an {@link Signal} that repeats the sequence of items emitted by the source
     * {@link Signal} until the provided condition function returns false.
     * </p>
     * 
     * @param condition A condition supplier that is called when the current {@link Signal}
     *            completes and unless it returns false, the current {@link Signal} is resubscribed.
     * @return Chainable API.
     */
    public final Signal<V> repeatIf(BooleanSupplier condition) {
        if (condition == null) {
            return this;
        }
        return repeatUntil(condition, BooleanSupplier::getAsBoolean);
    }

    /**
     * <p>
     * Returns an {@link Signal} that repeats the sequence of items emitted by the source
     * {@link Signal} until a stopper {@link Signal} emits an item.
     * </p>
     * 
     * @param stopper A {@link Signal} whose first emitted item will stop repeating.
     * @return Chainable API.
     */
    public final Signal<V> repeatUntil(Signal stopper) {
        return repeatUntil(stopper.take(1).to(), Variable::isAbsent);
    }

    /**
     * <p>
     * </p>
     * 
     * @param init
     * @param condition
     * @return
     */
    private <T> Signal<V> repeatUntil(T init, Predicate<T> condition) {
        return new Signal<>((observer, disposer) -> {
            Disposable[] latest = new Disposable[1];

            Subscriber subscriber = new Subscriber();
            subscriber.observer = observer;
            subscriber.complete = () -> {
                latest[0].dispose();

                if (condition.test(init)) {
                    subscriber.add(latest[0] = to(subscriber.child()));
                } else {
                    observer.complete();
                }
            };
            return subscriber.add(latest[0] = to(subscriber.child(), disposer));
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that, when the specified sampler {@link Signal} emits an item,
     * emits the most recently emitted item (if any) emitted by the source {@link Signal} since the
     * previous emission from the sampler {@link Signal}.
     * </p>
     *
     * @param sampler An {@link Signal} to use for sampling the source {@link Signal}.
     * @return Chainable API.
     */
    public final Signal<V> sample(Signal sampler) {
        // ignore invalid parameters
        if (sampler == null) {
            return NEVER;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicReference<V> latest = new AtomicReference(UNDEFINED);

            return to(latest::set, observer::error, observer::complete, disposer).add(sampler.to(sample -> {
                V value = latest.getAndSet((V) UNDEFINED);

                if (value != UNDEFINED) {
                    observer.accept(value);
                }
            }, disposer));
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies a function of your choosing to the first item emitted
     * by a source {@link Signal} and a seed value, then feeds the result of that function along
     * with the second item emitted by the source {@link Signal} into the same function, and so on
     * until all items have been emitted by the source {@link Signal}, emitting the result of each
     * of these iterations.
     * </p>
     *
     * @param init An initial (seed) accumulator item.
     * @param function An accumulator function to be invoked on each item emitted by the source
     *            {@link Signal}, whose result will be emitted to {@link Signal} via
     *            {@link Observer#accept(Object)} and used in the next accumulator call.
     * @return An {@link Signal} that emits initial value followed by the results of each call to
     *         the accumulator function.
     */
    public final <R> Signal<R> scan(R init, BiFunction<R, V, R> function) {
        return new Signal<>((observer, disposer) -> {
            AtomicReference<R> ref = new AtomicReference(init);

            return to(value -> {
                ref.set(function.apply(ref.get(), value));
                observer.accept(ref.get());
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns a new {@link Signal} that multicasts (shares) the original {@link Signal}. As long as
     * there is at least one {@link Observer} this {@link Signal} will be subscribed and emitting
     * data. When all observers have disposed it will disposes from the source {@link Signal}.
     * </p>
     * 
     * @return Chainable API.
     */
    public final Signal<V> share() {
        Disposable root = Disposable.empty();
        List<Observer<? super V>> observers = new CopyOnWriteArrayList();

        return new Signal<>((observer, dispoer) -> {
            if (observers.isEmpty()) {
                root.add(to(v -> {
                    for (Observer<? super V> o : observers) {
                        o.accept(v);
                    }
                }, e -> {
                    for (Observer<? super V> o : observers) {
                        o.error(e);
                    }
                }, () -> {
                    for (Observer<? super V> o : observers) {
                        o.complete();
                    }
                }));
            }
            observers.add(observer);

            return () -> {
                observers.remove(observer);

                if (observers.isEmpty()) {
                    root.dispose();
                }
            };
        });
    }

    /**
     * <p>
     * Alias for take(condition.negate()).
     * </p>
     *
     * @param condition A skip condition.
     * @return Chainable API.
     */
    public final Signal<V> skip(Predicate<V> condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }
        return take(condition.negate());
    }

    /**
     * <p>
     * Alias for skip(I.set(excludes)).
     * </p>
     *
     * @param excludes A collection of skip items.
     * @return Chainable API.
     */
    public final Signal<V> skip(V... excludes) {
        // ignore invalid parameter
        if (excludes == null) {
            return this;
        }
        return skip(I.set(excludes));
    }

    /**
     * <p>
     * Alias for skip(v -> excludes.contains(v)).
     * </p>
     *
     * @param excludes A collection of skip items.
     * @return Chainable API.
     */
    public final Signal<V> skip(Collection<V> excludes) {
        // ignore invalid parameter
        if (excludes == null) {
            return this;
        }
        return skip(excludes::contains);
    }

    /**
     * <p>
     * Alias for take(init, condition.negate()).
     * </p>
     *
     * @param condition A skip condition.
     * @return Chainable API.
     */
    public final Signal<V> skip(V init, BiPredicate<V, V> condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }
        return take(init, condition.negate());
    }

    /**
     * <p>
     * Bypasses a specified number of values in an {@link Signal} sequence and then returns the
     * remaining values.
     * </p>
     *
     * @param count A number of values to skip. Zero or negative number will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> skip(int count) {
        // ignore invalid parameter
        if (count <= 0) {
            return this;
        }
        return new Signal<>((observer, disposer) -> {
            AtomicInteger counter = new AtomicInteger();

            return to(value -> {
                if (count < counter.incrementAndGet()) {
                    observer.accept(value);
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Bypasses a specified duration in an {@link Signal} sequence and then returns the remaining
     * values.
     * </p>
     *
     * @param time Time to skip values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> skip(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            long timing = System.nanoTime() + unit.toNanos(time);

            return to(value -> {
                if (timing < System.nanoTime()) {
                    observer.accept(value);
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition An external boolean {@link Signal}. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> skip(Signal<Boolean> condition) {
        return take(condition.map(v -> !v), true);
    }

    /**
     * <p>
     * Returns a specified index values from the start of an {@link Signal} sequence.
     * </p>
     * 
     * @param condition A index condition of values to emit.
     * @return Chainable API.
     */
    public final Signal<V> skipAt(IntPredicate condition) {
        return takeAt(condition.negate());
    }

    /**
     * <p>
     * Alias for skip(Objects::isNull).
     * </p>
     *
     * @return Chainable API.
     */
    public final Signal<V> skipNull() {
        return skip(Objects::isNull);
    }

    /**
     * <p>
     * Returns an {@link Signal} sequence while the specified duration.
     * </p>
     *
     * @param time Time to skip values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> skipUntil(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            long timing = System.nanoTime() + unit.toNanos(time);

            return to(value -> {
                if (System.nanoTime() > timing) {
                    observer.accept(value);
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * This method is equivalent to the following code.
     * </p>
     * <pre>
     * skipUntil(v -> Objects.equals(v, value));
     * </pre>
     *
     * @param value A value to test each item emitted from the source {@link Signal}.
     * @return An {@link Signal} that begins emitting items emitted by the source {@link Signal}
     *         when the specified value is coming.
     */
    public final Signal<V> skipUntil(V value) {
        return skipUntil(v -> Objects.equals(v, value));
    }

    /**
     * <p>
     * Returns an {@link Signal} that skips all items emitted by the source {@link Signal} as long
     * as a specified condition holds true, but emits all further source items as soon as the
     * condition becomes false.
     * </p>
     *
     * @param predicate A function to test each item emitted from the source {@link Signal}.
     * @return An {@link Signal} that begins emitting items emitted by the source {@link Signal}
     *         when the specified predicate becomes false.
     */
    public final Signal<V> skipUntil(Predicate<? super V> predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicBoolean flag = new AtomicBoolean();

            return to(value -> {
                if (flag.get()) {
                    observer.accept(value);
                } else if (predicate.test(value)) {
                    flag.set(true);
                    observer.accept(value);
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns the values from the source {@link Signal} sequence only after the other
     * {@link Signal} sequence produces a value.
     * </p>
     *
     * @param timing The second {@link Signal} that has to emit an item before the source
     *            {@link Signal} elements begin to be mirrored by the resulting {@link Signal}.
     * @return An {@link Signal} that skips items from the source {@link Signal} until the second
     *         {@link Signal} emits an item, then emits the remaining items.
     */
    public final Signal<V> skipUntil(Signal timing) {
        // ignore invalid parameter
        if (timing == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Variable<Boolean> take = timing.take(1).toBinary();

            return to(value -> {
                if (take.get()) {
                    observer.accept(value);
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Alias for take(condition.map(value -> !value).
     * </p>
     *
     * @param condition A skip condition.
     * @return Chainable API.
     */
    public final Signal<V> skipWhile(Signal<Boolean> condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }
        return take(condition.startWith(false).map(value -> !value));
    }

    /**
     * <p>
     * Returns an {@link Signal} that skips all items emitted by the source {@link Signal} as long
     * as a specified condition holds true, but emits all further source items as soon as the
     * condition becomes false.
     * </p>
     *
     * @param predicate A function to test each item emitted from the source {@link Signal}.
     * @return An {@link Signal} that begins emitting items emitted by the source {@link Signal}
     *         when the specified predicate becomes false.
     */
    public final Signal<V> skipWhile(Predicate<? super V> predicate) {
        return skipUntil(predicate.negate());
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Signal}.
     * </p>
     *
     * @param value The initial values.
     * @return Chainable API.
     */
    public final Signal<V> startWith(Variable<V> value) {
        return startWith(value.v).skipNull();
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Signal}.
     * </p>
     *
     * @param value The initial value.
     * @return Chainable API.
     * @see StartWithTest#value()
     */
    public final Signal<V> startWith(V value) {
        return startWith(Arrays.asList(value));
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Signal}.
     * </p>
     * <p>
     * If you want an {@link Signal} to emit a specific sequence of items before it begins emitting
     * the items normally expected from it, apply the StartWith operator to it.
     * </p>
     * <p>
     * If, on the other hand, you want to append a sequence of items to the end of those normally
     * emitted by an {@link Signal}, you want the {@link #concatMap(Function)} operator.
     * </p>
     *
     * @param values The values that contains the items you want to emit first.
     * @return Chainable API.
     */
    @SafeVarargs
    public final Signal<V> startWith(V... values) {
        return values == null || values.length == 0 ? this : startWith(Arrays.asList(values));
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Signal}.
     * </p>
     * <p>
     * If you want an {@link Signal} to emit a specific sequence of items before it begins emitting
     * the items normally expected from it, apply the StartWith operator to it.
     * </p>
     * <p>
     * If, on the other hand, you want to append a sequence of items to the end of those normally
     * emitted by an {@link Signal}, you want the {@link #concatMap(Function)} operator.
     * </p>
     *
     * @param values The values that contains the items you want to emit first.
     * @return Chainable API.
     */
    public final Signal<V> startWith(Enumeration<V> values) {
        return values == null ? this : startWith(Collections.list(values));
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Signal}.
     * </p>
     * <p>
     * If you want an {@link Signal} to emit a specific sequence of items before it begins emitting
     * the items normally expected from it, apply the StartWith operator to it.
     * </p>
     * <p>
     * If, on the other hand, you want to append a sequence of items to the end of those normally
     * emitted by an {@link Signal}, you want the {@link #concatMap(Function)} operator.
     * </p>
     *
     * @param values The values that contains the items you want to emit first.
     * @return Chainable API.
     */
    public final <S extends BaseStream<V, S>> Signal<V> startWith(S values) {
        return values == null ? this : startWith(() -> values.iterator());
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Signal}.
     * </p>
     * <p>
     * If you want an {@link Signal} to emit a specific sequence of items before it begins emitting
     * the items normally expected from it, apply the StartWith operator to it.
     * </p>
     * <p>
     * If, on the other hand, you want to append a sequence of items to the end of those normally
     * emitted by an {@link Signal}, you want the {@link #concatMap(Function)} operator.
     * </p>
     *
     * @param values The values that contains the items you want to emit first.
     * @return Chainable API.
     */
    public final Signal<V> startWith(Iterable<V> values) {
        // ignore invalid parameter
        if (values == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Iterator<V> iterator = values.iterator();

            if (iterator != null) {
                while (iterator.hasNext() && !disposer.isDisposed()) {
                    observer.accept(iterator.next());
                }
            }
            return to(observer, disposer);
        });
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Signal}.
     * </p>
     *
     * @param values The initial values.
     * @return Chainable API.
     */
    public final Signal<V> startWith(Signal<V> values) {
        // ignore invalid parameter
        if (values == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return values.to(observer, disposer).add(to(observer, disposer));
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging the latest resulting {@link Signal} and emitting the results of this
     * merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> switchMap(Function<V, Signal<R>> function) {
        return new Signal<>((observer, disposer) -> {
            Disposable[] disposables = {null, Disposable.empty()};

            disposables[0] = to(value -> {
                disposables[1].dispose();
                disposables[1] = function.apply(value).to(observer);
            }, observer::error, observer::complete, disposer);
            return () -> {
                disposables[0].dispose();
                disposables[1].dispose();
            };
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition A function that evaluates the values emitted by the source {@link Signal},
     *            returning {@code true} if they pass the filter. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> take(Predicate<V> condition) {
        // ignore invalid parameters
        if (condition == null) {
            return this;
        }

        return on((observer, value) -> {
            if (condition.test(value)) {
                observer.accept(value);
            }
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition A function that evaluates the values emitted by the source {@link Signal},
     *            returning {@code true} if they pass the filter. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> take(V init, BiPredicate<V, V> condition) {
        // ignore invalid parameters
        if (condition == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicReference<V> ref = new AtomicReference(init);

            return to(value -> {
                if (condition.test(ref.getAndSet(value), value)) {
                    observer.accept(value);
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    public final Signal<V> take(BooleanSupplier condition) {
        return take(v -> condition.getAsBoolean());
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition An external boolean {@link Signal}. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> take(Signal<Boolean> condition) {
        return take(condition, false);
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition An external boolean {@link Signal}. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    private final Signal<V> take(Signal<Boolean> condition, boolean init) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicBoolean flag = new AtomicBoolean(init);

            return condition.to(flag::set, observer::error, observer::complete, disposer).add(to(v -> {
                if (flag.get()) {
                    observer.accept(v);
                }
            }, observer::error, observer::complete, disposer));
        });
    }

    /**
     * <p>
     * Returns a specified index values from the start of an {@link Signal} sequence.
     * </p>
     * 
     * @param condition A index condition of values to emit.
     * @return Chainable API.
     */
    public final Signal<V> takeAt(IntPredicate condition) {
        if (condition == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicInteger index = new AtomicInteger();

            return to(value -> {
                if (condition.test(index.getAndIncrement())) {
                    observer.accept(value);
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns a specified number of contiguous values from the start of an {@link Signal} sequence.
     * </p>
     *
     * @param count A number of values to emit. Zero or negative number will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> take(int count) {
        // ignore invalid parameter
        if (count <= 0) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicInteger counter = new AtomicInteger(count);
            return to(value -> {
                int current = counter.decrementAndGet();

                if (0 <= current) {
                    observer.accept(value);

                    if (0 == current) {
                        observer.complete();
                        disposer.dispose();
                    }
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} sequence while the specified duration.
     * </p>
     *
     * @param time Time to take values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> takeUntil(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            long timing = System.nanoTime() + unit.toNanos(time);

            return to(value -> {
                if (System.nanoTime() < timing) {
                    observer.accept(value);
                } else {
                    observer.complete();
                    disposer.dispose();
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * This method is equivalent to the following code.
     * </p>
     * <pre>
     * takeUntil(v -> Objects.equals(v, value));
     * </pre>
     *
     * @param value A value to test each item emitted from the source {@link Signal}.
     * @return An {@link Signal} that first emits items emitted by the source {@link Signal}, checks
     *         the specified condition after each item, and then completes if the condition is
     *         satisfied.
     */
    public final Signal<V> takeUntil(V value) {
        return takeUntil(v -> Objects.equals(v, value));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items emitted by the source {@link Signal}, checks the
     * specified predicate for each item, and then completes if the condition is satisfied.
     * </p>
     *
     * @param condition A function that evaluates an item emitted by the source {@link Signal} and
     *            returns a Boolean.
     * @return An {@link Signal} that first emits items emitted by the source {@link Signal}, checks
     *         the specified condition after each item, and then completes if the condition is
     *         satisfied.
     */
    public final Signal<V> takeUntil(Predicate<V> condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicBoolean flag = new AtomicBoolean();

            return to(value -> {
                if (flag.get() == false) {
                    observer.accept(value);

                    if (condition.test(value)) {
                        flag.set(true);
                        observer.complete();
                        disposer.dispose();
                    }
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Returns the values from the source {@link Signal} sequence until the other {@link Signal}
     * sequence produces a value.
     * </p>
     *
     * @param condition An {@link Signal} sequence that terminates propagation of values of the
     *            source sequence. <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final Signal<V> takeUntil(Signal condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Disposable disposable = Disposable.empty();
            return disposable.add(to(observer, disposer).add(condition.to(value -> {
                observer.complete();
                disposable.dispose();
            })));
            // return to(v -> {
            // observer.accept(v);
            // }, observer::error, observer::complete, disposer).add(condition.to(value -> {
            // observer.complete();
            // System.out.println("EDN " + value);
            // disposer.dispose();
            // }, disposer));
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items emitted by the source {@link Signal}, checks the
     * specified predicate for each item, and then completes if the condition is satisfied.
     * </p>
     *
     * @param condition A function that evaluates an item emitted by the source {@link Signal} and
     *            returns a Boolean.
     * @return An {@link Signal} that first emits items emitted by the source {@link Signal}, checks
     *         the specified condition after each item, and then completes if the condition is
     *         satisfied.
     */
    public final Signal<V> takeWhile(Predicate<V> condition) {
        return takeUntil(condition.negate());
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the boolean values alternately to each item emitted by
     * an {@link Signal} and emits the result. Initial value is true.
     * </p>
     *
     * @return Chainable API.
     */
    public final Signal<Boolean> toggle() {
        return toggle(true);
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the boolean values alternately to each item emitted by
     * an {@link Signal} and emits the result.
     * </p>
     *
     * @param initial A initial boolean value to apply to each value emitted by this {@link Signal}.
     * @return Chainable API.
     */
    public final Signal<Boolean> toggle(boolean initial) {
        return toggle(initial, !initial);
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given two constants alternately to each item
     * emitted by an {@link Signal} and emits the result.
     * </p>
     *
     * @param values A list of constants to apply to each value emitted by this {@link Signal}.
     * @return Chainable API.
     */
    @SafeVarargs
    public final <E> Signal<E> toggle(E... values) {
        if (values.length == 0) {
            return NEVER;
        }

        return new Signal<>((observer, disposable) -> {
            AtomicInteger count = new AtomicInteger();

            return to(value -> observer.accept(values[count.getAndIncrement() % values.length]));
        });
    }

    /**
     * <p>
     * Throttles by skipping values until "skipDuration" passes and then emits the next received
     * value.
     * </p>
     * <p>
     * Ignores the values from an {@link Signal} sequence which are followed by another value before
     * due time with the specified source and time.
     * </p>
     *
     * @param time Time to wait before sending another item after emitting the last item. Zero or
     *            negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> throttle(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        AtomicLong latest = new AtomicLong();
        long delay = unit.toNanos(time);

        return take(value -> {
            long now = System.nanoTime();

            if (latest.get() + delay <= now) {
                latest.set(now);
                return true;
            }
            return false;
        });
    }

    /**
     * Create event delegater with counter.
     * 
     * @param delgator
     * @param count
     * @return
     */
    private Runnable countable(Runnable delgator, int count) {
        AtomicInteger counter = new AtomicInteger();

        return () -> {
            if (counter.incrementAndGet() == count) {
                delgator.run();
            }
        };
    }

    // /**
    // * <p>
    // * Append the current time to each events.
    // * </p>
    // *
    // * @return
    // */
    // public final Events<Binary<V, Instant>> timeStamp() {
    // return map(value -> I.pair(value, Instant.now()));
    // }
    //
    // /**
    // * <p>
    // * Append {@link Duration} between the current value and the previous value.
    // * </p>
    // *
    // * @return
    // */
    // public final Events<Binary<V, Duration>> timeInterval() {
    // return timeStamp().map(null, (prev, current) -> current
    // .e(prev == null ? Duration.ZERO : Duration.between(prev.e, current.e)));
    // }
    //
    // /**
    // * <p>
    // * Append {@link Duration} between the current value and the first value.
    // * </p>
    // *
    // * @return
    // */
    // public final Events<Binary<V, Duration>> timeElapsed() {
    // return timeInterval().scan(I.pair((V) null, Duration.ZERO), (sum, now) ->
    // now.e(sum.e.plus(now.e)));
    // }

}
