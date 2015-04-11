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

import java.util.function.Function;

/**
 * @version 2015/04/11 9:03:02
 */
public class Unary<Param> {

    /** The first parameter. */
    public final Param a;

    /**
     * @param param
     */
    Unary(Param param) {
        this.a = param;
    }

    /**
     * Create new tuple which replace the first parameter.
     * 
     * @param param New first parameter.
     * @return A created new tuple.
     */
    public <NewParam> Unary<NewParam> a(NewParam param) {
        return I.pair(param);
    }

    /**
     * Create new tuple which calculate the first parameter.
     * 
     * @param calculation A calculation expression.
     * @return A created new tuple.
     */
    public <CalculationResult> Unary<CalculationResult> a(Function<Param, CalculationResult> calculation) {
        return I.pair(calculation.apply(a));
    }
}
