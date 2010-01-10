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
import java.util.List;

import ezbean.model.Model;
import ezbean.model.ModelWalker;
import ezbean.model.Property;

/**
 * @version 2010/01/10 10:20:13
 */
class JSON extends ModelWalker {

    /** The charcter sequence for output as JSON. */
    private final Appendable out;

    /** The flag whether the current property is the first item in context or not. */
    private boolean first = true;

    /**
     * @param root
     * @param out
     */
    public JSON(Appendable out) {
        // setup
        this.out = out;
    }

    /**
     * @see ezbean.model.ModelWalker#enter(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object, boolean)
     */
    protected void enter(Model model, Property property, Object node, boolean cyclic) {
        if (cyclic) {
            throw new ClassCircularityError(record.toString());
        }

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
            if (record.size() != 1 && model.type != List.class) {
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
     * @see ezbean.model.ModelWalker#leave(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object, boolean)
     */
    protected void leave(Model model, Property property, Object node, boolean cyclic) {
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
    }

    /**
     * <p>
     * Write JSON literal with quote.
     * </p>
     * 
     * @param chars A character sequence.
     * @throws IOException
     */
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
