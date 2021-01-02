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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.function.Consumer;

/**
 * <p>
 * Provides a mechanism for receiving push-based notifications.
 * </p>
 * <p>
 * After an {@link Observer} calls an {@link Signal#to(Observer)} method, the {@link Signal} calls
 * the {@link #accept(Object)} method to provide notifications. A well-behaved {@link Signal} will
 * call an {@link #complete()} closure exactly once or the Observer's {@link #error(Throwable)}
 * closure exactly once.
 * </p>
 * 
 * @param <V> The object that provides notification information.
 * @version 2017/05/26 21:42:55
 */
public interface Observer<V> extends Consumer<V> {

    /**
     * <p>
     * Notifies the observer that the provider has finished sending push-based notifications.
     * </p>
     * <p>
     * The {@link Signal} will not call this closure if it calls {@link #error(Throwable)}.
     * </p>
     */
    public default void complete() {
        // do nothing
    }

    /**
     * <p>
     * Notifies the observer that the provider has experienced an error condition.
     * </p>
     * <p>
     * If the {@link Signal} calls this closure, it will not thereafter call {@link #accept(Object)}
     * or {@link #complete()}.
     * </p>
     * 
     * @param e An object that provides additional information about the error.
     */
    public default void error(Throwable e) {
        UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        if (handler != null) {
            handler.uncaughtException(Thread.currentThread(), e);
        }
        throw I.quiet(e);
    }
}