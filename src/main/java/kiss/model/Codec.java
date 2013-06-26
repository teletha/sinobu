/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.lang.reflect.Constructor;

import kiss.Extensible;
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
 * @version 2009/12/30 22:11:00
 */
@Manageable(lifestyle = Singleton.class)
public class Codec<T> implements Extensible {

    /** The raw type information for this codec. */
    private final Class type;

    /** The actual constructer for decode. */
    private Constructor<T> constructor;

    /** The construct mode. <code>true</code> is String, <code>false</code> is char. */
    private boolean mode = true;

    /**
     * Exposed constructor for extension.
     */
    protected Codec() {
        this(null);
    }

    /**
     * Internal constructor.
     * 
     * @param type A codec type.
     */
    Codec(Class type) {
        this.type = type;

        // convert primitive class to wrapper class
        type = ClassUtil.wrap(type);

        // chech whether the specified class is stringizable or not
        try {
            if (type == Character.class) {
                mode = false;
                constructor = type.getConstructor(char.class);
            } else {
                constructor = type.getConstructor(String.class);
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    /**
     * Encode the source model object to the target model object.
     * 
     * @param value A object to encode.
     * @return A encoded object.
     * @throws IllegalArgumentException If the given value is illegal format.
     */
    public String encode(T value) {
        return value.toString();
    }

    /**
     * Decode the target model object to the source model object.
     * 
     * @param value A object to decode.
     * @return A decoded object.
     * @throws IllegalArgumentException If the given value is illegal format.
     */
    public T decode(String value) {
        // for enum
        if (type != null && type.isEnum()) {
            return (T) Enum.valueOf(type, value);
        }

        try {
            if (mode) {
                // for stringizable class
                return constructor.newInstance(value);
            } else {
                // for character class
                return constructor.newInstance(value.charAt(0));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
