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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @version 2017/03/31 13:45:33
 */
class Log extends Formatter {

    /** The date time format. */
    private static final DateTimeFormatter time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();

        String message = record.getMessage();
        Object[] params = record.getParameters();

        builder.append(time.format(LocalDateTime.now(ZoneId.systemDefault())))
                .append("   ")
                .append(params == null ? message : String.format(message, params))
                .append(System.lineSeparator());

        // detail error
        if (params != null) {
            int size = params.length;

            if (size != 0) {
                Object e = params[size - 1];

                if (e instanceof Throwable) {
                    StringWriter w = new StringWriter();
                    ((Throwable) e).printStackTrace(new PrintWriter(w));
                    builder.append(w.toString());
                }
            }
        }
        return builder.toString();
    }

}
