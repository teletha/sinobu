/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.bean;

import java.util.List;
import java.util.Map;

/**
 * DOCUMENT.
 * 
 * @version 2008/07/30 10:16:56
 */
public class NestedCollection {

    /** The complex property. */
    private Map<String, List<Person>> nest;

    /**
     * Get the nest property of this {@link NestedCollection}.
     * 
     * @return The nest property.
     */
    public Map<String, List<Person>> getNest() {
        return nest;
    }

    /**
     * Set the nest property of this {@link NestedCollection}.
     * 
     * @param nest The nest value to set.
     */
    public void setNest(Map<String, List<Person>> nest) {
        this.nest = nest;
    }
}
