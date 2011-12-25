/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.model;

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

    /** The machine readable identifier of this property. */
    int id;

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
    Property(Model model, String name) {
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
