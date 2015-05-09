/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.scratchpad;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @version 2014/09/23 9:57:39
 */
public class ParameterPassingStyleTest {

    @Test
    @Ignore
    public void param() {
        StringParam create = SinobuScratchpad.make(StringParam::new, "test");
    }

    /**
     * @version 2014/09/23 9:58:06
     */
    private static class StringParam {

        private final String param;

        public StringParam(String param) {
            this.param = param;
        }
    }
}
