/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy;

/**
 * @version 2015/04/28 11:24:11
 */
public abstract class ModelOperator<M, V> implements Lens<M, V> {

    /** The parent lens. */
    protected Lens<M, V> parent;

    /**
     * @param parent A previous lens.
     */
    protected ModelOperator(Lens<M, V> parent) {
        this.parent = parent == null ? Lens.Φ : parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final V get(M model) {
        return parent.get(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final M set(M model, V property) {
        return parent.set(model, property);
    }
}
