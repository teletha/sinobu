/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub.powerassert;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2012/01/18 13:15:48
 */
public class LongTest {

    @Rule
    public static final PowerAssert Assert = new PowerAssert(true);

    @Test
    public void constant_0() throws Exception {
        long value = -1;

        Assert.willUse("0");
        Assert.willCapture("value", value);
        assert 0 == value;
    }

    @Test
    public void constant_1() throws Exception {
        long value = -1;

        Assert.willUse("1");
        Assert.willCapture("value", value);
        assert 1 == value;
    }

    @Test
    public void constant_2() throws Exception {
        long value = -1;

        Assert.willUse("2");
        Assert.willCapture("value", value);
        assert 2 == value;
    }

    @Test
    public void constant_3() throws Exception {
        long value = -1;

        Assert.willUse("3");
        Assert.willCapture("value", value);
        assert 3 == value;
    }

    @Test
    public void constant_M1() throws Exception {
        long value = 0;

        Assert.willUse("-1");
        Assert.willCapture("value", value);
        assert -1 == value;
    }

    @Test
    public void big() throws Exception {
        long value = 2;

        Assert.willUse("1234567890123");
        Assert.willCapture("value", value);
        assert 1234567890123L == value;
    }

    @Test
    public void array() throws Exception {
        long[] array = {0, 1, 2};

        Assert.willCapture("array", array);
        assert array == null;
    }

    @Test
    public void arrayIndex() throws Exception {
        long[] array = {0, 1, 2};

        Assert.willCapture("array", array);
        Assert.willCapture("array[1]", 1L);
        assert array[1] == 128;
    }

    @Test
    public void arrayLength() throws Exception {
        long[] array = {0, 1, 2};

        Assert.willCapture("array", array);
        Assert.willCapture("array.length", 3);
        assert array.length == 10;
    }

    @Test
    public void method() throws Exception {
        Assert.willCapture("test()", 1L);
        assert test() == 2;
    }

    long test() {
        return 1;
    }

    @Test
    public void parameter() throws Exception {
        Assert.willCapture("test(12)", false);
        assert test(12);
    }

    private boolean test(long value) {
        return false;
    }

    /** The tester. */
    private long longField = 11;

    /** The tester. */
    private static long longFieldStatic = 11;

    @Test
    public void fieldLongAccess() throws Exception {
        Assert.willCapture("this.longField", 11L);
        assert longField == 0;
    }

    @Test
    public void fieldLongStaticAccess() throws Exception {
        Assert.willCapture("LongTest.longFieldStatic", 11L);
        assert longFieldStatic == 0;
    }
}
