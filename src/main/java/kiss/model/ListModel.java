/*
 * Copyright (C) 2022 The SINOBU Development Team
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
     * @param types A list of parameter classes.
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
        return new Property(itemModel, name, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(List object, Property property) {
        try {
            int index = Integer.valueOf(property.name);

            return object.size() <= index ? null : object.get(index);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List set(List object, Property property, Object value) {
        int id = Integer.valueOf(property.name);

        if (object.size() <= id) {
            int o = id - object.size() + 1;
            for (int i = 0; i < o; i++) {
                object.add(null);
            }
        }
        object.set(id, value);

        return object;
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
                walker.accept(this, new Property(itemModel, String.valueOf(counter++), null), value);
            }
        }
    }
}