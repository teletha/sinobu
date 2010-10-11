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
package ezbean;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;

import ezunit.PrivateModule;

/**
 * @version 2010/01/05 19:51:10
 */
public class I18NTest {

    @Rule
    public static final PrivateModule module = new PrivateModule();

    @Test
    public void i18n() throws Exception {
        assertEquals("メッセージ", I.i18n(MessageBundle.class).message());
    }

    @Test
    public void useNotLoadedBundleClass() {
        module.unload();

        assertEquals("message", I.i18n(MessageBundle.class).message());
    }

    @Test
    public void param() throws Exception {
        assertEquals("メッセージ10", I.i18n(MessageBundle.class).messageWithParam(10));
    }

    @Test
    public void override() throws Exception {
        assertEquals("message", I.i18n(MessageBundle.class).dontOverride());
    }

    @Test(expected = NullPointerException.class)
    public void withNull() {
        I.i18n(null);
    }

    /**
     * @version 2010/01/15 18:30:11
     */
    @SuppressWarnings("unused")
    private static class OverrideDefaultLocale implements Lifestyle<Locale> {

        /**
         * @see ezbean.Lifestyle#resolve()
         */
        public Locale resolve() {
            return Locale.JAPAN;
        }
    }

    /**
     * @version 2010/01/05 19:55:34
     */
    @Manageable(lifestyle = Singleton.class)
    private static class MessageBundle implements Extensible {

        public String message() {
            return "message";
        }

        public String messageWithParam(int number) {
            return "message" + number;
        }

        public final String dontOverride() {
            return "message";
        }
    }

    /**
     * @version 2010/01/05 19:55:37
     */
    @SuppressWarnings("unused")
    private static class MessageBundle_ja extends MessageBundle {

        public String message() {
            return "メッセージ";
        }

        public String messageWithParam(int number) {
            return "メッセージ" + number;
        }
    }
}
