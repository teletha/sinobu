/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

/**
 * @version 2018/04/02 8:35:09
 */
public interface WiseTriConsumer<Param1, Param2, Param3>
        extends
        Narrow<WiseBiConsumer<Param2, Param3>, Param1, WiseBiConsumer<Param1, Param2>, Param3, WiseTriConsumer<Param1, Param2, Param3>> {

    /**
     * Internal API.
     *
     * @param param1 The input argument
     * @param param2 The input argument
     * @param param3 The input argument
     */
    void ACCEPT(Param1 param1, Param2 param2, Param3 param3) throws Throwable;

    /**
     * Performs this operation on the given argument.
     *
     * @param param1 The input argument
     * @param param2 The input argument
     * @param param3 The input argument
     */
    default void accept(Param1 param1, Param2 param2, Param3 param3) {
        try {
            ACCEPT(param1, param2, param3);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Void invoke(Object... params) {
        accept((Param1) params[0], (Param2) params[1], (Param3) params[2]);
        return null;
    }
}
