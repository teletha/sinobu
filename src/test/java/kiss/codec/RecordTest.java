/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.codec;

import org.junit.jupiter.api.Test;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;
import kiss.LoadableTestBase;

class RecordTest extends LoadableTestBase {

    @Test
    void record() {
        assert I.transform(new Point(10, -10), String.class).equals("10 -10");
        assert I.transform("1 3", Point.class).equals(new Point(1, 3));
    }

    record Point(int x, int y) {
    }

    static class Codex implements Decoder<Point>, Encoder<Point> {

        @Override
        public String encode(Point value) {
            return value.x + " " + value.y;
        }

        @Override
        public Point decode(String value) {
            String[] values = value.split(" ");
            return new Point(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
        }
    }
}