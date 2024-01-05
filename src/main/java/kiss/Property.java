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

import static java.lang.reflect.Modifier.TRANSIENT;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;

/**
 * This class represents a property of object model.
 * <p>
 * Note: Care should be exercised if {@link Property} objects are used as keys in a
 * {@link java.util.SortedMap} or elements in a {@link java.util.SortedSet} since Property's natural
 * ordering is inconsistent with equals. See {@link Comparable}, {@link java.util.SortedMap} or
 * {@link java.util.SortedSet} for more information.
 */
public class Property {

    /** The associated object model with this {@link Property}. */
    public final Model model;

    /** The human-readable identifier of this {@link Property}. */
    public final String name;

    /** Whether this {@link Property} is transient or not. */
    public final boolean transitory;

    /** The property accessor. */
    public WiseFunction getter;

    /** The property accessor. */
    public WiseBiFunction setter;

    /** The property observer. */
    WiseFunction<Object, Signal> observer;

    /**
     * Create a property.
     * 
     * @param model A model that this property belongs to.
     * @param name A property name.
     * @param mem An associated member (filed or method).
     */
    public <M extends AccessibleObject & Member> Property(Model model, String name, M mem) {
        if (mem != null) {
            Managed managed = mem.getAnnotation(Managed.class);
            if (managed != null && !managed.name().isEmpty()) {
                name = managed.name();
            }
        }

        this.model = model;
        this.name = name;
        this.transitory = mem != null && (mem.getModifiers() & TRANSIENT) != 0;
    }
}