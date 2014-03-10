/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import kiss.I;

import org.junit.Test;

/**
 * @version 2014/03/11 2:15:34
 */
public class TransformTest {

    @Test
    public void inputNull() throws Exception {
        assert I.transform(null, int.class) == null;
        assert I.transform(null, String.class) == null;
    }

    @Test(expected = NullPointerException.class)
    public void outputNull() throws Exception {
        assert I.transform("1", null) == null;
    }

    @Test
    public void primitiveInt() {
        assert I.transform("1", int.class) == 1;
        assert I.transform(1, String.class).equals("1");
    }

    @Test
    public void primitiveLong() {
        assert I.transform("1", long.class) == 1L;
        assert I.transform(1L, String.class).equals("1");
    }

    @Test
    public void primitiveChar() {
        assert I.transform("1", char.class) == '1';
        assert I.transform('1', String.class).equals("1");
    }

    @Test
    public void primitiveFloat() {
        assert I.transform("1.3", float.class) == 1.3f;
        assert I.transform(1.3f, String.class).equals("1.3");
    }

    @Test
    public void primitiveDouble() {
        assert I.transform("1.3", double.class) == 1.3d;
        assert I.transform(1.3d, String.class).equals("1.3");
    }

    @Test
    public void primitiveBoolean() {
        assert I.transform("true", boolean.class);
        assert I.transform(true, String.class).equals("true");
    }

    @Test
    public void transformBean() {
        BeanA a = I.make(BeanA.class);
        a.setValue(10);

        // initial value
        assert 10 == a.getValue();

        // transform
        BeanB b = I.transform(a, BeanB.class);

        // initial value
        assert 10 == a.getValue();
        assert b.getValue().equals("10");
    }

    @Test
    public void date() throws Exception {
        assert I.transform(new Date(0), String.class).equals("1970-01-01T00:00:00");
        assert I.transform("1970-01-01T00:00:00", Date.class).equals(new Date(0));
    }

    @Test
    public void localDateTime() throws Exception {
        LocalDateTime local = LocalDateTime.of(2014, 3, 10, 13, 43, 56);
        String text = "2014-03-10T13:43:56";

        assert I.transform(local, String.class).equals(text);
        assert I.transform(text, LocalDateTime.class).equals(local);
    }

    @Test
    public void localDate() throws Exception {
        LocalDate local = LocalDate.of(2014, 3, 10);
        String text = "2014-03-10";

        assert I.transform(local, String.class).equals(text);
        assert I.transform(text, LocalDate.class).equals(local);
    }

    @Test
    public void localTime() throws Exception {
        LocalTime local = LocalTime.of(23, 45, 9, 765);
        String text = "23:45:09.000000765";

        assert I.transform(local, String.class).equals(text);
        assert I.transform(text, LocalTime.class).equals(local);
    }

    /**
     * @version 2011/03/15 15:09:16
     */
    protected static class BeanA {

        /** The property. */
        private int value;

        /**
         * Get the value property of this {@link TransformTest.BeanA1}.
         * 
         * @return The value property.
         */
        public int getValue() {
            return value;
        }

        /**
         * Set the value property of this {@link TransformTest.BeanA1}.
         * 
         * @param value The value value to set.
         */
        public void setValue(int value) {
            this.value = value;
        }
    }

    /**
     * @version 2011/03/15 15:09:20
     */
    protected static class BeanB {

        /** The property. */
        private String value;

        /**
         * Get the value property of this {@link TransformTest.BeanA1}.
         * 
         * @return The value property.
         */
        public String getValue() {
            return value;
        }

        /**
         * Set the value property of this {@link TransformTest.BeanA1}.
         * 
         * @param value The value value to set.
         */
        public void setValue(String value) {
            this.value = value;
        }
    }
}
