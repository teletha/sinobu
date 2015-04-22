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
import java.util.stream.Stream;

/**
 * @version 2015/04/21 10:45:23
 */
public class Seq<E> {

    protected LinkedList<E> list;

    Seq(LinkedList<E> list) {
        this.list = list;
    }

    public E head() {
        return list.peekFirst();
    }

    public E tail() {
        return list.peekLast();
    }

    public E get(int index) {
        return list.get(index);
    }

    public Stream<E> stream() {
        return list.stream();
    }

    /**
     * @param person
     * @return
     */
    public static <E> Seq<Person> of(E item) {
        LinkedList<E> list = new LinkedList();
        list.add(item);

        return new Seq(list);
    }

    /**
     * @param newer
     * @return
     */
    public static <E> Lens<Seq<E>, E> ADD(int index) {
        return Lens.<Seq<E>, E> of(seq -> seq.get(index), (seq, value) -> {
            LinkedList list = new LinkedList(seq.list);
            list.add(index, value);

            return new Seq(list);
        });
    }

}
