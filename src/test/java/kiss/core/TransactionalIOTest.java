/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import kiss.I;

class TransactionalIOTest {

    private static final String FILE = "base file";

    private static final String BACKUP = "backup";

    private static final String WRITE = "success to write";

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void read() {
        Path file = withFile();

        assert read(file).equals(FILE);
    }

    @Test
    void readNoFile() {
        Path file = withoutFile();

        Assertions.assertThrows(NoSuchFileException.class, () -> read(file));
    }

    @Test
    void readImcompletedBackup() throws IOException {
        Path file = withFile();
        withImcompletedBackup();

        assert read(file).equals(FILE);
    }

    @Test
    void readCompletedBackup() throws IOException {
        Path file = withFile();
        withCompletedBackup();

        assert read(file).equals(BACKUP);
    }

    @Test
    void write() {
        Path file = withFile();

        assert write(file).equals(WRITE);
    }

    @Test
    void writeNoFile() {
        Path file = withoutFile();

        assert write(file).equals(WRITE);
    }

    @Test
    void writeImcompletedBackup() throws IOException {
        Path file = withFile();
        withImcompletedBackup();

        assert write(file).equals(WRITE);
    }

    @Test
    void writeCompletedBackup() throws IOException {
        Path file = withFile();
        withCompletedBackup();

        assert write(file).equals(WRITE);
    }

    private String read(Path path) {
        String[] contents = {null};
        I.vouch(path, true, file -> {
            contents[0] = Files.readString(file);
        });
        return contents[0];
    }

    private String write(Path path) {
        I.vouch(path, false, file -> {
            Files.writeString(file, WRITE);
        });

        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    private Path withFile() {
        return room.locateFile("test", FILE);
    }

    private Path withoutFile() {
        return room.locate("test");
    }

    private Path withImcompletedBackup() {
        try {
            Path path = room.locateFile("test.back", BACKUP);
            Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
            return path;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    private Path withCompletedBackup() {
        try {
            Path path = room.locateFile("test.back", BACKUP);
            Files.setLastModifiedTime(path, FileTime.fromMillis(0));
            return path;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}
