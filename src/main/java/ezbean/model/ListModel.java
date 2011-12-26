/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.model;

import java.lang.reflect.Type;
import java.util.List;

import ezbean.PropertyWalker;

/**
 * @version 2009/07/22 23:37:56
 */
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
    ListModel(Class clazz, Type type, Type base) {
        super(clazz);

        itemModel = Model.load(type, base);
    }

    /**
     * @see ezbean.model.Model#getProperty(java.lang.String)
     */
    @Override
    public Property getProperty(String propertyIName) {
        try {
            return new Property(itemModel, propertyIName);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @see ezbean.model.Model#isCollection()
     */
    @Override
    public boolean isCollection() {
        return true;
    }

    /**
     * @see ezbean.model.Model#get(java.lang.Object, ezbean.model.Property)
     */
    @Override
    public Object get(Object object, Property property) {
        return ((List) object).get(Integer.valueOf(property.name));
    }

    /**
     * @see ezbean.model.Model#set(java.lang.Object, ezbean.model.Property, java.lang.Object)
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
     * @see ezbean.model.Model#walk(java.lang.Object, ezbean.PropertyWalker)
     */
    @Override
    public void walk(Object object, PropertyWalker walker) {
        if (object != null && walker != null) {
            // We must use extended for loop because the sequential access is not efficient for some
            // List implementation.
            int counter = 0;

            for (Object value : (List) object) {
                walker.walk(this, new Property(itemModel, String.valueOf(counter++)), value);
            }
        }
    }
}
