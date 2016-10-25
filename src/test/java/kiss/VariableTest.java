/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.junit.Before;
import org.junit.Test;

/**
 * @version 2016/10/23 13:49:19
 */
public class VariableTest {

    private Variable<String> empty;

    private Variable<String> string;

    @Before
    public void init() {
        empty = Variable.of((String) null);
        string = Variable.of("value");
    }

    @Test
    public void is() {
        assert empty.is("") == false;
        assert string.is("") == false;
        assert string.is("value");
    }

    @Test
    public void isNull() {
        String value = null;
        assert empty.is(value);
        assert string.is(value) == false;
    }

    @Test
    public void isCondition() {
        Predicate<String> condition = value -> value == null;
        assert empty.is(condition) == true;
        assert string.is(condition) == false;
    }

    @Test
    public void isNullCondition() {
        Predicate<String> condition = null;
        assert empty.is(condition) == false;
        assert string.is(condition) == false;
    }

    @Test
    public void getWithDefaultValue() {
        assert empty.get("default").equals("default");
        assert string.get("default").equals("value");
    }

    @Test
    public void getWithDefaultValueSupplier() {
        assert empty.get(() -> "default").equals("default");
        assert string.get(() -> "default").equals("value");
    }

    @Test
    public void set() {
        assert empty.set("change") == null;
        assert string.set("change").equals("value");

        Supplier<String> supplier = () -> "supply";
        assert empty.set(supplier).equals("change");
        assert string.set(supplier).equals("change");

        UnaryOperator<String> operator = current -> current + " update";
        assert empty.set(operator).equals("supply");
        assert string.set(operator).equals("supply");

        assert empty.is("supply update");
        assert string.is("supply update");
    }

    @Test
    public void or() throws Exception {
        assert empty.or("text").is("text");
        assert string.or("text").is("value");
    }

    @Test
    public void orNull() throws Exception {
        String nill = null;
        assert empty.or(nill).isAbsent();
        assert string.or(nill).is("value");
    }

    @Test
    public void orVariable() throws Exception {
        assert empty.or(string).is("value");
        assert string.or(empty).is("value");
    }

    @Test
    public void orNullVariable() throws Exception {
        Variable<String> nill = null;
        assert empty.or(nill).isAbsent();
        assert string.or(nill).is("value");
    }
}
