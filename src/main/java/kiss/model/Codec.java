/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Function;

import javafx.util.StringConverter;

import kiss.Extensible;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;

/**
 * <p>
 * Codec for object type conversion.
 * </p>
 * <p>
 * This class provides all-purpose codec for literalization. The class which is provided by the
 * constructor {@link kiss.model.Codec#Codec()} must fit the following two requirements. One is that
 * it have the constructor which has a single {@link String} argument like
 * {@link StringBuilder#StringBuilder(String)}. The other is that the constructor can restore to the
 * original state from a return value of its method {@link #toString()}.
 * </p>
 *
 * @param <T> A target type to decode and encode.
 * @version 2014/03/11 13:52:32
 * @see StringConverter
 */
@SuppressWarnings("unchecked")
@Manageable(lifestyle = Singleton.class)
public class Codec<T> extends StringConverter<T> implements Extensible {

    /** The raw type information for this codec. */
    Class type;

    /** The actual constructer for decode. */
    Constructor<T> constructor;

    /** The actual constructer for decode. */
    Method method;

    /** The encoder function. */
    Function<T, String> encoder;

    /** The decoder function. */
    Function<String, T> decoder;

    /**
     * Exposed constructor for extension.
     */
    protected Codec() {
    }

    /**
     * Encode the source model object to the target model object.
     *
     * @param value A object to encode.
     * @return A encoded object.
     * @throws IllegalArgumentException If the given value is illegal format.
     */
    @Override
    public String toString(T value) {
        if (encoder != null) {
            return encoder.apply(value);
        }
        return value.toString();
    }

    /**
     * Decode the target model object to the source model object.
     *
     * @param value A object to decode.
     * @return A decoded object.
     * @throws IllegalArgumentException If the given value is illegal format.
     */
    @Override
    public T fromString(String value) {
        if (decoder != null) {
            return decoder.apply(value);
        }

        // for enum
        if (type != null) {
            return (T) Enum.valueOf(type, value);
        }

        try {
            if (method != null) {
                return (T) method.invoke(null, value);
            }

            if (constructor != null) {
                // for stringizable class
                return constructor.newInstance(value);
            }

            // for character class
            return (T) (Object) value.charAt(0);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
