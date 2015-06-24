/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import antibug.CleanRoom.FileSystemDSL;
import kiss.I;

/**
 * @version 2015/06/24 10:04:35
 */
public class CopyTest {

    @Rule
    public CleanRoom room = new CleanRoom();

    @Test
    public void fileToFile() {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateFile("Out", "This text will be overwritten by input file.");

        I.copy(in, out);

        assert Files.exists(in);
        System.out.println(Files.exists(out.resolveSibling("In")));
        assert read(out).equals("Success");
    }

    @Test
    public void fileToAbsentFile() {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateAbsent("Out");

        I.copy(in, out);

        assert Files.exists(in);
        assert read(out).equals("Success");
    }

    @Test
    public void fileToDirectory() {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateDirectory("Out");

        I.copy(in, out);

        assert Files.exists(in);
        assert read(out.resolve("In")).equals("Success");
    }

    @Test
    public void directoryToDirectory() {
        Consumer<FileSystemDSL> children = $ -> {
            $.file("1", "One");
        };

        Path in = room.locateDirectory("In", children);
        Path out = room.locateDirectory("Out");

        I.copy(in, out);

        assert Files.exists(in);
        assert directory(out.resolve("In"));
        assert file(out.resolve("In/1"), "One");
    }

    @Test
    public void directoryToAbsent() {
        Path in = room.locateDirectory("In", $ -> {
            $.file("1", "One");
        });
        Path out = room.locateAbsent("Out");

        I.copy(in, out);

        assert directory(out.resolve("In"));
        assert file(out.resolve("In/1"), "One");
    }

    /**
     * <p>
     * Read first line.
     * </p>
     * 
     * @param file A target file to read.
     * @return A first line text.
     */
    private static String read(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);

            return lines.isEmpty() ? "" : lines.get(0);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Check whether the specified path is directory or not.
     * </p>
     * 
     * @param path
     * @return
     */
    private static boolean directory(Path path) {
        return Files.isDirectory(path);
    }

    /**
     * <p>
     * Check whether the specified path is directory or not.
     * </p>
     * 
     * @param path
     * @return
     */
    private static boolean file(Path path, String line) {
        assert Files.isRegularFile(path);
        assert read(path).equals(line);

        return true;
    }
}
