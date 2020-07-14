/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import static java.lang.reflect.Modifier.*;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import kiss.Signal;
import kiss.WiseBiConsumer;
import kiss.WiseFunction;

/**
 * This class represents a property of object model.
 * <p>
 * Note: Care should be exercised if {@link Property} objects are used as keys in a
 * {@link java.util.SortedMap} or elements in a {@link java.util.SortedSet} since Property's natural
 * ordering is inconsistent with equals. See {@link Comparable}, {@link java.util.SortedMap} or
 * {@link java.util.SortedSet} for more information.
 */
public class Property implements Comparable<Property> {

    /** The assosiated object model with this {@link Property}. */
    public final Model model;

    /** The human readable identifier of this {@link Property}. */
    public final String name;

    /** The flag whether this {@link Property} is transient or not. */
    public final boolean isTransient;

    /** The property accessor. */
    WiseFunction getter;

    /** The property accessor. */
    WiseBiConsumer setter;

    /** The property ovserver. */
    WiseFunction<Object, Signal> observer;

    /**
     * Create a property.
     * 
     * @param T The type which can treat as {@link Property}. (i.e. {@link Field} or {@link Method})
     * @param model A model that this property belongs to.
     * @param name A property name.
     */
    public Property(Model model, String name, Member... elements) {
        this.model = model;
        this.name = name;

        boolean serializable = false;

        for (Member element : elements) {
            if ((element.getModifiers() & TRANSIENT) != 0) {
                serializable = true;
            }
        }
        this.isTransient = serializable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Property o) {
        // compare type
        if (isAttribute() != o.isAttribute()) {
            return isAttribute() ? -1 : 1;
        }

        // compare name
        return name.compareTo(o.name);
    }

    /**
     * Decide whether this object model can be Attribute or not.
     * 
     * @return A result.
     */
    public boolean isAttribute() {
        return model.attribute || model.type.isArray();
    }
}