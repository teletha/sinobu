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
 * @version 2017/04/06 2:37:23
 */
public class MapTest extends SignalTestBase {

    @Test
    public void map() throws Exception {
        monitor(() -> signal(1, 2).map(v -> v * 2));

        assert result.value(2, 4);
        assert result.completed();
    }

    @Test
    public void mapNull() throws Exception {
        monitor(() -> signal(1, 2).map(null));

        assert result.value(1, 2);
        assert result.completed();
    }

    @Test
    public void throwError() throws Exception {
        monitor(() -> signal(1, 2).map(errorFunction()));

        assert result.value();
        assert result.isError();
    }
}
