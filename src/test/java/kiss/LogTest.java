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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import antibug.CleanRoom;

@Execution(ExecutionMode.SAME_THREAD)
class LogTest {

    private PrintStream original;

    private Log log;

    @RegisterExtension
    CleanRoom room = new CleanRoom();

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
                int start = log.indexOf(' ', log.indexOf('T') + 1) + 1;
                int end = log.indexOf('\t', start + 1);

                String priority = log.substring(start, end).trim();
                level = priority.equals("WARN") ? Level.WARNING : Level.valueOf(priority);
                message = log.substring(end + 1);
            }
        }
    }

    @BeforeEach
    void store() {
        original = System.out;
        System.setOut(log = new Log());

        I.env("*.file", Level.OFF);
        I.env("*.console", Level.ALL);
    }

    @AfterEach
    void restore() {
        System.setOut(original);
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
        I.env("ci.caller", Level.ALL);
        I.error("ci", "Message");

        assert assumeLog(Level.ERROR, "Message at kiss.LogTest.callerInfomation(LogTest.java:0)");
    }

    @Test
    void loggerName() {
        I.info("name", "Message");

        assert assumeLog(Level.INFO, "Message");
    }

    @Test
    void filterByLoggerLevel() {
        I.env("filterByLoggerLevel.console", Level.ERROR);

        I.info("filterByLoggerLevel", "NoOP");
        assert assumeNoLog();

        I.error("filterByLoggerLevel", "Message");
        assert assumeLog(Level.ERROR, "Message");
    }

    @Test
    void configureLogFileDirectory() {
        String loggerName = "change-directory";
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        Path logDirectory = room.locate("xyz");
        Path logFile = logDirectory.resolve(loggerName + date + ".log");
        assert Files.notExists(logFile);

        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", logDirectory.toString());

        I.info(loggerName, "Create log file in the specified directory.");
        assert Files.exists(logFile);
    }

    @Test
    void configureLogFileAppendMode() throws IOException {
        String loggerName = "append-log";
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", room.root.toString());

        Path logFile = room.locateFile(loggerName + date + ".log", "Pre-written string\n");
        assert Files.readAllLines(logFile).get(0).equals("Pre-written string");

        // set to append mode
        I.env(loggerName + ".append", true);

        // write log
        I.info(loggerName, "This logger will append the log to an existing file.");

        assert Files.readAllLines(logFile).get(0).equals("Pre-written string");
        assert Files.readAllLines(logFile).get(1).endsWith("This logger will append the log to an existing file.");
    }

    @Test
    void configureLogFileOverwriteMode() throws IOException {
        String loggerName = "overwrite-log";
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", room.root.toString());

        Path logFile = room.locateFile(loggerName + date + ".log", "Pre-written string\n");
        assert Files.readAllLines(logFile).get(0).equals("Pre-written string");

        // set to overwrite mode
        I.env(loggerName + ".append", false);

        // write log
        I.info(loggerName, "This logger will overwrite log on an existing file.");

        assert Files.readAllLines(logFile).get(0).endsWith("This logger will overwrite log on an existing file.");
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