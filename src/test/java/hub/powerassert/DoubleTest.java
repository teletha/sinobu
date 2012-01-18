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
public class DoubleTest {

    @Rule
    public static final PowerAssert Assert = new PowerAssert(true);

    @Test
    public void constant_0() throws Exception {
        double value = -1;

        Assert.willUse("0");
        Assert.willCapture("value", value);
        assert 0 == value;
    }

    @Test
    public void constant_1() throws Exception {
        double value = -1;

        Assert.willUse("1");
        Assert.willCapture("value", value);
        assert 1 == value;
    }

    @Test
    public void constant_2() throws Exception {
        double value = -1;

        Assert.willUse("2");
        Assert.willCapture("value", value);
        assert 2 == value;
    }

    @Test
    public void constant_3() throws Exception {
        double value = -1;

        Assert.willUse("3");
        Assert.willCapture("value", value);
        assert 3 == value;
    }

    @Test
    public void constant_M1() throws Exception {
        double value = 0;

        Assert.willUse("-1");
        Assert.willCapture("value", value);
        assert -1 == value;
    }

    @Test
    public void big() throws Exception {
        double value = 2;

        Assert.willUse("0.1234567898765432");
        Assert.willCapture("value", value);
        assert 0.1234567898765432d == value;
    }

    @Test
    public void array() throws Exception {
        double[] array = {0, 1, 2};

        Assert.willCapture("array", array);
        assert array == null;
    }

    @Test
    public void arrayIndex() throws Exception {
        double[] array = {0, 1, 2};

        Assert.willCapture("array", array);
        Assert.willCapture("array[1]", 1d);
        assert array[1] == 128;
    }

    @Test
    public void arrayLength() throws Exception {
        double[] array = {0, 1, 2};

        Assert.willCapture("array", array);
        Assert.willCapture("array.length", 3);
        assert array.length == 10;
    }

    @Test
    public void method() throws Exception {
        Assert.willCapture("test()", 1d);
        assert test() == 2;
    }

    double test() {
        return 1;
    }

    @Test
    public void parameter() throws Exception {
        Assert.willCapture("test(0.123456)", false);
        assert test(0.123456d);
    }

    private boolean test(double value) {
        return false;
    }

    /** The tester. */
    private double doubleField = 32.1011d;

    /** The tester. */
    private static double doubleFieldStatic = 32.1011d;

    @Test
    public void fieldDoubleAccess() throws Exception {
        Assert.willCapture("this.doubleField", 32.1011d);
        assert doubleField == 0;
    }

    @Test
    public void fieldDoubleStaticAccess() throws Exception {
        Assert.willCapture("DoubleTest.doubleFieldStatic", 32.1011d);
        assert doubleFieldStatic == 0;
    }
}
