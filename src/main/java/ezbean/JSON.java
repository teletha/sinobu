/*
 * Copyright (C) 2011 Nameless Production Committee.
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
import java.util.List;

import ezbean.model.Model;
import ezbean.model.ModelWalker;
import ezbean.model.Property;

/**
 * <p>
 * JSON serializer for Java object graph. This serializer rejects cyclic node within ancestor nodes,
 * but same object in sibling nodes will be acceptable.
 * </p>
 * 
 * @version 2010/01/12 20:32:11
 */
class JSON extends ModelWalker {

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
     * @see ezbean.model.ModelWalker#enter(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
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
     * @see ezbean.model.ModelWalker#leave(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
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
