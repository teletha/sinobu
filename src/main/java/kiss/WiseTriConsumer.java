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

import java.util.function.Supplier;

/**
 * @version 2018/04/02 8:35:09
 */
public interface WiseTriConsumer<Param1, Param2, Param3> {

    /**
     * Performs this operation on the given argument.
     *
     * @param param1 The input argument
     * @param param2 The input argument
     * @param param3 The input argument
     */
    void accept(Param1 param1, Param2 param2, Param3 param3);

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param1 A fixed parameter.
     * @return A partial applied function.
     */
    default WiseBiConsumer<Param2, Param3> with(Param1 param1) {
        return with(Variable.of(param1));
    }

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param1 A fixed parameter.
     * @return A partial applied function.
     */
    default WiseBiConsumer<Param2, Param3> with(Supplier<Param1> param1) {
        return (param2, param3) -> accept(param1.get(), param2, param3);
    }
}
