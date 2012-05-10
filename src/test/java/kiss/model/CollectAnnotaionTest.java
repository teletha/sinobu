/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.List;

import kiss.Table;

import org.junit.Test;

/**
 * @version 2012/05/10 11:17:18
 */
public class CollectAnnotaionTest {

    @Test
    public void collect() throws Exception {
        Table<Method, Annotation> table = ClassUtil.getAnnotations(Root.class);
        assert table.size() == 1;
        assert table.values().iterator().next().size() == 1;
    }

    @Test
    public void testname() throws Exception {
        Table<Method, Annotation> table = ClassUtil.getAnnotations(Child.class);
        assert table.size() == 1;

        for (List<Annotation> annotations : table.values()) {
            for (Annotation annotation : annotations) {
                System.out.println(annotation);
            }
        }
    }

    /**
     * @version 2012/05/10 11:18:04
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Marker {

        String value();
    }

    /**
     * @version 2012/05/10 11:18:33
     */
    private static class Root {

        @Marker("root")
        protected void marked() {
        }
    }

    /**
     * @version 2012/05/10 11:18:33
     */
    private static class Child extends Root {

        @Marker("root")
        @Override
        protected void marked() {
        }
    }
}
