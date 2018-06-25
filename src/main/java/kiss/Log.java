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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @version 2018/06/25 12:44:10
 */
class Log extends Formatter {

    /** The date time format. */
    private static final DateTimeFormatter time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * {@inheritDoc}
     */
    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(time.format(LocalDateTime.now(ZoneId.systemDefault()))).append("   ");

        String message = record.getMessage();
        Object[] params = record.getParameters();

        if (params == null) {
            builder.append(message);
        } else {
            int length = message.length();
            int paramIndex = 0;
            char p = ' ';

            for (int i = 0; i < length; i++) {
                char c = message.charAt(i);

                if (c == '{' && i != length - 1 && p != '\\' && paramIndex < params.length) {
                    char next = message.charAt(i + 1);

                    if (next == '}') {
                        i++;
                        builder.append(params[paramIndex++]);
                        continue;
                    }
                }
                builder.append(p = c);
            }
        }
        builder.append(System.lineSeparator());

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
