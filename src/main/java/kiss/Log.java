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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class Log extends Handler {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /** The actual log file. */
    private Writer writer;

    private long next = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli();

    private Path locate(LogRecord log, LocalDate day) throws Exception {
        Path p = Path.of(".log");
        Files.createDirectories(p);
        return p.resolve(Objects.requireNonNullElse(log.getLoggerName(), "system") + day.format(DateTimeFormatter.BASIC_ISO_DATE) + ".log");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final synchronized void publish(LogRecord log) {
        try {
            if (next <= log.getMillis()) {
                // stop old
                close();

                // start new
                LocalDate day = LocalDate.now();

                writer = new BufferedWriter(new FileWriter(locate(log, day).toFile(), StandardCharsets.UTF_8));
                next += 24 * 60 * 60 * 1000;

                // delete oldest
                day = day.minusDays(30);
                while (Files.deleteIfExists(locate(log, day))) {
                    day = day.minusDays(1);
                }
            }

            char c = log.getLevel().getName().charAt(0);
            writer.append(log.getInstant().atZone(ZoneId.systemDefault()).format(F))
                    .append(' ')
                    .append(c == 'E' ? "ERROR" : c == 'W' ? "WARN" : c == 'I' || c == 'C' ? "INFO" : "DEBUG")
                    .append('\t')
                    .append(log.getMessage());
            if (I.LogCaller) {
                writer.append("\t")
                        .append(log.getSourceClassName())
                        .append('#')
                        .append(log.getSourceMethodName())
                        .append(':')
                        .append(String.valueOf(log.getSequenceNumber()));
            }
            writer.append('\n');

            Throwable x = log.getThrown();
            if (x != null) {
                x.printStackTrace(new PrintWriter(writer));
            }
        } catch (Throwable x) {
            throw I.quiet(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void flush() {
        try {
            writer.flush();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() {
        I.quiet(writer);
    }
}