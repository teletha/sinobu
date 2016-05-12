/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import org.junit.Test;

/**
 * @version 2016/05/12 9:25:41
 */
public class TableTest {

    @Test
    public void find() {
        Table<String, Integer> table = new Table();
        table.push("a", 1);
        assert table.find("a") == 1;
    }
}
