/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.util.Locale;

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
        assertThrows(NullPointerException.class, () -> I.json((String) null));
    }

    @Test
    void readNullReader() {
        assertThrows(NullPointerException.class, () -> I.json((Reader) null));
    }

    @Test
    void readNullURL() {
        assertThrows(NullPointerException.class, () -> I.json((URL) null));
    }

    @Test
    void readNullURI() {
        assertThrows(NullPointerException.class, () -> I.json((URI) null));
    }

    @Test
    void readNullFile() {
        assertThrows(NullPointerException.class, () -> I.json((File) null));
    }

    @Test
    void readNullPath() {
        assertThrows(NullPointerException.class, () -> I.json((Path) null));
    }

    @Test
    void readNullInputStream() {
        assertThrows(NullPointerException.class, () -> I.json((InputStream) null));
    }

    @Test
    void readNullReadable() {
        assertThrows(NullPointerException.class, () -> I.json((Readable) null));
    }

    @Test
    void readNullHTTPRequest() {
        assertThrows(NullPointerException.class, () -> I.json((HttpRequest.Builder) null));
    }

    @Test
    void readToIncompatibleType() {
        Locale locale = I.json("{\"age\":\"15\"}").to(Locale.class);
        assert locale instanceof Locale;
        assert locale.toString().equals("");
    }

    @Test
    void readEmpty() {
        assertThrows(IllegalStateException.class, () -> I.json("").to(bean));
    }

    @Test
    void readJS() {
        Person instance = I.json("15").to(Person.class);
        assert instance != null;
        assert instance.getAge() == 0;
        assert instance.getFirstName() == null;
        assert instance.getLastName() == null;
    }

    @Test
    void readInvalidJSON() {
        assertThrows(IllegalStateException.class, () -> I.json("@").to(bean));
    }

    @Test
    void writeNullAppendable() {
        assertThrows(NullPointerException.class, () -> I.write(bean, (Appendable) null));
    }

    @Test
    void writeNullJavaObject() {
        assertThrows(NullPointerException.class, () -> I.write(null, new StringBuilder()));
    }

}