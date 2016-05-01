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

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import kiss.I;
import kiss.Ⅲ;

/**
 * @version 2016/05/01 9:50:22
 */
class ListModel<V> extends Model<List<V>> {

    /** The prameterized item of this model. */
    private final Model itemModel;

    /**
     * Create ListModel instance.
     * 
     * @param clazz A raw class.
     * @param type A parameter class.
     * @throws IllegalArgumentException If the list model has no parameter or invalid parameter.
     */
    ListModel(Class clazz, Type[] types, Type base) {
        super(clazz);

        itemModel = Model.of(types.length == 0 ? Object.class : types[0], base);
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
    public boolean isCollection() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(List object, Property property) {
        return object.get(Integer.valueOf(property.name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(List object, Property property, Object propertyValue) {
        List list = object;
        int id = Integer.valueOf(property.name);

        if (list.size() <= id) {
            int o = id - list.size() + 1;
            for (int i = 0; i < o; i++) {
                list.add(null);
            }
        }
        list.set(id, propertyValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(List<V> object, Consumer<Ⅲ<Model<List<V>>, Property, Object>> walker) {
        if (object != null && walker != null) {
            // We must use extended for loop because the sequential access is not efficient for some
            // List implementation.
            int counter = 0;

            for (V value : object) {
                walker.accept(I.pair(this, new Property(itemModel, String.valueOf(counter++)), value));
            }
        }
    }
}
