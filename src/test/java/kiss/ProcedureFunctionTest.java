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

import static kiss.Procedure.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @version 2014/07/18 8:50:57
 */
public class ProcedureFunctionTest {

    private int count;

    @Before
    public void initialize() {
        count = 0;
    }

    @Test
    public void runs() throws Exception {
        assert count == 0;
        λ(this::addFunction, 10).run();
        assert count == 10;
        λ(this::addConsumer, 20).run();
        assert count == 30;
        λ(this::addBiConsumer, 5, 5).run();
        assert count == 40;
        λ(this::addBiFunction, 2, 1).run();
        assert count == 43;
        λ(this::addSupplier).run();
        assert count == 44;
    }

    @Test
    public void error() throws Exception {
        assert count == 0;
        // Can't call because of throw
        // λ(this::addFunctionError, 2).run();
        λ(δ(this::addFunctionError), 2).run();
        assert count == 2;
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

    private int addFunctionError(int value) throws Exception {
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
