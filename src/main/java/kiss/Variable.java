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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A reassignable container object which may or may not contain a non-null value. If a value is
 * present, {@link #isPresent()} returns true. If no value is present, the object is considered
 * empty and {@link #isPresent()} returns false. Unlike {@link Optional}, this container is
 * reassignable.
 * <p>
 * Additional methods that depend on the presence or absence of a contained value are provided, such
 * as {@link #or(Object)} (returns a default value if no value is present) and {@link #to(Consumer)}
 * (performs an action if a value is present).
 * <p>
 */
public class Variable<V> implements Consumer<V>, Supplier<V> {

    /** The modifier base. */
    private static final MethodHandle set;

    static {
        try {
            Field modify = Variable.class.getField("v");
            modify.setAccessible(true);

            set = MethodHandles.lookup().unreflectSetter(modify);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The current value. This value is not final but read-only. */
    public transient final V v;

    /** The immutability. */
    private boolean fix;

    /** The observer */
    Signaling<V> signaling;

    /** The value interceptor. */
    private volatile WiseBiFunction<V, V, V> interceptor;

    /**
     * Hide constructor.
     */
    protected Variable(V value) {
        this.v = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(V value) {
        set(value);
    }

    /**
     * Get the current value. If there is no value, wait until the value is set.
     *
     * @return Current value or next value.
     * @see #acquire()
     * @see #exact()
     * @see #get()
     * @see #next()
     * @see #or(Object)
     * @see #or(Supplier)
     */
    public final V acquire() {
        return or(this::next);
    }

    /**
     * Get the current value. If there is no value, {@link NullPointerException} will be thrown.
     * 
     * @return Current value or throws error.
     * @see #acquire()
     * @see #exact()
     * @see #get()
     * @see #next()
     * @see #or(Object)
     * @see #or(Supplier)
     */
    public final V exact() {
        return or(() -> {
            throw new NullPointerException();
        });
    }

    /**
     * Get the current value. If there is no value, returns null.
     *
     * @return Current value or null.
     * @see #acquire()
     * @see #exact()
     * @see #get()
     * @see #next()
     * @see #or(Object)
     * @see #or(Supplier)
     */
    @Override
    public final V get() {
        return v;
    }

    /**
     * Intercept the value modification.
     * 
     * @param interceptor A interceptor for value modification. First parameter is the current
     *            value, Second parameter is the new value.
     * @return Chainable API.
     */
    public final Variable<V> intercept(WiseBiFunction<V, V, V> interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    /**
     * Test whether the current value is equal to the specified value or not.
     * 
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public final boolean is(V value) {
        return Objects.equals(v, value);
    }

    /**
     * Test whether the current value fulfills the specified condition or not.
     * 
     * @param condition A value validator.
     * @return A result of validation.
     */
    public final boolean is(Predicate<V> condition) {
        return condition == null ? false : condition.test(v);
    }

    /**
     * Test whether the current value is NOT equal to the specified value or not.
     * 
     * @param value A value to check the equality.
     * @return A result of equality.
     */
    public final boolean isNot(V value) {
        return !is(value);
    }

    /**
     * Test whether the current value does NOT fulfill the specified condition or not.
     * 
     * @param condition A value validator.
     * @return A result of validation.
     */
    public final boolean isNot(Predicate<V> condition) {
        return !is(condition);
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
     * Fix this value as immutable.
     * 
     * @return Chainable API.
     */
    public final Variable<V> fix() {
        fix = true;
        return this;
    }

    /**
     * Perform the specified action if the value is absent.
     *
     * @param then An action to perform.
     * @return The computed {@link Variable}.
     */
    public final <R> Variable<R> flip(Supplier<? extends R> then) {
        if (v == null && then != null) {
            try {
                return of(then.get());
            } catch (Throwable e) {
                // ignore
            }
        }
        return empty();
    }

    /**
     * If a value is present, returns the result of applying the given {@link Variable}-bearing
     * mapping function to the value, otherwise returns an empty {@link Variable}.
     *
     * @param mapper The mapping function to apply to a value, if present.
     * @return The computed {@link Variable}.
     */
    public final <R> Variable<R> flatFlip(Supplier<Variable<R>> mapper) {
        return v != null || mapper == null ? new Variable(null) : mapper.get();
    }

    /**
     * Perform the specified action if the value is present.
     *
     * @param then An action to perform.
     * @return The computed {@link Variable}.
     */
    public final <R> Variable<R> map(Function<? super V, ? extends R> then) {
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
     * If a value is present, returns the result of applying the given {@link Variable}-bearing
     * mapping function to the value, otherwise returns an empty {@link Variable}.
     *
     * @param mapper The mapping function to apply to a value, if present.
     * @return The computed {@link Variable}.
     */
    public final <R> Variable<R> flatMap(Function<V, Variable<R>> mapper) {
        return v == null || mapper == null ? new Variable(null) : mapper.apply(v);
    }

    /**
     * Get the value to be set next. The method waits until a new value is set.
     *
     * @return The next value.
     * @see #acquire()
     * @see #exact()
     * @see #get()
     * @see #next()
     * @see #or(Object)
     * @see #or(Supplier)
     */
    public final V next() {
        try {
            CompletableFuture<V> c = new CompletableFuture();
            observe().first().to(c::complete);
            return c.get();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Observe this {@link Variable}.
     *
     * @return The {@link Signal} which notifies value modification.
     */
    public final synchronized Signal<V> observe() {
        if (signaling == null) {
            signaling = new Signaling();
        }
        return signaling.expose;
    }

    /**
     * Observe this {@link Variable} with the current value.
     *
     * @return The {@link Signal} which notifies value modification.
     */
    public final Signal<V> observing() {
        return observe().startWith(this::get);
    }

    /**
     * Get the current value. If there is no value, returns the specified alternative value.
     *
     * @param other A value to be returned if there is no value present, may be null.
     * @return The current value or alternative value.
     * @see #acquire()
     * @see #exact()
     * @see #get()
     * @see #next()
     * @see #or(Object)
     * @see #or(Supplier)
     */
    public final V or(V other) {
        return v != null ? v : other;
    }

    /**
     * Get the current value. If there is no value, returns the specified alternative value.
     *
     * @param other A value to be returned if there is no value present, may be null.
     * @return The current value or alternative value.
     * @see #acquire()
     * @see #exact()
     * @see #get()
     * @see #next()
     * @see #or(Object)
     * @see #or(Supplier)
     */
    public final V or(Supplier<V> other) {
        return v != null ? v : other == null ? null : other.get();
    }

    /**
     * Assign the new value.
     *
     * @param value A value to assign.
     * @return A previous value.
     */
    public synchronized final V set(V value) {
        V prev = v;

        if (fix == false) {
            try {
                if (interceptor != null) {
                    value = interceptor.apply(this.v, value);
                }

                set.invoke(this, value);

                if (signaling != null) {
                    signaling.accept(v);
                }
            } catch (RuntimeException e) {
                // ignore
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        }
        return prev;
    }

    /**
     * Assign the new value.
     *
     * @param value A value generator.
     * @return A previous value.
     */
    public final V set(Supplier<V> value) {
        return set(value == null ? null : value.get());
    }

    /**
     * Assign the new value.
     *
     * @param value A value generator.
     * @return A previous value.
     */
    public final V set(UnaryOperator<V> value) {
        return set(value == null ? null : value.apply(v));
    }

    /**
     * Execute the specified action if the value is present.
     * 
     * @param some A user action.
     */
    public final void to(Consumer<V> some) {
        to(some, null);
    }

    /**
     * Execute the specified action.
     * 
     * @param some A user action.
     */
    public final void to(Consumer<V> some, Runnable none) {
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
    public final int hashCode() {
        return Objects.hash(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object obj) {
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
     * Create empty {@link Variable}.
     * </p>
     *
     * @return A new empty {@link Variable}.
     */
    public static <T> Variable<T> empty() {
        return new Variable(null);
    }
}