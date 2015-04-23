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

/**
 * @version 2015/04/22 0:32:58
 */
public class SeqOp<M, V, O extends ModelOperator<M, V>> extends ModelOperator<M, Seq<V>> {

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

    public O all() {
        return sub;
    }
}
