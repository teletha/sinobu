/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

/**
 * @version 2011/03/22 17:10:46
 */
public class StereotypeTest {

    @Test
    public void stereotype() {
        assert getAnnotation(Base.class, StereotypeAnnotation1.class).annotationType()
                .equals(StereotypeAnnotation1.class);
    }

    @Test
    public void priority() {
        assert getAnnotation(Base.class, StereotypeAnnotation2.class).name().equals("A");
    }

    /**
     * <p>
     * Returns all annotations present on this element. (Returns an array of length zero if this
     * element has no annotations.) The caller of this method is free to modify the returned array;
     * it will have no effect on the arrays returned to other callers.
     * </p>
     * 
     * @param <T> An annotation type.
     * @param element A target.
     * @return All annotations present on this element (including stereotype).
     */
    public static <T extends Annotation> Map<Class<T>, T> getAnnotations(AnnotatedElement element) {
        Map set = new HashMap();

        for (Annotation annotation : element.getAnnotations()) {
            if (!annotation.annotationType().getPackage().getName().equals("java.lang.annotation")) {
                set.putAll(getAnnotations(annotation.annotationType()));

                set.put(annotation.annotationType(), annotation);
            }
        }

        return set;
    }

    /**
     * <p>
     * Returns this element's annotation for the specified type if such an annotation is present,
     * else <code>null</code>.
     * </p>
     * 
     * @param <T> An annotation type.
     * @param element A target.
     * @param clazz A Class object corresponding to the annotation type.
     * @return This element's annotation for the specified annotation type if present on this
     *         element, else <code>null</code>.
     */
    public static <T extends Annotation> T getAnnotation(AnnotatedElement element, Class<T> clazz) {
        LinkedList<Annotation> list = new LinkedList();
        list.addAll(Arrays.asList(element.getAnnotations()));

        while (list.size() != 0) {
            Annotation annotation = list.pop();
            Class type = annotation.annotationType();

            if (type == clazz) {
                return (T) annotation;
            }

            if (!type.getPackage().getName().equals("java.lang.annotation")) {
                list.addAll(Arrays.asList(type.getAnnotations()));
            }

        }

        return null;
    }

    /**
     * @version 2010/01/14 2:17:55
     */
    @StereotypeAnnotation1
    @StereotypeAnnotation2(name = "A")
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Annotation1 {

    }

    /**
     * @version 2010/01/14 2:19:09
     */
    @StereotypeAnnotation2(name = "B")
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface StereotypeAnnotation1 {

    }

    /**
     * @version 2010/01/14 2:19:09
     */
    @StererotypeAnntation3
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface StereotypeAnnotation2 {

        String name();
    }

    /**
     * @version 2010/01/14 2:19:09
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface StererotypeAnntation3 {

    }

    /**
     * @version 2010/01/14 2:19:07
     */
    @Annotation1
    private static class Base {

    }
}
