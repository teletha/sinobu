/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

class StorableTest {

    @RegisterExtension
    CleanRoom room = new CleanRoom();

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
        assert Files.notExists(path);

        Files.createDirectories(path.getParent());
        Files.createFile(path);
        assert Files.exists(path);
        assert Files.size(path) == 0;

        instance.store();
        assert Files.exists(path);
        assert Files.size(path) != 0;
    }

    /**
     * @version 2018/11/11 10:54:33
     */
    private class Some implements Storable<Some> {

        public String value;

        public String valueWithDefault = "default";

        /**
         * {@inheritDoc}
         */
        @Override
        public String locate() {
            return room.locate(Some.class.getSimpleName()).toString();
        }
    }

    @Test
    void auto() throws Exception {
        Auto instance = new Auto();

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

    @Test
    void stopAutoSave() throws Exception {
        Auto instance = new Auto();

        instance.text.set("OK");
        instance.integer.set(20);
        Thread.sleep(100);
        assert instance.count.get() == 1;

        instance.disposer.dispose();
        instance.text.set("Change");
        instance.integer.set(30);
        Thread.sleep(100);
        assert instance.count.get() == 1;
        assert instance.text.is("Change");
        assert instance.integer.is(30);
    }

    /**
     * 
     */
    private class Auto implements Storable<Auto> {

        public Variable<String> text = Variable.empty();

        public Variable<Integer> integer = Variable.empty();

        private AtomicInteger count = new AtomicInteger();

        private Disposable disposer;

        /**
         * 
         */
        private Auto() {
            disposer = auto(timing -> timing.debounce(50, MILLISECONDS).effect(() -> count.incrementAndGet()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String locate() {
            return room.locate(Auto.class.getSimpleName()).toString();
        }
    }

    @Test
    void autoMapProperty() throws Exception {
        AutoRoot root = new AutoRoot();

        Auto child1 = new Auto();
        child1.text.set("child1");
        root.put("1", child1);

        AutoRoot other = new AutoRoot();
        assert other.children.size() == 1;
    }

    /**
     * 
     */
    private class AutoRoot implements Storable<AutoRoot> {

        public Map<String, Auto> children = new HashMap();

        private AutoRoot() {
            restore().auto();
        }

        private void put(String key, Auto child) {
            children.put(key, child);
            store().auto();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Disposable auto() {
            return auto(Function.identity());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String locate() {
            return room.locate(AutoRoot.class.getSimpleName()).toString();
        }
    }
}
