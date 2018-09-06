/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static java.util.concurrent.TimeUnit.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2018/09/06 23:36:57
 */
public interface Storable<Self> {

    /**
     * <p>
     * Restore all properties from persistence domain.
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
     * Store all properties to persistence domain.
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

    /**
     * Make this {@link Storable} save automatically.
     */
    default Self storeAuto() {
        auto(this, Model.of(this), this);
        return (Self) this;
    }

    /**
     * Search autosavable {@link Variable} property.
     * 
     * @param root
     * @param model
     * @param object
     */
    private void auto(Storable root, Model<Object> model, Object object) {
        for (Property property : model.properties()) {
            if (property.isAttribute()) {
                model.observe(object, property).diff().debounce(3, SECONDS).to(root::store);
            } else {
                auto(root, property.model, model.get(object, property));
            }
        }
    }
}
