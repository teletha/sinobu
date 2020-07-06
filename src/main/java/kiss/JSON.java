/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import kiss.model.Model;
import kiss.model.Property;

public class JSON {

    /** The root object. */
    private Object root;

    /**
     * Create empty JSON object.
     */
    public JSON() {
        this(new HashMap());
    }

    /**
     * Hide constructor.
     * 
     * @param root A root json object.
     */
    JSON(Object root) {
        this.root = root;
    }

    /**
     * Check the direct child value with the specified key.
     * 
     * @param key A target key.
     * @param value An expected value.
     * @return A result.
     */
    public boolean has(String key, Object value) {
        return root instanceof Map ? Objects.equals(((Map) root).get(key), value) : false;
    }

    /**
     * Get the direct child value as your type with the specified key. Unknown key and object key
     * will return null.
     * 
     * @param key A key for value to find.
     * @return An associated value.
     */
    public JSON get(String key) {
        return get(JSON.class, key);
    }

    /**
     * Get the direct child value as your type with the specified key. Unknown key and object key
     * will return null.
     * 
     * @param type A value type to find.
     * @param key A key for value to find.
     * @return An associated value.
     * @throws NullPointerException If type is null.
     */
    public <T> T get(Class<T> type, String key) {
        if (root instanceof Map) {
            Map m = (Map) root;
            Object o = m.get(key);
            if (o == null) {
                return null;
            } else {
                return to(type, o);
            }
        } else {
            return null;
        }
    }

    /**
     * Get the direct child value as your type with the specified key. Unknown key and object key
     * will return null.
     * 
     * @param key A key for value to find.
     * @return An associated value.
     */
    public String text(String key) {
        return get(String.class, key);
    }

    /**
     * Set the direct child value with the specified key.
     * 
     * @param key A key.
     * @param value A value.
     * @return Chainable API.
     */
    public JSON set(String key, Object value) {
        if (root instanceof Map) {
            ((Map) root).put(key, value);
        }
        return this;
    }

    /**
     * Get all objects pointed to by the property path starting from this JSON. The property path
     * can use the property name and the wildcard "*".
     * 
     * @param path A property path.
     * @return A result set.
     */
    public List<JSON> find(String... path) {
        return find(JSON.class, path);
    }

    /**
     * Get all objects pointed to by the property path starting from this JSON. The property path
     * can use the property name and the wildcard "*".
     * 
     * @param type The convertion type.
     * @param path A property path.
     * @return A result set.
     * @throws NullPointerException If type is null.
     */
    public <T> List<T> find(Class<T> type, String... path) {
        List items = List.of(root);

        for (int i = 0; i < path.length; i++) {
            List next = new ArrayList();

            for (Object item : items) {
                if (item instanceof Map) {
                    if (path[i].equals("*")) {
                        next.addAll(((Map) item).values());
                    } else {
                        Object value = ((Map) item).get(path[i]);
                        if (value != null) {
                            next.add(value);
                        }
                    }
                }
            }
            items = next;
        }

        for (int i = 0; i < items.size(); i++) {
            items.set(i, to(type, items.get(i)));
        }
        return items;
    }

    /**
     * Data mapping to the specified model.
     * 
     * @param type A model type.
     * @return A created model.
     */
    public <M> M to(Class<M> type) {
        return to(type, root);
    }

    /**
     * Data mapping to the specified model.
     * 
     * @param value A model.
     * @return A specified model.
     */
    public <M> M to(M value) {
        return to(Model.of(value), value, root);
    }

    /**
     * Helper method to convert json object to java object.
     * 
     * @param <M>
     * @param type
     * @param o
     * @return
     */
    private static <M> M to(Class<M> type, Object o) {
        if (JSON.class == type) {
            return (M) new JSON(o);
        } else if (o instanceof Map) {
            return to(Model.of(type), I.make(type), o);
        } else {
            return I.transform(o, type);
        }
    }

