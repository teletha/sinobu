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

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @version 2017/03/24 22:12:00
 */
public class LogTest {

    private static Handler[] handlers;

    private static Buffer buffer = new Buffer();

    @BeforeClass
    public static void store() {
        buffer.setFormatter(new Log());

        handlers = I.$logger.getHandlers();

        for (int i = 0; i < handlers.length; i++) {
            I.$logger.removeHandler(handlers[i]);
        }
        I.$logger.addHandler(buffer);
        I.$logger.setLevel(Level.ALL);
    }

    @AfterClass
    public static void restore() {
        I.config(new ConsoleHandler());
    }

    @Test
    public void log() throws Exception {
        I.log("message");
        assert buffer.is("message");

        I.log("param %s %s %s", 1, 2, 3);
        assert buffer.is("param 1 2 3");
    }

    @Test
    public void alert() throws Exception {
        I.alert("message");
        assert buffer.is("message");

        I.alert("param %s %s %s", 1, 2, 3);
        assert buffer.is("param 1 2 3");
    }

    @Test
    public void warn() throws Exception {
        I.warn("message");
        assert buffer.is("message");

        I.warn("param %s %s %s", 1, 2, 3);
        assert buffer.is("param 1 2 3");
    }

    @Test
    public void debug() throws Exception {
        I.debug("message");
        assert buffer.is("message");

        I.debug("param %s %s %s", 1, 2, 3);
        assert buffer.is("param 1 2 3");
    }

    /**
     * @version 2017/03/24 22:15:48
     */
    private static class Buffer extends Handler {

        private String buffer;

        /**
         * {@inheritDoc}
         */
        @Override
        public void publish(LogRecord record) {
            buffer = getFormatter().format(record);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void flush() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws SecurityException {
        }

        private boolean is(String expected) {
            return buffer.trim().endsWith(expected);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return buffer;
        }
    }
}
