/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.Arrays;

public interface Widen<F> {
    /**
     * Widen parameter at last (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default F append() {
        return I.make(this, Widen.class, 0, args -> {
            return ((Flexible) this).invoke(Arrays.copyOfRange(args, 0, args.length - 1));
        });
    }

    /**
     * Widen parameter at first (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default F prepend() {
        return I.make(this, Widen.class, 0, args -> {
            return ((Flexible) this).invoke(Arrays.copyOfRange(args, 1, args.length));
        });
    }
}
