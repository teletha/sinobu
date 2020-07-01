/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface WiseSupplier<Return> extends Supplier<Return>, Callable<Return>, Wise {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @return A proxy result.
     * @throws Throwable A sneaky exception for lambda.
     */
    Return GET() throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default Return get() {
        try {
            return GET();
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Return call() throws Exception {
        return get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Return invoke(Object... params) {
        return get();
    }
}