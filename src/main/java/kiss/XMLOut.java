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

import static kiss.I.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kiss.model.Model;
import kiss.model.Property;
import kiss.xml.XMLWriter;

import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>
 * XML writer for Object Graph serialization.
 * </p>
 * <p>
 * We could select to use List implementaion instead of Map for management of implicit object
 * identifier. But it requires linear time to search the existing element. So we should use Map
 * which provides constant-time performance for seaching element.
 * </p>
 * 
 * @version 2010/12/14 0:23:59
 */
class XMLOut extends JSON {

    /** The content handler. */
    private final XMLWriter writer;

    /** The object and id mapping. */
    private ConcurrentHashMap<Object, Integer> objects = new ConcurrentHashMap();

    /** The current traversing mode. */
    boolean mode = true;

    /** The current stored node name. */
    private String name;

    /** The attribute for reuse. */
    private AttributesImpl attributes = new AttributesImpl();

    /**
     * Create ConfigurationWriter instance.
     * 
     * @param writer An actual XML writer.
     */
    XMLOut(XMLWriter writer) {
        super(null);
        this.writer = writer;
    }

    /**
     * @see kiss.kiss.model.PropertyWalker#enter(kiss.model.Model, kiss.model.Property,
     *      java.lang.Object)
     */
    protected void enter(Model model, Property property, Object node) {
        if (mode) {
            if (!property.isAttribute() && nodes.contains(node)) {
                objects.putIfAbsent(node, objects.size());
            }
        } else {
            // If the specfied model or property requires new element for serialization, we must
            // write out the previous start element.
            if (model.isCollection()) {
                write();

                // collection item property
                name = property.model.name;

                // collection needs key attribute
                if (Map.class.isAssignableFrom(model.type)) {
                    attributes.addAttribute(URI, null, "ss:key", null, property.name);
                }
            } else if (!property.isAttribute()) {
                write();

                name = property.name;
            }

            // If the collection item is attribute node, that is represented as xml value attribute
            // and attribute node that collection node doesn't host is written as xml attribute too.
            if (node != null) {
                if (property.isAttribute()) {
                    attributes.addAttribute(null, null, (model.isCollection()) ? "value" : property.name, null, I.transform(node, String.class));
                } else {
                    Integer integer = objects.get(node);

                    if (integer != null) {
                        // create reference id attribute
                        attributes.addAttribute(URI, null, "ss:id", null, integer.toString());
                    }
                }
            }
        }
    }

    /**
     * @see kiss.kiss.model.PropertyWalker#leave(kiss.model.Model, kiss.model.Property,
     *      java.lang.Object)
     */
    protected void leave(Model model, Property property, Object node) {
        if (!mode) {
            // If the specfied model or property requires new element for serialization, we must
            // write out the previous start element.
            if (model.isCollection() || !property.isAttribute()) {
                write();

                writer.endElement(null, null, (model.isCollection()) ? property.model.name : property.name);
            }
        }
    }

    /**
     * Helper method to write out the stored sax event.
     */
    private void write() {
        // check node name
        if (name != null) {
            // write start element
            writer.startElement(null, null, name, attributes);

            // clear current state
            name = null;
            attributes.clear();
        }
    }
}
