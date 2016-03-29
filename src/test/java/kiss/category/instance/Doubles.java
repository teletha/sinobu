/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.category.instance;

import kiss.category.Monoid;

/**
 * @version 2016/03/29 14:27:09
 */
class Doubles implements Monoid<Double> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Double empty() {
        return 0d;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double append(Double one, Double other) {
        return one + other;
    }
}
