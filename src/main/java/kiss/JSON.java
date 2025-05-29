/*
 * Copyright (C) 2024 The SINOBU Development Team
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
import java.io.Serializable;
import java.io.StringReader;
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

@SuppressWarnings("serial")
public class JSON implements Serializable {

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
     * @param model A model type.
     * @return A created model.
     */
    public <M> M as(Model<M> model) {
        return as(model, I.make(model.type), root);
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
        return root instanceof Map && ((Map) root).containsKey(key);
    }

    /**
     * Check the direct child value with the specified key.
     * 
     * @param key A target key.
     * @param value An expected value.
     * @return A result.
     */
    public boolean has(String key, Object value) {
        return root instanceof Map && Objects.equals(((Map) root).get(key), String.valueOf(value));
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
     * @param type The conversion type.
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

        // don't use items.replaceAll to reduce code size and execution speed
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
        I.write(new MapModel(root), root, capture = new StringBuilder());
        return capture.toString();
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
     * Parser the given json.
     * 
     * @param <T>
     * @param reader A input stream.
     * @param text An input text.
     * @param type A model type.
     * @return
     * @throws IOException
     */
    <T> T parse(Reader reader, String text, Class<T> type) throws IOException {
        Ⅱ<char[], StringBuilder> b = P.poll();
        if (b == null) b = I.pair(new char[4096], new StringBuilder());

        this.buffer = b.ⅰ;
        this.capture = b.ⅱ;
        this.captureStart = -1;

        if (reader == null) {
            if (text.length() <= 4096) {
                this.fill = text.length();
                text.getChars(0, fill, buffer, 0);
            } else {
                this.reader = new StringReader(text);
            }
        } else {
            this.reader = reader;
        }

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
        if (current == '"') {
            return string();
        } else if (current == '{') {
            Object object = model == null ? new HashMap() : I.make(model.type);
            readUnspace();
            if (current == '}') {
                readUnspace();
                return object;
            }
            do {
                if (current != '"') expected('"');
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
        } else if (current == '[') {
            Object array = model == null ? new LinkedHashMap() : I.make(model.type);
            readUnspace();
            if (current == ']') {
                readUnspace();
                return array;
            }

            int count = -1;
            do {
                if (model == null) {
                    String name = ++count <= 9 ? C[count] : Integer.toString(count);
                    ((Map) array).put(name, value(null));
                } else {
                    ((List) array).add(value(((ListModel) model).item));
                }
            } while (readSeparator(']'));
            return array;
        } else if ((current >= '0' && current <= '9') || current == '-') {
            captureStart = index - 1;

            while (true) {
                if (index == fill) fill(0);
                current = buffer[index++];
                if (('0' <= current && current <= '9') || current == '.' || current == '-' || current == '+' || current == 'e' || current == 'E') {
                    continue;
                } else {
                    break;
                }
            }
            return endCapture();
        } else if (current == 't') {
            if (index + 3 > fill) fill(3);
            if (buffer[index++] == 'r' && buffer[index++] == 'u' && buffer[index++] == 'e') {
                readUnspace();
                return "true";
            } else {
                expected("true");
            }
        } else if (current == 'f') {
            if (index + 4 > fill) fill(4);
            if (buffer[index++] == 'a' && buffer[index++] == 'l' && buffer[index++] == 's' && buffer[index++] == 'e') {
                readUnspace();
                return "false";
            } else {
                expected("false");
            }
        } else if (current == 'n') {
            if (index + 3 > fill) fill(3);
            if (buffer[index++] == 'u' && buffer[index++] == 'l' && buffer[index++] == 'l') {
                readUnspace();
                return null;
            } else {
                expected(null);
            }
        } else if (current == 0x00) {
            return null;
        }
        return expected("value");
    }

    /**
     * Read the sequence of String.
     * 
     * @return A parsed string.
     * @throws IOException
     */
    private String string() throws IOException {
        captureStart = index;

        while (true) {
            if (index == fill) fill(0);

            char ch = buffer[index++];
            if (ch == '"') break;
            if (ch == '\\') {
                // pause capture
                capture.append(buffer, captureStart, index - 1 - captureStart);
                captureStart = -1;

                if (index == fill) fill(0);
                switch (buffer[index++]) {
                case '"':
                case '/':
                case '\\':
                    capture.append(buffer[index - 1]);
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
                        if (index == fill) fill(0);
                        chars[i] = buffer[index++];
                    }
                    capture.append((char) Integer.parseInt(new String(chars), 16));
                    break;
                default:
                    expected("escape sequence");
                }

                // start capture
                captureStart = index;
            }
        }

        String string = endCapture();
        readUnspace();
        return string;
    }

    private void fill(int req) throws IOException {
        if (reader == null) {
            return;
        }

        if (captureStart != -1) {
            capture.append(buffer, captureStart, fill - captureStart);
            captureStart = 0;
        }

        int remain = fill - index;
        if (0 < remain) System.arraycopy(buffer, index, buffer, 0, remain);

        fill = reader.read(buffer, remain, buffer.length - remain) + remain;
        if (fill < req) expected("shortage");

        index = 0;
    }

    /**
     * Read the next character.
     * 
     * @throws IOException
     */
    private void readUnspace() throws IOException {
        // Skip consecutive spaces in the current buffer at once
        while (index < fill) {
            current = buffer[index++];
            if (current > ' ') {
                return;
            }
        }

        // Processing when the end of buffer is reached
        if (reader == null) {
            current = 0;
            return;
        }

        if (captureStart != -1) {
            capture.append(buffer, captureStart, fill - captureStart);
            captureStart = 0;
        }

        fill = reader.read(buffer);
        index = 0;
        if (fill == -1) {
            current = 0;
            return;
        }

        // Skip contiguous spaces, even in new buffers
        readUnspace();
    }

    /**
     * Read the specified character.
     * 
     * @param end The ending character.
     * @return A result.
     * @throws IOException
     */
    private boolean readSeparator(int end) throws IOException {
        while (true) {
            if (current == ',') {
                readUnspace();
                return true;
            } else if (current == end) {
                readUnspace();
                return false;
            } else if (current == ' ' || current == '\n' || current == '\t' || current == '\r') {
                // continue
                readUnspace();
            } else {
                expected(end);
            }
        }
    }

    /**
     * Stop text capturing.
     */
    private String endCapture() {
        int end = index - 1;
        int len = end - captureStart;
        String captured;
        if (capture.length() > 0) {
            captured = capture.append(buffer, captureStart, len).toString();
            capture.setLength(0);
        } else if (len > 12) {
            captured = new String(buffer, captureStart, len);
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
            // atomically. However, using the usual synchronize block will sacrifice speed.
            // By using tuples, we can ensure atomicity in obtaining both references.
            Ⅱ<String, char[]> cache = S[hash & 65535];
            if (cache != null && Arrays.equals(buffer, captureStart, end, cache.ⅱ, 0, cache.ⅱ.length)) {
                captured = cache.ⅰ;
            } else {
                S[hash & 65535] = I.pair(captured = new String(buffer, captureStart, len), captured.toCharArray());
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
    /** The character sequence for output as JSON. */
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
                if (0 < fill) {
                    out.append('\n').append("\t".repeat(fill)); // indent

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
                    if (64 < fill) throw new ClassCircularityError();

                    JSON walker = new JSON(out);
                    walker.fill = fill + 1;
                    out.append(property.model.type == List.class ? '[' : '{');
                    Model<Object> m = property.model;
                    if ((m.type.getModifiers() & Modifier.ABSTRACT) != 0 && m.getClass() == Model.class) {
                        m = Model.of(value);
                        out.append('\n').append("\t".repeat(fill + 1)).append("\"#\": \"").append(m.type.getName()).append("\",");
                    }
                    m.walk(value, walker::write);
                    if (walker.index != 0) out.append('\n').append("\t".repeat(fill)); // indent
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