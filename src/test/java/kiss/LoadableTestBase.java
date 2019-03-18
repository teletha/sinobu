/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
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

    @BeforeEach
    public final void loadClasses() {
        if (loaded == null) {
            loaded = Disposable.empty();
        }
        loaded.add(I.load(base));
    }

    /**
     * <p>
     * Unload all loaded classes.
     * </p>
     */
    @AfterEach
    public final void unloadClasses() {
        if (loaded != null) {
            loaded.dispose();
            loaded = null;
        }
    }

    /**
     * Load the specified extension. Expose for test.
     * 
     * @param extension
     */
    public static final <E extends Extensible> Disposable load(Class<E> extension) {
        return I.loadE(extension);
    }
}
