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

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public void intBigConstantAndVariable() throws Exception {
        int value = 2;

        test.willCapture("123456789", 123456789);
        test.willCapture("value", value);
        assert 123456789 == value;
    }

    @Test
    public void longConstantAndVariable() throws Exception {
        long value = 2;

        test.willCapture("1", 1L);
        test.willCapture("value", value);
        assert 1L == value;
    }

    @Test
    public void longBigConstantAndVariable() throws Exception {
        long value = 2;

        test.willCapture("1234567890123", 1234567890123L);
        test.willCapture("value", value);
        assert 1234567890123L == value;
    }

    @Test
    public void floatConstantAndVariable() throws Exception {
        float value = 2;

        test.willCapture("1.0", 1f);
        test.willCapture("value", value);
        assert 1f == value;
    }

    @Test
    public void floatBigConstantAndVariable() throws Exception {
        float value = 2;

        test.willCapture("0.12345678", 0.12345678f);
        test.willCapture("value", value);
        assert 0.12345678f == value;
    }

    @Test
    public void doubleConstantAndVariable() throws Exception {
        double value = 2;

        test.willCapture("1.0", 1d);
        test.willCapture("value", value);
        assert 1d == value;
    }

    @Test
    public void doubleBigConstantAndVariable() throws Exception {
        double value = 2;

        test.willCapture("0.1234567898765432", 0.1234567898765432d);
        test.willCapture("value", value);
        assert 0.1234567898765432d == value;
    }

    @Test
    public void shortConstantAndVariable() throws Exception {
        short value = 2;

        test.willCapture("1", 1);
        test.willCapture("value", (int) value);
        assert (short) 1 == value;
    }

    @Test
    public void shortBigConstantAndVariable() throws Exception {
        short value = 2;

        test.willCapture("128", 128);
        test.willCapture("value", (int) value);
        assert (short) 128 == value;
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
    public void nullLiteral1() throws Exception {
        String value = "";

        test.willCapture("value", value);
        assert value == null;
    }

    @Test
    public void nullLiteral2() throws Exception {
        String value = null;

        test.willCapture("value", value);
        assert value != null;
    }

    @Test
    public void nullValue() throws Exception {
        String value = null;

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
    public void enumLiteral() throws Exception {
        RetentionPolicy value = RetentionPolicy.CLASS;

        test.willCapture("value", RetentionPolicy.CLASS);
        test.willCapture("RetentionPolicy.RUNTIME", RetentionPolicy.RUNTIME);
        assert RetentionPolicy.RUNTIME == value;
    }

    @Test
    public void methodCall() throws Exception {
        String value = "test";

        test.willCapture("value", value);
        test.willCapture("value.equals(\"a\")", false);
        assert value.equals("a");
    }

    @Test
    public void privateMethodCall() throws Exception {
        assert privateMethod();
    }

    private boolean privateMethod() {
        return false;
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

    @Test
    public void lessThan() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("<");
        assert other < one;
    }

    @Test
    public void lessEqual() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("<=");
        assert other <= one;
    }

    @Test
    public void greaterThan() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator(">");
        assert one > other;
    }

    @Test
    public void greaterEqual() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator(">=");
        assert one >= other;
    }

    @Test
    public void addition() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("+");
        test.willUseOperator("==");
        assert one + 1 == other;
    }

    @Test
    public void subtraction() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("-");
        test.willUseOperator("==");
        assert one - 1 == other;
    }

    @Test
    public void multiplication() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("*");
        test.willUseOperator("==");
        assert one * 3 == other;
    }

    @Test
    public void division() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("/");
        test.willUseOperator("==");
        assert one / 2 == other;
    }

    @Test
    public void remainder() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("%");
        test.willUseOperator("==");
        assert one % 2 == other;
    }

    @Test
    public void negative() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("-");
        test.willUseOperator("==");
        assert -one == other;
    }

    @Test
    public void leftShift() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("<<");
        test.willUseOperator("==");
        assert one << 3 == other;
    }

    @Test
    public void rightShift() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator(">>");
        test.willUseOperator("==");
        assert one >> 3 == other;
    }

    @Test
    public void unrotateRightShift() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator(">>>");
        test.willUseOperator("==");
        assert one >>> 3 == other;
    }

    @Test
    public void or() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("|");
        test.willUseOperator("==");
        assert (one | other) == other;
    }

    @Test
    public void xor() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("^");
        test.willUseOperator("==");
        assert (one ^ other) == other;
    }

    @Test
    public void and() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("&");
        test.willUseOperator("==");
        assert (one & other) == other;
    }

    @Test
    public void increment() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("++");
        test.willUseOperator("==");
        assert one++ == other;
    }

    @Test
    public void incrementPre() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one + 1);
        test.willCapture("other", other);
        test.willUseOperator("++");
        test.willUseOperator("==");
        assert ++one == other;
    }

    @Test
    public void decrement() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one);
        test.willCapture("other", other);
        test.willUseOperator("--");
        test.willUseOperator("==");
        assert one-- == other;
    }

    @Test
    public void decrementPre() {
        int one = 10;
        int other = 20;

        test.willCapture("one", one - 1);
        test.willCapture("other", other);
        test.willUseOperator("--");
        test.willUseOperator("==");
        assert --one == other;
    }

    @Test
    public void instanceOf() {
        Object value = "test";

        test.willCapture("value", value);
        test.willUseOperator("instanceof");
        assert value instanceof Map;
    }

    @Test
    public void instantiate() {
        Object value = "test";

        test.willCapture("value", value);
        test.willUseOperator("new Object()");
        assert value == new Object();
    }

    @Test
    public void instantiateWithParameter() {
        Object value = "test";

        test.willCapture("value", value);
        test.willUseOperator("new String(\"fail\")");
        assert value == new String("fail");
    }

    @Test
    public void assertTwice() throws Exception {
        int value = 2;
        assert value != 1; // success

        test.willUseOperator("==");
        test.willCapture("value", value);
        assert value == 3;
    }

    @Test
    public void throwAssertionError() {
        throw new AssertionError();
    }

    @Test
    public void throwAssertionErrorWithParameter() {
        throw new AssertionError("param");
    }

    @Test
    public void useAssertWithMessage() {
        int value = 4;

        test.willCapture("value", value);
        assert value == -1 : "this value is " + value;
    }
}
