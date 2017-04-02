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
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import javax.script.ScriptException;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2017/04/02 21:35:11
 */
public class IllegalInputTest {

    @Rule
    @ClassRule
    public static final CleanRoom room = new CleanRoom();

    /** The normal bean. */
    private Person bean = I.make(Person.class);

    @Test(expected = NoSuchFileException.class)
    public void readAbsentPath() throws Exception {
        I.read(room.locateAbsent("absent"), bean);
    }

    @Test(expected = NoSuchFileException.class)
    public void readNest() throws Exception {
        Path path = room.locateAbsent("dir/file");
        I.read(path, bean);
    }

    @Test(expected = AccessDeniedException.class)
    public void readDirectoryPath() throws Exception {
        I.read(room.locateDirectory("directory"), bean);
    }

    @Test(expected = NullPointerException.class)
    public void readNullPath() {
        I.read((Path) null, bean);
    }

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

    @Test(expected = ScriptException.class)
    public void readEmpty() {
        assert I.read("", bean) != null;
    }

    @Test(expected = ScriptException.class)
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
