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
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import kiss.I;

class TransactionalIOTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void write() throws IOException {
        Path file = sceneFileExist("base");
        I.transact(file, true, out -> Files.writeString(out, "success to write"));

        assert Files.readString(file).equals("success to write");
    }

    @Test
    void writeFailWhen() throws IOException {
        Path file = room.locateFile("test", "original");
        I.transact(file, true, out -> Files.writeString(out, "ok"));

        assert Files.readString(file).equals("ok");
    }

    private Path sceneFileExist(String content) {
        return room.locateFile("test", content);
    }
    
    private 
}
