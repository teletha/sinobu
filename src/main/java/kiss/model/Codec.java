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
 * constructor {@link Codec#Codec(Class)} must fit the following two requirements. One is that it
 * have the constructor which has a single {@link String} argument like
 * {@link StringBuilder#StringBuilder(String)}. The other is that the constructor can restore to the
 * original state from a return value of its method {@link #toString()}.
 * </p>
 * 
 * @param <T> A target type to decode and encode.
 * @see StringConverter
 * @version 2014/03/10 23:23:02
 */
@Manageable(lifestyle = Singleton.class)
public class Codec<T> extends StringConverter<T> implements Extensible {

    /** The raw type information for this codec. */
    private Class type;

    /** The actual constructer for decode. */
    private Constructor<T> constructor;

    /** The encoder function. */
    private Function<T, String> encoder;

    /** The decoder function. */
    private Function<String, T> decoder;

    /**
     * Exposed constructor for extension.
     */
    protected Codec() {
        this(null, null);
    }

    /**
     * Internal constructor.
     * 
     * @param type A codec type.
     */
    Codec(Class type) {
        this.type = type;

        // check whether the specified class is stringizable or not
        try {
            constructor = ClassUtil.wrap(type).getConstructor(String.class);
        } catch (Exception e) {
            // do nothing
        }
    }

    Codec(Function<String, T> decoder, Function<T, String> encoder) {
        this.decoder = decoder;
        this.encoder = encoder;
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
        } else {
            return value.toString();
        }
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
        } else {
            // for enum
            if (type != null && type.isEnum()) {
                return (T) Enum.valueOf(type, value);
            }

            try {
                if (constructor != null) {
                    // for stringizable class
                    return constructor.newInstance(value);
                } else {
                    // for character class
                    return (T) (Object) value.charAt(0);
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }
}
