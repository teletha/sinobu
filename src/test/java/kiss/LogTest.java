/*
 * Copyright (C) 2018 Nameless Production Committee
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
import java.util.logging.SimpleFormatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @version 2018/06/25 12:43:43
 */
class LogTest {

    static Handler[] handlers;

    static Buffer buffer = new Buffer();

    @BeforeAll
    static void store() {
        buffer.setFormatter(new SimpleFormatter());

        handlers = I.log.getHandlers();

        for (int i = 0; i < handlers.length; i++) {
            I.log.removeHandler(handlers[i]);
        }
        I.log.addHandler(buffer);
        I.log.setLevel(Level.ALL);
    }

    @AfterAll
    static void restore() {
        I.log.addHandler(new ConsoleHandler());
    }

    @Test
    void error() throws Exception {
        I.error("message");
        assert buffer.is("message");

        I.error("param {0} {1} {2}", 1, 2, 3);
        assert buffer.is("param 1 2 3");
    }

    @Test
    void info() throws Exception {
        I.info("message");
        assert buffer.is("message");

        I.info("param {0} {1} {2}", 1, 2, 3);
        assert buffer.is("param 1 2 3");
    }

    /**
     * @version 2018/06/25 9:04:29
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

        /**
         * Matching.
         * 
         * @param expected
         * @return
         */
        private boolean is(String expected) {
            return buffer.trim().endsWith(expected);
        }

        /**
         * Matching.
         * 
         * @param expected
         * @return
         */
        private boolean isNot(String expected) {
            return !is(expected);
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