/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;

import kiss.model.Model;
import kiss.model.Property;
import kiss.model.PropertyWalker;

/**
 * <p>
 * This is multi-purpose implementation class. Please connive this extremely-dirty code.
 * </p>
 * <ul>
 * <li>State recorder for XML serialization</li>
 * <li>{@link Reader} implementation for {@link I#read(Readable, Object)}</li>
 * <li>{@link PropertyWalker} implementation for {@link I#transform(Object, Class)}</li>
 * </ul>
 * 
 * @version 2012/11/09 11:04:01
 */
final class State extends Reader implements PropertyWalker {

    /** The current model. */
    Model model;

    /** The curret object. */
    Object object;

    /** The property for xml deserialization process. */
    Property property;

    /** The current location for deserialization process. */
    int i = 0;

    /** The latest cache for {@link I#read(Readable, Object)}. */
    private CharBuffer last;

    /**
     * Create utility instance.
     * 
     * @param object A actual object.
     * @param model A model of the specified object.
     */
    State(Object object, Model model) {
        this.object = object;
        this.model = model;
    }

    /**
     * <p>
     * {@link Reader} implementation for {@link I#read(Readable, Object)}.
     * </p>
     * <p>
     * Thid reader provides two functionalities. One is {@link Readable} reader and the other is
     * {@link PushbackReader}.
     * </p>
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        // We use ther int field as flag whether this reader shuold push back stream or not.
        if (i == 0) {
            // read data normally
            return ((Readable) object).read(last = CharBuffer.wrap(cbuf, off, len));
        } else {
            // push back data
            i = 0;

            // Read the latest buffer with JSON header text.
            // The sequential reading process is normal because the flag field was reset.
            return CharBuffer.wrap(cbuf, off, len).put("a=").put((CharBuffer) last.flip()).flip().limit();
        }
    }

    /**
     * <p>
     * {@link Reader} implementation for {@link I#read(Readable, Object)}.
     * </p>
     */
    @Override
    public void close() throws IOException {
        I.quiet(object);
    }

    /**
     * {@inheritDoc}
     */
    public void walk(Model model, Property property, Object node) {
        Property dest = this.model.getProperty(property.name);

        // never check null because PropertyWalker traverses existing properties
        this.model.set(object, dest, I.transform(node, dest.model.type));
    }
}
