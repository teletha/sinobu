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
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import kiss.I;
import kiss.Ⅲ;

/**
 * @version 2010/01/10 17:10:23
 */
@SuppressWarnings("unchecked")
class MapModel extends Model {

    /** The prameterized key of this model. */
    private final Model key;

    /** The prameterized value of this model. */
    private final Model value;

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
     * @see kiss.model.Model#property(java.lang.String)
     */
    @Override
    public Property property(String propertyIName) {
        return (key.decoder() == null) ? null : new Property(value, propertyIName);
    }

    /**
     * @see kiss.model.Model#isCollection()
     */
    @Override
    public boolean isCollection() {
        return key.decoder() != null;
    }

    /**
     * @see kiss.model.Model#get(java.lang.Object, kiss.model.Property)
     */
    @Override
    public Object get(Object object, Property property) {
        if (key.decoder() == null) {
            return super.get(object, property);
        } else {
            return ((Map) object).get(I.transform(property.name, key.type));
        }
    }

    /**
     * @see kiss.model.Model#set(java.lang.Object, kiss.model.Property, java.lang.Object)
     */
    @Override
    public void set(Object object, Property property, Object propertyValue) {
        if (key.decoder() == null) {
            super.set(object, property, propertyValue);
        } else {
            ((Map) object).put(I.transform(property.name, key.type), propertyValue);
        }
    }

    /**
     * @see kiss.model.Model#walk(java.lang.Object, kiss.model.PropertyWalker)
     */
    @Override
    public void walk(Object object, Consumer<Ⅲ<Model, Property, Object>> walker) {
        if (key.decoder() == null) {
            super.walk(object, walker);
        } else {
            for (Entry entry : ((Map<?, ?>) object).entrySet()) {
                walker.accept(I.pair(this, new Property(value, I.transform(entry.getKey(), String.class)), entry.getValue()));
            }
        }
    }
}
