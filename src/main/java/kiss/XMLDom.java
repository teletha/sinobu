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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kiss.model.Model;
import kiss.model.Property;

/**
 * @version 2012/02/24 15:58:52
 */
public class XMLDom extends JSON {

    Element element = Element.$(Element.dom.newDocument());

    private Deque<Element> elements = new ArrayDeque();

    private int counter = 0;

    private ConcurrentHashMap<Object, Element> map = new ConcurrentHashMap();

    /**
     * Create ConfigurationWriter instance.
     * 
     * @param writer An actual XML writer.
     */
    XMLDom() {
        super(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void enter(Model model, Property property, Object node) {
        // If the specfied model or property requires new element for serialization, we must
        // write out the previous start element.
        if (model.isCollection()) {
            elements.add(element);

            // collection item property
            element = Element.$(property.model.name);
            elements.peekLast().append(element);
            map.putIfAbsent(node, element);

            // collection needs key attribute
            if (Map.class.isAssignableFrom(model.type)) {
                element.attr("ss:key", property.name);
            }
        } else if (!property.isAttribute()) {
            elements.add(element);

            element = Element.$(property.name);
            elements.peekLast().append(element);
            map.putIfAbsent(node, element);
        }

        // If the collection item is attribute node, that is represented as xml value attribute
        // and attribute node that collection node doesn't host is written as xml attribute too.
        if (node != null) {
            if (property.isAttribute()) {
                element.attr(model.isCollection() ? "value" : property.name, I.transform(node, String.class));
            } else if (nodes.contains(node)) {
                Element e = map.get(node);

                if (e != null) {
                    String id = e.attr("ss:id");

                    if (id.length() == 0) {
                        id = String.valueOf(counter++);

                        e.attr("ss:id", id);
                    }
                    element.attr("ss:id", id);
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
            element = elements.pollLast();
        }

    }

}
