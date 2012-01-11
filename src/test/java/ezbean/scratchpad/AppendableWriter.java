/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.scratchpad;

import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

import ezbean.I;

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
        I.quiet(appendable);
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