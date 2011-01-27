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

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @version 2010/01/16 18:44:06
 */
public class DefaultLocaleTest {

    /** preserve. */
    private static Locale locale;

    @BeforeClass
    public static void before() {
        locale = Locale.getDefault();
    }

    @AfterClass
    public static void after() {
        Locale.setDefault(locale);
    }

    @Test
    public void defaultLocale() throws Exception {
        assertEquals(Locale.getDefault(), I.make(Locale.class));
    }

    @Test
    public void changeDefaultLocale() throws Exception {
        Locale.setDefault(Locale.CANADA);
        assertEquals(Locale.CANADA, I.make(Locale.class));

        Locale.setDefault(Locale.JAPAN);
        assertEquals(Locale.JAPAN, I.make(Locale.class));

        Locale.setDefault(Locale.ROOT);
        assertEquals(Locale.ROOT, I.make(Locale.class));
    }
}
