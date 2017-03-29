/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

/**
 * @version 2017/03/13 9:27:11
 */
public interface UsefulRunnable extends Runnable {

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
}
