/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/08/03 17:29:54
 */
class I18NTest {

    @Test
    void noParam() {
        assert I.i18n(Base::noParam).equals("ok");
    }

    @Test
    void param() {
        assert I.i18n(Base::param, "!").equals("ok!");
    }

    @Test
    void params() {
        assert I.i18n(Base::params, "!", 3).equals("ok!!!");
    }

    /**
     * @version 2018/08/03 17:31:41
     */
    private static class Base implements Extensible {

        private String noParam() {
            return "ok";
        }

        private String param(String text) {
            return "ok" + text;
        }

        private String params(String text, int count) {
            StringBuilder builder = new StringBuilder("ok");
            for (int i = 0; i < count; i++) {
                builder.append(text);
            }
            return builder.toString();
        }
    }
}
