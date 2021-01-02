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

/**
 * Codec for interconversion of object and {@link String}.
 * <p>
 * This class provides all-purpose codec for literalization and provids the default encoder which
 * uses {@link String#valueOf(Object)}.
 *
 * @param <M> A model class to decode and encode.
 * @see Encoder
 */
public interface Decoder<M> extends Extensible {

    /**
     * Decode the model object from {@link String}.
     * 
     * @param value A {@link String} representation to decode.
     * @return A decoded object.
     * @throws IllegalArgumentException If the given value is illegal format.
     */
    public M decode(String value);
}