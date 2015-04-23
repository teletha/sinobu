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

/**
 * @version 2015/04/21 23:20:12
 */
public class L {

    public static <M, P> M operate(M model, Setter<M> setter) {
        return setter.apply(model);
    }

    public static <M> M operate(M model, Setter<M> operation1, Setter<M> operation2) {
        model = operate(model, operation1);
        model = operate(model, operation2);

        return model;
    }
}
