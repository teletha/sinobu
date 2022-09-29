/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
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
    private static final ArrayBlockingQueue<char[]> P = new ArrayBlockingQueue(16);

    /** Reuse text symbol. */
    private static final Ⅱ<String, char[]>[] S = new Ⅱ[65536];

    /** Reuse array's index to reduce GC execution. */
    private static final String[] C = "0123456789".split("");

    private static final Map<Class, WiseFunction<FlatParser2, Object>> readers = Map
            .of(int.class, FlatParser2::readInt, String.class, FlatParser2::readString);

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

        char[] b = P.poll();
        this.buffer = b == null ? new char[1024 * 4] : b;
        capture = new StringBuilder();
        captureStart = -1;

        root = readObject(Model.of(type));
        P.offer(buffer);

        return root;
    }

    private Object readObject(Model model) throws IOException {
        readUnspace();

        if (current == '{') {
            Object map = I.make(model.type);
            do {
                Property p = model.property(readString());
                readUnspace();
                if (current != ':') throw new Error();

                Object value;
                WiseFunction<FlatParser2, Object> reader = readers.get(p.model.type);
                if (reader == null) {
                    value = readObject(p.model);
                } else {
                    value = reader.apply(this);
                }
                model.set(map, p, value);

                readUnspace();
            } while (current == ',');
            return map;
        } else if (current == '[') {
            Property p = model.property(C[0]);
            List array = (List) I.make(model.type);
            do {
                array.add(readObject(p.model));

                readUnspace();
            } while (current == ',');
            return array;
        } else {
            throw new Error(String.valueOf((char) current));
        }
    }

    private Integer readInt() throws IOException {
        readUnspace();

        captureStart = index;

        while ('0' <= current && current <= '9') {
            read();
        }

        return Integer.valueOf(endCapture());
    }

    private String readString() throws IOException {
        readUnspace();

        if (current == '"') {
            captureStart = index;

            do {
                read();
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
                P.offer(buffer);
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
                    P.offer(buffer);
                    return;
                }
            }
            current = buffer[index++];
        } while (current <= ' ');
    }
}
