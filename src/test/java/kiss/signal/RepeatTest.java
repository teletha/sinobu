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

import org.junit.Test;

/**
 * @version 2017/04/03 11:04:09
 */
public class RepeatTest extends SignalTestBase {

    @Test
    public void repeatUntil() throws Exception {
        monitor(() -> signal(1).effect(log1).repeatUntil(() -> log1.size() < 3));
        assert result.value(1, 1, 1);
    }
}
