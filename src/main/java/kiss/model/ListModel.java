/*
 * Copyright (C) 2019 Nameless Production Committee
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

import kiss.WiseTriConsumer;

/**
 * {@link Model} for general {@link List}.
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
    public Property property(String name) {
        try {
            return new Property(itemModel, Integer.valueOf(name).toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(List object, Property property) {
        int index = Integer.valueOf(property.name);

        return object.size() <= index ? null : object.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(List object, Property property, Object value) {
        List list = object;
        int id = Integer.valueOf(property.name);

        if (list.size() <= id) {
            int o = id - list.size() + 1;
            for (int i = 0; i < o; i++) {
                list.add(null);
            }
        }
        list.set(id, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(List<V> object, WiseTriConsumer<Model<List<V>>, Property, Object> walker) {
        if (object != null && walker != null) {
            // We must use extended for loop because the sequential access is not efficient for some
            // List implementation.
            int counter = 0;

            for (V value : object) {
                walker.accept(this, new Property(itemModel, String.valueOf(counter++)), value);
            }
        }
    }
}
