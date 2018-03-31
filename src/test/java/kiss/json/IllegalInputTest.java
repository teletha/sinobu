/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import java.io.Reader;

import org.junit.jupiter.api.Test;

import antibug.ExpectThrow;
import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2018/03/31 23:12:52
 */
public class IllegalInputTest {

    /** The normal bean. */
    private Person bean = I.make(Person.class);

    @ExpectThrow(NullPointerException.class)
    public void readNullCharSequence() {
        I.read((CharSequence) null, bean);
    }

    @ExpectThrow(NullPointerException.class)
    public void readNullReader() {
        I.read((Reader) null, bean);
    }

    @ExpectThrow(NullPointerException.class)
    public void readNullOutput() throws Exception {
        I.read("{\"age\":\"15\"}", (Object) null);
    }

    @Test
    public void readInvalidOutputBean() throws Exception {
        Class clazz = I.read("{\"age\":\"15\"}", Class.class);
        assert clazz == Class.class;
    }

    @ExpectThrow(IllegalStateException.class)
    public void readEmpty() {
        assert I.read("", bean) != null;
    }

    @ExpectThrow(IllegalStateException.class)
    public void readInvalid() {
        assert I.read("@", bean) != null;
    }

    @ExpectThrow(NullPointerException.class)
    public void writeNullAppendable() {
        I.write(bean, (Appendable) null);
    }

    @ExpectThrow(NullPointerException.class)
    public void writeNullJavaObject() {
        I.write(null, new StringBuilder());
    }

}
