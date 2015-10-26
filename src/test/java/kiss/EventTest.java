/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @version 2015/10/25 12:25:10
 */
public class EventTest {

    @Test
    public void testname() throws Exception {
        Events timing = Events.from();

        Events.from(1, 2, 3, 4, 5).interval(1, TimeUnit.SECONDS, timing).to(v -> {
            System.err.println(v);
        });

        Events.from(11, 12, 13, 14, 15).interval(1, TimeUnit.SECONDS, timing).to(v -> {
            System.err.println(v);
        });

        Thread.sleep(10000);
    }
}
