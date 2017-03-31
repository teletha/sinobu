/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import org.junit.After;
import org.junit.Before;

/**
 * @version 2017/04/01 1:17:35
 */
public abstract class LoadableTestBase {

    private final Class base;

    private Disposable loaded;

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
        if (loaded != null) {
            loaded.dispose();
            loaded = null;
        }
    }
}
