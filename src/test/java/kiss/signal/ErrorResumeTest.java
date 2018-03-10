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

import java.util.function.Function;

import org.junit.Test;

import kiss.Signal;

/**
 * @version 2018/03/11 2:53:01
 */
public class ErrorResumeTest extends SignalTester {

    @Test
    public void resumeSignal() {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume(signal(10, 11)));

        assert main.value(10, 11);
        assert main.isCompleted();
        assert main.isNotError();
    }

    @Test
    public void resumeNullSignal() {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume((Signal) null));

        assert main.value();
        assert main.isError();
    }

    @Test
    public void resumeFunction() {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume(e -> signal(10, 11)));

        assert main.value(10, 11);
        assert main.isCompleted();
        assert main.isNotError();
    }

    @Test
    public void resumeNullFunction() {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume((Function) null));

        assert main.value();
        assert main.isError();
    }
}
