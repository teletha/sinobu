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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import kiss.Signal;

/**
 * @version 2018/07/15 8:40:34
 */
class SignalConstructTest extends SignalTester {

    @Test
    void multipleCompletes() {
        List<String> values = new ArrayList();
        List<Throwable> errors = new ArrayList();
        AtomicInteger completes = new AtomicInteger();

        new Signal<String>((observer, disposer) -> {
            observer.complete();
            observer.complete();
            return disposer;
        }).to(values::add, errors::add, completes::incrementAndGet);

        assert values.size() == 0;
        assert errors.size() == 0;
        assert completes.get() == 1;
    }

    @Test
    void multipleCompletesWithValues() {
        List<String> values = new ArrayList();
        List<Throwable> errors = new ArrayList();
        AtomicInteger completes = new AtomicInteger();

        new Signal<String>((observer, disposer) -> {
            observer.accept("Before");
            observer.complete();
            observer.complete();
            observer.accept("After");

            return disposer;
        }).to(values::add, errors::add, completes::incrementAndGet);

        assert values.size() == 1;
        assert errors.size() == 0;
        assert completes.get() == 1;
    }

    @Test
    void multipleErrors() {
        List<String> values = new ArrayList();
        List<Throwable> errors = new ArrayList();

        new Signal<String>((observer, disposer) -> {
            observer.error(new FirstError());
            observer.error(new SecondError());
            return disposer;
        }).to(values::add, e -> errors.add(e));

        assert values.size() == 0;
        assert errors.size() == 1;
        assert errors.get(0) instanceof FirstError;
    }

    @Test
    void multipleErrorsWithValues() {
        List<String> values = new ArrayList();
        List<Throwable> errors = new ArrayList();

        new Signal<String>((observer, disposer) -> {
            observer.accept("Before");
            observer.error(new FirstError());
            observer.error(new SecondError());
            observer.accept("After");

            return disposer;
        }).to(values::add, e -> errors.add(e));

        assert values.size() == 1;
        assert errors.size() == 1;
        assert errors.get(0) instanceof FirstError;
    }

    /**
     * @version 2018/07/15 5:47:09
     */
    @SuppressWarnings("serial")
    private static class FirstError extends Error {
    }

    /**
     * @version 2018/07/15 5:47:09
     */
    @SuppressWarnings("serial")
    private static class SecondError extends Error {
    }
}
