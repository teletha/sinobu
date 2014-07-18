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
        Runnable function = run(10, value -> count += value);

        assert count == 0;
        function.run();
        assert count == 10;
    }
}
