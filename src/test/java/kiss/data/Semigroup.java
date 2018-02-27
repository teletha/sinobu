/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.data;

/**
 * @version 2015/04/23 15:06:35
 */
public interface Semigroup<Member> {
    
    Member dot(Member x, Member y);


    public static <Member> Semigroup<Member> first() {
        return (x, y) -> x;
    }

    public static <A> Semigroup<A> last() {
        return (x, y) -> y;
    }

    public static <A> Semigroup<A> dual(Semigroup<A> semigroup) {
        return (x, y) -> semigroup.dot(y, x);
    }

    public static <A extends Comparable<A>> Semigroup<A> min() {
        return (x, y) -> x.compareTo(y) <= 0 ? x : y;
    }

    public static <A extends Comparable<A>> Semigroup<A> max() {
        return (x, y) -> x.compareTo(y) >= 0 ? x : y;
    }
}
