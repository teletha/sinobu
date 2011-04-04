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
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import ezbean.Disposable;
import ezbean.I;
import ezbean.Listeners;

/**
 * @version 2011/04/01 17:55:54
 */
class Watcher implements Runnable, Disposable {

    /** The actual file system observer. */
    static WatchService service;

    /** The watching child paths. */
    static final Listeners<Path, Watcher> listeners = new Listeners();

    /** The watching path. */
    private Path path;

    private Path original;

    /** The event listener. */
    private FileListener listener;

    /** The actual watching system. */
    private WatchKey key;

    private CopyOnWriteArrayList<Watcher> children = new CopyOnWriteArrayList();

    private final boolean file;

    /**
     * @param path
     */
    Watcher(Path path, FileListener listener) {
        this.original = path;
        this.listener = listener;

        // Execute task in another thread if not running.
        if (service == null) {
            try {
                service = FileSystems.getDefault().newWatchService();
                FileWatcherTest.pool.execute(this);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }

        System.out.println("regist  " + path);

        try {
            if (!Files.isDirectory(path)) {
                this.file = true;
                this.path = path.getParent();
                this.key = path.getParent().register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                listeners.push(path.getParent(), this);
            } else {
                this.file = false;
                this.path = path;
                this.key = path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                listeners.push(path, this);

                DirectoryStream<Path> stream = Files.newDirectoryStream(path);

                for (Path child : stream) {
                    if (Files.isDirectory(child)) {
                        System.out.println("" + child);
                        children.add(new Watcher(child, listener));
                    }
                }
                stream.close();
            }
        } catch (Exception e) {
            throw I.quiet(e);
        }

    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        int latest = 0;

        while (true) {
            try {
                // wait for key to be signalled
                WatchKey key = service.take();
                System.out.println(" start new");

                for (WatchEvent event : key.pollEvents()) {
                    Kind kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    Path directory = (Path) key.watchable();
                    Path context = (Path) event.context();
                    Path path = directory.resolve(context);

                    // Some operation system rises multiple file system events against one operation
                    // from user's point of view. So, we should identify the event type and context
                    // to aggregate them.
                    int current = Objects.hash(directory, context, kind);
                    System.out.println(directory + "  " + context + "  " + kind + "  " + current + "  " + latest + "  " + this + "  " + Thread.currentThread());

                    if (current == latest) {
                        continue; // skip sequential same events
                    }
                    latest = current; // record current event

                    // Retrieve observing directory

                    for (Watcher watcher : listeners.get(directory)) {
                        if (watcher.file && !watcher.original.equals(path)) {
                            continue;
                        }

                        if (kind == ENTRY_CREATE) {
                            watcher.listener.create(directory.resolve(context));
                        } else if (kind == ENTRY_DELETE) {
                            watcher.listener.delete(directory.resolve(context));
                        } else if (kind == ENTRY_MODIFY) {
                            System.out.println(path);
                            watcher.listener.modify(directory.resolve(context));
                        }
                    }
                }

                // reset key and remove from set if directory no longer accessible
                key.reset();
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
        listeners.pull(path, this);

        if (listeners.get(path).size() == 0) {
            key.cancel();
            System.out.println("dispose child " + path + "  " + key.isValid());
        }

        for (Watcher child : children) {
            child.dispose();
        }

        children.clear();
    }
}