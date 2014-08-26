/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

/**
 * <p>
 * Provides a mechanism for receiving push-based notifications.
 * </p>
 * <p>
 * After an {@link Observer} calls an {@link Events#to(Observer)} method, the {@link Events} calls
 * the {@link #onNext(Object)} method to provide notifications. A well-behaved {@link Events} will
 * call an {@link #onCompleted()} closure exactly once or the Observer's {@link #onError(Throwable)}
 * closure exactly once.
 * </p>
 * 
 * @param <V> The object that provides notification information.
 */
public interface Observer<V> {

    /**
     * <p>
     * Notifies the observer that the provider has finished sending push-based notifications.
     * </p>
     * <p>
     * The {@link Events} will not call this closure if it calls {@link #onError(Throwable)}.
     * </p>
     */
    public default void onCompleted() {
        // do nothing
    }

    /**
     * <p>
     * Notifies the observer that the provider has experienced an error condition.
     * </p>
     * <p>
     * If the {@link Events} calls this closure, it will not thereafter call
     * {@link #onNext(Object)} or {@link #onCompleted()}.
     * </p>
     * 
     * @param error An object that provides additional information about the error.
     */
    public default void onError(Throwable error) {
        // do nothing
    }

    /**
     * <p>
     * Provides the observer with new data.
     * </p>
     * <p>
     * The {@link Events} calls this closure 1 or more times, unless it calls <code>onError</code>
     * in which case this closure may never be called.
     * </p>
     * <p>
     * The {@link Events} will not call this closure again after it calls either
     * {@link #onCompleted()} or {@link #onError(Throwable)}.
     * </p>
     * 
     * @param value The current notification information.
     */
    public default void onNext(V value) {
        // do nothing
    }
}