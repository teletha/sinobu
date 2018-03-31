/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import filer.Filer;

/**
 * @version 2017/05/02 18:25:05
 */
public class StorableTest {

    private Path temp = Filer.locateTemporary();

    @Test
    public void readFromNotExist() throws Exception {
        Some instance = new Some();
        Path path = Paths.get(instance.locate());
        assert Files.notExists(path);

        instance.restore();
        assert instance.value == null;
        assert instance.valueWithDefault == "default";
        assert Files.notExists(path);
    }

    @Test
    public void readFromSizeZero() throws Exception {
        Some instance = new Some();
        Path path = Paths.get(instance.locate());
        Files.createDirectories(path.getParent());
        Files.createFile(path);
        assert Files.exists(path);
        assert Files.size(path) == 0;

        instance.restore();
        assert instance.value == null;
        assert instance.valueWithDefault == "default";
        assert Files.exists(path);
        assert Files.size(path) == 0;
    }

    @Test
    public void writeToNotExist() throws Exception {
        Some instance = new Some();
        Path path = Paths.get(instance.locate());
        assert Files.notExists(path);

        instance.store();
        assert Files.exists(path);
    }

    @Test
    public void writeToSizeZero() throws Exception {
        Some instance = new Some();
        Path path = Paths.get(instance.locate());
        Files.createDirectories(path.getParent());
        Files.createFile(path);
        assert Files.exists(path);
        assert Files.size(path) == 0;

        instance.store();
        assert Files.exists(path);
        assert Files.size(path) != 0;
    }

    /**
     * @version 2017/03/29 10:49:49
     */
    private class Some implements Storable<Some> {

        public String value;

        public String valueWithDefault = "default";

        /**
         * {@inheritDoc}
         */
        @Override
        public String locate() {
            return temp.resolve(Some.class.getSimpleName()).toString();
        }
    }
}
