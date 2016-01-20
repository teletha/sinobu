/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.util.Locale;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import kiss.Extensible;
import kiss.I;
import kiss.Lifestyle;
import kiss.Manageable;
import kiss.Singleton;
import antibug.PrivateModule;

/**
 * @version 2011/03/22 16:31:23
 */
public class I18NTest {

    @Rule
    @ClassRule
    public static final PrivateModule module = new PrivateModule();

    @Test
    public void i18n() throws Exception {
        assert"メッセージ".equals(I.i18n(MessageBundle.class).message());
    }

    @Test
    public void useNotLoadedBundleClass() {
        module.unload();

        assert"message".equals(I.i18n(MessageBundle.class).message());
    }

    @Test
    public void param() throws Exception {
        assert"メッセージ10".equals(I.i18n(MessageBundle.class).messageWithParam(10));
    }

    @Test
    public void override() throws Exception {
        assert"message".equals(I.i18n(MessageBundle.class).dontOverride());
    }

    @Test(expected = NullPointerException.class)
    public void withNull() {
        I.i18n(null);
    }

    /**
     * @version 2011/03/22 16:31:29
     */
    @SuppressWarnings("unused")
    private static class OverrideDefaultLocale implements Lifestyle<Locale> {

        /**
         * @see kiss.Lifestyle#get()
         */
        @Override
        public Locale get() {
            return Locale.JAPAN;
        }
    }

    /**
     * @version 2011/03/22 16:31:32
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
     * @version 2011/03/22 16:31:35
     */
    @SuppressWarnings("unused")
    private static class MessageBundle_ja extends MessageBundle {

        @Override
        public String message() {
            return "メッセージ";
        }

        @Override
        public String messageWithParam(int number) {
            return "メッセージ" + number;
        }
    }
}
