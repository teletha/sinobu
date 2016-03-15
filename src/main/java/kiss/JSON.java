/*
 * Copyright (C) 2016 Nameless Production Committee
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
 * @version 2016/03/15 18:10:13
 */
class JSON implements PropertyWalker {

    /** The record for traversed objects. */
    private final ConcurrentHashMap reference = new ConcurrentHashMap();

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
     * {@inheritDoc}
     */
    @Override
    public void walk(Model model, Property property, Object node) {
        if (!property.isTransient) {
            try {
                // ========================================
                // Enter Node
                // ========================================
                // check whether this is first property in current context or not.
                if (first) {
                    // mark as not first
                    first = false;
                } else {
                    // write property seperator
                    out.append(',');
                }

                // write property key (root node and List node doesn't need key)
                if (reference.size() != 0 && model.type != List.class) {
                    write(property.name);
                    out.append(':');
                }

                // write property value
                if (property.isAttribute()) {
                    write(I.transform(node, String.class));
                } else {
                    // check cyclic node (non-attribute node only apply this check)
                    if (reference.putIfAbsent(node, 0) != null) {
                        throw new ClassCircularityError(reference.toString());
                    } else {
                        // write suitable brace
                        out.append(property.model.type == List.class ? '[' : '{');
                    }

                    // ========================================
                    // Traverse Child Node
                    // ========================================
                    boolean store = first; // store the first property state
                    first = true;

                    property.model.walk(node, this);

                    first = store; // restore the first property state

                    // ========================================
                    // Leave Node
                    // ========================================
                    // unregister non-attribute node
                    reference.remove(node);

                    // write suitable brace
                    out.append(property.model.type == List.class ? ']' : '}');
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
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
