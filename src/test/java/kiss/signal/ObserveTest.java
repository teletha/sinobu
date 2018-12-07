/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import org.junit.jupiter.api.Test;

import kiss.Variable;

/**
 * @version 2018/12/08 8:47:15
 */
class ObserveTest extends SignalTester {

    class Host {
        Variable<Integer> var = Variable.empty();

        Host(int value) {
            var.set(value);
        }
    }

    @Test
    void value() {
        monitor(1, Host.class, Integer.class, signal -> signal.observe(v -> v.var));

        Host host1 = new Host(10);
        assert main.emit(host1).value(10);
        host1.var.set(20);
        assert main.value(20);

        Host host2 = new Host(30);
        assert main.emit(host2).value(30);
        host2.var.set(40);
        assert main.value(40);

        // no-relation
        host1.var.set(50);
        assert main.value();

        assert main.isNotCompleted();
        assert main.isNotError();
        assert main.isNotDisposed();

        main.emit(Complete);
        assert main.isCompleted();
        assert main.isNotError();
        assert main.isDisposed();
    }
}
