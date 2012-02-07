/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import static kiss.xml.Element.*;

import org.junit.Test;

/**
 * @version 2012/02/07 11:15:35
 */
public class ElementManipulationTest {

    @Test
    public void append() throws Exception {
        String xml = "<m><Q/></m>";

        assert $(xml).find("Q").append("<R/><R/>").find("R").size() == 2;
    }
}
