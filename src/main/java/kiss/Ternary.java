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
 * @version 2015/04/10 1:17:24
 */
public class Ternary<Param1, Param2, Param3> {

    /** The first parameter. */
    public final Param1 く;

    /** The second parameter. */
    public final Param2 巜;

    /** The third parameter. */
    public final Param3 巛;

    /**
     * @param param1
     * @param param2
     * @param param3
     */
    Ternary(Param1 param1, Param2 param2, Param3 param3) {
        this.く = param1;
        this.巜 = param2;
        this.巛 = param3;
    }

    /**
     * Create new tuple which replace the first parameter.
     * 
     * @param param New first parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ternary<NewParam, Param2, Param3> く(NewParam param) {
        return I.pair(param, 巜, 巛);
    }

    /**
     * Create new tuple which replace the second parameter.
     * 
     * @param param New second parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ternary<Param1, NewParam, Param3> 巜(NewParam param) {
        return I.pair(く, param, 巛);
    }

    /**
     * Create new tuple which replace the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ternary<Param1, Param2, NewParam> 巛(NewParam param) {
        return I.pair(く, 巜, param);
    }
}
