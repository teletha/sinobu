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
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    void useLoggerName() {
        String loggerName = "your-logger";

        I.info(loggerName, "Message");

        assert assumeLog(Level.INFO, "Message");
    }

    @Test
    void useMessageWithCallerInfomation() {
        String loggerName = "enable-caller-infomation";

        // enable caller infomation
        I.env(loggerName + ".caller", Level.ALL);

        // write log
        I.error(loggerName, "Message");

        assert assumeLog(Level.ERROR, "Message at kiss.LogTest.useMessageWithCallerInfomation(LogTest.java:0)");
    }

    @Test
    void useMultiLoggers() throws IOException {
        String loggerName = "multi-loggers";
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        Path logFile = room.locate(loggerName + date + ".log");

        // use file and console logger
        I.env(loggerName + ".console", Level.ALL);
        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", logFile.getParent());

        // write log
        I.info(loggerName, "Write log on file and console.");

        // check log on file
        assert Files.readAllLines(logFile).get(0).endsWith("Write log on file and console.");

        // check log on cosole
        assert assumeLog(Level.INFO, "Write log on file and console.");
    }

    @Test
    void checkAnyLoggerFilterByItsLevel() {
        String loggerName = "change-logger-level";

        // change logger's level
        I.env(loggerName + ".console", Level.ERROR);

        I.info(loggerName, "Ignore this message");
        assert assumeNoLog();

        I.error(loggerName, "Accept this message");
        assert assumeLog(Level.ERROR, "Accept this message");
    }

    @Test
    void checkFileLoggerChangeLogDirectory() {
        String loggerName = "change-directory";
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        Path yourDirectory = room.locate("xyz");
        Path logFile = yourDirectory.resolve(loggerName + date + ".log");

        // check no log file
        assert Files.notExists(logFile);

        // use file logger
        I.env(loggerName + ".file", Level.ALL);

        // change log file directory
        I.env(loggerName + ".dir", yourDirectory);

        // write log
        I.info(loggerName, "Create log file in the specified directory.");

        // check generated log file
        assert Files.exists(logFile);
    }

    @Test
    void checkFileLoggerAppendMode() throws IOException {
        String loggerName = "append-log";
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        Path logFile = room.locateFile(loggerName + date + ".log", "Pre-written string\n");
        assert Files.readAllLines(logFile).get(0).equals("Pre-written string");

        // use file logger
        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", room.root);

        // use append mode
        I.env(loggerName + ".append", true);

        // write log
        I.info(loggerName, "This logger will append the log to an existing file.");

        // check exsited log file
        assert Files.readAllLines(logFile).get(0).equals("Pre-written string");
        assert Files.readAllLines(logFile).get(1).endsWith("This logger will append the log to an existing file.");
    }

    @Test
    void checkFileLoggerOverwriteMode() throws IOException {
        String loggerName = "overwrite-log";
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        Path logFile = room.locateFile(loggerName + date + ".log", "Pre-written string\n");
        assert Files.readAllLines(logFile).get(0).equals("Pre-written string");

        // use file logger
        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", room.root);

        // use overwrite mode
        I.env(loggerName + ".append", false);

        // write log
        I.info(loggerName, "This logger will overwrite log on an existing file.");

        // check regenerated log file
        assert Files.readAllLines(logFile).get(0).endsWith("This logger will overwrite log on an existing file.");
    }

    @Test
    void checkFileLoggerRotateLogFiles() {
        String loggerName = "rotate-log";

        // generate old log files
        List<Path> olds = IntStream.range(1, 40)
                .mapToObj(i -> room.locateFile(loggerName + LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_DATE) + ".log"))
                .collect(Collectors.toList());

        for (Path old : olds) {
            assert Files.exists(old);
        }

        // use file logger
        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", room.root);

        // write log
        I.info(loggerName, "Create new log file and delete old files.");

        // check old log files
        for (Path old : olds.subList(0, 29)) {
            assert Files.exists(old);
        }
        for (Path old : olds.subList(30, 39)) {
            assert Files.notExists(old);
        }
    }

    @Test
    void checkFileLoggerRotateSparseLogFiles() throws IOException {
        String loggerName = "rotate-sparse-log";

        // generate old log files
        List<Path> olds = IntStream.range(1, 40)
                .mapToObj(i -> room.locateFile(loggerName + LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_DATE) + ".log"))
                .collect(Collectors.toList());

        for (Path old : olds) {
            assert Files.exists(old);
        }

        // decimate log files
        Files.delete(olds.get(33));
        Files.delete(olds.get(36));
        Files.delete(olds.get(37));

        // use file logger
        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", room.root);

        // write log
        I.info(loggerName, "Create new log file and delete old files.");

        // check old log files
        for (Path old : olds.subList(0, 29)) {
            assert Files.exists(old);
        }
        for (Path old : olds.subList(30, 39)) {
            assert Files.notExists(old);
        }
    }

    @Test
    void checkFileLoggerRotationSize() {
        String loggerName = "change-rotation-size";

        // generate old log files
        List<Path> olds = IntStream.range(1, 40)
                .mapToObj(i -> room.locateFile(loggerName + LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_DATE) + ".log"))
                .collect(Collectors.toList());

        for (Path old : olds) {
            assert Files.exists(old);
        }

        // use file logger
        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", room.root);

        // change rotation size
        I.env(loggerName + ".rotate", 7);

        // write log
        I.info(loggerName, "Create new log file and delete old files.");

        // check old log files
        for (Path old : olds.subList(0, 6)) {
            assert Files.exists(old);
        }
        for (Path old : olds.subList(7, 39)) {
            assert Files.notExists(old);
        }
    }

    @Test
    void checkFileLoggerDisableRotation() {
        String loggerName = "no-rotation";

        // generate old log files
        List<Path> olds = IntStream.range(1, 40)
                .mapToObj(i -> room.locateFile(loggerName + LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_DATE) + ".log"))
                .collect(Collectors.toList());

        for (Path old : olds) {
            assert Files.exists(old);
        }

        // use file logger
        I.env(loggerName + ".file", Level.ALL);
        I.env(loggerName + ".dir", room.root);

        // disable rotation
        I.env(loggerName + ".rotate", 0);

        // write log
        I.info(loggerName, "Create new log file and delete old files.");

        // check old log files
        for (Path old : olds) {
            assert Files.exists(old);
        }
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