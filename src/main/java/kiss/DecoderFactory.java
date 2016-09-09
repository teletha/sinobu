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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * @version 2016/08/01 22:58:09
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

        try {
            switch (type.getName().hashCode()) {
            case 64711720: // boolean
            case 344809556: // java.lang.Boolean
            case 104431: // int
            case -2056817302: // java.lang.Integer
            case 3327612: // long
            case 398795216: // java.lang.Long
            case 97526364: // float
            case -527879800: // java.lang.Float
            case -1325958191: // double
            case 761287205: // java.lang.Double
            case 3039496: // byte
            case 398507100: // java.lang.Byte
            case 109413500: // short
            case -515992664: // java.lang.Short
            case 1195259493: // java.lang.String
            case -1555282570: // java.lang.StringBuilder
            case 1196660485: // java.lang.StringBuffer
            case 2130072984: // java.io.File
            case 2050244018: // java.net.URL
            case 2050244015: // java.net.URI
            case -989675752: // java.math.BigInteger
            case -1405464277: // java.math.BigDecimal
                // constructer pattern
                Constructor<?> constructor = I.wrap(type).getConstructor(String.class);

                return value -> {
                    try {
                        return constructor.newInstance(value);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                };

            case 3052374: // char
            case 155276373: // java.lang.Character
                return value -> value.charAt(0);

            case -1246033885: // java.time.LocalTime
            case -1246518012: // java.time.LocalDate
            case -1179039247: // java.time.LocalDateTime
            case -682591005: // java.time.OffsetDateTime
            case -1917484011: // java.time.OffsetTime
            case 1505337278: // java.time.ZonedDateTime
            case 649475153: // java.time.MonthDay
            case -537503858: // java.time.YearMonth
            case -1062742510: // java.time.Year
            case -1023498007: // java.time.Duration
            case 649503318: // java.time.Period
            case 1296075756: // java.time.Instant
                // parse method pattern
                Method method = type.getMethod("parse", CharSequence.class);

                return value -> {
                    try {
                        return method.invoke(null, value);
                    } catch (Exception e) {
                        throw I.quiet(e);
                    }
                };

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
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