    /**
     * Helper method to traverse json structure using Java Object {@link Model}.
     *
     * @param <M> A current model type.
     * @param model A java object model.
     * @param java A java value.
     * @param js A javascript value.
     * @return A restored java object.
     */
    private static <M> M to(Model<M> model, M java, Object js) {
        if (js instanceof Map) {
            for (Entry<String, Object> e : ((Map<String, Object>) js).entrySet()) {
                Property p = model.property(e.getKey());

                if (p != null && !p.isTransient) {
                    Object value = e.getValue();

                    // convert value
                    if (p.isAttribute()) {
                        value = I.transform(value, p.model.type);
                    } else if (value != null) {
                        Object nest = model.get(java, p);
                        String impl = (String) ((Map) value).get("#");
                        Model m = impl == null ? p.model : Model.of(I.type(impl));
                        value = to(m, nest == null ? I.make(m.type) : nest, value);
                    }

                    // assign value
                    model.set(java, p, value);
                }
            }
        }

        // API definition
        return java;
    }

    // ===========================================================
    // Parser API
    // ===========================================================
    /** The input source. */
    private Reader reader;

    /** The input buffer. */
    private char[] buffer;

    /** The index of input buffer. */
    private int index;

    /** The limit of input buffer. */
    private int fill;

    /** The current character data. */
    private int current;

    /** The capturing text. */
    private StringBuilder capture;

    /** The capture index in input buffer. */
    private int captureStart;

    /**
     * Initialize parser.
     * 
     * @param reader
     * @throws IOException
     */
    JSON(Reader reader) throws IOException {
        this.reader = reader;
        this.buffer = new char[1024];
        this.captureStart = -1;
        this.capture = new StringBuilder();

        read();
        space();
        root = value();
    }

    /**
     * Read value.
     * 
     * @throws IOException
     */
    private Object value() throws IOException {
        switch (current) {
        // keyword
        case 'n':
            return keyword(null);
        case 't':
            return keyword(Boolean.TRUE);
        case 'f':
            return keyword(Boolean.FALSE);

        // string
        case '"':
            return string();

        // array
        case '[':
            Map array = new LinkedHashMap();
            read();
            space();
            if (read(']')) {
                return array;
            }

            int count = 0;
            do {
                space();
                array.put(String.valueOf(count++), value());
                space();
            } while (read(','));
            token(']');
            return array;

        // object
        case '{':
            Map object = new HashMap();
            read();
            space();
            if (read('}')) {
                return object;
            }
            do {
                space();
                String name = string();
                space();
                token(':');
                space();
                object.put(name, value());
                space();
            } while (read(','));
            token('}');
            return object;

        // number
        case '-':
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            // start capture
            captureStart = index - 1;
            read('-');
            if (current == '0') {
                read();
            } else {
                digit();
            }

            // fraction
            if (read('.')) {
                digit();
            }

            // exponent
            if (read('e') || read('E')) {
                if (!read('+')) {
                    read('-');
                }
                digit();
            }
            return endCapture();

        // invalid token
        default:
            return expected("value");
        }
    }

    /**
     * Read the sequence of white spaces
     * 
     * @throws IOException
     */
    private void space() throws IOException {
        while (current == ' ' || current == '\t' || current == '\n' || current == '\r') {
            read();
        }
    }

    /**
     * Read the sequence of digit.
     * 
     * @throws IOException
     */
    private void digit() throws IOException {
        int count = 0;

        while ('0' <= current && current <= '9') {
            read();
            count++;
        }

        if (count == 0) {
            expected("digit");
        }
    }

    /**
     * Read the sequence of keyword.
     * 
     * @param keyword A target value.
     * @return A target value.
     * @throws IOException
     */
    private Object keyword(Object keyword) throws IOException {
        read();

        String value = String.valueOf(keyword);

        for (int i = 1; i < value.length(); i++) {
            token(value.charAt(i));
        }
        return keyword;
    }

    /**
     * Read the sequence of String.
     * 
     * @return A parsed string.
     * @throws IOException
     */
    private String string() throws IOException {
        token('"');
        // start capture
        captureStart = index - 1;
        while (current != '"') {
            if (current == '\\') {
                pauseCapture();
                // escape
                read();
                switch (current) {
                case '"':
                case '/':
                case '\\':
                    capture.append((char) current);
                    break;
                case 'b':
                    capture.append('\b');
                    break;
                case 'f':
                    capture.append('\f');
                    break;
                case 'n':
                    capture.append('\n');
                    break;
                case 'r':
                    capture.append('\r');
                    break;
                case 't':
                    capture.append('\t');
                    break;
                case 'u':
                    char[] chars = new char[4];
                    for (int i = 0; i < 4; i++) {
                        read();
                        chars[i] = (char) current;
                    }
                    capture.append((char) Integer.parseInt(new String(chars), 16));
                    break;
                default:
                    expected("escape sequence");
                }
                read();

                // start capture
                captureStart = index - 1;
            } else if (current < 0x20) {
                expected("string character");
            } else {
                read();
            }
        }
        String string = endCapture();
        read();
        return string;
    }

