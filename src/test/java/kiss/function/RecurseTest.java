/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.function;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.WiseBiConsumer;
import kiss.WiseBiFunction;
import kiss.WiseConsumer;
import kiss.WiseFunction;
import kiss.WiseTriConsumer;
import kiss.WiseTriFunction;

class RecurseTest {

    @Test
    void Runnable() {
        AtomicInteger value = new AtomicInteger();
        Runnable function = I.recurse(self -> {
            if (value.get() < 10) {
                value.incrementAndGet();
                self.run();
            }
        });
        function.run();

        assert value.get() == 10;
    }

    @Test
    void RunnableNull() {
        assertThrows(NullPointerException.class, () -> I.recurse((WiseConsumer) null));
    }

    @Test
    void Consumer() {
        AtomicInteger value = new AtomicInteger();
        Consumer<Integer> function = I.recurse((self, p) -> {
            value.set(p);

            if (p < 10) {
                self.accept(p + 1);
            }
        });
        function.accept(0);

        assert value.get() == 10;
    }

    @Test
    void ConsumerNull() {
        assertThrows(NullPointerException.class, () -> I.recurse((WiseBiConsumer) null));
    }

    @Test
    void BiConsumer() {
        AtomicInteger value = new AtomicInteger();
        BiConsumer<Integer, Integer> function = I.recurse((self, p, q) -> {
            value.set(p + q);

            if (p < 10) {
                self.accept(p + 1, q + 2);
            }
        });
        function.accept(0, 10);

        assert value.get() == 40;
    }

    @Test
    void BiConsumerNull() {
        assertThrows(NullPointerException.class, () -> I.recurse((WiseTriConsumer) null));
    }

    @Test
    void Supplier() {
        AtomicInteger value = new AtomicInteger();
        Supplier<Integer> function = I.recurse(self -> {
            if (value.get() < 10) {
                value.incrementAndGet();
                return self.get();
            } else {
                return value.get();
            }
        });

        assert function.get() == 10;
    }

    @Test
    void SupplierNull() {
        assertThrows(NullPointerException.class, () -> I.recurse((WiseFunction) null));
    }

    @Test
    void Function() {
        Function<Integer, Integer> function = I.recurse((self, param) -> {
            if (param < 10) {
                return self.apply(param + 1);
            } else {
                return param;
            }
        });

        assert function.apply(0) == 10;
    }

    @Test
    void FunctionNull() {
        assertThrows(NullPointerException.class, () -> I.recurse((WiseBiFunction) null));
    }

    @Test
    void BiFunction() {
        BiFunction<Integer, Integer, Integer> function = I.recurse((self, param1, param2) -> {
            if (param1 < 10) {
                return self.apply(param1 + 1, param2 + 2);
            } else {
                return param2;
            }
        });

        assert function.apply(0, 10) == 30;
    }

    @Test
    void BiFunctionNull() {
        assertThrows(NullPointerException.class, () -> I.recurse((WiseTriFunction) null));
    }
}