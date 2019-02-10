/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Variable;
import kiss.WiseFunction;

/**
 * @version 2018/12/08 14:05:03
 */
class FlatVariableTest extends SignalTester {

    class Person {
        Variable<Integer> age = Variable.empty();

        Person(int value) {
            age.set(value);
        }
    }

    @Test
    void value() {
        monitor(Person.class, Integer.class, signal -> signal.flatVariable(v -> v.age));

        Person me = new Person(20);
        assert main.emit(me).value(20);

        // change age
        me.age.set(21);
        assert main.value(21);

        // another person
        Person misa = new Person(19);
        assert main.emit(misa).value(19);

        // change misa's age
        misa.age.set(20);
        assert main.value(20);

        // change my age again
        me.age.set(22);
        assert main.value(22);

        // change misa's age again
        misa.age.set(21);
        assert main.value(21);

        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();
    }

    @Test
    void complete() {
        monitor(Person.class, Integer.class, signal -> signal.flatVariable(v -> v.age));

        Person me = new Person(20);
        assert main.emit(me, Complete).value(20);

        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();

        me.age.set(40);
        assert main.value();
    }

    @Test
    void error() {
        monitor(Person.class, Integer.class, signal -> signal.flatVariable(v -> v.age));

        Person me = new Person(20);
        assert main.emit(me, Error).value(20);

        assert main.isNotCompleted();
        assert main.isError();
        assert main.isDisposed();
    }

    @Test
    void rejectNull() {
        assertThrows(NullPointerException.class, () -> {
            monitor(() -> signal(1, 2).flatVariable((WiseFunction) null));
        });
    }

    @Test
    void fromFinitToInfinit() {
        Person person = new Person(33);

        monitor(1, () -> I.signal(person).flatVariable(p -> p.age));

        assert main.value(33);
        assert main.isNotError();
        assert main.isCompleted();
        assert main.isDisposed();
    }
}
