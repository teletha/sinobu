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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import ezbean.Disposable;
import ezbean.I;
import ezbean.Listeners;

/**
 * @version 2011/04/01 17:55:54
 */
class Watcher implements Runnable, Disposable {

    /** The actual file system observer. */
    private static final WatchService service;

    static {
        try {
            service = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /** The file system observer services. */
    private final CopyOnWriteArraySet<WatchService> services = new CopyOnWriteArraySet();

    private final ConcurrentHashMap<WatchKey, Path> keys = new ConcurrentHashMap();

    /** The actual listener pool. */
    private final Listeners<Path, FileListener> listeners = new Listeners();

    /**
     * @param target
     * @param listener
     * @param kinds
     * @throws Exception
     */
    void register(Path target, FileListener listener, Kind... kinds) throws Exception {
        if (!Files.isDirectory(target)) {
            target = target.getParent();
        }

        // register listener
        listeners.push(target, listener);

        // register file system observer
        keys.put(target.register(service, kinds), target);
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
                Path directory = keys.get(key);

                for (WatchEvent event : key.pollEvents()) {
                    Kind kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    for (FileListener listener : listeners.get(directory)) {
                        listener.change(event);
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
            } catch (InterruptedException x) {
                return;
            }

        }
    }

    /**
     * @see ezbean.Disposable#dispose()
     */
    @Override
    public void dispose() {
        try {
            service.close();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}