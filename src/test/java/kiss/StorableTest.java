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

import static java.util.concurrent.TimeUnit.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import filer.Filer;

/**
 * @version 2018/09/07 9:32:21
 */
class StorableTest {

    private Path temp = Filer.locateTemporary();

    @Test
    void readFromNotExist() throws Exception {
        Some instance = new Some();
        Path path = Paths.get(instance.locate());
        assert Files.notExists(path);

        instance.restore();
        assert instance.value == null;
        assert instance.valueWithDefault == "default";
        assert Files.notExists(path);
    }

    @Test
    void readFromSizeZero() throws Exception {
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
    void writeToNotExist() throws Exception {
        Some instance = new Some();
        Path path = Paths.get(instance.locate());
        assert Files.notExists(path);

        instance.store();
        assert Files.exists(path);
    }

    @Test
    void writeToSizeZero() throws Exception {
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

    @Test
    void auto() throws Exception {
        Auto instance = new Auto();
        Path path = Paths.get(instance.locate());
        assert Files.notExists(path);

        instance.text.set("OK");
        instance.integer.set(20);
        Thread.sleep(100);
        assert instance.count.get() == 1;

        instance.text.set("OK");
        instance.integer.set(20);
        Thread.sleep(100);
        assert instance.count.get() == 1;

        instance.text.set("Change");
        instance.integer.set(40);
        Thread.sleep(100);
        assert instance.count.get() == 2;
    }

    /**
     * @version 2018/09/07 9:29:53
     */
    private class Auto implements Storable<Auto> {

        public Variable<String> text = Variable.empty();

        public Variable<Integer> integer = Variable.empty();

        private AtomicInteger count = new AtomicInteger();

        /**
         * 
         */
        private Auto() {
            auto(timing -> timing.debounce(50, MILLISECONDS).effect(() -> count.incrementAndGet()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String locate() {
            return temp.resolve(Auto.class.getSimpleName()).toString();
        }
    }
}
