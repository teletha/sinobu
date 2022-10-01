/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.ToIntFunction;

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
     * Data mapping to the specified model.
     * 
     * @param type A model type.
     * @return A created model.
     */
    public <M> M as(Class<M> type) {
        return as(type, root);
    }

    /**
     * Data mapping to the specified model.
     * 
     * @param value A model.
     * @return A specified model.
     */
    public <M> M as(M value) {
        return as(Model.of(value), value, root);
    }

    /**
     * Data mapping to the {@link Map} with {@link String} key and your specified model.
     * 
     * @param type A model type.
     * @return The key-value pair.
     */
    public <M> Map<String, M> asMap(Class<M> type) {
        if (root instanceof Map) {
            Map map = new HashMap();
            for (Entry e : ((Map<String, Object>) root).entrySet()) {
                map.put(e.getKey(), as(type, e.getValue()));
            }
            return map;
        } else {
            return Map.of();
        }
    }

    /**
     * Helper method to convert json object to java object.
     * 
     * @param <M>
     * @param type
     * @param o
     * @return
     */
    private static <M> M as(Class<M> type, Object o) {
        if (JSON.class == type) {
            return (M) new JSON(o);
        } else if (o instanceof Map) {
            return as(Model.of(type), I.make(type), o);
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
    private static <M> M as(Model<M> model, M java, Object js) {
        if (js instanceof Map) {
            for (Entry<String, Object> e : ((Map<String, Object>) js).entrySet()) {
                Property p = model.property(e.getKey());

                if (p != null && !p.transitory) {
                    Object value = e.getValue();

                    // convert value
                    if (p.model.atomic) {
                        value = I.transform(value, p.model.type);
                    } else if (value != null) {
                        Object nest = model.get(java, p);
                        String impl = (String) ((Map) value).get("#");
                        Model m = impl == null ? p.model : Model.of(I.type(impl));
                        value = as(m, nest == null ? I.make(m.type) : nest, value);
                    }

                    // assign value
                    java = model.set(java, p, value);
                }
            }
        }

        // API definition
        return java;
    }

    /**
     * Check the direct child which has the specified key.
     * 
     * @param key A target key.
     * @return A result.
     */
    public boolean has(String key) {
        return root instanceof Map ? ((Map) root).containsKey(key) : false;
    }

    /**
     * Check the direct child value with the specified key.
     * 
     * @param key A target key.
     * @param value An expected value.
     * @return A result.
     */
    public boolean has(String key, Object value) {
        return root instanceof Map ? Objects.equals(((Map) root).get(key), String.valueOf(value)) : false;
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
                return as(type, o);
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
     * @throws NullPointerException If type or path is null.
     */
    public <T> List<T> find(Class<T> type, String... path) {
        List items = new ArrayList(4);
        items.add(root);

        for (int i = 0; i < path.length; i++) {
            int mode = path[i].equals("*") ? -1 : path[i].equals("$") ? -2 : 0;

            for (int j = 0; j < items.size();) {
                Object item = items.get(j);

                if (item instanceof Map) {
                    Map m = (Map) item;
                    if (mode != 0) {
                        items.remove(j);
                        items.addAll(j, m.values());
                        j += m.size();
                    } else {
                        Object value = m.get(path[i]);
                        if (value != null) {
                            items.set(j++, value);
                        } else {
                            items.remove(j);
                        }
                    }
                } else {
                    j++;
                }
            }

            if (mode == -2) {
                Collections.reverse(items);
            }
        }

        for (int i = 0; i < items.size(); i++) {
            items.set(i, as(type, items.get(i)));
        }
        return items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return root.toString();
    }

    // ===========================================================
    // Parser API
    // ===========================================================
    /** Reuse buffers. */
    private static final ArrayBlockingQueue<Ⅱ<char[], StringBuilder>> P = new ArrayBlockingQueue(16);

    /** Reuse array's index to reduce GC execution. */
    private static final String[] C = "0123456789".split("");

    /** Reuse text symbol. */
    private static final Ⅱ<String, char[]>[] S = new Ⅱ[65536];

    /** The input source. */
    private ToIntFunction<char[]> reader;

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

    private String input;

    private int size;

    private int red;

    /**
     * @param input
     */
    JSON(String input) {
        this.input = input;
        this.size = input.length();
    }

    private int read(char[] buffer) {
        if (size <= red) return -1;
        int len = size - red;
        if (4096 < len) len = 4096;
        input.getChars(red, red += len, buffer, 0);
        return len;
    }

    /**
     * Parser the given json.
     * 
     * @param reader
     * @throws IOException
     */
    <T> T parse(ToIntFunction<char[]> reader, Class<T> type) throws IOException {
        Ⅱ<char[], StringBuilder> b = P.poll();
        if (b == null) b = I.pair(new char[1024 * 4], new StringBuilder());

        this.reader = reader == null ? this::read : reader;
        this.buffer = b.ⅰ;
        this.capture = b.ⅱ;
        this.captureStart = -1;

        readUnspace();
        if (fill != -1) {
            root = value(type == null ? null : Model.of(type));
        }

        capture.setLength(0);
        P.offer(b);

        return (T) (type == null ? this : root);
    }

    /**
     * Read value.
     * 
     * @throws IOException
     */
    private Object value(Model model) throws IOException {
        switch (current) {
        // keyword
        case 'n':
            return keyword(null);
        case 't':
            return keyword("true");
        case 'f':
            return keyword("false");

        // string
        case '"':
            return string();

        // array
        case '[':
            Object array = model == null ? new LinkedHashMap() : I.make(model.type);
            readUnspace();
            if (current == ']') {
                readUnspace();
                return array;
            }

            int count = -1;
            do {
                String name = ++count <= 9 ? C[count] : Integer.toString(count);
                if (model == null) {
                    ((Map) array).put(name, value(null));
                } else {
                    Property p = model.property(name);
                    model.set(array, p, value(p.model));
                }
            } while (readSeparator(']'));
            return array;

        // object
        case '{':
            Object object = model == null ? new HashMap() : I.make(model.type);
            readUnspace();
            if (current == '}') {
                return object;
            }
            do {
                String name = string();
                if (current != ':') expected(":");
                readUnspace();
                if (model == null) {
                    ((Map) object).put(name, value(null));
                } else {
                    Property p = model.property(name);
                    if (p.model.atomic) {
                        model.set(object, p, p.model.decoder.decode((String) value(p.model)));
                    } else {
                        model.set(object, p, value(p.model));
                    }
                }
            } while (readSeparator('}'));
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
            if (current == '-') read();
            if (current == '0') {
                read();
            } else {
                digit();
            }

            // fraction
            if (current == '.') {
                read();
                digit();
            }

            // exponent
            if (current == 'e' || current == 'E') {
                read();
                if (current == '+' || current == '-') read();
                digit();
            }
            return endCapture();
        // invalid token
        default:
            return expected("value");
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
        String value = String.valueOf(keyword);

        for (int i = 0; i < value.length(); i++) {
            if (current != value.charAt(i)) expected(value);
            read();
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
        captureStart = index;

        read();
        while (current != '"') {
            if (current == '\\') {
                // pause capture
                int end = current == -1 ? index : index - 1;
                capture.append(buffer, captureStart, end - captureStart);
                captureStart = -1;

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
            } else {
                read();
            }
        }
        String string = endCapture();
        readUnspace();
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

            fill = reader.applyAsInt(buffer);
            index = 0;
            if (fill == -1) {
                return;
            }
        }
        current = buffer[index++];
    }

    /**
     * Read the next character.
     * 
     * @throws IOException
     */
    private void readUnspace() throws IOException {
        do {
            if (index == fill) {
                if (captureStart != -1) {
                    capture.append(buffer, captureStart, fill - captureStart);
                    captureStart = 0;
                }

                fill = reader.applyAsInt(buffer);
                index = 0;
                if (fill == -1) {
                    return;
                }
            }
        } while ((current = buffer[index++]) <= ' ');
    }

    /**
     * Read the specified character.
     * 
     * @param end The ending character.
     * @return A result.
     * @throws IOException
     */
    private boolean readSeparator(char end) throws IOException {
        switch (current) {
        case ',':
            readUnspace();
            return true;

        case ' ':
        case '\t':
        case '\r':
        case '\n':
            readUnspace();
            return readSeparator(end);

        default:
            if (current == end) {
                readUnspace();
            } else {
                expected(end);
            }
            return false;
        }
    }

    /**
     * Stop text capturing.
     */
    private String endCapture() {
        int end = index - 1;
        String captured;
        if (capture.length() > 0) {
            captured = capture.append(buffer, captureStart, end - captureStart).toString();
            capture.setLength(0);
        } else {
            int hash = 0;
            for (int i = captureStart; i < end; i++) {
                hash = 31 * hash + buffer[i];
            }

            // Caching each String and char[] in a separate array without using tuples would reduce
            // memory usage and footprint. However, that method is NO LONGER thread-safe: the time
            // difference between a char[] reference and a String reference allows another thread to
            // rewrite the String cache.
            // So the char[] reference and the String reference must be obtained completely
            // atomically. However, using the usual syncronize block will sacrifice speed.
            // By using tuples, we can ensure atomicity in obtaining both references.
            Ⅱ<String, char[]> cache = S[hash & 65535];
            if (cache != null && Arrays.equals(buffer, captureStart, end, cache.ⅱ, 0, cache.ⅱ.length)) {
                captured = cache.ⅰ;
            } else {
                S[hash & 65535] = I.pair(captured = new String(buffer, captureStart, end - captureStart), captured.toCharArray());
            }
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
        if (!property.transitory && property.name != null) {
            try {
                // non-first properties requires separator
                if (index++ != 0) out.append(',');

                // all properties need the properly indents
                if (0 < current) {
                    out.append('\n').append("\t".repeat(current)); // indent

                    // property key (List node doesn't need key)
                    if (model.type != List.class) {
                        write(property.name, String.class);
                        out.append(": ");
                    }
                }

                // property value
                if (property.model.atomic) {
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
                    if ((m.type.getModifiers() & Modifier.ABSTRACT) != 0 && m.getClass() == Model.class) {
                        m = Model.of(value);
                        out.append('\n').append("\t".repeat(current + 1)).append("\"#\": \"").append(m.type.getName()).append("\",");
                    }
                    m.walk(value, walker::write);
                    if (walker.index != 0) out.append('\n').append("\t".repeat(current)); // indent
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