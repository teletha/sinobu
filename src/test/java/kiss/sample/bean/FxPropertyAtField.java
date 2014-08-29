/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.sample.bean;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import kiss.I;

/**
 * @version 2014/07/21 21:03:05
 */
public class FxPropertyAtField {

    public final StringProperty string = new SimpleStringProperty();

    public final IntegerProperty integer = new SimpleIntegerProperty();

    public final ListProperty<String> list = I.make(ListProperty.class);

    public final MapProperty<String, Long> map = I.make(MapProperty.class);
}
