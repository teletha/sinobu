/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package hub;

import hub.PowerAssert.PowerAssertionContext;
import hub.PowerAssert.PowerAssertionError;

import org.junit.Rule;
import org.junit.Test;


/**
 * @version 2012/01/10 9:53:52
 */
public class PowerAssertTest {

    @Rule
    public static final PowerAssert test = new PowerAssert(true);

    @Test
    public void intConstantAndVariable() throws Exception {
        int value = 2;

        test.willCapture("1", 1);
        test.willCapture("value", value);
        assert 1 == value;
    }

    @Test
    public void longConstantAndVariable() throws Exception {
        long value = 2;

        test.willCapture("1", 1L);
        test.willCapture("value", value);
        assert 1L == value;
    }

    @Test
    public void floatConstantAndVariable() throws Exception {
        float value = 2;

        test.willCapture("1.0", 1f);
        test.willCapture("value", value);
        assert 1f == value;
    }

    @Test
    public void doubleConstantAndVariable() throws Exception {
        double value = 2;

        test.willCapture("1.0", 1d);
        test.willCapture("value", value);
        assert 1d == value;
    }

    @Test
    public void shortConstantAndVariable() throws Exception {
        short value = 2;

        test.willCapture("1", 1);
        test.willCapture("value", value);
        assert (short) 1 == value;
    }

    @Test
    public void booleanConstantAndVariable() throws Exception {
        boolean value = false;

        test.willCapture("value", value);
        assert value;
    }

    public void asm() {
        PowerAssertionContext context = new PowerAssertionContext();
        boolean value = true;
        context.add(false);
        context.addVariable(value, "value");
        context.addExpression("==");

        throw new PowerAssertionError(context);
    }
}
