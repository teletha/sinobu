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

import java.util.Optional;

/**
 * @version 2015/04/17 22:25:48
 */
public interface Lens<Model, Property> {

    Optional<Property> get(Model model);

    Model set(Model model, Property property);

    default <NestedProperty> Lens<Model, NestedProperty> then(Lens<Property, NestedProperty> lens) {
        return new ChainLens(this, lens);
    }

    static <Model, Property> Model set(Model model, Lens<Model, Property> lens, Property property) {
        return lens.set(model, property);
    }

    static <Model, Property1, Property2> Model set(Model model, Lens<Model, Property1> lens1, Lens<Property1, Property2> lens2, Property2 property) {
        return set(model, lens1.then(lens2), property);
    }

    static <Model, Property1, Property2, Property3> Model set(Model model, Lens<Model, Property1> lens1, Lens<Property1, Property2> lens2, Lens<Property2, Property3> lens3, Property3 property) {
        return set(model, lens1.then(lens2).then(lens3), property);
    }

    static <Model, Property1, Property2> Model set(Model model, Setter<Model, Property1> lens1, Setter<Model, Property2> lens2) {

        return null;
    }
}
