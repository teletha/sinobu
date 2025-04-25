/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package sinobu;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import kiss.Extensible;
import kiss.I;

public class ExtensionTest {
    /**
     * This is extension point.
     */
    interface Codec<T> extends Extensible {
        String encode(T value);
    }

    /**
     * This is extension with LocalDate key.
     */
    class LocalDateCodec implements Codec<LocalDate> {

        @Override
        public String encode(LocalDate value) {
            return DateTimeFormatter.ISO_DATE.format(value);
        }
    }

    /**
     * Load all extensions once at application's entry point.
     */
    static {
        I.load(ExtensionTest.class);
    }

    /**
     * User code.
     */
    @Test
    void usage() {
        // find extension by type
        Codec<LocalDate> codec = I.find(Codec.class, LocalDate.class);

        assert codec.encode(LocalDate.of(2022, 11, 11)).equals("2022-11-11");
    }
}