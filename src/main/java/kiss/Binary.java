/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

/**
 * @version 2015/04/10 1:02:17
 */
public class Binary<Param1, Param2> {

    /** The first parameter. */
    public final Param1 く;

    /** The second parameter. */
    public final Param2 巜;

    /**
     * @param param1
     * @param param2
     */
    Binary(Param1 param1, Param2 param2) {
        this.く = param1;
        this.巜 = param2;
    }
}
