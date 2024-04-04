/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.function.Consumer;

/**
 * Provides a mechanism for receiving push-based notifications.
 * 
 * After an {@link Observer} calls an {@link Signal#to(Observer)} method, the {@link Signal} calls
 * the {@link #accept(Object)} method to provide notifications. A well-behaved {@link Signal} will
 * call an {@link #complete()} closure exactly once or the Observer's {@link #error(Throwable)}
 * closure exactly once.
 * 
 * @param <V> The object that provides notification information.
 */
public interface Observer<V> extends Consumer<V> {

    /**
     * Notifies the observer that the provider has finished sending push-based notifications.
     * 
     * The {@link Signal} will not call this closure if it calls {@link #error(Throwable)}.
     */
    default void complete() {
        // do nothing
    }

    /**
     * Notifies the observer that the provider has experienced an error condition.
     * 
     * If the {@link Signal} calls this closure, it will not thereafter call {@link #accept(Object)}
     * or {@link #complete()}.
     * 
     * @param e An object that provides additional information about the error.
     */
    default void error(Throwable e) {
        I.error.accept(e);
        throw I.quiet(e);
    }
}