/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import java.lang.reflect.Type;
import java.util.List;

import kiss.I;

/**
 * {@link Model} for general {@link List}.
 */
class GenericModel<V> extends Model<V> {

    /** The prameterized item of this model. */
    private final List<Model> models;

    /**
     * Create ListModel instance.
     * 
     * @param clazz A raw class.
     * @param types A list of parameter classes.
     * @throws IllegalArgumentException If the list model has no parameter or invalid parameter.
     */
    GenericModel(Class clazz, Type[] types, Type base) {
        super(clazz);

        models = I.signal(types).map(t -> Model.of(t, base)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property property(String name) {
        Property property = properties.get(name);
        System.out.println(name);
        return super.property(name);
    }
}