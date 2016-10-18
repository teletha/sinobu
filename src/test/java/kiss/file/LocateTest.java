/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.file;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.AntiBug;
import antibug.CleanRoom;
import kiss.I;

/**
 * @version 2016/10/18 15:46:47
 */
public class LocateTest {

    @Rule
    @ClassRule
    public static final CleanRoom room = new CleanRoom("file");

    @Test
    public void locateRelative() {
        Path path = I.locate("context");
        assert !path.isAbsolute();
    }

    @Test
    public void locateAbsolute() {
        Path path = I.locate(room.root.toAbsolutePath().toString());
        assert path.isAbsolute();
    }

    @Test
    public void locateWhitespace() throws MalformedURLException {
        Path path = I.locate(new URL("file://white space"));
        assert !path.isAbsolute();
    }

    @Test
    public void locateTemporary() {
        Path path = I.locateTemporary();
        assert !Files.exists(path);
        assert !Files.isDirectory(path);
        assert !Files.isRegularFile(path);
    }

    @Test
    public void locateTemporaries() {
        Path path1 = I.locateTemporary();
        Path path2 = I.locateTemporary();
        Path path3 = I.locateTemporary();
        assert !Files.exists(path1);
        assert !Files.exists(path2);
        assert !Files.exists(path3);
        assert path1.getFileName() != path2.getFileName();
        assert path3.getFileName() != path2.getFileName();
        assert path3.getFileName() != path1.getFileName();
    }

    @Test
    public void locateArchive() {
        Path archive = I.locate(LocateTest.class);
        assert archive != null;
        assert Files.exists(archive);
        assert Files.isDirectory(archive);
    }

    @Test
    public void locateResource() {
        Path resource = I.locate(LocateTest.class, LocateTest.class.getSimpleName() + ".class");
        assert resource != null;
        assert Files.exists(resource);
        assert Files.isRegularFile(resource);
    }

    @Test
    public void locateResourceInJar() {
        Path resource = I.locate(AntiBug.class, AntiBug.class.getSimpleName() + ".class");
        assert resource != null;
        assert Files.exists(resource);
        assert Files.isRegularFile(resource);
    }

    @Test
    public void locateArchiveByJDKClass() {
        Path archive = I.locate(Map.class);
        assert archive == null;
    }
}
