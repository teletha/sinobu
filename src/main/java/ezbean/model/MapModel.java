/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.model;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ezbean.I;
import ezbean.PropertyWalker;

/**
 * @version 2010/01/10 17:10:23
 */
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

        this.key = Model.load(types[0], base);
        this.value = Model.load(types[1], base);
    }

    /**
     * @see ezbean.model.Model#getProperty(java.lang.String)
     */
    @Override
    public Property getProperty(String propertyIName) {
        return (key.codec == null) ? null : new Property(value, propertyIName);
    }

    /**
     * @see ezbean.model.Model#isCollection()
     */
    @Override
    public boolean isCollection() {
        return key.codec != null;
    }

    /**
     * @see ezbean.model.Model#get(java.lang.Object, ezbean.model.Property)
     */
    @Override
    public Object get(Object object, Property property) {
        if (key.codec == null) {
            return super.get(object, property);
        } else {
            return ((Map) object).get(I.transform(property.name, key.type));
        }
    }

    /**
     * @see ezbean.model.Model#set(java.lang.Object, ezbean.model.Property, java.lang.Object)
     */
    @Override
    public void set(Object object, Property property, Object propertyValue) {
        if (key.codec == null) {
            super.set(object, property, propertyValue);
        } else {
            ((Map) object).put(I.transform(property.name, key.type), propertyValue);
        }
    }

    /**
     * @see ezbean.model.Model#walk(java.lang.Object, ezbean.PropertyWalker)
     */
    @Override
    public void walk(Object object, PropertyWalker walker) {
        if (key.codec == null) {
            super.walk(object, walker);
        } else {
            for (Entry<Object, Object> entry : (Set<Entry<Object, Object>>) ((Map) object).entrySet()) {
                walker.walk(this, new Property(value, I.transform(entry.getKey(), String.class)), entry.getValue());
            }
        }
    }
}
