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

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import kiss.Observer;
import kiss.Signal;

/**
 * @version 2017/04/01 11:11:33
 */
public class CompleteTest {

    private Checker checker = new Checker();

    @After
    public void clear() {
        checker.value.clear();
        checker.completed = false;
    }

    @Test
    public void from() throws Exception {
        Signal.from(1).to(checker);

        assert checker.value.size() == 1;
        assert checker.completed;
    }

    /**
     * @version 2017/04/01 11:12:47
     */
    private class Checker implements Observer<Integer> {

        List<Integer> value = new ArrayList();

        boolean completed;

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(Integer v) {
            value.add(v);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void complete() {
            completed = true;
        }
    }
}
