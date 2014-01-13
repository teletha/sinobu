/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * @version 2014/01/13 15:08:57
 */
class Event implements WatchEvent<Path> {

    /** The actual event. */
    private final WatchEvent<Path> event;

    /** The resolved path. */
    private final Path path;

    /**
     * @param event
     * @param path
     */
    protected Event(WatchEvent event, Path path) {
        this.event = event;
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    public java.nio.file.WatchEvent.Kind<Path> kind() {
        return event.kind();
    }

    /**
     * {@inheritDoc}
     */
    public int count() {
        return event.count();
    }

    /**
     * {@inheritDoc}
     */
    public Path context() {
        return path;
    }
}