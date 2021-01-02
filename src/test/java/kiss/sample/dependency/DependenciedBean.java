/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.dependency;

import java.io.Serializable;

/**
 * @version 2009/07/01 18:05:59
 */
@SuppressWarnings("serial")
public class DependenciedBean implements Serializable {

    private final NoDependencySingleton singleton;

    private String name;

    public DependenciedBean(NoDependencySingleton singleton) {
        this.singleton = singleton;
    }

    /**
     * Get the name property of this {@link DependenciedBean}.
     * 
     * @return The name property.
     */
    public String getName() {
        return name + singleton;
    }

    /**
     * Set the name property of this {@link DependenciedBean}.
     * 
     * @param name The name value to set.
     */
    public void setName(String name) {
        this.name = name;
    }

}