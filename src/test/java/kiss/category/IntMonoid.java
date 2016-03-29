/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.category;

/**
 * @version 2016/03/29 14:27:09
 */
class IntMonoid implements Monoid<Integer> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer empty() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer append(Integer one, Integer other) {
        return one + other;
    }
}
