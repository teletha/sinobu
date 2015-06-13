/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.serialization;

import java.io.IOError;
import java.io.Reader;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import kiss.I;
import kiss.sample.bean.Person;

import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;

/**
 * @version 2011/03/31 17:29:31
 */
public class IllegalInputTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    /** The normal bean. */
    private static Person bean = I.make(Person.class);

    @Test(expected = NoSuchFileException.class)
    public void readAbsentPath() throws Exception {
        I.read(room.locateAbsent("absent"), bean);
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

    @Test(expected = IOError.class)
    public void readEmptyJSON() {
        assert I.read("", bean) != null;
    }

    @Test(expected = IOError.class)
    public void readInvalidJSON() {
        assert I.read("@", bean) != null;
    }

    @Test(expected = NullPointerException.class)
    public void writeNullAppendableJSON() {
        I.write(bean, (Appendable) null, true);
    }

    @Test(expected = NullPointerException.class)
    public void writeNullAppendableXML() {
        I.write(bean, (Appendable) null, false);
    }

    @Test(expected = NullPointerException.class)
    public void writeNullJavaObjectJSON() {
        I.write(null, new StringBuilder(), true);
    }

    @Test(expected = NullPointerException.class)
    public void writeNullJavaObjectXML() {
        I.write(null, new StringBuilder(), false);
    }

    @Test(expected = NullPointerException.class)
    public void writeNullPathJSON() {
        I.write(bean, (Path) null, true);
    }

    @Test(expected = NullPointerException.class)
    public void writeNullPathXML() {
        I.write(bean, (Path) null, false);
    }

    @Test
    public void writeAbsentPathJSON() {
        Path file = room.locateAbsent("absent");
        I.write(bean, file, true);

        assert Files.exists(file);
    }

    @Test
    public void writeAbsentPathXML() {
        Path file = room.locateAbsent("absent");
        I.write(bean, file, false);

        assert Files.exists(file);
    }

    @Test
    public void writeFileJSON() {
        Path file = room.locateFile("file");
        I.write(bean, file, true);

        assert Files.exists(file);
    }

    @Test
    public void writeFileXML() {
        Path file = room.locateFile("file");
        I.write(bean, file, false);

        assert Files.exists(file);
    }

    @Test(expected = AccessDeniedException.class)
    public void writeDirectoryJSON() {
        I.write(bean, room.locateDirectory("directory"), true);
    }

    @Test(expected = AccessDeniedException.class)
    public void writeDirectoryXML() {
        I.write(bean, room.locateDirectory("directory"), false);
    }
}
