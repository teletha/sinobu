/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.module;

import static org.junit.Assert.*;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezbean.unit.PrivateModule;

/**
 * @version 2010/02/03 21:01:59
 */
public class ClassloaderUnloadTest {

    @Rule
    public static PrivateModule module = new PrivateModule("monitoring");

    /** The memory monitor. */
    private static MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

    /** The class monitor. */
    private static ClassLoadingMXBean classLoading = ManagementFactory.getClassLoadingMXBean();

    @Test
    public void count() throws Exception {
        // use module class
        assertNotNull(I.make(module.forName("Single")));

        // reload module and execute gc if possible
        module.load();
        memory.gc();
        Thread.sleep(1000);

        // create snapshot
        int initialLoaded = classLoading.getLoadedClassCount();
        long initialUnloaded = classLoading.getUnloadedClassCount();

        // use module class
        assertNotNull(I.make(module.forName("Single")));

        // create snapshot
        int loaded = classLoading.getLoadedClassCount();
        long unloaded = classLoading.getUnloadedClassCount();
        assertEquals(2, loaded - initialLoaded);
        assertEquals(0, unloaded - initialUnloaded);

        // reload module and execute gc if possible
        module.load();
        memory.gc();
        Thread.sleep(1000);

        // create snapshot
        int lastLoaded = classLoading.getLoadedClassCount();
        long lastUnloaded = classLoading.getUnloadedClassCount();
        assertTrue(lastLoaded - loaded <= -2);
        assertTrue(2 <= lastUnloaded - unloaded);
        assertEquals(loaded - lastLoaded, lastUnloaded - unloaded);
    }

    /**
     * @version 2010/02/04 1:00:18
     */
    protected static class Single {

        private String name;

        /**
         * Get the name property of this {@link ClassloaderUnloadTest.Single}.
         * 
         * @return The name property.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link ClassloaderUnloadTest.Single}.
         * 
         * @param name The name value to set.
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}
