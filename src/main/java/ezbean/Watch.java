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
import java.util.List;
import java.util.Map.Entry;

/**
 * @version 2011/04/07 20:31:03
 */
class Watch implements Disposable {

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

        // register
        register(directory);
    }

    void register(Path path) {
        List<Path> list = I.walkDirectory(path);
        list.add(path);

        for (Path directory : list) {
            try {
                // register
                I.watches.push(directory.register(I.service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), this);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * @see ezbean.Disposable#dispose()
     */
    @Override
    public void dispose() {
        for (Entry<WatchKey, List<Watch>> entry : I.watches.entrySet()) {
            if (entry.getValue().remove(this)) {

                if (entry.getValue().size() == 0) {
                    entry.getKey().cancel();
                }
            }
        }
    }
}
