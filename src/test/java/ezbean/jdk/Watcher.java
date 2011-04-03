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

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ezbean.Disposable;
import ezbean.I;
import ezbean.Listeners;

/**
 * @version 2011/04/01 17:55:54
 */
class Watcher extends SimpleFileVisitor<Path> implements Runnable, Disposable {

    private static Executor pool = Executors.newCachedThreadPool();

    /** The actual file system observer. */
    static final WatchService service;

    static {
        try {
            service = FileSystems.getDefault().newWatchService();

            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    private static final ConcurrentHashMap<WatchKey, Watcher> keys = new ConcurrentHashMap();

    /** The actual watching path. */
    private final Path path;

    /** The user listener. */
    private final FileListener listener;

    /** The actual watching system. */
    private WatchKey key;

    private final Listeners<Path, FileListener> listeners = new Listeners();

    /**
     * @param path
     */
    Watcher(Path path, FileListener listener) {
        if (Files.notExists(path)) {
            // error
        }

        this.path = path;
        this.listener = listener;

        listeners.pull(path, listener);

        if (!Files.isDirectory(path)) {
            path = path.getParent();
        } else {
            I.walk(path, this);
        }

        try {
            key = path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            // register file system observer
            keys.put(key, this);

            // Execute task in another thread if not running.
            if (keys.size() == 1) {
                pool.execute(this);
            }
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (Files.isSameFile(dir, path)) {
            // skip root
            return FileVisitResult.CONTINUE;
        } else {

            new Watcher(dir, listener);

            return FileVisitResult.SKIP_SUBTREE;
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (true) {
            try {
                // wait for key to be signalled
                WatchKey key = service.take();

                // Retrieve observing directory.
                Watcher watcher = keys.get(key);

                // Some operation system rises multiple file system events against one operation
                // from user's point of view. So, we should identify the event type and context to
                // aggregate them.
                HashSet identity = new HashSet();

                for (WatchEvent event : key.pollEvents()) {
                    Kind kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    Path path = ((Path) key.watchable()).resolve((Path) event.context());

                    if (this.path.equals(path) && identity.add(Objects.hash(event.context(), kind))) {

                        System.out.println(path + "  " + watcher);

                        if (kind == ENTRY_CREATE) {
                            watcher.listener.create(path);
                        } else if (kind == ENTRY_DELETE) {
                            watcher.listener.delete(path);
                        } else if (kind == ENTRY_MODIFY) {
                            watcher.listener.modify(path);
                        }
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();

                if (!valid) {
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            } catch (Exception x) {
                return;
            }
        }
    }

    /**
     * @see ezbean.Disposable#dispose()
     */
    @Override
    public void dispose() {
        Watcher watcher = keys.remove(key);

        if (watcher != null) {
            listeners.remove(watcher.path, watcher.listener);
        }
        key.cancel();
    }
}