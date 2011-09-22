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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * <p>
 * This is pseudo character-based user.
 * </p>
 * 
 * @version 2011/09/22 15:34:02
 */
public class CharacterUser extends ReusableRule {

    /** The mock system input. */
    private MockInputStream input;

    /** The original system output. */
    private MockOutputStream output;

    /** The original system error. */
    private MockOutputStream error;

    /**
     * @see ezunit.ReusableRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        // swap
        System.setIn(input = new MockInputStream());
        System.setOut(output = new MockOutputStream(false));
        System.setErr(error = new MockOutputStream(true));
    }

    /**
     * @see ezunit.ReusableRule#after(java.lang.reflect.Method)
     */
    @Override
    protected void after(Method method) {
        // restore original
        System.setIn(input.original);
        System.setOut(output.original);
        System.setErr(error.original);
    }

    /**
     * @param value
     */
    public void willInput(String... values) {
        for (String value : values) {
            input.deque.add(new UserInput(value));
        }
    }

    /**
     * @version 2011/09/22 16:12:01
     */
    private static class MockOutputStream extends PrintStream {

        /** The original. */
        private final PrintStream original;

        /**
         * @param original
         */
        private MockOutputStream(boolean error) {
            super(error ? System.err : System.out);

            this.original = (PrintStream) out;
        }

        /**
         * @see java.io.PrintStream#write(byte[], int, int)
         */
        @Override
        public void write(byte[] buf, int off, int len) {
            // do nothing
        }
    }

    /**
     * @version 2011/09/22 15:36:26
     */
    private static class MockInputStream extends InputStream {

        /** The original system input. */
        private final InputStream original = System.in;

        /** The user input. */
        private final Deque<UserInput> deque = new ArrayDeque();

        /**
         * @see java.io.InputStream#read()
         */
        @Override
        public int read() throws IOException {
            UserInput input = deque.peekFirst();

            if (input == null) {
                return -1;
            } else {
                int i = input.read();

                if (i == -1) {
                    deque.pollFirst();

                    return -1;
                }
                return i;
            }
        }
    }

    /**
     * @version 2011/09/22 15:38:07
     */
    private static class UserInput {

        /** The user input value. */
        private final String input;

        /** The current input index. */
        private int index = 0;

        /**
         * @param input
         */
        private UserInput(String input) {
            this.input = input;
        }

        /**
         * <p>
         * Read next input character.
         * </p>
         * 
         * @return A next input character.
         */
        private int read() {
            return index == input.length() ? -1 : input.charAt(index++);
        }
    }
}
