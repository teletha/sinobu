/**
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.serialization;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ezbean.I;
import ezbean.io.FileSystem;
import ezbean.sample.bean.StringList;

/**
 * DOCUMENT.
 * 
 * @version 2007/07/14 19:19:53
 */
public class ConfigurationMultiThreadTest {

    /** The test file. */
    private static File testFile = new File(I.getWorkingDirectory(), "configurationMultiThreadTest.xml");

    /** Thread pool for this test. */
    private ExecutorService pool = Executors.newFixedThreadPool(2);

    /**
     * Initialize all resources.
     */
    @Before
    public void init() throws Exception {
        // create new thread pool
        pool = Executors.newFixedThreadPool(4);

    }

    /**
     * Release all resources.
     */
    @After
    public void release() throws Exception {
        // shutdown all pooled threads
        pool.shutdownNow();

        FileSystem.delete(testFile);
    }

    /**
     * Test method for {@link ezbean.Configuration#read(java.io.File, java.lang.Object)}.
     */
    @Test
    public void testReadAndWrite1() throws Exception {
        StringList bean = createBigList();

        // write
        pool.execute(new Writer(bean));

        // read
        Future<StringList> future = pool.submit(new Reader());

        StringList result = future.get();

        assertNotNull(result);
        assertNotNull(result.getList());
        assertEquals(100000, result.getList().size());
    }

    private StringList createBigList() {
        List list = new ArrayList(100000);

        for (int i = 0; i < 100000; i++) {
            list.add(i);
        }

        StringList bean = I.make(StringList.class);
        bean.setList(list);

        return bean;
    }

    /**
     * DOCUMENT.
     * 
     * @version 2007/07/14 19:37:24
     */
    private static class Reader implements Callable<StringList> {

        /**
         * @see java.util.concurrent.Callable#call()
         */
        public StringList call() throws Exception {
            return I.xml(testFile, I.make(StringList.class));
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2007/07/14 19:37:24
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
            I.xml(bean, testFile);
        }
    }
}
