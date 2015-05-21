/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.buffer;

/**
 * @version 2015/05/21 2:40:43
 */
class MultipleBuffer<E> implements Buffer<E> {

    private final E head;

    private final Buffer<E> tail;

    /**
     * @param head
     * @param tail
     */
    MultipleBuffer(E head, Buffer<E> tail) {
        this.head = head;
        this.tail = tail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E head() {
        return head;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer<E> head(E item) {
        return new MultipleBuffer(item, tail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E tail() {
        return tail.tail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer<E> tail(E item) {
        return new MultipleBuffer(head, tail.tail(item));
    }

}
