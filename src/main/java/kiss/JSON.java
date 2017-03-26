/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2017/03/26 12:53:30
 */
public class JSON {

    /** The root object. */
    private final Object root;

    /**
     * Hide constructor.
     * 
     * @param root A root json object.
     */
    JSON(Object root) {
        this.root = root;
    }

    public Events<String> find(String expression) {
        return find(expression, String.class);
    }

    public <M> Events<M> find(String expression, Class<M> type) {
        return select(expression).map(v -> v.to(type));
    }

    private Events<JSON> select(String expression) {
        Events<Object> current = Events.from(root);

        for (String name : expression.split("\\.")) {
            current = current.flatMap(v -> {
                if (v instanceof Map) {
                    int i = name.lastIndexOf('[');
                    String main = i == -1 ? name : name.substring(0, i);
                    String sub = i == -1 ? null : name.substring(i + 1, name.length() - 1);
                    Object value = ((Map) v).get(main);

                    if (value instanceof ScriptObjectMirror) {
                        ScriptObjectMirror m = (ScriptObjectMirror) value;

                        if (sub != null) {
                            return Events.from(m.get(sub));
                        } else if (m.isArray()) {
                            return Events.from(m.values());
                        }
                    }
                    return Events.from(value);
                } else {
                    return Events.NEVER;
                }
            });
        }
        return current.map(JSON::new);
    }

    public <M> M to(Class<M> type) {
        Model<M> model = Model.of(type);
        return model.attribute ? I.transform(root, type) : to(model, I.make(type), root);
    }

    public <M> M to(M value) {
        return to(Model.of(value), value, root);
    }

    /**
     * <p>
     * Helper method to traverse json structure using Java Object {@link Model}.
     * </p>
     *
     * @param <M> A current model type.
     * @param model A java object model.
     * @param java A java value.
     * @param js A javascript value.
     * @return A restored java object.
     */
    private <M> M to(Model<M> model, M java, Object js) {
        if (js instanceof Map) {
            Map<String, Object> map = (Map) js;

            List<Property> properties = new ArrayList(model.properties());

            if (properties.isEmpty()) {
                for (String id : map.keySet()) {
                    Property property = model.property(id);

                    if (property != null) {
                        properties.add(property);
                    }
                }
            }

            for (Property property : properties) {
                if (!property.isTransient) {
                    if (map.containsKey(property.name)) {
                        // calculate value
                        map.containsKey(property.name);
                        Object value = map.get(property.name);
                        Class type = property.model.type;

                        // convert value
                        if (property.isAttribute()) {
                            value = I.transform(value, type);
                        } else {
                            value = to(property.model, I.make(type), value);
                        }

                        // assign value
                        model.set(java, property, value);
                    }
                }
            }
        }

        // API definition
        return java;
    }
}
