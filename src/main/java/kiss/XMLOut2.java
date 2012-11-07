/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyWalker;

/**
 * @version 2012/11/07 3:37:56
 */
class XMLOut2 implements PropertyWalker {

    /** The record for traversed objects. */
    protected final ConcurrentHashMap<Object, Element> reference = new ConcurrentHashMap();

    private Element current;

    private int counter = 0;

    /**
     * @param model
     * @param property
     * @param input
     * @param output
     */
    XMLOut2(Model model, Property property, Object input, Appendable output) {
        // build xml
        walk(model, property, input);

        // write down
        current.writeTo(output);

        I.quiet(output);
    }

    /**
     * <p>
     * Traverse this object graph actually.
     * </p>
     * 
     * @param model A object model of the base node that {@link PropertyWalker} started from. This
     *            value must not be <code>null</code>. If the visited node is root, this value will
     *            be a object model of the root node.
     * @param property An arc in object graph. This value must not be <code>null</code>. If the
     *            visited node is root, this value will be a object property of the root node.
     * @param node A current node that {@link PropertyWalker} arrives at.
     * @see kiss.model.PropertyWalker#walk(kiss.model.Model, kiss.model.Property, java.lang.Object)
     */
    @Override
    public final void walk(Model model, Property property, Object node) {
        if (!property.isTransient()) {
            // ========================================
            // Enter Node
            // ========================================
            if (model.isCollection()) {
                // collection item property
                current = child(property.model.name);

                // collection needs key attribute
                if (Map.class.isAssignableFrom(model.type)) {
                    current.attr("ss:key", property.name);
                }
            } else if (!property.isAttribute()) {
                current = child(property.name);
            }

            // If the collection item is attribute node, that is represented as xml value attribute
            // and attribute node that collection node doesn't host is written as xml attribute too.
            if (node != null) {
                if (property.isAttribute()) {
                    current.attr(model.isCollection() ? "value" : property.name, I.transform(node, String.class));
                } else {
                    Element ref = reference.get(node);

                    if (ref == null) {
                        // associate node object with element
                        reference.put(node, current);

                        // assign new id
                        current.attr("ss:id", counter++);

                        // ========================================
                        // Traverse Child Node
                        // ========================================
                        property.model.walk(node, this);
                    } else {
                        // share id
                        current.attr("ss:id", ref.attr("ss:id"));
                    }
                }
            }

            // ========================================
            // Leave Node
            // ========================================
            if (model.isCollection() || !property.isAttribute()) {
                current = current.parent();
            }
        }
    }

    private Element child(String name) {
        if (current == null) {
            return Element.$(name).attr("xmlns:ss", I.URI);

        } else {
            return current.child(name);
        }
    }
}
