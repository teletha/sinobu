/*
 * Copyright (C) 2018 Nameless Production Committee
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
 * @version 2017/05/02 15:28:23
 */
public interface WiseSupplier<Return> extends Supplier<Return> {

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
     * <p>
     * Convert to {@link WiseFunction}.
     * </p>
     * 
     * @return A converted {@link WiseFunction}.
     */
    default WiseFunction asFunction() {
        return p -> GET();
    }
}
