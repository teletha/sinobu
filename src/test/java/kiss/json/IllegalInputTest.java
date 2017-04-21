/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import java.io.Reader;

import org.junit.Test;

import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2017/04/21 11:17:27
 */
public class IllegalInputTest {

    /** The normal bean. */
    private Person bean = I.make(Person.class);

    @Test(expected = NullPointerException.class)
    public void readNullCharSequence() {
        I.read((CharSequence) null, bean);
    }

    @Test(expected = NullPointerException.class)
    public void readNullReader() {
        I.read((Reader) null, bean);
    }

    @Test(expected = NullPointerException.class)
    public void readNullOutput() throws Exception {
        I.read("{\"age\":\"15\"}", (Object) null);
    }

    @Test
    public void readInvalidOutputBean() throws Exception {
        Class clazz = I.read("{\"age\":\"15\"}", Class.class);
        assert clazz == Class.class;
    }

    @Test(expected = IllegalStateException.class)
    public void readEmpty() {
        assert I.read("", bean) != null;
    }

    @Test(expected = IllegalStateException.class)
    public void readInvalid() {
        assert I.read("@", bean) != null;
    }

    @Test(expected = NullPointerException.class)
    public void writeNullAppendable() {
        I.write(bean, (Appendable) null);
    }

    @Test(expected = NullPointerException.class)
    public void writeNullJavaObject() {
        I.write(null, new StringBuilder());
    }

}
