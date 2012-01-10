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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2012/01/10 19:26:11
 */
public class AgentTest {

    private static boolean called = false;

    @Rule
    public static final Agent agent = new Agent(Transformer.class);

    @Test
    public void agentable() throws Exception {
        // load new class
        new Runnable() {

            @Override
            public void run() {
            }
        }.run();

        assert called;
    }

    /**
     * @version 2012/01/02 11:17:34
     */
    private static final class Transformer implements ClassFileTransformer {

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            called = true;
            return classfileBuffer;
        }
    }
}
