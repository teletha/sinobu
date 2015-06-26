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
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.function.BiConsumer;
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
    public void fileToFile() {
        Path in = room.locateFile("In", "Success");
        Path out = room.locateFile("Out", "This text will be overwritten by input file.");

        I.copy(in, out);

        assert same(in, out);
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

    /**
     * <p>
     * Helper method to check {@link Path} existence.
     * </p>
     * 
     * @param paths
     * @return
     */
    protected static boolean same(Path... paths) {
        return validate(paths, (one, other) -> {
            try {
                // In case of file
                if (Files.isRegularFile(one)) {
                    if (!Files.isRegularFile(other)) {
                        throw error(one, " is file.", other, " is not file.");
                    }

                    assert lastModified(one, other);
                    assert content(one, other);
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });
    }

    /**
     * <p>
     * Helper method to check {@link Path} attributes.
     * </p>
     * 
     * @param paths A path set to check.
     * @return A test result.
     */
    protected static boolean lastModified(Path... paths) {
        return validate(paths, (one, other) -> {
            try {
                BasicFileAttributes oneAttributes = Files.readAttributes(one, BasicFileAttributes.class);
                BasicFileAttributes otherAttributes = Files.readAttributes(other, BasicFileAttributes.class);

                FileTime oneTime = oneAttributes.lastModifiedTime();
                FileTime otherTime = otherAttributes.lastModifiedTime();

                if (!oneTime.equals(otherTime)) {
                    throw error(one, " is modified at " + oneTime + ".", other, " is modified at " + otherTime + ".");
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });
    }

    /**
     * <p>
     * Helper method to check {@link Path} contents by using {@link CRC32} hash.
     * </p>
     * 
     * @param paths A path set to check.
     * @return A test result.
     */
    protected static boolean content(Path... paths) {
        return validate(paths, (one, other) -> {
            try {
                CRC32 oneHash = new CRC32();
                CRC32 otherHash = new CRC32();

                oneHash.update(Files.readAllBytes(one));
                otherHash.update(Files.readAllBytes(other));

                if (oneHash.getValue() != otherHash.getValue()) {
                    throw error(one, " has " + oneHash.getValue() + " hash.", other, " has " + otherHash
                            .getValue() + " hash.");
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        });
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
     * Helper method to validate paths each other.
     * </p>
     * 
     * @param paths A path set to validate.
     * @param validation A validation rule.
     * @return
     */
    private static boolean validate(Path[] paths, BiConsumer<Path, Path> validation) {
        if (paths != null && 2 <= paths.length) {
            for (int i = 1; i < paths.length; i++) {
                validation.accept(paths[0], paths[i]);
            }
        }
        return true;
    }

    /**
     * <p>
     * Create {@link AssertionError}.
     * </p>
     * 
     * @param one
     * @param oneMessage
     * @param other
     * @param otherMessage
     * @return
     */
    private static AssertionError error(Path one, String oneMessage, Path other, String otherMessage) {
        StringBuilder builder = new StringBuilder("\r\n");
        builder.append("Path[").append(one).append("] ").append(oneMessage).append("\r\n");
        builder.append("Path[").append(other).append("] ").append(otherMessage).append("\r\n");

        return new AssertionError(builder.toString());
    }
}
