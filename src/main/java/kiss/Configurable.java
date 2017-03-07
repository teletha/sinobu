/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.nio.file.Path;

import kiss.model.Model;

/**
 * @version 2017/03/07 15:03:49
 */
public interface Configurable<Self> {

    /**
     * <p>
     * Restore all settings from persistence domain.
     * </p>
     * 
     * @return Chainable API.
     */
    default Self restore() {
        try {
            I.read(locate(), this);
        } catch (Throwable e) {
            // ignore error
            e.printStackTrace();
        }
        return (Self) this;
    }

    /**
     * <p>
     * Store all settings to persistence domain.
     * </p>
     * 
     * @return Chainable API.
     */
    default Self store() {
        try {
            I.write(this, locate());
        } catch (Throwable e) {
            // ignore error
            e.printStackTrace();
        }
        return (Self) this;
    }

    /**
     * <p>
     * Specify the location of persistence file.
     * </p>
     * 
     * @return A location of persistence file.
     */
    default Path locate() {
        return I.locate("preferences").resolve(Model.of(this).type.getName() + ".json");
    }
}
