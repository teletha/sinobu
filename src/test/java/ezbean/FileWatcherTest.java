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
package ezbean;

import static ezbean.FileWatchEventType.*;
import static java.util.concurrent.TimeUnit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ezunit.CleanRoom;

/**
 * @version 2011/04/01 16:45:40
 */
public class FileWatcherTest {

    @Rule
    public static final CleanRoom room = new CleanRoom(I.locateTemporary());

    /** The file system event listener. */
    private EventQueue queue = new EventQueue();

    /** The disposable instances. */
    private List<Disposable> disposables = new ArrayList();

    @Before
    public void before() {
        disposables.clear();
    }

    @After
    public void after() {
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
    }

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
    public void modifyMultipleFilesInSameDirectory() throws Exception {
        Path path1 = room.locateFile("sameDirectory1");
        Path path2 = room.locateFile("sameDirectory2");

        // observe
        observe(path1);
        observe(path2);

        // modify and verify
        write(path1);
        verify(path1, Modified);

        // modify
        write(path2);
        verify(path2, Modified);
    }

    @Test
    public void modifyMultipleFilesInSameDirectoryButObserveOnlyOne() throws Exception {
        Path path1 = room.locateFile("sameDirectoryOnlyOne1");
        Path path2 = room.locateFile("sameDirectoryOnlyOne2");

        // observe
        observe(path1); // only 1

        // modify
        write(path1);
        write(path2);

        // verify
        verify(path1, Modified);
    }

    @Test
    public void createFile() throws Exception {
        Path path = room.locateAbsent("test");

        // observe
        observe(path);

        // create
        create(path);

        // verify events
        verify(path, Created);
    }

    @Test
    public void deleteFile() throws Exception {
        Path path = room.locateFile("test");

        // observe
        observe(path);

        // delete
        delete(path);

        // verify events
        verify(path, Deleted);
    }

    @Test
    public void modifyFileObserveFromDirectory() throws Exception {
        Path path = room.locateFile("directory/file");

        // observe root
        observe(path.getParent());

        // modify
        write(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyDirectoryObserveFromDirectory() throws Exception {
        Path path = room.locateDirectory("directory/child");

        // observe
        observe(path.getParent());

        // modify
        touch(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyFileObserveFromDeepDirectory() throws Exception {
        Path path = room.locateFile("directory/child/item/deep/file");

        // observe root
        observe(path.getParent().getParent().getParent());

        // modify
        write(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyDirectoryObserveFromDeepDirectory() throws Exception {
        Path path = room.locateDirectory("directory/child/descendant");

        // observe
        observe(path.getParent().getParent());

        // modify
        touch(path);

        // verify events
        verify(path, Modified);
    }

    @Test
    public void modifyFileInCreatedDirectory() throws Exception {
        Path directory = room.locateDirectory("directory");
        Path file = room.locateAbsent("directory/child/file");

        // observe
        observe(directory);

        // create sub directory
        Files.createDirectories(file.getParent());

        // verify events
        verify(file.getParent(), Created);

        // create file in created directory
        Files.createFile(file);

        // verify events
        verify(file, Created);
    }

    @Test(expected = NoSuchFileException.class)
    public void absent() throws Exception {
        Path path = room.locateAbsent("absent/file");

        observe(path);
    }

    /**
     * <p>
     * Helper method to observe the specified path.
     * </p>
     * 
     * @param path
     */
    private void observe(Path path) {
        disposables.add(I.observe(path, queue));
    }

    /**
     * <p>
     * Helper method to create file with no data.
     * </p>
     * 
     * @param path
     */
    private void create(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * <p>
     * Helper method to modify file attribute.
     * </p>
     * 
     * @param path
     */
    private void touch(Path path) {
        try {
            Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
        } catch (IOException e) {
            throw I.quiet(e);
        }
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
     * Helper method to delete file.
     * </p>
     * 
     * @param path
     */
    private void delete(Path path) {
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw I.quiet(e);
            }
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
                Event retrieved = queue.poll(500, MILLISECONDS);

                if (retrieved == null) {
                    throw new AssertionError(event + " event doesn't rise in '" + path + "'.");
                } else {
                    assert Files.isSameFile(path, retrieved.path) : "Expected is " + path + "   but retrieved is " + retrieved.path;
                }
            } catch (InterruptedException e) {
                throw I.quiet(e);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        verifyNone();
    }

    /**
     * <p>
     * Verify that any events doesn't happen.
     * </p>
     */
    private void verifyNone() {
        try {
            Event retrieved = queue.poll(30, MILLISECONDS);

            while (retrieved != null) {
                retrieved = queue.poll(30, MILLISECONDS);
            }
        } catch (InterruptedException e) {
            throw I.quiet(e);
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
         * @see ezbean.FileListener#create(java.nio.file.Path)
         */
        @Override
        public void create(Path path) {
            try {
                put(new Event(path, Created));
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }

        /**
         * @see ezbean.FileListener#delete(java.nio.file.Path)
         */
        @Override
        public void delete(Path path) {
            try {
                put(new Event(path, Deleted));
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }

        /**
         * @see ezbean.FileListener#modify(java.nio.file.Path)
         */
        @Override
        public void modify(Path path) {
            try {
                put(new Event(path, Modified));
            } catch (InterruptedException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * @version 2011/04/04 15:09:12
     */
    private static class Event {

        /** The event path. */
        private final Path path;

        /** The event type. */
        private final FileWatchEventType type;

        /**
         * @param path
         * @param type
         */
        private Event(Path path, FileWatchEventType type) {
            this.path = path;
            this.type = type;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
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
            if (type != other.type) return false;
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
}
