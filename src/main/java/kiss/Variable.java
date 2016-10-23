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
        V current = this.value.get();
        return condition == null || current == null ? false : condition.test(current);
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
     * Assign the new value.
     * </p>
     * 
     * @param value A value to assign.
     * @return A previous value.
     */
    public V set(V value) {
        return set(current -> value);
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
        return set(current -> value == null ? current : value.get());
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
        return value == null ? this.value.get() : this.value.getAndUpdate(current -> {
            V newValue = value.apply(current);

            if (observers != null) {
                for (Observer observer : observers) {
                    observer.accept(newValue);
                }
            }
            return newValue;
        });
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
        return set(current -> condition != null && value != null && condition.test(current) ? value.apply(current) : current);
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
     * Convert to {@link Events}.
     * </p>
     * 
     * @return
     */
    public Events<V> to() {
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
