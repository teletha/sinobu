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
 * <p>
 * Versatile event wrapper.
 * </p>
 * 
 * @see Visitor#run()
 * @version 2014/01/13 15:08:57
 */
class Event implements WatchEvent {

    /** The event holder. */
    WatchEvent watch;

    /** The event holder. */
    Path path;

    /**
     * Hide constructor.
     */
    Event() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Kind kind() {
        return watch.kind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count() {
        return watch.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object context() {
        return path;
    }
}