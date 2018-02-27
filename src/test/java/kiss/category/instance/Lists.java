/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.category.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kiss.category.Monoid;

/**
 * @version 2016/03/29 14:33:12
 */
class Lists implements Monoid<List> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List empty() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List append(List one, List other) {
        List list = new ArrayList();
        list.addAll(one);
        list.addAll(other);

        return list;
    }
}
