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
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Log extends Handler {

    /** The actual log file. */
    private Writer writer;

    private long next = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli();

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

                writer = new BufferedWriter(new FileWriter(I.LogName.apply(day), StandardCharsets.UTF_8));
                next += 24 * 60 * 60 * 1000;

                // delete oldest
                day = day.minusDays(I.LogRotate <= 0 ? Integer.MAX_VALUE : I.LogRotate);
                Path p = Path.of(I.LogName.apply(day));
                while (Files.exists(p)) {
                    Files.delete(p);
                    day = day.minusDays(1);
                }
            }

            // hold caller location info (replaced by high-speed stacktrace extractor)
            if (I.LogCaller) {
                StackTraceElement e = StackWalker.getInstance().walk(s -> s.skip(5).findFirst().get()).toStackTraceElement();
                log.setSourceClassName(e.getClassName());
                log.setSourceMethodName(e.getMethodName());
                log.setSequenceNumber(e.getLineNumber());
            }

            I.LogFormat.ACCEPT(writer, log);
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