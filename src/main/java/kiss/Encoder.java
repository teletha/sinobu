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

/**
 * Codec for interconversion of object and {@link String}.
 * <p>
 * This class provides all-purpose codec for literalization and provides the default encoder which
 * uses {@link String#valueOf(Object)}.
 *
 * @param <M> A model class to decode and encode.
 * @see Decoder
 */
public interface Encoder<M> extends Extensible {

    /**
     * Encode the model object to {@link String}.
     * 
     * @param value A model object to encode.
     * @return An encoded {@link String} representation.
     * @throws IllegalArgumentException If the given value is illegal format.
     */
    String encode(M value);
}