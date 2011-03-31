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
 * @version 2011/03/29 14:07:03
 */
class Parser extends Reader {

    /** The actual reader. */
    private Readable readable;

    /** The latest red cache. */
    private CharBuffer last;

    /** The flag whether this reader is reseted or not. */
    boolean reset = false;

    /**
     * @param readable
     */
    Parser(Readable readable) {
        this.readable = readable;
    }

    /**
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (!reset) {
            return readable.read(last = CharBuffer.wrap(cbuf, off, len));
        } else {
            // flag off
            reset = false;

            // Read the latest buffer with JSON header text.
            return CharBuffer.wrap(cbuf, off, len).put("a=").put((CharBuffer) last.flip()).flip().limit();
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
