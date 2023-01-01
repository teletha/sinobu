/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kiss.I;

class QuietTest {

    @Test
    void quietWithNull() {
        I.quiet((Object) null);
    }

    @Test
    void catchException() {
        try {
            throwError();
        } catch (Exception e) {
            assert e instanceof ClassNotFoundException;
        }
    }

    /**
     * Throw error.
     */
    private void throwError() {
        try {
            throw new ClassNotFoundException();
        } catch (ClassNotFoundException e) {
            throw I.quiet(e);
        }
    }

    @Test
    void exceptionQuietly() {
        assertThrows(ClassNotFoundException.class, () -> I.quiet(new ClassNotFoundException()));
        assertThrows(UnsupportedOperationException.class, () -> I.quiet(new UnsupportedOperationException()));
        assertThrows(LinkageError.class, () -> I.quiet(new LinkageError()));
    }
}