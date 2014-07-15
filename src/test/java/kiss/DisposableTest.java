/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import org.junit.Test;

/**
 * @version 2014/01/31 16:40:27
 */
public class DisposableTest {

    @Test
    public void and() throws Exception {
        End end1 = new End();
        End end2 = new End();
        Disposable composed = end1.and(end2);

        assert end1.disposed == false;
        assert end2.disposed == false;

        composed.dispose();
        assert end1.disposed == true;
        assert end2.disposed == true;
    }

    /**
     * @version 2014/01/31 16:41:02
     */
    private static class End implements Disposable {

        private boolean disposed;

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            disposed = true;
        }
    }
}
