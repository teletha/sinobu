/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import kiss.Table;
import kiss.sample.annotation.RepeatableAnnotation;

import org.junit.Test;

/**
 * @version 2013/12/27 10:01:29
 */
public class CollectAnnotaionTest {

    @Test
    public void collect() throws Exception {
        Table<Method, Annotation> table = ClassUtil.getAnnotations(Root.class);

        List<Annotation> annotations = filter("marked", table);
        assert annotations.size() == 1;
        assert annotations.get(0) instanceof Marker;

        Marker marker = (Marker) annotations.get(0);
        assert marker.value().equals("root");
    }

    @Test
    public void privateMethodShouldntInheritFromSameSignatureMethod() throws Exception {
        Table<Method, Annotation> table = ClassUtil.getAnnotations(Child.class);

        List<Annotation> annotations = filter("collectable", table);
        assert annotations.size() == 1;
        assert annotations.get(0) instanceof AnotherMarker;

        AnotherMarker another = (AnotherMarker) annotations.get(0);
        assert another.value().equals("private in child");
    }

    @Test
    public void overrideMethodHasSameAnnotation() throws Exception {
        Table<Method, Annotation> table = ClassUtil.getAnnotations(Child.class);

        List<Annotation> annotations = filter("marked", table);
        assert annotations.size() == 1;
        assert annotations.get(0) instanceof Marker;

        Marker marker = (Marker) annotations.get(0);
        assert marker.value().equals("child");
    }

    @Test
    public void overrideMethodHasSameRepetableAnnotation() throws Exception {
        Table<Method, Annotation> table = ClassUtil.getAnnotations(Child.class);

        List<Annotation> annotations = filter("rootMultipleMarked", table);
        assert annotations.size() == 3;
        assert annotations.get(0) instanceof RepeatableAnnotation;
        assert annotations.get(1) instanceof RepeatableAnnotation;
        assert annotations.get(2) instanceof RepeatableAnnotation;

        RepeatableAnnotation marker = (RepeatableAnnotation) annotations.get(0);
        assert marker.value().equals("child");

        marker = (RepeatableAnnotation) annotations.get(1);
        assert marker.value().equals("root1");

        marker = (RepeatableAnnotation) annotations.get(2);
        assert marker.value().equals("root2");
    }

    @Test
    public void overrideMethodHasSameAnnotationWhichIsRepetableInChild() throws Exception {
        Table<Method, Annotation> table = ClassUtil.getAnnotations(Child.class);

        List<Annotation> annotations = filter("childMultipleMarked", table);
        assert annotations.size() == 3;
        assert annotations.get(0) instanceof RepeatableAnnotation;
        assert annotations.get(1) instanceof RepeatableAnnotation;
        assert annotations.get(2) instanceof RepeatableAnnotation;

        RepeatableAnnotation marker = (RepeatableAnnotation) annotations.get(0);
        assert marker.value().equals("child1");

        marker = (RepeatableAnnotation) annotations.get(1);
        assert marker.value().equals("child2");

        marker = (RepeatableAnnotation) annotations.get(2);
        assert marker.value().equals("root");
    }

    /**
     * <p>
     * Filter by method name.
     * </p>
     */
    private List<Annotation> filter(String methodName, Table<Method, Annotation> table) {
        for (Entry<Method, List<Annotation>> entry : table.entrySet()) {
            if (methodName.equals(entry.getKey().getName())) {
                return entry.getValue();
            }
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @version 2013/12/26 10:09:15
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    private static @interface Marker {

        String value();
    }

    /**
     * @version 2012/05/10 11:18:04
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    private static @interface AnotherMarker {

        String value();
    }

    /**
     * @version 2012/05/10 11:18:33
     */
    private static class Root {

        @Marker("root")
        protected void marked() {
        }

        @Marker("private in root")
        private void collectable() {
        }

        @RepeatableAnnotation("root1")
        @RepeatableAnnotation("root2")
        protected void rootMultipleMarked() {
        }

        @RepeatableAnnotation("root")
        protected void childMultipleMarked() {
        }
    }

    /**
     * @version 2012/05/10 11:18:33
     */
    private static class Child extends Root {

        @Marker("child")
        @Override
        protected void marked() {
        }

        @AnotherMarker("private in child")
        private void collectable() {
            // This is not override method, but parent class has same signature method.
        }

        @RepeatableAnnotation("child")
        @Override
        protected void rootMultipleMarked() {
        }

        @RepeatableAnnotation("child1")
        @RepeatableAnnotation("child2")
        @Override
        protected void childMultipleMarked() {
        }
    }
}
