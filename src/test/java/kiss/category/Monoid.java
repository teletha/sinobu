/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.category;

import java.util.function.BinaryOperator;

import kiss.Extensible;

/**
 * A <code>Semigroup<code/> with an identity element: append(x,identity) == append(identity,x) == x
 * 
 * @version 2016/03/29 14:25:53
 */
public interface Monoid<A> extends Semigroup<A>, Extensible {

    A empty();

    public static <A> Monoid<A> dual(Monoid<A> monoid) {
        return create(monoid.empty(), (x, y) -> monoid.append(y, x));
    }

    public static <A> Monoid<A> create(A identity, BinaryOperator<A> fn) {
        return new Monoid<A>() {

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
