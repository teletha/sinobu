/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

/**
 * @version 2015/04/17 22:48:59
 */
class ChainLens<Model, Property, Nested> implements Lens<Model, Nested> {

    private final Lens<Model, Property> before;

    private final Lens<Property, Nested> after;

    /**
     * @param before
     * @param after
     */
    ChainLens(Lens<Model, Property> before, Lens<Property, Nested> after) {
        this.before = before;
        this.after = after;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Nested get(Model model) {
        return after.get(before.get(model));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model set(Model model, Nested property) {
        return before.set(model, after.set(before.get(model), property));
    }
}
