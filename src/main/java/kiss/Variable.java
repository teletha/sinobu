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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @version 2016/10/23 13:23:45
 */
public class Variable<V> implements Consumer<V>, Supplier<V> {

    /** The modifier. */
    private static final Field modify;

    static {
        try {
            modify = Variable.class.getField("v");
            modify.setAccessible(true);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The current value. This value is not final but read-only. */
    public transient final V v;

    /** The immutability. */
    private final AtomicBoolean fix = new AtomicBoolean();

    /** The observers. */
    private volatile List<Observer> observers;

    /**
     * Hide constructor.
     */
    private Variable(V value) {
        this.v = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(V value) {
        set(v);
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
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public boolean is(V value) {
        return Objects.equals(v, value);
    }

    /**
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public boolean is(BooleanSupplier condition) {
        return condition == null ? false : condition.getAsBoolean();
    }

    /**
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public boolean is(Predicate<V> condition) {
        return condition == null ? false : condition.test(v);
    }

    /**
     * Check whether the value is absent or not.
     *
     * @return A result.
     */
    public boolean isAbsent() {
        return is(Objects::isNull);
    }

    /**
     * Check whether the value is present or not.
     *
     * @return A result.
     */
    public boolean isPresent() {
        return is(Objects::nonNull);
    }

    /**
     * <p>
     * Execute the specified action if the value is present.
     * </p>
     * 
     * @param action A user action.
     */
    public void map(Consumer<V> action) {
        if (v != null && action != null) {
            action.accept(v);
        }
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
        return v == null || then == null ? empty() : of(then.apply(v));
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
    public Events<V> observe() {
        return new Events<V>(observer -> {
            if (observers == null) {
                observers = new CopyOnWriteArrayList();
            }
            observers.add(observer);

            return () -> {
                observers.remove(observer);

                if (observers.isEmpty()) {
                    observers = null;
                }
            };
        });
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
    public Variable<V> or(Variable<V> other) {
        return v != null ? this : other != null ? other : empty();
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
        return setIf(I.accept(), value);
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
        return setIf(I.accept(), value);
    }

    /**
     * <p>
     * Assign the new value.
     * </p>
     *
     * @param value A value to assign.
     * @return A previous value.
     */
    public V set(Variable<V> value) {
        return setIf(I.accept(), value);
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
        return setIf(I.accept(), value);
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
        return setIf(I.accept(), value);
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
    public V setIf(Predicate<V> condition, Variable<V> value) {
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
    public V setIf(Predicate<V> condition, Supplier<V> value) {
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
        return letIf(I.accept(), value);
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
        return letIf(I.accept(), value);
    }

    /**
     * <p>
     * Assign the new immutable value.
     * </p>
     *
     * @param value A value to assign.
     * @return A previous value.
     */
    public V let(Variable<V> value) {
        return letIf(I.accept(), value);
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
        return letIf(I.accept(), value);
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
        return letIf(I.accept(), value);
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
    public V letIf(Predicate<V> condition, Variable<V> value) {
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
    public V letIf(Predicate<V> condition, Supplier<V> value) {
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
    private V assign(Predicate<V> condition, Variable<V> value, boolean let) {
        V prev = v;

        if (fix.get() == false) {
            if (is(condition)) {
                if (fix.compareAndSet(false, let)) {
                    try {
                        modify.set(this, value == null ? null : value.v);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }

                    if (observers != null) {
                        for (Observer observer : observers) {
                            observer.accept(v);
                        }
                    }
                }
            }
        }
        return prev;
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
        return fix.get() ? "Immutable" : "Mutable" + " Variable [" + v + "]";
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
