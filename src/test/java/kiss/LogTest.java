/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LogTest {

    static Handler[] handlers;

    static Buffer buffer = new Buffer();

    @BeforeAll
    static void store() {
        handlers = I.log.getHandlers();

        for (int i = 0; i < handlers.length; i++) {
            I.log.removeHandler(handlers[i]);
        }
        buffer.setFormatter(new SimpleFormatter());
        I.log.addHandler(buffer);
        I.log.setLevel(Level.ALL);
    }

    @AfterAll
    static void restore() {
        I.log.removeHandler(buffer);

        for (int i = 0; i < handlers.length; i++) {
            I.log.addHandler(handlers[i]);
        }
    }

    @Test
    void log() {
        I.info("message");
        assert buffer.is("message");
    }

    @Test
    void debug() {
        I.debug("message");
        assert buffer.is("message");
    }

    @Test
    void error() {
        I.info(new Error("ERROR"));
        assert buffer.contains("java.lang.Error: ERROR");
    }

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
        private boolean contains(String expected) {
            return buffer.trim().contains(expected);
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
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return buffer;
        }
    }
}