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

import static java.nio.file.StandardWatchEventKinds.*;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * @version 2011/04/08 17:40:16
 */
class Watch implements Disposable, Runnable {

    /** The actual file event notification facility. */
    final WatchService service;

    /** The user speecified event listener. */
    final PathListener listener;

    /** The pattern matching utility. */
    final Visitor visitor;

    /**
     * <p>
     * Ezbean's file event notification facility.
     * </p>
     * 
     * @param path A target directory.
     * @param listener A event listener.
     * @param visitor Name matching patterns.
     */
    Watch(Path path, PathListener listener, Visitor visitor) {
        try {
            this.listener = listener;
            this.visitor = visitor;
            this.service = path.getFileSystem().newWatchService();

            // register
            for (Path dir : I.walkDirectory(path)) {
                dir.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            }
        } catch (Exception e) {
            throw I.quiet(e);
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

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (true) {
            try {
                WatchKey key = service.take();

                for (WatchEvent event : key.pollEvents()) {
                    // make current modified path
                    Path path = ((Path) key.watchable()).resolve((Path) event.context());

                    // pattern matching
                    if (visitor.accept(visitor.from.relativize(path))) {
                        if (event.kind() == ENTRY_CREATE) {
                            listener.create(path); // fire event

                            if (Files.isDirectory(path)) {
                                for (Path dir : I.walkDirectory(path)) {
                                    dir.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                                }
                            }
                        } else if (event.kind() == ENTRY_DELETE) {
                            listener.delete(path); // fire event
                        } else {
                            listener.modify(path); // fire event
                        }
                    }
                }

                // reset key
                key.reset();
            } catch (ClosedWatchServiceException e) {
                break; // Dispose this file watching service.
            } catch (Exception e) {
                continue;
            }
        }
    }
}
