/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import org.junit.jupiter.api.Test;

class RecordPropertyTest {

    @Test
    void record() {
        @SuppressWarnings("unused")
        record Point(int x, int y) {
        }

        Model<Point> model = Model.of(Point.class);
        assert model.type == Point.class;
        assert model.properties().size() == 2;

        Property propertyX = model.property("x");
        assert propertyX.name.equals("x");
        assert propertyX.model.type == int.class;

        Property propertyY = model.property("y");
        assert propertyY.name.equals("y");
        assert propertyY.model.type == int.class;
    }
}
