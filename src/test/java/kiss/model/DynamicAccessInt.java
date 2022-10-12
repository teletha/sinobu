/*
 * Copyright (C) 2022 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.util.function.BiConsumer;
import java.util.function.Function;

import kiss.json.JSONMappingBenchmark.Person;

public class DynamicAccessInt implements Function, BiConsumer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(Object o, Object v) {
        ((Person) o).age = (int) v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object apply(Object o) {
        return ((Person) o).age;
    }
}
