/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;

import ezbean.model.Model;
import ezbean.model.ModelWalkListener;
import ezbean.model.ModelWalker;
import ezbean.model.Property;

/**
 * @version 2009/04/14 16:36:21
 */
class JSON extends ModelWalker implements ModelWalkListener {

    /** The charcter sequence for output as JSON. */
    private final Appendable out;

    /** The flag whether the current property is the first item in context or not. */
    private boolean first = true;

    /** The node collection to check circular reference. */
    private ArrayDeque stack = new ArrayDeque();

    /**
     * @param root
     * @param out
     */
    public JSON(Object root, Appendable out) {
        super(root);

        // setup
        this.out = out;
        addListener(this);
    }

    /**
     * @see ezbean.model.ModelWalkListener#enterNode(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    public void enterNode(Model model, Property property, Object node) {
        if (stack.contains(node)) {
            throw new IllegalStateException("Circular Reference : " + stack);
        }
        stack.add(node);

        try {
            // check whether this is first property in current context or not.
            if (first) {
                // mark as not first
                first = false;
            } else {
                // write property seperator
                out.append(',');
            }

            // write property key
            if (record.size() != 0 && model.type != List.class) {
                write(property.name);
                out.append(':');
            }

            // write property value
            if (property.isAttribute()) {
                write(I.transform(node, String.class));
            } else {
                if (property.model.type == List.class) {
                    out.append('[');
                } else {
                    out.append('{');
                }

                // reset next context
                first = true;
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see ezbean.model.ModelWalkListener#leaveNode(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    public void leaveNode(Model model, Property property, Object node) {
        try {
            if (!property.isAttribute()) {
                if (property.model.type == List.class) {
                    out.append(']');
                } else {
                    out.append('}');
                }
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }

        stack.remove(node);
    }

    private void write(CharSequence chars) throws IOException {
        out.append('"');

        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);

            switch (c) {
            case '"':
            case '\\':
            case '\b':
            case '\f':
            case '\n':
            case '\r':
            case '\t':
                out.append('\\');
                // pass through

            default:
                out.append(c);
            }
        }

        out.append('"');
    }
}
