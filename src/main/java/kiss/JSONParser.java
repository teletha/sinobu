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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @version 2017/04/19 22:48:59
 */
class JSONParser {

    private final Reader reader;

    private final char[] buffer;

    private int index;

    private int fill;

    private int current;

    private StringBuilder captureBuffer;

    private int captureStart;

    JSONParser(Reader reader) {
        this.reader = reader;
        buffer = new char[1024];
        captureStart = -1;
    }

    Object parse() throws IOException {
        read();
        space();
        Object result = readValue();
        space();

        if (current != -1) {
            throw new IllegalStateException("Unexpected character");
        }
        return result;
    }

    private Object readValue() throws IOException {
        switch (current) {
        case 'n':
            read();
            readRequiredChar('u');
            readRequiredChar('l');
            readRequiredChar('l');
            return null;

        case 't':
            read();
            readRequiredChar('r');
            readRequiredChar('u');
            readRequiredChar('e');
            return true;

        case 'f':
            read();
            readRequiredChar('a');
            readRequiredChar('l');
            readRequiredChar('s');
            readRequiredChar('e');
            return false;

        case '"':
            return readString();

        case '[':
            read();
            Map array = new LinkedHashMap();
            space();
            if (readChar(']')) {
                return array;
            }

            int count = 0;
            do {
                space();
                array.put(String.valueOf(count++), readValue());
                space();
            } while (readChar(','));

            if (!readChar(']')) {
                throw expected("',' or ']'");
            }
            return array;

        case '{':
            read();
            Map object = new HashMap();
            space();
            if (readChar('}')) {
                return object;
            }
            do {
                space();

                if (current != '"') {
                    throw expected("name");
                }
                String name = readString();
                space();
                if (!readChar(':')) {
                    throw expected("':'");
                }
                space();
                object.put(name, readValue());
                space();
            } while (readChar(','));
            if (!readChar('}')) {
                throw expected("',' or '}'");
            }
            return object;

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
            readChar('-');
            int firstDigit = current;
            if (!readDigit()) {
                throw expected("digit");
            }
            if (firstDigit != '0') {
                while (readDigit()) {
                }
            }

            // fraction
            if (readChar('.')) {
                if (!readDigit()) {
                    throw expected("digit");
                }
                while (readDigit()) {
                }
            }

            // exponet
            if (readChar('e') || readChar('E')) {
                if (!readChar('+')) {
                    readChar('-');
                }
                if (!readDigit()) {
                    throw expected("digit");
                }
                while (readDigit()) {
                }
            }
            return endCapture();

        default:
            throw expected("value");
        }
    }

    private void readRequiredChar(char ch) throws IOException {
        if (!readChar(ch)) {
            throw expected("'" + ch + "'");
        }
    }

    private String readString() throws IOException {
        read();
        startCapture();
        while (current != '"') {
            if (current == '\\') {
                pauseCapture();
                readEscape();
                startCapture();
            } else if (current < 0x20) {
                throw expected("valid string character");
            } else {
                read();
            }
        }
        String string = endCapture();
        read();
        return string;
    }

    private void readEscape() throws IOException {
        read();
        switch (current) {
        case '"':
        case '/':
        case '\\':
            captureBuffer.append((char) current);
            break;
        case 'b':
            captureBuffer.append('\b');
            break;
        case 'f':
            captureBuffer.append('\f');
            break;
        case 'n':
            captureBuffer.append('\n');
            break;
        case 'r':
            captureBuffer.append('\r');
            break;
        case 't':
            captureBuffer.append('\t');
            break;
        case 'u':
            char[] hexChars = new char[4];
            for (int i = 0; i < 4; i++) {
                read();
                if (current >= '0' && current <= '9' || current >= 'a' && current <= 'f' || current >= 'A' && current <= 'F') {
                    hexChars[i] = (char) current;
                } else {
                    throw expected("hexadecimal digit");
                }
            }
            captureBuffer.append((char) Integer.parseInt(new String(hexChars), 16));
            break;
        default:
            throw expected("valid escape sequence");
        }
        read();
    }

    private boolean readChar(char ch) throws IOException {
        if (current != ch) {
            return false;
        }
        read();
        return true;
    }

    private boolean readDigit() throws IOException {
        if ('0' <= current && current <= '9') {
            read();
            return true;
        } else {
            return false;
        }
    }

    private void space() throws IOException {
        while (current == ' ' || current == '\t' || current == '\n' || current == '\r') {
            read();
        }
    }

    private void read() throws IOException {
        if (index == fill) {
            if (captureStart != -1) {
                captureBuffer.append(buffer, captureStart, fill - captureStart);
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

    private void startCapture() {
        if (captureBuffer == null) {
            captureBuffer = new StringBuilder();
        }
        captureStart = index - 1;
    }

    private void pauseCapture() {
        int end = current == -1 ? index : index - 1;
        captureBuffer.append(buffer, captureStart, end - captureStart);
        captureStart = -1;
    }

    private String endCapture() {
        int end = current == -1 ? index : index - 1;
        String captured;
        if (captureBuffer.length() > 0) {
            captureBuffer.append(buffer, captureStart, end - captureStart);
            captured = captureBuffer.toString();
            captureBuffer.setLength(0);
        } else {
            captured = new String(buffer, captureStart, end - captureStart);
        }
        captureStart = -1;
        return captured;
    }

    private IllegalStateException expected(String expected) {
        if (current == -1) {
            return new IllegalStateException("Unexpected end of input");
        } else {
            return new IllegalStateException("Expected " + expected);
        }
    }
}
