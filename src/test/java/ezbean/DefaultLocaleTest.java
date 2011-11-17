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

import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @version 2011/03/22 16:43:38
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
        assert Locale.getDefault() == I.make(Locale.class);
    }

    @Test
    public void changeDefaultLocale() throws Exception {
        Locale.setDefault(Locale.CANADA);
        assert Locale.CANADA == I.make(Locale.class);

        Locale.setDefault(Locale.JAPAN);
        assert Locale.JAPAN == I.make(Locale.class);

        Locale.setDefault(Locale.ROOT);
        assert Locale.ROOT == I.make(Locale.class);
    }
}
