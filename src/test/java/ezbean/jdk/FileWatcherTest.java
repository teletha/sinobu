/*
 * Copyright (C) 2011 Nameless Production Committee.
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
package ezbean.jdk;

import static java.nio.file.StandardWatchEventKind.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Rule;
import org.junit.Test;

import ezbean.Disposable;
import ezbean.I;
import ezunit.CleanRoom;

/**
 * @version 2011/04/01 16:45:40
 */
public class FileWatcherTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    /** The file system event listener. */
    private Counter watcher = new Counter();

    @Test
    public void watch() throws Exception {
        Path file = room.locateFile("test");

        // observe
        observe(file, watcher, ENTRY_MODIFY);

        // modify
        Files.write(file, Arrays.asList("test"), I.getEncoding());

    }

    /**
     * @version 2011/04/01 17:48:34
     */
    private static class Counter implements FileListener {

        /**
         * @see ezbean.jdk.FileListener#change(java.nio.file.WatchEvent)
         */
        @Override
        public void change(WatchEvent<Path> event) {
            System.out.println(event);
        }
    }

    private static Executor pool = Executors.newCachedThreadPool();

    private static Watcher task = new Watcher();

    static {
        pool.execute(task);
    }

    /**
     * <p>
     * Observe file system.
     * </p>
     * 
     * @param target
     * @param listener
     * @param kinds
     * @return
     */
    public static Disposable observe(Path target, FileListener listener, Kind... kinds) throws Exception {
        // Create file system watcher task.
        task.register(target, listener, kinds);

        // Execute task in another thread.

        // API definition
        return task;
    }
}
