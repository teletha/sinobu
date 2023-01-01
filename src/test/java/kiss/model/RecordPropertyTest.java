/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.model;

import org.junit.jupiter.api.Test;

class RecordPropertyTest {

    @Test
    void record() {
        record Point(int x, int y) {
        }

        Model<Point> model = Model.of(Point.class);
        assert model.type == Point.class;
        assert model.properties.size() == 2;

        Property propertyX = model.property("x");
        assert propertyX.name.equals("x");
        assert propertyX.model.type == int.class;

        Property propertyY = model.property("y");
        assert propertyY.name.equals("y");
        assert propertyY.model.type == int.class;
    }

    @Test
    void generic() {
        record Generic<T> (T value) {
        }

        Model<Generic> model = Model.of(Generic.class);
        assert model.type == Generic.class;
        assert model.properties.size() == 1;

        Property propertyX = model.property("value");
        assert propertyX.name.equals("value");
        assert propertyX.model.type == Object.class;
    }
}