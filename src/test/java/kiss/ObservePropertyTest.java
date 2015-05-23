/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.junit.Test;

/**
 * @version 2014/07/22 22:18:59
 */
public class ObservePropertyTest {

    @Test
    public void observableValueString() throws Exception {
        StringProperty property = new SimpleStringProperty();
        EventFacade<String, String> facade = new EventFacade(property);
        assert facade.retrieve() == null;

        // change property
        property.set("fire");
        assert facade.retrieve().equals("fire");

        // dipose
        assert facade.dispose();

        // change property
        property.set("change");
        assert facade.retrieve() == null;
    }

    @Test
    public void observableValueInt() throws Exception {
        IntegerProperty property = new SimpleIntegerProperty();
        EventFacade<Number, Number> facade = new EventFacade(property);
        assert facade.retrieve().intValue() == 0;

        // change property
        property.set(10);
        assert facade.retrieve().intValue() == 10;

        // dipose
        assert facade.dispose();

        // change property
        property.set(20);
        assert facade.retrieve() == null;
    }

    @Test
    public void observable() throws Exception {
        IntegerProperty property = new SimpleIntegerProperty();
        EventFacade<Number, Number> facade = new EventFacade(property);
        assert facade.retrieve().intValue() == 0;

        // change property
        property.set(10);
        assert facade.retrieve().intValue() == 10;
    }
}
