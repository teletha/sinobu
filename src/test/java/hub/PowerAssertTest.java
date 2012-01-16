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

import hub.PowerAssert.PowerAssertContext;

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
    public void classLiteral() throws Exception {
        Class value = int.class;

        test.willCapture("value", value);
        test.willCapture("Integer.class", Integer.class);
        assert Integer.class == value;
    }

    @Test
    public void classLiteralWithMethodCall() throws Exception {
        test.willCapture("Integer.class.getName()", "java.lang.Integer");
        assert Integer.class.getName() == "fail";
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

    @Test
    public void methodCallObject() throws Exception {
        List list = new ArrayList();
        list.add("a");
        list.add("b");
        list.add("c");

        test.willCapture("list", list);
        test.willCapture("list.get(1)", "b");
        assert list.get(1) == "fail";
    }

    @Test
    public void methodStaticCall() throws Exception {
        Object value = "";

        test.willCapture("Integer.valueOf(10)", new Integer(10));
        test.willCapture("value", "");
        assert Integer.valueOf(10) == value;
    }

    /** The tester. */
    private int intField = 11;

    /** The tester. */
    private static int intFieldStatic = 11;

    @Test
    public void fieldIntAccess() throws Exception {
        test.willCapture("this.intField", 11);
        assert intField == 0;
    }

    @Test
    public void fieldIntStaticAccess() throws Exception {
        test.willCapture("PowerAssertTest.intFieldStatic", 11);
        assert intFieldStatic == 0;
    }

    /** The tester. */
    private boolean booleanField = false;

    /** The tester. */
    private static boolean booleanFieldStatic = false;

    @Test
    public void fieldBooleanAccess() throws Exception {
        test.willCapture("this.booleanField", false);
        assert booleanField;
    }

    @Test
    public void fieldBooleanStaticAccess() throws Exception {
        test.willCapture("PowerAssertTest.booleanFieldStatic", false);
        assert booleanFieldStatic;
    }

    /** The tester. */
    private String objectField = "hitagi";

    /** The tester. */
    private static String objectFieldStatic = "hitagi";

    @Test
    public void fieldObjectAccess() throws Exception {
        test.willCapture("this.objectField", "hitagi");
        assert objectField == "nadeko";
    }

    @Test
    public void fieldObjectStaticAccess() throws Exception {
        test.willCapture("PowerAssertTest.objectFieldStatic", "hitagi");
        assert objectFieldStatic == "nadeko";
    }

    public void asm() {

        assert intField == 0;
        PowerAssertContext.get().recodeField("test", intField);
        throw new AssertionError();
    }
}
