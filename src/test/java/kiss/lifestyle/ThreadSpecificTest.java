/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lifestyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Managed;
import kiss.ThreadSpecific;
import kiss.WiseFunction;

class ThreadSpecificTest {

    @Test
    void onSameThread() {
        ThreadSpecificClass instance1 = I.make(ThreadSpecificClass.class);
        assert instance1 != null;

        ThreadSpecificClass instance2 = I.make(ThreadSpecificClass.class);
        assert instance2 != null;
        assert instance1 == instance2;
    }

    @Test
    void onDifferentThread() throws Exception {
        List<Callable<ThreadSpecificClass>> tasks = new ArrayList();
        for (int i = 0; i < 2; i++) {
            tasks.add(() -> I.make(ThreadSpecificClass.class));
        }

        Set<ThreadSpecificClass> results = Executors.newCachedThreadPool()
                .invokeAll(tasks)
                .stream()
                .map((WiseFunction<Future, ThreadSpecificClass>) Future<ThreadSpecificClass>::get)
                .collect(Collectors.toSet());
        assert results.size() == 2;
    }

    @Managed(value = ThreadSpecific.class)
    private static class ThreadSpecificClass {
    }
}
