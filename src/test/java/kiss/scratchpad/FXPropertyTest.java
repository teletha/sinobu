/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

import javafx.beans.property.SimpleIntegerProperty;

import org.junit.Test;

/**
 * @version 2014/01/30 12:17:05
 */
public class FXPropertyTest {

    @Test
    public void proeprty() throws Exception {
        SimpleIntegerProperty property1 = new SimpleIntegerProperty(10);
        SimpleIntegerProperty property2 = new SimpleIntegerProperty(20);
        assert property1.get() == 10;
        assert property2.get() == 20;

        property1.bindBidirectional(property2);
        assert property1.get() == 20;
        assert property2.get() == 20;

        property1.set(30);
        assert property1.get() == 30;
        assert property2.get() == 30;
    }
}
