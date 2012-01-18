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
public class IntTest {

    @Rule
    public static final PowerAssert Assert = new PowerAssert(true);

    @Test
    public void constant_0() throws Exception {
        int value = -1;

        Assert.willCapture("0", 0);
        Assert.willCapture("value", value);
        assert 0 == value;
    }

    @Test
    public void constant_1() throws Exception {
        int value = -1;

        Assert.willCapture("1", 1);
        Assert.willCapture("value", value);
        assert 1 == value;
    }

    @Test
    public void constant_2() throws Exception {
        int value = -1;

        Assert.willCapture("2", 2);
        Assert.willCapture("value", value);
        assert 2 == value;
    }

    @Test
    public void constant_3() throws Exception {
        int value = -1;

        Assert.willCapture("3", 3);
        Assert.willCapture("value", value);
        assert 3 == value;
    }

    @Test
    public void constant_M1() throws Exception {
        int value = 0;

        Assert.willCapture("-1", -1);
        Assert.willCapture("value", value);
        assert -1 == value;
    }

    @Test
    public void big() throws Exception {
        int value = 2;

        Assert.willCapture("123456789", 123456789);
        Assert.willCapture("value", value);
        assert 123456789 == value;
    }

    @Test
    public void array() throws Exception {
        int[] array = {0, 1, 2};

        Assert.willCapture("array", array);
        assert array == null;
    }

    @Test
    public void arrayIndex() throws Exception {
        int[] array = {0, 1, 2};

        Assert.willCapture("array", array);
        Assert.willCapture("array[1]", 1);
        assert array[1] == 128;
    }

    @Test
    public void arrayLength() throws Exception {
        int[] array = {0, 1, 2};

        Assert.willCapture("array", array);
        Assert.willCapture("array.length", 3);
        assert array.length == 10;
    }

    @Test
    public void parameter() throws Exception {
        Assert.willCapture("test(12)", false);
        assert test(12);
    }

    private boolean test(int value) {
        return false;
    }
}
