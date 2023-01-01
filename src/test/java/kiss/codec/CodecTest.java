/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.codec;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiPredicate;

import org.junit.jupiter.api.Test;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

class CodecTest {

    static {
        System.out.println("sun.nio.fs.UnixPath".hashCode());
    }

    @Test
    void Boolean() {
        assert codec(Boolean.TRUE);
    }

    @Test
    void Integer() {
        assert codec(Integer.valueOf(1234));
    }

    @Test
    void Long() {
        assert codec(Long.valueOf(1234));
    }

    @Test
    void Float() {
        assert codec(Float.valueOf(1.234f));
    }

    @Test
    void Double() {
        assert codec(Double.valueOf(1.234d));
    }

    @Test
    void Byte() {
        assert codec(Byte.valueOf((byte) 1));
    }

    @Test
    void Short() {
        assert codec(Short.valueOf((short) 1));
    }

    @Test
    void Character() {
        assert codec(Character.valueOf('c'));
    }

    @Test
    void localDate() {
        assert codec(LocalDate.of(2023, 1, 1));
    }

    @Test
    void localTime() {
        assert codec(LocalTime.of(9, 10, 22));
    }

    @Test
    void localDateTime() {
        assert codec(LocalDateTime.of(2023, 1, 1, 9, 10, 22));
    }

    @Test
    void offsetTime() {
        assert codec(OffsetTime.of(9, 10, 22, 545, ZoneOffset.UTC));
    }

    @Test
    void offsetDateTime() {
        assert codec(OffsetDateTime.of(2023, 1, 1, 9, 10, 22, 432, ZoneOffset.UTC));
    }

    @Test
    void zonedDateTime() {
        assert codec(ZonedDateTime.of(2023, 1, 23, 5, 22, 13, 332, ZoneOffset.UTC));
    }

    @Test
    void monthDay() {
        assert codec(MonthDay.of(11, 23));
    }

    @Test
    void yearMonth() {
        assert codec(YearMonth.of(2025, 11));
    }

    @Test
    void year() {
        assert codec(Year.of(2023));
    }

    @Test
    void duration() {
        assert codec(Duration.of(30, ChronoUnit.SECONDS));
    }

    @Test
    void period() {
        assert codec(Period.of(3, 2, 10));
    }

    @Test
    void instant() {
        assert codec(Instant.ofEpochSecond(2000));
    }

    @Test
    void biginteger() {
        assert codec(new BigInteger("1234567890987654321"));
    }

    @Test
    void bigdecimal() {
        assert codec(new BigDecimal("123456789.987654321"));
    }

    @Test
    void locale() {
        assert codec(Locale.ENGLISH);
    }

    @Test
    void string() {
        assert codec("string");
    }

    @Test
    void builder() {
        assert codec(new StringBuilder("text"), (x, y) -> x.toString().contentEquals(y));
    }

    @Test
    void buffer() {
        assert codec(new StringBuffer("text"), (x, y) -> x.toString().contentEquals(y));
    }

    @Test
    void clazz() {
        assert codec(Map.class);
    }

    @Test
    void uri() {
        assert codec(URI.create("http://www.test.com/path/index.html?param=value"));
    }

    @Test
    void url() throws MalformedURLException {
        assert codec(new URL("http://www.test.com/path/index.html?param=value"));
    }

    @Test
    void file() {
        assert codec(new File("path/file.txt"));
    }

    @Test
    void path() {
        assert codec(Path.of("path", "file.txt"));
    }

    private <T> boolean codec(T value) {
        return codec(value, Object::equals);
    }

    private <T> boolean codec(T value, BiPredicate<T, T> equality) {
        Encoder<T> encoder = I.find(Encoder.class, value.getClass());
        Decoder<T> decoder = I.find(Decoder.class, value.getClass());
        assert equality.test(decoder.decode(encoder.encode(value)), value);

        return true;
    }
}