/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.model;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.Test;

/**
 * @version 2011/12/11 20:02:56
 */
public class CollectAnnotationTest {

    @Test
    public void single() throws Exception {
        List<Annotation> annotations = ClassUtil.getAnnotation(Root.class.getDeclaredMethod("single"));
        Mark mark = (Mark) annotations.get(0);
        assert mark.value().equals("at root");
    }

    @Test
    public void multiple() throws Exception {
        List<Annotation> annotations = ClassUtil.getAnnotation(Root.class.getDeclaredMethod("multiple"));
        Mark mark = (Mark) annotations.get(0);
        Check check = (Check) annotations.get(1);
        assert mark.value().equals("at root mark");
        assert check.value().equals("at root check");
    }

    @Test
    public void collect() throws Exception {
        List<Annotation> annotations = ClassUtil.getAnnotation(Parent.class.getDeclaredMethod("single"));
        Mark mark = (Mark) annotations.get(1);
        Check check = (Check) annotations.get(0);
        assert mark.value().equals("at root");
        assert check.value().equals("at parent");
    }

    @Test
    public void override() throws Exception {
        List<Annotation> annotations = ClassUtil.getAnnotation(Parent.class.getDeclaredMethod("multiple"));
        Mark mark = (Mark) annotations.get(0);
        Check check = (Check) annotations.get(1);
        assert mark.value().equals("at parent mark");
        assert check.value().equals("at root check");
        assert annotations.size() == 2;
    }

    /**
     * @version 2011/12/11 20:04:02
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Mark {

        String value();
    }

    /**
     * @version 2011/12/11 20:04:02
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface Check {

        String value();
    }

    /**
     * @version 2011/12/11 20:03:45
     */
    @SuppressWarnings("unused")
    private static class Root {

        @Mark("at root")
        public void single() {
        }

        @Mark("at root mark")
        @Check("at root check")
        public void multiple() {
        }
    }

    /**
     * @version 2011/12/11 20:03:45
     */
    private static class Parent extends Root {

        @Check("at parent")
        public void single() {
        }

        @Mark("at parent mark")
        public void multiple() {
        }
    }
}
