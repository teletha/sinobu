/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.jdk;

import java.lang.reflect.Field;

import org.junit.Test;

/**
 * @version 2016/10/24 15:52:32
 */
public class FinalFieldModificationTest {

    @Test
    public void byReflection() throws Exception {
        Final instance = new Final("final");
        assert instance.text.equals("final");

        Field field = Final.class.getDeclaredField("text");
        field.setAccessible(true);
        field.set(instance, "modified");
        assert instance.text.equals("modified"); // OMG
    }

    /**
     * @version 2016/10/24 15:52:53
     */
    private static class Final {

        private final String text;

        /**
         * @param text
         */
        private Final(String text) {
            this.text = text;
        }
    }
}
