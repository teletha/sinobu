/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static java.lang.Boolean.*;
import static java.util.concurrent.TimeUnit.*;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collector;

/**
 * @version 2018/08/01 13:38:16
 */
public final class Signal<V> {

    /**
     * For reuse.
     */
    public static final Signal EMPTY = new Signal<>((observer, disposer) -> {
        if (disposer.isNotDisposed()) observer.complete();
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
    public Signal(Collection<Observer<V>> observers) {
        this((observer, disposer) -> {
            observers.add((Observer<V>) observer);

            return disposer.add(() -> observers.remove(observer));
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
        return to(I.wise(next).asConsumer(), null, null);
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
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @param complete A delegator method of {@link Observer#complete()}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Consumer<? super V> next, Runnable complete) {
        return to(next, null, complete);
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
    public final Disposable to(Runnable next, Consumer<Throwable> error, Runnable complete) {
        return to(I.wise(next).asConsumer(), error, complete);
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
        return to(next, error, complete, Disposable.empty(), true);
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
        return to(observer::accept, observer::error, observer::complete, Disposable.empty(), true);
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
    private Disposable to(Consumer<? super V> next, Consumer<? extends Throwable> error, Runnable complete, Disposable disposer) {
        return to(next, error, complete, disposer, false);
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
    private Disposable to(Consumer<? super V> next, Consumer<? extends Throwable> error, Runnable complete, Disposable disposer, boolean auto) {
        Subscriber<V> subscriber = new Subscriber();
        subscriber.next = e -> {
            if (disposer.isNotDisposed()) next.accept(e);
        };
        subscriber.error = e -> {
            if (error != null && disposer.isNotDisposed()) ((Consumer<Throwable>) error).accept(e);
            if (auto) disposer.dispose();
        };
        subscriber.complete = () -> {
            if (complete != null && disposer.isNotDisposed()) complete.run();
            if (auto) disposer.dispose();
        };

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
    private Disposable to(Observer<? super V> observer, Disposable disposer) {
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
     * Receive values as {@link Collection} from this {@link Signal}.
     * </p>
     *
     * @return A {@link Collection} as value receiver.
     */
    public final <C extends Collection<V>> C toCollection(C collection) {
        to(collection::add);
        return collection;
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
     * Returns {@link Signal} that emits a Boolean that indicates whether all of the items emitted
     * by the source {@link Signal} satisfy a condition.
     * 
     * @param condition A condition that evaluates an item and returns a Boolean.
     * @return A {@link Signal} that emits true if all items emitted by the source {@link Signal}
     *         satisfy the predicate; otherwise, false.
     */
    public final Signal<Boolean> all(Predicate<? super V> condition) {
        Objects.requireNonNull(condition);

        return signal(condition.negate(), FALSE, false, FALSE, true, TRUE);
    }

    /**
     * Returns a {@link Signal} that emits true if any item emitted by the source {@link Signal}
     * satisfies a specified condition, otherwise false. Note: this always emits false if the source
     * {@link Signal} is empty.
     * 
     * @param condition A condition to test items emitted by the source {@link Signal}.
     * @return A {@link Signal} that emits a Boolean that indicates whether any item emitted by the
     *         source {@link Signal} satisfies the predicate.
     */
    public final Signal<Boolean> any(Predicate<? super V> condition) {
        Objects.requireNonNull(condition);

        return signal(condition, TRUE, false, FALSE, true, FALSE);
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
        return (Signal<R>) (type == null ? this : take(I.wrap(type)::isInstance));
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
    public final Signal<List<V>> buffer() {
        return buffer(NEVER);
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
        return buffer(I.signal(time, time, unit));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits non-overlapping buffered items from the source
     * {@link Signal} each time the specified boundary {@link Signal} emits an item.
     * </p>
     * 
     * @param boundary A boundary {@link Signal}.
     * @return Chainable API.
     */
    public final Signal<List<V>> buffer(Signal<?> boundary) {
        return buffer(boundary, ArrayList::new);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits non-overlapping buffered items from the source
     * {@link Signal} each time the specified boundary {@link Signal} emits an item.
     * </p>
     * 
     * @param boundary A boundary {@link Signal}.
     * @param supplier A factory function that returns an instance of the collection subclass to be
     *            used and returned as the buffer.
     * @return Chainable API.
     */
    public final <B extends Collection<V>> Signal<B> buffer(Signal<?> boundary, Supplier<B> supplier) {
        return buffer(boundary, supplier, Collection::add).skip(B::isEmpty);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits non-overlapping buffered items from the source
     * {@link Signal} each time the specified boundary {@link Signal} emits an item.
     * </p>
     * 
     * @param boundary A boundary {@link Signal}.
     * @param supplier A factory function that returns an instance of the collection subclass to be
     *            used and returned as the buffer.
     * @param assigner A operation function that assigns a value to the buffer.
     * @return Chainable API.
     */
    public final <B> Signal<B> buffer(Signal<?> boundary, Supplier<B> supplier, BiConsumer<B, V> assigner) {
        return buffer(boundary, supplier, assigner, false);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits non-overlapping buffered items from the source
     * {@link Signal} each time the specified boundary {@link Signal} emits an item.
     * </p>
     * 
     * @param boundary A boundary {@link Signal}.
     * @param supplier A factory function that returns an instance of the collection subclass to be
     *            used and returned as the buffer.
     * @param assigner A operation function that assigns a value to the buffer.
     * @param ignoreRemaining A flag whether completion event emits the remaining values or not.
     * @return Chainable API.
     */
    private <B> Signal<B> buffer(Signal<?> boundary, Supplier<B> supplier, BiConsumer<B, V> assigner, boolean ignoreRemaining) {
        return new Signal<>((observer, disposer) -> {
            AtomicReference<B> buffer = new AtomicReference(supplier.get());

            WiseRunnable transfer = () -> observer.accept(buffer.getAndSet(supplier.get()));
            WiseRunnable completer = ignoreRemaining ? observer::complete : transfer.then(observer::complete);

            return to(v -> assigner.accept(buffer.get(), v), observer::error, completer, disposer)
                    .add(boundary.to(transfer, observer::error, completer));
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
     * @param combiner An aggregation function used to combine the items emitted by the source
     *            {@link Signal}.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     */
    public final <O, R> Signal<R> combine(Signal<O> other, BiFunction<V, O, R> combiner) {
        return new Signal<>((observer, disposer) -> {
            LinkedList<V> baseValue = new LinkedList();
            LinkedList<O> otherValue = new LinkedList();
            Subscriber completer = countable(observer, 2);

            return to(value -> {
                if (otherValue.isEmpty()) {
                    baseValue.add(value);
                } else {
                    observer.accept(combiner.apply(value, otherValue.pollFirst()));
                }
            }, observer::error, () -> {
                if (baseValue.isEmpty()) {
                    observer.complete();
                } else {
                    completer.complete();
                }
            }, disposer).add(other.to(value -> {
                if (baseValue.isEmpty()) {
                    otherValue.add(value);
                } else {
                    observer.accept(combiner.apply(baseValue.pollFirst(), value));
                }
            }, observer::error, () -> {
                if (otherValue.isEmpty()) {
                    observer.complete();
                } else {
                    completer.complete();
                }
            }, disposer));
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
            Subscriber completer = countable(observer, 2);

            return disposer.add(to(value -> {
                baseValue.set(value);
                O joined = otherValue.get();

                if (joined != UNDEFINED) {
                    observer.accept(function.apply(value, joined));
                }
            }, observer::error, completer::complete)).add(other.to(value -> {
                otherValue.set(value);

                V joined = baseValue.get();

                if (joined != UNDEFINED) {
                    observer.accept(function.apply(joined, value));
                }
            }, observer::error, completer::complete));
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
     * Returns an {@link Signal} that emits the items emitted by {@link Signal}s, one after the
     * other, without interleaving them.
     * </p>
     * 
     * @param others A sequence of {@link Signal}s to concat.
     * @return Chainable API.
     */
    public final Signal<V> concat(Signal<? extends V>... others) {
        // ignore invalid parameters
        if (others == null || others.length == 0) {
            return this;
        }
        return concat(I.list(others));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits the items emitted by {@link Signal}s, one after the
     * other, without interleaving them.
     * </p>
     * 
     * @param others A sequence of {@link Signal}s to concat.
     * @return Chainable API.
     */
    public final Signal<V> concat(Iterable<Signal<? extends V>> others) {
        // ignore invalid parameters
        if (others == null) {
            return this;
        }

        return new Signal<V>((observer, disposer) -> {
            Iterator<Signal<? extends V>> signals = I.signal(others).skipNull().startWith(this).toList().iterator();

            I.recurseR(self -> () -> {
                if (signals.hasNext()) {
                    signals.next().to(observer::accept, observer::error, self, disposer.sub(), true);
                } else {
                    observer.complete();
                }
            }).run();

            return disposer;
        });
    }

    /**
     * Maps a sequence of values into {@link Signal} and concatenates these {@link Signal} eagerly
     * into a single {@link Signal}. Eager concatenation means that once a subscriber subscribes,
     * this operator subscribes to all of the source {@link Signal}. The operator buffers the values
     * emitted by these {@link Signal} and then drains them in order, each one after the previous
     * one completes.
     * 
     * @param function A function that maps a sequence of values into a sequence of {@link Signal}
     *            that will be eagerly concatenated.
     * @return Chainable API.
     */
    public final <R> Signal<R> concatMap(WiseFunction<V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            AtomicLong processing = new AtomicLong();
            Map<Long, Ⅱ<AtomicBoolean, LinkedList<R>>> buffer = new ConcurrentHashMap();

            Consumer<Long> complete = I.recurseC(self -> index -> {
                if (processing.get() == index) {
                    Ⅱ<AtomicBoolean, LinkedList<R>> next = buffer.remove(processing.incrementAndGet());

                    if (next != null) {
                        // emit stored items
                        for (R value : next.ⅱ) {
                            observer.accept(value);
                        }

                        // this indexed buffer has been completed already, step into next buffer
                        if (next.ⅰ.get() == true) {
                            self.accept(processing.get());
                        }
                    }
                }
            });

            Subscriber end = countable(observer, 1);

            return

            index().to(indexed -> {
                AtomicBoolean completed = new AtomicBoolean();
                LinkedList<R> items = new LinkedList();
                buffer.put(indexed.ⅱ, I.pair(completed, items));
                end.index++;

                function.apply(indexed.ⅰ).to(v -> {
                    if (processing.get() == indexed.ⅱ) {
                        observer.accept(v);
                    } else {
                        items.add(v);
                    }
                }, observer::error, () -> {
                    completed.set(true);
                    complete.accept(indexed.ⅱ);
                    end.complete();
                }, disposer.sub(), true);
            }, observer::error, end::complete, disposer);
        });

    }

    /**
     * Returns a {@link Signal} that emits a Boolean that indicates whether the source
     * {@link Signal} emitted a specified item.
     * 
     * @param value An item to search for in the emissions from the source {@link Signal}.
     * @return A {@link Signal} that emits true if the specified item is emitted by the source
     *         {@link Signal}, or false if the source {@link Signal} completes without emitting that
     *         item.
     */
    public final Signal<Boolean> contains(Object value) {
        return any(v -> Objects.equals(v, value));
    }

    /**
     * Returns a {@link Signal} that counts the total number of items emitted by the source
     * {@link Signal} and emits this count as a 64-bit Long.
     * 
     * @return {@link Signal} that emits a single item: the number of items emitted by the source
     *         {@link Signal} as a 64-bit Long item
     */
    public final Signal<Long> count() {
        return map(AtomicLong::new, (context, value) -> context.incrementAndGet());
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

        return new Signal<V>((observer, disposer) -> {
            AtomicReference<Future> latest = new AtomicReference();

            return to(value -> {
                Future future = latest.get();

                if (future != null) {
                    future.cancel(true);
                }

                Runnable task = () -> {
                    latest.set(null);
                    observer.accept(value);
                };
                latest.set(I.schedule(time, unit, true, task));
            }, observer::error, observer::complete, disposer);
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
     * Returns {@link Signal} that emits the items emitted by the source {@link Signal} shifted
     * forward in time by a specified delay at parallel thread. Error notifications from the source
     * {@link Signal} are not delayed.
     *
     * @param time The delay to shift the source by.
     * @param unit The {@link TimeUnit} in which {@code period} is defined.
     * @return The source {@link Signal} shifted in time by the specified delay.
     * @see #wait(long, TimeUnit)
     */
    public final Signal<V> delay(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (unit == null) {
            return this;
        }
        return delay(Duration.of(time, unit.toChronoUnit()));
    }

    /**
     * Returns {@link Signal} that emits the items emitted by the source {@link Signal} shifted
     * forward in time by a specified delay at parallel thread. Error notifications from the source
     * {@link Signal} are not delayed.
     *
     * @param time The delay to shift the source by.
     * @return The source {@link Signal} shifted in time by the specified delay.
     */
    public final Signal<V> delay(Duration time) {
        // ignore invalid parameters
        if (time == null || time.isNegative() || time.isZero()) {
            return this;
        }
        return delay(Variable.of(time));
    }

    /**
     * Returns {@link Signal} that emits the items emitted by the source {@link Signal} shifted
     * forward in time by a specified delay at parallel thread. Error notifications from the source
     * {@link Signal} are not delayed.
     *
     * @param time The delay to shift the source by.
     * @return The source {@link Signal} shifted in time by the specified delay.
     */
    public final Signal<V> delay(Supplier<Duration> time) {
        // ignore invalid parameters
        if (time == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(value -> {
                Future<?> future = I.schedule(time.get().toNanos(), NANOSECONDS, false, () -> {
                    if (disposer.isNotDisposed()) {
                        observer.accept(value);
                    }
                });

                disposer.add(() -> future.cancel(true));
            }, observer::error, () -> I.schedule(time.get().toNanos(), NANOSECONDS, false, observer::complete), disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits all items emitted by the source {@link Signal} that are
     * distinct from their immediate predecessors based on {@link Object#equals(Object)} comparison.
     * </p>
     * <p>
     * It is recommended the elements' class {@code V} in the flow overrides the default
     * {@code Object.equals()} to provide meaningful comparison between items as the default Java
     * implementation only considers reference equivalence. Alternatively, use the
     * {@link #diff(BiPredicate)} overload and provide a comparison function in case the class
     * {@code V} can't be overridden with custom {@code equals()} or the comparison itself should
     * happen on different terms or properties of the class {@code V}.
     * <p>
     *
     * @return {@link Signal} that emits those items from the source {@link Signal} that are
     *         distinct from their immediate predecessors.
     * @see #diff(Function)
     * @see #diff(BiPredicate)
     */
    public final Signal<V> diff() {
        return skip((V) null, Objects::equals);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits all items emitted by the source {@link Signal} that are
     * distinct from their immediate predecessors, according to a key selector function and based on
     * {@link Object#equals(Object)} comparison of those objects returned by the key selector
     * function.
     * </p>
     * <p>
     * It is recommended the keys' class {@code K} overrides the default {@code Object.equals()} to
     * provide meaningful comparison between the key objects as the default Java implementation only
     * considers reference equivalence. Alternatively, use the {@link #diff(BiPredicate)} overload
     * and provide a comparison function in case the class {@code K} can't be overridden with custom
     * {@code equals()} or the comparison itself should happen on different terms or properties of
     * the item class {@code V} (for which the keys can be derived via a similar selector).
     * </p>
     *
     * @param <K> the key type
     * @param keySelector A function that projects an emitted item to a key value that is used to
     *            decide whether an item is distinct from another one or not.
     * @return {@link Signal} that emits those items from the source {@link Signal} whose keys are
     *         distinct from those of their immediate predecessors.
     * @see #diff()
     * @see #diff(BiPredicate)
     */
    public final <K> Signal<V> diff(WiseFunction<V, K> keySelector) {
        // ignore invalid parameter
        if (keySelector == null) {
            return this;
        }
        return diff((prev, now) -> Objects.equals(keySelector.apply(prev), keySelector.apply(now)));
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits all items emitted by the source {@link Signal} that are
     * distinct from their immediate predecessors when compared with each other via the provided
     * comparator function.
     * </p>
     *
     * @param comparer The function that receives the previous item and the current item and is
     *            expected to return true if the two are equal, thus skipping the current value.
     * @return {@link Signal} that emits those items from the source {@link Signal} that are
     *         distinct from their immediate predecessors.
     * @see #diff()
     * @see #diff(Function)
     */
    public final Signal<V> diff(BiPredicate<V, V> comparer) {
        // ignore invalid parameter
        if (comparer == null) {
            return this;
        }

        return skip((V) null, (prev, now) -> {
            if (prev == null) {
                return now == null;
            } else if (now == null) {
                return false;
            } else {
                return comparer.test(prev, now);
            }
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
    public final Signal<V> distinct() {
        return distinct(I.wise(Function.identity()));
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     * </p>
     *
     * @return Chainable API.
     */
    public final <K> Signal<V> distinct(WiseFunction<V, K> keySelector) {
        return take(HashSet::new, (set, v) -> set.add(v == null ? null : keySelector.apply(v)), true, false, false);
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effect(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }
        return effect(effect.asConsumer());
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effect(WiseConsumer<? super V> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(I.bundle(effect, observer), observer::error, observer::complete, disposer);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect only once when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke only once when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effectOnce(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }
        return effectOnce(effect.asConsumer());
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect only once when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke only once when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effectOnce(WiseConsumer<? super V> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Subscriber<V> subscriber = new Subscriber();
            subscriber.observer = observer;
            subscriber.next = v -> {
                effect.accept(v);
                observer.accept(v);
                subscriber.next = null;
            };
            return to(subscriber, disposer);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#complete()}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#complete()}
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effectOnComplete(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, observer::error, I.bundle(effect, observer::complete), disposer);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#complete()}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#complete()}
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effectOnComplete(WiseConsumer<V> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            ArrayList<V> list = new ArrayList();

            return to(v -> {
                list.add(v);
                observer.accept(v);
            }, observer::error, () -> {
                list.forEach(effect::accept);
                observer.complete();
            }, disposer);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Disposable#dispose()}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Disposable#dispose()}
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effectOnDispose(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(observer, disposer.add(effect::run));
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#error(Throwable)}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#error(Throwable)}
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effectOnError(WiseConsumer<Throwable> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, I.bundle(effect, observer::error), observer::complete, disposer);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes the given effect when it is observed
     * from its observers. Each observation will result in an invocation of the given action except
     * when the source {@link Signal} is reference counted, in which case the source {@link Signal}
     * will invoke the given action for the first observation.
     *
     * @param effect The {@link Runnable} that gets called when an {@link Observer} subscribes to
     *            the current {@link Signal}.
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effectOnObserve(WiseRunnable effect) {
        return effectOnObserve(effect == null ? null : effect.asConsumer());
    }

    /**
     * Modifies the source {@link Signal} so that it invokes the given effect when it is observed
     * from its observers. Each observation will result in an invocation of the given action except
     * when the source {@link Signal} is reference counted, in which case the source {@link Signal}
     * will invoke the given action for the first observation.
     *
     * @param effect The {@link Consumer} that gets called when an {@link Observer} subscribes to
     *            the current {@link Signal}.
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effectOnObserve(WiseConsumer<? super Disposable> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            effect.accept(disposer);
            return to(observer, disposer);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#error(Throwable)} or {@link Observer#complete()}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#error(Throwable)} or {@link Observer#complete()}.
     * @return The source {@link Signal} with the side-effecting behavior applied.
     * @see #effect(Consumer)
     * @see #effectOnError(Consumer)
     * @see #effectOnComplete(Runnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(Runnable)
     * @see #effectOnObserve(Consumer)
     */
    public final Signal<V> effectOnTerminate(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, I.bundle(effect.asConsumer(), observer::error), I.bundle(effect, observer::complete), disposer);
        });
    }

    /**
     * <p>
     * Instructs an {@link Signal} to emit an item (returned by a specified function) rather than
     * invoking onError if it encounters an error.
     * </p>
     * 
     * @param resumer
     * @return Chainable API.
     */
    public final Signal<V> errorResume(Signal<? extends V> resumer) {
        if (resumer == null) {
            return this;
        }
        return errorResume(e -> resumer);
    }

    /**
     * <p>
     * Instructs an {@link Signal} to emit an item (returned by a specified function) rather than
     * invoking onError if it encounters an error.
     * </p>
     * 
     * @param resumer
     * @return Chainable API.
     */
    public final Signal<V> errorResume(WiseFunction<? super Throwable, Signal<? extends V>> resumer) {
        if (resumer == null) {
            return this;
        }
        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, e -> resumer.apply(e).to(observer, disposer), observer::complete, disposer);
        });
    }

    /**
     * Returns {@link Signal} that emits only the very first item emitted by the source
     * {@link Signal}, or completes if the source {@link Signal} is empty.
     * 
     * @return Chainable API.
     */
    public final Signal<V> first() {
        return first(null);
    }

    /**
     * Returns {@link Signal} that emits only the very first item emitted by the source
     * {@link Signal}, or completes if the source {@link Signal} is empty.
     * 
     * @return Chainable API.
     */
    public final Signal<V> first(V defaultValue) {
        return signal(I.accept(), null, FALSE, null, TRUE, defaultValue);
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
    public final <R> Signal<R> flatMap(WiseFunction<V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            Subscriber end = countable(observer, 1);

            return to(value -> {
                end.index++;
                function.apply(value).to(observer::accept, observer::error, end::complete, disposer.sub(), true);
            }, observer::error, end::complete, disposer);
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
    public final <R> Signal<R> flatVariable(WiseFunction<V, Variable<R>> function) {
        return flatMap(function.andThen(I::signal));
    }

    /**
     * Append index (starting from 0).
     * 
     * @return Chainable API.
     */
    public final Signal<Ⅱ<V, Long>> index() {
        return index(0);
    }

    /**
     * Append index (starting from the specified value).
     * 
     * @param start A starting index number.
     * @return Chainable API.
     */
    public final Signal<Ⅱ<V, Long>> index(long start) {
        return map(() -> new AtomicLong(start), (context, value) -> I.pair(value, context.getAndIncrement()));
    }

    /**
     * <p>
     * Ensure the interval time for each values in {@link Signal} sequence.
     * </p>
     *
     * @param interval Time to emit values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified interval. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> interval(long interval, TimeUnit unit) {
        // ignore invalid parameters
        if (interval <= 0 || unit == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            long time = unit.toNanos(interval);
            LinkedList queue = new LinkedList();
            AtomicLong next = new AtomicLong();

            Runnable sender = I.recurseR(self -> () -> {
                next.set(System.nanoTime() + time);
                Object item = queue.pollFirst();

                if (item == UNDEFINED) {
                    observer.complete();
                } else {
                    observer.accept((V) item);
                }

                if (!queue.isEmpty()) {
                    I.schedule(time, NANOSECONDS, false, self);
                }
            });

            return to(value -> {
                queue.add(value);
                if (queue.size() == 1) I.schedule(next.get() - System.nanoTime(), NANOSECONDS, false, sender);
            }, observer::error, () -> {
                queue.add(UNDEFINED);
                if (queue.size() == 1) I.schedule(next.get() - System.nanoTime(), NANOSECONDS, false, sender);
            }, disposer);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given {@link Predicate} function to each value
     * emitted by an {@link Signal} and emits the result.
     * </p>
     *
     * @param condition A conditional function to apply to each value emitted by this
     *            {@link Signal}.
     * @return Chainable API.
     */
    public final Signal<Boolean> is(Predicate<? super V> condition) {
        Objects.requireNonNull(condition);

        return map(condition::test);
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given {@link Predicate} function to each value
     * emitted by an {@link Signal} and emits the result.
     * </p>
     *
     * @param condition A conditional function to apply to each value emitted by this
     *            {@link Signal}.
     * @return Chainable API.
     */
    public final Signal<Boolean> isNot(Predicate<? super V> condition) {
        Objects.requireNonNull(condition);

        return is(condition.negate());
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is completed.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         completed.
     */
    public final Signal<Boolean> isCompleted() {
        return signal(null, FALSE, false, FALSE, true, TRUE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} emits any value.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} emits
     *         any value.
     */
    public final Signal<Boolean> isEmitted() {
        return signal(I.accept(), TRUE, true, FALSE, true, FALSE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is completed without any value emitted.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         completed without any value emitted.
     */
    public final Signal<Boolean> isEmpty() {
        return signal(I.accept(), FALSE, true, FALSE, true, TRUE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is errored.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         errored.
     */
    public final Signal<Boolean> isErrored() {
        return signal(null, FALSE, true, TRUE, true, FALSE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is emitted, errored or completed.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         emitted, errored or completed.
     */
    public final Signal<Boolean> isSignaled() {
        return signal(I.accept(), TRUE, true, TRUE, true, TRUE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is errored or completed.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         errored or completed.
     */
    public final Signal<Boolean> isTerminated() {
        return signal(null, FALSE, true, TRUE, true, TRUE);
    }

    /**
     * Returns a {@link Signal} that emits the last item emitted by this {@link Signal} or completes
     * if this {@link Signal} is empty.
     * 
     * @return Chainable API.
     */
    public final Signal<V> last() {
        return last(null);
    }

    /**
     * Returns a {@link Signal} that emits only the last item emitted by this {@link Signal}, or a
     * default item if this {@link Signal} completes without emitting any items.
     * 
     * @return Chainable API.
     */
    public final Signal<V> last(V defaultValue) {
        return buffer(NEVER, () -> new AtomicReference<V>(defaultValue), AtomicReference::set).map(AtomicReference::get).skipNull();
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
        Objects.requireNonNull(converter);

        return map((WiseSupplier) null, (context, value) -> converter.apply(value));
    }

    /**
     * {@link #map(Function)} with context.
     * 
     * @param contextSupplier A {@link Supplier} of {@link Signal} specific context.
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <C, R> Signal<R> map(WiseSupplier<C> contextSupplier, WiseBiFunction<C, ? super V, R> converter) {
        Objects.requireNonNull(converter);

        return new Signal<>((observer, disposer) -> {
            C context = contextSupplier == null ? null : contextSupplier.get();

            return to(value -> observer.accept(converter.apply(context, value)), observer::error, observer::complete, disposer);
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
    public final <R> Signal<R> maps(WiseBiFunction<? super V, ? super V, R> converter) {
        Objects.requireNonNull(converter);

        return buffer(2, 1).map(values -> converter.apply(values.get(0), values.get(1)));
    }

    /**
     * {@link #map(Function)} with previuos value.
     *
     * @param init A initial previous value.
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <R> Signal<R> maps(V init, WiseBiFunction<? super V, ? super V, R> converter) {
        Objects.requireNonNull(converter);

        return startWith(init).maps(converter);
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
        return merge(I.list(others));
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
    public final Signal<V> merge(Iterable<Signal<? extends V>> others) {
        // ignore invalid parameters
        if (others == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            List<Signal<? extends V>> signals = I.signal(others).skipNull().startWith(this).toList();
            Subscriber completer = countable(observer, signals.size());

            for (Signal<? extends V> signal : signals) {
                if (disposer.isDisposed()) {
                    break;
                }
                signal.to(observer::accept, observer::error, completer::complete, disposer.sub(), true);
            }
            return disposer;
        });
    }

    /**
     * Returns {@link Signal} that emits a Boolean that indicates whether all of the items emitted
     * by the source {@link Signal} unsatisfy a condition.
     * 
     * @param condition A condition that evaluates an item and returns a Boolean.
     * @return A {@link Signal} that emits false if all items emitted by the source {@link Signal}
     *         satisfy the predicate; otherwise, true.
     */
    public final Signal<Boolean> none(Predicate<? super V> condition) {
        Objects.requireNonNull(condition);

        return signal(condition, FALSE, false, FALSE, true, TRUE);
    }

    /**
     * Return an {@link Signal} that is observed as long as the specified timing {@link Signal}
     * indicates true. When the timing {@link Signal} returns false, the currently subscribed
     * {@link Signal} is immediately disposed.
     *
     * @param timing A timing whether the {@link Signal} is observed or not.
     * @return Chainable API.
     */
    public final Signal<V> observeWhile(Signal<Boolean> timing) {
        if (timing == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Disposable[] root = {Disposable.empty()};

            return disposer.add(timing.startWith(false).diff().to(v -> {
                if (v) {
                    root[0] = to(observer, disposer.sub());
                } else {
                    root[0].dispose();
                }
            }));
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
            scheduler.accept(() -> observer.accept(v));
        }, e -> {
            scheduler.accept(() -> observer.error(e));
        }, () -> {
            scheduler.accept(observer::complete);
        }, disposer));
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
        return repeatWhen(complete -> complete.take(count - 1));
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
        return repeatWhen(complete -> complete.takeWhile(v -> condition.getAsBoolean()), true);
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
        return repeatWhen(complete -> complete.takeUntil(stopper));
    }

    /**
     * Returns an {@link Signal} that emits the same values as the source signal with the exception
     * of an {@link Observer#error(Throwable)}. An error notification from the source will result in
     * the emission of a Throwable item to the {@link Signal} provided as an argument to the
     * notificationHandler function. If that {@link Signal} calls {@link Observer#complete()} or
     * {@link Observer#error(Throwable)} then retry will call {@link Observer#complete()} or
     * {@link Observer#error(Throwable) } on the child subscription. Otherwise, this {@link Signal}
     * will resubscribe to the source {@link Signal}.
     * 
     * @param notifier A receives an {@link Signal} of notifications with which a user can complete
     *            or error, aborting the retry.
     * @return Chainable API
     */
    public final Signal<V> repeatWhen(WiseFunction<Signal<? extends Object>, Signal<?>> notifier) {
        return repeatWhen(notifier, false);
    }

    /**
     * Returns an {@link Signal} that emits the same values as the source signal with the exception
     * of an {@link Observer#error(Throwable)}. An error notification from the source will result in
     * the emission of a Throwable item to the {@link Signal} provided as an argument to the
     * notificationHandler function. If that {@link Signal} calls {@link Observer#complete()} or
     * {@link Observer#error(Throwable)} then retry will call {@link Observer#complete()} or
     * {@link Observer#error(Throwable) } on the child subscription. Otherwise, this {@link Signal}
     * will resubscribe to the source {@link Signal}.
     * 
     * @param notifier A receives an {@link Signal} of notifications with which a user can complete
     *            or error, aborting the retry.
     * @return Chainable API
     */
    private Signal<V> repeatWhen(Function<Signal<? extends Object>, Signal<?>> notifier, boolean immediate) {
        return new Signal<>((observer, disposer) -> {
            // recorder for the processing complete
            Object[] processing = new Object[1];

            // build the actual complete handler
            Subscriber<Object> subscriber = new Subscriber();
            WiseRunnable complete = () -> {
                subscriber.accept(processing[0] = UNDEFINED);
            };

            // number of remaining repeats
            AtomicInteger remaining = new AtomicInteger();
            // previous repeat operation
            Disposable[] previous = new Disposable[] {Disposable.empty()};

            // define complete repeating flow
            notifier.apply(subscriber.signal()).to(v -> {
                processing[0] = null; // processing complete will be handled, so clear it

                // If you are not repeating, repeat it immediately, otherwise you can do it later
                if (remaining.getAndIncrement() == 0) {
                    do {
                        // dispose previous and reconnect
                        previous[0].dispose();
                        previous[0] = to(observer::accept, observer::error, complete, disposer.sub(), true);
                    } while (remaining.decrementAndGet() != 0);
                }
            }, observer::error, () -> {
                // Since this complete flow has ended,
                // all subsequent complete are passed to the source signal.
                if (immediate) {
                    observer.complete();
                } else {
                    subscriber.next = I.wise(observer::complete).asConsumer();

                    // Since there is a complete in processing, but this complete flow has ended,
                    // the processing complete is passed to the source signal.
                    if (processing[0] != null) observer.complete();
                }
            });

            // connect with complete handling flow
            previous[0] = to(observer::accept, observer::error, complete, disposer.sub(), true);

            // API difinition
            return disposer;
        });
    }

    /**
     * <p>
     * Recover the source {@link Signal} on any error by the specified value.
     * </p>
     * 
     * @param value A value to replace error.
     * @return Chainable API
     */
    public final Signal<V> recover(V value) {
        return recover(null, value);
    }

    /**
     * <p>
     * Recover the source {@link Signal} on the specified error by the specified value. Unspecified
     * error types will pass through the source {@link Signal}.
     * </p>
     * 
     * @param type An error type that you want to recover.
     * @param value A value to replace error.
     * @return Chainable API
     */
    public final Signal<V> recover(Class<? extends Throwable> type, V value) {
        return recoverWhen(type, fail -> fail.mapTo(value));
    }

    /**
     * <p>
     * Recover the source {@link Signal} on any error by the notifier emitting values.
     * </p>
     * <h>When the notifier signal emits event</h>
     * <ul>
     * <li>Next - Replace source error and propagate values to source signal.</li>
     * <li>Error - Propagate to source error and dispose them.</li>
     * <li>Complete - Terminate notifier signal. Souce signal will never recover errors.</li>
     * </ul>
     * 
     * @param notifier An error notifier to define recovering flow.
     * @return Chainable API
     */
    public final Signal<V> recoverWhen(WiseFunction<Signal<? extends Throwable>, Signal<V>> notifier) {
        return recoverWhen(null, notifier);
    }

    /**
     * <p>
     * Recover the source {@link Signal} on the specified error by the notifier emitting values.
     * Unspecified errors will pass through the source {@link Signal}.
     * </p>
     * <h>When the notifier signal emits event</h>
     * <ul>
     * <li>Next - Replace source error and propagate values to source signal.</li>
     * <li>Error - Propagate to source error and dispose them.</li>
     * <li>Complete - Terminate notifier signal. Souce signal will never recover errors.</li>
     * </ul>
     * 
     * @param type An error type that you want to recover.
     * @param notifier An error notifier to define recovering flow.
     * @return Chainable API
     */
    public final <E extends Throwable> Signal<V> recoverWhen(Class<E> type, WiseFunction<Signal<? extends E>, Signal<V>> notifier) {
        // ignore invalid parameter
        if (notifier == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            // error notifier
            Subscriber<E> error = new Subscriber();
            error.next = e -> {
                if (e instanceof UndeclaredThrowableException) {
                    e = (E) e.getCause();
                }

                if (type == null || type.isInstance(e)) {
                    error.observer.accept(e);
                } else {
                    observer.error(e);
                }
            };

            // define error recovering flow
            notifier.apply(error.signal()).to(observer::accept, observer::error, () -> error.next = observer::error);

            // delegate error to the notifier
            return to(observer::accept, error, observer::complete, disposer);
        });
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that retry the given value infinitely.
     * </p>
     *
     * @return Chainable API.
     */
    public final Signal<V> retry() {
        return retryUntil(NEVER);
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that retry the given value finitely.
     * </p>
     *
     * @param count A number of retry. Zero or negative number will ignore this instruction.
     * @return Chainable API.
     */
    public final Signal<V> retry(int count) {
        // ignore invalid parameter
        if (count < 1) {
            return this;
        }
        return retryWhen(fail -> fail.take(count));
    }

    /**
     * <p>
     * Returns an {@link Signal} that retry the sequence of items emitted by the source
     * {@link Signal} until the provided condition function returns false.
     * </p>
     * 
     * @param condition A condition supplier that is called when the current {@link Signal}
     *            completes and unless it returns false, the current {@link Signal} is resubscribed.
     * @return Chainable API.
     */
    public final Signal<V> retryIf(BooleanSupplier condition) {
        return retryIf(null, condition);
    }

    /**
     * <p>
     * Retry the source {@link Signal} whenever the specified error is occured if the stopper is
     * signaled.
     * </p>
     * 
     * @param type An error type that you want to retry.
     * @param condition A {@link Signal} whose first emitted item will stop retrying.
     * @return Chainable API.
     */
    public final Signal<V> retryIf(Class<? extends Throwable> type, BooleanSupplier condition) {
        return retryWhen(type, fail -> fail.map(v -> {
            if (condition == null || condition.getAsBoolean()) {
                return v;
            } else {
                throw v;
            }
        }));
    }

    /**
     * <p>
     * Retry the source {@link Signal} whenever any error is occured until the stopper is signaled.
     * </p>
     * 
     * @param stopper A {@link Signal} whose first emitted item will stop retrying.
     * @return Chainable API.
     */
    public final Signal<V> retryUntil(Signal stopper) {
        return retryUntil(null, stopper);
    }

    /**
     * <p>
     * Retry the source {@link Signal} whenever the specified error is occured until the stopper is
     * signaled.
     * </p>
     * 
     * @param type An error type that you want to retry.
     * @param stopper A {@link Signal} whose first emitted item will stop retrying.
     * @return Chainable API.
     */
    public final Signal<V> retryUntil(Class<? extends Throwable> type, Signal stopper) {
        return retryWhen(type, fail -> fail.takeUntil(stopper));
    }

    /**
     * <p>
     * Retry the source {@link Signal} whenever any error is occured.
     * </p>
     * <h>When the notifier signal emits event</h>
     * <ul>
     * <li>Next - Retry source {@link Signal}.</li>
     * <li>Error - Propagate to source error and dispose them.</li>
     * <li>Complete - Terminate notifier signal. Souce signal will never retry errors.</li>
     * </ul>
     * 
     * @param type An error type that you want to retry.
     * @param notifier An error notifier to define retrying flow.
     * @return Chainable API
     */
    public final Signal<V> retryWhen(WiseFunction<Signal<? extends Throwable>, Signal<?>> notifier) {
        return retryWhen(null, notifier);
    }

    /**
     * <p>
     * Retry the source {@link Signal} when the specified error is occured. Unspecified errors will
     * pass through the source {@link Signal}.
     * </p>
     * <h>When the notifier signal emits event</h>
     * <ul>
     * <li>Next - Retry source {@link Signal}.</li>
     * <li>Error - Propagate to source error and dispose them.</li>
     * <li>Complete - Terminate notifier signal. Souce signal will never retry errors.</li>
     * </ul>
     * 
     * @param type An error type that you want to retry.
     * @param flow An error notifier to define retrying flow.
     * @return Chainable API
     */
    public final <E extends Throwable> Signal<V> retryWhen(Class<E> type, WiseFunction<Signal<? extends E>, Signal<?>> flow) {
        return new Signal<>((observer, disposer) -> {
            // recorder for the processing error
            Throwable[] processing = new Throwable[1];

            // build the actual error handler
            Subscriber<E> error = new Subscriber();
            error.next = e -> {
                if (e instanceof UndeclaredThrowableException) {
                    e = (E) e.getCause();
                }
                // determine whether errors should be handled by type
                if (type == null || type.isInstance(e)) {
                    error.observer.accept(processing[0] = e); // to user defined error flow
                } else {
                    observer.error(e); // to source signal
                }
            };

            // number of remaining retrys
            AtomicInteger remaining = new AtomicInteger();
            // previous retry operation
            Disposable[] previous = new Disposable[] {Disposable.empty()};

            // define error retrying flow
            flow.apply(error.signal()).to(v -> {
                processing[0] = null; // processing error will be handled, so clear it

                // If you are not retrying, retry it immediately, otherwise you can do it later
                if (remaining.getAndIncrement() == 0) {
                    do {
                        // dispose previous and reconnect
                        previous[0].dispose();
                        previous[0] = to(observer::accept, error, observer::complete, disposer.sub(), true);
                    } while (remaining.decrementAndGet() != 0);
                }
            }, observer::error, () -> {
                // Since this error flow has ended,
                // all subsequent errors are passed to the source signal.
                error.next = observer::error;

                // Since there is an error in processing, but this error flow has ended,
                // the processing error is passed to the source signal.
                if (processing[0] != null) observer.error(processing[0]);
            });

            // connect with error handling flow
            previous[0] = to(observer::accept, error, observer::complete, disposer.sub(), true);

            // API definition
            return disposer;
        });
    }

    /**
     * <p>
     * Buffer all values until complete, then all buffered values are emitted in descending order.
     * </p>
     * 
     * @return Chainable API.
     */
    public final Signal<V> reverse() {
        return buffer(NEVER, LinkedList<V>::new, Deque::addFirst).flatIterable(I.wise(Function.identity()));
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
    public final Signal<V> sample(Signal<?> sampler) {
        return buffer(sampler, AtomicReference<V>::new, AtomicReference<V>::set, true).map(AtomicReference<V>::get);
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
     * @param collector An accumulator function to be invoked on each item emitted by the source
     *            {@link Signal}, whose result will be emitted to {@link Signal} via
     *            {@link Observer#accept(Object)} and used in the next accumulator call.
     * @return An {@link Signal} that emits initial value followed by the results of each call to
     *         the accumulator function.
     */
    public final <A, R> Signal<R> scan(Collector<? super V, A, R> collector) {
        return new Signal<>((observer, disposer) -> {
            A ref = collector.supplier().get();

            return to(value -> {
                collector.accumulator().accept(ref, value);
                observer.accept(collector.finisher().apply(ref));
            }, observer::error, observer::complete, disposer);
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
     * @param first An accumulator which process only first value.
     * @param others An accumulator function to be invoked on each item emitted by the source
     *            {@link Signal}, whose result will be emitted to {@link Signal} via
     *            {@link Observer#accept(Object)} and used in the next accumulator call.
     * @return An {@link Signal} that emits initial value followed by the results of each call to
     *         the accumulator function.
     */
    public final <R> Signal<R> scan(WiseFunction<V, R> first, WiseBiFunction<R, V, R> others) {
        return new Signal<>((observer, disposer) -> {
            AtomicReference<R> ref = new AtomicReference(UNDEFINED);

            return to(v -> {
                observer.accept(ref.updateAndGet(prev -> prev == UNDEFINED ? first.apply(v) : others.apply(prev, v)));
            }, observer::error, observer::complete, disposer);
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
    public final <R> Signal<R> scanWith(R init, WiseBiFunction<R, V, R> function) {
        return scan(Collector.of(() -> new AtomicReference<R>(init), (ref, value) -> {
            ref.set(function.apply(ref.get(), value));
        }, (a, b) -> a, AtomicReference<R>::get));
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
        Disposable[] root = new Disposable[1];
        List<Observer<? super V>> observers = new CopyOnWriteArrayList();

        return new Signal<>((observer, disposer) -> {
            observers.add(observer);

            if (observers.size() == 1) {
                root[0] = to(v -> {
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
                });
            }

            return disposer.add(() -> {
                observers.remove(observer);

                if (observers.isEmpty() && root[0] != null) {
                    root[0].dispose();
                    root[0] = null;
                }
            });
        });
    }

    /**
     * Returns {@link Signal} that emits the single element only. If this {@link Signal} has no
     * element or too many elements, signal will complete immediately.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         emitted, errored or completed.
     */
    public final Signal<V> single() {
        return size(1).map(v -> v.get(0));
    }

    /**
     * Returns a {@link Signal} that counts the total number of items emitted by the source
     * {@link Signal} and emits this count as a 32-bit Integer.
     * 
     * @return {@link Signal} that emits a single item: the number of items emitted by the source
     *         {@link Signal} as a 32-bit Integer item
     */
    public final Signal<List<V>> size(int size) {
        if (size < 0) {
            return EMPTY;
        }

        return new Signal<>((observer, disposer) -> {
            ArrayList<V> list = new ArrayList(size);

            return to(v -> {
                if (list.size() < size) {
                    list.add(v);
                } else {
                    observer.complete();
                }
            }, observer::error, () -> {
                if (list.size() == size) observer.accept(list);
                observer.complete();
            }, disposer);
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
    public final Signal<V> skip(Predicate<? super V> condition) {
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
    public final Signal<V> skip(V init, BiPredicate<? super V, ? super V> condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }
        return take(init, condition.negate());
    }

    /**
     * {@link #skip(Predicate)} with context.
     * 
     * @param contextSupplier A {@link Supplier} of {@link Signal} specific context.
     * @param condition A condition function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <C> Signal<V> skip(Supplier<C> contextSupplier, BiPredicate<C, ? super V> condition) {
        // ignore invalid parameters
        if (condition == null) {
            return this;
        }
        return take(contextSupplier, condition.negate());
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
     * Skip all items emitted by the source {@link Signal} and only calls
     * {@link Observer#complete()} or {@link Observer#error(Throwable)}.
     * 
     * @return An empty {@link Signal} that only calls {@link Observer#complete()} or
     *         {@link Observer#error(Throwable)}, based on which one is called by the
     *         {@link Signal}. Observable
     */
    public final Signal<V> skipAll() {
        return skip(I.accept());
    }

    /**
     * <p>
     * Returns a specified index values from the start of an {@link Signal} sequence.
     * </p>
     * 
     * @param condition A index condition of values to emit.
     * @return Chainable API.
     */
    public final Signal<V> skipAt(LongPredicate condition) {
        return takeAt(condition.negate());
    }

    /**
     * Return the {@link Signal} which ignores all errors.
     * 
     * @return {@link Signal} which ignores all errors.
     */
    public final Signal<V> skipError() {
        return skipError(null);
    }

    /**
     * Return the {@link Signal} which ignores the specified error.
     * 
     * @param type A error type to ignore.
     * @return {@link Signal} which ignores the specified error.
     */
    public final Signal<V> skipError(Class<? extends Throwable> type) {
        return recoverWhen(type, fail -> NEVER);
    }

    /**
     * Return the {@link Signal} which ignores complete event.
     * 
     * @return {@link Signal} which ignores complete event.
     */
    public final Signal<V> skipComplete() {
        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, observer::error, I.NoOP, disposer);
        });
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
        return take(timing.isSignaled());
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
     * Buffer all items until complete event and then soted items will be emitted sequentially.
     * 
     * @param comparator
     * @return Chainable API.
     */
    public final Signal<V> sort(Comparator<? super V> comparator) {
        return buffer().effect(e -> e.sort(comparator)).flatIterable(e -> e);
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
    public final Signal<V> startWith(Supplier<V> value) {
        return value == null ? this : startWith(value.get());
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
        // ignore invalid parameter
        if (values == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            while (values.hasMoreElements() && disposer.isNotDisposed()) {
                observer.accept(values.nextElement());
            }
            return to(observer, disposer);
        });
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
                while (iterator.hasNext() && disposer.isNotDisposed()) {
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
        // ignore invalid parameter
        if (values == null) {
            return this;
        }
        return startWith(values::iterator);
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
    public final Signal<V> startWith(WiseSupplier<V> value) {
        return startWith(value.get()).skipNull();
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
            values.to(observer::accept, observer::error, () -> to(observer, disposer), disposer.sub(), true);
            return disposer;
        });
    }

    /**
     * <p>
     * Emit <code>null</code> item before beginning to emit the items from the source
     * {@link Signal}.
     * </p>
     * 
     * @return Chainable API.
     */
    public final Signal<V> startWithNull() {
        return startWith((V) null);
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
    public final <R> Signal<R> switchMap(WiseFunction<V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            Disposable[] disposables = {null, Disposable.empty()};
            Subscriber end = countable(observer, 1);

            disposables[0] = to(value -> {
                end.index++;
                disposables[1].dispose();
                disposables[1] = function.apply(value).to(observer::accept, observer::error, end::complete, disposer.sub(), true);
            }, observer::error, end::complete, disposer.sub());
            return disposer.add(() -> {
                disposables[0].dispose();
                disposables[1].dispose();
            });
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
    public final Signal<V> take(long count) {
        // ignore invalid parameter
        if (count <= 0) {
            return this;
        }
        return take(AtomicLong::new, (context, value) -> context.incrementAndGet() < count, true, true, true);
    }

    /**
     * <p>
     * Alias for take(I.set(includes)).
     * </p>
     *
     * @param includes A collection of take items.
     * @return Chainable API.
     */
    public final Signal<V> take(V... includes) {
        // ignore invalid parameter
        if (includes == null) {
            return this;
        }
        return take(I.set(includes));
    }

    /**
     * <p>
     * Alias for take(v -> includes.contains(v)).
     * </p>
     *
     * @param includes A collection of take items.
     * @return Chainable API.
     */
    public final Signal<V> take(Collection<V> includes) {
        // ignore invalid parameter
        if (includes == null) {
            return this;
        }
        return take(includes::contains);
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
    public final Signal<V> take(Predicate<? super V> condition) {
        // ignore invalid parameters
        if (condition == null) {
            return this;
        }
        return take((Supplier) null, (context, value) -> condition.test(value));
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
    public final Signal<V> take(V init, BiPredicate<? super V, ? super V> condition) {
        // ignore invalid parameters
        if (condition == null) {
            return this;
        }
        return take(() -> new AtomicReference<>(init), (context, value) -> condition.test(context.getAndSet(value), value));
    }

    /**
     * {@link #take(Predicate)} with context.
     * 
     * @param contextSupplier A {@link Supplier} of {@link Signal} specific context.
     * @param condition A condition function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <C> Signal<V> take(Supplier<C> contextSupplier, BiPredicate<C, ? super V> condition) {
        return take(contextSupplier, condition, true, false, false);
    }

    public final Signal<V> take(BooleanSupplier condition) {
        return take(v -> condition.getAsBoolean());
    }

    public final Signal<V> take(Supplier<Boolean> condition) {
        return take(v -> condition.get());
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

            return to(v -> {
                if (flag.get()) {
                    observer.accept(v);
                }
            }, observer::error, observer::complete, disposer).add(condition.to(flag::set));
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
    public final Signal<V> takeAt(LongPredicate condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }
        return take(AtomicLong::new, (context, value) -> condition.test(context.getAndIncrement()), true, false, false);
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
        return take(() -> System.nanoTime() + unit.toNanos(time), (limit, value) -> System.nanoTime() < limit, true, true, false);
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
        return take(() -> value, Objects::equals, false, true, true);
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
    public final Signal<V> takeUntil(Predicate<? super V> condition) {
        return take(null, (context, value) -> condition.test(value), false, true, true);
    }

    /**
     * Returns {@link Signal} that emits the items emitted by the source {@link Signal} until a
     * second {@link Signal} emits an item.
     *
     * @param timing A {@link Signal} whose first emitted item will cause takeUntil to stop emitting
     *            items from the source {@link Signal}. <code>null</code> will ignore this.
     *            instruction.
     * @return A {@link Signal} that emits the items emitted by the source {@link Signal} until such
     *         time as other emits its first item.
     */
    public final Signal<V> takeUntil(Signal timing) {
        // ignore invalid parameter
        if (timing == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(observer, disposer).add(timing.isSignaled().to(observer::complete));
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
    public final Signal<V> takeWhile(Predicate<? super V> condition) {
        return take(null, (context, value) -> condition.test(value), true, true, false);
    }

    /**
     * Take operator helper.
     * 
     * @param contextSupplier
     * @param condition
     * @param stopOnFail
     * @param includeOnStop
     * @return
     */
    private <C> Signal<V> take(Supplier<C> contextSupplier, BiPredicate<C, ? super V> condition, boolean expected, boolean stopOnFail, boolean includeOnStop) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            // Normally, Signal will be diposed automatically after a COMPLETE event is sent. But it
            // is not immediately disposed if the COMPLETE event itself is delayed for some
            // reason (#delay, #interval or #on, etc).
            // In that case, even though the current signal has already stopped event propagation,
            // it is possible to propagate the subsequent event to the next signal.
            // There are several ways to avoid this.
            //
            // 1. Execute "dispose" immediately at the same time as the complete event
            // Although this way seems to work at first glance, problems will arise in #repeat or
            // #recover and so on because invokeing "dispose" forcedly.
            //
            // 2. The sender transmits the event while sequentially checking Disposable#isDisposed.
            // The DISPOSE event itself is delayed and issued, so it can not be used.
            //
            // 3. After COMPLETE, stop all the events
            // Here we adopt this way.
            AtomicBoolean stopped = new AtomicBoolean();
            C context = contextSupplier == null ? null : contextSupplier.get();

            return to(value -> {
                if (stopped.get() == false) {
                    if (condition.test(context, value) == expected) {
                        observer.accept(value);
                    } else {
                        if (stopOnFail) {
                            stopped.set(true); // flag up immediately
                            if (includeOnStop) observer.accept(value);
                            observer.complete();
                        }
                    }
                }
            }, observer::error, observer::complete, disposer);
        });
    }

    /**
     * Returns an Signal that mirrors the source Signal but applies a timeout policy for each
     * emitted item. If the next item isn't emitted within the specified timeout duration starting
     * from its predecessor, the resulting Signal terminates and notifies observers of a
     * {@link TimeoutException}.
     * 
     * @param time Time to take values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Signal<V> timeout(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Runnable timeout = () -> {
                observer.error(new TimeoutException());
                disposer.dispose();
            };

            AtomicReference<Future<?>> future = new AtomicReference<>(I.schedule(time, unit, true, timeout));

            return to(v -> {
                future.getAndSet(I.schedule(time, unit, true, timeout)).cancel(false);
                observer.accept(v);
            }, e -> {
                future.get().cancel(false);
                observer.error(e);
            }, () -> {
                future.get().cancel(false);
                observer.complete();
            }, disposer);
        });
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

        return new Signal<>((observer, disposer) -> {
            AtomicInteger count = new AtomicInteger();

            return to(value -> observer
                    .accept(values[count.getAndIncrement() % values.length]), observer::error, observer::complete, disposer);
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

        long delay = unit.toNanos(time);
        return take(AtomicLong::new, (context, value) -> {
            long now = System.nanoTime();

            if (context.get() + delay <= now) {
                context.set(now);
                return true;
            }
            return false;
        });
    }

    /**
     * Ensures that all values of the {@link Signal} are the specified type. If an unspecified typed
     * value flows, {@link Signal} will be completed immediately.
     *
     * @param types A list of types you want.
     * @return Chainable API.
     */
    public final Signal<V> type(Class... types) {
        // ignore invalid parameters
        if (types == null) {
            return this;
        }

        return takeWhile(v -> {
            for (Class type : types) {
                if (I.wrap(type).isInstance(v)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Returns {@link Signal} that emits the items emitted by the source {@link Signal} shifted
     * forward in time by a specified delay at current thread. Error notifications from the source
     * {@link Signal} are not delayed.
     *
     * @param time The delay to shift the source by.
     * @param unit The {@link TimeUnit} in which {@code period} is defined.
     * @return The source {@link Signal} shifted in time by the specified delay.
     * @see #delay(long, TimeUnit)
     */
    public final Signal<V> wait(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }
        return effect(() -> Thread.sleep(unit.toMillis(time)));
    }

    /**
     * Create countable completer.
     * 
     * @param delgator A complete action.
     * @param count A complete count.
     * @return Chainable API.
     */
    private Subscriber countable(Observer delgator, int count) {
        Subscriber completer = new Subscriber();
        completer.index = count;
        completer.complete = () -> {
            completer.index--;
            if (completer.index == 0) {
                delgator.complete();
            }
        };
        return completer;
    }

    /**
     * Signal detection operator helper.
     * 
     * @param emitCondition A value condition.
     * @param emitOutput A required condition output.
     * @param acceptError
     * @param errorOutput
     * @param acceptComplete
     * @param completeOuput
     * @return Chainable API.
     */
    private <T> Signal<T> signal(Predicate<? super V> emitCondition, T emitOutput, boolean acceptError, T errorOutput, boolean acceptComplete, T completeOuput) {
        return new Signal<>((observer, disposer) -> {
            return to(v -> {
                if (emitCondition != null && emitCondition.test(v)) {
                    observer.accept(emitOutput == null ? (T) v : emitOutput);
                    observer.complete();
                    disposer.dispose();
                }
            }, e -> {
                if (acceptError) {
                    if (errorOutput != null) observer.accept(errorOutput);
                    observer.complete();
                    disposer.dispose();
                } else {
                    observer.error(e);
                }
            }, () -> {
                if (acceptComplete && completeOuput != null) observer.accept(completeOuput);
                observer.complete();
                disposer.dispose();
            }, disposer);
        });
    }

    // /**
    // * <p>
    // * Append the current time to each events.
    // * </p>
    // *
    // * @return Chainable API.
    // */
    // public final Signal<Ⅱ<V, Instant>> timeStamp() {
    // return map(value -> I.pair(value, Instant.now()));
    // }
    //
    // /**
    // * <p>
    // * Append {@link Duration} between the current value and the previous value.
    // * </p>
    // *
    // * @return Chainable API.
    // */
    // public final Signal<Ⅱ<V, Duration>> timeInterval() {
    // return timeStamp().map((Ⅱ) null, (prev, now) -> I.pair(now.ⅰ, Duration.between(prev == null ?
    // now.ⅱ : prev.ⅱ, now.ⅱ)));
    // }
    //
    // /**
    // * <p>
    // * Append {@link Duration} between the current value and the first value.
    // * </p>
    // *
    // * @return Chainable API.
    // */
    // public final Signal<Ⅱ<V, Duration>> timeElapsed() {
    // return map(Variable::<Instant> empty, (context, value) -> {
    // Instant prev = context.let(Instant::now);
    //
    // return I.pair(value, prev == null ? Duration.ZERO : Duration.between(prev, Instant.now()));
    // });
    // }
}
