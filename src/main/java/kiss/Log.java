/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * @version 2017/04/27 18:28:48
 */
class Log extends Logger {

    /**
     *
     */
    Log() {
        super("", null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHandler(Handler handler) throws SecurityException {
        handler.setFormatter(new LogFormat());
        super.addHandler(handler);
    }
}
