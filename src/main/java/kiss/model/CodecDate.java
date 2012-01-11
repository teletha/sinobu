/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * Codec for {@link Date}.
 * </p>
 * 
 * @version 2011/12/25 17:29:11
 */
class CodecDate extends Codec<Date> {

    /**
     * The date format for W3CDTF. Date formats are not synchronized. It is recommended to create
     * separate format instances for each thread. If multiple threads access a format concurrently,
     * it must be synchronized externally.
     */
    private final static DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * @see kiss.model.Codec#decode(java.lang.String)
     */
    public synchronized Date decode(String value) {
        try {
            return format.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * @see kiss.model.Codec#encode(java.lang.Object)
     */
    public synchronized String encode(Date value) {
        return format.format(value);
    }
}
