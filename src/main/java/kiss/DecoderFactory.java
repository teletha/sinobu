/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
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
import java.time.ZonedDateTime;
import java.util.Locale;

/**
 * @version 2016/09/12 9:50:54
 */
class DecoderFactory implements ExtensionFactory<Decoder> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Decoder create(Class type) {
        if (type.isEnum()) {
            return value -> Enum.valueOf((Class<Enum>) type, value);
        }

        switch (type.getName().hashCode()) {
        case 64711720: // boolean
        case 344809556: // java.lang.Boolean
            return Boolean::new;
        case 104431: // int
        case -2056817302: // java.lang.Integer
            return Integer::new;
        case 3327612: // long
        case 398795216: // java.lang.Long
            return Long::new;
        case 97526364: // float
        case -527879800: // java.lang.Float
            return Float::new;
        case -1325958191: // double
        case 761287205: // java.lang.Double
            return Double::new;
        case 3039496: // byte
        case 398507100: // java.lang.Byte
            return Byte::new;
        case 109413500: // short
        case -515992664: // java.lang.Short
            return Short::new;
        case 3052374: // char
        case 155276373: // java.lang.Character
            return value -> value.charAt(0);
        case 1195259493: // java.lang.String
            return String::new;
        case -1555282570: // java.lang.StringBuilder
            return StringBuilder::new;
        case 1196660485: // java.lang.StringBuffer
            return StringBuffer::new;
        case 2130072984: // java.io.File
            return File::new;
        case 2050244018: // java.net.URL
            return value -> {
                try {
                    return new URL(value);
                } catch (Exception e) {
                    throw I.quiet(e);
                }
            };
        case 2050244015: // java.net.URI
            return URI::create;
        case -989675752: // java.math.BigInteger
            return BigInteger::new;
        case -1405464277: // java.math.BigDecimal
            return BigDecimal::new;
        case -1246033885: // java.time.LocalTime
            return LocalTime::parse;
        case -1246518012: // java.time.LocalDate
            return LocalDate::parse;
        case -1179039247: // java.time.LocalDateTime
            return LocalDateTime::parse;
        case -682591005: // java.time.OffsetDateTime
            return OffsetDateTime::parse;
        case -1917484011: // java.time.OffsetTime
            return OffsetTime::parse;
        case 1505337278: // java.time.ZonedDateTime
            return ZonedDateTime::parse;
        case 649475153: // java.time.MonthDay
            return MonthDay::parse;
        case -537503858: // java.time.YearMonth
            return YearMonth::parse;
        case -1062742510: // java.time.Year
            return Year::parse;
        case -1023498007: // java.time.Duration
            return Duration::parse;
        case 649503318: // java.time.Period
            return Period::parse;
        case 1296075756: // java.time.Instant
            return Instant::parse;
        case -1165211622: // java.util.Locale
            return Locale::forLanguageTag;
        case 1464606545: // java.nio.file.Path
        case -2015077501: // sun.nio.fs.WindowsPath
            return I::locate;

        // case -89228377: // java.nio.file.attribute.FileTime
        // decoder = value -> FileTime.fromMillis(Long.valueOf(value));
        // encoder = (Encoder<FileTime>) value -> String.valueOf(value.toMillis());
        // break;
        default:
            return null;
        }
    }
}
