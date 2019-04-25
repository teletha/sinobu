/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.sample.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kiss.Variable;

/**
 * @version 2017/04/19 11:38:39
 */
public class VariablePropertyAtField {

    public final Variable<String> string = Variable.empty();

    public final Variable<Integer> integer = Variable.of(0);

    public final Variable<List<String>> list = Variable.of(new ArrayList());

    public final Variable<Map<String, Long>> map = Variable.of(new HashMap());

    public final DoubleVariable sub = new DoubleVariable(10D);

    public static class DoubleVariable extends Variable<Double> {

        /**
         * @param value
         */
        public DoubleVariable(Double value) {
            super(value);
        }
    }
}
