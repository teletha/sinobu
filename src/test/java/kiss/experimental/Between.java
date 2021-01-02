/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

public class Between {
    /**
     * Checks if the specified value exists between the minimum and maximum values.
     * 
     * @param <C> Target type to compare.
     * @param min The minimum value.
     * @param target A target value to compare.
     * @param max The maximum value.
     * @return The test result.
     * @throws NullPointerException If any of the values are null.
     * @see #bound(Comparable, Comparable, Comparable)
     */
    public static <C extends Comparable> boolean between(C min, C target, C max) {
        return min.compareTo(target) <= 0 && target.compareTo(max) <= 0;
    }

    /**
     * Rounds the value so that the specified value falls between the minimum and maximum value.
     * 
     * @param <C> Target type to round.
     * @param min The minimum value.
     * @param target A target value to round.
     * @param max The maximum value.
     * @return The test result.
     * @throws NullPointerException If any of the values are null.
     * @see #between(Comparable, Comparable, Comparable)
     */
    public static <C extends Comparable> C bound(C min, C target, C max) {
        return min.compareTo(target) <= 0 ? target.compareTo(max) <= 0 ? target : max : min;
    }
}