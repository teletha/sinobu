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

/**
 * <p>
 * Codec for interconversion of object and {@link String}.
 * </p>
 * <p>
 * This class provides all-purpose codec for literalization and provids the default encoder which
 * uses {@link String#valueOf(Object)}.
 * </p>
 *
 * @param <M> A model class to decode and encode.
 * @see Encoder
 * @version 2016/01/20 10:39:37
 */
public interface Decoder<M> extends Extensible {

    /**
     * <p>
     * Decode the model object from {@link String}.
     * </p>
     * 
     * @param value A {@link String} representation to decode.
     * @return A decoded object.
     * @throws IllegalArgumentException If the given value is illegal format.
     */
    public M decode(String value);
}
