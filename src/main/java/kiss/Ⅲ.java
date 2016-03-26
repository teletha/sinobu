/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @version 2016/03/26 21:14:51
 */
public class Ⅲ<Param1, Param2, Param3> {

    /** The first parameter. */
    public final Param1 ⅰ;

    /** The second parameter. */
    public final Param2 ⅱ;

    /** The third parameter. */
    public final Param3 ⅲ;

    /**
     * @param param1
     * @param param2
     * @param param3
     */
    Ⅲ(Param1 param1, Param2 param2, Param3 param3) {
        this.ⅰ = param1;
        this.ⅱ = param2;
        this.ⅲ = param3;
    }

    /**
     * <p>
     * The with statement extends the scope chain for a statement.
     * </p>
     * 
     * @param environment
     */
    public void with(Function<Param1, Function<Param2, Consumer<Param3>>> environment) {
        if (environment != null) {
            environment.apply(ⅰ).apply(ⅱ).accept(ⅲ);
        }
    }

    /**
     * Create new tuple which replace the first parameter.
     * 
     * @param param New first parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ⅲ<NewParam, Param2, Param3> a(NewParam param) {
        return I.pair(param, ⅱ, ⅲ);
    }

    /**
     * Create new tuple which calculate the first parameter.
     * 
     * @param calculation A calculation expression.
     * @return A created new tuple.
     */
    public <CalculationResult> Ⅲ<CalculationResult, Param2, Param3> a(Function<Param1, CalculationResult> calculation) {
        return I.pair(calculation.apply(ⅰ), ⅱ, ⅲ);
    }

    /**
     * Create new tuple which replace the second parameter.
     * 
     * @param param New second parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ⅲ<Param1, NewParam, Param3> e(NewParam param) {
        return I.pair(ⅰ, param, ⅲ);
    }

    /**
     * Create new tuple which calculate the second parameter.
     * 
     * @param calculation A calculation expression.
     * @return A created new tuple.
     */
    public <CalculationResult> Ⅲ<Param1, CalculationResult, Param3> e(Function<Param2, CalculationResult> calculation) {
        return I.pair(ⅰ, calculation.apply(ⅱ), ⅲ);
    }

    /**
     * Create new tuple which replace the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ⅲ<Param1, Param2, NewParam> o(NewParam param) {
        return I.pair(ⅰ, ⅱ, param);
    }

    /**
     * Create new tuple which calculate the third parameter.
     * 
     * @param calculation A calculation expression.
     * @return A created new tuple.
     */
    public <CalculationResult> Ⅲ<Param1, Param2, CalculationResult> o(Function<Param3, CalculationResult> calculation) {
        return I.pair(ⅰ, ⅱ, calculation.apply(ⅲ));
    }

    /**
     * Create new tuple which remove the first parameter.
     * 
     * @return A created new tuple.
     */
    public Ⅱ<Param2, Param3> á() {
        return I.pair(ⅱ, ⅲ);
    }

    /**
     * Create new tuple which remove the second parameter.
     * 
     * @return A created new tuple.
     */
    public Ⅱ<Param1, Param3> é() {
        return I.pair(ⅰ, ⅲ);
    }

    /**
     * Create new tuple which remove the third parameter.
     * 
     * @return A created new tuple.
     */
    public Ⅱ<Param1, Param2> ó() {
        return I.pair(ⅰ, ⅱ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(ⅰ, ⅱ, ⅲ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ⅲ) {
            Ⅲ other = (Ⅲ) obj;

            return Objects.equals(ⅰ, other.ⅰ) && Objects.equals(ⅱ, other.ⅱ) && Objects.equals(ⅲ, other.ⅲ);
        }
        return false;
    }
}
