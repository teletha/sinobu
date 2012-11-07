/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.serialization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kiss.I;
import kiss.sample.bean.StringListProperty;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;

/**
 * @version 2011/03/29 12:37:35
 */
public class XMLCuncurrentTest {

    /** The temporaries. */
    @Rule
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

    /**
     * Test method for {@link kiss.Configuration#read(java.io.File, java.lang.Object)}.
     */
    @Test
    public void testReadAndWrite1() throws Exception {
        StringListProperty bean = createBigList();

        // write
        for (int i = 0; i < 100; i++) {
            pool.execute(new Writer(bean));
        }

        // read
        try {
            Future<StringListProperty> future = pool.submit(new Reader());

            StringListProperty result = future.get();
            //
            System.out.println(result);

            assert result != null;
            assert result.getList() != null;
            assert 100000 == result.getList().size();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

    private StringListProperty createBigList() {
        List list = new ArrayList(100000);

        for (int i = 0; i < 10; i++) {
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
         * @see java.util.concurrent.Callable#call()
         */
        public StringListProperty call() throws Exception {
            System.out.println("read");

            if (Files.notExists(testFile)) {
                System.out.println("not exist");
            }
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
         * 
         * @param bean
         */
        private Writer(Object bean) {
            this.bean = bean;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                System.out.println("write");
                I.write(bean, Files.newBufferedWriter(testFile, I.$encoding), false);
                System.out.println("write end");
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }
}
