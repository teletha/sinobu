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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @version 2018/11/11 10:21:07
 */
public class Variable<V> implements Consumer<V>, Supplier<V> {

    /** The modifier base. */
    private static final MethodHandle base;

    static {
        try {
            Field modify = Variable.class.getField("v");
            modify.setAccessible(true);

            base = MethodHandles.lookup().unreflectSetter(modify);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The current value. This value is not final but read-only. */
    public transient final V v;

    /** The binded modifier. */
    private final MethodHandle set;

    /** The immutability. */
    private boolean fix;

    /** The observers. */
    private volatile List<Observer> observers;

    /** The adjuster. */
    private volatile Function<V, V> adjuster;

    /**
     * Hide constructor.
     */
    private Variable(V value) {
        this.v = value;
        this.set = base.bindTo(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(V value) {
        set(value);
    }

    /**
     * <p>
     * Compute the current value. If it is <code>null</code>, this method returns the specified
     * default value.
     * </p>
     *
     * @param value The default value.
     * @return The current value or the specified default value.
     */
    @Override
    public V get() {
        return v;
    }

    /**
     * <p>
     * Test whether the current value is equal to the specified value or not.
     * </p>
     * 
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public final boolean is(V value) {
        return Objects.equals(v, value);
    }

    /**
     * <p>
     * Test whether the current value fulfills the specified condition or not.
     * </p>
     * 
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public final boolean is(Predicate<V> condition) {
        return condition == null ? false : condition.test(v);
    }

    /**
     * <p>
     * Test whether the value fulfills the specified condition or not. This method is shortcut for
     * </p>
     * <pre>
     * variable.observeNow().is(condition);
     * </pre>
     * 
     * @param value A value to check the equality.
     * @return A result {@link Signal}.
     */
    public final Signal<Boolean> iŝ(Predicate<V> condition) {
        return observeNow().is(condition);
    }

    /**
     * <p>
     * Test whether the current value is NOT equal to the specified value or not.
     * </p>
     * 
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public final boolean isNot(V value) {
        return !is(value);
    }

    /**
     * <p>
     * Test whether the current value does NOT fulfill the specified condition or not.
     * </p>
     * 
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public final boolean isNot(Predicate<V> condition) {
        return !is(condition);
    }

    /**
     * <p>
     * Test whether the value fulfills the specified condition or not. This method is shortcut for
     * </p>
     * <pre>
     * variable.observeNow().isNot(condition);
     * </pre>
     * 
     * @param value A value to check the equality.
     * @return A result {@link Signal}.
     */
    public final Signal<Boolean> iŝNot(Predicate<V> condition) {
        return observeNow().isNot(condition);
    }

    /**
     * Check whether the value is absent or not.
     *
     * @return A result.
     */
    public final boolean isAbsent() {
        return is(Objects::isNull);
    }

    /**
     * Check whether the value is present or not.
     *
     * @return A result.
     */
    public final boolean isPresent() {
        return is(Objects::nonNull);
    }

    /**
     * Chech whether the value is fiexed or not.
     * 
     * @return A result.
     */
    public final boolean isFixed() {
        return fix;
    }

    /**
     * <p>
     * Perform the specified action if the value is present.
     * </p>
     *
     * @param then An action to perform.
     * @return The computed {@link Variable}.
     */
    public <R> Variable<R> map(Function<? super V, ? extends R> then) {
        if (v != null && then != null) {
            try {
                return of(then.apply(v));
            } catch (Throwable e) {
                // ignore
            }
        }
        return empty();
    }

    /**
     * <p>
     * Perform the specified action if the value is present.
     * </p>
     *
     * @param action An action to perform.
     * @return The computed {@link Variable}.
     */
    public <R> Variable<R> flatMap(Function<V, Variable<R>> converter) {
        return v == null || converter == null ? new Variable(null) : converter.apply(v);
    }

    /**
     * <p>
     * Observe this {@link Variable}.
     * </p>
     *
     * @return
     */
    public Signal<V> observe() {
        return new Signal<>((observer, disposer) -> {
            if (observers == null) {
                observers = new CopyOnWriteArrayList();
            }
            observers.add(observer);

            return disposer.add(() -> {
                observers.remove(observer);

                if (observers.isEmpty()) {
                    observers = null;
                }
            });
        });
    }

    /**
     * <p>
     * Observe this {@link Variable} with the current value.
     * </p>
     *
     * @return
     */
    public Signal<V> observeNow() {
        return observe().startWith(this::get);
    }

    /**
     * <p>
     * If the value is present, return this {@link Variable}. If the value is absent, return other
     * {@link Variable}.
     * </p>
     *
     * @param other An other value.
     * @return A {@link Variable}.
     */
    public Variable<V> or(V other) {
        return v != null ? this : of(other);
    }

    /**
     * <p>
     * If the value is present, return this {@link Variable}. If the value is absent, return other
     * {@link Variable}.
     * </p>
     *
     * @param other An other value.
     * @return A {@link Variable}.
     */
    public Variable<V> or(Optional<V> other) {
        return v != null ? this : of(other);
    }

    /**
     * <p>
     * If the value is present, return this {@link Variable}. If the value is absent, return other
     * {@link Variable}.
     * </p>
     *
     * @param other An other value.
     * @return A {@link Variable}.
     */
    public Variable<V> or(Supplier<V> other) {
        return v != null ? this : of(other);
    }

    /**
     * <p>
     * Assign the new value.
     * </p>
     *
     * @param value A value to assign.
     * @return A previous value.
     */
    public V set(V value) {
        return setIf(null, value);
    }

    /**
     * <p>
     * Assign the new value.
     * </p>
     *
     * @param value A value to assign.
     * @return A previous value.
     */
    public V set(Optional<V> value) {
        return setIf(null, value);
    }

    /**
     * <p>
     * Assign the new value.
     * </p>
     *
     * @param value A value generator.
     * @return A previous value.
     */
    public V set(Supplier<V> value) {
        return setIf(null, value);
    }

    /**
     * <p>
     * Assign the new value.
     * </p>
     *
     * @param value A value generator.
     * @return A previous value.
     */
    public V set(UnaryOperator<V> value) {
        return setIf(null, value);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid.
     * </p>
     *
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIf(Predicate<V> condition, V value) {
        return assign(condition, value, false);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid.
     * </p>
     *
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIf(Predicate<V> condition, Optional<V> value) {
        return setIf(condition, of(value));
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid.
     * </p>
     *
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIf(Predicate<V> condition, Supplier<V> value) {
        return assign(condition, of(value).v, false);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid.
     * </p>
     *
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIf(Predicate<V> condition, UnaryOperator<V> value) {
        return setIf(condition, value == null ? null : value.apply(v));
    }

    /**
     * <p>
     * Assign the new immutable value.
     * </p>
     *
     * @param value A value to assign.
     * @return A previous value.
     */
    public V let(V value) {
        return letIf(null, value);
    }

    /**
     * <p>
     * Assign the new immutable value.
     * </p>
     *
     * @param value A value to assign.
     * @return A previous value.
     */
    public V let(Optional<V> value) {
        return letIf(null, value);
    }

    /**
     * <p>
     * Assign the new immutable value.
     * </p>
     *
     * @param value A value generator.
     * @return A previous value.
     */
    public V let(Supplier<V> value) {
        return letIf(null, value);
    }

    /**
     * <p>
     * Assign the new immutable value.
     * </p>
     *
     * @param value A value generator.
     * @return A previous value.
     */
    public V let(UnaryOperator<V> value) {
        return letIf(null, value);
    }

    /**
     * <p>
     * Assign the new immutable value when the specified condition is valid.
     * </p>
     *
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V letIf(Predicate<V> condition, V value) {
        return assign(condition, value, true);
    }

    /**
     * <p>
     * Assign the new immutable value when the specified condition is valid.
     * </p>
     *
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V letIf(Predicate<V> condition, Optional<V> value) {
        return letIf(condition, of(value));
    }

    /**
     * <p>
     * Assign the new immutable value when the specified condition is valid.
     * </p>
     *
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V letIf(Predicate<V> condition, Supplier<V> value) {
        return letIf(condition, of(value).v);
    }

    /**
     * <p>
     * Assign the new immutable value when the specified condition is valid.
     * </p>
     *
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V letIf(Predicate<V> condition, UnaryOperator<V> value) {
        return letIf(condition, value == null ? null : value.apply(v));
    }

    /**
     * <p>
     * Assign the new value if we can.
     * </p>
     * 
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @param let A state of let or set.
     * @return A previous value.
     */
    private synchronized V assign(Predicate<V> condition, V value, boolean let) {
        V prev = v;

        if (fix == false && (condition == null || is(condition))) {
            if (adjuster != null) {
                value = adjuster.apply(value);
            }

            if (let) fix = true;

            try {
                set.invoke(value);
            } catch (Throwable e) {
                throw I.quiet(e);
            }

            if (observers != null) {
                for (Observer observer : observers) {
                    observer.accept(v);
                }
            }
        }
        return prev;
    }

    /**
     * Set requirment of this {@link Variable}.
     * 
     * @param adjuster
     * @return Chainable API.
     */
    public Variable<V> adjust(Function<V, V> adjuster) {
        this.adjuster = adjuster;
        return this;
    }

    /**
     * Set requirment of this {@link Variable}.
     * 
     * @param requirement
     * @return Chainable API.
     */
    public Variable<V> require(Predicate<V> requirement) {
        if (requirement != null) {
            adjust(v -> requirement.test(v) ? v : this.v);
        }
        return this;
    }

    /**
     * <p>
     * Execute the specified action if the value is present.
     * </p>
     * 
     * @param some A user action.
     */
    public void to(Consumer<V> some) {
        to(some, null);
    }

    /**
     * Execute the specified action.
     * 
     * @param some A user action.
     */
    public void to(Consumer<V> some, Runnable none) {
        if (v != null) {
            if (some != null) {
                some.accept(v);
            }
        } else {
            if (none != null) {
                none.run();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable == false) {
            return false;
        }
        return ((Variable) obj).is(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.valueOf(v);
    }

    /**
     * <p>
     * Create {@link Variable} with the specified value.
     * </p>
     *
     * @param value An actual value, <code>null</code> will be acceptable.
     * @return A created {@link Variable}.
     */
    public static <T> Variable<T> of(T value) {
        return new Variable(value);
    }

    /**
     * <p>
     * Create {@link Variable} with the specified value.
     * </p>
     *
     * @param value An actual value, <code>null</code> will be acceptable.
     * @return A created {@link Variable}.
     */
    public static <T> Variable<T> of(Supplier<T> value) {
        return of(value == null ? null : value.get());
    }

    /**
     * <p>
     * Create {@link Variable} with the specified value.
     * </p>
     *
     * @param value An actual value, <code>null</code> will be acceptable.
     * @return A created {@link Variable}.
     */
    public static <T> Variable<T> of(Optional<T> value) {
        return of(value == null ? null : value.orElse(null));
    }

    /**
     * <p>
     * Create empty {@link Variable}.
     * </p>
     *
     * @return A new empty {@link Variable}.
     */
    public static <T> Variable<T> empty() {
        return new Variable(null);
    }
}
