/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link Model} for general {@link Map}.
 */
class MapModel<K, V> extends Model<Map<K, V>> {

    /** The parameterized key of this model. */
    private final Model key;

    /** The parameterized value of this model. */
    private final Model value;

    /**
     * Special model for {@link JSON} writing.
     * 
     * @param o
     */
    MapModel(Object o) {
        super(o instanceof LinkedHashMap ? List.class : Map.class);
        key = null;
        value = Model.of(String.class);
    }

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
            for (Entry e : object.entrySet()) {
                Model sub = key == null && e.getValue() instanceof Map ? new MapModel(e.getValue()) : value;
                walker.accept(this, new Property(sub, I.transform(e.getKey(), String.class), null), e.getValue());
            }
        }
    }
}