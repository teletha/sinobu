/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.unit;

import java.io.File;
import java.io.FileReader;
import java.security.AccessControlException;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2010/02/09 11:23:23
 */
public class SandboxTest {

    @Rule
    public static Sandbox sandbox = new Sandbox(Sandbox.READ);

    @Test
    public void write1() throws Exception {
        sandbox.readable(true);

        new FileReader(new File("pom.xml"));
    }

    @Test(expected = AccessControlException.class)
    public void write2() throws Exception {
        new FileReader(new File("pom.xml"));
    }
}
