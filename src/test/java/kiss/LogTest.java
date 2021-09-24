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

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
class LogTest {

    private PrintStream original;

    private Log log;

    private static class Log extends PrintStream {

        private final Deque<Entry> entries = new ArrayDeque();

        private StringBuilder line = new StringBuilder();

        private Log() {
            super(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PrintStream append(CharSequence csq) {
            for (int i = 0; i < csq.length(); i++) {
                handle(csq.charAt(i));
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PrintStream append(char c) {
            handle(c);
            return this;
        }

        private void handle(char c) {
            if (c == '\n') {
                String text = line.toString();
                if (!text.isBlank()) {
                    entries.add(new Entry(text));
                }
                line.setLength(0);
            } else {
                line.append(c);
            }
        }
    }

    private static class Entry {

        private Level level;

        private String message;

        private Entry(String log) {
            if (log.startsWith("\tat ")) {
                message = log.trim();
            } else {
                int start = log.indexOf(' ', log.indexOf(' ') + 1) + 1;
                int end = log.indexOf('\t', start + 1);

                level = Level.valueOf(log.substring(start, end));
                message = log.substring(end + 1);
            }
        }
    }

    @BeforeEach
    void store() {
        original = System.out;
        System.setOut(log = new Log());

        I.LogFile = Level.OFF;
        I.LogConsole = Level.ALL;
    }

    @AfterEach
    void restore() {
        System.setOut(original);

        I.LogFile = Level.ALL;
        I.LogConsole = Level.INFO;
        I.LogCaller = Level.OFF;
    }

    @Test
    void logString() {
        I.info("TEXT");

        assert assumeLog(Level.INFO, "TEXT");
    }

    @Test
    void logObject() {
        I.info(new Object() {
            @Override
            public String toString() {
                return "From ToString";
            }
        });

        assert assumeLog(Level.INFO, "From ToString");
    }

    @Test
    void logSupplier() {
        I.info((Supplier) () -> "From Supplier");

        assert assumeLog(Level.INFO, "From Supplier");
    }

    @Test
    void logThrowable() {
        I.info(new Error("From Error"));

        assert assumeLog(Level.INFO, "java.lang.Error: From Error");
        assert assumeStackTrace("at kiss.LogTest.logThrowable(LogTest.java:1)");
    }

    @Test
    void trace() {
        I.trace("Message");

        assert assumeLog(Level.TRACE, "Message");
    }

    @Test
    void debug() {
        I.debug("Message");

        assert assumeLog(Level.DEBUG, "Message");
    }

    @Test
    void info() {
        I.info("Message");

        assert assumeLog(Level.INFO, "Message");
    }

    @Test
    void warn() {
        I.warn("Message");

        assert assumeLog(Level.WARNING, "Message");
    }

    @Test
    void error() {
        I.error("Message");

        assert assumeLog(Level.ERROR, "Message");
    }

    @Test
    void callerInfomation() {
        I.LogCaller = Level.ALL;
        I.error("Message");

        assert assumeLog(Level.ERROR, "Message at kiss.LogTest.callerInfomation(LogTest.java:0)");
    }

    @Test
    void loggerName() {
        I.info("name", "Message");

        assert assumeLog(Level.INFO, "Message");
    }

    @Test
    void file() {
        I.LogFile = Level.ALL;
    }

    @Test
    void filterByLoggerLevel() {
        I.env("filtered-logger.level", "ERROR");

        I.info("filtered-logger", "NoOP");
        assert assumeNoLog();

        I.error("filtered-logger", "Message");
        assert assumeLog(Level.ERROR, "Message");
    }

    @Test
    void filterByGlobalConsoleLevel() {
        I.LogConsole = Level.ERROR;

        I.warn("NoOP");
        I.info("NoOP");
        I.debug("NoOP");
        I.trace("NoOP");
        assert assumeNoLog();

        I.error("Message");
        assert assumeLog(Level.ERROR, "Message");

        // change level dynamically
        I.LogConsole = Level.DEBUG;

        I.trace("NoOP");
        assert assumeNoLog();

        I.error("Message");
        I.warn("Message");
        I.info("Message");
        I.debug("Message");
        assert assumeLog(Level.ERROR, "Message");
        assert assumeLog(Level.WARNING, "Message");
        assert assumeLog(Level.INFO, "Message");
        assert assumeLog(Level.DEBUG, "Message");
    }

    @Test
    void filterByGlobalFileLevel() {

    }

    private boolean assumeLog(Level level, String message) {
        awaitLogProcess();

        Entry entry = log.entries.pop();
        assert entry.level == level;
        assert normalize(entry.message).equals(normalize(message));

        return true;
    }

    private boolean assumeNoLog() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw I.quiet(e);
        }
        assert log.entries.isEmpty();

        return true;
    }

    private boolean assumeStackTrace(String message) {
        awaitLogProcess();

        Entry entry = log.entries.pop();
        assert normalize(entry.message).equals(normalize(message));

        return true;
    }

    private String normalize(String message) {
        return message.replaceAll(":\\d+", ":0").replaceAll("\t", " ");
    }

    private void awaitLogProcess() {
        while (log.entries.size() == 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }
    }
}