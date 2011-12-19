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
package ezbean.jdk;

import java.lang.reflect.Method;
import java.util.Objects;

import org.junit.Test;

/**
 * @version 2011/12/19 0:50:10
 */
public class OverloadMethodHash {

    @Test
    public void hash() throws Exception {
        Method method1 = Target.class.getMethod("test");
        Method method2 = Target.class.getMethod("test", int.class);

        assert method1.hashCode() == method2.hashCode();
        assert Objects.hash(method1.getName(), method1.getParameterTypes()) != Objects.hash(method2.getName(), method2.getParameterTypes());
    }

    /**
     * @version 2011/12/19 0:51:07
     */
    @SuppressWarnings("unused")
    private static class Target {

        public void test() {
        }

        public void test(int index) {
        }
    }
}
