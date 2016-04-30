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
 * @version 2016/03/30 1:11:45
 */
@SuppressWarnings("unchecked")
class ListModel extends Model {

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
     * @see kiss.model.Model#property(java.lang.String)
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
     * @see kiss.model.Model#isCollection()
     */
    @Override
    public boolean isCollection() {
        return true;
    }

    /**
     * @see kiss.model.Model#get(java.lang.Object, kiss.model.Property)
     */
    @Override
    public Object get(Object object, Property property) {
        return ((List) object).get(Integer.valueOf(property.name));
    }

    /**
     * @see kiss.model.Model#set(java.lang.Object, kiss.model.Property, java.lang.Object)
     */
    @Override
    public void set(Object object, Property property, Object propertyValue) {
        List list = (List) object;
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
     * @see kiss.model.Model#walk(java.lang.Object, kiss.model.PropertyWalker)
     */
    @Override
    public void walk(Object object, Consumer<Ⅲ<Model, Property, Object>> walker) {
        if (object != null && walker != null) {
            // We must use extended for loop because the sequential access is not efficient for some
            // List implementation.
            int counter = 0;

            for (Object value : (List) object) {
                walker.accept(I.pair(this, new Property(itemModel, String.valueOf(counter++)), value));
            }
        }
    }
}
