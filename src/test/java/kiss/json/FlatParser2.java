/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import kiss.I;
import kiss.WiseFunction;
import kiss.Ⅱ;
import kiss.model.Model;
import kiss.model.Property;

public class FlatParser2 {

    /** Reuse buffers. */
    private static final ArrayBlockingQueue<Ⅱ<char[], StringBuilder>> P = new ArrayBlockingQueue(16);

    /** Reuse text symbol. */
    private static final Ⅱ<String, char[]>[] S = new Ⅱ[65536];

    /** Reuse array's index to reduce GC execution. */
    private static final String[] C = "0123456789".split("");

    private static final Map<Model, WiseFunction<FlatParser2, Object>> readers = Map
            .of(Model.of(int.class), FlatParser2::readInt, Model.of(String.class), FlatParser2::readString);

    private Reader reader;

    private int current = 0;

    private int index;

    private char[] buffer;

    private int fill;

    private Object root;

    /** The capturing text. */
    private StringBuilder capture;

    /** The capture index in input buffer. */
    private int captureStart;

    public Object parse(Reader reader, Class type) throws IOException {
        this.reader = reader;

        Ⅱ<char[], StringBuilder> b = P.poll();
        if (b == null) b = I.pair(new char[1024 * 4], new StringBuilder());
        this.buffer = b.ⅰ;
        this.capture = b.ⅱ;
        captureStart = -1;

        root = readObject(Model.of(type));

        capture.setLength(0);
        P.offer(b);

        return root;
    }

    private Object readObject(Model model) throws IOException {
        readUnspace();

        switch (current) {
        case '[':
            Model next = model.property(C[0]).model;
            List array = (List) I.make(model.type);
            do {
                array.add(readObject(next));
                readUnspace();
            } while (current == ',');
            return array;

        case '{':
            Object map = I.make(model.type);
            do {
                Property p = model.property(readString());
                read(':');

                WiseFunction<FlatParser2, Object> reader = readers.get(p.model);
                model.set(map, p, reader == null ? readObject(p.model) : reader.apply(this));

                readUnspace();
            } while (current == ',');
            return map;

        default:
            throw new Error(String.valueOf((char) current));
        }
    }

    private void read(char c) throws IOException {
        readUnspace();
        if (current != c) throw new Error("Expected " + c);
    }

    private Integer readInt() throws IOException {
        readUnspace();

        while ('0' <= current && current <= '9') {
            read();
        }

        return Integer.MAX_VALUE;
    }

    private String readString() throws IOException {
        readUnspace();

        if (current == '"') {
            captureStart = index;

            do {
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
                        throw new Error();
                    }
                    read();

                    // start capture
                    captureStart = index - 1;
                } else {
                    read();
                }
            } while (current != '"');

            return endCapture();
        } else {
            throw new Error();
        }
    }

    /**
     * Stop text capturing.
     */
    private String endCapture() {
        int end = current == -1 ? index : index - 1;
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

            fill = reader.read(buffer);
            index = 0;
            if (fill == -1) {
                System.out.println("OK");
                current = -1;
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

                fill = reader.read(buffer);
                index = 0;
                if (fill == -1) {
                    System.out.println("OK2");
                    current = -1;
                    return;
                }
            }
        } while ((current = buffer[index++]) <= ' ');
    }
}