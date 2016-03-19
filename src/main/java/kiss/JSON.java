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

    /** The charcter sequence for output as JSON. */
    private final Appendable out;

    /** The size of indent. */
    private final int indent;

    /** The location number. */
    int index;

    /**
     * JSON serializer.
     * 
     * @param out An output target.
     * @param indent A size of indent.
     */
    JSON(Appendable out, int indent) {
        if (64 < indent) {
            throw new ClassCircularityError();
        }
        this.out = out;
        this.indent = indent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void walk(Model model, Property property, Object object) {
        if (!property.isTransient && property.name != null) {
            try {
                // non-first properties requires separator
                if (index++ != 0) out.append(',');

                // all properties need the properly indents
                indent();

                // property key (List node doesn't need key)
                if (model.type != List.class) {
                    write(property.name);
                    out.append(": ");
                }

                // property value
                if (property.isAttribute()) {
                    write(I.transform(object, String.class));
                } else {
                    JSON walker = new JSON(out, indent + 1);
                    out.append(property.model.type == List.class ? '[' : '{');
                    property.model.walk(object, walker);
                    if (walker.index != 0) indent();
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
        if (value == null) {
            out.append("null");
        } else {
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

    /**
     * <p>
     * Helper method to write line and indent.
     * </p>
     * 
     * @throws IOException
     */
    private void indent() throws IOException {
        out.append("\r\n");

        for (int i = 0; i < indent; i++) {
            out.append('\t');
        }
    }
}
