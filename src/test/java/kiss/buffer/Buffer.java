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

import java.util.Arrays;

/**
 * @version 2015/05/21 2:28:42
 */
public interface Buffer<E> {

    E head();

    Buffer<E> head(E item);

    E tail();

    Buffer<E> tail(E item);

    static <E> Buffer<E> of(E... items) {
        if (items.length == 1) {
            return new SingleBuffer(items[0]);
        } else {
            return new MultipleBuffer(items[0], of(Arrays.copyOfRange(items, 1, items.length)));
        }
    }
}
