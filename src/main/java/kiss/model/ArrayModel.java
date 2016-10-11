/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.lang.reflect.Array;
import java.util.function.Consumer;

import kiss.I;

/**
 * @version 2016/10/11 11:01:24
 */
class ArrayModel extends Model {

    /** The prameterized item of this model. */
    private final Model itemModel;

    /**
     * Create ListModel instance.
     * 
     * @param clazz A raw class.
     * @throws IllegalArgumentException If the list model has no parameter or invalid parameter.
     */
    ArrayModel(Class clazz) {
        super(clazz);

        itemModel = Model.of(clazz.getComponentType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property property(String propertyIName) {
        try {
            return new Property(itemModel, propertyIName);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(Object array, Property property) {
        return Array.get(array, Integer.valueOf(property.name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(Object array, Property property, Object propertyValue) {
        int id = Integer.valueOf(property.name);

        if (id < Array.getLength(array)) {
            Array.set(array, id, propertyValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(Object array, Consumer walker) {
        if (array != null && walker != null) {
            // We must use extended for loop because the sequential access is not efficient for some
            // List implementation.
            int counter = 0;
            int size = Array.getLength(array);

            for (int i = 0; i < size; i++) {
                walker.accept(I.pair(this, new Property(itemModel, String.valueOf(counter++)), Array.get(array, counter)));
            }
        }
    }
}
