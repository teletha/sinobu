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
 * @version 2015/05/21 2:37:33
 */
class SingleBuffer<E> implements Buffer<E> {

    /** The sigle item. */
    private final E item;

    /**
     * @param item
     */
    SingleBuffer(E item) {
        this.item = item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E head() {
        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer<E> head(E item) {
        return Buffer.of(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E tail() {
        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer<E> tail(E item) {
        return Buffer.of(item);
    }
}
