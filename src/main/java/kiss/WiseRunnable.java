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

public interface WiseRunnable extends Runnable, Widen<WiseConsumer, Void> {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @throws Throwable A sneaky exception for lambda.
     */
    void RUN() throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default void run() {
        try {
            RUN();
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Void invoke(Object... params) {
        run();
        return null;
    }
}
