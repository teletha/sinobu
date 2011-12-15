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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import org.junit.Test;

/**
 * @version 2011/12/15 18:21:34
 */
public class MethodHandleTest {

    @Test
    public void field() throws Throwable {
        MethodHandle setter = MethodHandles.lookup().unreflectSetter(Tihayahuru.class.getDeclaredField("name"));
        MethodHandle getter = MethodHandles.lookup().unreflectGetter(Tihayahuru.class.getDeclaredField("name"));

        Tihayahuru object = new Tihayahuru();
        assert object.name == null;

        setter.invoke(object, "test");
        assert object.name.equals("test");
        assert getter.invoke(object).equals("test");
    }

    /**
     * @version 2011/12/15 18:22:12
     */
    private static class Tihayahuru {

        public String name;
    }
}
