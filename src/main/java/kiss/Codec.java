/*
 * Copyright (C) 2015 Nameless Production Committee
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
 * Codec for interconversion of object and {@link String}.
 * </p>
 * <p>
 * This class provides all-purpose codec for literalization and provids the default encoder which
 * uses {@link String#valueOf(Object)}.
 * </p>
 *
 * @param <M> A model class to decode and encode.
 * @version 2015/10/20 17:56:12
 */
public interface Codec<M> extends Extensible {

    /**
     * <p>
     * Encode the model object to {@link String}.
     * </p>
     * 
     * @param value A model object to encode.
     * @return A encoded {@link String} representation.
     * @throws IllegalArgumentException If the given value is illegal format.
     */
    public default String encode(M value) {
        return value instanceof Enum ? ((Enum) value).name() : String.valueOf(value);
    }

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
