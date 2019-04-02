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

/**
 * Provide the signature morphing functionality.
 */
public interface Widen<Wide, Self> extends Flexible<Self> {

    /**
     * Insert parameter at first (appended parameter will be ignored).
     * 
     * @return An expanded function.
     */
    default Wide widen() {
        return I.make(this, Widen.class, args -> {
            return invoke(Arrays.copyOfRange(args, 1, args.length));
        });
    }

    /**
     * Insert parameter at last (appended parameter will be ignored).
     * 
     * @return An expanded function.
     */
    default Wide widenLast() {
        return I.make(this, Widen.class, this);
    }
}
