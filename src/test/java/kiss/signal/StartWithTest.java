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

import java.util.stream.BaseStream;

import org.junit.Test;

/**
 * @version 2017/04/04 12:34:58
 */
public class StartWithTest extends SignalTestBase {

    @Test
    public void value() throws Exception {
        monitor(() -> signal(1, 2).startWith(0));

        assert result.value(0, 1, 2);
    }

    @Test
    public void values() throws Exception {
        monitor(() -> signal(1, 2).startWith(-1, 0));

        assert result.value(-1, 0, 1, 2);
    }

    @Test
    public void valueNull() throws Exception {
        monitor(() -> signal("1", "2").startWith((String) null));

        assert result.value(null, "1", "2");
    }

    @Test
    public void valuesNull() throws Exception {
        monitor(() -> signal("1", "2").startWith((String[]) null));

        assert result.value("1", "2");
    }

    @Test
    public void iterable() throws Exception {
        monitor(() -> signal(1, 2).startWith(list(-1, 0)));

        assert result.value(-1, 0, 1, 2);
    }

    @Test
    public void iterableNull() throws Exception {
        monitor(() -> signal(1, 2).startWith((Iterable) null));

        assert result.value(1, 2);
    }

    @Test
    public void stream() throws Exception {
        monitor(() -> signal(1, 2).startWith(stream(-1, 0)));

        assert result.value(-1, 0, 1, 2);
    }

    @Test
    public void streamNull() throws Exception {
        monitor(() -> signal(1, 2).startWith((BaseStream) null));

        assert result.value(1, 2);
    }
}
