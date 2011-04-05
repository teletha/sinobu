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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;

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

    /** The watching directory. */
    private Path directory;

    private Path file;

    /** The event listener. */
    private FileListener listener;

    /** The actual watching system. */
    private WatchKey key;

    /**
     * @param path
     */
    Watcher(Path path, FileListener listener) {
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

        try {
            if (!Files.isDirectory(path)) {
                this.file = path;
                this.directory = path.getParent();
                this.key = path.getParent().register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                listeners.push(path.getParent(), this);
            } else {
                this.file = null;
                this.directory = path;
                this.key = path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                listeners.push(path, this);

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

                    if (current == latest) {
                        continue; // skip sequential same events
                    }

                    latest = current; // record current event

                    // Retrieve observing directory

                    for (Watcher watcher : listeners.get(directory)) {
                        if (watcher.file != null && !watcher.file.equals(path)) {
                            continue;
                        }

                        if (kind == ENTRY_CREATE) {
                            watcher.listener.create(directory.resolve(context));
                        } else if (kind == ENTRY_DELETE) {
                            watcher.listener.delete(directory.resolve(context));
                        } else if (kind == ENTRY_MODIFY) {
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
        listeners.pull(directory, this);

        if (listeners.get(directory).size() == 0) {
            key.cancel();
        }
    }
}