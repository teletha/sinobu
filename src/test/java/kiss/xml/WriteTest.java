/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.xml;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import kiss.I;

/**
 * @version 2018/11/11 10:35:38
 */
class WriteTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void xml() throws Exception {
        Path file = room.locateFile("test.xml");
        I.xml("root").to(Files.newBufferedWriter(file));

        assert Files.exists(file);
        assert Files.size(file) != 0;
    }
}
