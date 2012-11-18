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

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyWalker;

/**
 * <p>
 * XML writer for Object Graph serialization.
 * </p>
 * <p>
 * We could select to use List implementaion instead of Map for management of implicit object
 * identifier. But it requires linear time to search the existing element. So we should use Map
 * which provides constant-time performance for seaching element.
 * </p>
 * <p>
 * This is also {@link Appendable} {@link Writer}.
 * </p>
 * 
 * @version 2012/11/07 21:01:06
 */
class XMLWriter extends Writer implements PropertyWalker {

    /** The record for traversed objects. */
    protected final ConcurrentHashMap<Object, XML> reference = new ConcurrentHashMap();

    /** The actual output. */
    private final Appendable output;

    /** The reference counter. */
    private int counter = 0;

    /** The current processing element. */
    XML current;

    /**
     * @param output
     */
    XMLWriter(Appendable output) {
        this.output = output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        I.quiet(output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        if (output instanceof Flushable) {
            ((Flushable) output).flush();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        output.append(new String(cbuf, off, len));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(Model model, Property property, Object node) {
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
                    XML ref = reference.get(node);

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

    /**
     * @param name
     * @return
     */
    private XML child(String name) {
        if (current == null) {
            return I.xml(name).attr("xmlns:ss", I.URI);
        } else {
            return current.child(name);
        }
    }
}
