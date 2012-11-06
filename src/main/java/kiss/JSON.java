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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyWalker;

/**
 * <p>
 * JSON serializer for Java object graph. This serializer rejects cyclic node within ancestor nodes,
 * but same object in sibling nodes will be acceptable.
 * </p>
 * 
 * @version 2010/01/12 20:32:11
 */
class JSON implements PropertyWalker {

    /** The record for traversed objects. */
    final ConcurrentHashMap<Object, Integer> nodes = new ConcurrentHashMap();

    /** The charcter sequence for output as JSON. */
    private final Appendable out;

    /** The flag whether the current property is the first item in context or not. */
    private boolean first = true;

    /**
     * JSON serializer.
     * 
     * @param out An output target.
     */
    JSON(Appendable out) {
        this.out = out;
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
    public final void walk(Model model, Property property, Object node) {
        if (!property.isTransient()) {
            // enter node
            enter(model, property, node);

            // check cyclic node
            if (node != null && nodes.putIfAbsent(node, nodes.size()) == null) property.model.walk(node, this);

            // leave node
            leave(model, property, node);
        }
    }

    /**
     * This method is called whenever the {@link PropertyWalker} visits a node in object graph.
     * 
     * @param model A object model of the base node that {@link PropertyWalker} started from. This
     *            value must not be <code>null</code>. If the visited node is root, this value will
     *            be a object model of the root node.
     * @param property An arc in object graph. This value must not be <code>null</code>. If the
     *            visited node is root, this value will be a object property of the root node.
     * @param node A current node that {@link PropertyWalker} arrives at.
     */
    protected void enter(Model model, Property property, Object node) {
        try {
            // check whether this is first property in current context or not.
            if (first) {
                // mark as not first
                first = false;
            } else {
                // write property seperator
                out.append(',');
            }

            // write property key (root node and List node doesn't need key)
            if (nodes.size() != 0 && model.type != List.class) {
                write(property.name);
                out.append(':');
            }

            // write property value
            if (property.isAttribute()) {
                write(I.transform(node, String.class));
            } else {
                // check cyclic node (non-attribute node only apply this check)
                if (nodes.contains(node)) {
                    throw new ClassCircularityError(nodes.toString());
                }

                // write suitable brace
                out.append(property.model.type == List.class ? '[' : '{');

                // reset next context
                first = true;
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * This method is called whenever the {@link PropertyWalker} leaves a node in object graph.
     * 
     * @param model A object model of the base node that {@link PropertyWalker} started from. This
     *            value must not be <code>null</code>. If the visited node is root, this value will
     *            be a object model of the root node.
     * @param property An arc in object graph. This value must not be <code>null</code>. If the
     *            visited node is root, this value will be a object property of the root node.
     * @param node A current node that {@link PropertyWalker} arrives at.
     */
    protected void leave(Model model, Property property, Object node) {
        try {
            if (!property.isAttribute()) {
                // unregister non-attribute node
                nodes.remove(node);

                // write suitable brace
                out.append(property.model.type == List.class ? ']' : '}');
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Write JSON literal with quote.
     * </p>
     * 
     * @param value A character sequence.
     * @throws IOException
     */
    private void write(String value) throws IOException {
        out.append('"');

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
            case '"':
                out.append("\\\"");
                break;

            case '\\':
                out.append("\\\\");
                break;

            case '\b':
                out.append("\\b");
                break;

            case '\f':
                out.append("\\f");
                break;

            case '\n':
                out.append("\\n");
                break;

            case '\r':
                out.append("\\r");
                break;

            case '\t':
                out.append("\\t");
                break;

            default:
                out.append(c);
            }
        }

        out.append('"');
    }
}
