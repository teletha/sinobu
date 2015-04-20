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
    public final Param1 a;

    /** The second parameter. */
    public final Param2 e;

    /**
     * @param param1
     * @param param2
     */
    public Binary(Param1 param1, Param2 param2) {
        this.a = param1;
        this.e = param2;
    }

    /**
     * Create new tuple which replace the first parameter.
     * 
     * @param param New first parameter.
     * @return A created new tuple.
     */
    public <NewParam> Binary<NewParam, Param2> a(NewParam param) {
        return I.pair(param, e);
    }

    /**
     * Create new tuple which replace the second parameter.
     * 
     * @param param New second parameter.
     * @return A created new tuple.
     */
    public <NewParam> Binary<Param1, NewParam> e(NewParam param) {
        return I.pair(a, param);
    }

    /**
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ternary<AdditionalParam, Param1, Param2> à(AdditionalParam param) {
        return I.pair(param, a, e);
    }

    /**
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ternary<Param1, AdditionalParam, Param2> è(AdditionalParam param) {
        return I.pair(a, param, e);
    }

    /**
     * Create new tuple which add the third parameter.
     * 
     * @param param New third parameter.
     * @return A created new tuple.
     */
    public <AdditionalParam> Ternary<Param1, Param2, AdditionalParam> ò(AdditionalParam param) {
        return I.pair(a, e, param);
    }
}
