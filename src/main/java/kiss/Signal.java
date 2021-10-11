/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static java.lang.Boolean.*;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * <p>
 * The {@link Signal} class that implements the Reactive Pattern. This class provides methods for
 * subscribing to the {@link Signal} as well as delegate methods to the various observers.
 * </p>
 * <p>
 * In Reactive Pattern an observer subscribes to a {@link Signal}. Then that observer reacts to
 * whatever item or sequence of items the {@link Signal} emits. This pattern facilitates concurrent
 * operations because it does not need to block while waiting for the {@link Signal} to emit
 * objects, but instead it creates a sentry in the form of an observer that stands ready to react
 * appropriately at whatever future time the {@link Signal} does so.
 * </p>
 * <p>
 * The subscribe method is how you connect an {@link Observer} to a {@link Signal}. Your
 * {@link Observer} implements some subset of the following methods:
 * </p>
 * <dl>
 * <dt>{@link Observer#accept(Object)}</dt>
 * <dd>A {@link Signal} calls this method whenever the {@link Signal} emits an item. This method
 * takes as a parameter the item emitted by the {@link Signal}.</dd>
 * <dt>{@link Observer#error(Throwable)}</dt>
 * <dd>A {@link Signal} calls this method to indicate that it has failed to generate the expected
 * data or has encountered some other error. It will not make further calls to
 * <code> {@link Observer#error(Throwable)} </code> or <code> {@link Observer#complete()} </code>.
 * The <code> {@link Observer#error(Throwable)} </code> method takes as its parameter an indication
 * of what caused the error.</dd>
 * <dt>{@link Observer#complete()}</dt>
 * <dd>A {@link Signal} calls this method after it has called
 * <code> {@link Observer#accept(Object)}</code> for the final time, if it has not encountered any
 * errors.</dd>
 * </dl>
 * <p>
 * By the terms of the {@link Signal} contract, it may call
 * <code> {@link Observer#accept(Object)} </code> zero or more times, and then may follow those
 * calls with a call to either <code> {@link Observer#complete()}</code> or
 * <code> {@link Observer#error(Throwable)}</code> but not both, which will be its last call. By
 * convention, in this document, calls to <code> {@link Observer#accept(Object)}</code> are usually
 * called &ldquo;emissions&rdquo; of items, whereas calls to
 * <code> {@link Observer#complete()}</code> or <code> {@link Observer#error(Throwable)}</code> are
 * called &ldquo;notifications.&rdquo;
 * </p>
 * 
 * @ChainableAPI A new {@link Signal} instance. {@link Signal} is the definition (blueprint) of the
 *               process, and does not perform any processing by itself. In order to perform the
 *               actual processing, you need to execute the termination operation (toXXX method,
 *               i.e. {@link Signal#to()} or {@link Signal#to(Consumer)}).
 */
public final class Signal<V> {

    /**
     * For reuse.
     */
    public static final <R> Signal<R> never() {
        return new Signal<>((observer, disposer) -> disposer);
    }

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
     * Create {@link Signal} preassign the specified subscriber {@link Collection} which will be
     * invoked whenever you calls {@link #to(Observer)} related methods.
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

            return disposer.add(() -> {
                observers.remove(observer);
            });
        });
    }

    /**
     * Create {@link Signal} preassign the specified subscriber {@link BiFunction} which will be
     * invoked whenever you calls {@link #to(Observer)} related methods.
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
     * An {@link Observer} must call an Observable's {@code subscribe} method in order to receive
     * items and notifications from the Observable.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Runnable next) {
        return to(I.wiseC(next), null, null);
    }

    /**
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
     * Receive values from this {@link Signal}.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @param error A delegator method of {@link Observer#error(Throwable)}.
     * @param complete A delegator method of {@link Observer#complete()}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Runnable next, Consumer<Throwable> error, Runnable complete) {
        return to(I.wiseC(next), error, complete);
    }

    /**
     * Receive values from this {@link Signal}.
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
     * Receive values from this {@link Signal}.
     *
     * @param observer A value observer of this {@link Signal}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Observer<? super V> observer) {
        Subscriber subscriber = new Subscriber();
        subscriber.index = 1;
        subscriber.disposer = Disposable.empty();
        subscriber.observer = observer;

        return to(subscriber, subscriber.disposer);
    }

    /**
     * Receive values from this {@link Signal}.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @param error A delegator method of {@link Observer#error(Throwable)}.
     * @param complete A delegator method of {@link Observer#complete()}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    private Disposable to(Consumer<? super V> next, Consumer<? extends Throwable> error, Runnable complete, Disposable disposer, boolean auto) {
        Subscriber<V> subscriber = new Subscriber();
        subscriber.index = auto ? 1 : 0;
        subscriber.disposer = disposer;
        subscriber.next = next;
        subscriber.error = (Consumer<Throwable>) error;
        subscriber.complete = complete;

        return to(subscriber, disposer);
    }

    /**
     * Receive values from this {@link Signal}.
     *
     * @param observer A value observer of this {@link Signal}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Observer<? super V> observer, Disposable disposer) {
        try {
            return subscriber.apply(observer, disposer);
        } catch (Throwable e) {
            observer.error(e);
            return disposer;
        }
    }

    /**
     * Receive values as {@link Variable} from this {@link Signal}.
     *
     * @return A {@link Variable} as value receiver.
     */
    public final Variable<V> to() {
        return to(Variable.empty(), Variable::set);
    }

    /**
     * Receive values as {@link Variable} from this {@link Signal}.
     *
     * @return A {@link Variable} as value receiver.
     */
    public final <A, R> R to(Collector<? super V, A, R> receiver) {
        return receiver.finisher().apply(to(receiver.supplier().get(), receiver.accumulator()::accept));
    }

    /**
     * Receive values from this {@link Signal}.
     *
     * @param receiver A value receiver.
     * @param assigner A value assigner.
     * @return A value receiver.
     */
    public final <R> R to(R receiver, BiConsumer<R, V> assigner) {
        // start receiving values
        to(I.wiseBC(assigner).bind(receiver));

        // API definition
        return receiver;
    }

    /**
     * Receive values as {@link Set} from this {@link Signal}. Each value alternates between In and
     * Out.
     *
     * @return A {@link Set} as value receiver.
     */
    public final Set<V> toAlternate() {
        return to(new HashSet(), (set, value) -> {
            if (!set.add(value)) {
                set.remove(value);
            }
        });
    }

    /**
     * Receive values as {@link Collection} from this {@link Signal}.
     *
     * @return A {@link Collection} as value receiver.
     */
    public final <C extends Collection<? super V>> C toCollection(C collection) {
        return to(collection, Collection::add);
    }

    /**
     * Groups the elements according to the classification function and returns the result in a
     * {@link Map}. The classification function maps elements to some key type K. The method
     * produces a Map<K, List<V>> whose keys are the values resulting from applying the
     * classification function to the input elements, and whose corresponding values are Lists
     * containing the input elements which map to the associated key under the classification
     * function.
     * <p>
     * There are no guarantees on the type, mutability, serializability, or thread-safety of the
     * {@link Map} or {@link List} objects returned.
     * 
     * @param <K> The type of the keys.
     * @param keyGenerator The classifier function mapping input elements to keys.
     * @return A grouping {@link Map} by your lassification.
     */
    public final <K> Map<K, List<V>> toGroup(Function<V, K> keyGenerator) {
        return to(Collectors.groupingBy(keyGenerator));
    }

    /**
     * Receive values as {@link List} from this {@link Signal}.
     *
     * @return A {@link List} as value receiver.
     */
    public final List<V> toList() {
        return toCollection(new ArrayList());
    }

    /**
     * Receive values as {@link Map} from this {@link Signal}.
     * 
     * @param keyGenerator A {@link Map} key generator.
     * @return A {@link Map} as value receiver.
     */
    public final <Key> Map<Key, V> toMap(Function<V, Key> keyGenerator) {
        return toMap(keyGenerator, Function.identity());
    }

    /**
     * Receive values as {@link Map} from this {@link Signal}.
     * 
     * @param keyGenerator A {@link Map} key generator.
     * @param valueGenerator A {@link Map} value generator.
     * @return A {@link Map} as value receiver.
     */
    public final <Key, Value> Map<Key, Value> toMap(Function<V, Key> keyGenerator, Function<V, Value> valueGenerator) {
        return to(Collectors.toMap(keyGenerator, valueGenerator, (a, b) -> b));
    }

    /**
     * Receive values as {@link Set} from this {@link Signal}.
     *
     * @return A {@link Set} as value receiver.
     */
    public final Set<V> toSet() {
        return toCollection(new HashSet());
    }

    /**
     * Returns {@link Signal} that emits a Boolean that indicates whether all of the items emitted
     * by the source {@link Signal} satisfy a condition.
     * 
     * @param condition A condition that evaluates an item and returns a Boolean.
     * @return {ChainableAPI}
     */
    public final Signal<Boolean> all(Predicate<? super V> condition) {
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
        return signal(Objects.requireNonNull(condition), TRUE, false, FALSE, true, FALSE);
    }

    /**
     * Filters the values of an {@link Signal} sequence based on the specified type.
     *
     * @param type The type of result. <code>null</code> throws {@link NullPointerException}.
     * @return {ChainableAPI}
     * @throws NullPointerException If the type is <code>null</code>.
     */
    public final <R> Signal<R> as(Class<R> type) {
        return (Signal<R>) (type == null ? this : take(I.wrap(type)::isInstance));
    }

    /**
     * Indicates each value of an {@link Signal} sequence into consecutive non-overlapping buffers
     * which are produced based on value count information.
     *
     * @return {ChainableAPI}
     */
    public final Signal<List<V>> buffer() {
        return buffer(never());
    }

    /**
     * Indicates each value of an {@link Signal} sequence into consecutive non-overlapping buffers
     * which are produced based on value count information.
     *
     * @param size A length of each buffer.
     * @return {ChainableAPI}
     */
    public final Signal<List<V>> buffer(int size) {
        return buffer(size, size);
    }

    /**
     * Indicates each values of an {@link Signal} sequence into zero or more buffers which are
     * produced based on value count information.
     *
     * @param size A length of each buffer. Zero or negative number are treated exactly the same way
     *            as 1.
     * @param interval A number of values to skip between creation of consecutive buffers. Zero or
     *            negative number are treated exactly the same way as 1.
     * @return {ChainableAPI}
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
            }, observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * Indicates each values of an {@link Signal} sequence into zero or more buffers which are
     * produced based on time count information.
     *
     * @param time Time to collect values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @param scheduler An event scheduler.
     * @return {ChainableAPI}
     */
    public final Signal<List<V>> buffer(long time, TimeUnit unit, ScheduledExecutorService... scheduler) {
        return buffer(I.schedule(time, time, unit, true, scheduler));
    }

    /**
     * Returns an {@link Signal} that emits non-overlapping buffered items from the source
     * {@link Signal} each time the specified boundary {@link Signal} emits an item.
     * 
     * @param boundary A boundary {@link Signal}.
     * @return {ChainableAPI}
     */
    public final Signal<List<V>> buffer(Signal<?> boundary) {
        return buffer(boundary, ArrayList::new);
    }

    /**
     * Returns an {@link Signal} that emits non-overlapping buffered items from the source
     * {@link Signal} each time the specified boundary {@link Signal} emits an item.
     * 
     * @param boundary A boundary {@link Signal}.
     * @param supplier A factory function that returns an instance of the collection subclass to be
     *            used and returned as the buffer.
     * @return {ChainableAPI}
     */
    public final <B extends Collection<V>> Signal<B> buffer(Signal<?> boundary, Supplier<B> supplier) {
        return buffer(boundary, supplier, Collection::add).skip(B::isEmpty);
    }

    /**
     * Returns an {@link Signal} that emits non-overlapping buffered items from the source
     * {@link Signal} each time the specified boundary {@link Signal} emits an item.
     * 
     * @param boundary A boundary {@link Signal}.
     * @param supplier A factory function that returns an instance of the collection subclass to be
     *            used and returned as the buffer.
     * @param assigner A operation function that assigns a value to the buffer.
     * @return {ChainableAPI}
     */
    public final <B> Signal<B> buffer(Signal<?> boundary, Supplier<B> supplier, BiConsumer<B, V> assigner) {
        return buffer(boundary, supplier, assigner, false);
    }

    /**
     * Returns an {@link Signal} that emits non-overlapping buffered items from the source
     * {@link Signal} each time the specified boundary {@link Signal} emits an item.
     * 
     * @param boundary A boundary {@link Signal}.
     * @param supplier A factory function that returns an instance of the collection subclass to be
     *            used and returned as the buffer.
     * @param assigner A operation function that assigns a value to the buffer.
     * @param ignoreRemaining A flag whether completion event emits the remaining values or not.
     * @return {ChainableAPI}
     */
    private <B> Signal<B> buffer(Signal<?> boundary, Supplier<B> supplier, BiConsumer<B, V> assigner, boolean ignoreRemaining) {
        return new Signal<>((observer, disposer) -> {
            AtomicReference<B> buffer = new AtomicReference(supplier.get());

            WiseRunnable transfer = () -> observer.accept(buffer.getAndSet(supplier.get()));
            WiseRunnable completer = ignoreRemaining ? observer::complete : I.bundle(transfer, observer::complete);

            return to(v -> assigner.accept(buffer.get(), v), observer::error, completer, disposer, false)
                    .add(boundary.to(transfer, observer::error, completer));
        });
    }

    /**
     * Returns an {@link Signal} that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by this {@link Signal} and the other
     * specified {@link Signal}.
     *
     * @param other An other {@link Signal} to combine.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     */
    public final <O> Signal<Ⅱ<V, O>> combine(Signal<O> other) {
        return combine(other, I::pair);
    }

    /**
     * Returns an {@link Signal} that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by this {@link Signal} and the other
     * specified {@link Signal}.
     *
     * @param other An other {@link Signal} to combine.
     * @param another An another {@link Signal} to combine.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     */
    public final <O, A> Signal<Ⅲ<V, O, A>> combine(Signal<O> other, Signal<A> another) {
        return combine(other, I::<V, O> pair).combine(another, Ⅱ<V, O>::<A> ⅲ);
    }

    /**
     * Returns an {@link Signal} that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by this {@link Signal} and the other
     * specified {@link Signal}.
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
            boolean[] completes = new boolean[2];

            return to(value -> {
                if (completes[0] == false) {
                    if (otherValue.isEmpty()) {
                        baseValue.add(value);
                    } else {
                        observer.accept(combiner.apply(value, otherValue.pollFirst()));
                        if (completes[1] && otherValue.isEmpty()) observer.complete();
                    }
                }
            }, observer::error, () -> {
                completes[0] = true;
                if (baseValue.isEmpty() || completes[1]) {
                    observer.complete();
                }
            }, disposer, false).add(other.to(value -> {
                if (completes[1] == false) {
                    if (baseValue.isEmpty()) {
                        otherValue.add(value);
                    } else {
                        observer.accept(combiner.apply(baseValue.pollFirst(), value));
                        if (completes[0] && baseValue.isEmpty()) observer.complete();
                    }
                }
            }, observer::error, () -> {
                completes[1] = true;
                if (otherValue.isEmpty() || completes[0]) {
                    observer.complete();
                }
            }, disposer, false));
        });
    }

    /**
     * Returns an {@link Signal} that emits the results of a function of your choosing applied to
     * combinations of several items emitted, in sequence, by this {@link Signal} and the other
     * specified {@link Signal}.
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
     * Combines two source {@link Signal} by emitting an item that aggregates the latest values of
     * each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
     *
     * @param other An other constant {@link Signal} to combine.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public final <O> Signal<Ⅱ<V, O>> combineLatest(O other) {
        return combineLatest(I.signal(other));
    }

    /**
     * Combines two source {@link Signal} by emitting an item that aggregates the latest values of
     * each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
     *
     * @param other An other {@link Signal} to combine.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public final <O> Signal<Ⅱ<V, O>> combineLatest(Signal<O> other) {
        return combineLatest(other, I::pair);
    }

    /**
     * Combines two source {@link Signal} by emitting an item that aggregates the latest values of
     * each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
     *
     * @param other An other {@link Signal} to combine.
     * @param another An another {@link Signal} to combine.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public final <O, A> Signal<Ⅲ<V, O, A>> combineLatest(Signal<O> other, Signal<A> another) {
        return combineLatest(other, I::<V, O> pair).combineLatest(another, Ⅱ<V, O>::<A> ⅲ);
    }

    /**
     * Combines two source {@link Signal} by emitting an item that aggregates the latest values of
     * each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
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
     * Combines several source {@link Signal} by emitting an item that aggregates the latest values
     * of each of the source {@link Signal} each time an item is received from either of the source
     * {@link Signal}, where this aggregation is defined by a specified function.
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

    public final <R> Signal<Map<V, R>> combineLatestMap(WiseFunction<V, Signal<R>> mapper) {
        return flatMap(v -> mapper.apply(v).map(x -> I.pair(v, x))).scanBy(ConcurrentHashMap::new, (map, v) -> {
            map.put(v.ⅰ, v.ⅱ);
            return map;
        });
    }

    /**
     * Returns an {@link Signal} that emits the items emitted by {@link Signal}s, one after the
     * other, preassignout interleaving them.
     * 
     * @param others A sequence of {@link Signal}s to concat.
     * @return {ChainableAPI}
     */
    public final Signal<V> concat(Signal<? extends V>... others) {
        // ignore invalid parameters
        if (others == null || others.length == 0) {
            return this;
        }
        return concat(I.list(others));
    }

    /**
     * Returns an {@link Signal} that emits the items emitted by {@link Signal}s, one after the
     * other, preassignout interleaving them.
     * 
     * @param others A sequence of {@link Signal}s to concat.
     * @return {ChainableAPI}
     */
    public final Signal<V> concat(Iterable<Signal<? extends V>> others) {
        // ignore invalid parameters
        if (others == null) {
            return this;
        }

        return new Signal<V>((observer, disposer) -> {
            Iterator<Signal<? extends V>> signals = I.signal(others).skipNull().startWith(this).toList().iterator();

            I.recurse(self -> {
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
     * Returns a new {@link Signal} that emits items resulting from applying a function that you
     * supply to each item emitted by the current {@link Signal}, where that function returns an
     * {@link Signal}, and then emitting the items that result from concatenating those returned
     * {@link Signal}.
     * 
     * @param <R>
     * @param function A function that, when applied to an item emitted by the current
     *            {@link Signal}, returns an {@link Signal}
     * @return {ChainableAPI}
     */
    public final <R> Signal<R> concatMap(WiseFunction<V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            Subscriber count = countable(observer, 1);

            AtomicBoolean now = new AtomicBoolean();
            LinkedList<V> values = new LinkedList();

            WiseRunnable end = I.recurse(self -> {
                count.complete();

                if (!values.isEmpty()) {
                    disposer.add(function.apply(values.pollFirst()).to(observer, count::error, self));
                } else {
                    now.set(false);
                }
            });

            return to(v -> {
                count.index++;

                if (now.compareAndSet(false, true)) {
                    disposer.add(function.apply(v).to(observer, count::error, end));
                } else {
                    values.add(v);
                }
            }, observer::error, count::complete, disposer, false);
        });
    }

    /**
     * Returns a {@link Signal} that emits a Boolean that indicates whether the source
     * {@link Signal} emitted a specified item.
     * 
     * @param value An item to search for in the emissions from the source {@link Signal}.
     * @return A {@link Signal} that emits true if the specified item is emitted by the source
     *         {@link Signal}, or false if the source {@link Signal} completes preassignout emitting
     *         that item.
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
        return map(AtomicLong::new, I.wiseBF(AtomicLong::incrementAndGet));
    }

    /**
     * Drops values that are followed by newer values before a timeout. The timer resets on each
     * value emission.
     *
     * @param time A time value. Zero or negative number will ignore this instruction.
     * @param unit A time unit. <code>null</code> will ignore this instruction.
     * @param scheduler
     * @return {ChainableAPI}
     */
    public final Signal<V> debounce(long time, TimeUnit unit, ScheduledExecutorService... scheduler) {
        return debounceAll(time, unit, scheduler).flatMap(list -> I.signal(list).last());
    }

    /**
     * Collect values that are followed by newer values before a timeout. The timer resets on each
     * value emission.
     *
     * @param time A time value. Zero or negative number will ignore this instruction.
     * @param unit A time unit. <code>null</code> will ignore this instruction.
     * @param scheduler
     * @return {ChainableAPI}
     */
    public final Signal<List<V>> debounceAll(long time, TimeUnit unit, ScheduledExecutorService... scheduler) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return map(List::of);
        }

        return new Signal<List<V>>((observer, disposer) -> {
            AtomicReference<Disposable> latest = new AtomicReference();
            AtomicReference<List<V>> list = new AtomicReference(new ArrayList());

            return to(value -> {
                list.get().add(value);
                Disposable d = latest.get();

                if (d != null) {
                    d.dispose();
                }

                latest.set(I.schedule(time, unit, scheduler).to(() -> {
                    latest.set(null);
                    observer.accept(list.getAndSet(new ArrayList<>()));
                }));
            }, observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * Indicates the {@link Signal} sequence by item count preassign the specified source and time.
     *
     * @param count The positive time used to shift the {@link Signal} sequence. Zero or negative
     *            number will ignore this instruction.
     * @return {ChainableAPI}
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
     * @param scheduler An event scheduler.
     * @return The source {@link Signal} shifted in time by the specified delay.
     * @see #wait(long, TimeUnit)
     */
    public final Signal<V> delay(long time, TimeUnit unit, ScheduledExecutorService... scheduler) {
        if (unit == null) {
            return this;
        }
        return delay(Duration.of(time, unit.toChronoUnit()), scheduler);
    }

    /**
     * Returns {@link Signal} that emits the items emitted by the source {@link Signal} shifted
     * forward in time by a specified delay at parallel thread. Error notifications from the source
     * {@link Signal} are not delayed.
     *
     * @param time The delay to shift the source by.
     * @return The source {@link Signal} shifted in time by the specified delay.
     */
    public final Signal<V> delay(Duration time, ScheduledExecutorService... scheduler) {
        // ignore invalid parameters
        if (time == null || time.isNegative() || time.isZero()) {
            return this;
        }
        return delay(Variable.of(time), scheduler);
    }

    /**
     * Returns {@link Signal} that emits the items emitted by the source {@link Signal} shifted
     * forward in time by a specified delay at parallel thread. Error notifications from the source
     * {@link Signal} are not delayed.
     *
     * @param time The delay to shift the source by.
     * @return The source {@link Signal} shifted in time by the specified delay.
     */
    public final Signal<V> delay(Supplier<Duration> time, ScheduledExecutorService... scheduler) {
        // ignore invalid parameters
        if (time == null) {
            return this;
        }
        return delay(I.wiseF(time), scheduler);
    }

    /**
     * Returns {@link Signal} that emits the items emitted by the source {@link Signal} shifted
     * forward in time by a specified delay at parallel thread. Error notifications from the source
     * {@link Signal} are not delayed.
     *
     * @param time The delay to shift the source by.
     * @return The source {@link Signal} shifted in time by the specified delay.
     */
    public final Signal<V> delay(Function<V, Duration> time, ScheduledExecutorService... scheduler) {
        // ignore invalid parameters
        if (time == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            ConcurrentSkipListSet<Ⅱ<Object, Long>> queue = new ConcurrentSkipListSet<>(Comparator.comparingLong(Ⅱ::ⅱ));

            Runnable sender = I.recurse(self -> {
                Ⅱ<Object, Long> item = queue.pollFirst();

                if (item != null) {
                    if (item.ⅰ == this) {
                        observer.complete();
                    } else if (!disposer.isDisposed()) {
                        observer.accept((V) item.ⅰ);
                    }
                }
                if (!queue.isEmpty()) {
                    I.schedule(queue.first().ⅱ - System.nanoTime(), NANOSECONDS, scheduler).to(self);
                }
            });

            long[] lastDelay = new long[1];

            return to(value -> {
                long delay = time.apply(value).toNanos();
                queue.add(I.pair(value, lastDelay[0] = delay + System.nanoTime()));
                if (queue.size() == 1) I.schedule(delay, NANOSECONDS, scheduler).to(sender);
            }, observer::error, () -> {
                queue.add(I.pair(this, lastDelay[0] + 1));
                if (queue.size() == 1) I.schedule(lastDelay[0] + 1 - System.nanoTime(), NANOSECONDS, scheduler).to(sender);
            }, disposer, false);
        });
    }

    /**
     * Returns an {@link Signal} that emits all items emitted by the source {@link Signal} that are
     * distinct from their immediate predecessors based on {@link Object#equals(Object)} comparison.
     * <p>
     * It is recommended the elements' class {@code V} in the flow overrides the default
     * {@code Object.equals()} to provide meaningful comparison between items as the default Java
     * implementation only considers reference equivalence. Alternatively, use the
     * {@link #diff(BiPredicate)} overload and provide a comparison function in case the class
     * {@code V} can't be overridden preassign custom {@code equals()} or the comparison itself
     * should happen on different terms or properties of the class {@code V}.
     *
     * @return {@link Signal} that emits those items from the source {@link Signal} that are
     *         distinct from their immediate predecessors.
     * @see #diff(WiseFunction)
     * @see #diff(BiPredicate)
     */
    public final Signal<V> diff() {
        return skip((V) null, Objects::equals);
    }

    /**
     * Returns an {@link Signal} that emits all items emitted by the source {@link Signal} that are
     * distinct from their immediate predecessors, according to a key selector function and based on
     * {@link Object#equals(Object)} comparison of those objects returned by the key selector
     * function.
     * <p>
     * It is recommended the keys' class {@code K} overrides the default {@code Object.equals()} to
     * provide meaningful comparison between the key objects as the default Java implementation only
     * considers reference equivalence. Alternatively, use the {@link #diff(BiPredicate)} overload
     * and provide a comparison function in case the class {@code K} can't be overridden preassign
     * custom {@code equals()} or the comparison itself should happen on different terms or
     * properties of the item class {@code V} (for which the keys can be derived via a similar
     * selector).
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
     * Returns an {@link Signal} that emits all items emitted by the source {@link Signal} that are
     * distinct from their immediate predecessors when compared preassign each other via the
     * provided comparator function.
     *
     * @param comparer The function that receives the previous item and the current item and is
     *            expected to return true if the two are equal, thus skipping the current value.
     * @return {@link Signal} that emits those items from the source {@link Signal} that are
     *         distinct from their immediate predecessors.
     * @see #diff()
     * @see #diff(WiseFunction)
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
     * Returns an {@link Signal} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     *
     * @return {ChainableAPI}
     */
    public final Signal<V> distinct() {
        return distinct(I.wiseF(Function.identity()));
    }

    /**
     * Returns an {@link Signal} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     *
     * @return {ChainableAPI}
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
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effect(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }
        return effect(I.wiseC(effect));
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effect(WiseConsumer<? super V> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to((Consumer<? super V>) I.bundle(effect, observer), observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectAfter(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }
        return effectAfter(I.wiseC(effect));
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectAfter(WiseConsumer<? super V> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to((Consumer<? super V>) I.bundle(observer, effect), observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectOnLifecycle(WiseFunction<Disposable, WiseConsumer<V>> effect) {
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return effect(effect.apply(disposer)).to(observer, disposer);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect only once when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke only once when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectOnce(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }
        return effectOnce(I.wiseC(effect));
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect only once when it calls
     * {@link Observer#accept(Object)}.
     *
     * @param effect The action to invoke only once when the source {@link Signal} calls
     *            {@link Observer#accept(Object)}
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
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
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectOnComplete(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> to(observer::accept, observer::error, I
                .bundle(effect, observer::complete), disposer, false));
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#complete()}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#complete()}
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectOnComplete(WiseConsumer<List<V>> effect) {
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
                effect.accept(list);
                observer.complete();
            }, disposer, false);
        });
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Disposable#dispose()}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Disposable#dispose()}
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
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
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectOnError(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }
        return effectOnError(I.wiseC(effect));
    }

    /**
     * Modifies the source {@link Signal} so that it invokes an effect when it calls
     * {@link Observer#error(Throwable)}.
     *
     * @param effect The action to invoke when the source {@link Signal} calls
     *            {@link Observer#error(Throwable)}
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectOnError(WiseConsumer<Throwable> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, I.bundle(effect, observer::error), observer::complete, disposer, false);
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
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectOnObserve(WiseRunnable effect) {
        return effectOnObserve(effect == null ? null : I.wiseC(effect));
    }

    /**
     * Modifies the source {@link Signal} so that it invokes the given effect when it is observed
     * from its observers. Each observation will result in an invocation of the given action except
     * when the source {@link Signal} is reference counted, in which case the source {@link Signal}
     * will invoke the given action for the first observation.
     *
     * @param effect The {@link Consumer} that gets called when an {@link Observer} subscribes to
     *            the current {@link Signal}.
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
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
     * @return The source {@link Signal} preassign the side-effecting behavior applied.
     * @see #effect(WiseConsumer)
     * @see #effectOnError(WiseConsumer)
     * @see #effectOnComplete(WiseRunnable)
     * @see #effectOnTerminate(WiseRunnable)
     * @see #effectOnDispose(WiseRunnable)
     * @see #effectOnObserve(WiseConsumer)
     */
    public final Signal<V> effectOnTerminate(WiseRunnable effect) {
        return effectOnError(effect).effectOnComplete(effect);
    }

    /**
     * Instructs an {@link Signal} to emit an item (returned by a specified function) rather than
     * invoking onError if it encounters an error.
     * 
     * @param resumer
     * @return {ChainableAPI}
     */
    public final Signal<V> errorResume(Signal<? extends V> resumer) {
        if (resumer == null) {
            return this;
        }
        return errorResume(e -> resumer);
    }

    /**
     * Instructs an {@link Signal} to emit an item (returned by a specified function) rather than
     * invoking onError if it encounters an error.
     * 
     * @param resumer
     * @return {ChainableAPI}
     */
    public final Signal<V> errorResume(WiseFunction<? super Throwable, Signal<? extends V>> resumer) {
        if (resumer == null) {
            return this;
        }
        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, e -> resumer.apply(e).to(observer, disposer), observer::complete, disposer, false);
        });
    }

    /**
     * Returns {@link Signal} that emits only the very first item emitted by the source
     * {@link Signal}, or completes if the source {@link Signal} is empty.
     * 
     * @return {ChainableAPI}
     */
    public final Signal<V> first() {
        return signal(I::accept, null, FALSE, null, TRUE, null);
    }

    /**
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatArray(WiseFunction<V, R[]> function) {
        Objects.requireNonNull(function);

        // There are two reasons why you should not use the following code.
        // return flatMap(I.wiseF(function.andThen(I::signal)));
        //
        // One is the issue of code size. It may seem counter-intuitive, but the code size is
        // slightly smaller when using lambda expressions. However, if you do the same for
        // flatIterable or flatEnum, the code size will increase.
        //
        // The other is the type variable issue. I don't know why, but if you don't use lambda
        // expressions, ECJ doesn't keep information about array type variables in the class file,
        // which causes problems in runtime environments that use Jar compiled with Javac.
        return flatMap(v -> I.signal(function.apply(v)));
    }

    /**
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatEnum(WiseFunction<V, ? extends Enumeration<R>> function) {
        return flatMap(I.wiseF(function.andThen(I::signal)));
    }

    /**
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatIterable(WiseFunction<V, ? extends Iterable<R>> function) {
        return flatMap(I.wiseF(function.andThen(I::signal)));
    }

    /**
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
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
            end.next = observer;

            return to(value -> {
                end.index++;
                function.apply(value).to(end, end::error, null, disposer.sub().add(end::complete), true);
            }, observer::error, end::complete, disposer, false);
        });
    }

    /**
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     *
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> flatVariable(WiseFunction<V, Variable<R>> function) {
        Objects.requireNonNull(function);

        Signal<V> share = share();
        return share.flatMap(v -> function.apply(v).observing().takeUntil(share.isCompleted()));
    }

    /**
     * Append index (starting from the specified value).
     * 
     * @param start A starting index number.
     * @return {ChainableAPI}
     */
    public final Signal<Ⅱ<V, Long>> index(long start) {
        return map(() -> new AtomicLong(start), (context, value) -> I.pair(value, context.getAndIncrement()));
    }

    /**
     * Ensure the interval time for each values in {@link Signal} sequence.
     *
     * @param interval Time to emit values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified interval. <code>null</code> will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public final Signal<V> interval(long interval, TimeUnit unit, ScheduledExecutorService... scheduler) {
        // ignore invalid parameters
        if (interval <= 0 || unit == null) {
            return this;
        }

        Signaling<Duration> next = new Signaling();

        return combine(next.expose.startWith(Duration.ZERO).delay(Function.identity(), scheduler)).map(Ⅱ::ⅰ)
                .effectAfter(I.wiseC(next).bind(Duration.of(interval, unit.toChronoUnit())));
    }

    /**
     * Compare the incoming items. If it matches the given value, it sends true, otherwise it sends
     * false to the subsequent {@link Signal}.
     *
     * @param value A value to be compared.
     * @return {ChainableAPI}
     */
    public final Signal<Boolean> is(V value) {
        return is(value == null ? Objects::isNull : value::equals);
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given {@link Predicate} function to each value
     * emitted by an {@link Signal} and emits the result.
     * </p>
     *
     * @param condition A conditional function to apply to each value emitted by this
     *            {@link Signal}.
     * @return {ChainableAPI}
     */
    public final Signal<Boolean> is(Predicate<? super V> condition) {
        return map(condition::test);
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given {@link Predicate} function to each value
     * emitted by an {@link Signal} and emits the result.
     * </p>
     *
     * @param value An expected value.
     * @return {ChainableAPI}
     */
    public final Signal<Boolean> isNot(V value) {
        return isNot(value == null ? Objects::isNull : value::equals);
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given {@link Predicate} function to each value
     * emitted by an {@link Signal} and emits the result.
     * </p>
     *
     * @param condition A conditional function to apply to each value emitted by this
     *            {@link Signal}.
     * @return {ChainableAPI}
     */
    public final Signal<Boolean> isNot(Predicate<? super V> condition) {
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
        return signal(I::accept, TRUE, true, FALSE, true, FALSE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is completed preassignout any value emitted.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         completed preassignout any value emitted.
     */
    public final Signal<Boolean> isEmpty() {
        return signal(I::accept, FALSE, true, FALSE, true, TRUE);
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
        return signal(I::accept, TRUE, true, TRUE, true, TRUE);
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
     * Returns a new {@link Signal} that invokes the mapper action in parallel thread and waits all
     * of them until all actions are completed.
     *
     * @param mapper A mapper function.
     * @return {ChainableAPI}
     */
    public final <R> Signal<R> joinAll(WiseFunction<V, R> mapper) {
        return map(mapper::bind).buffer().flatIterable(v -> I.signal(I.scheduler.invokeAll(v)).map(Future<R>::get).toList());
    }

    /**
     * Returns a new {@link Signal} that invokes the mapper action in parallel thread and waits
     * until any single action is completed. All other actions will be cancelled.
     * 
     * @param mapper A mapper function.
     * @return {ChainableAPI}
     */
    public final <R> Signal<R> joinAny(WiseFunction<V, R> mapper) {
        return map(mapper::bind).buffer().map(I.scheduler::invokeAny);
    }

    /**
     * Returns a {@link Signal} that emits the last item emitted by this {@link Signal} or completes
     * if this {@link Signal} is empty.
     * 
     * @return {ChainableAPI}
     */
    public final Signal<V> last() {
        return buffer(never(), AtomicReference<V>::new, AtomicReference::set).map(AtomicReference::get).skipNull();
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given function to each value emitted by an
     * {@link Signal} and emits the result.
     * </p>
     * <pre class="marble-diagram">
     * ───①───②───③───④───⑤──┼
     *    ↓   ↓   ↓   ↓   ↓
     *  ┌────────────────────┐
     *   map ○→●
     *  └────────────────────┘
     *    ↓   ↓   ↓   ↓   ↓  ↓
     * ───❶───❷───❸───❹───❺──┼
     * </pre>
     *
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return {ChainableAPI}
     */
    public final <R> Signal<R> map(WiseFunction<? super V, R> converter) {
        Objects.requireNonNull(converter);

        return map((WiseSupplier) null, (context, value) -> converter.apply(value));
    }

    /**
     * <p>
     * {@link #map(WiseFunction)} preassign context.
     * </p>
     * <pre class="marble-diagram">
     * ───①───②───③───④───⑤──┼
     *    ↓   ↓   ↓   ↓   ↓
     *  ┌────────────────────┐
     *   map ○→●
     *  └────────────────────┘
     *    ↓   ↓   ↓   ↓   ↓  ↓
     * ───❶───❷───❸───❹───❺──┼
     * </pre>
     * 
     * @param contextSupplier A {@link Supplier} of {@link Signal} specific context.
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return {ChainableAPI}
     */
    public final <C, R> Signal<R> map(WiseSupplier<C> contextSupplier, WiseBiFunction<C, ? super V, R> converter) {
        Objects.requireNonNull(converter);

        return new Signal<>((observer, disposer) -> {
            C context = contextSupplier == null ? null : contextSupplier.get();

            return to(value -> observer.accept(converter.apply(context, value)), observer::error, observer::complete, disposer, false);
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
     * @return {ChainableAPI}
     */
    public final <R> Signal<R> maps(WiseBiFunction<? super V, ? super V, R> converter) {
        Objects.requireNonNull(converter);

        return buffer(2, 1).map(values -> converter.apply(values.get(0), values.get(1)));
    }

    /**
     * {@link #map(WiseFunction)} preassign previuos value.
     *
     * @param init A initial previous value.
     * @param converter A converter function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return {ChainableAPI}
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
     * @return {ChainableAPI}
     */
    public final <R> Signal<R> mapTo(R constant) {
        return map(v -> constant);
    }

    /**
     * <p>
     * Flattens a sequence of {@link Signal} emitted by an {@link Signal} into one {@link Signal},
     * preassignout any transformation.
     * </p>
     * <pre class="marble-diagram">
     * ───①───②───③───④───⑤──┼
     *    ↓   ↓   ↓   ↓   ↓
     * ─────❶────❷────❸────┼
     *      ↓    ↓    ↓
     *  ┌────────────────────┐
     *   merge
     *  └────────────────────┘
     *    ↓ ↓ ↓  ↓↓   ↓   ↓  ↓
     * ───①─❶─②──❷③───④❸──⑤──┼
     * </pre>
     *
     * @param others A target {@link Signal} to merge. <code>null</code> will be ignored.
     * @return {ChainableAPI}
     */
    public final Signal<V> merge(Signal<? extends V>... others) {
        return merge(I.list(others));
    }

    /**
     * <p>
     * Flattens a sequence of {@link Signal} emitted by an {@link Signal} into one {@link Signal},
     * preassignout any transformation.
     * </p>
     *
     * @param others A target {@link Signal} set to merge. <code>null</code> will be ignored.
     * @return {ChainableAPI}
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
     * <link rel="stylesheet" href="main.css" type="text/css">
     * <p>
     * Switch event stream context.
     * </p>
     * <pre class="marble">
    * ───①───②───③───④───⑤──┼
    *    ↓   ↓   ↓   ↓   ↓
    *  ┌────────────────────┐
    *   on ━
    *  └────────────────────┘
    *    ↓   ↓   ↓   ↓   ↓  ↓
    * ━━━❶━━━❷━━━❸━━━❹━━━❺━━╋
    * </pre>
     * 
     * @param scheduler A new context
     * @return {ChainableAPI}
     */
    public final Signal<V> on(Consumer<Runnable> scheduler) {
        // ignore invalid parameters
        if (scheduler == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return to(v -> {
                scheduler.accept(I.wiseC(observer).bind(v));
            }, e -> {
                scheduler.accept(I.wiseC(observer::error).bind(e));
            }, I.wiseC(scheduler).bind(observer::complete), disposer, false);
        });
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that guarantee one item at least.
     * </p>
     *
     * @return {ChainableAPI}
     */
    public final Signal<V> or(V value) {
        return or(I.signal(value));
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that guarantee one item at least.
     * </p>
     *
     * @return {ChainableAPI}
     */
    public final Signal<V> or(Supplier<V> value) {
        return or(I.signal(value));
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that guarantee one item at least.
     * </p>
     *
     * @return {ChainableAPI}
     */
    public final Signal<V> or(Signal<V> values) {
        return new Signal<>((observer, disposer) -> {
            Subscriber sub = new Subscriber();
            sub.observer = observer;
            if (values != null) sub.complete = () -> values.to(observer, disposer);

            return effectOnce(() -> sub.complete = null).to(sub, disposer);
        });
    }

    /**
     * Helps to insert {@link Signal} chain from outside.
     * 
     * @param <O> An output type.
     * @param plug A chain builder to insert.
     * @return A chained {@link Signal}.
     */
    public final <O> Signal<O> plug(Function<Signal<V>, Signal<O>> plug) {
        return Objects.requireNonNull(plug).apply(this);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     * 
     * @param recurse A mapper function to enumerate values recursively.
     * @return {ChainableAPI}
     */
    public final Signal<V> recurse(WiseFunction<V, V> recurse) {
        return recurse(recurse, Runnable::run);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     * 
     * @param recurse A mapper function to enumerate values recursively.
     * @param executor An execution context.
     * @return {ChainableAPI}
     */
    public final Signal<V> recurse(WiseFunction<V, V> recurse, Executor executor) {
        return recurseMap(e -> e.map(recurse), executor);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     * 
     * @param recurse A mapper function to enumerate values recursively.
     * @return {ChainableAPI}
     */
    public final Signal<V> recurseMap(WiseFunction<Signal<V>, Signal<V>> recurse) {
        return recurseMap(recurse, Runnable::run);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Signal}, where that function returns an {@link Signal}
     * , and then merging those resulting {@link Signal} and emitting the results of this merger.
     * </p>
     * 
     * @param recurse A mapper function to enumerate values recursively.
     * @param executor An execution context.
     * @return {ChainableAPI}
     */
    public final Signal<V> recurseMap(WiseFunction<Signal<V>, Signal<V>> recurse, Executor executor) {
        // DON'T use the recursive call, it will throw StackOverflowError.
        return flatMap(init -> new Signal<V>((observer, disposer) -> {
            (executor == null ? I.scheduler : executor).execute(() -> {
                try {
                    LinkedList<V> values = new LinkedList(); // LinkedList accepts null
                    LinkedTransferQueue<Signal<V>> signal = new LinkedTransferQueue();
                    signal.put(I.signal(init));

                    while (!disposer.isDisposed()) {
                        signal.take().to(v -> {
                            values.addLast(v);
                            observer.accept(v);
                        }, observer::error, () -> {
                            if (values.isEmpty()) {
                                observer.complete();
                            } else {
                                signal.put(recurse.apply(I.signal(values.pollFirst())));
                            }
                        });
                    }
                } catch (Throwable e) {
                    observer.error(e);
                }
            });
            return disposer;
        }));
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that repeats the given value infinitely.
     * </p>
     *
     * @return {ChainableAPI}
     */
    public final Signal<V> repeat() {
        return repeatUntil(never());
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that repeats the given value finitely.
     * </p>
     *
     * @param count A number of repeat. Zero or negative number will ignore this instruction.
     * @return {ChainableAPI}
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
     * {@link Signal} until a stopper {@link Signal} emits an item.
     * </p>
     * 
     * @param stopper A {@link Signal} whose first emitted item will stop repeating.
     * @return {ChainableAPI}
     */
    public final Signal<V> repeatUntil(Signal stopper) {
        return repeatWhen(complete -> complete.takeUntil(stopper));
    }

    /**
     * Returns an {@link Signal} that emits the same values as the source signal preassign the
     * exception of an {@link Observer#error(Throwable)}. An error notification from the source will
     * result in the emission of a Throwable item to the {@link Signal} provided as an argument to
     * the notificationHandler function. If that {@link Signal} calls {@link Observer#complete()} or
     * {@link Observer#error(Throwable)} then retry will call {@link Observer#complete()} or
     * {@link Observer#error(Throwable) } on the child subscription. Otherwise, this {@link Signal}
     * will resubscribe to the source {@link Signal}.
     * 
     * @param notifier A receives an {@link Signal} of notifications preassign which a user can
     *            complete or error, aborting the retry.
     * @return Chainable API
     */
    public final Signal<V> repeatWhen(WiseFunction<Signal<?>, Signal<?>> notifier) {
        return repeatWhen(notifier, false);
    }

    /**
     * Returns an {@link Signal} that emits the same values as the source signal preassign the
     * exception of an {@link Observer#error(Throwable)}. An error notification from the source will
     * result in the emission of a Throwable item to the {@link Signal} provided as an argument to
     * the notificationHandler function. If that {@link Signal} calls {@link Observer#complete()} or
     * {@link Observer#error(Throwable)} then retry will call {@link Observer#complete()} or
     * {@link Observer#error(Throwable) } on the child subscription. Otherwise, this {@link Signal}
     * will resubscribe to the source {@link Signal}.
     * 
     * @param notifier A receives an {@link Signal} of notifications preassign which a user can
     *            complete or error, aborting the retry.
     * @return Chainable API
     */
    private Signal<V> repeatWhen(Function<Signal<?>, Signal<?>> notifier, boolean immediate) {
        // ignore invalid parameter
        if (notifier == null) {
            return this;
        }

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
                    subscriber.next = I.wiseC(observer::complete);

                    // Since there is a complete in processing, but this complete flow has ended,
                    // the processing complete is passed to the source signal.
                    if (processing[0] != null) observer.complete();
                }
            });

            // connect preassign complete handling flow
            previous[0] = to(observer::accept, observer::error, complete, disposer.sub(), true);

            // API difinition
            return disposer;
        });
    }

    /**
     * <p>
     * Recover the source {@link Signal} infinitly on any error by the specified value.
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
     * Recover the source {@link Signal} infinitly on any error by the specified value.
     * </p>
     * 
     * @param value A value to replace error.
     * @return Chainable API
     */
    public final Signal<V> recover(Signal<V> value) {
        return recover(null, value);
    }

    /**
     * <p>
     * Recover the source {@link Signal} finitly on any error by the specified value.
     * </p>
     *
     * @param count A number of recovery. Zero or negative number will ignore this instruction.
     * @param value A value to replace error.
     * @return Chainable API
     */
    public final Signal<V> recover(int count, V value) {
        // ignore invalid parameter
        if (count < 1) {
            return this;
        }
        return recoverWhen(null, fail -> fail.take(count).mapTo(value));
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
        return recover(type, I.signal(value));
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
    public final Signal<V> recover(Class<? extends Throwable> type, Signal<V> value) {
        return recoverWhen(type, fail -> fail.flatMap(e -> value == null ? I.signal() : value));
    }

    /**
     * <link rel="stylesheet" href= "javadoc.css"/>
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
    public final <E extends Throwable> Signal<V> recoverWhen(WiseFunction<Signal<E>, Signal<V>> notifier) {
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
    public final <E extends Throwable> Signal<V> recoverWhen(Class<E> type, WiseFunction<Signal<E>, Signal<V>> notifier) {
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
            return to(observer::accept, error, observer::complete, disposer, false);
        });
    }

    /**
     * <p>
     * Retry the source {@link Signal} infinitely whenever any error is occured.
     * </p>
     *
     * @return {ChainableAPI}
     */
    public final Signal<V> retry() {
        return retryUntil(never());
    }

    /**
     * <p>
     * Retry the source {@link Signal} finitely whenever any error is occured.
     * </p>
     *
     * @param count A number of retry. Zero or negative number will ignore this instruction.
     * @return {ChainableAPI}
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
     * Retry the source {@link Signal} whenever the specified error type is occured.
     * </p>
     * 
     * @param type An error type that you want to retry. The null value accepts any types.
     * @return {ChainableAPI}
     */
    public final Signal<V> retry(Class<? extends Throwable> type) {
        return retryUntil(type, never());
    }

    /**
     * <p>
     * Retry the source {@link Signal} whenever any error is occured until the stopper is signaled.
     * </p>
     * 
     * @param stopper A {@link Signal} whose first emitted item will stop retrying.
     * @return {ChainableAPI}
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
     * @param type An error type that you want to retry. The null value accepts any types.
     * @param stopper A {@link Signal} whose first emitted item will stop retrying.
     * @return {ChainableAPI}
     */
    public final <E extends Throwable> Signal<V> retryUntil(Class<E> type, Signal stopper) {
        // Use partial applied function to reduce code size.
        // return retryWhen(type, fail -> fail.takeUntil(stopper));
        return retryWhen(type, ((WiseBiFunction<Signal<E>, Signal, Signal<?>>) Signal::takeUntil).bindLast(stopper));
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
     * @param notifier An error notifier to define retrying flow.
     * @return Chainable API
     */
    public final <E extends Throwable> Signal<V> retryWhen(WiseFunction<Signal<E>, Signal<?>> notifier) {
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
     * @param type An error type that you want to retry. The null value accepts any types.
     * @param flow An error notifier to define retrying flow.
     * @return Chainable API
     */
    public final <E extends Throwable> Signal<V> retryWhen(Class<E> type, WiseFunction<Signal<E>, Signal<?>> flow) {
        // ignore invalid parameter
        if (flow == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            // recorder for the processing error
            Throwable[] processing = new Throwable[1];

            // build the actual error handler
            Subscriber<E> error = new Subscriber();
            error.disposer = disposer;
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

                // The following code is not used as it may send null.
                // if (processing[0] != null) observer.error(processing[0]);
                Throwable t = processing[0];
                if (t != null) observer.error(t);
            });

            // connect preassign error handling flow
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
     * @return {ChainableAPI}
     */
    public final Signal<V> reverse() {
        return buffer(never(), LinkedList<V>::new, Deque::addFirst).flatIterable(I.wiseF(Function.identity()));
    }

    /**
     * <p>
     * Returns an {@link Signal} that, when the specified sampler {@link Signal} emits an item,
     * emits the most recently emitted item (if any) emitted by the source {@link Signal} since the
     * previous emission from the sampler {@link Signal}.
     * </p>
     *
     * @param sampler An {@link Signal} to use for sampling the source {@link Signal}.
     * @return {ChainableAPI}
     */
    public final Signal<V> sample(Signal<?> sampler) {
        return buffer(sampler, AtomicReference<V>::new, AtomicReference<V>::set, true).map(AtomicReference<V>::get);
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies a function of your choosing to the first item emitted
     * by a source {@link Signal} and a seed value, then feeds the result of that function along
     * preassign the second item emitted by the source {@link Signal} into the same function, and so
     * on until all items have been emitted by the source {@link Signal}, emitting the result of
     * each of these iterations.
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
            }, observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies a function of your choosing to the first item emitted
     * by a source {@link Signal} and a seed value, then feeds the result of that function along
     * preassign the second item emitted by the source {@link Signal} into the same function, and so
     * on until all items have been emitted by the source {@link Signal}, emitting the result of
     * each of these iterations.
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
            }, observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies a function of your choosing to the first item emitted
     * by a source {@link Signal} and a seed value, then feeds the result of that function along
     * preassign the second item emitted by the source {@link Signal} into the same function, and so
     * on until all items have been emitted by the source {@link Signal}, emitting the result of
     * each of these iterations.
     * </p>
     *
     * @param init An initial (seed) accumulator item.
     * @param function An accumulator function to be invoked on each item emitted by the source
     *            {@link Signal}, whose result will be emitted to {@link Signal} via
     *            {@link Observer#accept(Object)} and used in the next accumulator call.
     * @return An {@link Signal} that emits initial value followed by the results of each call to
     *         the accumulator function.
     */
    public final <R> Signal<R> scanBy(Supplier<R> init, WiseBiFunction<R, V, R> function) {
        return scan(Collector.of(() -> new AtomicReference<R>(init.get()), (ref, value) -> {
            ref.set(function.apply(ref.get(), value));
        }, (a, b) -> a, AtomicReference<R>::get));
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies a function of your choosing to the first item emitted
     * by a source {@link Signal} and a seed value, then feeds the result of that function along
     * preassign the second item emitted by the source {@link Signal} into the same function, and so
     * on until all items have been emitted by the source {@link Signal}, emitting the result of
     * each of these iterations.
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
        return scanBy(Variable.of(init), function);
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
     * @return {ChainableAPI}
     */
    public final <R> Signal<R> sequenceMap(WiseFunction<V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            AtomicLong processing = new AtomicLong();
            Map<Long, Ⅱ<AtomicBoolean, LinkedList<R>>> buffer = new ConcurrentHashMap();

            Consumer<Long> complete = I.recurse((self, index) -> {
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

            return index(0).to(indexed -> {
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
            }, observer::error, end::complete, disposer, false);
        });
    }

    /**
     * <p>
     * Returns a new {@link Signal} that multicasts (shares) the original {@link Signal}. As long as
     * there is at least one {@link Observer} this {@link Signal} will be subscribed and emitting
     * data. When all observers have disposed it will disposes from the source {@link Signal}.
     * </p>
     * 
     * @return {ChainableAPI}
     */
    public final Signal<V> share() {
        Signaling<V> share = new Signaling();
        Disposable[] root = new Disposable[1];

        return new Signal<>((observer, disposer) -> {
            share.observers.add(observer);

            if (share.observers.size() == 1) {
                root[0] = to(share);
            }

            return disposer.add(() -> {
                share.observers.remove(observer);

                if (share.observers.isEmpty() && root[0] != null) {
                    root[0].dispose();
                    root[0] = null;
                }
            });
        });
    }

    /**
     * <p>
     * Bypasses a specified number of values in an {@link Signal} sequence and then returns the
     * remaining values.
     * </p>
     *
     * @param count A number of values to skip. Zero or negative number will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public final Signal<V> skip(long count) {
        // ignore invalid parameter
        if (count <= 0) {
            return this;
        }
        return new Signal<>((observer, disposer) -> {
            AtomicLong counter = new AtomicLong();

            return to(value -> {
                if (count < counter.incrementAndGet()) {
                    observer.accept(value);
                }
            }, observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * <p>
     * Alias for skip(I.set(excludes)).
     * </p>
     *
     * @param excludes A collection of skip items.
     * @return {ChainableAPI}
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
     * @return {ChainableAPI}
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
     * Alias for take(condition.negate()).
     * </p>
     *
     * @param condition A skip condition.
     * @return {ChainableAPI}
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
     * Alias for take(init, condition.negate()).
     * </p>
     *
     * @param condition A skip condition.
     * @return {ChainableAPI}
     */
    public final Signal<V> skip(V init, BiPredicate<? super V, ? super V> condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }
        return take(init, condition.negate());
    }

    /**
     * {@link #skip(Predicate)} preassign context.
     * 
     * @param contextSupplier A {@link Supplier} of {@link Signal} specific context.
     * @param condition A condition function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return {ChainableAPI}
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
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition An external boolean {@link Signal}. <code>null</code> will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public final Signal<V> skip(Signal<Boolean> condition) {
        return take(condition, false, false);
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
     * @return {ChainableAPI}
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
            }, observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * <p>
     * Returns a specified index values from the start of an {@link Signal} sequence.
     * </p>
     * 
     * @param condition A index condition of values to emit.
     * @return {ChainableAPI}
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
        return recover(type, (Signal) null);
    }

    /**
     * Return the {@link Signal} which ignores complete event.
     * 
     * @return {@link Signal} which ignores complete event.
     */
    public final Signal<V> skipComplete() {
        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, observer::error, I.NoOP, disposer, false);
        });
    }

    /**
     * <p>
     * Alias for skip(Objects::isNull).
     * </p>
     *
     * @return {ChainableAPI}
     */
    public final Signal<V> skipNull() {
        return skip(Objects::isNull);
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
            }, observer::error, observer::complete, disposer, false);
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
     * @return {ChainableAPI}
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
     * emitted by an {@link Signal}, you want the {@link #sequenceMap(WiseFunction)} operator.
     * </p>
     *
     * @param values The values that contains the items you want to emit first.
     * @return {ChainableAPI}
     */
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
     * emitted by an {@link Signal}, you want the {@link #sequenceMap(WiseFunction)} operator.
     * </p>
     *
     * @param value The values that contains the items you want to emit first.
     * @return {ChainableAPI}
     */
    public final Signal<V> startWith(Supplier<V> value) {
        if (value == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            if (!disposer.isDisposed()) {
                observer.accept(value.get());
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
     * emitted by an {@link Signal}, you want the {@link #sequenceMap(WiseFunction)} operator.
     * </p>
     *
     * @param values The values that contains the items you want to emit first.
     * @return {ChainableAPI}
     */
    public final Signal<V> startWith(Enumeration<V> values) {
        // ignore invalid parameter
        if (values == null) {
            return this;
        }
        return startWith(values::asIterator);
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
     * emitted by an {@link Signal}, you want the {@link #sequenceMap(WiseFunction)} operator.
     * </p>
     *
     * @param values The values that contains the items you want to emit first.
     * @return {ChainableAPI}
     */
    public final Signal<V> startWith(Iterable<V> values) {
        // ignore invalid parameter
        if (values == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Iterator<V> iterator = values.iterator();

            if (iterator != null) {
                while (iterator.hasNext() && disposer.isDisposed() == false) {
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
     * @return {ChainableAPI}
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
     * @return {ChainableAPI}
     */
    public final Signal<V> startWithNull() {
        return startWith((V) null);
    }

    /**
     * Asynchronously subscribes {@link Observer} to this {@link Signal} on the specified scheduler.
     * 
     * @param scheduler You specify which scheduler this operator will use.
     * @return {ChainableAPI}
     */
    public final Signal<V> subscribeOn(Consumer<Runnable> scheduler) {
        // ignore invalid parameters
        if (scheduler == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Disposable e = Disposable.empty();
            scheduler.accept(() -> {
                e.add(to(observer, disposer));
            });
            return e;
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
    public final <R> Signal<R> switchMap(WiseFunction<V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            Disposable[] disposables = {null, Disposable.empty()};
            Subscriber end = countable(observer, 1);

            disposables[0] = to(value -> {
                end.index++;
                disposables[1].dispose();
                disposables[1] = function.apply(value).to(observer::accept, end::error, I.NoOP, disposer.sub().add(end::complete), true);
            }, observer::error, end::complete, disposer.sub(), false);
            return disposer.add(() -> {
                disposables[0].dispose();
                disposables[1].dispose();
            });
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
    public final <R> Signal<R> switchVariable(WiseFunction<V, Variable<R>> function) {
        Objects.requireNonNull(function);

        Signal<V> share = share();
        return share.switchMap(v -> function.apply(v).observing().takeUntil(share.isCompleted()));
    }

    /**
     * Return an {@link Signal} that is observed as long as the specified timing {@link Signal}
     * indicates false. When the timing {@link Signal} returns true, the currently subscribed
     * {@link Signal} is immediately disposed.
     *
     * @param timing A timing whether the {@link Signal} is observed or not.
     * @return {ChainableAPI}
     * @throws NullPointerException Timing is null.
     */
    public final Signal<V> switchOff(Signal<Boolean> timing) {
        return switchOn(timing.map(Boolean.FALSE::equals));
    }

    /**
     * Return an {@link Signal} that is observed as long as the specified timing {@link Signal}
     * indicates true. When the timing {@link Signal} returns false, the currently subscribed
     * {@link Signal} is immediately disposed.
     *
     * @param timing A timing whether the {@link Signal} is observed or not.
     * @return {ChainableAPI}
     * @throws NullPointerException Timing is null.
     */
    public final Signal<V> switchOn(Signal<Boolean> timing) {
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
     * Returns a specified number of contiguous values from the start of an {@link Signal} sequence.
     * </p>
     *
     * @param count A number of values to emit. Zero or negative number will ignore this
     *            instruction.
     * @return {ChainableAPI}
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
     * @return {ChainableAPI}
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
     * @return {ChainableAPI}
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
     * @return {ChainableAPI}
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
     * @return {ChainableAPI}
     */
    public final Signal<V> take(V init, BiPredicate<? super V, ? super V> condition) {
        // ignore invalid parameters
        if (condition == null) {
            return this;
        }
        return take(() -> new AtomicReference<>(init), (context, value) -> condition.test(context.getAndSet(value), value));
    }

    /**
     * {@link #take(Predicate)} preassign context.
     * 
     * @param contextSupplier A {@link Supplier} of {@link Signal} specific context.
     * @param condition A condition function to apply to each value emitted by this {@link Signal} .
     *            <code>null</code> will ignore this instruction.
     * @return {ChainableAPI}
     */
    public final <C> Signal<V> take(Supplier<C> contextSupplier, BiPredicate<C, ? super V> condition) {
        return take(contextSupplier, condition, true, false, false);
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition An external boolean {@link Signal}. <code>null</code> will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public final Signal<V> take(Signal<Boolean> condition) {
        return take(condition, false, true);
    }

    /**
     * <p>
     * Returns an {@link Signal} sequence while the specified duration.
     * </p>
     *
     * @param time Time to take values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public final Signal<V> take(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }
        return take(() -> System.nanoTime() + unit.toNanos(time), (limit, value) -> System.nanoTime() < limit, true, true, false);
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition An external boolean {@link Signal}. <code>null</code> will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    private final Signal<V> take(Signal<Boolean> condition, boolean init, boolean expect) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            AtomicBoolean flag = new AtomicBoolean(init);

            return to(v -> {
                if (flag.get() == expect) {
                    observer.accept(v);
                }
            }, observer::error, observer::complete, disposer, false).add(condition.to(flag::set));
        });
    }

    /**
     * <p>
     * Returns a specified index values from the start of an {@link Signal} sequence.
     * </p>
     * 
     * @param condition A index condition of values to emit.
     * @return {ChainableAPI}
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
            }, observer::error, () -> {
                if (stopped.get() == false) {
                    observer.complete();
                }
            }, disposer, false);
        });
    }

    /**
     * Returns an Signal that mirrors the source Signal but applies a timeout policy for each
     * emitted item. If the next item isn't emitted preassignin the specified timeout duration
     * starting from its predecessor, the resulting Signal terminates and notifies observers of a
     * {@link TimeoutException}.
     * 
     * @param time Time to take values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @param scheduler An event scheduler.
     * @return {ChainableAPI}
     */
    public final Signal<V> timeout(long time, TimeUnit unit, ScheduledExecutorService... scheduler) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Runnable timeout = () -> {
                observer.error(new TimeoutException());
                disposer.dispose();
            };

            AtomicReference<Disposable> d = new AtomicReference<>(I.schedule(time, unit, scheduler).to(timeout));

            WiseConsumer<Object> effect = v -> {
                d.getAndSet(v == null ? null : I.schedule(time, unit, scheduler).to(timeout)).dispose();
            };
            return effect(effect.bind(this)).effectOnTerminate(effect.bind((V) null)).to(observer, disposer);
        });
    }

    /**
     * <p>
     * Throttles by skipping values until "skipDuration" passes and then emits the next received
     * value.
     * </p>
     * <p>
     * Ignores the values from an {@link Signal} sequence which are followed by another value before
     * due time preassign the specified source and time.
     * </p>
     *
     * @param time Time to wait before sending another item after emitting the last item. Zero or
     *            negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public final Signal<V> throttle(long time, TimeUnit unit) {
        return throttle(time, unit, System::nanoTime);
    }

    /**
     * <p>
     * Throttles by skipping values until "skipDuration" passes and then emits the next received
     * value.
     * </p>
     * <p>
     * Ignores the values from an {@link Signal} sequence which are followed by another value before
     * due time preassign the specified source and time.
     * </p>
     *
     * @param time Time to wait before sending another item after emitting the last item. Zero or
     *            negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @param nanoClock A nano-time stamp provider.
     * @return {ChainableAPI}
     */
    public final Signal<V> throttle(long time, TimeUnit unit, LongSupplier nanoClock) {
        // ignore invalid parameters
        if (time <= 0 || unit == null || nanoClock == null) {
            return this;
        }

        long delay = unit.toNanos(time);
        return take(AtomicLong::new, (context, value) -> {
            long now = nanoClock.getAsLong();

            if (context.get() + delay <= now) {
                context.set(now);
                return true;
            }
            return false;
        });
    }

    /**
     * Returns an {@link Signal} that applies the given two constants alternately to each item
     * emitted by an {@link Signal} and emits the result.
     *
     * @param values A list of constants to apply to each value emitted by this {@link Signal}.
     * @return {ChainableAPI}
     */
    public final <E> Signal<E> toggle(E... values) {
        if (values.length == 0) {
            return never();
        }

        return new Signal<>((observer, disposer) -> {
            AtomicInteger count = new AtomicInteger();

            return to(value -> observer
                    .accept(values[count.getAndIncrement() % values.length]), observer::error, observer::complete, disposer, false);
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
     * @see #delay(long, TimeUnit, ScheduledExecutorService...)
     */
    public final Signal<V> wait(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }
        return effect(((WiseConsumer<Long>) Thread::sleep).bind(unit.toMillis(time)));
    }

    /**
     * Synchronization Support Tool : Wait in the current thread until this {@link Signal} to be
     * terminated. Termination is one of the states of completed, error or disposed.
     * 
     * @return
     */
    public final Signal<V> waitForTerminate() {
        return new Signal<>((observer, disposer) -> {
            try {
                CountDownLatch latch = new CountDownLatch(1);
                to(observer::accept, I.bundle(I.wiseC(latch::countDown), observer::error), I
                        .bundle(latch::countDown, observer::complete), disposer.add(latch::countDown), false);
                latch.await();
            } catch (Exception e) {
                observer.error(e);
            }
            return disposer;
        });
    }

    /**
     * This is another name for {@link #flatMap(WiseFunction)}, primarily for use in DSL.
     * 
     * @param function A function that, when applied to an item emitted by the source {@link Signal}
     *            , returns an {@link Signal}.
     * @return An {@link Signal} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Signal} and merging the results of the
     *         {@link Signal} obtained from this transformation.
     */
    public final <R> Signal<R> $(WiseFunction<V, Signal<R>> function) {
        return flatMap(function);
    }

    /**
     * Create countable completer.
     * 
     * @param delgator A complete action.
     * @param count A complete count.
     * @return {ChainableAPI}
     */
    private Subscriber countable(Observer delgator, int count) {
        Subscriber<?> completer = new Subscriber();
        completer.index = count;
        completer.error = e -> {
            completer.index = -1;
            delgator.error(e);
        };
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
     * @return {ChainableAPI}
     */
    private <T> Signal<T> signal(Predicate<? super V> emitCondition, T emitOutput, boolean acceptError, T errorOutput, boolean acceptComplete, T completeOuput) {
        return new Signal<>((observer, disposer) -> {
            Subscriber end = countable(observer, 1);
            Disposable sub = disposer.sub();

            return to(v -> {
                if (emitCondition != null && emitCondition.test(v)) {
                    observer.accept(emitOutput == null ? (T) v : emitOutput);
                    end.complete();
                    sub.dispose();
                }
            }, e -> {
                if (acceptError) {
                    if (errorOutput != null) observer.accept(errorOutput);
                    end.complete();
                    sub.dispose();
                } else {
                    observer.error(e);
                }
            }, () -> {
                if (acceptComplete && completeOuput != null) observer.accept(completeOuput);
                end.complete();
                sub.dispose();
            }, sub, false);
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