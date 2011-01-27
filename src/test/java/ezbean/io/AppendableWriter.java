/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.io;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * @version 2010/01/08 11:46:58
 */
public class AppendableWriter extends Writer {

    /** The actual output. */
    private final Appendable appendable;

    /**
     * @param appendable
     */
    public AppendableWriter(Appendable appendable) {
        this.appendable = appendable;
    }

    /**
     * @see java.io.Writer#close()
     */
    @Override
    public void close() throws IOException {
        FileSystem.close(appendable);
    }

    /**
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush() throws IOException {
        if (appendable instanceof Flushable) {
            ((Flushable) appendable).flush();
        }
    }

    /**
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        appendable.append(new String(cbuf, off, len));
    }
}