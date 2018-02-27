/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.category;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @version 2016/10/18 20:57:36
 */
public interface Lens<Model, Value> extends Function<Function<Value, Functor<Value>>, Function<Model, Functor<Model>>> {

    /**
     * A generic getter.
     * 
     * @param model
     * @return
     */
    default Value get(Model model) {
        return ((Const<Value, Model>) apply(Const::new).apply(model)).value;
    }

    /**
     * A generic setter.
     * 
     * @param model
     * @param value
     * @return
     */
    default Model set(Model model, Value value) {
        return set(model, ignore -> value);
    }

    /**
     * A generic setter using the current value.
     * 
     * @param model
     * @param value
     * @return
     */
    default Model set(Model model, Function<Value, Value> value) {
        return ((Identity<Model>) apply(value.andThen(Identity::new)).apply(model)).value;
    }

    /**
     * Compose accessors.
     * 
     * @param other
     * @return
     */
    default <NestedValue> Lens<Model, NestedValue> then(Lens<Value, NestedValue> other) {
        return compose(other)::apply;
    }

    /**
     * Helper method to create {@link Lens} from getter and setter functions.
     * 
     * @param getter
     * @param setter
     * @return
     */
    static <M, V> Lens<M, V> of(Function<M, V> getter, BiFunction<M, V, M> setter) {
        return lens -> model -> lens.apply(getter.apply(model)).fmap(newValue -> setter.apply(model, newValue));
    }

    /**
     * Helper method to create {@link Lens} from getter and setter functions.
     * 
     * @param getter
     * @param setter
     * @return
     */
    static <M, V> Lens<M, V> of(Function<M, V> getter, BiConsumer<M, V> setter) {
        return lens -> model -> lens.apply(getter.apply(model)).fmap(newValue -> {
            setter.accept(model, newValue);
            return model;
        });
    }

    /**
     * @version 2016/10/18 20:54:32
     */
    public static class Identity<Param> implements Functor<Param> {

        /** The actual value. */
        private final Param value;

        /**
         * @param value
         */
        private Identity(Param value) {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <Return> Identity<Return> fmap(Function<Param, Return> function) {
            return new Identity<>(function.apply(value));
        }
    }

    /**
     * @version 2016/10/18 20:55:55
     */
    public static class Const<Value, UnusedParam> implements Functor<UnusedParam> {

        /** The actual value. */
        private final Value value;

        /**
         * @param value
         */
        private Const(Value value) {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <UnusedReturn> Const<Value, UnusedReturn> fmap(Function<UnusedParam, UnusedReturn> fn) {
            return (Const<Value, UnusedReturn>) this;
        }
    }
}
