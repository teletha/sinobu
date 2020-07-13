/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

/**
 * <p>
 * This lifestyle guarantees that only one instance of the specific class exists in Sinobu.
 * </p>
 * 
 * @param <M> A {@link Managed} class.
 * @see Prototype
 */
public class Singleton<M> extends Prototype<M> {

    /** The singleton instance. */
    protected final M instance;

    /**
     * Create Singleton instance.
     * 
     * @param modelClass A target class.
     */
    protected Singleton(Class<M> modelClass) {
        super(modelClass);

        instance = super.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M get() {
        return instance;
    }
}