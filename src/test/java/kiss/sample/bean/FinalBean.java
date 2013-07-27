/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.bean;

import kiss.sample.modifier.Final;

/**
 * @version 2011/12/09 20:53:25
 */
public final class FinalBean {

    /** The property. */
    private int property;

    /**
     * Get the property property of this {@link Final}.
     * 
     * @return The property property.
     */
    public int getProperty() {
        return property;
    }

    /**
     * Set the property property of this {@link Final}.
     * 
     * @param property The property value to set.
     */
    public void setProperty(int property) {
        this.property = property;
    }
}
