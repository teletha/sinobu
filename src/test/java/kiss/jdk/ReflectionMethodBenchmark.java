/*
 * Copyright (C) 2022 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.jdk;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.reflect.Method;
import java.util.function.Function;

import antibug.profiler.Benchmark;
import kiss.I;

public class ReflectionMethodBenchmark {

    private static final MethodHandle constantMH;

    static {
        try {
            constantMH = MethodHandles.lookup().findVirtual(ReflectionMethodBenchmark.class, "one", MethodType.methodType(String.class));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        Benchmark benchmark = new Benchmark().novisualize();
        ReflectionMethodBenchmark base = new ReflectionMethodBenchmark();

        MethodType type = MethodType.methodType(String.class);
        MethodHandle mh = MethodHandles.lookup().findVirtual(ReflectionMethodBenchmark.class, "one", type);
        CallSite site = new MutableCallSite(mh);
        MethodHandle callsited = site.dynamicInvoker();
        site.setTarget(mh);
        benchmark.measure("CallsetedMethodHandle", () -> {
            try {
                return (String) callsited.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        Method method = ReflectionMethodBenchmark.class.getMethod("one");
        benchmark.measure("Reflection", () -> {
            try {
                return method.invoke(base);
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });

        MethodHandle directMH = MethodHandles.lookup()
                .findVirtual(ReflectionMethodBenchmark.class, "one", MethodType.methodType(String.class));
        benchmark.measure("MethodHandle", () -> {
            try {
                return (String) directMH.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("ConstantedMethodHandle", () -> {
            try {
                return (String) constantMH.invokeExact(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        CallSite callsite = LambdaMetafactory.metafactory(MethodHandles.lookup(), "apply", MethodType
                .methodType(Function.class), directMH.type().generic(), directMH, directMH.type());
        Function function = (Function) callsite.getTarget().invokeExact();
        benchmark.measure("LambdaFactoryMethodHandle", () -> {
            try {
                return function.apply(base);
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.measure("DirectCall", () -> {
            try {
                return base.one();
            } catch (Throwable e) {
                throw I.quiet(e);
            }
        });

        benchmark.perform();
    }

    public String one() {
        return "text";
    }
}