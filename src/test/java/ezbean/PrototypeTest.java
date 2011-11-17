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
 * @version 2011/03/22 16:26:42
 */
public class PrototypeTest {

    @Test
    public void resolve() {
        PrototypeClass instance1 = I.make(PrototypeClass.class);
        assert instance1 != null;

        PrototypeClass instance2 = I.make(PrototypeClass.class);
        assert instance2 != null;
        assert instance1 != instance2;
    }

    /**
     * @version 2011/03/22 16:30:07
     */
    @Manageable(lifestyle = Prototype.class)
    private static class PrototypeClass {
    }
}
