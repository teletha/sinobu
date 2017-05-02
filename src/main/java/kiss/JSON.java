/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kiss.model.Model;
import kiss.model.Property;

/**
 * <p>
 * JSON serializer for Java object graph. This serializer rejects cyclic node within ancestor nodes,
 * but same object in sibling nodes will be acceptable.
 * </p>
 * 
 * @version 2017/04/26 11:45:46
 */
public class JSON implements WiseTriConsumer<Model, Property, Object> {

    /** The root object. */
    private Object root;

    /**
     * Hide constructor.
     * 
     * @param root A root json object.
     */
    JSON(Object root) {
        this.root = root;
    }

    /**
     * <p>
     * Find values by the specified name path.
     * </p>
     * 
     * @param path A name path.
     * @return
     */
    public Signal<String> find(String path) {
        return find(path, String.class);
    }

    /**
     * <p>
     * Find values by the specified name path.
     * </p>
     * 
     * @param path A name path.
     * @return
     */
    public <M> Signal<M> find(String path, Class<M> type) {
        return select(path).map(v -> v.to(type));
    }

    /**
     * <p>
     * Find values by the specified name path.
     * </p>
     * 
     * @param path A name path.
     * @return
     */
    private Signal<JSON> select(String path) {
        Signal<Object> current = I.signal(root);

        for (String name : path.split("\\.")) {
            current = current.flatMap(v -> {
                if (v instanceof Map) {
                    int i = name.lastIndexOf('[');
                    String main = i == -1 ? name : name.substring(0, i);
                    Object value = ((Map) v).get(main);

                    if (i != -1) {
                        return I.signal(((Map) value).get(name.substring(i + 1, name.length() - 1)));
                    } else if (value instanceof LinkedHashMap) {
                        return I.signal(((Map) value).values());
                    }

                    return I.signal(value);
                } else {
                    return Signal.NEVER;
                }
            });
        }
        return current.map(JSON::new);
    }

    /**
     * <p>
     * Data mapping to the specified model.
     * </p>
     * 
     * @param type A model type.
     * @return A created model.
     */
    public <M> M to(Class<M> type) {
        Model<M> model = Model.of(type);
        return model.attribute ? I.transform(root, type) : to(model, I.make(type), root);
    }

    /**
     * <p>
     * Data mapping to the specified model.
     * </p>
     * 
     * @param value A model.
     * @return A specified model.
     */
    public <M> M to(M value) {
        return to(Model.of(value), value, root);
    }

    /**
     * <p>
     * Helper method to traverse json structure using Java Object {@link Model}.
     * </p>
     *
     * @param <M> A current model type.
     * @param model A java object model.
     * @param java A java value.
     * @param js A javascript value.
     * @return A restored java object.
     */
    private <M> M to(Model<M> model, M java, Object js) {
        if (js instanceof Map) {
            Map<String, Object> map = (Map) js;

            List<Property> properties = new ArrayList(model.properties());

            if (properties.isEmpty()) {
                for (String id : map.keySet()) {
                    Property property = model.property(id);

                    if (property != null) {
                        properties.add(property);
                    }
                }
            }

            for (Property property : properties) {
                if (!property.isTransient) {
                    if (map.containsKey(property.name)) {
                        // calculate value
                        map.containsKey(property.name);
                        Object value = map.get(property.name);
                        Class type = property.model.type;

                        // convert value
                        if (property.isAttribute()) {
                            value = I.transform(value, type);
                        } else {
                            value = to(property.model, I.make(type), value);
                        }

                        // assign value
                        model.set(java, property, value);
                    }
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

        read();
        space();
        root = value();
    }

    /**
     * <p>
     * Read value.
     * </p>
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
            startCapture();
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
     * <p>
     * Read the sequence of white spaces
     * </p>
     * 
     * @throws IOException
     */
    private void space() throws IOException {
        while (current == ' ' || current == '\t' || current == '\n' || current == '\r') {
            read();
        }
    }

    /**
     * <p>
     * Read the sequence of digit.
     * </p>
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
     * <p>
     * Read the sequence of keyword.
     * </p>
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
     * <p>
     * Read the sequence of String.
     * </p>
     * 
     * @return A parsed string.
     * @throws IOException
     */
    private String string() throws IOException {
        token('"');
        startCapture();
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
                startCapture();
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
     * <p>
     * Read the next character.
     * </p>
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
     * <p>
     * Read the specified character.
     * </p>
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
     * <p>
     * Read the specified character surely.
     * </p>
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
     * Start text capturing.
     */
    private void startCapture() {
        if (capture == null) {
            capture = new StringBuilder();
        }
        captureStart = index - 1;
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
     * <p>
     * Throw parsing error.
     * </p>
     * 
     * @param expected A reason.
     * @return This method NEVER return value.
     */
    private Object expected(Object expected) {
        throw new IllegalStateException("Expected : ".concat(String.valueOf(expected)));
    }

    // ===========================================================
    // Writer API
    // ===========================================================
    /** The charcter sequence for output as JSON. */
    private Appendable out;

    /**
     * 
     */
    JSON(Appendable out) {
        this.out = out;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(Model model, Property property, Object value) {
        if (!property.isTransient && property.name != null) {
            try {
                // non-first properties requires separator
                if (index++ != 0) out.append(',');

                // all properties need the properly indents
                if (0 < current) {
                    indent();

                    // property key (List node doesn't need key)
                    if (model.type != List.class) {
                        write(property.name, String.class);
                        out.append(": ");
                    }
                }

                // property value
                if (property.isAttribute()) {
                    write(I.transform(value, String.class), property.model.type);
                } else {
                    if (64 < current) {
                        throw new ClassCircularityError();
                    }

                    JSON walker = new JSON(out);
                    walker.current = current + 1;
                    walker.out.append(property.model.type == List.class ? '[' : '{');
                    property.model.walk(value, walker);
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

    /**
     * <p>
     * Helper method to write line and indent.
     * </p>
     * 
     * @throws IOException
     */
    private void indent() throws IOException {
        out.append("\r\n");

        for (int i = 0; i < current; i++) {
            out.append('\t');
        }
    }
}
