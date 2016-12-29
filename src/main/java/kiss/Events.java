/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
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
import java.util.List;
import java.util.Objects;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;

/**
 * @version 2016/12/02 22:19:08
 */
public class Events<V> {

    /**
     * For reuse.
     */
    public static final Events NEVER = new Events<>(observer -> Disposable.Φ);

    /**
     * For reuse.
     */
    private static final Object UNDEFINED = new Object();

    /**
     * The subscriber.
     */
    private final Function<Observer<? super V>, Disposable> subscriber;

    /**
     * <p>
     * Create {@link Events} with the specified subscriber {@link Collection} which will be invoked
     * whenever you calls {@link #to(Observer)} related methods.
     * </p>
     *
     * @param observers A subscriber {@link Function}.
     * @see #to(Observer)
     * @see #to(Consumer, Consumer)
     * @see #to(Consumer, Consumer, Runnable)
     */
    public Events(Collection<Observer<? super V>> observers) {
        this(observer -> {
            observers.add(observer);

            return () -> observers.remove(observer);
        });
    }

    /**
     * <p>
     * Create {@link Events} with the specified subscriber {@link Function} which will be invoked
     * whenever you calls {@link #to(Observer)} related methods.
     * </p>
     *
     * @param subscriber A subscriber {@link Function}.
     * @see #to(Observer)
     * @see #to(Consumer, Consumer)
     * @see #to(Consumer, Consumer, Runnable)
     */
    public Events(Function<Observer<? super V>, Disposable> subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * <p>
     * Receive values as {@link Variable} from this {@link Events}.
     * </p>
     *
     * @return A {@link Variable} as value receiver.
     */
    public final Disposable to(WritableValue<? super V> value) {
        return value == null ? Disposable.Φ : to(value::setValue);
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
        return to(v -> next.run(), null, null);
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
     * Receive values from this {@link Events}.
     * </p>
     *
     * @param next A delegator method of {@link Observer#accept(Object)}.
     * @param error A delegator method of {@link Observer#error(Throwable)}.
     * @param complete A delegator method of {@link Observer#complete()}.
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
     * Receive values from this {@link Events}.
     * </p>
     *
     * @param observer A value observer of this {@link Events}.
     * @return Calling {@link Disposable#dispose()} will dispose this subscription.
     */
    public final Disposable to(Observer<? super V> observer) {
        return subscriber.apply(observer);
    }

    /**
     * <p>
     * Receive values as {@link Variable} from this {@link Events}.
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
     * Receive values as {@link SetProperty} from this {@link Events}. Each value alternates between
     * In and Out.
     * </p>
     *
     * @return A {@link SetProperty} as value receiver.
     */
    public final SetProperty<V> toAlternate() {
        return toSet((set, value) -> {
            if (!set.add(value)) {
                set.remove(value);
            }
        });
    }

    /**
     * <p>
     * Receive values as {@link BooleanProperty} from this {@link Events}. Each value alternates
     * between true and false.
     * </p>
     *
     * @return A {@link BooleanProperty} as value receiver.
     */
    public final BooleanProperty toBinary() {
        // value receiver
        BooleanProperty property = new SimpleBooleanProperty();

        // start receiving values
        to(v -> property.set(!property.get()));

        // API definition
        return property;
    }

    /**
     * <p>
     * Receive values as {@link SetProperty} from this {@link Events}.
     * </p>
     *
     * @return A {@link SetProperty} as value receiver.
     */
    public final ListProperty<V> toList() {
        // value receiver
        ListProperty<V> property = I.make(ListProperty.class);

        // start receiving values
        to(property::add);

        // API definition
        return property;
    }

    /**
     * <p>
     * Receive values as {@link MapProperty} from this {@link Events}.
     * </p>
     *
     * @return A {@link MapProperty} as value receiver.
     */
    public final <Key> MapProperty<Key, V> toMap(Function<V, Key> keyGenerator) {
        return toMap(keyGenerator, Function.identity());
    }

    /**
     * <p>
     * Receive values as {@link MapProperty} from this {@link Events}.
     * </p>
     *
     * @return A {@link MapProperty} as value receiver.
     */
    public final <Key, Value> MapProperty<Key, Value> toMap(Function<V, Key> keyGenerator, Function<V, Value> valueGenerator) {
        // value receiver
        MapProperty<Key, Value> property = I.make(MapProperty.class);

        // start receiving values
        to(v -> property.put(keyGenerator.apply(v), valueGenerator.apply(v)));

        // API definition
        return property;
    }

    /**
     * <p>
     * Receive values as {@link SetProperty} from this {@link Events}.
     * </p>
     *
     * @return A {@link SetProperty} as value receiver.
     */
    public final SetProperty<V> toSet() {
        return toSet(SetProperty::add);
    }

    /**
     * <p>
     * Receive values as {@link SetProperty} from this {@link Events}.
     * </p>
     *
     * @return A {@link SetProperty} as value receiver.
     */
    private SetProperty<V> toSet(BiConsumer<SetProperty<V>, V> collector) {
        // value receiver
        SetProperty<V> property = I.make(SetProperty.class);

        // start receiving values
        to(v -> collector.accept(property, v));

        // API definition
        return property;
    }

    /**
     * <p>
     * Receive values as {@link Property} from this {@link Events}.
     * </p>
     *
     * @return A {@link Property} as value receiver.
     */
    public final Property<V> toProperty() {
        SimpleObjectProperty<V> property = new SimpleObjectProperty();
        to(property::set);
        return property;
    }

    /**
     * <p>
     * Receive values as {@link MapProperty} from this {@link Events}.
     * </p>
     *
     * @return A {@link MapProperty} as value receiver.
     */
    public final <Key> Table<Key, V> toTable(Function<V, Key> keyGenerator) {
        return toTable(new Table(), keyGenerator);
    }

    /**
     * <p>
     * Receive values as {@link MapProperty} from this {@link Events}.
     * </p>
     *
     * @return A {@link MapProperty} as value receiver.
     */
    public final <Key> Table<Key, V> toTable(Table<Key, V> table, Function<V, Key> keyGenerator) {
        return toTable(table, keyGenerator, Function.identity());
    }

    /**
     * <p>
     * Receive values as {@link MapProperty} from this {@link Events}.
     * </p>
     *
     * @return A {@link MapProperty} as value receiver.
     */
    public final <Key, Value> Table<Key, Value> toTable(Function<V, Key> keyGenerator, Function<V, Value> valueGenerator) {
        return toTable(new Table(), keyGenerator, valueGenerator);
    }

    /**
     * <p>
     * Receive values as {@link MapProperty} from this {@link Events}.
     * </p>
     *
     * @return A {@link MapProperty} as value receiver.
     */
    public final <Key, Value> Table<Key, Value> toTable(Table<Key, Value> table, Function<V, Key> keyGenerator, Function<V, Value> valueGenerator) {
        // start receiving values
        to(v -> table.push(keyGenerator.apply(v), valueGenerator.apply(v)));

        // API definition
        return table;
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
        return (Events<R>) take(type::isInstance);
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
    public final Events<List<V>> buffer(int size) {
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
    public final Events<List<V>> buffer(int size, int interval) {
        int creationSize = 0 < size ? size : 1;
        int creationInterval = 0 < interval ? interval : 1;

        return new Events<>(observer -> {
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
            });
        });
    }

    /**
     * <p>
     * Indicates each values of an {@link Events} sequence into zero or more buffers which are
     * produced based on time count information.
     * </p>
     *
     * @param time Time to collect values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<List<V>> buffer(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return NEVER;
        }

        return new Events<>(observer -> {
            AtomicReference<List<V>> ref = new AtomicReference<>();

            return to(value -> ref.updateAndGet(buffer -> {
                if (buffer == null) {
                    buffer = new ArrayList<>();

                    I.schedule(time, unit, true, () -> observer.accept(ref.getAndSet(null)));
                }
                return buffer;
            }).add(value));
        });
    }

    /**
     * <p>
     * Returns an {@link Events} that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by this {@link Events} and the other
     * specified {@link Events}.
     * </p>
     *
     * @param other An other {@link Events} to combine.
     * @return A {@link Events} that emits items that are the result of combining the items emitted
     *         by source {@link Events} by means of the given aggregation function.
     */
    public final <O> Events<Ⅱ<V, O>> combine(Events<O> other) {
        return combine(other, I::pair);
    }

    /**
     * <p>
     * Returns an {@link Events} that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by this {@link Events} and the other
     * specified {@link Events}.
     * </p>
     *
     * @param other An other {@link Events} to combine.
     * @param another An another {@link Events} to combine.
     * @return A {@link Events} that emits items that are the result of combining the items emitted
     *         by source {@link Events} by means of the given aggregation function.
     */
    public final <O, A> Events<Ⅲ<V, O, A>> combine(Events<O> other, Events<A> another) {
        return combine(other, I::<V, O>pair).combine(another, Ⅱ<V, O>::<A>append);
    }

    /**
     * <p>
     * Returns an {@link Events} that emits the results of a function of your choosing applied to
     * combinations of two items emitted, in sequence, by this {@link Events} and the other
     * specified {@link Events}.
     * </p>
     *
     * @param other An other {@link Events} to combine.
     * @param function A function that, when applied to an item emitted by each of the source
     *            {@link Events}, results in an item that will be emitted by the resulting
     *            {@link Events}.
     * @return A {@link Events} that emits items that are the result of combining the items emitted
     *         by source {@link Events} by means of the given aggregation function.
     */
    public final <O, R> Events<R> combine(Events<O> other, BiFunction<V, O, R> function) {
        return new Events<>(observer -> {
            ArrayDeque<V> baseValue = new ArrayDeque();
            ArrayDeque<O> otherValue = new ArrayDeque();

            return to(value -> {
                if (otherValue.isEmpty()) {
                    baseValue.add(value);
                } else {
                    observer.accept(function.apply(value, otherValue.pollFirst()));
                }
            }).and(other.to(value -> {
                if (baseValue.isEmpty()) {
                    otherValue.add(value);
                } else {
                    observer.accept(function.apply(baseValue.pollFirst(), value));
                }
            }));
        });
    }

    /**
     * <p>
     * Returns an {@link Events} that emits the results of a function of your choosing applied to
     * combinations of several items emitted, in sequence, by this {@link Events} and the other
     * specified {@link Events}.
     * </p>
     *
     * @param others Other {@link Events} to combine.
     * @param operator A function that, when applied to an item emitted by each of the source
     *            {@link Events}, results in an item that will be emitted by the resulting
     *            {@link Events}.
     * @return A {@link Events} that emits items that are the result of combining the items emitted
     *         by source {@link Events} by means of the given aggregation function.
     */
    public final Events<V> combine(Events<V>[] others, BinaryOperator<V> operator) {
        Events<V> base = this;

        if (others != null) {
            for (Events<V> other : others) {
                base = base.combine(other, operator);
            }
        }
        return base;
    }

    /**
     * <p>
     * Combines two source {@link Events} by emitting an item that aggregates the latest values of
     * each of the source {@link Events} each time an item is received from either of the source
     * {@link Events}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param other An other constant {@link Events} to combine.
     * @return An {@link Events} that emits items that are the result of combining the items emitted
     *         by the source {@link Events} by means of the given aggregation function
     */
    public final <O> Events<Ⅱ<V, O>> combineLatest(O other) {
        return combineLatest(from(other));
    }

    /**
     * <p>
     * Combines two source {@link Events} by emitting an item that aggregates the latest values of
     * each of the source {@link Events} each time an item is received from either of the source
     * {@link Events}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param other An other {@link Events} to combine.
     * @return An {@link Events} that emits items that are the result of combining the items emitted
     *         by the source {@link Events} by means of the given aggregation function
     */
    public final <O> Events<Ⅱ<V, O>> combineLatest(Events<O> other) {
        return combineLatest(other, I::pair);
    }

    /**
     * <p>
     * Combines two source {@link Events} by emitting an item that aggregates the latest values of
     * each of the source {@link Events} each time an item is received from either of the source
     * {@link Events}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param other An other {@link Events} to combine.
     * @param another An another {@link Events} to combine.
     * @return An {@link Events} that emits items that are the result of combining the items emitted
     *         by the source {@link Events} by means of the given aggregation function
     */
    public final <O, A> Events<Ⅲ<V, O, A>> combineLatest(Events<O> other, Events<A> another) {
        return combineLatest(other, I::<V, O>pair).combineLatest(another, Ⅱ<V, O>::<A>append);
    }

    /**
     * <p>
     * Combines two source {@link Events} by emitting an item that aggregates the latest values of
     * each of the source {@link Events} each time an item is received from either of the source
     * {@link Events}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param other An other {@link Events} to combine.
     * @param function An aggregation function used to combine the items emitted by the source
     *            {@link Events}.
     * @return An {@link Events} that emits items that are the result of combining the items emitted
     *         by the source {@link Events} by means of the given aggregation function
     */
    public final <O, R> Events<R> combineLatest(Events<O> other, BiFunction<V, O, R> function) {
        return new Events<>(observer -> {
            AtomicReference<V> baseValue = new AtomicReference(UNDEFINED);
            AtomicReference<O> otherValue = new AtomicReference(UNDEFINED);

            return to(value -> {
                baseValue.set(value);
                O joined = otherValue.get();

                if (joined != UNDEFINED) {
                    observer.accept(function.apply(value, joined));
                }
            }).and(other.to(value -> {
                otherValue.set(value);

                V joined = baseValue.get();

                if (joined != UNDEFINED) {
                    observer.accept(function.apply(joined, value));
                }
            }));
        });
    }

    /**
     * <p>
     * Combines several source {@link Events} by emitting an item that aggregates the latest values
     * of each of the source {@link Events} each time an item is received from either of the source
     * {@link Events}, where this aggregation is defined by a specified function.
     * </p>
     *
     * @param others Other {@link Events} to combine.
     * @param operator An aggregation function used to combine the items emitted by the source
     *            {@link Events}.
     * @return An {@link Events} that emits items that are the result of combining the items emitted
     *         by the source {@link Events} by means of the given aggregation function
     */
    public final Events<V> combineLatest(Events<V>[] others, BinaryOperator<V> operator) {
        Events<V> base = this;

        if (others != null) {
            for (Events<V> other : others) {
                base = base.combineLatest(other, operator);
            }
        }
        return base;
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
                observer.accept(value);
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

        return on((observer, value) -> I.schedule(time, unit, false, () -> observer.accept(value)));
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
        return take(null, ((BiPredicate) Objects::equals).negate());
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
            HashSet set = new HashSet();

            return to(value -> {
                if (set.add(value)) {
                    observer.accept(value);
                }
            });
        });
    }

    /**
     * <p>
     * Returns an {@link Events} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Events}, where that function returns an {@link Events}
     * , and then merging those resulting {@link Events} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Events}
     *            , returns an {@link Events}.
     * @return An {@link Events} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Events} and merging the results of the
     *         {@link Events} obtained from this transformation.
     */
    public final <R> Events<R> flatArray(Function<V, R[]> function) {
        return flatMap(function.andThen(Events::from));
    }

    /**
     * <p>
     * Returns an {@link Events} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Events}, where that function returns an {@link Events}
     * , and then merging those resulting {@link Events} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Events}
     *            , returns an {@link Events}.
     * @return An {@link Events} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Events} and merging the results of the
     *         {@link Events} obtained from this transformation.
     */
    public final <R> Events<R> flatIterable(Function<V, Iterable<R>> function) {
        return flatMap(function.andThen(Events::from));
    }

    /**
     * <p>
     * Returns an {@link Events} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Events}, where that function returns an {@link Events}
     * , and then merging those resulting {@link Events} and emitting the results of this merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Events}
     *            , returns an {@link Events}.
     * @return An {@link Events} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Events} and merging the results of the
     *         {@link Events} obtained from this transformation.
     */
    public final <R> Events<R> flatMap(Function<V, Events<R>> function) {
        return new Events<>(observer -> {
            Disposable disposer = Disposable.empty();
            disposer.and(to(value -> disposer.and(function.apply(value).to(observer))));

            return disposer;
        });
    }

    /**
     * <p>
     * Ensure the minimum interval time of each {@link Events} items.
     * </p>
     *
     * @param time The absolute time used to interval the {@link Events} sequence. Zero or negative
     *            number will ignore this instruction.
     * @param unit A unit of time for the specified time. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> interval(long time, TimeUnit unit) {
        return interval(time, unit, this);
    }

    /**
     * <p>
     * Ensure the minimum interval time of each {@link Events} items. The specified queue is shared
     * by multiple {@link Events}.
     * </p>
     *
     * @param time The absolute time used to interval the {@link Events} sequence. Zero or negative
     *            number will ignore this instruction.
     * @param unit A unit of time for the specified time. <code>null</code> will ignore this
     *            instruction.
     * @param group A shared item group.
     * @return Chainable API.
     */
    public final Events<V> interval(long time, TimeUnit unit, Object group) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return NEVER;
        }

        Deque<Ⅱ<V, Observer>> buffer = I.associate(group, ArrayDeque.class);

        return new Events<>(observer -> to(value -> {
            if (buffer.isEmpty()) {
                I.schedule(() -> interval(unit.toMillis(time), 0, buffer));
            }
            buffer.addLast(I.pair(value, observer));
        }));
    }

    /**
     * <p>
     * Ensure the minimum interval time of each {@link Events} items. The specified queue is shared
     * by multiple {@link Events}.
     * </p>
     *
     * @param interval A specified interval time.
     * @param latest A latest execution date.
     * @param buffer The remaining values.
     */
    private void interval(long interval, long latest, Deque<Ⅱ<V, Observer>> buffer) {
        Ⅱ<V, Observer> context = buffer.pollFirst();
        context.ⅱ.accept(context.ⅰ);

        if (!buffer.isEmpty()) {
            long now = System.nanoTime();
            I.schedule(Math.min(interval, now - latest), TimeUnit.NANOSECONDS, true, () -> interval(interval, now, buffer));
        }
    }

    /**
     * <p>
     * Returns an {@link Events} that applies the given {@link Predicate} function to each value
     * emitted by an {@link Events} and emits the result.
     * </p>
     *
     * @param converter A converter function to apply to each value emitted by this {@link Events} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final Events<Boolean> is(Predicate<? super V> converter) {
        // ignore invalid parameters
        if (converter == null) {
            return NEVER;
        }
        return map(converter::test);
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
        return new Events<>(observer -> to(value -> observer.accept(converter.apply(value))));
    }

    /**
     * <p>
     * Returns an {@link Events} that applies the given function to each value emitted by an
     * {@link Events} and emits the result.
     * </p>
     *
     * @param init A initial value.
     * @param converter A converter function to apply to each value emitted by this {@link Events} .
     *            <code>null</code> will ignore this instruction.
     * @return Chainable API.
     */
    public final <R> Events<R> map(V init, BiFunction<V, V, R> converter) {
        // ignore invalid parameters
        if (converter == null) {
            return (Events<R>) this;
        }

        return new Events<>(observer -> {
            AtomicReference<V> ref = new AtomicReference(init);

            return to(value -> observer.accept(converter.apply(ref.getAndSet(value), value)));
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
    public final <R> Events<R> mapTo(R constant) {
        return map(v -> constant);
    }

    /**
     * <p>
     * Flattens a sequence of {@link Events} emitted by an {@link Events} into one {@link Events},
     * without any transformation.
     * </p>
     *
     * @param others A target {@link Events} to merge. <code>null</code> will be ignored.
     * @return Chainable API.
     */
    @SafeVarargs
    public final Events<V> merge(Events<? extends V>... others) {
        return merge(Arrays.asList(others));
    }

    /**
     * <p>
     * Flattens a sequence of {@link Events} emitted by an {@link Events} into one {@link Events},
     * without any transformation.
     * </p>
     *
     * @param others A target {@link Events} set to merge. <code>null</code> will be ignored.
     * @return Chainable API.
     */
    public final Events<V> merge(Iterable<? extends Events<? extends V>> others) {
        // ignore invalid parameters
        if (others == null) {
            return this;
        }

        return new Events<>(observer -> {
            Disposable disposable = to(observer);

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
    private Events<V> on(BiConsumer<Observer<? super V>, V> next) {
        // ignore invalid parameters
        if (next == null) {
            return this;
        }

        return new Events<>(observer -> {
            Agent<V> agent = new Agent();
            agent.observer = observer;
            agent.next = value -> next.accept(observer, value);

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
                observer.complete();
                agent.and(to(agent));
            };
            return agent.and(to(agent));
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

        return new Events<>(observer -> {
            AtomicInteger counter = new AtomicInteger(count);
            Agent agent = new Agent();
            agent.observer = observer;
            agent.complete = () -> {
                if (counter.decrementAndGet() == 0) {
                    observer.complete();
                } else {
                    observer.complete();
                    agent.and(to(agent));
                }
            };

            return agent.and(to(agent));
        });
    }

    /**
     * <p>
     * Returns an {@link Events} that, when the specified sampler {@link Events} emits an item,
     * emits the most recently emitted item (if any) emitted by the source {@link Events} since the
     * previous emission from the sampler {@link Events}.
     * </p>
     *
     * @param sampler An {@link Events} to use for sampling the source {@link Events}.
     * @return Chainable API.
     */
    public final Events<V> sample(Events sampler) {
        // ignore invalid parameters
        if (sampler == null) {
            return NEVER;
        }

        return new Events<>(observer -> {
            AtomicReference<V> latest = new AtomicReference(UNDEFINED);

            return to(latest::set).and(sampler.to(sample -> {
                V value = latest.getAndSet((V) UNDEFINED);

                if (value != UNDEFINED) {
                    observer.accept(value);
                }
            }));
        });
    }

    /**
     * <p>
     * Returns an {@link Events} that applies a function of your choosing to the first item emitted
     * by a source {@link Events} and a seed value, then feeds the result of that function along
     * with the second item emitted by the source {@link Events} into the same function, and so on
     * until all items have been emitted by the source {@link Events}, emitting the result of each
     * of these iterations.
     * </p>
     *
     * @param init An initial (seed) accumulator item.
     * @param function An accumulator function to be invoked on each item emitted by the source
     *            {@link Events}, whose result will be emitted to {@link Events} via
     *            {@link Observer#accept(Object)} and used in the next accumulator call.
     * @return An {@link Events} that emits initial value followed by the results of each call to
     *         the accumulator function.
     */
    public final <R> Events<R> scan(R init, BiFunction<R, V, R> function) {
        return new Events<>(observer -> {
            AtomicReference<R> ref = new AtomicReference(init);

            return to(value -> {
                ref.set(function.apply(ref.get(), value));
                observer.accept(ref.get());
            });
        });
    }

    /**
     * <p>
     * Invokes an action for each value in the {@link Events} sequence.
     * </p>
     *
     * @param effect An action to invoke for each value in the {@link Events} sequence.
     * @return Chainable API.
     */
    public final Events<V> sideEffect(Consumer<? super V> effect) {
        return new Events<>(observer -> to(v -> {
            effect.accept(v);
            observer.accept(v);
        }));
    }

    /**
     * <p>
     * Alias for take(condition.negate()).
     * </p>
     *
     * @param condition A skip condition.
     * @return Chainable API.
     */
    public final Events<V> skip(Predicate<V> condition) {
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
    public final Events<V> skip(V... excludes) {
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
    public final Events<V> skip(Collection<V> excludes) {
        // ignore invalid parameter
        if (excludes == null) {
            return this;
        }
        return skip(v -> excludes.contains(v));
    }

    /**
     * <p>
     * Alias for take(init, condition.negate()).
     * </p>
     *
     * @param condition A skip condition.
     * @return Chainable API.
     */
    public final Events<V> skip(V init, BiPredicate<V, V> condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }
        return take(init, condition.negate());
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
            AtomicInteger counter = new AtomicInteger();

            return to(value -> {
                if (count < counter.incrementAndGet()) {
                    observer.accept(value);
                }
            });
        });
    }

    /**
     * <p>
     * Bypasses a specified duration in an {@link Events} sequence and then returns the remaining
     * values.
     * </p>
     *
     * @param time Time to skip values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> skip(long time, TimeUnit unit) {
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        return new Events<>(observer -> {
            long timing = System.nanoTime() + unit.toNanos(time);

            return to(value -> {
                if (timing < System.nanoTime()) {
                    observer.accept(value);
                }
            });
        });
    }

    /**
     * <p>
     * Alias for skip(Objects::isNull).
     * </p>
     *
     * @return Chainable API.
     */
    public final Events<V> skipNull() {
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
     * @param value A value to test each item emitted from the source {@link Events}.
     * @return An {@link Events} that begins emitting items emitted by the source {@link Events}
     *         when the specified value is coming.
     */
    public final Events<V> skipUntil(V value) {
        return skipUntil(v -> Objects.equals(v, value));
    }

    /**
     * <p>
     * Returns an {@link Events} that skips all items emitted by the source {@link Events} as long
     * as a specified condition holds true, but emits all further source items as soon as the
     * condition becomes false.
     * </p>
     *
     * @param predicate A function to test each item emitted from the source {@link Events}.
     * @return An {@link Events} that begins emitting items emitted by the source {@link Events}
     *         when the specified predicate becomes false.
     */
    public final Events<V> skipUntil(Predicate<? super V> predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Events<>(observer -> {
            AtomicBoolean flag = new AtomicBoolean();

            return to(value -> {
                if (flag.get()) {
                    observer.accept(value);
                } else if (predicate.test(value)) {
                    flag.set(true);
                    observer.accept(value);
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
     * @param timing The second {@link Events} that has to emit an item before the source
     *            {@link Events} elements begin to be mirrored by the resulting {@link Events}.
     * @return An {@link Events} that skips items from the source {@link Events} until the second
     *         {@link Events} emits an item, then emits the remaining items.
     */
    public final Events<V> skipUntil(Events timing) {
        // ignore invalid parameter
        if (timing == null) {
            return this;
        }

        return new Events<>(observer -> {
            Agent agent = new Agent();
            agent.and(timing.to(value -> agent.dispose()));

            return to(value -> {
                if (agent.disposables == null) {
                    observer.accept(value);
                }
            }).and(agent);
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
    public final Events<V> skipWhile(Events<Boolean> condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }
        return takeWhile(condition.startWith(false).map(value -> !value));
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Events}.
     * </p>
     *
     * @param value The initial value.
     * @return Chainable API.
     */
    public final Events<V> startWith(V value) {
        return startWith(Arrays.asList(value));
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Events}.
     * </p>
     *
     * @param values The initial values.
     * @return Chainable API.
     */
    @SafeVarargs
    public final Events<V> startWith(V... values) {
        return startWith(Arrays.asList(values));
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Events}.
     * </p>
     *
     * @param values The initial values.
     * @return Chainable API.
     */
    public final Events<V> startWith(Enumeration<V> values) {
        return startWith(Collections.list(values));
    }

    /**
     * <p>
     * Emit a specified sequence of items before beginning to emit the items from the source
     * {@link Events}.
     * </p>
     *
     * @param values The initial values.
     * @return Chainable API.
     */
    public final Events<V> startWith(Iterable<V> values) {
        // ignore invalid parameter
        if (values == null) {
            return this;
        }

        return new Events<>(observer -> {
            for (V value : values) {
                observer.accept(value);
            }
            return to(observer);
        });
    }

    /**
     * <p>
     * Returns an {@link Events} that emits items based on applying a function that you supply to
     * each item emitted by the source {@link Events}, where that function returns an {@link Events}
     * , and then merging the latest resulting {@link Events} and emitting the results of this
     * merger.
     * </p>
     *
     * @param function A function that, when applied to an item emitted by the source {@link Events}
     *            , returns an {@link Events}.
     * @return An {@link Events} that emits the result of applying the transformation function to
     *         each item emitted by the source {@link Events} and merging the results of the
     *         {@link Events} obtained from this transformation.
     */
    public final <R> Events<R> switchMap(Function<V, Events<R>> function) {
        return new Events<>(observer -> {
            Disposable[] disposables = {null, Disposable.Φ};

            disposables[0] = to(value -> {
                disposables[1].dispose();
                disposables[1] = function.apply(value).to(observer);
            });
            return () -> {
                disposables[0].dispose();
                disposables[1].dispose();
            };
        });
    }

    /**
     * <p>
     * Returns an {@link Events} consisting of the values of this {@link Events} that match the
     * given predicate.
     * </p>
     *
     * @param condition A function that evaluates the values emitted by the source {@link Events},
     *            returning {@code true} if they pass the filter. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> take(Predicate<V> condition) {
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
     * Returns an {@link Events} consisting of the values of this {@link Events} that match the
     * given predicate.
     * </p>
     *
     * @param condition A function that evaluates the values emitted by the source {@link Events},
     *            returning {@code true} if they pass the filter. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> take(V init, BiPredicate<V, V> condition) {
        // ignore invalid parameters
        if (condition == null) {
            return this;
        }

        return new Events<>(observer -> {
            AtomicReference<V> ref = new AtomicReference(init);

            return to(value -> {
                if (condition.test(ref.getAndSet(value), value)) {
                    observer.accept(value);
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
            AtomicInteger counter = new AtomicInteger(count);

            Disposable disposer = Disposable.empty();

            return disposer.and(to(value -> {
                int current = counter.decrementAndGet();

                if (0 <= current) {
                    observer.accept(value);

                    if (0 == current) {
                        observer.complete();
                        disposer.dispose();
                    }
                }
            }));
        });
    }

    /**
     * <p>
     * Returns an {@link Events} sequence while the specified duration.
     * </p>
     *
     * @param time Time to take values. Zero or negative number will ignore this instruction.
     * @param unit A unit of time for the specified timeout. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> take(long time, TimeUnit unit) {
        // ignore invalid parameter
        // ignore invalid parameters
        if (time <= 0 || unit == null) {
            return this;
        }

        return new Events<>(observer -> {
            long timing = System.nanoTime() + unit.toNanos(time);
            Disposable disposer = Disposable.empty();

            return disposer.and(to(value -> {
                if (System.nanoTime() < timing) {
                    observer.accept(value);
                } else {
                    observer.complete();
                    disposer.dispose();
                }
            }));
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
     * @param value A value to test each item emitted from the source {@link Events}.
     * @return An {@link Events} that first emits items emitted by the source {@link Events}, checks
     *         the specified condition after each item, and then completes if the condition is
     *         satisfied.
     */
    public final Events<V> takeUntil(V value) {
        return takeUntil(v -> Objects.equals(v, value));
    }

    /**
     * <p>
     * Returns an {@link Events} that emits items emitted by the source {@link Events}, checks the
     * specified predicate for each item, and then completes if the condition is satisfied.
     * </p>
     *
     * @param predicate A function that evaluates an item emitted by the source {@link Events} and
     *            returns a Boolean.
     * @return An {@link Events} that first emits items emitted by the source {@link Events}, checks
     *         the specified condition after each item, and then completes if the condition is
     *         satisfied.
     */
    public final Events<V> takeUntil(Predicate<V> predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Events<>(observer -> {
            Disposable disposer = Disposable.empty();

            return disposer.and(to(value -> {
                if (predicate.test(value)) {
                    observer.accept(value);
                    observer.complete();
                    disposer.dispose();
                } else {
                    observer.accept(value);
                }
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
    public final Events<V> takeUntil(Events predicate) {
        // ignore invalid parameter
        if (predicate == null) {
            return this;
        }

        return new Events<>(observer -> {
            Disposable disposer = Disposable.empty();

            return disposer.and(to(observer).and(predicate.to(value -> {
                observer.complete();
                disposer.dispose();
            })));
        });
    }

    /**
     * <p>
     * Returns an {@link Events} consisting of the values of this {@link Events} that match the
     * given predicate.
     * </p>
     *
     * @param condition An external boolean {@link Events}. <code>null</code> will ignore this
     *            instruction.
     * @return Chainable API.
     */
    public final Events<V> takeWhile(Events<Boolean> condition) {
        // ignore invalid parameter
        if (condition == null) {
            return this;
        }

        return new Events<>(observer -> {
            AtomicBoolean flag = new AtomicBoolean();

            return condition.to(flag::set).and(to(v -> {
                if (flag.get()) {
                    observer.accept(v);
                }
            }));
        });
    }

    /**
     * <p>
     * Returns an {@link Events} that applies the boolean values alternately to each item emitted by
     * an {@link Events} and emits the result. Initial value is true.
     * </p>
     *
     * @return Chainable API.
     */
    public final Events<Boolean> toggle() {
        return toggle(true);
    }

    /**
     * <p>
     * Returns an {@link Events} that applies the boolean values alternately to each item emitted by
     * an {@link Events} and emits the result.
     * </p>
     *
     * @param initial A initial boolean value to apply to each value emitted by this {@link Events}.
     * @return Chainable API.
     */
    public final Events<Boolean> toggle(boolean initial) {
        return toggle(initial, !initial);
    }

    /**
     * <p>
     * Returns an {@link Events} that applies the given two constants alternately to each item
     * emitted by an {@link Events} and emits the result.
     * </p>
     *
     * @param values A list of constants to apply to each value emitted by this {@link Events}.
     * @return Chainable API.
     */
    @SafeVarargs
    public final <E> Events<E> toggle(E... values) {
        if (values.length == 0) {
            return NEVER;
        }

        return new Events<>(observer -> {
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
        if (observables == null || observables.length == 0) {
            return NEVER;
        }
        return from(Boolean.TRUE).combineLatest(observables, (base, other) -> base && other);
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
        if (observables == null || observables.length == 0) {
            return NEVER;
        }
        return from(Boolean.FALSE).combineLatest(observables, (base, other) -> base || other);
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
        if (observables == null || observables.length == 0) {
            return NEVER;
        }
        return from(Boolean.TRUE).combineLatest(observables, (base, other) -> !base && !other);
    }

    /**
     * <p>
     * Returns an {@link Events} that emits the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return An {@link Events} that emits values as a first sequence.
     */
    @SafeVarargs
    public static <V> Events<V> from(V... values) {
        return NEVER.startWith(values);
    }

    /**
     * <p>
     * Returns an {@link Events} that emits the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return An {@link Events} that emits values as a first sequence.
     */
    public static <V> Events<V> from(Iterable<V> values) {
        return NEVER.startWith(values);
    }

    /**
     * <p>
     * Returns an {@link Events} that emits the specified values.
     * </p>
     *
     * @param values A list of values to emit.
     * @return An {@link Events} that emits values as a first sequence.
     */
    public static <V> Events<V> from(Enumeration<V> values) {
        return NEVER.startWith(values);
    }

    /**
     * @param value A initial value.
     * @param time A time to interval.
     * @param unit A time unit.
     * @param <V> Value type.
     * @return An {@link Events} that emits values as a first sequence.
     */
    public static <V> Events<V> infinite(V value, long time, TimeUnit unit) {
        return new Events<>(observer -> {
            Future schedule = I.schedule(() -> observer.accept(value), time, unit);

            return () -> schedule.cancel(true);
        });
    }
}
