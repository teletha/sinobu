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
 * @version 2017/04/07 1:57:35
 */
public class TakeTest extends SignalTestBase {

    @Test
    public void takeAt() throws Exception {
        monitor(() -> signal(1, 2, 3, 4, 5, 6).takeAt(index -> 3 < index));
        assert result.value(5, 6);

        monitor(() -> signal(1, 2, 3, 4, 5, 6).takeAt(index -> index % 2 == 0));
        assert result.value(1, 3, 5);
    }
}
