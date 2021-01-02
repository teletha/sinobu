/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.signal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Signaling;

class SignalingTest {

    @Test
    void accept() {
        Signaling<String> signaling = new Signaling();
        List<String> result = signaling.expose.map(v -> v.toLowerCase()).toList();
        assert result.size() == 0;

        signaling.accept("FIRST");
        assert result.get(0).equals("first");

        signaling.accept("SECOND");
        assert result.get(1).equals("second");
    }

    @Test
    void error() {
        Signaling<String> signaling = new Signaling();
        List<String> values = new ArrayList();
        List<Throwable> errors = new ArrayList();
        signaling.expose.to(values::add, errors::add, I.NoOP);

        signaling.accept("FIRST");
        assert values.get(0).equals("FIRST");

        signaling.error(new Error());
        assert errors.size() == 1;

        signaling.accept("Don't emit");
        assert values.size() == 1;

        signaling.error(new Error());
        assert errors.size() == 1;
    }

    @Test
    void complete() {
        Signaling<String> signaling = new Signaling();
        List<String> values = new ArrayList();
        AtomicInteger completes = new AtomicInteger();
        signaling.expose.to(e -> values.add(e), null, completes::incrementAndGet);

        signaling.accept("FIRST");
        assert values.get(0).equals("FIRST");

        signaling.complete();
        assert completes.get() == 1;

        signaling.accept("Don't emit");
        assert values.size() == 1;

        signaling.complete();
        assert completes.get() == 1;
    }
}