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

/**
 * @version 2015/09/11 16:39:19
 */
public class Ⅱ<Param1, Param2> {

    /** The first parameter. */
    public final Param1 a;

    /** The second parameter. */
    public final Param2 e;

    /**
     * @param param1
     * @param param2
     */
    public Ⅱ(Param1 param1, Param2 param2) {
        this.a = param1;
        this.e = param2;
    }

    /**
     * Create new tuple which replace the first parameter.
     * 
     * @param param New first parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ⅱ<NewParam, Param2> a(NewParam param) {
        return I.pair(param, e);
    }

    /**
     * Create new tuple which replace the second parameter.
     * 
     * @param param New second parameter.
     * @return A created new tuple.
     */
    public <NewParam> Ⅱ<Param1, NewParam> e(NewParam param) {
        return I.pair(a, param);
    }

    /**
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ⅲ<AdditionalParam, Param1, Param2> à(AdditionalParam param) {
        return I.pair(param, a, e);
    }

    /**
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ⅲ<Param1, AdditionalParam, Param2> è(AdditionalParam param) {
        return I.pair(a, param, e);
    }

    /**
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ⅲ<Param1, Param2, AdditionalParam> ò(AdditionalParam param) {
        return I.pair(a, e, param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(a, e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ⅱ) {
            Ⅱ other = (Ⅱ) obj;

            return Objects.equals(a, other.a) && Objects.equals(e, other.e);
        }
        return false;
    }
}
