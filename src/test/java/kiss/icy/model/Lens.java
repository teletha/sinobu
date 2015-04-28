/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy.model;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @version 2015/04/19 22:00:04
 */
public interface Lens<M, V> {

    Lens Φ = new Lens() {

        /**
         * {@inheritDoc}
         */
        @Override
        public Object get(Object model) {
            return model;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object set(Object model, Object property) {
            return property;
        }
    };

    /**
     * <p>
     * Getter.
     * </p>
     * 
     * @param model
     * @return
     */
    V get(M model);

    /**
     * <p>
     * Operation.
     * </p>
     * 
     * @param model
     * @param property
     * @return
     */
    M set(M model, V property);
    
    /**
     * <p>
     * Operation by using current value.
     * </p>
     * <p></p>
     * 
     * @param model
     * @param property
     * @return
     */
    default M set(M model, Function<V, V> property) {
        return set(model, property.apply(get(model)));
    }

    /**
     * <p>
     * Compose {@link Lens}.
     * </p>
     * 
     * @param lens
     * @return
     */
    public default <P> Lens<M, P> then(Lens<V, P> lens) {
        return new Lens<M, P>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public P get(M model) {
                return lens.get(Lens.this.get(model));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public M set(M model, P property) {
                return Lens.this.set(model, lens.set(Lens.this.get(model), property));
            }
        };
    }

    /**
     * <p>
     * Helper function to create lens easily.
     * </p>
     * 
     * @param getter
     * @param setter
     * @return
     */
    public static <M, V> Lens<M, V> of(Function<M, V> getter, BiFunction<M, V, M> setter) {
        return new Lens<M, V>() {

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
