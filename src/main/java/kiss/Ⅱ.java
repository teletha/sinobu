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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @version 2016/03/26 21:15:44
 */
public class Ⅱ<Param1, Param2> {

    /** The first parameter. */
    public final Param1 ⅰ;

    /** The second parameter. */
    public final Param2 ⅱ;

    /**
     * @param param1
     * @param param2
     */
    public Ⅱ(Param1 param1, Param2 param2) {
        this.ⅰ = param1;
        this.ⅱ = param2;
    }

    /**
     * <p>
     * The with statement extends the scope chain for a statement.
     * </p>
     * 
     * @param environment
     */
    public void with(BiConsumer<Param1, Param2> environment) {
        if (environment != null) {
            environment.accept(ⅰ, ⅱ);
        }
    }

    /**
     * <p>
     * The with statement extends the scope chain for a statement.
     * </p>
     * 
     * @param environment
     */
    public void with(Function<Param1, Consumer<Param2>> environment) {
        if (environment != null) {
            environment.apply(ⅰ).accept(ⅱ);
        }
    }

    /**
     * Create new tuple which replace the first parameter.
     * 
     * @param param New first parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ⅱ<NewParam, Param2> ⅰ(NewParam param) {
        return I.pair(param, ⅱ);
    }

    /**
     * Create new tuple which replace the second parameter.
     * 
     * @param param New second parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ⅱ<Param1, NewParam> ⅱ(NewParam param) {
        return I.pair(ⅰ, param);
    }

    /**
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ⅲ<AdditionalParam, Param1, Param2> add1(AdditionalParam param) {
        return I.pair(param, ⅰ, ⅱ);
    }

    /**
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ⅲ<Param1, AdditionalParam, Param2> add2(AdditionalParam param) {
        return I.pair(ⅰ, param, ⅱ);
    }

    /**
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ⅲ<Param1, Param2, AdditionalParam> add3(AdditionalParam param) {
        return I.pair(ⅰ, ⅱ, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(ⅰ, ⅱ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ⅱ) {
            Ⅱ other = (Ⅱ) obj;

            return Objects.equals(ⅰ, other.ⅰ) && Objects.equals(ⅱ, other.ⅱ);
        }
        return false;
    }
}
