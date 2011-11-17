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
package ezbean;

import org.junit.Test;

/**
 * @version 2011/03/22 16:27:26
 */
public class SingletonTest {

    @Test
    public void resolve() {
        SingletonClass instance1 = I.make(SingletonClass.class);
        assert instance1 != null;

        SingletonClass instance2 = I.make(SingletonClass.class);
        assert instance2 != null;
        assert instance1 == instance2;
    }

    /**
     * @version 2011/03/22 16:29:43
     */
    @Manageable(lifestyle = Singleton.class)
    private static class SingletonClass {
    }
}
