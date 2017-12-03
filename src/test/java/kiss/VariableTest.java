/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
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
        empty = Variable.empty();
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
    public void isIfConsumer() throws Exception {
        AtomicReference<String> value = new AtomicReference();

        string.is("value", value::set);
        assert value.get().equals("value");
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

    @Test
    public void mapFunction() {
        Function<String, Integer> size = e -> e.length();
        assert string.map(size).is(5);
        assert empty.map(size).isAbsent();
    }

    @Test
    public void mapFunctionNull() {
        assert string.map((Function) null).isAbsent();
        assert empty.map((Function) null).isAbsent();
    }

    @Test
    public void flatMap() {
        Function<String, Variable<Integer>> size = e -> Variable.of(e.length());
        assert string.flatMap(size).is(5);
        assert empty.flatMap(size).isAbsent();
    }

    @Test
    public void flatMapNull() {
        assert string.flatMap(null).isAbsent();
        assert empty.flatMap(null).isAbsent();
    }

    @Test
    public void correctHashAndEqual() {
        Variable<String> one = Variable.of("one");
        Variable<String> other = Variable.of("one");
        assert one != other;
        assert one.hashCode() == other.hashCode();
        assert one.equals(other);
        assert other.equals(one);
    }

    @Test
    public void incorrectHashAndEqual() {
        Variable<String> one = Variable.of("one");
        Variable<String> other = Variable.of("other");
        assert one != other;
        assert one.hashCode() != other.hashCode();
        assert one.equals(other) == false;
        assert other.equals(one) == false;
    }

    @Test
    public void emptyHashAndEqual() {
        Variable<String> one = Variable.empty();
        Variable<String> other = Variable.empty();
        assert one != other;
        assert one.hashCode() == other.hashCode();
        assert one.equals(other);
        assert other.equals(one);
    }

    @Test
    public void observeDispose() throws Exception {
        Variable<String> start = Variable.of("test");
        Variable<String> end = start.observe().take(1).to();
        assert end.isAbsent();
        assert checkObserverSize(start) == 1;

        start.set("first");
        assert end.is("first");
        assert checkObserverSize(start) == 0;

        start.set("second");
        assert end.is("first");
    }

    /**
     * Helper method to private field data.
     * 
     * @param variable
     * @return
     */
    private int checkObserverSize(Variable variable) {
        try {
            Field field = Variable.class.getDeclaredField("observers");
            field.setAccessible(true);

            List list = (List) field.get(variable);
            return list == null ? 0 : list.size();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}
