/*
 * Copyright (C) 2010 Nameless Production Committee.
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ezbean.I;

/**
 * @version 2009/12/30 22:35:22
 */
class MapModel extends Model<Map> {

    /** The prameterized key of this model. */
    private final Model key;

    /** The prameterized value of this model. */
    private final Model value;

    /**
     * Create MapModel instance.
     * 
     * @param type
     * @throws IllegalArgumentException If the map model has no parameter or invalid parameter.
     */
    MapModel(ParameterizedType parameterizedType, Class base) {
        super(Map.class);

        // verify model
        Type[] types = parameterizedType.getActualTypeArguments();

        if (types.length != 2) {
            throw new IllegalArgumentException("MapModel must have only two parameters.");
        }

        this.key = Model.load(types[0], base);
        this.value = Model.load(types[1], base);
    }

    /**
     * @see ezbean.model.Model#getProperty(java.lang.String)
     */
    @Override
    public Property getProperty(String propertyIName) {
        return (key.getCodec() == null) ? null : new Property(value, propertyIName);
    }

    /**
     * @see ezbean.model.Model#isCollection()
     */
    @Override
    public boolean isCollection() {
        return key.getCodec() != null;
    }

    /**
     * @see ezbean.model.Model#get(java.lang.Object, ezbean.model.Property)
     */
    @Override
    public Object get(Map object, Property property) {
        if (key.getCodec() == null) {
            return super.get(object, property);
        } else {
            return object.get(I.transform(property.name, key.type));
        }
    }

    /**
     * @see ezbean.model.Model#set(java.lang.Object, ezbean.model.Property, java.lang.Object)
     */
    @Override
    public void set(Map object, Property property, Object propertyValue) {
        if (key.getCodec() == null) {
            super.set(object, property, propertyValue);
        } else {
            object.put(I.transform(property.name, key.type), propertyValue);
        }
    }

    /**
     * @see ezbean.model.Model#walk(java.lang.Object, ezbean.model.PropertyWalker)
     */
    @Override
    public void walk(Map object, PropertyWalker walker) {
        if (key.getCodec() == null) {
            super.walk(object, walker);
        } else {
            for (Entry<Object, Object> entry : (Set<Entry<Object, Object>>) object.entrySet()) {
                walker.walk(this, new Property(value, I.transform(entry.getKey(), String.class)), entry.getValue());
            }
        }
    }
}
