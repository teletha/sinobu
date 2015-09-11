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

import java.util.Objects;
import java.util.function.Function;

/**
 * @version 2015/04/10 1:17:24
 */
public class Ternary<Param1, Param2, Param3> {

    /** The first parameter. */
    public final Param1 a;

    /** The second parameter. */
    public final Param2 e;

    /** The third parameter. */
    public final Param3 o;

    /**
     * @param param1
     * @param param2
     * @param param3
     */
    Ternary(Param1 param1, Param2 param2, Param3 param3) {
        this.a = param1;
        this.e = param2;
        this.o = param3;
    }

    /**
     * Create new tuple which replace the first parameter.
     * 
     * @param param New first parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ternary<NewParam, Param2, Param3> a(NewParam param) {
        return I.pair(param, e, o);
    }

    /**
     * Create new tuple which calculate the first parameter.
     * 
     * @param calculation A calculation expression.
     * @return A created new tuple.
     */
    public <CalculationResult> Ternary<CalculationResult, Param2, Param3> a(Function<Param1, CalculationResult> calculation) {
        return I.pair(calculation.apply(a), e, o);
    }

    /**
     * Create new tuple which replace the second parameter.
     * 
     * @param param New second parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ternary<Param1, NewParam, Param3> e(NewParam param) {
        return I.pair(a, param, o);
    }

    /**
     * Create new tuple which calculate the second parameter.
     * 
     * @param calculation A calculation expression.
     * @return A created new tuple.
     */
    public <CalculationResult> Ternary<Param1, CalculationResult, Param3> e(Function<Param2, CalculationResult> calculation) {
        return I.pair(a, calculation.apply(e), o);
    }

    /**
     * Create new tuple which replace the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ternary<Param1, Param2, NewParam> o(NewParam param) {
        return I.pair(a, e, param);
    }

    /**
     * Create new tuple which calculate the third parameter.
     * 
     * @param calculation A calculation expression.
     * @return A created new tuple.
     */
    public <CalculationResult> Ternary<Param1, Param2, CalculationResult> o(Function<Param3, CalculationResult> calculation) {
        return I.pair(a, e, calculation.apply(o));
    }

    /**
     * Create new tuple which remove the first parameter.
     * 
     * @return A created new tuple.
     */
    public Binary<Param2, Param3> á() {
        return I.pair(e, o);
    }

    /**
     * Create new tuple which remove the second parameter.
     * 
     * @return A created new tuple.
     */
    public Binary<Param1, Param3> é() {
        return I.pair(a, o);
    }

    /**
     * Create new tuple which remove the third parameter.
     * 
     * @return A created new tuple.
     */
    public Binary<Param1, Param2> ó() {
        return I.pair(a, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(a, e, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ternary) {
            Ternary other = (Ternary) obj;

            return Objects.equals(a, other.a) && Objects.equals(e, other.e) && Objects.equals(o, other.o);
        }
        return false;
    }
}
