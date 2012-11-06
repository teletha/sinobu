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

import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2012/11/07 3:37:56
 */
public class XMLOut2 extends JSON {

    Element current;

    /**
     * @param out
     */
    public XMLOut2(Appendable out) {
        super(out);
    }

    private Element child(String name) {
        if (current == null) {
            return Element.$(name).ns("ss", I.URI);
        } else {
            return current.child(name);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void enter(Model model, Property property, Object node) {

        // If the specfied model or property requires new element for serialization, we must
        // write out the previous start element.
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
                Integer integer = nodes.get(node);
                System.out.println(integer);
                if (integer != null) {
                    // create reference id attribute
                    current.attr("ss:id", integer.toString());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void leave(Model model, Property property, Object node) {
        // If the specfied model or property requires new element for serialization, we must
        // write out the previous start element.
        if (model.isCollection() || !property.isAttribute()) {
            current = current.parent();
        }
    }

}
