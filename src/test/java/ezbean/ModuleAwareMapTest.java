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
package ezbean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @version 2011/12/19 11:07:07
 */
public class ModuleAwareMapTest {

    @Test
    public void assignable() throws Exception {
        Map<Class, Object> test1 = new HashMap();
        I.aware(test1);

        HashMap<Class, Object> test2 = new HashMap();
        I.aware(test2);

        Map test3 = new HashMap();
        I.aware(test3);

        @SuppressWarnings("unused")
        Map<Method, Object> wrong = new HashMap();
        // I.aware(wrong);
    }
}
