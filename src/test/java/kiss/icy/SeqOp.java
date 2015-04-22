/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @version 2015/04/22 0:32:58
 */
public class SeqOp<M, V> extends ModelOperator<M, Seq<V>> {

    /**
     * @param lens
     */
    public SeqOp(Lens<M, Seq<V>> lens) {
        super(lens);
    }

    public Setter<M> add(int index, V item) {
        return model -> {
            Seq<V> seq = lens.get(model);
            LinkedList list = new LinkedList(seq.list);
            list.add(index, item);

            return lens.set(model, new Seq(list));
        };
    }

    public PersonOp2<M> all() {
        return new PersonOp2<M>(new Lens<M, Person>() {

            private Iterator<V> iterator;

            /**
             * {@inheritDoc}
             */
            @Override
            public Person get(M model) {
                if (iterator == null) {
                    iterator = lens.get(model).list.iterator();
                }

                if (iterator.hasNext()) {
                    return iterator.next();
                }
                return;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public M set(M model, Person property) {
                Seq<V> seq = lens.get(model);
                LinkedList<V> list = new LinkedList();

                for (V value : seq.list) {
                    list.add();
                }

                return lens.set(model, new Seq(list));
            }

        });
    }
}
