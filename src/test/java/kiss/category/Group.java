/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.category;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import kiss.Extensible;

/**
 * A <code>Monoid</code> where every element has an inverse. x == inverse(inverse(x))
 * inverse(identity) == identity
 * 
 * @version 2016/03/30 3:03:36
 */
public interface Group<A> extends Monoid<A>, Extensible {

    public A inverse(A a);

    public static <A> Group<A> create(A identity, BinaryOperator<A> fn, UnaryOperator<A> inv) {
        return new Group<A>() {

            @Override
            public A inverse(A a) {
                return inv.apply(a);
            }

            @Override
            public A empty() {
                return identity;
            }

            @Override
            public A append(A x, A y) {
                return fn.apply(x, y);
            }
        };
    }
}
