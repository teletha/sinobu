/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.jdk;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FinalFieldModificationTest {

    @Test
    void clazz() throws Exception {
        Final instance = new Final("final", 10);
        assert instance.text.equals("final");
        assert instance.primitive == 10;

        Field field = Final.class.getDeclaredField("text");
        field.setAccessible(true);
        field.set(instance, "modified");
        assert instance.text.equals("modified"); // OMG

        field = Final.class.getDeclaredField("primitive");
        field.setAccessible(true);
        field.set(instance, 20);
        assert instance.primitive == 20; // OMG
    }

    private static class Final {

        private final String text;

        private final int primitive;

        private Final(String text, int primitive) {
            this.text = text;
            this.primitive = primitive;
        }
    }

    @Test
    void record() throws Exception {
        Rec instance = new Rec("final", 10);
        assert instance.text.equals("final");
        assert instance.primitive == 10;

        Field field = Rec.class.getDeclaredField("text");
        field.setAccessible(true);
        Assertions.assertThrows(IllegalAccessException.class, () -> field.set(instance, "FAIL!"));
    }

    public record Rec(String text, int primitive) {
    }
}