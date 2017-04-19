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

import javax.script.ScriptException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

/**
 * @version 2017/04/19 22:48:59
 */
class JSONParser {

    private final Reader reader;

    private final char[] buffer;

    private int bufferOffset;

    private int index;

    private int fill;

    private int line;

    private int lineOffset;

    private int current;

    private StringBuilder captureBuffer;

    private int captureStart;

    /*
     * | bufferOffset v [a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t] < input [l|m|n|o|p|q|r|s|t|?|?] <
     * buffer ^ ^ | index fill
     */

    JSONParser(Reader reader) {
        this.reader = reader;
        buffer = new char[1024];
        line = 1;
        captureStart = -1;
    }

    Object parse() throws IOException {
        read();
        space();
        Object result = readValue();
        space();
        if (!isEndOfText()) {
            throw error("Unexpected character");
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
            return readTrue();
        case 'f':
            return readFalse();
        case '"':
            return readString();
        case '[':
            return readArray();
        case '{':
            return readObject();
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
            return readNumber();
        default:
            throw expected("value");
        }
    }

    private Map readArray() throws IOException {
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
    }

    private Map readObject() throws IOException {
        read();
        Map object = new HashMap();
        space();
        if (readChar('}')) {
            return object;
        }
        do {
            space();
            String name = readName();
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
    }

    private String readName() throws IOException {
        if (current != '"') {
            throw expected("name");
        }
        return readString();
    }

    private JsonValue readTrue() throws IOException {
        read();
        readRequiredChar('r');
        readRequiredChar('u');
        readRequiredChar('e');
        return Json.TRUE;
    }

    private JsonValue readFalse() throws IOException {
        read();
        readRequiredChar('a');
        readRequiredChar('l');
        readRequiredChar('s');
        readRequiredChar('e');
        return Json.FALSE;
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
                if (!isHexDigit()) {
                    throw expected("hexadecimal digit");
                }
                hexChars[i] = (char) current;
            }
            captureBuffer.append((char) Integer.parseInt(new String(hexChars), 16));
            break;
        default:
            throw expected("valid escape sequence");
        }
        read();
    }

    private String readNumber() throws IOException {
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
        readFraction();
        readExponent();
        return endCapture();
    }

    private boolean readFraction() throws IOException {
        if (!readChar('.')) {
            return false;
        }
        if (!readDigit()) {
            throw expected("digit");
        }
        while (readDigit()) {
        }
        return true;
    }

    private boolean readExponent() throws IOException {
        if (!readChar('e') && !readChar('E')) {
            return false;
        }
        if (!readChar('+')) {
            readChar('-');
        }
        if (!readDigit()) {
            throw expected("digit");
        }
        while (readDigit()) {
        }
        return true;
    }

    private boolean readChar(char ch) throws IOException {
        if (current != ch) {
            return false;
        }
        read();
        return true;
    }

    private boolean readDigit() throws IOException {
        if (!isDigit()) {
            return false;
        }
        read();
        return true;
    }

    private void space() throws IOException {
        while (isWhiteSpace()) {
            read();
        }
    }

    private void read() throws IOException {
        if (index == fill) {
            if (captureStart != -1) {
                captureBuffer.append(buffer, captureStart, fill - captureStart);
                captureStart = 0;
            }
            bufferOffset += fill;
            fill = reader.read(buffer, 0, buffer.length);
            index = 0;
            if (fill == -1) {
                current = -1;
                return;
            }
        }
        if (current == '\n') {
            line++;
            lineOffset = bufferOffset + index;
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

    private Error expected(String expected) {
        if (isEndOfText()) {
            return error("Unexpected end of input");
        } else {
            return error("Expected " + expected);
        }
    }

    private Error error(String message) {
        int absIndex = bufferOffset + index;
        int column = absIndex - lineOffset;
        int offset = isEndOfText() ? absIndex : absIndex - 1;
        throw I.quiet(new ScriptException(message));
    }

    private boolean isWhiteSpace() {
        return current == ' ' || current == '\t' || current == '\n' || current == '\r';
    }

    private boolean isDigit() {
        return current >= '0' && current <= '9';
    }

    private boolean isHexDigit() {
        return current >= '0' && current <= '9' || current >= 'a' && current <= 'f' || current >= 'A' && current <= 'F';
    }

    private boolean isEndOfText() {
        return current == -1;
    }
}
