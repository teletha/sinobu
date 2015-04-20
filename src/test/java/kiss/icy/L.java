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

import kiss.Binary;
import kiss.I;

/**
 * @version 2015/04/19 19:45:05
 */
public class L {

    public static <M, Ops extends ModelOperationSet<M>> M operate(M model, ModelOperation<Ops> operation1) {
        Ops ops = (Ops) I.find(ModelOperationSet.class, model.getClass());
        ops.with(model);

        operation1.apply(ops);

        return ops.build();
    }

    public static <M, Ops extends ModelOperationSet<M>> M operate(M model, ModelOperation<Ops> operation1, ModelOperation<Ops> operation2) {
        Ops ops = (Ops) I.find(ModelOperationSet.class, model.getClass());
        ops.with(model);

        operation1.apply(ops);
        operation2.apply(ops);

        return ops.build();
    }

    public static <M, P> M operate(M model, Lens<M, P> lens, P value) {
        return lens.set(model, value);
    }

    public static <M, P1, P2> M operate(M model, Lens<M, P1> lens1, Lens<P1, P2> lens2, P2 value) {
        return operate(model, lens1.then(lens2), value);
    }

    public static <M, P> M operate(M model, Binary<Lens<M, P>, P> lens) {
        return lens.a.set(model, lens.e);
    }
}
