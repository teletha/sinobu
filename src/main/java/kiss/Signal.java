/*
 * Copyright (C) 2024 The SINOBU Development Team
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
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
public class Signal<V> {

    /**
     * For reuse.
     */
    private static final BinaryOperator UNDEF = (a, b) -> b;

    /**
     * For reuse.
     */
    public static <R> Signal<R> never() {
        return new Signal<>(UNDEF);
    }

    /**
     * The subscriber.
     */
    private final BiFunction<Observer<V>, Disposable, Disposable> subscriber;

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
            observers.add(observer);

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
    public Signal(BiFunction<Observer<V>, Disposable, Disposable> subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * An {@link Observer} must call an {@link Signal#to()} method in order to receive items and
     * notifications from the Observable.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public Disposable to(Runnable next) {
        return to(I.wiseC(next), null, null);
    }

    /**
     * An {@link Observer} must call an {@link Signal#to()} method in order to receive items and
     * notifications from the Observable.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public Disposable to(Consumer<? super V> next) {
        return to(next, null, null);
    }

    /**
     * An {@link Observer} must call an {@link Signal#to()} method in order to receive items and
     * notifications from the Observable.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @param error A delegator method of {@link Observer#error(Throwable)}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public Disposable to(Consumer<? super V> next, Consumer<Throwable> error) {
        return to(next, error, null);
    }

    /**
     * Receive values from this {@link Signal}.
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @param error A delegator method of {@link Observer#error(Throwable)}.
     * @param complete A delegator method of {@link Observer#complete()}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public Disposable to(Runnable next, Consumer<Throwable> error, Runnable complete) {
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
    public Disposable to(Consumer<? super V> next, Consumer<Throwable> error, Runnable complete) {
        return to(next, error, complete, Disposable.empty(), true);
    }

    /**
     * Receive values from this {@link Signal}.
     *
     * @param observer A value observer of this {@link Signal}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public Disposable to(Observer<? super V> observer) {
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
    public Disposable to(Observer<V> observer, Disposable disposer) {
        try {
            return subscriber.apply(observer, disposer == null ? Disposable.empty() : disposer);
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
    public Variable<V> to() {
        return to(Variable.empty(), Variable::set);
    }

    /**
     * Receive values as {@link Variable} from this {@link Signal}.
     *
     * @return A {@link Variable} as value receiver.
     */
    public <A, R> R to(Collector<? super V, A, R> receiver) {
        return receiver.finisher().apply(to(receiver.supplier().get(), receiver.accumulator()::accept));
    }

    /**
     * Receive values from this {@link Signal}.
     *
     * @param receiver A value receiver.
     * @param assigner A value assigner.
     * @return A value receiver.
     */
    public <R> R to(R receiver, WiseBiConsumer<R, V> assigner) {
        // start receiving values
        to(assigner.bind(receiver));

        // API definition
        return receiver;
    }

    /**
     * Receive values as {@link Collection} from this {@link Signal}.
     *
     * @return A {@link Collection} as value receiver.
     */
    public <C extends Collection<? super V>> C toCollection(C collection) {
        return to(collection, Collection::add);
    }

    /**
     * Groups the elements according to the classification function and returns the result in a
     * {@link Map}. The classification function maps elements to some key type K. The method
     * produces a {@link Map} whose keys are the values resulting from applying the classification
     * function to the input elements, and whose corresponding values are Lists containing the input
     * elements which map to the associated key under the classification function.
     * <p>
     * There are no guarantees on the type, mutability, serializability, or thread-safety of the
     * {@link Map} or {@link List} objects returned.
     * 
     * @param <K> The type of the keys.
     * @param keyGenerator The classifier function mapping input elements to keys.
     * @return A grouping {@link Map} by your classification.
     */
    public <K> Map<K, List<V>> toGroup(Function<V, K> keyGenerator) {
        return to(Collectors.groupingBy(keyGenerator));
    }

    /**
     * Receive values as {@link List} from this {@link Signal}.
     *
     * @return A {@link List} as value receiver.
     */
    public List<V> toList() {
        return toCollection(new ArrayList());
    }

    /**
     * Receive values as {@link Map} from this {@link Signal}.
     * 
     * @param keyGenerator A {@link Map} key generator.
     * @return A {@link Map} as value receiver.
     */
    public <Key> Map<Key, V> toMap(Function<V, Key> keyGenerator) {
        return toMap(keyGenerator, Function.identity());
    }

    /**
     * Receive values as {@link Map} from this {@link Signal}.
     * 
     * @param keyGenerator A {@link Map} key generator.
     * @param valueGenerator A {@link Map} value generator.
     * @return A {@link Map} as value receiver.
     */
    public <Key, Value> Map<Key, Value> toMap(Function<V, Key> keyGenerator, Function<V, Value> valueGenerator) {
        // When compiling with ECJ, this is not a problem, but when compiling with JDK, an error
        // will occur unless you explicitly specify the generic type.
        return to(Collectors.toMap(keyGenerator, valueGenerator, (BinaryOperator<Value>) UNDEF));
    }

    /**
     * Receive values as {@link Set} from this {@link Signal}.
     *
     * @return A {@link Set} as value receiver.
     */
    public Set<V> toSet() {
        return toCollection(new HashSet());
    }

    /**
     * Returns {@link Signal} that emits a Boolean that indicates whether all the items emitted
     * by the source {@link Signal} satisfy a condition.
     * 
     * @param condition A condition that evaluates an item and returns a Boolean.
     * @return {ChainableAPI}
     */
    public Signal<Boolean> all(Predicate<? super V> condition) {
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
    public Signal<Boolean> any(Predicate<? super V> condition) {
        return signal(Objects.requireNonNull(condition), TRUE, false, FALSE, true, FALSE);
    }

    /**
     * Filters the values of an {@link Signal} sequence based on the specified type.
     *
     * @param type The type of result. {@code null} throws {@link NullPointerException}.
     * @return {ChainableAPI}
     * @throws NullPointerException If the type is {@code null}.
     */
    public <R> Signal<R> as(Class<? extends R>... type) {
        if (type == null || type.length == 0) {
            return (Signal<R>) this;
        }

        return (Signal<R>) take(v -> {
            for (Class c : type) {
                if (c != null && I.wrap(c).isInstance(v)) return true;
            }

            if (v instanceof Throwable) {
                throw I.quiet(v);
            } else {
                return false;
            }
        });
    }

    /**
     * <p>
     * It accumulates all the elements and flows them together as {@link List} buffer upon
     * completion.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①───②───③──╂
     * ↓ ↓ ↓ ↓
     * ┌─────────────┐
     * buffer (all)
     * └─────────────┘
     * ↓
     * ────────────[①②③]╂
     * </pre>
     *
     * @return {ChainableAPI}
     * @see <a href="https://reactivex.io/documentation/operators/buffer.html">ReactiveX buffer</a>
     */
    public Signal<List<V>> buffer() {
        return buffer(never());
    }

    /**
     * <p>
     * It accumulates elements, and whenever it reaches the specified size, it flows them together
     * as {@link List} buffer. Note that if the elements have not accumulated to the specified size
     * at the time of completion, they will all be discarded.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①─②─③─④─⑤─⑥─⑦╂
     * ↓ ↓ ↓ ↓ ↓ ↓ ↓
     * ┌──────────────┐
     * buffer (3)
     * └──────────────┘
     * ↓ ↓
     * ──────[①②③]──[④⑤⑥]─╂
     * </pre>
     *
     * @param size A length of each buffer.
     * @return {ChainableAPI}
     * @see <a href="https://reactivex.io/documentation/operators/buffer.html">ReactiveX buffer</a>
     */
    public Signal<List<V>> buffer(int size) {
        return buffer(size, size);
    }

    /**
     * <p>
     * It accumulates elements at the specified intervals, and whenever it reaches the specified
     * size, it flows them together as {@link List} buffer. Note that if the elements have not
     * accumulated to the specified size at the time of completion, they will all be discarded.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①───②───③───④───⑤─╂
     * ↓ ↓ ↓ ↓ ↓
     * ┌───────────────────┐
     * buffer (2, 1)
     * └───────────────────┘
     * ↓ ↓ ↓ ↓
     * ───────[①②]─[②③]─[③④]─[④⑤]─╂
     * </pre>
     *
     * @param size A length of each buffer. Zero or negative number are treated exactly the same way
     *            as 1.
     * @param interval A number of values to skip between creation of consecutive buffers. Zero or
     *            negative number are treated exactly the same way as 1.
     * @return {ChainableAPI}
     * @see <a href="https://reactivex.io/documentation/operators/buffer.html">ReactiveX buffer</a>
     */
    public Signal<List<V>> buffer(int size, int interval) {
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
                    // Thinking about what is the fastest way to copy data from Deque to List.
                    // The following three methods are slower and memory-heavy because they perform
                    // two complete copies from the data source array.
                    //
                    // List.copyOf(buffer)
                    // List.of(buffer.toArray())
                    // new ArrayList<>(buffer)
                    //
                    // The following method is faster and lighter because it only copies the array
                    // only once and wraps as list.
                    observer.accept((List<V>) Arrays.asList(buffer.toArray()));
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
     * <p>
     * It accumulates elements and flows them together as {@link List} buffer at each specified
     * timing. Note that all unflowed accumulated elements at the time of completion will be
     * discarded.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ────────▽──────────▽─╂ timing
     * ↓ ↓
     * ───①──②────③────④⑤───╂ signal
     * ↓ ↓ ↓ ↓↓
     * ┌───────────────────┐
     * buffer (timing)
     * └───────────────────┘
     * ↓ ↓
     * ───────[①②]────────[③④⑤]╂
     * </pre>
     * 
     * @param timing A timing {@link Signal}.
     * @return {ChainableAPI}
     * @see <a href="https://reactivex.io/documentation/operators/buffer.html">ReactiveX buffer</a>
     */
    public Signal<List<V>> buffer(Signal timing) {
        return buffer(timing, (Supplier<List<V>>) ArrayList::new, List::add, false).skip(List::isEmpty);
    }

    /**
     * Returns an {@link Signal} that emits non-overlapping buffered items from the source
     * {@link Signal} each time the specified timing {@link Signal} emits an item.
     * 
     * @param timing A timing {@link Signal}.
     * @param supplier A factory function that returns an instance of the collection subclass to be
     *            used and returned as the buffer.
     * @param assigner An operation function that assigns a value to the buffer.
     * @param ignoreRemaining A flag whether completion event emits the remaining values or not.
     * @return {ChainableAPI}
     */
    private <B> Signal<B> buffer(Signal<?> timing, Supplier<B> supplier, BiConsumer<B, V> assigner, boolean ignoreRemaining) {
        return new Signal<>((observer, disposer) -> {
            AtomicReference<B> buffer = new AtomicReference(supplier.get());

            WiseRunnable transfer = () -> observer.accept(buffer.getAndSet(supplier.get()));
            WiseRunnable completer = ignoreRemaining ? observer::complete : I.bundle(transfer, observer::complete);

            return to(v -> assigner.accept(buffer.get(), v), observer::error, completer, disposer, false)
                    .add(timing.to(transfer, observer::error, completer));
        });
    }

    /**
     * <p>
     * It flows the pair of each elements coming from all signals. In order to flow a new pair,
     * there must be at least one or more unflowed elements in every signals.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②──────③─────④⑤──╂ signal
     * ↓↓ ↓ ↓↓
     * ─────❶───❷──❸──❹─────╂ other
     * ↓ ↓ ↓ ↓
     * ┌───────────────────┐
     * combine (other)
     * └───────────────────┘
     * ↓ ↓ ↓ ↓
     * ─────[①❶]─[②❷]─[③❸]──[④❹]──╂
     * </pre>
     *
     * @param other Another {@link Signal} to combine.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     * @see <a href="https://reactivex.io/documentation/operators/zip.html">ReactiveX zip</a>
     */
    public <O> Signal<Ⅱ<V, O>> combine(Signal<O> other) {
        return combine(other, I::pair);
    }

    /**
     * <p>
     * It flows the pair of each elements coming from all signals. In order to flow a new pair,
     * there must be at least one or more unflowed elements in every signals.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②──────③─────④⑤──╂ signal
     * ↓↓ ↓ ↓↓
     * ─────❶───❷──❸──❹─────╂ other
     * ↓ ↓ ↓ ↓
     * ──Ⓐ───────Ⓑ────────Ⓒ─╂ another
     * ↓ ↓ ↓
     * ┌───────────────────┐
     * combine (other, another)
     * └───────────────────┘
     * ↓ ↓ ↓
     * ────[①❶Ⓐ]─[②❷Ⓑ]─────[③❸Ⓒ]─╂
     * </pre>
     *
     * @param other Another {@link Signal} to combine.
     * @param another An another {@link Signal} to combine.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     * @see <a href="https://reactivex.io/documentation/operators/zip.html">ReactiveX zip</a>
     */
    public <O, A> Signal<Ⅲ<V, O, A>> combine(Signal<O> other, Signal<A> another) {
        return combine(other, I::pair).combine(another, Ⅱ<V, O>::<A> ⅲ);
    }

    /**
     * <p>
     * It flows the pair of each elements coming from all signals. In order to flow a new pair,
     * there must be at least one or more unflowed elements in every signals.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②──────③─────④⑤─╂ signal
     * ↓↓ ↓ ↓↓
     * ─────○───●──◎──●────╂ other
     * ↓ ↓ ↓ ↓
     * ┌──────────────────┐
     * combine (other, a and b)
     * └──────────────────┘
     * ↓ ↓ ↓ ↓
     * ─────①───❷──⓷────❹──╂
     * </pre>
     *
     * @param other Another {@link Signal} to combine.
     * @param combiner An aggregation function used to combine the items emitted by the source
     *            {@link Signal}.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     * @see <a href="https://reactivex.io/documentation/operators/zip.html">ReactiveX zip</a>
     */
    public <O, R> Signal<R> combine(Signal<O> other, BiFunction<V, O, R> combiner) {
        return new Signal<>((observer, disposer) -> {
            LinkedList<V> baseValue = new LinkedList();
            LinkedList<O> otherValue = new LinkedList();
            boolean[] completes = new boolean[2];

            return to(value -> {
                if (!completes[0]) {
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
                if (!completes[1]) {
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
     * <p>
     * It flows the pair of each elements coming from all signals. In order to flow a new pair,
     * there must be at least one or more unflowed elements in every signals.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②──────③─────④⑤─╂ signal
     * ↓↓ ↓ ↓↓
     * ─────○───●──◎──●────╂ other
     * ↓ ↓ ↓ ↓
     * ┌──────────────────┐
     * combine (other, a and b)
     * └──────────────────┘
     * ↓ ↓ ↓ ↓
     * ─────①───❷──⓷────❹──╂
     * </pre>
     *
     * @param others Other {@link Signal} to combine.
     * @param operator A function that, when applied to an item emitted by each of the source
     *            {@link Signal}, results in an item that will be emitted by the resulting
     *            {@link Signal}.
     * @return A {@link Signal} that emits items that are the result of combining the items emitted
     *         by source {@link Signal} by means of the given aggregation function.
     * @see <a href="https://reactivex.io/documentation/operators/zip.html">ReactiveX zip</a>
     */
    public Signal<V> combine(Signal<V>[] others, BinaryOperator<V> operator) {
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
     * It flows the pair of each latest elements coming from all signals. In order to flow a new
     * pair, there must be at least one or more unflowed elements in any signal.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②───────③───④──╂ signal
     * ↓↓ ↓ ↓
     * ─────❶───❷─────────╂ other
     * ↓ ↓
     * ┌─────────────────┐
     * combineLatest (other)
     * └─────────────────┘
     * ↓ ↓ ↓ ↓
     * ────[②❶]─[②❷]─[③❷]─[④❷]──╂
     * </pre>
     *
     * @param other Another {@link Signal} to combine.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public <O> Signal<Ⅱ<V, O>> combineLatest(Signal<O> other) {
        return combineLatest(other, I::pair);
    }

    /**
     * <p>
     * It flows the pair of each latest elements coming from all signals. In order to flow a new
     * pair, there must be at least one or more unflowed elements in any signal.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②─────────③──────╂ signal
     * ↓↓ ↓
     * ─────❶────❷──────────╂ other
     * ↓ ↓
     * ──Ⓐ────────────────Ⓑ─╂ another
     * ↓ ↓
     * ┌───────────────────┐
     * combineLatest (other, another)
     * └───────────────────┘
     * ↓ ↓ ↓ ↓
     * ────[②❶Ⓐ]─[②❷Ⓐ]─[③❷Ⓐ]─[③❷Ⓑ]╂
     * </pre>
     *
     * @param other Another {@link Signal} to combine.
     * @param another An another {@link Signal} to combine.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public <O, A> Signal<Ⅲ<V, O, A>> combineLatest(Signal<O> other, Signal<A> another) {
        return combineLatest(other, I::pair).combineLatest(another, Ⅱ<V, O>::<A> ⅲ);
    }

    /**
     * <p>
     * It flows the pair of each latest elements coming from all signals. In order to flow a new
     * pair, there must be at least one or more unflowed elements in any signal.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②───────③───④──╂ signal
     * ↓↓ ↓ ↓
     * ─────◎───●─────○───╂ other
     * ↓ ↓ ↓
     * ┌─────────────────┐
     * combineLatest (other, A and B)
     * └─────────────────┘
     * ↓ ↓ ↓ ↓↓
     * ─────⓶───❷──❸──③④─╂
     * </pre>
     *
     * @param other Another {@link Signal} to combine.
     * @param function An aggregation function used to combine the items emitted by the source
     *            {@link Signal}.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public <O, R> Signal<R> combineLatest(Signal<O> other, BiFunction<V, O, R> function) {
        return new Signal<>((observer, disposer) -> {
            AtomicReference<V> baseValue = new AtomicReference(UNDEF);
            AtomicReference<O> otherValue = new AtomicReference(UNDEF);
            Subscriber completer = countable(observer, 2);

            return disposer.add(to(value -> {
                baseValue.set(value);
                O joined = otherValue.get();

                if (joined != UNDEF) {
                    observer.accept(function.apply(value, joined));
                }
            }, observer::error, completer::complete)).add(other.to(value -> {
                otherValue.set(value);

                V joined = baseValue.get();

                if (joined != UNDEF) {
                    observer.accept(function.apply(joined, value));
                }
            }, observer::error, completer::complete));
        });
    }

    /**
     * <p>
     * It flows the pair of each latest elements coming from all signals. In order to flow a new
     * pair, there must be at least one or more unflowed elements in any signal.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②───────③───④──╂ signal
     * ↓↓ ↓ ↓
     * ─────◎───●─────○───╂ other
     * ↓ ↓ ↓
     * ┌─────────────────┐
     * combineLatest (others, A and B)
     * └─────────────────┘
     * ↓ ↓ ↓ ↓↓
     * ─────⓶───❷──❸──③④──╂
     * </pre>
     *
     * @param others Other {@link Signal} to combine.
     * @param operator An aggregation function used to combine the items emitted by the source
     *            {@link Signal}.
     * @return An {@link Signal} that emits items that are the result of combining the items emitted
     *         by the source {@link Signal} by means of the given aggregation function
     */
    public Signal<V> combineLatest(Signal<V>[] others, BinaryOperator<V> operator) {
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
     * Start the specified signal after the current signal is completed.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②──③──╂ signal
     * ↓↓ ↓ ↓
     * ─④───⑤⑥─╂ other
     * ↓ ↓↓ ↓
     * ┌─────────────────┐
     * concat (other)
     * └─────────────────┘
     * ↓↓ ↓ ↓ ↓↓
     * ───①②──③───④───⑤⑥─╂
     * </pre>
     * 
     * @param others A sequence of {@link Signal}s to concat.
     * @return {ChainableAPI}
     * @see <a href="https://reactivex.io/documentation/operators/concat.html">ReeactiveX concat</a>
     */
    public Signal<V> concat(Signal<? extends V>... others) {
        // ignore invalid parameters
        if (others == null || others.length == 0) {
            return this;
        }
        return concat(Arrays.asList(others));
    }

    /**
     * <p>
     * Start the specified signal after the current signal is completed.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①②──③──╂ signal
     * ↓↓ ↓ ↓
     * ─④───⑤⑥─╂ other
     * ↓ ↓↓ ↓
     * ┌─────────────────┐
     * concat (other)
     * └─────────────────┘
     * ↓↓ ↓ ↓ ↓↓
     * ───①②──③───④───⑤⑥─╂
     * </pre>
     * 
     * @param others A sequence of {@link Signal}s to concat.
     * @return {ChainableAPI}
     * @see <a href="https://reactivex.io/documentation/operators/concat.html">ReeactiveX concat</a>
     */
    public Signal<V> concat(Iterable<Signal<? extends V>> others) {
        // ignore invalid parameters
        if (others == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Iterator<Signal<? extends V>> signals = I.signal(others).skipNull().startWith(this).toList().iterator();

            I.recurse(self -> {
                if (signals.hasNext()) {
                    signals.next().to(observer, observer::error, self, disposer.sub(), true);
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
    public <R> Signal<R> concatMap(WiseFunction<V, Signal<R>> function) {
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
     * Returns a {@link Signal} that counts the total number of items emitted by the source
     * {@link Signal} and emits this count as a 64-bit Long.
     * 
     * @return {@link Signal} that emits a single item: the number of items emitted by the source
     *         {@link Signal} as a 64-bit Long item
     */
    public Signal<Long> count() {
        return map(AtomicLong::new, (counter, v) -> counter.incrementAndGet());
    }

    /**
     * Drops values that are followed by newer values before a timeout. The timer resets on each
     * value emission.
     *
     * @param time A time value. Zero or negative number will ignore this instruction.
     * @param unit A time unit. {@code null} will ignore this instruction.
     * @param scheduler
     * @return {ChainableAPI}
     */
    public Signal<V> debounce(long time, TimeUnit unit, ScheduledExecutorService... scheduler) {
        return debounce(time, unit, false, scheduler);
    }

    /**
     * Drops values that are followed by newer values before a timeout. The timer resets on each
     * value emission.
     *
     * @param time A time value. Zero or negative number will ignore this instruction.
     * @param unit A time unit. {@code null} will ignore this instruction.
     * @param acceptFirst Determines whether to pass the first element or not. It is useful to get
     *            the beginning and end of a sequence of events.
     * @param scheduler
     * @return {ChainableAPI}
     */
    public Signal<V> debounce(long time, TimeUnit unit, boolean acceptFirst, ScheduledExecutorService... scheduler) {
        return debounce(Variable.of(time), unit, acceptFirst, scheduler);
    }

    /**
     * Drops values that are followed by newer values before a timeout. The timer resets on each
     * value emission.
     *
     * @param time A time value. Zero or negative number will ignore this instruction.
     * @param unit A time unit. {@code null} will ignore this instruction.
     * @param acceptFirst Determines whether to pass the first element or not. It is useful to get
     *            the beginning and end of a sequence of events.
     * @param scheduler
     * @return {ChainableAPI}
     */
    public Signal<V> debounce(Variable<Long> time, TimeUnit unit, boolean acceptFirst, ScheduledExecutorService... scheduler) {
        return debounceX(time, unit, acceptFirst, scheduler).flatMap(v -> I.signal(v).last());
    }

    /**
     * Collect values that are followed by newer values before a timeout. The timer resets on each
     * value emission.
     *
     * @param time A time value. Zero or negative number will ignore this instruction.
     * @param unit A time unit. {@code null} will ignore this instruction.
     * @param scheduler
     * @return {ChainableAPI}
     */
    public Signal<List<V>> debounceAll(long time, TimeUnit unit, ScheduledExecutorService... scheduler) {
        return debounceAll(Variable.of(time), unit, scheduler);
    }

    /**
     * Collect values that are followed by newer values before a timeout. The timer resets on each
     * value emission.
     *
     * @param time A time value. Zero or negative number will ignore this instruction.
     * @param unit A time unit. {@code null} will ignore this instruction.
     * @param scheduler
     * @return {ChainableAPI}
     */
    public Signal<List<V>> debounceAll(Variable<Long> time, TimeUnit unit, ScheduledExecutorService... scheduler) {
        return debounceX(time, unit, false, scheduler);
    }

    /**
     * Collect values that are followed by newer values before a timeout. The timer resets on each
     * value emission.
     *
     * @param time A time value. Zero or negative number will ignore this instruction.
     * @param unit A time unit. {@code null} will ignore this instruction.
     * @param scheduler
     * @return {ChainableAPI}
     */
    private Signal<List<V>> debounceX(Variable<Long> time, TimeUnit unit, boolean acceptFirst, ScheduledExecutorService... scheduler) {
        // ignore invalid parameters
        if (time == null || time.or(0L) <= 0 || unit == null) {
            return map(List::of);
        }

        return new Signal<>((observer, disposer) -> {
            AtomicReference<Disposable> latest = new AtomicReference();
            AtomicReference<List<V>> list = new AtomicReference(new ArrayList());

            return to(value -> {
                List<V> q = list.get();
                if (acceptFirst && q.isEmpty()) {
                    observer.accept(List.of(value));
                }
                q.add(value);

                Disposable d = latest.get();

                if (d != null) {
                    d.dispose();
                }

                latest.set(I.schedule(time.v, 0, unit, false, scheduler).to(() -> {
                    latest.set(null);
                    observer.accept(list.getAndSet(new ArrayList<>()));
                }));
            }, observer::error, observer::complete, disposer, false);
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
     */
    public Signal<V> delay(long time, TimeUnit unit, ScheduledExecutorService... scheduler) {
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
    public Signal<V> delay(Duration time, ScheduledExecutorService... scheduler) {
        // ignore invalid parameters
        if (time == null || time.isNegative() || time.isZero()) {
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
    public Signal<V> delay(Function<V, Duration> time, ScheduledExecutorService... scheduler) {
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
     * @see #diff(BiPredicate)
     */
    public Signal<V> diff() {
        return skip((V) null, Objects::equals);
    }

    /**
     * Returns an {@link Signal} that emits all items emitted by the source {@link Signal} that are
     * distinct from their immediate predecessors when compared preassign each other via the
     * provided comparator function.
     *
     * @param compare The function that receives the previous item and the current item and is
     *            expected to return true if the two are equal, thus skipping the current value.
     * @return {@link Signal} that emits those items from the source {@link Signal} that are
     *         distinct from their immediate predecessors.
     * @see #diff()
     */
    public Signal<V> diff(BiPredicate<V, V> compare) {
        // ignore invalid parameter
        if (compare == null) {
            return this;
        }

        return skip((V) null, (prev, now) -> prev == null || now == null ? prev == now : compare.test(prev, now));
    }

    /**
     * Returns an {@link Signal} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     *
     * @return {ChainableAPI}
     * @see #distinct(WiseFunction)
     */
    public Signal<V> distinct() {
        return distinct(I.wiseF(Function.identity()));
    }

    /**
     * Returns an {@link Signal} consisting of the distinct values (according to
     * {@link Object#equals(Object)}) of this stream.
     *
     * @return {ChainableAPI}
     * @see #distinct()
     */
    public Signal<V> distinct(WiseFunction<V, ?> keySelector) {
        if (keySelector == null) {
            return this;
        }
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
    public Signal<V> effect(WiseRunnable effect) {
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
    public Signal<V> effect(WiseConsumer<? super V> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Subscriber o = new Subscriber();
            o.observer = observer;
            o.next = I.bundle(effect, observer);
            return to(o, disposer);
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
    public Signal<V> effectAfter(WiseRunnable effect) {
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
    public Signal<V> effectAfter(WiseConsumer<? super V> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Subscriber o = new Subscriber();
            o.observer = observer;
            o.next = I.bundle(observer, effect);
            return to(o, disposer);
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
    public Signal<V> effectOnce(WiseRunnable effect) {
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
    public Signal<V> effectOnce(WiseConsumer<? super V> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Subscriber<V> o = new Subscriber();
            o.observer = observer;
            o.next = v -> {
                effect.accept(v);
                observer.accept(v);
                o.next = null;
            };
            return to(o, disposer);
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
    public Signal<V> effectOnComplete(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Subscriber o = new Subscriber();
            o.observer = observer;
            o.complete = I.bundle(effect, observer::complete);
            return to(o, disposer);
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
    public Signal<V> effectOnDispose(WiseRunnable effect) {
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
    public Signal<V> effectOnError(WiseRunnable effect) {
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
    public Signal<V> effectOnError(WiseConsumer<Throwable> effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Subscriber o = new Subscriber();
            o.observer = observer;
            o.error = I.bundle(effect, observer::error);
            return to(o, disposer);
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
    public Signal<V> effectOnLifecycle(WiseFunction<Disposable, WiseConsumer<V>> effect) {
        if (effect == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            return effect(effect.apply(disposer)).to(observer, disposer);
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
    public Signal<V> effectOnObserve(WiseRunnable effect) {
        // ignore invalid parameter
        if (effect == null) {
            return this;
        }
        return effectOnObserve(I.wiseC(effect));
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
    public Signal<V> effectOnObserve(WiseConsumer<? super Disposable> effect) {
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
    public Signal<V> effectOnTerminate(WiseRunnable effect) {
        return effectOnError(effect).effectOnComplete(effect);
    }

    /**
     * Returns {@link Signal} that emits only the very first item emitted by the source
     * {@link Signal}, or completes if the source {@link Signal} is empty.
     * 
     * @return {ChainableAPI}
     */
    public Signal<V> first() {
        return signal(I::accept, null, false, null, true, null);
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
    public <R> Signal<R> flatArray(WiseFunction<V, R[]> function) {
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
    public <R> Signal<R> flatIterable(WiseFunction<V, ? extends Iterable<R>> function) {
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
    public <R> Signal<R> flatMap(WiseFunction<V, Signal<R>> function) {
        return flatMap(Variable.of(Objects.requireNonNull(function)), WiseFunction::apply);
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
    public <C, R> Signal<R> flatMap(Supplier<C> context, WiseBiFunction<C, V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            C c = context == null ? null : context.get();
            Subscriber end = countable(observer, 1);
            end.next = observer;

            return to(value -> {
                end.index++;
                function.apply(c, value).to(end, end::error, null, disposer.sub().add(end::complete), true);
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
    public <R> Signal<R> flatVariable(WiseFunction<V, Variable<R>> function) {
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
    public Signal<Ⅱ<V, Long>> index(long start) {
        return map(((WiseFunction<Long, AtomicLong>) AtomicLong::new)
                .bind(start), (context, value) -> I.pair(value, context.getAndIncrement()));
    }

    /**
     * Ensure the interval time for each values in {@link Signal} sequence.
     *
     * @param interval Time to emit values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified interval. {@code null} will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public Signal<V> interval(long interval, TimeUnit unit, ScheduledExecutorService... scheduler) {
        // ignore invalid parameters
        if (interval <= 0 || unit == null) {
            return this;
        }

        Signaling<Duration> next = new Signaling();

        return combine(next.expose.startWith(Duration.ZERO).delay(Function.identity(), scheduler)).map(Ⅱ::ⅰ)
                .effectAfter(I.wiseC(next).bind(Duration.of(interval, unit.toChronoUnit())));
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
    public Signal<Boolean> is(Predicate<? super V> condition) {
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
     * @return {ChainableAPI}
     */
    public Signal<Boolean> isNot(Predicate<? super V> condition) {
        return is(condition.negate());
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is completed.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         completed.
     */
    public Signal<Boolean> isCompleted() {
        return signal(null, FALSE, false, FALSE, true, TRUE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} emits any value.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} emits
     *         any value.
     */
    public Signal<Boolean> isEmitted() {
        return signal(I::accept, TRUE, true, FALSE, true, FALSE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is completed preassignout any value emitted.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         completed preassignout any value emitted.
     */
    public Signal<Boolean> isEmpty() {
        return signal(I::accept, FALSE, true, FALSE, true, TRUE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is erred.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         erred.
     */
    public Signal<Boolean> isErred() {
        return signal(null, FALSE, true, TRUE, true, FALSE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is emitted, erred or completed.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         emitted, erred or completed.
     */
    public Signal<Boolean> isSignaled() {
        return signal(I::accept, TRUE, true, TRUE, true, TRUE);
    }

    /**
     * Returns {@link Signal} that emits <code>true</code> that indicates whether the source
     * {@link Signal} is erred or completed.
     * 
     * @return A {@link Signal} that emits <code>true</code> when the source {@link Signal} is
     *         erred or completed.
     */
    public Signal<Boolean> isTerminated() {
        return signal(null, FALSE, true, TRUE, true, TRUE);
    }

    /**
     * Returns a new {@link Signal} that invokes the mapper action in parallel thread and waits all
     * of them until all actions are completed.
     *
     * @param function A mapper function.
     * @return {ChainableAPI}
     */
    public <R> Signal<R> joinAll(WiseFunction<V, R> function, ExecutorService... executor) {
        return map(function::bind).buffer().flatIterable(v -> I.signal(I.vouch(I.Jobs, executor).invokeAll(v)).map(Future::get).toList());
    }

    /**
     * Executes all tasks in parallel and returns their results only if all succeed.
     * If any task fails, all remaining tasks are cancelled and the exception is propagated.
     *
     * @param function The task mapper function.
     * @return {ChainableAPI}
     */
    public <R> Signal<R> joinAllOrNone(WiseFunction<V, R> function, ExecutorService... executor) {
        return map(function::bind).buffer().flatIterable(v -> {
            List<Future<R>> futures = new ArrayList();
            ExecutorCompletionService<R> service = new ExecutorCompletionService<>(I.vouch(I.Jobs, executor));

            for (Callable<R> task : v) {
                futures.add(service.submit(task));
            }

            try {
                for (int i = 0; i < v.size(); i++) {
                    service.take().get();
                }
            } catch (Throwable e) {
                for (Future<R> future : futures) {
                    future.cancel(true);
                }
                throw e;
            }

            return futures;
        }).map(Future::get);
    }

    /**
     * Returns a new {@link Signal} that invokes the mapper action in parallel thread and waits
     * until any single action is completed. All other actions will be cancelled.
     * 
     * @param function A mapper function.
     * @return {ChainableAPI}
     */
    @SuppressWarnings("resource")
    public <R> Signal<R> joinAny(WiseFunction<V, R> function, ExecutorService... executor) {
        return map(function::bind).buffer().map(I.vouch(I.Jobs, executor)::invokeAny);
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
    public <R> Signal<Map<V, R>> keyMap(WiseFunction<V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return flatMap(ConcurrentHashMap<V, R>::new, (map, v) -> {
            return function.apply(v).map(r -> {
                map.put(v, r);
                return map;
            });
        });
    }

    /**
     * Returns a {@link Signal} that emits the last item emitted by this {@link Signal} or completes
     * if this {@link Signal} is empty.
     * 
     * @return {ChainableAPI}
     */
    public Signal<V> last() {
        return buffer(never(), AtomicReference<V>::new, AtomicReference::set, false).map(AtomicReference::get).skipNull();
    }

    /**
     * <p>
     * Returns an {@link Signal} that applies the given function to each value emitted by an
     * {@link Signal} and emits the result.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①───②───③───④───⑤──┼
     * ↓ ↓ ↓ ↓ ↓ ↓
     * ┌────────────────────┐
     * map ○→●
     * └────────────────────┘
     * ↓ ↓ ↓ ↓ ↓ ↓
     * ───❶───❷───❸───❹───❺──┼
     * </pre>
     *
     * @param function A converter function to apply to each value emitted by this {@link Signal} .
     *            {@code null} will ignore this instruction.
     * @return {ChainableAPI}
     */
    public <R> Signal<R> map(WiseFunction<? super V, R> function) {
        return map(Variable.of(Objects.requireNonNull(function)), WiseFunction::apply);
    }

    /**
     * <p>
     * {@link #map(WiseFunction)} preassign context.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①───②───③───④───⑤──┼
     * ↓ ↓ ↓ ↓ ↓
     * ┌────────────────────┐
     * map ○→●
     * └────────────────────┘
     * ↓ ↓ ↓ ↓ ↓ ↓
     * ───❶───❷───❸───❹───❺──┼
     * </pre>
     * 
     * @param context A {@link Supplier} of {@link Signal} specific context.
     * @param function A converter function to apply to each value emitted by this {@link Signal} .
     *            {@code null} will ignore this instruction.
     * @return {ChainableAPI}
     */
    public <C, R> Signal<R> map(Supplier<C> context, WiseBiFunction<C, ? super V, R> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            C c = context == null ? null : context.get();

            return to(value -> observer.accept(function.apply(c, value)), observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * 
     * 
     * @param function
     * @return
     */
    public Signal<V> mapError(WiseFunction<Throwable, Throwable> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            return to(observer::accept, e -> observer.error(function.apply(e)), observer::complete, disposer, false);
        });
    }

    /**
     * Returns an {@link Signal} that applies the given constant to each item emitted by an
     * {@link Signal} and emits the result.
     *
     * @param constant A constant to apply to each value emitted by this {@link Signal}.
     * @return {ChainableAPI}
     */
    public <R> Signal<R> mapTo(R constant) {
        return map(I.wiseF(constant));
    }

    /**
     * <p>
     * Flattens a sequence of {@link Signal} emitted by an {@link Signal} into one {@link Signal},
     * preassignout any transformation.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①───②───③───④───⑤──┼
     * ↓ ↓ ↓ ↓ ↓
     * ─────❶────❷────❸────┼
     * ↓ ↓ ↓
     * ┌────────────────────┐
     * merge
     * └────────────────────┘
     * ↓ ↓ ↓ ↓↓ ↓ ↓ ↓
     * ───①─❶─②──❷③───④❸──⑤──┼
     * </pre>
     *
     * @param others A target {@link Signal} to merge. {@code null} will be ignored.
     * @return {ChainableAPI}
     */
    public Signal<V> merge(Signal<? extends V>... others) {
        return merge(Arrays.asList(others));
    }

    /**
     * <p>
     * Flattens a sequence of {@link Signal} emitted by an {@link Signal} into one {@link Signal},
     * preassignout any transformation.
     * </p>
     *
     * @param others A target {@link Signal} set to merge. {@code null} will be ignored.
     * @return {ChainableAPI}
     */
    public Signal<V> merge(Iterable<Signal<? extends V>> others) {
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
                signal.to(observer, observer::error, completer::complete, disposer.sub(), true);
            }
            return disposer;
        });
    }

    /**
     * <link rel="stylesheet" href="main.css" type="text/css">
     * <p>
     * Switch event stream context.
     * </p>
     * <pre class="marble-diagram" style="font: 11px/1.2 'Yu Gothic';">
     * ───①───②───③───④───⑤──┼
     * ↓ ↓ ↓ ↓ ↓
     * ┌────────────────────┐
     * on ━
     * └────────────────────┘
     * ↓ ↓ ↓ ↓ ↓ ↓
     * ━━━❶━━━❷━━━❸━━━❹━━━❺━━╋
     * </pre>
     * 
     * @param scheduler A new context
     * @return {ChainableAPI}
     */
    public Signal<V> on(Consumer<Runnable> scheduler) {
        // ignore invalid parameters
        if (scheduler == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            int[] countable = {1};
            return to(v -> {
                countable[0]++;
                scheduler.accept(() -> {
                    observer.accept(v);
                    if (--countable[0] == 0) {
                        observer.complete();
                    }
                });
            }, e -> {
                scheduler.accept(((WiseConsumer<Throwable>) observer::error).bind(e));
            }, () -> {
                if (--countable[0] == 0) {
                    scheduler.accept(observer::complete);
                }
            }, disposer, false);
        });
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that guarantee one item at least.
     * </p>
     *
     * @return {ChainableAPI}
     */
    public Signal<V> or(V value) {
        return or(I.signal(value));
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that guarantee one item at least.
     * </p>
     *
     * @return {ChainableAPI}
     */
    public Signal<V> or(Supplier<V> value) {
        return or(I.signal(value));
    }

    /**
     * <p>
     * Generates an {@link Signal} sequence that guarantee one item at least.
     * </p>
     *
     * @return {ChainableAPI}
     */
    public Signal<V> or(Signal<V> values) {
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
    public <O> Signal<O> plug(Function<Signal<V>, Signal<O>> plug) {
        return plug.apply(this);
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
    public Signal<V> recurse(WiseFunction<V, V> recurse, Executor... executor) {
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
     * @param executor An execution context.
     * @return {ChainableAPI}
     */
    public Signal<V> recurseMap(WiseFunction<Signal<V>, Signal<V>> recurse, Executor... executor) {
        // DON'T use the recursive call, it will throw StackOverflowError.
        return flatMap(init -> new Signal<>((observer, disposer) -> {
            I.vouch(Runnable::run, executor).execute(() -> {
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
     * Recover the source {@link Signal} on the specified error by the specified value. Unspecified
     * error types will pass through the source {@link Signal}.
     * 
     * @param value A value to replace error.
     * @return Chainable API
     */
    public Signal<V> recover(V value) {
        return recover(e -> e.mapTo(value));
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
     * <li>Complete - Terminate notifier signal. Source signal will never recover errors.</li>
     * </ul>
     * 
     * @param flow An error notifier to define recovering flow.
     * @return Chainable API
     */
    public <E extends Throwable> Signal<V> recover(WiseFunction<Signal<E>, Signal<V>> flow) {
        // ignore invalid parameter
        if (flow == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            // error notifier
            Subscriber<E> sub = new Subscriber();
            sub.next = e -> {
                // recorde the processing error
                sub.observer.accept(sub.o = e);
            };

            // define error recovering flow
            flow.apply(sub.signal()).to(v -> {
                sub.o = null; // processing error will be handled, so clear it
                if (v == UNDEF) {
                    observer.complete();
                } else {
                    observer.accept(v);
                }
            }, observer::error, () -> {
                // Since this error flow has ended,
                // all subsequent errors are passed to the source signal.
                sub.next = observer::error;

                // Since there is an error in processing, but this error flow has ended,
                // the processing error is passed to the source signal.

                // The following code is not used as it may send null.
                // if (sub.obj != null) observer.error(sub.obj);
                Throwable t = sub.o;
                if (t != null) observer.error(t);
            });

            // delegate error to the notifier
            return to(observer, sub, observer::complete, disposer, false);
        });
    }

    /**
     * Generates an {@link Signal} sequence that repeats the given value infinitely.
     *
     * @return {ChainableAPI}
     */
    public Signal<V> repeat() {
        return repeat(I.wiseF(Function.identity()));
    }

    /**
     * Returns an {@link Signal} that emits the same values as the source signal preassign the
     * exception to an {@link Observer#error(Throwable)}. An error notification from the source will
     * result in the emission of a Throwable item to the {@link Signal} provided as an argument to
     * the notificationHandler function. If that {@link Signal} calls {@link Observer#complete()} or
     * {@link Observer#error(Throwable)} then retry will call {@link Observer#complete()} or
     * {@link Observer#error(Throwable) } on the child subscription. Otherwise, this {@link Signal}
     * will resubscribe to the source {@link Signal}.
     * 
     * @param flow A receives an {@link Signal} of notifications preassign which a user can complete
     *            or error, aborting the retry.
     * @return Chainable API
     */
    public Signal<V> repeat(WiseFunction<Signal<?>, Signal<?>> flow) {
        // ignore invalid parameter
        if (flow == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            // build the actual complete handler
            Subscriber<Object> sub = new Subscriber();
            WiseRunnable complete = () -> {
                // recorde the processing completion
                sub.accept(sub.o = UNDEF);
            };

            // number of remaining repeats
            AtomicInteger remaining = new AtomicInteger();
            // previous repeat operation
            sub.disposer = Disposable.empty();

            // define complete repeating flow
            flow.apply(sub.signal()).to(v -> {
                sub.o = null; // processing complete will be handled, so clear it

                // If you are not repeating, repeat it immediately, otherwise you can do it later
                if (remaining.getAndIncrement() == 0) {
                    do {
                        // dispose previous and reconnect
                        sub.disposer.dispose();
                        sub.disposer = to(observer, observer::error, complete, disposer.sub(), true);
                    } while (remaining.decrementAndGet() != 0);
                }
            }, observer::error, () -> {
                // Since this complete flow has ended,
                // all subsequent complete are passed to the source signal.
                sub.next = I.wiseC(observer::complete);

                // Since there is a complete in processing, but this complete flow has ended,
                // the processing complete is passed to the source signal.
                if (sub.o != null) observer.complete();
            });

            // connect preassign complete handling flow
            sub.disposer = to(observer, observer::error, complete, disposer.sub(), true);

            // API definition
            return disposer;
        });
    }

    /**
     * Retry the source {@link Signal} infinitely whenever any error is occurred.
     *
     * @return {ChainableAPI}
     */
    public Signal<V> retry() {
        return retry(Signal::skipNull);
    }

    /**
     * <p>
     * Retry the source {@link Signal} when the specified error is occurred. Unspecified errors will
     * pass through the source {@link Signal}.
     * </p>
     * <h>When the notifier signal emits event</h>
     * <ul>
     * <li>Next - Retry source {@link Signal}.</li>
     * <li>Error - Propagate to source error and dispose them.</li>
     * <li>Complete - Terminate notifier signal. Source signal will never retry errors.</li>
     * </ul>
     * 
     * @param flow An error notifier to define retrying flow.
     * @return Chainable API
     */
    public <E extends Throwable> Signal<V> retry(WiseFunction<Signal<E>, Signal<?>> flow) {
        // ignore invalid parameter
        if (flow == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            // build the actual error handler
            Subscriber<E> sub = new Subscriber();
            sub.next = e -> {
                if (e instanceof UndeclaredThrowableException) {
                    e = (E) e.getCause();
                }
                // recorde the processing error
                //
                // to user defined error flow
                sub.observer.accept(sub.o = e);
            };

            // number of remaining retry
            AtomicInteger remaining = new AtomicInteger();
            // previous retry operation
            sub.disposer = Disposable.empty();

            // define error retrying flow
            flow.apply(sub.signal()).to(v -> {
                sub.o = null; // processing error will be handled, so clear it

                // If you are not retrying, retry it immediately, otherwise you can do it later
                if (remaining.getAndIncrement() == 0) {
                    do {
                        // dispose previous and reconnect
                        sub.disposer.dispose();
                        sub.disposer = to(observer, sub, observer::complete, disposer.sub(), true);
                    } while (remaining.decrementAndGet() != 0);
                }
            }, observer::error, () -> {
                // Since this error flow has ended,
                // all subsequent errors are passed to the source signal.
                sub.next = observer::error;

                // Since there is an error in processing, but this error flow has ended,
                // the processing error is passed to the source signal.
                //
                // The following code is not used as it may send null.
                // if (sub.obj != null) observer.error(sub.obj);
                Throwable t = sub.o;
                if (t != null) observer.error(t);
            });

            // connect preassign error handling flow
            sub.disposer = to(observer, sub, observer::complete, disposer.sub(), true);

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
    public Signal<V> reverse() {
        return buffer(never(), LinkedList<V>::new, Deque::addFirst, false).flatIterable(I.wiseF(Function.identity()));
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
    public Signal<V> sample(Signal<?> sampler) {
        return buffer(sampler, AtomicReference::new, AtomicReference<V>::set, true).map(AtomicReference::get);
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
    public <A, R> Signal<R> scan(Collector<? super V, A, R> collector) {
        return scan(collector.supplier(), I.make(null, WiseBiFunction.class, (WiseBiConsumer<A, V>) collector.accumulator()::accept))
                .map(x -> collector.combiner().apply(collector.supplier().get(), x))
                .map(I.wiseF(collector.finisher()));
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
    public <R> Signal<R> scan(Supplier<R> init, WiseBiFunction<R, V, R> function) {
        return scan(function.bindLazily(init), function);
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
    public <R> Signal<R> scan(WiseFunction<V, R> first, WiseBiFunction<R, V, R> others) {
        return new Signal<>((observer, disposer) -> {
            AtomicReference<R> ref = new AtomicReference(UNDEF);

            return to(v -> {
                observer.accept(ref.updateAndGet(prev -> prev == UNDEF ? first.apply(v) : others.apply(prev, v)));
            }, observer::error, observer::complete, disposer, false);
        });
    }

    /**
     * Maps a sequence of values into {@link Signal} and concatenates these {@link Signal} eagerly
     * into a single {@link Signal}. Eager concatenation means that once a subscriber subscribes,
     * this operator subscribes to all the source {@link Signal}. The operator buffers the values
     * emitted by these {@link Signal} and then drains them in order, each one after the previous
     * one completes.
     * 
     * @param function A function that maps a sequence of values into a sequence of {@link Signal}
     *            that will be eagerly concatenated.
     * @return {ChainableAPI}
     */
    public <R> Signal<R> sequenceMap(WiseFunction<V, Signal<R>> function) {
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
                        if (next.ⅰ.get()) {
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
    public Signal<V> share() {
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
    public Signal<V> skip(long count) {
        // ignore invalid parameter
        if (count <= 0) {
            return this;
        }
        return skipAt(i -> i < count);
    }

    /**
     * <p>
     * Alias for skip(I.set(excludes)).
     * </p>
     *
     * @param excludes A collection of skip items.
     * @return {ChainableAPI}
     */
    public Signal<V> skip(V... excludes) {
        return skip(I.set(excludes)::contains);
    }

    /**
     * <p>
     * Alias for take(condition.negate()).
     * </p>
     *
     * @param condition A skip condition.
     * @return {ChainableAPI}
     */
    public Signal<V> skip(Predicate<? super V> condition) {
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
    public Signal<V> skip(V init, BiPredicate<? super V, ? super V> condition) {
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
     *            {@code null} will ignore this instruction.
     * @return {ChainableAPI}
     */
    public <C> Signal<V> skip(Supplier<C> contextSupplier, BiPredicate<C, ? super V> condition) {
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
     * @param condition An external boolean {@link Signal}. {@code null} will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public Signal<V> skip(Signal<Boolean> condition) {
        return take(condition, false, false);
    }

    /**
     * <p>
     * Returns a specified index values from the start of an {@link Signal} sequence.
     * </p>
     * 
     * @param condition An index condition of values to emit.
     * @return {ChainableAPI}
     */
    public Signal<V> skipAt(LongPredicate condition) {
        return takeAt(condition.negate());
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items emitted by the source {@link Signal}, checks the
     * specified predicate for each item, and then completes if the condition is not signaled.
     * </p>
     *
     * @param condition A function that evaluates an item emitted by the source {@link Signal} and
     *            returns a value.
     * @return An {@link Signal} that first emits items emitted by the source {@link Signal}, checks
     *         the specified condition after each item, and then completes if the condition is not
     *         signaled.
     */
    public Signal<V> skipIf(Function<V, Signal<?>> condition) {
        return skip(v -> condition.apply(v).isEmitted().to().v);
    }

    /**
     * Return the {@link Signal} which ignores the specified error.
     * 
     * @param types A list of error types to ignore.
     * @return {@link Signal} which ignores the specified error.
     * @see #stopError(Class...)
     */
    public Signal<V> skipError(Class<? extends Throwable>... types) {
        return recover(e -> e.as(types).mapTo((V) null).skip(I::accept));
    }

    /**
     * Return the {@link Signal} which ignores complete event.
     * 
     * @return {@link Signal} which ignores complete event.
     */
    public Signal<V> skipComplete() {
        return new Signal<>((observer, disposer) -> {
            Subscriber o = new Subscriber();
            o.observer = observer;
            o.complete = I.NoOP;
            return to(o, disposer);
        });
    }

    /**
     * Alias for skip(Objects::isNull).
     *
     * @return {ChainableAPI}
     */
    public Signal<V> skipNull() {
        return skip(Objects::isNull);
    }

    /**
     * Returns an {@link Signal} that skips all items emitted by the source {@link Signal} as long
     * as a specified condition holds true, but emits all further source items as soon as the
     * condition becomes false.
     *
     * @param predicate A function to test each item emitted from the source {@link Signal}.
     * @return An {@link Signal} that begins emitting items emitted by the source {@link Signal}
     *         when the specified predicate becomes false.
     */
    public Signal<V> skipUntil(Predicate<? super V> predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Subscriber<V> o = new Subscriber();
            o.observer = observer;
            o.next = value -> {
                if (predicate.test(value)) {
                    o.next = null;
                    observer.accept(value);
                }
            };
            return to(o, disposer);
        });
    }

    /**
     * Returns the values from the source {@link Signal} sequence only after the other
     * {@link Signal} sequence produces a value.
     *
     * @param timing The second {@link Signal} that has to emit an item before the source
     *            {@link Signal} elements begin to be mirrored by the resulting {@link Signal}.
     * @return An {@link Signal} that skips items from the source {@link Signal} until the second
     *         {@link Signal} emits an item, then emits the remaining items.
     */
    public Signal<V> skipUntil(Signal timing) {
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
    public Signal<V> skipWhile(Predicate<? super V> predicate) {
        return skipUntil(predicate.negate());
    }

    /**
     * Buffer all items until complete event and then sorted items will be emitted sequentially.
     * 
     * @param comparator
     * @return {ChainableAPI}
     */
    public Signal<V> sort(Comparator<? super V> comparator) {
        return buffer().flatIterable(e -> {
            e.sort(comparator);
            return e;
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
    public Signal<V> startWith(V... values) {
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
    public Signal<V> startWith(Supplier<V> value) {
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
    public Signal<V> startWith(Iterable<V> values) {
        // ignore invalid parameter
        if (values == null) {
            return this;
        }

        return new Signal<>((observer, disposer) -> {
            Iterator<V> iterator = values.iterator();

            while (iterator.hasNext() && !disposer.isDisposed()) {
                observer.accept(iterator.next());
            }
            return to(observer, disposer);
        });
    }

    /**
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Signal}.
     *
     * @param values The initial values.
     * @return {ChainableAPI}
     */
    public Signal<V> startWith(Signal<V> values) {
        return values == null ? this : values.concat(this);
    }

    /**
     * Emit {@code null} item before beginning to emit the items from the source {@link Signal}.
     * 
     * @return {ChainableAPI}
     */
    public Signal<V> startWithNull() {
        return startWith((V) null);
    }

    /**
     * Return the {@link Signal} which replaces the specified error by complete event.
     * 
     * @param types A list of error types to replace.
     * @return {@link Signal} which replaces the specified error by complete event.
     * @see #skipError(Class...)
     */
    public Signal<V> stopError(Class<? extends Throwable>... types) {
        return recover(e -> (Signal<V>) e.as(types).mapTo(UNDEF));
    }

    /**
     * Asynchronously subscribes {@link Observer} to this {@link Signal} on the specified scheduler.
     * 
     * @param scheduler You specify which scheduler this operator will use.
     * @return {ChainableAPI}
     */
    public Signal<V> subscribeOn(Consumer<Runnable> scheduler) {
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
    public <R> Signal<R> switchMap(WiseFunction<V, Signal<R>> function) {
        Objects.requireNonNull(function);

        return new Signal<>((observer, disposer) -> {
            Disposable[] disposables = {null, Disposable.empty()};
            Subscriber end = countable(observer, 1);

            disposables[0] = to(value -> {
                end.index++;
                disposables[1].dispose();
                disposables[1] = function.apply(value).to(observer, end::error, I.NoOP, disposer.sub().add(end::complete), true);
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
    public <R> Signal<R> switchVariable(WiseFunction<V, Variable<R>> function) {
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
    public Signal<V> switchOff(Signal<Boolean> timing) {
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
    public Signal<V> switchOn(Signal<Boolean> timing) {
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
    public Signal<V> take(long count) {
        return take(AtomicLong::new, (context, value) -> context.incrementAndGet() < count, true, true, 0 < count);
    }

    /**
     * <p>
     * Alias for take(I.set(includes)).
     * </p>
     *
     * @param includes A collection of take items.
     * @return {ChainableAPI}
     */
    public Signal<V> take(V... includes) {
        return take(I.set(includes)::contains);
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition A function that evaluates the values emitted by the source {@link Signal},
     *            returning {@code true} if they pass the filter. {@code null} will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public Signal<V> take(Predicate<? super V> condition) {
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
     *            returning {@code true} if they pass the filter. {@code null} will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public Signal<V> take(V init, BiPredicate<? super V, ? super V> condition) {
        // ignore invalid parameters
        if (condition == null) {
            return this;
        }

        return take(((WiseFunction<V, AtomicReference<V>>) AtomicReference<V>::new)
                .bind(init), (context, value) -> condition.test(context.getAndSet(value), value));
    }

    /**
     * {@link #take(Predicate)} preassign context.
     * 
     * @param contextSupplier A {@link Supplier} of {@link Signal} specific context.
     * @param condition A condition function to apply to each value emitted by this {@link Signal} .
     *            {@code null} will ignore this instruction.
     * @return {ChainableAPI}
     */
    public <C> Signal<V> take(Supplier<C> contextSupplier, BiPredicate<C, ? super V> condition) {
        return take(contextSupplier, condition, true, false, false);
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition An external boolean {@link Signal}. {@code null} will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public Signal<V> take(Signal<Boolean> condition) {
        return take(condition, false, true);
    }

    /**
     * <p>
     * Returns an {@link Signal} consisting of the values of this {@link Signal} that match the
     * given predicate.
     * </p>
     *
     * @param condition An external boolean {@link Signal}. {@code null} will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    private Signal<V> take(Signal<Boolean> condition, boolean init, boolean expect) {
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
     * @param condition An index condition of values to emit.
     * @return {ChainableAPI}
     */
    public Signal<V> takeAt(LongPredicate condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }
        return take(AtomicLong::new, (context, value) -> condition.test(context.getAndIncrement()), true, false, false);
    }

    /**
     * <p>
     * Returns an {@link Signal} that emits items emitted by the source {@link Signal}, checks the
     * specified predicate for each item, and then completes if the condition is signaled.
     * </p>
     *
     * @param condition A function that evaluates an item emitted by the source {@link Signal} and
     *            returns a value.
     * @return An {@link Signal} that first emits items emitted by the source {@link Signal}, checks
     *         the specified condition after each item, and then completes if the condition is
     *         signaled.
     */
    public Signal<V> takeIf(Function<V, Signal<?>> condition) {
        return take(v -> condition.apply(v).isEmitted().to().v);
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
    public Signal<V> takeUntil(Predicate<? super V> condition) {
        return take(null, (context, value) -> condition.test(value), false, true, true);
    }

    /**
     * Returns {@link Signal} that emits the items emitted by the source {@link Signal} until a
     * second {@link Signal} emits an item.
     *
     * @param timing A {@link Signal} whose first emitted item will cause takeUntil to stop emitting
     *            items from the source {@link Signal}. {@code null} will ignore this. instruction.
     * @return A {@link Signal} that emits the items emitted by the source {@link Signal} until such
     *         time as other emits its first item.
     */
    public Signal<V> takeUntil(Signal timing) {
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
    public Signal<V> takeWhile(Predicate<? super V> condition) {
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
            // Normally, Signal will be disposed automatically after a COMPLETE event is sent. But
            // it
            // is not immediately disposed if the COMPLETE event itself is delayed for some
            // reason (#delay, #interval or #on, etc.).
            // In that case, even though the current signal has already stopped event propagation,
            // it is possible to propagate the subsequent event to the next signal.
            // There are several ways to avoid this.
            //
            // 1. Execute "dispose" immediately at the same time as the complete event
            // Although this way seems to work at first glance, problems will arise in #repeat or
            // #recover and so on because invoking "dispose" forcibly.
            //
            // 2. The sender transmits the event while sequentially checking Disposable#isDisposed.
            // The DISPOSE event itself is delayed and issued, so it can not be used.
            //
            // 3. After COMPLETE, stop all the events
            // Here we adopt this way.
            AtomicBoolean stopped = new AtomicBoolean();
            C context = contextSupplier == null ? null : contextSupplier.get();

            return to(value -> {
                if (!stopped.get()) {
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
                if (!stopped.get()) {
                    observer.complete();
                }
            }, disposer, false);
        });
    }

    /**
     * Returns a Signal that mirrors the source Signal but applies a timeout policy for each
     * emitted item. If the next item isn't emitted preassignin the specified timeout duration
     * starting from its predecessor, the resulting Signal terminates and notifies observers of a
     * {@link TimeoutException}.
     * 
     * @param time Time to take values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. {@code null} will ignore this
     *            instruction.
     * @param scheduler An event scheduler.
     * @return {ChainableAPI}
     */
    public Signal<V> timeout(long time, TimeUnit unit, ScheduledExecutorService... scheduler) {
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
            return effect(effect.bind(this)).effectOnTerminate(effect.bind(null)).to(observer, disposer);
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
     * @param unit A unit of time for the specified timeout. {@code null} will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public Signal<V> throttle(long time, TimeUnit unit) {
        return throttle(Variable.of(time), unit);
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
     * @param unit A unit of time for the specified timeout. {@code null} will ignore this
     *            instruction.
     * @return {ChainableAPI}
     */
    public Signal<V> throttle(Variable<Long> time, TimeUnit unit) {
        // ignore invalid parameters
        if (time == null || unit == null) {
            return this;
        }

        return take(AtomicLong::new, (context, value) -> {
            long now = System.nanoTime();

            if (context.get() + unit.toNanos(time.or(0L)) <= now) {
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
    public <E> Signal<E> toggle(E... values) {
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
     * Synchronization Support Tool : Wait in the current thread until this {@link Signal} to be
     * terminated. Termination is one of the states of completed, error or disposed.
     * 
     * @return
     */
    public Signal<V> waitForTerminate() {
        return new Signal<>((observer, disposer) -> {
            Variable<Throwable> error = Variable.empty();

            try {
                CountDownLatch latch = new CountDownLatch(1);
                to(observer, I.bundle(error, I.wiseC(latch::countDown)), I.bundle(latch::countDown, observer::complete), disposer
                        .add(latch::countDown), false);
                latch.await();
            } catch (Throwable e) {
                error.set(e);
            }
            error.to(observer::error);
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
    public <R> Signal<R> $(WiseFunction<V, Signal<R>> function) {
        return flatMap(function);
    }

    /**
     * Create countable completer.
     * 
     * @param delegator A complete action.
     * @param count A complete count.
     * @return {ChainableAPI}
     */
    private Subscriber countable(Observer delegator, int count) {
        Subscriber<?> completer = new Subscriber();
        completer.index = count;
        completer.error = e -> {
            completer.index = -1;
            delegator.error(e);
        };
        completer.complete = () -> {
            completer.index--;
            if (completer.index == 0) {
                delegator.complete();
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
     * @param completeOutput
     * @return {ChainableAPI}
     */
    private <T> Signal<T> signal(Predicate<? super V> emitCondition, T emitOutput, boolean acceptError, T errorOutput, boolean acceptComplete, T completeOutput) {
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
                if (acceptComplete && completeOutput != null) observer.accept(completeOutput);
                end.complete();
                sub.dispose();
            }, sub, false);
        });
    }
}