/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface WiseList<E> extends List<E> {

    /**
     * Determine if the list is NOT empty.
     * 
     * @return
     */
    default boolean isNotEmpty() {
        return !isEmpty();
    }

    default <R> R foldLeft(R init, BiFunction<? super R, ? super E, ? extends R> combine) {
        ListIterator<E> iterator = listIterator();
        while (iterator.hasNext()) {
            init = combine.apply(init, iterator.next());
        }
        return init;
    }

    default <R> R foldRight(R init, BiFunction<? super E, ? super R, ? extends R> combine) {
        ListIterator<E> iterator = listIterator(size());
        while (iterator.hasPrevious()) {
            init = combine.apply(iterator.previous(), init);
        }
        return init;
    }

    /**
     * Get the first element of this list.
     * 
     * @return
     */
    default Variable<E> first() {
        if (isEmpty()) {
            return Variable.empty();
        } else {
            return Variable.of(get(0));
        }
    }

    /**
     * Get the last element of this list.
     * 
     * @return
     */
    default Variable<E> last() {
        if (isEmpty()) {
            return Variable.empty();
        } else {
            return Variable.of(get(size() - 1));
        }
    }

    /**
     * Returns the first element of the iterable for which the predicate evaluates to true or null
     * in the case where no element returns true.
     * 
     * @return
     */
    default Variable<E> find(Predicate<E> matcher) {
        return I.signal(this).take(matcher).first().to();
    }

    /**
     * Returns all elements of the source collection that return true when evaluating the predicate.
     * This method is also commonly called filter.
     * 
     * @param selector
     * @return
     */
    default WiseList<E> take(Predicate<E> selector) {
        return I.signal(this).take(selector).toCollection(I.make(getClass()));
    }

    /**
     * Returns all elements of the source collection that return false when evaluating of the
     * predicate. This method is also sometimes called filterNot and is the equivalent of calling
     * iterable.select(Predicates.not(predicate)).
     * 
     * @param selector
     * @return
     */
    default WiseList<E> skip(Predicate<E> selector) {
        return I.signal(this).skip(selector).toCollection(I.make(getClass()));
    }
}
