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
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @version 2011/04/06 17:36:35
 */
class Watch implements Disposable {

    private static ConcurrentHashMap<Path, WatchKey> keies = new ConcurrentHashMap();

    /** The registered directory path. */
    final Path directory;

    /** The user speecified event listener. */
    final FileListener listener;

    /** The pattern matching utility. */
    final Visitor visitor;

    /** The sub watchers. */
    final CopyOnWriteArrayList<Watch> children = new CopyOnWriteArrayList();

    /**
     * @param directory
     * @param parent
     */
    Watch(Path directory, Watch parent) {
        this.directory = directory;
        this.listener = parent.listener;
        this.visitor = parent.visitor;

        try {
            keies.put(directory, directory.register(I.service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY));

            // register
            I.watches.push(directory, this);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @param directory
     * @param listener
     */
    Watch(Path directory, Path root, FileListener listener, String... patterns) {
        this.directory = directory;
        this.listener = listener;
        this.visitor = new Visitor(root, null, 6, null, patterns);

        try {
            for (Path child : I.walkDirectory(directory)) {
                children.add(new Watch(child, directory, listener, patterns));
            }

            keies.put(directory, directory.register(I.service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY));

            // register
            I.watches.push(directory, this);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    void register(Path path) {

        try {
            keies.put(path, path.register(I.service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY));

            // register
            I.watches.push(path, this);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see ezbean.Disposable#dispose()
     */
    @Override
    public void dispose() {
        I.watches.pull(directory, this);

        if (I.watches.get(directory).size() == 0) {
            keies.remove(directory).cancel();
        }

        // dispose sub directories
        for (Watch watch : children) {
            watch.dispose();
        }
    }
}
