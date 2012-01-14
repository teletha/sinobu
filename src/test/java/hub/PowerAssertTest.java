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

import java.util.ArrayList;
import java.util.List;

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
        test.willCapture("value", (int) value);
        assert (short) 1 == value;
    }

    @Test
    public void booleanConstantAndVariable() throws Exception {
        boolean value = false;

        test.willCapture("value", value);
        assert value;
    }

    @Test
    public void objectConstantAndVariable() throws Exception {
        String value = "test";

        test.willCapture("value", value);
        assert value == "test";
    }

    @Test
    public void methodCall() throws Exception {
        String value = "test";

        test.willCapture("value", value);
        test.willCapture("value.equals(\"a\")", false);
        assert value.equals("a");
    }

    @Test
    public void methodCalls() throws Exception {
        String value = "test";

        test.willCapture("value", value);
        test.willCapture("value.substring(2)", "st");
        test.willCapture("value.substring(2).equals(\"xx\")", false);
        assert value.substring(2).equals("xx");
    }

    @Test
    public void methodCallInt() throws Exception {
        List list = new ArrayList();
        list.add("a");
        list.add("b");
        list.add("c");

        test.willCapture("list", list);
        test.willCapture("list.size()", 3);
        assert list.size() == 34;
    }

    public void asm() {
        PowerAssertionContext context = new PowerAssertionContext();
        boolean value = false;
        context.addMethod("equals", "test", false);
        context.addExpression("==");
        context.recodeLocalVariable(null, null, null, null, 1);

        Object aaa = "test".substring(1);
        Character.valueOf('c');

        throw new PowerAssertionError(context);
    }
}
