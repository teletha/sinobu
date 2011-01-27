/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.jdk;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.Test;

/**
 * @version 2010/11/06 7:36:14
 */
public class AnnotatedElementTest {

    @Test
    public void cantRetrieveAnnotationFromOverriddenMethod() throws Exception {
        Method method = Child.class.getMethod("publicMethod");
        Annotation[] annotations = method.getAnnotations();
        assertEquals(0, annotations.length);
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
