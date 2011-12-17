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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;

/**
 * @version 2011/09/22 16:22:02
 */
public class CommandLineUserTest {

    @Rule
    public static final CommandLineUser user = new CommandLineUser();

    @Test
    public void input() throws Exception {
        user.willInput("1");
        assert "1".equals(read());
    }

    @Test
    public void inputSeparately() throws Exception {
        user.willInput("1");
        assert "1".equals(read());

        user.willInput("2");
        assert "2".equals(read());
    }

    @Test
    public void inputSequentially() throws Exception {
        user.willInput("1");
        user.willInput("2");
        assert "1".equals(read());
        assert "2".equals(read());
    }

    /**
     * Helper method to read character from user input.
     * 
     * @return
     */
    private String read() {
        try {
            return new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}
