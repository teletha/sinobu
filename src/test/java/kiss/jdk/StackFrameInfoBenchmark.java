/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.jdk;

import java.lang.StackWalker.StackFrame;

import antibug.profiler.Benchmark;

public class StackFrameInfoBenchmark {

    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();

        StackFrame frame = StackWalker.getInstance().walk(s -> s.skip(0).findAny().get());
        System.out.println(frame.toString());
        System.out.println(frame.getClassName() + frame.getMethodName() + "(" + frame.getFileName() + ":" + frame.getLineNumber() + ")");

        benchmark.measure("toString", () -> {
            return frame.toString();
        });

        benchmark.measure("custom", () -> {
            return frame.getClassName() + frame.getMethodName() + "(" + frame.getFileName() + ":" + frame.getLineNumber() + ")";
        });

        benchmark.perform();
    }
}
