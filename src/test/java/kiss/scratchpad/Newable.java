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

import kiss.I;

/**
 * @version 2015/08/25 16:28:14
 */
public interface Newable<T> {

    default T newInstance() {
        try {
            return (T) I.make(MethodFinder.method(this).getParameters()[0].getType());
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
