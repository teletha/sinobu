/*
 * Copyright (C) 2012 Nameless Production Committee.
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
package ezunit;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2012/01/10 9:53:52
 */
public class PowerAssertTest {

    @Rule
    public static final PowerAssert test = new PowerAssert();

    @Test
    @Ignore
    public void testname() throws Exception {
        int value = 2;
        assert 1 == value;
    }

    public void teaaa() {
        int value = 2;
        float te = 22;
        assert value != te;
        assert value != 5;
    }
}
