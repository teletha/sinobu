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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @version 2016/10/23 13:23:45
 */
public class Variable<V> {

    /** The accept condition. */
    private static final Predicate Accept = c -> true;

    /** The current value. */
    private final AtomicReference<V> value = new AtomicReference();

    /** The observers. */
    private volatile List<Observer> observers;

    /**
     * Hide constructor.
     */
    private Variable() {
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
    public V get(V value) {
        return get(() -> value);
    }

    /**
     * <p>
     * Compute the current value. If it is <code>null</code>, this method returns the specified
     * default value.
     * </p>
     * 
     * @param value The default value supplier.
     * @return The current value or the specified default value.
     */
    public V get(Supplier<V> value) {
        V current = this.value.get();
        return current == null ? value == null ? null : value.get() : current;
    }

    /**
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public boolean is(V value) {
        return Objects.equals(this.value.get(), value);
    }

    /**
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public boolean is(Predicate<V> condition) {
        return condition == null ? false : condition.test(this.value.get());
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
     * Perform the specified action if the value is present.
     * </p>
     * 
     * @param action An action to perform.
     */
    public void map(Consumer<V> action) {
        if (this.value != null && action != null) {
            action.accept(this.value.get());
        }
    }

    /**
     * <p>
     * Perform the specified action if the value is present.
     * </p>
     * 
     * @param action An action to perform.
     * @return The computed {@link Variable}.
     */
    public <R> Variable<R> map(Function<V, R> converter) {
        return this.value == null || converter == null ? new Variable() : of(converter.apply(this.value.get()));
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
        return this.value == null || converter == null ? new Variable() : converter.apply(this.value.get());
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
        return or(of(other));
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
        return isAbsent() ? other == null ? of(null) : other : this;
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
        return setIf(Accept, value);
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
        return setIf(Accept, value);
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
        return setIf(Accept, value);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid..
     * </p>
     * 
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIf(Predicate<V> condition, V value) {
        return setIf(condition, current -> value);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid..
     * </p>
     * 
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIf(Predicate<V> condition, Supplier<V> value) {
        return setIf(condition, current -> value == null ? current : value.get());
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid..
     * </p>
     * 
     * @param condition A condition for value assign.
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIf(Predicate<V> condition, UnaryOperator<V> value) {
        return value == null || condition == null ? this.value.get() : this.value.getAndUpdate(current -> {
            if (condition.test(current)) {
                current = value.apply(current);

                if (observers != null) {
                    for (Observer observer : observers) {
                        observer.accept(current);
                    }
                }
            }
            return current;
        });
    }

    /**
     * <p>
     * Assign the new value when the specified condition is invalid.
     * </p>
     * 
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIfAbsent(V value) {
        return setIf(Objects::isNull, value);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is invalid.
     * </p>
     * 
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIfAbsent(Supplier<V> value) {
        return setIf(Objects::isNull, value);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is invalid.
     * </p>
     * 
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIfAbsent(UnaryOperator<V> value) {
        return setIf(Objects::isNull, value);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid.
     * </p>
     * 
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIfPresent(V value) {
        return setIf(Objects::nonNull, value);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid.
     * </p>
     * 
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIfPresent(Supplier<V> value) {
        return setIf(Objects::nonNull, value);
    }

    /**
     * <p>
     * Assign the new value when the specified condition is valid.
     * </p>
     * 
     * @param value A value to assign.
     * @return A previous value.
     */
    public V setIfPresent(UnaryOperator<V> value) {
        return setIf(Objects::nonNull, value);
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
        Variable<T> var = new Variable();
        var.value.set(value);
        return var;
    }
}
