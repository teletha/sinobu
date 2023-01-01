/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

/**
 * This lifestyle guarantees that only one instance of the specific class exists in Sinobu.
 * 
 * @param <M> A {@link Managed} class.
 * @see I#prototype(Class)
 */
public class Singleton<M> implements Lifestyle<M> {

    /** The singleton instance. */
    protected final M instance;

    /**
     * Create Singleton instance.
     * 
     * @param modelClass A target class.
     */
    public Singleton(Class<? extends M> modelClass) {
        instance = I.prototype(modelClass).get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M call() {
        return instance;
    }
}