    /**
     * Read the next character.
     * 
     * @throws IOException
     */
    private void read() throws IOException {
        if (index == fill) {
            if (captureStart != -1) {
                capture.append(buffer, captureStart, fill - captureStart);
                captureStart = 0;
            }
            fill = reader.read(buffer, 0, buffer.length);
            index = 0;
            if (fill == -1) {
                current = -1;
                return;
            }
        }
        current = buffer[index++];
    }

    /**
     * Read the specified character.
     * 
     * @param c The character to be red.
     * @return A result.
     * @throws IOException
     */
    private boolean read(char c) throws IOException {
        if (current == c) {
            read();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Read the specified character surely.
     * 
     * @param c The character to be red.
     * @return A result.
     * @throws IOException
     */
    private void token(char c) throws IOException {
        if (current == c) {
            read();
        } else {
            expected(c);
        }
    }

    /**
     * Pause text capturing.
     */
    private void pauseCapture() {
        int end = current == -1 ? index : index - 1;
        capture.append(buffer, captureStart, end - captureStart);
        captureStart = -1;
    }

    /**
     * Stop text capturing.
     */
    private String endCapture() {
        int end = current == -1 ? index : index - 1;
        String captured;
        if (capture.length() > 0) {
            capture.append(buffer, captureStart, end - captureStart);
            captured = capture.toString();
            capture.setLength(0);
        } else {
            captured = new String(buffer, captureStart, end - captureStart);
        }
        captureStart = -1;
        return captured;
    }

    /**
     * Throw parsing error.
     * 
     * @param expected A reason.
     * @return This method NEVER return value.
     */
    private Object expected(Object expected) {
        throw new IllegalStateException("Expected ".concat(String.valueOf(expected)));
    }

    // ===========================================================
    // Writer API
    // ===========================================================
    /** The charcter sequence for output as JSON. */
    private Appendable out;

    /**
     * JSON serializer for Java object graph.
     */
    JSON(Appendable out) {
        this.out = out;
    }

    /**
     * JSON serializer for Java object graph. This serializer rejects cyclic node within ancestor
     * nodes, but same object in sibling nodes will be acceptable.
     * 
     * @param model
     * @param property
     * @param value
     */
    void write(Model model, Property property, Object value) {
        if (!property.isTransient && property.name != null) {
            try {
                // non-first properties requires separator
                if (index++ != 0) out.append(',');

                // all properties need the properly indents
                if (0 < current) {
                    out.append("\r\n").append("\t".repeat(current)); // indent

                    // property key (List node doesn't need key)
                    if (model.type != List.class) {
                        write(property.name, String.class);
                        out.append(model.type == Bundle.class ? ":\n\t\t" : ": ");
                    }
                }

                // property value
                if (property.isAttribute()) {
                    write(I.transform(value, String.class), property.model.type);
                } else if (value == null) {
                    out.append("null");
                } else {
                    if (64 < current) {
                        throw new ClassCircularityError();
                    }

                    JSON walker = new JSON(out);
                    walker.current = current + 1;
                    out.append(property.model.type == List.class ? '[' : '{');
                    Model<Object> m = property.model;
                    if (Modifier.isAbstract(m.type.getModifiers()) && m.getClass() == Model.class) {
                        m = Model.of(value);
                        out.append("\r\n").append("\t".repeat(current + 1)).append("\"#\": \"").append(m.type.getName()).append("\",");
                    }
                    m.walk(value, walker::write);
                    if (walker.index != 0) out.append("\r\n").append("\t".repeat(current)); // indent
                    out.append(property.model.type == List.class ? ']' : '}');
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * Write JSON literal with quote.
     * 
     * @param value A value.
     * @param type A value type.
     * @throws IOException
     */
    private void write(String value, Class type) throws IOException {
        if (value == null) {
            out.append("null");
        } else {
            boolean primitive = type.isPrimitive() && type != char.class;

            if (!primitive) out.append('"');

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
            if (!primitive) out.append('"');
        }
    }
}