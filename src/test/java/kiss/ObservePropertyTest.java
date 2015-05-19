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
        EventFacade<String> emitter = new EventFacade();

        // observe
        Disposable disposer = I.observe(property).to(emitter);
        assert emitter.retrieve() == null;

        // change property
        property.set("fire");
        assert emitter.retrieve().equals("fire");

        // dipose
        disposer.dispose();

        // change property
        property.set("change");
        assert emitter.isEmpty();
    }

    @Test
    public void observableValueInt() throws Exception {
        IntegerProperty property = new SimpleIntegerProperty();
        EventFacade<Number> emitter = new EventFacade();

        // observe
        Disposable disposer = I.observe(property).to(emitter);
        assert emitter.retrieve().intValue() == 0;

        // change property
        property.set(10);
        assert emitter.retrieve().intValue() == 10;

        // dipose
        disposer.dispose();

        // change property
        property.set(20);
        assert emitter.isEmpty();
    }

    @Test
    public void observable() throws Exception {
        IntegerProperty property = new SimpleIntegerProperty();
        EventFacade<Number> emitter = new EventFacade();

        // observe
        Disposable disposer = I.observe(property).to(emitter);
        assert emitter.retrieve().intValue() == 0;

        // change property
        property.set(10);
        assert emitter.retrieve().intValue() == 10;
    }
}
