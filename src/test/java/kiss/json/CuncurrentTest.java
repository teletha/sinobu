/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import kiss.I;
import kiss.sample.bean.StringListProperty;

/**
 * @version 2016/03/16 21:08:47
 */
public class CuncurrentTest {

    /** The temporaries. */
    @Rule
    @ClassRule
    public static final CleanRoom room = new CleanRoom();

    /** The serialization file. */
    private static final Path testFile = room.locateFile("config.xml");

    /** Thread pool for this test. */
    private ExecutorService pool = Executors.newFixedThreadPool(2);

    /**
     * Initialize all resources.
     */
    @Before
    public void init() throws Exception {
        // create new thread pool
        pool = Executors.newFixedThreadPool(5);

    }

    /**
     * Release all resources.
     */
    @After
    public void release() throws Exception {
        // shutdown all pooled threads
        pool.shutdownNow();
    }

    @Test
    public void testReadAndWrite1() throws Exception {
        StringListProperty bean = createBigList();

        // write
        for (int i = 0; i < 5; i++) {
            pool.execute(new Writer(bean));
        }

        // read
        StringListProperty result = pool.submit(new Reader()).get();

        assert result != null;
        assert result.getList() != null;
        assert 10000 == result.getList().size();
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
            return I.read(Files.newBufferedReader(testFile, I.$encoding), I.make(StringListProperty.class));
        }
    }

    /**
     * @version 2011/03/29 12:37:27
     */
    private static class Writer implements Runnable {

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
        public void run() {
            try {
                I.write(bean, Files.newBufferedWriter(testFile, I.$encoding));
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }
}
