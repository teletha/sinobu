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

/**
 * 
 */
public interface Widen<Wide> {
    /**
     * Widen parameter at last (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default Wide append() {
        return I.make(this, Widen.class, 0, args -> args.remove(args.size() - 1));
    }

    /**
     * Widen parameter at first (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default Wide prepend() {
        return I.make(this, Widen.class, 0, args -> args.remove(0));
    }
}
