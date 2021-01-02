/*
 * Copyright (C) 2021 Nameless Production Committee
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
     * {@inheritDoc}
     */
    @Override
    default Return get() {
        try {
            return call();
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Return invoke(Object... params) {
        return get();
    }
}