/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VariableTest {

    private Variable<String> empty;

    private Variable<String> string;

    @BeforeEach
    void init() {
        empty = Variable.empty();
        string = Variable.of("value");
    }

    @Test
    void of() {
        Variable<String> var = Variable.of("A");
        assert var != null;
        assert var.v != null;
        assert var.v.equals("A");
    }

    @Test
    void ofNull() {
        Variable<String> var = Variable.of((String) null);
        assert var != null;
        assert var.v == null;
    }

    @Test
    void ofSupplier() {
        Variable<String> var = Variable.of(() -> "A");
        assert var != null;
        assert var.v != null;
        assert var.v.equals("A");

        var = Variable.of(() -> null);
        assert var != null;
        assert var.v == null;
    }

    @Test
    void ofNullSupplier() {
        Variable<String> var = Variable.of((Supplier) null);
        assert var != null;
        assert var.v == null;
    }

    @Test
    void emptyIsNotSingleton() {
        Variable<String> e1 = Variable.empty();
        Variable<String> e2 = Variable.empty();
        assert e1 != e2;
    }

    @Test
    void is() {
        assert empty.is("") == false;
        assert string.is("") == false;
        assert string.is("value");
    }

    @Test
    void isNull() {
        String value = null;
        assert empty.is(value);
        assert string.is(value) == false;
    }

    @Test
    void isCondition() {
        Predicate<String> condition = value -> value == null;
        assert empty.isNot(condition) == false;
        assert string.isNot(condition) == true;
    }

    @Test
    void isNullCondition() {
        Predicate<String> condition = null;
        assert empty.isNot(condition) == true;
        assert string.isNot(condition) == true;
    }

    @Test
    void isNot() {
        assert empty.is("") == false;
        assert string.is("") == false;
        assert string.is("value");
    }

    @Test
    void isNotNull() {
        String value = null;
        assert empty.isNot(value) == false;
        assert string.isNot(value) == true;
    }

    @Test
    void isNotCondition() {
        Predicate<String> condition = value -> value == null;
        assert empty.isNot(condition) == false;
        assert string.isNot(condition) == true;
    }

    @Test
    void isNotNullCondition() {
        Predicate<String> condition = null;
        assert empty.isNot(condition) == true;
        assert string.isNot(condition) == true;
    }

    @Test
    void accept() {
        string.accept("ok");
        assert string.is("ok");
    }

    @Test
    void set() {
        assert string.set("change").equals("value");
        assert string.set(() -> "supply").equals("change");
        assert string.set(current -> current + " update").equals("supply");
        assert string.set(Variable.of("variable")).equals("supply update");
        assert string.is("variable");
    }

    @Test
    void setNull() {
        assert string.set((String) null).equals("value");
        assert string.isAbsent();
    }

    @Test
    void setNullSupplier() {
        assert string.set((Supplier<String>) null).equals("value");
        assert string.isAbsent();
    }

    @Test
    void setNullOperator() {
        assert string.set((UnaryOperator<String>) null).equals("value");
        assert string.isAbsent();
    }

    @Test
    void setNullVariable() {
        assert string.set((Variable) null).equals("value");
        assert string.isAbsent();
    }

    @Test
    void or() {
        assert empty.or("text").equals("text");
        assert string.or("text").equals("value");
    }

    @Test
    void orNull() {
        String nill = null;
        assert empty.or(nill) == null;
        assert string.or(nill).equals("value");
    }

    @Test
    void mapFunction() {
        Function<String, Integer> size = e -> e.length();
        assert string.map(size).is(5);
        assert empty.map(size).isAbsent();
    }

    @Test
    void mapFunctionNull() {
        assert string.map((Function) null).isAbsent();
        assert empty.map((Function) null).isAbsent();
    }

    @Test
    void flatMap() {
        Function<String, Variable<Integer>> size = e -> Variable.of(e.length());
        assert string.flatMap(size).is(5);
        assert empty.flatMap(size).isAbsent();
    }

    @Test
    void flatMapNull() {
        assert string.flatMap(null).isAbsent();
        assert empty.flatMap(null).isAbsent();
    }

    @Test
    void correctHashAndEqual() {
        Variable<String> one = Variable.of("one");
        Variable<String> other = Variable.of("one");
        assert one != other;
        assert one.hashCode() == other.hashCode();
        assert one.equals(other);
        assert other.equals(one);
    }

    @Test
    void incorrectHashAndEqual() {
        Variable<String> one = Variable.of("one");
        Variable<String> other = Variable.of("other");
        assert one != other;
        assert one.hashCode() != other.hashCode();
        assert one.equals(other) == false;
        assert other.equals(one) == false;
    }

    @Test
    void emptyHashAndEqual() {
        Variable<String> one = Variable.empty();
        Variable<String> other = Variable.empty();
        assert one != other;
        assert one.hashCode() == other.hashCode();
        assert one.equals(other);
        assert other.equals(one);
    }

    @Test
    void observeDispose() throws Exception {
        Variable<String> start = Variable.of("test");
        Variable<String> end = start.observe().take(1).to();
        assert end.isAbsent();
        assert start.signaling.observers.size() == 1;

        start.set("first");
        assert end.is("first");
        assert start.signaling.observers.size() == 0;

        start.set("second");
        assert end.is("first");
    }

    @Test
    void intercept() {
        Variable<String> upper = Variable.<String> empty().intercept((o, n) -> n.toUpperCase());
        upper.set("lower");
        assert upper.get().equals("LOWER");
    }

    @Test
    void require() {
        Variable<String> min = Variable.<String> empty().intercept((o, n) -> {
            return n.length() <= 4 ? n : o;
        });
        min.set("ok");
        assert min.get().equals("ok");

        min.set("pass");
        assert min.get().equals("pass");

        min.set("non-qualified");
        assert min.get().equals("pass");
    }

    @Test
    void fix() {
        Variable<String> var = Variable.empty();
        var.set("change");
        assert var.is("change");

        var.fix();
        var.set("fail");
        assert var.is("change");
    }
}
