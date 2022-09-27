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
import java.util.Map;
import java.util.Map.Entry;

import kiss.I;
import kiss.WiseTriConsumer;

/**
 * {@link Model} for general {@link Map}.
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
    public Property property(String name) {
        return new Property(value, name, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(Map object, Property property) {
        return object.get(I.transform(property.name, key.type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map set(Map object, Property property, Object value) {
        object.put(I.transform(property.name, key.type), value);
        return object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(Map<K, V> object, WiseTriConsumer<Model<Map<K, V>>, Property, Object> walker) {
        if (object != null) {
            for (Entry<K, V> entry : object.entrySet()) {
                walker.accept(this, new Property(value, I.transform(entry.getKey(), String.class), null), entry.getValue());
            }
        }
    }
}