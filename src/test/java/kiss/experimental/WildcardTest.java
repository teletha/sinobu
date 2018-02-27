/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

import org.junit.Test;

/**
 * @version 2011/03/22 17:09:40
 */
public class WildcardTest {

    @Test
    public void none() throws Exception {
        assert match("test.java", "test.java");
    }

    @Test
    public void wildcard() throws Exception {
        assert match("test.java", "*");
        assert match("test.java", "**");
        assert match("test.java", "***");
        assert match("test.java", "****");
        assert match("test.java", "***st**");
    }

    @Test
    public void wildcardRight() throws Exception {
        assert match("benchmarktest.java", "*.java");
        assert !match("short", "*patternIsLongerThanInputValue");
        assert !match("val", "*patternIsLongerThanInputValue");
    }

    @Test
    public void wildcardLeft() throws Exception {
        assert match("test.java", "test.*");
        assert !match("short", "patternIsLongerThanInputValue*");
        assert !match("pattern", "patternIsLongerThanInputValue*");
    }

    @Test
    public void wildcardBoth() throws Exception {
        assert match("test.java", "test*java");
    }

    @Test
    public void wildcardNone() throws Exception {
        assert match("test.java", "test*.java");
    }

    @Test
    public void ambiguous() throws Exception {
        assert match("test.java", "t*a");
    }

    @Test
    public void multiple() throws Exception {
        assert match("test.java", "t*s*.j*a");
    }

    @Test
    public void dontRetreat() throws Exception {
        assert !match("MatchAtNextPosition", "*ext*ext*");
    }

    @Test
    public void dontRetreatOne() throws Exception {
        assert !match("MatchAtNextPosition", "*e*e*");
        assert match("MatchAtNextPosition", "*e*x*");
    }

    @Test
    public void dontRetreatMix() throws Exception {
        assert !match("MatchAtNextPosition", "*e*ext*");
        assert match("MatchAtNextPosition", "*e*xt*");
    }

    @Test
    public void escapeChar() throws Exception {
        assert match("test.java", "\\test.java");
    }

    @Test
    public void escapeAstarisk() throws Exception {
        assert match("i*i", "i\\*i");
    }

    @Test
    public void escapeEscape() throws Exception {
        assert match("i\\i", "i\\\\i");
    }

    @Test
    public void escapeComplex() throws Exception {
        assert !match("complex", "comple\\*");
        assert match("comple*", "comple\\*");
        assert !match("complex", "comple\\\\*");
        assert match("comple\\x", "comple\\\\*");
        assert !match("comple\\\\", "comple\\\\\\*");
        assert match("comple\\*", "comple\\\\\\*");
    }

    @Test
    public void more() throws Exception {
        assert !match("test.java", "test.javaa");
        assert !match("test.java", "ttest.java");
    }

    @Test
    public void less() throws Exception {
        assert !match("test.java", "test.jav");
        assert !match("test.java", "est.java");
    }

    @Test
    public void miss() throws Exception {
        assert !match("test.java", "test.jawa");
    }

    @Test
    public void missWildcardRight() throws Exception {
        assert !match("benchmarktest.java", "*.txt");
    }

    @Test
    public void missHead() throws Exception {
        assert !match("head doesn't match", "d*h");
    }

    @Test
    public void missTail() throws Exception {
        assert !match("tail doesn't match", "t*m");
    }

    @Test
    public void missBoth() throws Exception {
        assert !match("abcdefghijklmn", "*00*");
    }

    @Test
    public void noneAsciiWildcardSingleCharcter() throws Exception {
        assert match("あいうえお", "あ*お");
    }

    @Test
    public void noneAsciiWildcardMultipleCharcters() throws Exception {
        assert match("あいうえお", "あい*お");
    }

    @Test
    public void noneAsciiMultipleWildcards() throws Exception {
        assert match("天上天下唯我独尊", "天上*下*独尊");
    }

    @Test
    public void noneAsciiMiss() throws Exception {
        assert !match("天上天下唯我独尊", "天*使");
    }

    @Test
    public void noneAsciiAmbiguous() throws Exception {
        assert match("天使ちゃんまじ天使", "天*使");
        assert match("天使ちゃんまじ天使", "*まじ天*使");
        assert match("天使ちゃんまじ天使", "天*使ちゃん*");
    }

    @Test
    public void ignoreCaseUpper() throws Exception {
        assert match("abcd", "*CD");
        assert match("abcd", "AB*");
        assert match("abcd", "*B*");
        assert match("abcd", "*BC*");
    }

    @Test
    public void ignoreCaseLower() throws Exception {
        assert match("ABCD", "*cd");
        assert match("ABCD", "ab*");
        assert match("ABCD", "*b*");
        assert match("ABCD", "*bc*");
    }

    @Test
    public void ignoreCaseMix() throws Exception {
        assert match("aBcD", "*Cd");
        assert match("aBcD", "Ab*");
        assert match("aBcD", "*b*");
        assert match("aBcD", "*bC*");
    }

    @Test
    public void slash() throws Exception {
        assert match("directory/file", "directory/file");
    }

    @Test
    public void slashWildcard() throws Exception {
        assert match("directory/file", "directory/*");
    }

    /**
     * Helper method to match.
     */
    private boolean match(String value, String pattern) {
        return new Wildcard(pattern).match(value);
    }
}
