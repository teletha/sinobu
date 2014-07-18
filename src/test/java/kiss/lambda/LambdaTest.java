/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lambda;

import static kiss.Lambda.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @version 2014/07/18 8:50:57
 */
public class LambdaTest {

    private int count;

    @Before
    public void initialize() {
        count = 0;
    }

    @Test
    public void runs() throws Exception {
        assert count == 0;
        run(10, this::addFunction).dispose();
        assert count == 10;
        run(20, this::addConsumer).dispose();
        assert count == 30;
        run(5, 5, this::addBiConsumer).dispose();
        assert count == 40;
        run(2, 1, this::addBiFunction).dispose();
        assert count == 43;
        run(this::addSupplier).dispose();
        assert count == 44;
    }

    /**
     * Test helper.
     */
    private int addSupplier() {
        count += 1;

        return count;
    }

    /**
     * Test helper.
     */
    private void addConsumer(int value) {
        count += value;
    }

    /**
     * Test helper.
     */
    private void addBiConsumer(int x, int y) {
        count += x + y;
    }

    /**
     * Test helper.
     */
    private int addFunction(int value) {
        count += value;

        return count;
    }

    /**
     * Test helper.
     */
    private int addBiFunction(int x, int y) {
        count += x + y;

        return count;
    }
}
