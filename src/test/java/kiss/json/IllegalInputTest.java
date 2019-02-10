/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2018/09/28 13:20:01
 */
class IllegalInputTest {

    /** The normal bean. */
    private Person bean = I.make(Person.class);

    @Test
    void readNullCharSequence() {
        assertThrows(NullPointerException.class, () -> {
            I.read((CharSequence) null, bean);
        });
    }

    @Test
    void readNullReader() {
        assertThrows(NullPointerException.class, () -> {
            I.read((Reader) null, bean);
        });
    }

    @Test
    void readNullOutput() {
        assertThrows(NullPointerException.class, () -> {
            I.read("{\"age\":\"15\"}", (Object) null);
        });
    }

    @Test
    void readInvalidOutputBean() {
        Class clazz = I.read("{\"age\":\"15\"}", Class.class);
        assert clazz == Class.class;
    }

    @Test
    void readEmpty() {
        assertThrows(IllegalStateException.class, () -> {
            assert I.read("", bean) != null;
        });
    }

    @Test
    void readInvalid() {
        assertThrows(IllegalStateException.class, () -> {
            assert I.read("@", bean) != null;
        });
    }

    @Test
    void writeNullAppendable() {
        assertThrows(NullPointerException.class, () -> {
            I.write(bean, (Appendable) null);
        });
    }

    @Test
    void writeNullJavaObject() {
        assertThrows(NullPointerException.class, () -> {
            I.write(null, new StringBuilder());
        });
    }

}
