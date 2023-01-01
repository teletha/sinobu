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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import kiss.I;
import kiss.Ⅱ;

public class FlatParser {

    /** Reuse buffers. */
    private static final ArrayBlockingQueue<char[]> P = new ArrayBlockingQueue(16);

    /** Reuse text symbol. */
    private static final Ⅱ<String, char[]>[] S = new Ⅱ[65536];

    private String reader;

    private int current = 0;

    private int max = 0;

    private char[] chars;

    private int mode = 0;

    private int textStart = 0;

    private int numStart = 0;

    private String key;

    private Object value;

    Map root;

    public FlatParser(String reader) throws IOException {
        this.reader = reader;

        char[] b = P.poll();
        this.chars = b == null ? new char[1024 * 4] : b;

        root = parse(null, false);
    }

    <T> T parse(T object, boolean isKey) throws IOException {
        while (true) {
            if (current == max) {
                if (max != 0) {
                    P.offer(chars);
                    return object;
                }

                current = 0;
                reader.getChars(0, reader.length(), chars, 0);
                max = reader.length();
                if (max == -1) {
                    P.offer(chars);
                    return object;
                }
            }

            char c = chars[current++];

            switch (mode) {
            // ===================================
            // String Mode
            // ===================================
            case 1:
                switch (c) {
                case '"':
                    mode = 0;
                    String text;
                    int hash = 0;
                    for (int i = textStart; i < current; i++) {
                        hash = 31 * hash + chars[i];
                    }

                    // Caching each String and char[] in a separate array without using tuples would
                    // reduce
                    // memory usage and footprint. However, that method is NO LONGER thread-safe:
                    // the time
                    // difference between a char[] reference and a String reference allows another
                    // thread to
                    // rewrite the String cache.
                    // So the char[] reference and the String reference must be obtained completely
                    // atomically. However, using the usual syncronize block will sacrifice speed.
                    // By using tuples, we can ensure atomicity in obtaining both references.
                    Ⅱ<String, char[]> cache = S[hash & 65535];
                    if (cache != null && Arrays.equals(chars, textStart, current - 1, cache.ⅱ, 0, cache.ⅱ.length)) {
                        text = cache.ⅰ;
                    } else {
                        S[hash & 65535] = I.pair(text = new String(chars, textStart, current - 1 - textStart), text.toCharArray());
                    }

                    if (isKey) {
                        key = text;
                    } else {
                        value = text;
                    }
                    break;

                default:
                    break;
                }
                break;

            // ===================================
            // Number Mode
            // ===================================
            case 2:
                switch (c) {
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
                case '-':
                case '+':
                case '.':
                case 'e':
                    break;

                default:
                    mode = 0;
                    String num = new String(chars, numStart, current - 1 - numStart);
                    if (isKey) {
                        key = num;
                    } else {
                        value = num;
                    }
                    break;
                }
                // fall-though

                // ===================================
                // Normal Mode
                // ===================================
            default:
                switch (c) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    break;

                case '{':
                    object = (T) parse(new HashMap(), true);
                    break;

                case ',':
                    isKey = true;
                    ((Map) object).put(key, value);
                    break;

                case ']':
                case '}':
                    if (key != null) {
                        ((Map) object).put(key, value);
                    }
                    return object;

                case ':':
                    isKey = false;
                    break;

                case '"':
                    mode = 1;
                    textStart = current;
                    break;

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
                case '-':
                case '+':
                case '.':
                    mode = 2;
                    numStart = current - 1;
                    break;

                case 't':
                    if (4 <= max - current && chars[current] == 'r' && chars[current + 1] == 'u' && chars[current + 2] == 'e') {
                        value = Boolean.TRUE;
                        current += 3;
                    }
                    break;

                case 'f':
                    if (5 <= max - current && chars[current] == 'a' && chars[current + 1] == 'l' && chars[current + 2] == 's' && chars[current + 3] == 'e') {
                        value = Boolean.FALSE;
                        current += 4;
                    }
                    break;

                default:
                    break;
                }
                break;
            }
        }
    }
}