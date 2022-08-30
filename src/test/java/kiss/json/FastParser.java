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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import kiss.I;
import kiss.JSON;
import kiss.Ⅱ;
import kiss.model.Model;
import kiss.model.Property;

public class FastParser {

    /** Reuse buffers. */
    private static final ArrayBlockingQueue<char[]> P = new ArrayBlockingQueue(16);

    /** Reuse array's index to reduce GC execution. */
    private static final String[] C = "0123456789".split("");

    /** Reuse text symbol. */
    private static final Ⅱ<String, char[]>[] S = new Ⅱ[65536];

    private char[] buffer;

    private Reader reader;

    private int fill;

    private int index;

    private int current;

    /** The capturing text. */
    private StringBuilder capture;

    /** The capture index in input buffer. */
    private int captureStart;

    /**
     * @return
     * @throws IOException
     */
    public <T> T parse(Reader reader, Class<T> type) throws IOException {
        char[] b = P.poll();
        this.buffer = b == null ? new char[1024 * 4] : b;
        this.reader = reader;
        this.captureStart = -1;
        this.capture = new StringBuilder();

        readUnspace();
        if (fill != -1) {
            return (T) value(type == JSON.class ? null : Model.of(type));
        }

        return null;
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
            Object list = model == null ? new LinkedHashMap() : I.make(model.type);
            readUnspace();
            if (current == ']') {
                readUnspace();
                return list;
            }

            int count = -1;
            do {
                String name = ++count <= 9 ? C[count] : Integer.toString(count);
                if (model == null) {
                    ((Map) list).put(name, value(null));
                } else {
                    Property p = model.property(name);
                    model.set(list, p, value(p.model));
                }
            } while (readSeparator(']'));
            return list;

        // object
        case '{':
            Object obj = model == null ? new HashMap() : I.make(model.type);
            readUnspace();
            if (current == '}') {
                return obj;
            }
            do {
                String name = string();
                if (current != ':') expected(":");
                readUnspace();

                if (model == null) {
                    ((Map) obj).put(name, value(null));
                } else {
                    Property p = model.property(name);
                    if (p.model.atomic) {
                        model.set(obj, p, I.transform(value(p.model), p.model.type));
                    } else {
                        model.set(obj, p, value(p.model));
                    }
                }
            } while (readSeparator('}'));
            return obj;

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
        read();

        // start capture
        captureStart = index - 1;
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
            fill = reader.read(buffer, 0, buffer.length);
            index = 0;
            if (fill == -1) {
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
                fill = reader.read(buffer, 0, buffer.length);
                index = 0;
                if (fill == -1) {
                    current = -1;
                    P.offer(buffer);
                    return;
                }
            }
            current = buffer[index++];
        } while (current == ' ' || current == '\t' || current == '\n' || current == '\r');
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
     * Throw parsing error.
     * 
     * @param expected A reason.
     * @return This method NEVER return value.
     */
    private Object expected(Object expected) {
        throw new IllegalStateException("Expected ".concat(String.valueOf(expected)));
    }
}
