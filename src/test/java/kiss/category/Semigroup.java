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

import kiss.Extensible;

/**
 * A structure supporting an associative operation "apply": apply(x,apply(y,z)) ==
 * apply(apply(x,y),z)
 * 
 * @version 2016/03/30 2:58:54
 */
public interface Semigroup<A> extends Extensible {

    public A append(A x, A y);

    // public default A fold(A a, List<A> as) {
    // return as.isEmpty() ? a : fold(apply(a, as.head()), as.tail());
    // }
    //
    // public default A fold(A a, A... as) {
    // return fold(a, List.of(as));
    // }

    public static <A> Semigroup<A> first() {
        return (x, y) -> x;
    }

    public static <A> Semigroup<A> last() {
        return (x, y) -> y;
    }

    public static <A> Semigroup<A> dual(Semigroup<A> semigroup) {
        return (x, y) -> semigroup.append(y, x);
    }

    public static <A extends Comparable<A>> Semigroup<A> min() {
        return (x, y) -> x.compareTo(y) <= 0 ? x : y;
    }

    public static <A extends Comparable<A>> Semigroup<A> max() {
        return (x, y) -> x.compareTo(y) >= 0 ? x : y;
    }
}
