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

import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @version 2015/04/22 0:32:58
 */
public class SeqOp<M, V, O extends ModelOperator<V, ?>, O2 extends ModelOperator<M, V>>
        extends ModelOperator<M, Seq<V>> {

    private O sub;

    /**
     * @param lens
     */
    public SeqOp(Lens<M, Seq<V>> lens, O sub) {
        super(lens);

        this.sub = sub;
    }

    public Setter<M> add(int index, V item) {
        return model -> {
            Seq<V> seq = lens.get(model);
            LinkedList list = new LinkedList(seq.list);
            list.add(index, item);

            return lens.set(model, new Seq(list));
        };
    }

    public Setter<M> all(Function<O, Setter<V>> operator) {
        return model -> {
            Seq<V> seq = lens.get(model);
            LinkedList<V> list = new LinkedList();
            Setter<V> setter = operator.apply(sub);

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
    public Setter<M> select(Predicate<V> condition, Function<O, Setter<V>> operator) {
        return model -> {
            Seq<V> seq = lens.get(model);
            LinkedList<V> list = new LinkedList();
            Setter<V> setter = operator.apply(sub);

            for (V value : seq.list) {
                if (condition.test(value)) {
                    list.add(L.operate(value, setter));
                } else {
                    list.add(value);
                }
            }
            return lens.set(model, new Seq(list));
        };
    }
}
