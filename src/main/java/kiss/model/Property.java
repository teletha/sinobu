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

import java.lang.reflect.Method;

/**
 * <p>
 * This class represents a property of object model.
 * </p>
 * <p>
 * Note: Care should be exercised if {@link Property} objects are used as keys in a
 * {@link java.util.SortedMap} or elements in a {@link java.util.SortedSet} since Property's natural
 * ordering is inconsistent with equals. See {@link Comparable}, {@link java.util.SortedMap} or
 * {@link java.util.SortedSet} for more information.
 * </p>
 * 
 * @version 2009/07/17 15:03:16
 */
public class Property implements Comparable<Property> {

    /** The assosiated object model with this property. */
    public final Model model;

    /** The human readable identifier of this property. */
    public final String name;

    /** The actual accessor methods. */
    Method[] accessors;

    /** The transient type of this property. */
    boolean type;

    /**
     * Create a property.
     * 
     * @param model A model that this property belongs to.
     * @param name A property name.
     */
    public Property(Model model, String name) {
        this.model = model;
        this.name = name;
    }

    /**
     * Decide whether this object model can be Attribute or not.
     * 
     * @return A result.
     */
    public boolean isAttribute() {
        return model.codec != null || model.type.isArray();
    }

    /**
     * Check whether this property is transient or not.
     * 
     * @return A result.
     */
    public boolean isTransient() {
        return type;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Property o) {
        // compare type
        if (isAttribute() != o.isAttribute()) {
            return isAttribute() ? -1 : 1;
        }

        // compare name
        return name.compareTo(o.name);
    }
}
