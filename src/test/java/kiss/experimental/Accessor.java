/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @version 2016/04/30 12:23:03
 */
public interface Accessor<M, V> {

    /** The empty accessor. */
    Accessor Φ = of(model -> model, (model, property) -> property);

    /**
     * Getter.
     * 
     * @param model A target model to operate.
     * @return A value.
     */
    V get(M model);

    /**
     * Operation by using new value only.
     * 
     * @param model A target model to operate.
     * @param property A new value to set.
     * @return An applied model.
     */
    M set(M model, V property);

    /**
     * <p>
     * Operation by using current value.
     * </p>
     * <p>
     * </p>
     * 
     * @param model A target model to operate.
     * @param property A value apllicative function.
     * @return An applied model.
     */
    default M set(M model, UnaryOperator<V> property) {
        return set(model, property.apply(get(model)));
    }

    /**
     * <p>
     * Compose {@link Accessor}.
     * </p>
     * 
     * @param lens A next {@link Accessor}.
     * @return A composed {@link Accessor}.
     */
    public default <P> Accessor<M, P> then(Accessor<V, P> lens) {
        return new Accessor<M, P>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public P get(M model) {
                return lens.get(Accessor.this.get(model));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public M set(M model, P property) {
                return Accessor.this.set(model, lens.set(Accessor.this.get(model), property));
            }
        };
    }

    public default UnaryOperator<M> then(UnaryOperator<V> operation) {
        return model -> this.set(model, operation.apply(this.get(model)));
    }

    /**
     * <p>
     * Helper function to create lens easily.
     * </p>
     * 
     * @param getter A value retriever.
     * @param setter A value associator.
     * @return A created {@link Accessor}.
     */
    public static <M, V> Accessor<M, V> of(Function<M, V> getter, BiFunction<M, V, M> setter) {
        return new Accessor<M, V>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public V get(M model) {
                return getter.apply(model);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public M set(M model, V property) {
                return setter.apply(model, property);
            }
        };
    }
}