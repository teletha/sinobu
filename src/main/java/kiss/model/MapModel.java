/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import kiss.I;
import kiss.WiseTriConsumer;

/**
 * @version 2016/09/09 16:51:16
 */
class MapModel<K, V> extends Model<Map<K, V>> {

    /** The prameterized key of this model. */
    private final Model<K> key;

    /** The prameterized value of this model. */
    private final Model<V> value;

    /**
     * Create MapModel instance.
     * 
     * @param clazz A raw class.
     * @param types A list of parameter classes.
     * @throws IllegalArgumentException If the map model has no parameter or invalid parameter.
     */
    MapModel(Class clazz, Type[] types, Type base) {
        super(clazz);

        if (types.length == 0) {
            types = new Type[] {Object.class, Object.class};
        }
        this.key = Model.of(types[0], base);
        this.value = Model.of(types[1], base);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property property(String propertyIName) {
        return !key.attribute ? null : new Property(value, propertyIName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(Map object, Property property) {
        if (!key.attribute) {
            return super.get(object, property);
        } else {
            return object.get(I.transform(property.name, key.type));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(Map object, Property property, Object propertyValue) {
        if (!key.attribute) {
            super.set(object, property, propertyValue);
        } else {
            object.put(I.transform(property.name, key.type), propertyValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(Map<K, V> object, WiseTriConsumer<Model<Map<K, V>>, Property, Object> walker) {
        if (!key.attribute) {
            super.walk(object, walker);
        } else {
            if (object != null) {
                for (Entry<K, V> entry : object.entrySet()) {
                    walker.accept(this, new Property(value, I.transform(entry.getKey(), String.class)), entry.getValue());
                }
            }
        }
    }
}
