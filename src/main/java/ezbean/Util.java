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
import java.io.Reader;
import java.nio.CharBuffer;

import ezbean.model.Model;
import ezbean.model.Property;
import ezbean.model.PropertyWalker;

/**
 * <p>
 * This is multi-purpose implementation class.
 * </p>
 * <ul>
 * <li>State recorder for XML serialization ({@link XMLIn})</li>
 * <li>{@link PropertyListener} implementation for {@link I#transform(Object, Class)}</li>
 * <li>{@link Reader} implementation for {@link I#read(Readable, Object)}</li>
 * </ul>
 * 
 * @version 2011/04/01 11:21:56
 */
final class Util extends Reader implements PropertyWalker {

    /** The current model. */
    Model model;

    /** The curret object. */
    Object object;

    /** The property for {@link XMLIn} process. */
    Property property;

    /** The current location for {@link XMLIn} process. */
    int i = 0;

    /** The latest cache for {@link I#read(Readable, Object)}. */
    private CharBuffer last;

    /**
     * Create utility instance.
     * 
     * @param object A actual object.
     * @param model A model of the specified object.
     */
    Util(Object object, Model model) {
        this.object = object;
        this.model = model;
    }

    /**
     * @see ezbean.model.PropertyWalker#walk(ezbean.model.Model, ezbean.model.Property,
     *      java.lang.Object)
     */
    public void walk(Model model, Property property, Object node) {
        Property dest = this.model.getProperty(property.name);

        // never check null because PropertyWalker traverses existing properties
        this.model.set(object, dest, I.transform(node, dest.model.type));
    }

    /**
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (i == 0) {
            return ((Readable) object).read(last = CharBuffer.wrap(cbuf, off, len));
        } else {
            // flag off
            i = 0;

            // Read the latest buffer with JSON header text.
            return CharBuffer.wrap(cbuf, off, len).put("a=").put((CharBuffer) last.flip()).flip().limit();
        }
    }

    /**
     * @see java.io.Reader#close()
     */
    @Override
    public void close() throws IOException {
        I.quiet(object);
    }
}
