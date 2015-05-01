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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import kiss.icy.L;
import kiss.icy.Operation;

/**
 * @version 2015/04/21 10:45:23
 */
public abstract class Seq<E> implements Operatable<Seq<E>> {

    protected List<E> list;

    private Seq() {
    }

    public E get(int index) {
        return list.get(index);
    }

    public abstract Seq<E> set(int index, E value);

    public abstract Seq<E> add(E value);

    public int size() {
        return list.size();
    }

    public abstract Seq<E> clear();

    public Stream<E> stream() {
        return list.stream();
    }

    /**
     * @param person
     * @return
     */
    public static <E> Seq<E> of(E item) {
        LinkedList<E> list = new LinkedList();
        list.add(item);

        return new Melty(list);
    }

    /**
     * @param person
     * @return
     */
    public static <E> Seq<E> of(E item1, E item2) {
        LinkedList<E> list = new LinkedList();
        list.add(item1);
        list.add(item2);

        return new Melty(list);
    }

    /**
     * @param newer
     * @return
     */
    public static <E> Lens<Seq<E>, E> ADD(int index) {
        return Lens.<Seq<E>, E> of(seq -> seq.get(index), (seq, value) -> {
            LinkedList list = new LinkedList(seq.list);
            list.add(index, value);

            return new Melty(list);
        });
    }

    /**
     * @version 2015/04/28 13:15:42
     */
    private static class Icy<E> extends Seq<E> {

        /**
         * @param list
         */
        private Icy(Seq<E> base) {
            list = new ArrayList();

            if (base != null) {
                for (E item : base.list) {
                    if (item instanceof Operatable) {
                        list.add((E) ((Operatable) item).ice());
                    } else {
                        list.add(item);
                    }
                }
            }
        }

        private Icy(List<E> base) {
            list = base;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Seq<E> set(int index, E value) {
            List<E> created = new ArrayList(list);
            created.set(index, value);

            return new Icy(created);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Seq<E> add(E value) {
            List<E> created = new ArrayList(list);
            created.add(value);

            return new Icy(created);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Seq<E> clear() {
            list.clear();
            return new Icy(Collections.EMPTY_LIST);
        }

        /**
         * <p>
         * Create new mutable model.
         * </p>
         * 
         * @return An immutable model.
         */
        @Override
        public Seq<E> melt() {
            return new Melty(this);
        }
    }

    /**
     * @version 2015/04/28 13:15:00
     */
    private static class Melty<E> extends Seq<E> {

        /**
         * @param list
         */
        private Melty(Seq<E> base) {
            list = new ArrayList();
            if (base == null) {

            } else {
                for (E item : base.list) {
                    if (item instanceof Operatable) {
                        list.add((E) ((Operatable) item).melt());
                    } else {
                        list.add(item);
                    }
                }
            }
        }

        /**
         * @param list
         */
        private Melty(List<E> base) {
            list = base;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Seq<E> set(int index, E value) {
            list.set(index, value);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Seq<E> add(E value) {
            list.add(value);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Seq<E> clear() {
            list.clear();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Seq<E> ice() {
            return new Icy(this);
        }
    }

    /**
     * @version 2015/04/28 11:53:32
     */
    public static class Operator<M, V, O extends ModelOperator<M, V>> extends ModelOperator<M, Seq<V>> {

        private O sub;

        /**
         * @param lens
         */
        public Operator(Lens<M, Seq<V>> lens, O sub) {
            super(lens);

            this.sub = sub;
        }

        public O at(int index) {
            sub.lens = lens.then(Lens.of(model -> model.get(index), (model, value) -> model.set(index, value)));

            return sub;
        }

        public Lens<M, V> add() {
            sub.lens = lens.then(Lens.of(null, Seq::add));

            return sub;
        }

        public UnaryOperator<M> clear() {
            return lens.then(Seq::clear);
        }

        public <S> Lens<M, S> all(Function<O, ModelOperator<M, S>> operator) {
            return model -> {
                Seq<V> seq = lens.get(model);
                LinkedList<V> list = new LinkedList();
                Operation<V> setter = operator.apply(sub);

                for (V value : seq.list) {
                    list.add(L.operate(value, setter));
                }

                return lens.set(model, new Seq(list));
            };
        }

        /**
         * @param object
         * @param object2
         * @return
         */
        public Operation<M> select(Predicate<V> condition, Function<O, Operation<V>> operator) {
            return model -> {
                Seq<V> seq = lens.get(model);
                LinkedList<V> list = new LinkedList();
                Operation<V> setter = operator.apply(sub);

                for (V value : seq.list) {
                    if (condition.test(value)) {
                        list.add(L.operate(value, setter));
                    } else {
                        list.add(value);
                    }
                }
                return lens.set(model, new Melty(list));
            };
        }
    }
}
