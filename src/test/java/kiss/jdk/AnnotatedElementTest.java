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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.Test;

/**
 * @version 2011/03/22 16:55:52
 */
public class AnnotatedElementTest {

    @Test
    public void cantRetrieveAnnotationFromOverriddenMethod() throws Exception {
        Method method = Child.class.getMethod("publicMethod");
        Annotation[] annotations = method.getAnnotations();
        assert 0 == annotations.length;
    }

    /**
     * @version 2010/11/06 7:40:07
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Marker {
    }

    /**
     * @version 2010/11/06 7:40:04
     */
    private static class Parent {

        @Marker
        public void publicMethod() {
        }
    }

    /**
     * @version 2010/11/06 7:40:01
     */
    private static class Child extends Parent {

        public void publicMethod() {
            super.publicMethod();
        }
    }
}
