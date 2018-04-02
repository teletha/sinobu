/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;
import kiss.I;
import kiss.sample.bean.StringListProperty;

/**
 * @version 2018/03/31 9:32:11
 */
public class CuncurrentTest {

    /** The temporaries. */
    @RegisterExtension
    static final CleanRoom room = new CleanRoom();

    /** The serialization file. */
    static final Path testFile = room.locateFile("config.xml");

    /** Thread pool for this test. */
    private ExecutorService pool;

    /**
     * Initialize all resources.
     */
    @BeforeEach
    public void init() throws Exception {
        // create new thread pool
        pool = Executors.newFixedThreadPool(12);

    }

    /**
     * Release all resources.
     */
    @AfterEach
    public void release() throws Exception {
        // shutdown all pooled threads
        pool.shutdownNow();
    }

    @Test
    public void testReadAndWrite1() throws Exception {
        int writeCount = 100;

        CountDownLatch latch = new CountDownLatch(writeCount * 2);
        StringListProperty bean = createBigList();

        // write
        for (int i = 0; i < writeCount; i++) {
            I.signal(pool.submit(new Writer(bean))).to(v -> {
                latch.countDown();
            });
            I.signal(pool.submit(new Reader())).to(result -> {
                assert result != null;
                assert result.getList() != null;
                assert 10000 == result.getList().size();
                latch.countDown();
            });
        }

        latch.await();
    }

    private StringListProperty createBigList() {
        List list = new ArrayList(10000);

        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }

        StringListProperty bean = I.make(StringListProperty.class);
        bean.setList(list);

        return bean;
    }

    /**
     * @version 2011/03/29 12:37:30
     */
    private static class Reader implements Callable<StringListProperty> {

        /**
         * {@inheritDoc}
         */
        @Override
        public StringListProperty call() throws Exception {
            return I.read(Files.newBufferedReader(testFile, StandardCharsets.UTF_8), I.make(StringListProperty.class));
        }
    }

    /**
     * @version 2011/03/29 12:37:27
     */
    private static class Writer implements Callable<Object> {

        private final Object bean;

        /**
         * Create Writer instance.
         */
        private Writer(Object bean) {
            this.bean = bean;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object call() throws Exception {
            try {
                I.write(bean, Files.newBufferedWriter(testFile, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw I.quiet(e);
            }
            return null;
        }
    }
}
