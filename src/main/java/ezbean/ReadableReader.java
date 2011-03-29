/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * @version 2011/03/29 10:02:50
 */
class ReadableReader extends Reader {

    /** The actual reader. */
    private Readable readable;

    /** The appendable header. */
    private String header;

    /**
     * @param readable
     * @param header
     */
    ReadableReader(Readable readable, String header) {
        this.readable = readable;
        this.header = header;
    }

    /**
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (header == null) {
            return readable.read(CharBuffer.wrap(cbuf, off, len));
        } else {
            String text = header;
            int length = text.length();

            // reset
            header = null;

            // insert header text
            for (int i = 0; i < length; i++) {
                cbuf[i] = text.charAt(i);
            }

            // read normally
            return readable.read(CharBuffer.wrap(cbuf, off + length, len - length)) + length;
        }
    }

    /**
     * @see java.io.Reader#close()
     */
    @Override
    public void close() throws IOException {
        I.quiet(readable);
    }
}
