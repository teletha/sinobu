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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import kiss.model.Model;

/**
 * @version 2017/04/02 21:44:44
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
            I.read(new BufferedReader(new InputStreamReader(new FileInputStream(new File(locate())), StandardCharsets.UTF_8)), this);
        } catch (Throwable e) {
            // ignore error
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
            File file = new File(locate());

            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            I.write(this, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(locate())), StandardCharsets.UTF_8)));
        } catch (Throwable e) {
            // ignore error
        }
        return (Self) this;
    }

    /**
     * <p>
     * Specify the identifier of persistence location.
     * </p>
     * 
     * @return An identifier of persistence location.
     */
    default String locate() {
        return ".preferences/" + Model.of(this).type.getName() + ".json";
    }
}
