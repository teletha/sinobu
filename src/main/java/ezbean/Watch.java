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

import static java.nio.file.StandardWatchEventKind.*;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

/**
 * @version 2011/04/08 16:05:45
 */
class Watch implements Disposable, Runnable {

    /** The actual file system watch system. */
    final WatchService service;

    /** The user speecified event listener. */
    final FileListener listener;

    /** The pattern matching utility. */
    final Visitor visitor;

    /**
     * @param directory
     * @param listener
     */
    Watch(Path directory, FileListener listener, String... patterns) {
        this.listener = listener;
        this.visitor = new Visitor(directory, null, 6, null, patterns);

        try {
            this.service = directory.getFileSystem().newWatchService();

            // register
            register(directory);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Helper
     * </p>
     * 
     * @param path
     */
    private void register(Path path) throws Exception {
        List<Path> list = I.walkDirectory(path);
        list.add(path);

        for (Path directory : list) {
            directory.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        }
    }

    /**
     * @see ezbean.Disposable#dispose()
     */
    @Override
    public void dispose() {
        try {
            service.close();
        } catch (IOException e) {
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
                    // OMFG!!! Kind is not enum!!! So we convert it int value for usability.
                    // 0 : CREATE
                    // 1 : DELETE
                    // 2 : MODIFY
                    Kind kind = event.kind();
                    int type = kind == ENTRY_CREATE ? 0 : kind == ENTRY_DELETE ? 1 : 2;

                    // make current modified path
                    Path path = ((Path) key.watchable()).resolve((Path) event.context());

                    // pattern matching
                    if (visitor.accept(visitor.from.relativize(path))) {
                        switch (type) {
                        case 0: // CREATE
                            listener.create(path); // fire event

                            if (Files.isDirectory(path)) {
                                register(path);
                            }
                            break;

                        case 1: // DELETE
                            listener.delete(path); // fire event
                            break;

                        default: // MODIFY
                            listener.modify(path); // fire event
                            break;
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
