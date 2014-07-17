/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @version 2014/07/18 2:08:54
 */
public class Task implements Runnable {

    private final List<Runnable> tasks = new ArrayList();

    public void add(Runnable task) {
        if (task != null) {
            tasks.add(task);
        }
    }

    public <T> void add(T param, Consumer<T> task) {
        add(Shape.with(param, task));
    }

    public <P1, P2> void add(P1 param1, P2 param2, BiConsumer<P1, P2> task) {
        add(Shape.with(param2, Shape.with(param1, task)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        for (Runnable task : tasks) {
            task.run();
        }
    }

    /**
     * @version 2014/07/18 2:17:35
     */
    public static class Shape {

        public static <Param> Runnable with(Param parameter, Consumer<Param> consumer) {
            return () -> consumer.accept(parameter);
        }

        public static <Param, T> Consumer<T> with(Param parameter, BiConsumer<Param, T> consumer) {
            return param -> consumer.accept(parameter, param);
        }
    }

}
