/*
 * Copyright (C) 2025 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import kiss.XML;

/**
 * Utility for asserting selector behavior in XML-based tests.
 * Provides support for verifying correct element selection and
 * validating combinations of selectors with permutations and whitespace.
 */
public class FindAssetion {

    /**
     * Assert that the given selector matches the expected number of elements.
     *
     * @param xml The target XML.
     * @param expectedCount The expected number of matched elements.
     * @param selector The CSS selector to test.
     * @return true if the selector behaves as expected.
     */
    public static boolean select(XML xml, int expectedCount, String selector) {
        return select(xml, expectedCount, selector, true);
    }

    /**
     * Assert that the given selector matches the expected number of elements,
     * with optional checks for whitespace variation in the selector.
     *
     * @param xml The target XML.
     * @param expectedCount The expected number of matched elements.
     * @param selector The CSS selector to test.
     * @param spacer If true, test selector variants with added whitespace.
     * @return true if all assertions pass.
     */
    public static boolean select(XML xml, int expectedCount, String selector, boolean spacer) {
        // Basic assertion without spacing variation
        assert xml.find(selector).size() == expectedCount;

        if (spacer) {
            // Whitespace variations to test around combinators and operators
            String[] spaces = {" ", "  ", "\t"};
            for (String s : spaces) {
                String spaced = selector.replaceAll("(\\^=|\\$=|~=|\\|=|\\*=|=|,|<|>|~|\\+)", s + "$1" + s)
                        .replace("[", "[" + s)
                        .replace("]", s + "]")
                        .replace("(", "(" + s)
                        .replace(")", s + ")")
                        .replace("n-", "n" + s + "-" + s)
                        .replace("n+", "n" + s + "+" + s);

                // Check the spaced variant
                assert xml.find(s + spaced + s).size() == expectedCount;
            }
        }
        return true;
    }

    /**
     * Assert that all permutations of the given selectors,
     * when combined with commas, match the expected number of elements.
     *
     * @param xml The target XML.
     * @param expectedCount The expected number of matched elements.
     * @param selectors The array of selectors to permute.
     * @return true if all permutations pass the assertion.
     */
    public static boolean selectWithPermutation(XML xml, int expectedCount, String... selectors) {
        for (List<String> list : permute(selectors)) {
            String joined = list.stream().collect(Collectors.joining(","));
            assert select(xml, expectedCount, joined);
        }
        return true;
    }

    /**
     * Generate all permutations of a given selector array.
     *
     * @param input The selector strings.
     * @return A list of all permutations.
     */
    private static List<List<String>> permute(String[] input) {
        List<List<String>> result = new ArrayList<>();
        permute(new ArrayList<>(Arrays.asList(input)), 0, result);
        return result;
    }

    /**
     * Helper method to recursively generate permutations.
     *
     * @param list The current list to permute.
     * @param index Current position in the permutation.
     * @param result Accumulator for storing all permutations.
     */
    private static void permute(List<String> list, int index, List<List<String>> result) {
        if (index == list.size()) {
            result.add(new ArrayList<>(list));
            return;
        }
        for (int i = index; i < list.size(); i++) {
            Collections.swap(list, index, i);
            permute(list, index + 1, result);
            Collections.swap(list, index, i); // backtrack
        }
    }
}
