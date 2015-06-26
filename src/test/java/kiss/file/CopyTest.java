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
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.CRC32;

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
    public void fileToFile() throws Exception {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateFile("Out", "This text will be overwritten by input file.");
        I.copy(in, out);

        assert sameFile(in, out);
    }

    @Test
    public void fileToFileWithSameTimeStamp() throws Exception {
        Instant now = Instant.now();
        Path in = room.locateFile("In", now, "Success");
        Path out = room.locateFile("Out", now, "This text will be overwritten by input file.");
        I.copy(in, out);

        assert sameFile(in, out);
    }

    @Test
    public void fileToFileWithDifferentTimeStamp() {
        Instant now = Instant.now();
        Path in = room.locateFile("In", now, "Success");
        Path out = room.locateFile("Out", now.plusSeconds(10), "This text will be overwritten by input file.");
        I.copy(in, out);

        assert sameFile(in, out);
    }

    @Test
    public void fileToAbsentFile() {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateAbsent("Out");

        I.copy(in, out);

        assert sameFile(in, out);
    }

    @Test
    public void fileToDirectory() {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateDirectory("Out");

        I.copy(in, out);

        assert sameFile(in, out.resolve("In"));
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

    /**
     * <p>
     * Helper method to check {@link Path} equality as file.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean sameFile(Path one, Path other) {
        assert exist(one, other);
        assert file(one, other);
        assert sameLastModified(one, other);
        try {
            System.out.println(checksum(one) + "  " + Files.readAllLines(one) + " " + one);
            System.out.println(checksum(other) + " " + Files.readAllLines(other) + " " + other);
        } catch (IOException e) {
            throw I.quiet(e);
        }
        assert checksum(one) == checksum(other);

        return true;
    }

    /**
     * <p>
     * Helper method to check {@link Path} attributes.
     * </p>
     * 
     * @param paths A path set to check.
     * @return A test result.
     */
    protected static boolean sameLastModified(Path one, Path other) {
        try {
            assert Files.getLastModifiedTime(one).equals(Files.getLastModifiedTime(other));
        } catch (Exception e) {
            throw I.quiet(e);
        }
        return true;
    }

    /**
     * <p>
     * Helper method to compute {@link Path} checksume.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static long checksum(Path path) {
        try {
            CRC32 crc = new CRC32();
            crc.update(Files.readAllBytes(path));

            return crc.getValue();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper method to check {@link Path} existence.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean exist(Path... paths) {
        for (Path path : paths) {
            assert Files.exists(path);
        }
        return true;
    }

    /**
     * <p>
     * Helper method to check {@link Path} kind.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean file(Path... paths) {
        for (Path path : paths) {
            assert Files.isRegularFile(path);
        }
        return true;
    }

    /**
     * <p>
     * Helper method to check {@link Path} kind.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean directory(Path... paths) {
        for (Path path : paths) {
            assert Files.isDirectory(path);
        }
        return true;
    }
}
