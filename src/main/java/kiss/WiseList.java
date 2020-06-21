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
import java.util.Optional;

public interface WiseList<E> extends List<E> {

    /**
     * Determine if the list is NOT empty.
     * 
     * @return
     */
    default boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * Get the first element of this list.
     * 
     * @return
     */
    default Optional<E> peekFirst() {
        if (isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(get(0));
        }
    }

    /**
     * Get the last element of this list.
     * 
     * @return
     */
    default Optional<E> peekLast() {
        if (isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(get(size() - 1));
        }
    }

    /**
     * Get the last element of this list.
     * 
     * @return
     */
    default Optional<E> peekAt(int index) {
        int size = size();

        if (index < size) {
            return Optional.ofNullable(get(index));
        } else {
            return Optional.empty();
        }
    }
}
