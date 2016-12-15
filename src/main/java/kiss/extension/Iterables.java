/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.extension;

import java.util.StringJoiner;

import bee.extension.Extension;

/**
 * @version 2016/12/15 9:03:45
 */
@Extension
public class Iterables {

    /**
     * <p>
     * Join values with the specified separator.
     * </p>
     * 
     * @param values
     * @param delimiter the sequence of characters to be used between each element added to the
     *            {@code StringJoiner}
     * @param prefix the sequence of characters to be used at the beginning
     * @param suffix the sequence of characters to be used at the end
     * @return
     */
    @Extension.Method
    public static <V> String join(Iterable<V> values, CharSequence delimiter) {
        return join(values, delimiter, "", "");
    }

    /**
     * <p>
     * Join values with the specified separator.
     * </p>
     * 
     * @param values
     * @param delimiter the sequence of characters to be used between each element added to the
     *            {@code StringJoiner}
     * @param prefix the sequence of characters to be used at the beginning
     * @param suffix the sequence of characters to be used at the end
     * @return
     */
    @Extension.Method
    public static <V> String join(Iterable<V> values, CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        for (Object value : values) {
            joiner.add(String.valueOf(value));
        }
        return joiner.toString();
    }
}
