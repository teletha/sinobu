/*
 * Copyright (C) 2023 The SINOBU Development Team
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    void autoMapProperty() {
        AutoRoot root = new AutoRoot();

        Auto child = new Auto();
        child.text.set("save automatically");
        root.putAndSave("1", child);

        // restore saved data
        AutoRoot other = new AutoRoot();
        Auto otherChild = other.map.get("1");
        assert otherChild.text.is("save automatically");

        // update and save
        otherChild.text.set("restored child can save automatically too");

        // restore saved data
        AutoRoot another = new AutoRoot();
        Auto anotherChild = another.map.get("1");
        assert anotherChild.text.is("restored child can save automatically too");
    }

    @Test
    void autoListProperty() {
        AutoRoot root = new AutoRoot();

        Auto child = new Auto();
        child.text.set("save automatically");
        root.addAndSave(child);

        // restore saved data
        AutoRoot other = new AutoRoot();
        Auto otherChild = other.list.get(0);
        assert otherChild.text.is("save automatically");

        // update and save
        otherChild.text.set("restored child can save automatically too");

        // restore saved data
        AutoRoot another = new AutoRoot();
        Auto anotherChild = another.list.get(0);
        assert anotherChild.text.is("restored child can save automatically too");
    }

    /**
     * 
     */
    private class AutoRoot implements Storable<AutoRoot> {

        public Map<String, Auto> map = new HashMap();

        public List<Auto> list = new ArrayList();

        private AutoRoot() {
            restore().auto();
        }

        private void putAndSave(String key, Auto child) {
            map.put(key, child);
            store().auto();
        }

        private void addAndSave(Auto child) {
            list.add(child);
            store().auto();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Disposable auto() {
            return auto(Function.<Signal> identity());
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