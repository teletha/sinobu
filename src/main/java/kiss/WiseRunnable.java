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

import java.io.Serializable;

/**
 * @version 2018/04/02 8:35:30
 */
public interface WiseRunnable extends Runnable, Serializable {

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
     * <p>
     * Convert to {@link WiseConsumer}.
     * </p>
     * 
     * @return A converted {@link WiseConsumer}.
     */
    default <P> WiseConsumer<P> asConsumer() {
        return p -> RUN();
    }

    /**
     * Returns a composed {@link WiseRunnable} that performs, in sequence, this operation followed by
     * the after operation. If performing either operation throws an exception, it is relayed to the
     * caller of the composed operation. If performing this operation throws an exception, the after
     * operation will not be performed.
     * 
     * @param after The operation to perform after this operation.
     * @return A composed {@link WiseRunnable} that performs in sequence this operation followed by the
     *         after operation.
     */
    default WiseRunnable then(Runnable after) {
        return () -> {
            run();
            if (after != null) after.run();
        };
    }
}
