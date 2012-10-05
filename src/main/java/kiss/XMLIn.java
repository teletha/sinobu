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

import java.util.HashMap;
import java.util.LinkedList;

import kiss.model.Codec;
import kiss.model.Model;
import kiss.model.Property;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * <p>
 * XML reader for Object Graph deserialization.
 * </p>
 * <p>
 * We could select to use List implementaion instead of Map for management of implicit object
 * identifier. But it requires linear time to search the existing element. So we should use Map
 * which provides constant-time performance for seaching element.
 * </p>
 * 
 * @version 2010/01/12 22:54:15
 */
class XMLIn extends XMLFilterImpl {

    /** The root object. */
    private Object root;

    /** The id and object mapping. */
    private final HashMap objects = new HashMap();

    /** The stack of states. */
    private final LinkedList<Util> states = new LinkedList<Util>();

    /**
     * Create ConfigurationReader instance.
     * 
     * @param root
     */
    XMLIn(Object root) {
        this.root = root;
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        Util state;

        if (states.size() == 0) {
            state = new Util(root, Model.load(root.getClass()));
        } else {
            Util parent = states.peekLast();

            // Compute property.
            //
            // A name of the current element indicates the hint of the property name.
            // So you can get a valid property from the parent state.
            if (parent.model.isCollection()) {
                localName = attributes.getValue(URI, "key");

                if (localName == null) {
                    localName = String.valueOf(parent.i++);
                }
            }

            Property property = parent.model.getProperty(localName);

            // Compute object
            //
            // Property indicates a object, so you should create a suitable object.
            Object object = null;

            // check attribute model
            Codec codec = property.model.getCodec();

            if (codec != null) {
                String value = attributes.getValue("value");

                if (value == null) {
                    object = null;
                } else {
                    object = codec.decode(value);
                }
            } else {
                // collection model and normal model
                object = make(property.model.type);
            }

            // create next state
            state = new Util(object, property.model);
            state.property = property;
        }

        // assign properties which are represented by attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            // check namespace
            if (attributes.getURI(i).equals(URI)) {
                if (attributes.getLocalName(i).equals("id")) {
                    // retrieve identifier for the current object
                    localName = attributes.getValue(i);

                    // retrieve object for the identifier
                    Object object = objects.get(localName);

                    if (object == null) {
                        // Object is not registered for the identifier, so this is first encounter
                        // of thie object.
                        objects.put(localName, state.object);
                    } else {
                        // Object is registered for the identifier, so this is referenced object.
                        state.object = object;
                    }
                }
            } else {
                Property property = state.model.getProperty(attributes.getLocalName(i));

                // ignore deprecated property
                if (property != null) {
                    // restore a property value form an attribute value
                    Codec codec = property.model.getCodec();

                    if (codec != null) {
                        state.model.set(state.object, property, codec.decode(attributes.getValue(i)));
                    }
                }
            }
        }

        // stack current state for reference
        states.offer(state);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) {
        Util current = states.pollLast();
        Util parent = states.peekLast();

        if (parent != null) {
            parent.model.set(parent.object, current.property, current.object);
        }
    }
}
