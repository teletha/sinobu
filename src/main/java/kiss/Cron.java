/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single field in a cron expression.
 */
class Cron {
    private static final Pattern FORMAT = Pattern
            .compile("(?:(?:(\\*)|(\\?|L|LW|R)) | ([0-9]{1,2}|[a-z]{3,3})(?:(L|W) | -([0-9]{1,2}|[a-z]{3,3}))?)(?:(R|/|\\#)([0-9]{1,7})?)?", Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);

    private ChronoField field;

    /**
     * [0] - start
     * [1] - end
     * [2] - increment
     * [3] - modifier
     * [4] - modifierForIncrement
     */
    private List<int[]> parts = new ArrayList();

    /**
     * Constructs a new Field instance based on the given type and expression.
     *
     * @param expr The expression string for this field.
     * @throws IllegalArgumentException if the expression is invalid.
     */
    Cron(ChronoField field, int min, int max, String names, String modifier, String increment, String expr, int hash) {
        this.field = field;

        for (String range : expr.split(",")) {
            Matcher m = FORMAT.matcher(range);
            if (!m.matches()) {
                throw new IllegalArgumentException(range);
            }

            String start = m.group(3);
            String mod = m.group(4);
            String end = m.group(5);
            String inc = m.group(7);

            int[] part = {-1, -1, 1, 0, 0};
            if (start != null) {
                part[0] = part[1] = map(start, names);
                part[3] = mod == null ? 0 : mod.charAt(0);
                if (end != null) {
                    part[1] = map(end, names);
                } else if (inc != null) {
                    part[1] = max;
                }

                if ("R".equals(m.group(6))) {
                    part[0] = part[1] = hash % (part[1] - part[0]) + part[0];
                }
            }
            // astarisk
            if (m.group(1) != null) {
                part[0] = min;
                part[1] = max;
            }
            if (m.group(2) != null) {
                mod = m.group(2);
                part[3] = mod.charAt(mod.length() - 1);
                if (part[3] == 'R') {
                    part[0] = part[1] = hash % (max - min) + min;
                }
            }
            if (inc != null) {
                part[4] = m.group(6).charAt(0);
                part[2] = Integer.parseInt(inc);
            }

            // validate parts
            // @formatter:off
            if ((part[0] != -1 && part[0] < min) || max < part[1] || part[0] > part[1] || (part[3] != 0 && modifier.indexOf(part[3]) == -1) || part[4] != 0 && increment.indexOf(part[4]) == -1) {
                throw new IllegalArgumentException(range);
            }
            // @formatter:on
            parts.add(part);
        }
    }

    /**
     * Maps a string representation to its corresponding numeric value for this field.
     *
     * @param name The string representation to map.
     * @return The corresponding numeric value.
     */
    private int map(String name, String names) {
        int index = names.indexOf(name.toUpperCase().concat(" "));
        if (index != -1) {
            // The minimum value of the field needs to be added, but since this function is only
            // used for Month and DayOfWeek, there is no problem with always using the constant
            // value 1 instead of field.range().getMinimum().
            return index / 4 + 1;
        }
        int value = Integer.parseInt(name);
        return value == 0 && field == ChronoField.DAY_OF_WEEK ? 7 : value;
    }

    /**
     * Checks if the given date matches this field's constraints.
     *
     * @param date The LocalDate to check.
     * @return true if the date matches, false otherwise.
     */
    boolean matches(ZonedDateTime date) {
        int day = date.getDayOfMonth();
        int dow = date.getDayOfWeek().getValue();
        int last = YearMonth.of(date.getYear(), date.getMonth()).lengthOfMonth();

        for (int[] part : parts) {
            if (part[3] == 'L') {
                if (field == ChronoField.DAY_OF_WEEK) {
                    if (dow == part[0] && day > (last - 7)) {
                        return true;
                    }
                } else {
                    if (day == last - (part[0] == -1 ? 0 : part[0])) {
                        return true;
                    }
                }
            } else if (part[3] == 'W') {
                if (dow <= 5) {
                    int target = part[0] == -1 ? last : part[0];

                    if (day == target) {
                        return true;
                    } else if (dow == 5 && (target == day + 1 || (target == last && last - 2 <= day))) {
                        return true;
                    } else if (dow == 1 && (target == day - 1 || (target == 1 && day <= 3))) {
                        return true;
                    }
                }
            } else if (part[4] == '#') {
                if (dow == part[0] && part[2] == (day % 7 == 0 ? day / 7 : day / 7 + 1)) {
                    return true;
                }
            } else {
                int value = date.get(field);
                if (part[3] == '?' || (part[0] <= value && value <= part[1] && (value - part[0]) % part[2] == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Finds the next matching value for this field.
     *
     * @param date Array containing a single ZonedDateTime to be updated.
     * @return true if a match was found, false if the field overflowed.
     */
    boolean matches(ZonedDateTime[] date) {
        int value = date[0].get(field);
        TreeSet<ZonedDateTime> set = new TreeSet();

        for (int[] part : parts) {
            int next = part[0] <= value ? value : part[0];
            int rem = (next - part[0]) % part[2];
            if (rem != 0) next += part[2] - rem;

            if (next <= part[1]) {
                ZonedDateTime target = date[0];
                if (next != value) {
                    if (field == ChronoField.MONTH_OF_YEAR) {
                        target = target.with(field, next).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                    } else {
                        target = target.with(field, next).truncatedTo(field.getBaseUnit());
                    }
                }
                set.add(target);
                continue;
            }
        }

        if (!set.isEmpty()) {
            date[0] = set.first();
            return true;
        }

        if (field == ChronoField.MONTH_OF_YEAR) {
            date[0] = date[0].plus(1, field.getRangeUnit()).withMonth(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        } else {
            // The field must be set to the minimum value, but this method is only used for Second,
            // Minute, Hour, and Month, and since Month is handled in the above branch, there is no
            // problem with always using the constant value 0 instead of field.range().getMinimum().
            date[0] = date[0].plus(1, field.getRangeUnit()).with(field, 0).truncatedTo(field.getBaseUnit());
        }
        return false;
    }
}