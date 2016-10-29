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

import java.util.Optional;
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
    public void of() {
        Variable<String> var = Variable.of("A");
        assert var != null;
        assert var.v != null;
        assert var.v.equals("A");
    }

    @Test
    public void ofNull() {
        Variable<String> var = Variable.of((String) null);
        assert var != null;
        assert var.v == null;
    }

    @Test
    public void ofOptional() {
        Variable<String> var = Variable.of(Optional.of("A"));
        assert var != null;
        assert var.v != null;
        assert var.v.equals("A");

        var = Variable.of(Optional.empty());
        assert var != null;
        assert var.v == null;
    }

    @Test
    public void ofNullOptional() {
        Variable<String> var = Variable.of((Optional) null);
        assert var != null;
        assert var.v == null;
    }

    @Test
    public void ofSupplier() {
        Variable<String> var = Variable.of(() -> "A");
        assert var != null;
        assert var.v != null;
        assert var.v.equals("A");

        var = Variable.of(() -> null);
        assert var != null;
        assert var.v == null;
    }

    @Test
    public void ofNullSupplier() {
        Variable<String> var = Variable.of((Supplier) null);
        assert var != null;
        assert var.v == null;
    }

    @Test
    public void emptyIsNotSingleton() {
        Variable<String> e1 = Variable.empty();
        Variable<String> e2 = Variable.empty();
        assert e1 != e2;
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
        assert string.set("change").equals("value");
        assert string.set(() -> "supply").equals("change");
        assert string.set(current -> current + " update").equals("supply");
        assert string.set(Optional.of("optional")).equals("supply update");
        assert string.set(Variable.of("variable")).equals("optional");
        assert string.is("variable");
    }

    @Test
    public void setNull() {
        assert string.set((String) null).equals("value");
        assert string.isAbsent();
    }

    @Test
    public void setNullSupplier() {
        assert string.set((Supplier<String>) null).equals("value");
        assert string.isAbsent();
    }

    @Test
    public void setNullOperator() {
        assert string.set((UnaryOperator<String>) null).equals("value");
        assert string.isAbsent();
    }

    @Test
    public void setNullOptional() {
        assert string.set((Optional) null).equals("value");
        assert string.isAbsent();
    }

    @Test
    public void setNullVariable() {
        assert string.set((Variable) null).equals("value");
        assert string.isAbsent();
    }

    @Test
    public void setIf() {
        assert string.setIf(I.reject(), "change").equals("value");
        assert string.setIf(I.reject(), () -> "supply").equals("value");
        assert string.setIf(I.reject(), current -> current + " update").equals("value");
        assert string.setIf(I.reject(), Optional.of("optional")).equals("value");
        assert string.setIf(I.reject(), Variable.of("variable")).equals("value");
        assert string.is("value");

        assert string.setIf(I.accept(), "change").equals("value");
        assert string.setIf(I.accept(), () -> "supply").equals("change");
        assert string.setIf(I.accept(), current -> current + " update").equals("supply");
        assert string.setIf(I.accept(), Optional.of("optional")).equals("supply update");
        assert string.setIf(I.accept(), Variable.of("variable")).equals("optional");
        assert string.is("variable");
    }

    @Test
    public void setIfNullCondition() {
        assert string.setIf(null, "change").equals("value");
        assert string.setIf(null, () -> "supply").equals("value");
        assert string.setIf(null, current -> current + " update").equals("value");
        assert string.setIf(null, Optional.of("optional")).equals("value");
        assert string.setIf(null, Variable.of("variable")).equals("value");
        assert string.is("value");
    }

    @Test
    public void let() {
        assert string.let("immutable").equals("value");
        assert string.set("failed").equals("immutable");
        assert string.let("failed").equals("immutable");
    }

    @Test
    public void letNull() {
        assert string.let((String) null).equals("value");
        assert string.set("failed") == null;
        assert string.let("failed") == null;
    }

    @Test
    public void letIf() {
        assert string.letIf(I.reject(), "rejected").equals("value");

        assert string.letIf(I.accept(), "accepted").equals("value");
        assert string.letIf(I.accept(), "failed").equals("accepted");
        assert string.letIf(I.accept(), "failed").equals("accepted");
    }

    @Test
    public void letIfNull() {
        assert string.letIf(I.reject(), (String) null).equals("value");
        assert string.setIf(I.reject(), "rejected").equals("value");
        assert string.letIf(I.reject(), "rejected").equals("value");

        assert string.letIf(I.accept(), (String) null).equals("value");
        assert string.setIf(I.accept(), "failed") == null;
        assert string.letIf(I.accept(), "failed") == null;
    }

    @Test
    public void letSupplier() {
        Supplier<String> updater = () -> "supplied";
        assert string.let(updater).equals("value");
        assert string.let(updater).equals("supplied");
    }

    @Test
    public void letNullSupplier() {
        Supplier<String> updater = null;
        assert string.let(updater).equals("value");
        assert string.let(() -> "failed") == null;
        assert string.let(() -> "failed") == null;
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
