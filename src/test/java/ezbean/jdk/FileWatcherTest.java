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

import static ezbean.jdk.FileWatchEventType.*;
import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.SynchronousQueue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
    private EventQueue queue = new EventQueue();

    /** The disposable. */
    private Disposable disposable;

    /** The latest event. */
    private Event last;

    @Test
    public void modifyFile() throws Exception {
        Path path = room.locateFile("test");

        // observe
        observe(path);

        // modify
        write(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyFileMultiple() throws Exception {
        Path path = room.locateFile("multiple");

        // observe
        observe(path);

        // modify
        write(path);
        write(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyMultipleFilesInSameDirectory1() throws Exception {
        Path path1 = room.locateFile("sameDirectory1");
        Path path2 = room.locateFile("sameDirectory2");

        // observe
        observe(path1); // only 1

        // modify and verify
        write(path1);
        verify(path1, Modified);

        // modify
        write(path2);

    }

    @Test
    public void modifyMultipleFilesInSameDirectory() throws Exception {
        Path path1 = room.locateFile("sameDirectory1");
        Path path2 = room.locateFile("sameDirectory2");

        // observe
        observe(path1); // only 1
        observe(path2); // only 1

        // modify and verify
        write(path1);
        verify(path1, Modified);

        // modify
        write(path2);
        verify(path2, Modified);

    }

    @Test
    public void modifyMultipleFilesInSameDirectory2() throws Exception {
        Path path1 = room.locateFile("sameDirectory1");
        Path path2 = room.locateFile("sameDirectory2");

        // observe
        observe(path1); // only 1

        // modify
        write(path1);
        write(path2);

        // verify
        verify(path1, Modified);
        verify(path2, Modified);
    }

    @Before
    public void before() {
        disposable = null;
        last = null;
    }

    @After
    public void after() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        Watcher.service.close();
    }

    /**
     * <p>
     * Helper method to observe the specified path.
     * </p>
     * 
     * @param path
     */
    private void observe(Path path) {
        disposable = observe(path, queue);
    }

    /**
     * <p>
     * Helper method to write file with some data.
     * </p>
     * 
     * @param path
     */
    private void write(Path path) {
        try {
            Files.write(path, Arrays.asList("write"), I.getEncoding());
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Verify events of the specified path.
     * </p>
     * 
     * @param path
     */
    private void verify(Path path, FileWatchEventType... events) {
        for (FileWatchEventType event : events) {
            try {
                Event retrieved = queue.poll(1000, MILLISECONDS);

                if (retrieved == null) {
                    throw new AssertionError(event + " event doesn't rise in '" + path + "'.");
                } else {
                    // consume similar events
                    while (retrieved.equals(last)) {
                        retrieved = queue.poll(1000, MILLISECONDS);
                    }

                    // record as last event
                    last = retrieved;

                    // check path
                    assert Files.isSameFile(path, retrieved.path);
                }
            } catch (InterruptedException e) {
                throw I.quiet(e);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * <p>
     * Verify that any events doesn't happen.
     * </p>
     */
    private void verifyZero() {
        // consume similar events
        while (true) {
            Event retrieved;
            try {
                retrieved = queue.poll(200, MILLISECONDS);

                if (retrieved == null) {
                    return;
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * @version 2011/04/03 14:14:01
     */
    @SuppressWarnings("serial")
    private static class EventQueue extends SynchronousQueue<Event> implements FileListener {

        /**
         * 
         */
        private EventQueue() {
            super(true);
        }

        /**
         * @see ezbean.jdk.FileListener#create(java.nio.file.Path)
         */
        @Override
        public void create(Path path) {
            try {
                put(new Event(path, "create"));
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }

        /**
         * @see ezbean.jdk.FileListener#delete(java.nio.file.Path)
         */
        @Override
        public void delete(Path path) {
            try {
                put(new Event(path, "delete"));
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }

        /**
         * @see ezbean.jdk.FileListener#modify(java.nio.file.Path)
         */
        @Override
        public void modify(Path path) {
            try {
                put(new Event(path, "modify"));
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * @version 2011/04/03 15:21:50
     */
    private static class Event {

        /** The event path. */
        private final Path path;

        /** The event type. */
        private final String type;

        /**
         * @param path
         * @param type
         */
        private Event(Path path, String type) {
            this.path = path;
            this.type = type;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Event other = (Event) obj;
            if (path == null) {
                if (other.path != null) return false;
            } else if (!path.equals(other.path)) return false;
            if (type == null) {
                if (other.type != null) return false;
            } else if (!type.equals(other.type)) return false;
            return true;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Event [path=" + path + ", type=" + type + "]";
        }
    }

    /**
     * <p>
     * Observe file system.
     * </p>
     * 
     * @param target
     * @param listener
     * @return
     */
    public static Disposable observe(Path target, FileListener listener) {
        return new Watcher(target, listener);
    }
}
