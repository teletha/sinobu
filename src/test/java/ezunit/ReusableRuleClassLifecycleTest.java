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

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2011/03/20 9:26:12
 */
public class ReusableRuleClassLifecycleTest {

    @Rule
    public static final ClassLifeCycle rule = new ClassLifeCycle();

    @Test
    public void notInvoked() throws Exception {
        throw new AssertionError("We must not invoke this test method.");
    }

    /**
     * @version 2011/03/20 9:26:32
     */
    private static final class ClassLifeCycle extends ReusableRule {

        /**
         * @see ezunit.ReusableRule#beforeClass()
         */
        @Override
        protected void beforeClass() throws Exception {
            // error in before class
            throw new BeforeClassError();
        }

        /**
         * @see ezunit.ReusableRule#afterClass()
         */
        @Override
        protected void afterClass() {
            burkeError(BeforeClassError.class);
        }
    }

    /**
     * @version 2011/03/20 10:06:58
     */
    private static class BeforeClassError extends Error {

        private static final long serialVersionUID = -8184462531495172018L;
    }
}
