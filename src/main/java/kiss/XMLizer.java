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

import static kiss.xml.Element.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyWalker;
import kiss.xml.Element;

/**
 * @version 2012/02/13 13:47:28
 */
public class XMLizer implements PropertyWalker {

    /** The record for traversed objects. */
    final Set nodes = new LinkedHashSet();

    private Deque<Element> contexts = new ArrayDeque();

    private Element e;

    public void write(Object input) {
        Model model = Model.load(input.getClass());
        Property property = new Property(model, model.name);

        e = $("<" + model.name + "/>");
        contexts.add(e);

        walk(model, property, input);

        System.out.println(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(Model model, Property property, Object node) {
        if (!property.isTransient()) {
            // enter node
            enter(model, property, node);

            // check cyclic node
            if (node != null && nodes.add(node)) property.model.walk(node, this);

            // leave node
            leave(model, property, node);
        }
    }

    protected void enter(Model model, Property property, Object node) {

        if (property.isAttribute()) {
            e.attr(property.name, I.transform(node, String.class));
        } else {
            System.out.println("enter " + node);

            Element child = $("<" + property.model.name + "/>");
            if (e != null) {
                e.append(child);

                System.out.println(e + "  @@  " + child);
                contexts.add(e);
            }

            e = child;
        }
    }

    protected void leave(Model model, Property property, Object node) {

        if (property.isAttribute()) {

        } else {
            System.out.println("leave " + node);
            e = contexts.pollLast();
        }
    }
}
