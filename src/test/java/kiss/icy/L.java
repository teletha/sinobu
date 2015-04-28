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

import java.util.function.Function;

import kiss.icy.model.Lens;

/**
 * @version 2015/04/21 23:20:12
 */
public class L {

    public static <M, P> M operate(M model, Operation<M> setter) {
        return setter.apply(model);
    }

    public static <M> M operate(M model, Operation<M> operation1, Operation<M> operation2) {
        model = operate(model, operation1);
        model = operate(model, operation2);

        return model;
    }

    public static <M, P1, P2> M operate(M model, Lens<M, P1> lens1, Lens<P1, P2> lens2, P2 value) {
        return lens1.then(lens2).set(model, value);
    }

    public static <M, P1, P2> M operate(M model, Lens<M, P1> lens1, Lens<P1, P2> lens2, Function<P1, Function<P2, P2>> value) {
        P1 property1 = lens1.get(model);
        P2 property2 = lens2.get(property1);

        property2 = value.apply(property1).apply(property2);

        return lens1.set(model, lens2.set(property1, property2));
    }

}
