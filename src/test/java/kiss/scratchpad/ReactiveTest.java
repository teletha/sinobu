/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

/**
 * @version 2014/01/30 22:02:21
 */
public class ReactiveTest {

    public void action(Value<String> value) {

    }

    public static class Value<T> {

        public T current;

        /**
         * @param subscriber
         */
        public Value() {
            this(null);
        }

        public Value(T initial) {
            current = initial;
        }

    }
}
