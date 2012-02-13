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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import antibug.Ezunit;

/**
 * @version 2012/02/13 13:47:28
 */
public class XMLizer implements PropertyWalker {

    /** The document builder. */
    private static final DocumentBuilder dom;

    /** The xpath evaluator. */
    private static final XPath xpath;

    // initialization
    static {
        try {
            dom = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xpath = XPathFactory.newInstance().newXPath();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /** The record for traversed objects. */
    final Set nodes = new LinkedHashSet();

    private Deque<Node> contexts = new ArrayDeque();

    /** The current document. */
    private Document doc;

    private Node e;

    public void write(Object input) {
        Model model = Model.load(input.getClass());
        Property property = new Property(model, model.name);

        doc = dom.newDocument();
        e = doc;
        contexts.add(e);

        walk(model, property, input);

        Ezunit.dumpXML(doc);
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
            ((Element) e).setAttribute(property.name, I.transform(node, String.class));
        } else {
            System.out.println("enter " + node);

            Element child = doc.createElement(property.model.name);

            if (e != null) {
                e.appendChild(child);

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
