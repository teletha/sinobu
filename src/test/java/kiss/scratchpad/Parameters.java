/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

/**
 * @version 2015/08/25 16:27:27
 */
public interface Parameters<T> extends NewableConsumer<T> {

    default T get() {
        T t = newInstance();
        accept(t);
        return t;
    }
}