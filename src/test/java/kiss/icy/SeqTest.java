/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy;

import org.junit.Test;

/**
 * @version 2015/05/31 21:46:52
 */
public class SeqTest {

    @Test
    public void icySet() throws Exception {
        Seq<String> icy = Seq.of("one", "two").ice();
        Seq<String> modified1 = icy.set(0, "1");

        assert icy.get(0) == "one";
        assert modified1.get(0) == "1";
    }

    @Test
    public void icySetOperator() throws Exception {
        Seq<String> icy = Seq.of("one", "two").ice();

        assert icy.get(0) == "one";
    }
}
