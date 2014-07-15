/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import kiss.I;

/**
 * @version 2011/12/13 13:19:17
 */
public class AnnotationInheritanceTest {

    @Test
    public void novalue() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        NoValue annotation = club.getClass().getDeclaredMethod("test").getAnnotation(NoValue.class);
        assert annotation != null;
    }

    @Test
    public void primitiveInt() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        PrimitiveInt annotation = club.getClass().getDeclaredMethod("test").getAnnotation(PrimitiveInt.class);
        assert annotation != null;
        assert annotation.value() == 3;
    }

    @Test
    public void primitiveDouble() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        PrimitiveDouble annotation = club.getClass().getDeclaredMethod("test").getAnnotation(PrimitiveDouble.class);
        assert annotation != null;
        assert annotation.value() == 0.2d;
    }

    @Test
    public void primitiveBoolean() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        PrimitiveBoolean annotation = club.getClass().getDeclaredMethod("test").getAnnotation(PrimitiveBoolean.class);
        assert annotation != null;
        assert annotation.value() == false;
    }

    @Test
    public void primitiveArray() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        PrimitiveArray annotation = club.getClass().getDeclaredMethod("test").getAnnotation(PrimitiveArray.class);
        assert annotation != null;
        int[] values = annotation.value();
        assert values[0] == 1;
        assert values[1] == 2;
        assert values[2] == 3;
    }

    @Test
    public void string() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        ObjectString annotation = club.getClass().getDeclaredMethod("test").getAnnotation(ObjectString.class);
        assert annotation != null;
        assert annotation.value().equals("name");
    }

    @Test
    public void clazz() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        ObjectClass annotation = club.getClass().getDeclaredMethod("test").getAnnotation(ObjectClass.class);
        assert annotation != null;
        assert annotation.value() == HalfPricerClub.class;
    }

    @Test
    public void annotation() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        ObjectAnnotation annotation = club.getClass().getDeclaredMethod("test").getAnnotation(ObjectAnnotation.class);
        assert annotation != null;
        ObjectString nest = annotation.value();
        assert nest != null;
        assert nest.value().equals("nest");
    }

    @Test
    public void enumuration() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        ObjectEnum annotation = club.getClass().getDeclaredMethod("test").getAnnotation(ObjectEnum.class);
        assert annotation != null;
        assert annotation.value() == RetentionPolicy.RUNTIME;
    }

    @Test
    public void array() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        ObjectArray annotation = club.getClass().getDeclaredMethod("test").getAnnotation(ObjectArray.class);
        assert annotation != null;
        String[] array = annotation.value();
        assert array[0].equals("1");
        assert array[1].equals("2");
        assert array[2].equals("3");
    }

    @Test
    public void arrayAnnotation() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        ObjectAnnotationArray annotation = club.getClass()
                .getDeclaredMethod("test")
                .getAnnotation(ObjectAnnotationArray.class);
        assert annotation != null;
        ObjectString[] array = annotation.value();
        assert array[0].value().equals("1");
        assert array[1].value().equals("2");
    }

    @Test
    public void arrayEnum() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        ObjectEnumArray annotation = club.getClass().getDeclaredMethod("test").getAnnotation(ObjectEnumArray.class);
        assert annotation != null;
        RetentionPolicy[] array = annotation.value();
        assert array[0] == RetentionPolicy.RUNTIME;
        assert array[1] == RetentionPolicy.CLASS;
    }

    @Test
    public void defaultValue() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        DefaultValue annotation = club.getClass().getDeclaredMethod("test").getAnnotation(DefaultValue.class);
        assert annotation != null;
        assert annotation.value().equals("def");
    }

    @Test
    public void multiValue() throws Exception {
        HalfPricerClub club = I.make(HalfPricerClub.class);
        MultiValue annotation = club.getClass().getDeclaredMethod("test").getAnnotation(MultiValue.class);
        assert annotation != null;
        assert annotation.one() == 0.1f;
        assert annotation.two() == 't';
    }

    /**
     * @version 2011/12/13 13:20:09
     */
    protected static class HalfPricerClub {

        private String name;

        @NoValue
        @PrimitiveInt(3)
        @PrimitiveDouble(0.2d)
        @PrimitiveBoolean(false)
        @PrimitiveArray({1, 2, 3})
        @ObjectString("name")
        @ObjectClass(HalfPricerClub.class)
        @ObjectAnnotation(@ObjectString("nest"))
        @ObjectEnum(RetentionPolicy.RUNTIME)
        @ObjectArray({"1", "2", "3"})
        @ObjectAnnotationArray({@ObjectString("1"), @ObjectString("2")})
        @ObjectEnumArray({RetentionPolicy.RUNTIME, RetentionPolicy.CLASS})
        @DefaultValue
        @MultiValue(one = 0.1f, two = 't')
        protected void test() {
        }

        /**
         * Get the name property of this {@link AnnotationInheritanceTest.HalfPricerClub}.
         * 
         * @return The name property.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link AnnotationInheritanceTest.HalfPricerClub}.
         * 
         * @param name The name value to set.
         */
        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface NoValue {
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface PrimitiveInt {

        int value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface PrimitiveDouble {

        double value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface PrimitiveBoolean {

        boolean value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface PrimitiveArray {

        int[] value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ObjectString {

        String value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ObjectClass {

        Class value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ObjectAnnotation {

        ObjectString value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ObjectArray {

        String[] value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ObjectEnum {

        RetentionPolicy value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ObjectAnnotationArray {

        ObjectString[] value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ObjectEnumArray {

        RetentionPolicy[] value();
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface DefaultValue {

        String value() default "def";
    }

    /**
     * @version 2011/12/13 13:20:56
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface MultiValue {

        float one();

        char two();
    }
}
