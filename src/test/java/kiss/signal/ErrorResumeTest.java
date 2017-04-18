/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.signal;

import java.util.function.Function;

import org.junit.Test;

import kiss.Signal;
import kiss.SignalTester;

/**
 * @version 2017/04/06 11:46:22
 */
public class ErrorResumeTest extends SignalTester {

    @Test
    public void resumeSignal() throws Exception {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume(signal(10, 11)));

        assert main.value(10, 11);
        assert main.isCompleted();
    }

    @Test
    public void resumeNullSignal() throws Exception {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume((Signal) null));

        assert main.value();
        assert main.isError();
    }

    @Test
    public void resumeFunction() throws Exception {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume(e -> signal(10, 11)));

        assert main.value(10, 11);
        assert main.isCompleted();
    }

    @Test
    public void resumeNullFunction() throws Exception {
        monitor(() -> signal(1, 2).map(errorFunction()).errorResume((Function) null));

        assert main.value();
        assert main.isError();
    }
}
