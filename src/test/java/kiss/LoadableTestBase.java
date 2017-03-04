/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.io.IOException;
import java.net.URLClassLoader;

import org.junit.After;
import org.junit.Before;

/**
 * @version 2017/03/04 16:41:48
 */
public abstract class LoadableTestBase {

    private final Class base;

    private URLClassLoader loaded;

    /**
     * 
     */
    public LoadableTestBase() {
        this(null);
    }

    /**
     * 
     */
    public LoadableTestBase(Class base) {
        this.base = base == null ? getClass() : base;
    }

    @Before
    public final void loadClasses() {
        loaded = I.load(base, true);
    }

    /**
     * <p>
     * Unload all loaded classes.
     * </p>
     */
    @After
    public final void unloadClasses() {
        try {
            if (loaded != null) {
                loaded.close();
                loaded = null;
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}
