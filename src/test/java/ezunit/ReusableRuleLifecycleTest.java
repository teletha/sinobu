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
package ezunit;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2011/03/20 10:09:04
 */
public class ReusableRuleLifecycleTest {

    @Rule
    public static final Lifecycle rule = new Lifecycle();

    @Test
    public void notInvoked() throws Exception {
        fail("We must not invoke this test method.");
    }

    @Test
    public void notExecuted() throws Exception {
        fail("We must not invoke this test method.");
    }

    /**
     * @version 2011/03/20 10:09:44
     */
    private static final class Lifecycle extends ReusableRule {

        /**
         * @see ezunit.ReusableRule#before(java.lang.reflect.Method)
         */
        @Override
        protected void before(Method method) throws Exception {
            // error in before method
            throw new BeforeError();
        }

        /**
         * @see ezunit.ReusableRule#after(java.lang.reflect.Method)
         */
        @Override
        protected void after(Method method) {
            burkeError(BeforeError.class);
        }
    }

    /**
     * @version 2011/03/20 10:11:10
     */
    private static final class BeforeError extends Error {

        private static final long serialVersionUID = -6146166690359683917L;
    }
}
