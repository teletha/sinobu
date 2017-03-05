/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

/**
 * @version 2017/02/08 10:06:56
 */
public interface ThrowableTriFunction<Param1, Param2, Param3, Return> extends EnhancedLambda {
    /**
     * Applies this function to the given argument.
     *
     * @param param The function argument.
     * @return The function result.
     * @throws Throwable The execution error.
     */
    Return APPLY(Param1 param1, Param2 param2, Param3 param3) throws Throwable;

    /**
     * Applies this function to the given argument.
     *
     * @param param The function argument.
     * @return The function result.
     * @throws Throwable The execution error.
     */
    default Return apply(Param1 param1, Param2 param2, Param3 param3) {
        try {
            return APPLY(param1, param2, param3);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }
}
