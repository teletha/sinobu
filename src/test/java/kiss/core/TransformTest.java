/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import kiss.I;

class TransformTest {

    @Test
    void inputNull() {
        assert I.transform(null, int.class) == null;
        assert I.transform(null, String.class) == null;
    }

    @Test
    void outputNull() {
        assertThrows(NullPointerException.class, () -> {
            I.transform("1", null);
        });
    }

    @Test
    void primitiveInt() {
        assert I.transform("1", int.class) == 1;
        assert I.transform(1, String.class).equals("1");
    }

    @Test
    void primitiveLong() {
        assert I.transform("1", long.class) == 1L;
        assert I.transform(1L, String.class).equals("1");
    }

    @Test
    void primitiveChar() {
        assert I.transform("1", char.class) == '1';
        assert I.transform('1', String.class).equals("1");
    }

    @Test
    void primitiveFloat() {
        assert I.transform("1.3", float.class) == 1.3f;
        assert I.transform(1.3f, String.class).equals("1.3");
    }

    @Test
    void primitiveDouble() {
        assert I.transform("1.3", double.class) == 1.3d;
        assert I.transform(1.3d, String.class).equals("1.3");
    }

    @Test
    void primitiveBoolean() {
        assert I.transform("true", boolean.class);
        assert I.transform(true, String.class).equals("true");
    }

    @Test
    void url() throws MalformedURLException, URISyntaxException {
        URL value = URI.create("http://localhost:8888/").toURL();
        String text = "http://localhost:8888/";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, URL.class).toURI().equals(value.toURI());
    }

    @Test
    void uri() throws URISyntaxException {
        URI value = new URI("http://localhost:8888/");
        String text = "http://localhost:8888/";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, URI.class).equals(value);
    }

    @Test
    void locale() {
        Locale value = Locale.ENGLISH;
        String text = "en";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, Locale.class).equals(value);
    }

    @Test
    void bigInteger() {
        BigInteger value = new BigInteger("12345678901234567890");
        String text = "12345678901234567890";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, BigInteger.class).equals(value);
    }

    @Test
    void bigDecimal() {
        BigDecimal value = new BigDecimal("123.456789012345678901");
        String text = "123.456789012345678901";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, BigDecimal.class).equals(value);
    }

    @Test
    void stringBuilder() {
        StringBuilder value = new StringBuilder("123.456789012345678901");
        String text = "123.456789012345678901";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, StringBuilder.class).toString().equals(value.toString());
    }

    @Test
    void stringBuffer() {
        StringBuffer value = new StringBuffer("123.456789012345678901");
        String text = "123.456789012345678901";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, StringBuffer.class).toString().equals(value.toString());
    }

    @Test
    void localDateTime() {
        LocalDateTime local = LocalDateTime.of(2014, 3, 10, 13, 43, 56);
        String text = "2014-03-10T13:43:56";

        assert I.transform(local, String.class).equals(text);
        assert I.transform(text, LocalDateTime.class).equals(local);
    }

    @Test
    void localDate() {
        LocalDate local = LocalDate.of(2014, 3, 10);
        String text = "2014-03-10";

        assert I.transform(local, String.class).equals(text);
        assert I.transform(text, LocalDate.class).equals(local);
    }

    @Test
    void localTime() {
        LocalTime local = LocalTime.of(23, 45, 9, 765);
        String text = "23:45:09.000000765";

        assert I.transform(local, String.class).equals(text);
        assert I.transform(text, LocalTime.class).equals(local);
    }

    @Test
    void offsetDateTime() {
        OffsetDateTime offset = OffsetDateTime.of(2014, 3, 10, 13, 43, 56, 203, ZoneOffset.ofHours(9));
        String text = "2014-03-10T13:43:56.000000203+09:00";

        assert I.transform(offset, String.class).equals(text);
        assert I.transform(text, OffsetDateTime.class).equals(offset);
    }

    @Test
    void offsetTime() {
        OffsetTime offset = OffsetTime.of(13, 43, 56, 203, ZoneOffset.ofHours(9));
        String text = "13:43:56.000000203+09:00";

        assert I.transform(offset, String.class).equals(text);
        assert I.transform(text, OffsetTime.class).equals(offset);
    }

    @Test
    void zonedDateTime() {
        ZonedDateTime offset = ZonedDateTime.of(2014, 3, 10, 13, 43, 56, 203, ZoneId.of(ZoneId.SHORT_IDS.get("JST")));
        String text = "2014-03-10T13:43:56.000000203+09:00[Asia/Tokyo]";

        assert I.transform(offset, String.class).equals(text);
        assert I.transform(text, ZonedDateTime.class).equals(offset);
    }

    @Test
    void monthDay() {
        MonthDay date = MonthDay.of(10, 31);
        String text = "--10-31";

        assert I.transform(date, String.class).equals(text);
        assert I.transform(text, MonthDay.class).equals(date);
    }

    @Test
    void yearMonth() {
        YearMonth date = YearMonth.of(2014, 5);
        String text = "2014-05";

        assert I.transform(date, String.class).equals(text);
        assert I.transform(text, YearMonth.class).equals(date);
    }

    @Test
    void year() {
        Year date = Year.of(2014);
        String text = "2014";

        assert I.transform(date, String.class).equals(text);
        assert I.transform(text, Year.class).equals(date);
    }

    @Test
    void duration() {
        Duration duration = Duration.of(40, ChronoUnit.SECONDS);
        String text = "PT40S";

        assert I.transform(duration, String.class).equals(text);
        assert I.transform(text, Duration.class).equals(duration);
    }

    @Test
    void period() {
        Period period = Period.of(1, 2, 14);
        String text = "P1Y2M14D";

        assert I.transform(period, String.class).equals(text);
        assert I.transform(text, Period.class).equals(period);
    }

    @Test
    void instant() {
        Instant instant = Instant.ofEpochMilli(1000);
        String text = "1970-01-01T00:00:01Z";

        assert I.transform(instant, String.class).equals(text);
        assert I.transform(text, Instant.class).equals(instant);
    }
}