/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

/**
 * @version 2015/04/18 21:14:49
 */
public class val<Model, Property> {

    /** The actual model. */
    private final Model model;

    /** The actual value. */
    public final Property value;

    /**
     * @param value
     */
    public val(Model model, Property value) {
        this.model = model;
        this.value = value;
    }

    Property get() {
        return value;
    }

    Model set(Property value) {
        return null;
    }
}
