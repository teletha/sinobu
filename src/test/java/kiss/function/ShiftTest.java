/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.function;

import org.junit.jupiter.api.Test;

import kiss.Variable;
import kiss.WiseBiConsumer;
import kiss.WiseConsumer;
import kiss.WiseTriConsumer;

/**
 * 
 */
public class ShiftTest {

    @Test
    void consumer() {
        Variable<String> var = Variable.of("init");
        WiseConsumer<Variable<String>> setter = v -> v.set("change");

        setter.shift().accept(var);
        assert var.is("change");

        setter.shift().shift().accept(var);
        assert var.is("change");
    }

    @Test
    void biconsumer() {
        Variable<String> var = Variable.of("init");
        WiseBiConsumer<Variable<String>, String> setter = (v, value) -> v.set(value);

        setter.shift().accept("change", var);
        assert var.is("change");

        setter.shift().shift().accept(var, "revert");
        assert var.is("revert");
    }

    @Test
    void triconsumer() {
        Variable<String> var = Variable.of("init");
        WiseTriConsumer<Integer, Variable<String>, String> setter = (times, v, value) -> v.set(value.repeat(times));

        setter.shift().accept("change", 2, var);
        assert var.is("changechange");

        setter.shift().shift().accept(var, "shift", 1);
        assert var.is("shift");

        setter.shift().shift().shift().accept(3, var, "revert");
        assert var.is("revertrevertrevert");
    }
}
